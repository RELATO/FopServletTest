package br.net.oncloud.servlet;

import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.xmlgraphics.util.MimeConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 *
 * @author Relato
 */
public class FOP2PDFServlet extends HttpServlet {

    private FopFactory fopFactory = FopFactory.newInstance();
    private TransformerFactory tFactory = TransformerFactory.newInstance();

    public void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        try {

            String pathInfo = request.getPathInfo();
            String queryInfo = request.getQueryString();
            if (queryInfo == null) queryInfo=""; else queryInfo="?"+queryInfo;
            if (pathInfo != null) {

                //dumpRequest(request);  // just for tests - shows what's happening

                response.setContentType("application/pdf");
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, response.getOutputStream());
                Transformer transformer = tFactory.newTransformer();
                transformer.setURIResolver(new ClassPathURIResolver());

                Source src = new StreamSource(getURL(request) + "/fop/" +pathInfo+ ".jsp"+queryInfo);
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.transform(src, res);
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
}
