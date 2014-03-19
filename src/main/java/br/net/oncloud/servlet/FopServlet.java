package br.net.oncloud.servlet;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.servlet.ServletContextURIResolver;
import org.xml.sax.SAXException;

/**
 * Example servlet to generate a PDF from a servlet.
 * <br/>
 * Servlet param is:
 * <ul>
 *   <li>fo: the path to a XSL-FO file to render
 * </ul>
 * or
 * <ul>
 *   <li>xml: the path to an XML file to render</li>
 *   <li>xslt: the path to an XSLT file that can transform the above XML to XSL-FO</li>
 * </ul>
 * <br/>
 * Example URL: http://servername/fop/servlet/FopServlet?fo=readme.fo
 * <br/>
 * Example URL: http://servername/fop/servlet/FopServlet?xml=data.xml&xslt=format.xsl
 * <br/>
 * For this to work with Internet Explorer, you might need to append "&ext=.pdf"
 * to the URL.
 * (todo) Ev. add caching mechanism for Templates objects
 */
public class FopServlet extends HttpServlet {

    private static final long serialVersionUID = -908918093488215264L;

    /** Name of the parameter used for the XSL-FO file */
    protected static final String FO_REQUEST_PARAM = "fo";
    /** Name of the parameter used for the XML file */
    protected static final String XML_REQUEST_PARAM = "xml";
    /** Name of the parameter used for the XSLT file */
    protected static final String XSLT_REQUEST_PARAM = "xslt";

    protected static final String TEMP_PREFIX = "pdfgen.tmp.";

    /** The TransformerFactory used to create Transformer instances */
    protected TransformerFactory transFactory = null;
    /** The FopFactory used to create Fop instances */
    protected FopFactory fopFactory = null;
    /** URIResolver for use by this servlet */
    protected URIResolver uriResolver;
    ServletContext ctx;

    /**
     * {@inheritDoc}
     */
    public void init() throws ServletException {
        ctx = getServletConfig().getServletContext();
        Logger.getLogger(FopServlet.class.getName()).log(Level.INFO, "Servlet context = {0}", ctx.getRealPath(""));
        //System.setProperty("user.home", ctx.getRealPath(""));
        this.uriResolver = new ServletContextURIResolver(getServletContext());
        this.transFactory = TransformerFactory.newInstance();
        this.transFactory.setURIResolver(this.uriResolver);
        //Configure FopFactory as desired
        this.fopFactory = FopFactory.newInstance();
        this.fopFactory.setURIResolver(this.uriResolver);
        try {
            this.fopFactory.setUserConfig(new File(ctx.getRealPath("fopconf.xml")));
        } catch (SAXException ex) {
            throw new ServletException(ex);
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
        try {
            //fopFactory.setBaseURL("file:///"+ctx.getRealPath(""));
            //fopFactory.setBaseURL("/");
            fopFactory.getFontManager().setFontBaseURL("file:///"+ctx.getRealPath("")+"/fonts");
        } catch (MalformedURLException ex) {
            throw new ServletException(ex);
        }

        configureFopFactory();
    }

    /**
     * This method is called right after the FopFactory is instantiated and can be overridden
     * by subclasses to perform additional configuration.
     */
    protected void configureFopFactory() {
    }

    
    public void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        try {

            String pathInfo = request.getPathInfo();
            String queryInfo = request.getQueryString();
            String foParam = pathInfo+".jsp";

            
            if (queryInfo == null) queryInfo=""; else queryInfo="?"+queryInfo;
            if (pathInfo != null) {

                //dumpRequest(request);  // just for tests - shows what's happening

//                response.setContentType("application/pdf");
//                Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, response.getOutputStream());
//                Transformer transformer = transFactory.newTransformer();
//                //transformer.setURIResolver(new ClassPathURIResolver());
//                
//                //System.out.println(" URL DO FOP ---------------> " + getURL(request) + "/fop" +pathInfo+ ".jsp"+queryInfo);
//                
//                System.out.println(" URL DO FOP ----------> http://localhost/FopServletTest/fop" +pathInfo+ ".jsp"+queryInfo);
                

                //Source src = new StreamSource(getURL(request) + "/fop" +pathInfo+ ".jsp"+queryInfo);
                //Source src = new StreamSource( "http://localhost/FopServletTest/fop" +pathInfo+ ".jsp"+queryInfo);
                
                
//                System.out.println(" ----------> " + ctx.getRealPath("WEB-INF/fop/"+pathInfo+ ".jsp"));
//                
//                Source src = new StreamSource(ctx.getResourceAsStream(ctx.getRealPath("WEB-INF/fop/"+pathInfo+ ".jsp")));
//                
//                Result res = new SAXResult(fop.getDefaultHandler());
//                transformer.transform(src, res);
                
                request.getRequestDispatcher("/fop/"+foParam).include(request, response);
                // okokok renderFO(ctx.getRealPath("WEB-INF/fop/"+foParam), response);
                
                
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Error</title></head>\n"
                        + "<body><h1>PDF-FopServlet Error</h1><h3>No '{fo-file}.jsp' "
                        + "request param given.</body></html>");
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    
    public static String getURL(HttpServletRequest req) {

        String scheme = req.getScheme();             // http
        String serverName = req.getServerName();     // hostname.com
        int serverPort = req.getServerPort();        // 80
        String contextPath = req.getContextPath();   // /mywebapp
        String servletPath = req.getServletPath();   // /servlet/MyServlet
        String pathInfo = req.getPathInfo();         // /a/b;c=123
        String queryString = req.getQueryString();          // d=789

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);

//        url.append(servletPath);
//        if (pathInfo != null) {
//            url.append(pathInfo);
//        }
//        if (queryString != null) {
//            url.append("?").append(queryString);
//        }
        return url.toString();
    }

    private void dumpRequest(HttpServletRequest request) {

        System.out.println("Method: " + request.getMethod());
        System.out.println("ContextPath: " + request.getContextPath());
        System.out.println("ServletPath: " + request.getServletPath());
        System.out.println("PathInfo: " + request.getPathInfo());
        System.out.println("QueryString: " + request.getQueryString());
        System.out.println("RequestURI: " + request.getRequestURI());
        System.out.println("RequestURL: " + request.getRequestURL());

        for (Enumeration i = request.getHeaderNames(); i.hasMoreElements();) {
            String name = (String) i.nextElement();
            System.out.println(name + " --> " + request.getHeader(name));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void doGetOld(HttpServletRequest request,
                      HttpServletResponse response) throws ServletException {
        try {
            //Get parameters
            String foParam = request.getParameter(FO_REQUEST_PARAM);
            String xmlParam = request.getParameter(XML_REQUEST_PARAM);
            String xsltParam = request.getParameter(XSLT_REQUEST_PARAM);

            //Analyze parameters and decide with method to use
            if (foParam != null) {
                renderFO(foParam, response);
            } else if ((xmlParam != null) && (xsltParam != null)) {
                renderXML(xmlParam, xsltParam, response);
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Error</title></head>\n"
                          + "<body><h1>FopServlet Error</h1><h3>No 'fo' "
                          + "request param given.</body></html>");
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        File post;
        //accept UTF-8 by default
        try {
            if (request.getCharacterEncoding() == null) {
                request.setCharacterEncoding("UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            throw new ServletException(ex);
        }
        //save post data to temp file
        try {
            post = java.io.File.createTempFile(TEMP_PREFIX, "post");
            Writer out = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(post)), "UTF-8");

            // Get a reader to read the incoming data
            BufferedReader reader = request.getReader();
            char[] buf = new char[4 * 1024];  // 4Kchar buffer
            int len;
            while ((len = reader.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            if (post.length() < 1) {
                throw new ServletException();
            }
        } catch (IOException ex) {
            throw new ServletException(ex);
        }
	try {
		renderFO(post.getAbsolutePath(), response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
	post.delete();

}
    /**
     * Converts a String parameter to a JAXP Source object.
     * @param param a String parameter
     * @return Source the generated Source object
     */
    protected Source convertString2Source(String param) {
        Source src;
        try {
            src = uriResolver.resolve(param, null);
        } catch (TransformerException e) {
            src = null;
        }
        if (src == null) {
            src = new StreamSource(new File(param));
        }
        return src;
    }

    private void sendPDF(byte[] content, HttpServletResponse response) throws IOException {
        //Send the result back to the client
        response.setContentType("application/pdf");
        response.setContentLength(content.length);
        response.getOutputStream().write(content);
        response.getOutputStream().flush();
    }

    /**
     * Renders an XSL-FO file into a PDF file. The PDF is written to a byte
     * array that is returned as the method's result.
     * @param fo the XSL-FO file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs while parsing the input
     * file
     * @throws IOException In case of an I/O problem
     */
    protected void renderFO(String fo, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup source
        Source foSrc = convertString2Source(fo);

        //Setup the identity transformation
        Transformer transformer = this.transFactory.newTransformer();
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(foSrc, transformer, response);
    }

    /**
     * Renders an XML file into a PDF file by applying a stylesheet
     * that converts the XML to XSL-FO. The PDF is written to a byte array
     * that is returned as the method's result.
     * @param xml the XML file
     * @param xslt the XSLT file
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void renderXML(String xml, String xslt, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        //Setup sources
        Source xmlSrc = convertString2Source(xml);
        Source xsltSrc = convertString2Source(xslt);

        //Setup the XSL transformation
        Transformer transformer = this.transFactory.newTransformer(xsltSrc);
        transformer.setURIResolver(this.uriResolver);

        //Start transformation and rendering process
        render(xmlSrc, transformer, response);
    }

    /**
     * Renders an input file (XML or XSL-FO) into a PDF file. It uses the JAXP
     * transformer given to optionally transform the input document to XSL-FO.
     * The transformer may be an identity transformer in which case the input
     * must already be XSL-FO. The PDF is written to a byte array that is
     * returned as the method's result.
     * @param src Input XML or XSL-FO
     * @param transformer Transformer to use for optional transformation
     * @param response HTTP response object
     * @throws FOPException If an error occurs during the rendering of the
     * XSL-FO
     * @throws TransformerException If an error occurs during XSL
     * transformation
     * @throws IOException In case of an I/O problem
     */
    protected void render(Source src, Transformer transformer, HttpServletResponse response)
                throws FOPException, TransformerException, IOException {

        FOUserAgent foUserAgent = getFOUserAgent();

        //Setup output
        ByteArrayOutputStream out = new ByteArrayOutputStream();

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

}
