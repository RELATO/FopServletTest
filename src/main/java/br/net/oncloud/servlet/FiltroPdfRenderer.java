package br.net.oncloud.servlet;
 
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;
 
/**
 * Adaptado de http://today.java.net/pub/a/today/2006/10/31/combine-facelets-and-flying-saucer-renderer.html
 * 
 * @author Geraldo Massahud
 */
public class FiltroPdfRenderer implements Filter {
 
    private FilterConfig config;    
     /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;
    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory = null;
    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver;

    @Override
    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.transFactory = TransformerFactory.newInstance();
        this.transFactory.setURIResolver(this.uriResolver);

        //Configure FopFactory as desired
        this.fopFactory = FopFactory.newInstance();
        this.fopFactory.setURIResolver(this.uriResolver);
        try {
            this.fopFactory.setUserConfig(new File(config.getServletContext().getRealPath("fopconf.xml")));
            fopFactory.getFontManager().setFontBaseURL("file:///"+config.getServletContext().getRealPath("")+"/fonts");
        } catch (SAXException ex) {
            throw new ServletException(ex);
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
    }
 
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain filterChain) throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        try {

            Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, response.getOutputStream());
            Transformer transformer = transFactory.newTransformer();

            //Capture the content for this request
            ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
            filterChain.doFilter(request, capContent);
        
            StringReader contentReader = new StringReader(capContent.getContent());
            Source src = new StreamSource(contentReader);

            //Start transformation and rendering process
            render(src, transformer, response);
 
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(FiltroPdfRenderer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(FiltroPdfRenderer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FOPException ex) {
            Logger.getLogger(FiltroPdfRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void destroy() {
    }

    protected void render(Source src, Transformer transformer, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent();

        //Setup output
        org.apache.commons.io.output.ByteArrayOutputStream out = new org.apache.commons.io.output.ByteArrayOutputStream();

        //Setup FOP
        Fop fop = fopFactory.newFop("application/pdf", foUserAgent, out);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(fop.getDefaultHandler());

        //Start the transformation and rendering process
        transformer.transform(src, res);

        //Return the result
        sendPDF(out.toByteArray(), response);
    }

    /** @return a new FOUserAgent for FOP */
    protected FOUserAgent getFOUserAgent() {
        FOUserAgent userAgent = fopFactory.newFOUserAgent();
        //Configure foUserAgent as desired
        return userAgent;
    }
    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

}
