package br.net.oncloud.servlet;
 
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
 
/**
 * Adaptado de http://today.java.net/pub/a/today/2006/10/31/combine-facelets-and-flying-saucer-renderer.html
 * 
 * @author Geraldo Massahud
 */
public class FiltroFS2Pdf implements Filter {
 
    private DocumentBuilder documentBuilder;
    private FilterConfig config;    
 
    public void init(FilterConfig config) throws ServletException {
        try {
            this.config = config;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            documentBuilder = factory.newDocumentBuilder();            
        } catch (ParserConfigurationException ex) {
            throw new ServletException(ex);
        }
    }
 
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain filterChain) throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        URL urlDocumento = new URL(request.getProtocol(), "localhost", request.getLocalPort(), request.getRequestURI());
        
        //Capture the content for this request
        ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(response);
        filterChain.doFilter(request, capContent);
        
 
        try {
            //Parse the XHTML content to a document that is readable by the XHTML renderer.
            StringReader contentReader = new StringReader(capContent.getContent());
            InputSource source = new InputSource(contentReader);
 
            ITextRenderer renderer = parse(source, urlDocumento);
            
            response.setContentType("application/pdf");
            OutputStream browserStream = response.getOutputStream();            
            renderer.createPDF(browserStream);
            return;
 
        } catch (SAXException e) {
            throw new ServletException(e);
        } catch (DocumentException e) {
            throw new ServletException(e);
        }
    }
 
    public void destroy() {
    }
 
    private synchronized ITextRenderer parse(InputSource source, URL urlDocumento) throws SAXException, IOException {                
        Document xhtmlContent = documentBuilder.parse(source);
        ITextRenderer renderer = new ITextRenderer();        
        
        renderer.setDocument(xhtmlContent, urlDocumento.toExternalForm());        
        renderer.layout();
        return renderer;
 
    }
}
 