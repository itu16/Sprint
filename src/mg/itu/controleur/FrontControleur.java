/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package mg.itu.controleur;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.Controleur;
import mg.itu.annotation.GET;
import mg.itu.util.Mapping;

public class FrontControleur extends HttpServlet {
    private Map<String,Mapping> controleurs = new HashMap<>();

    private void scannePackage(String cPackage) throws ClassNotFoundException {
        if (cPackage == null) {
            ServletContext sc = getServletContext();
            cPackage = sc.getInitParameter("packageControleur");
        }

        String path = cPackage.replace(".", "/");
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if(file.isFile() && file.getName().endsWith(".class")) {
                    String className = cPackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class class1 = Class.forName(className);
                    Annotation annotation = class1.getAnnotation(Controleur.class);
                    if (annotation != null) {
                        this.setMapping(class1);
                    }
                } else if (file.isDirectory()) {
                    String newPackage = cPackage + "." + file.getName();
                    scannePackage(newPackage);
                }
            }
        }
    }

    private void setMapping(Class c) {
        Method[] methodes = c.getMethods();
        for (int j = 0; j < methodes.length; j++) {
            GET annotGet = methodes[j].getAnnotation(GET.class);
            if ( annotGet !=null ) {
                String url = (annotGet.value().charAt(0) == '/') ? annotGet.value() : "/" + annotGet.value();
                controleurs.put(url, new Mapping(c.getName() , methodes[j].getName()));
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try(PrintWriter out = response.getWriter()) {
            String requestUrl = request.getRequestURI().replace(request.getContextPath(), "");
            Mapping mapping = controleurs.getOrDefault(requestUrl, null);
            if (mapping == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,  "La ressource demandée ["+requestUrl+"] n'est pas disponible");
                return;
            }
            out.println("<ul>");
            out.println("<li><h1>"+ requestUrl +"</h1><ul>");
            out.println("<li><strong>Nom class</strong>:" + mapping.getClassName() + "</li>");
            out.println("<li><strong>Methode:</strong>" + mapping.getMethodName() + "</li>");
            out.println("<li><strong>Content:</strong>"+ mapping.getResponse() +"</li>");
            out.println("</ul></li>");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.scannePackage(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
