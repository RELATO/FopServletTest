FopServletTest
==============

Project for testing FOP parser - transform JSP with XSL-FO tags into PDF

Our team needs to transform a JSP into PDF. 

Inside the JSP file (/fop/hello.jsp) are tags XSL-FO and JSP. Of course, the JSP file MUST be parsed before FOP transformation. 
To do this we use a servlet called FopServlet and a filter called FiltroPdfRenderer.

This is a maven project and was test using Netbeans 7.4 and Glassfish 3.1
It should run using Tomcat or Jetty (not tested)

After deploy you can use the following URL in your browser:

http://localhost:8080/FopServletTest/rpdf/hello

We hope this helps other developers. 
