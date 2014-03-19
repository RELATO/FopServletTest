package br.net.oncloud.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class DumpFilter implements Filter {

    private static class ByteArrayServletStream extends ServletOutputStream {

        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int param) throws IOException {
            baos.write(param);
        }
    }

    private static class ByteArrayPrintWriter {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private final PrintWriter pw = new PrintWriter(baos);

        private final ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    private class BufferedServletInputStream extends ServletInputStream {

        ByteArrayInputStream bais;

        public BufferedServletInputStream(ByteArrayInputStream bais) {
            this.bais = bais;
        }

        @Override
        public int available() {
            return bais.available();
        }

        @Override
        public int read() {
            return bais.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return bais.read(buf, off, len);
        }

    }

    private class BufferedRequestWrapper extends HttpServletRequestWrapper {

        ByteArrayInputStream bais;

        ByteArrayOutputStream baos;

        BufferedServletInputStream bsis;

        byte[] buffer;

        public BufferedRequestWrapper(HttpServletRequest req) throws IOException {
            super(req);
            InputStream is = req.getInputStream();
            baos = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int letti;
            while ((letti = is.read(buf)) > 0) {
                baos.write(buf, 0, letti);
            }
            buffer = baos.toByteArray();
        }

        @Override
        public ServletInputStream getInputStream() {
            try {
                bais = new ByteArrayInputStream(buffer);
                bsis = new BufferedServletInputStream(bais);
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            return bsis;
        }

        public byte[] getBuffer() {
            return buffer;
        }

    }

    private boolean dumpRequest;
    private boolean dumpResponse;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        dumpRequest = Boolean.valueOf(filterConfig.getInitParameter("dumpRequest"));
        dumpResponse = Boolean.valueOf(filterConfig.getInitParameter("dumpResponse"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        BufferedRequestWrapper bufferedRequest = new BufferedRequestWrapper(httpRequest);

        if (dumpRequest) {
            System.out.println("REQUEST -> " + new String(bufferedRequest.getBuffer()));
        }

        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        final ByteArrayPrintWriter pw = new ByteArrayPrintWriter();
        HttpServletResponse wrappedResp = new HttpServletResponseWrapper(response) {
            @Override
            public PrintWriter getWriter() {
                return pw.getWriter();
            }

            @Override
            public ServletOutputStream getOutputStream() {
                return pw.getStream();
            }

        };

        filterChain.doFilter(bufferedRequest, wrappedResp);

        byte[] bytes = pw.toByteArray();
        response.getOutputStream().write(bytes);
        if (dumpResponse) {
            System.out.println("RESPONSE -> " + new String(bytes));
        }
    }

    @Override
    public void destroy() {
    }

}
