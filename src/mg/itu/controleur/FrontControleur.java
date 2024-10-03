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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.Controleur;
import mg.itu.annotation.GET;
import mg.itu.annotation.POST;
import mg.itu.annotation.Url;
import mg.itu.util.Mapping;

public class FrontControleur extends HttpServlet {
    private final String INIT_PACKAGE = "package_controleur";


    private Map<String, Mapping> controleurs = new HashMap<>();

    private void scannePackage(String cPackage) throws Exception {
        if (cPackage == null) {
            ServletContext sc = getServletContext();
            cPackage = sc.getInitParameter(INIT_PACKAGE);
        }

        String path = cPackage.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if(url == null) {
            throw new Exception("Le package ["+ cPackage +"] n'existe pas");
        }

        File directory = new File(url.getFile());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if(file.isFile() && file.getName().endsWith(".class")) {
                    String className = cPackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> class1 = Class.forName(className);
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

    private void setMapping(Class<?> c) throws Exception {
        Method[] methodes = c.getMethods();
        for (int j = 0; j < methodes.length; j++) {
            Url annotUrl = methodes[j].getAnnotation(Url.class);
            if ( annotUrl !=null ) {
                String url = (annotUrl.value().charAt(0) == '/') ? annotUrl.value() : "/" + annotUrl.value();
                if (controleurs.containsKey(url)) {
                    throw new Exception("Duplicate url ["+ url +"] dans "+ c.getName() + " et "+ controleurs.get(url).getClassName());
                }

                Mapping map = new Mapping(
                    c.getName(),
                    methodes[j].getName(), 
                    methodes[j].getParameters()
                );

                if (methodes[j].isAnnotationPresent(POST.class)) {
                    map.addVerb("POST");
                } else {
                    map.addVerb("GET");
                }
                controleurs.put(url, map);
            }
        }
    }

    private String getRequestUrl(HttpServletRequest request) {
        String urlPattern = request.getHttpServletMapping().getPattern().replace("*", "");
        String requestUrl = request.getRequestURI()
                            .replace(request.getContextPath(), "")
                            .replace(urlPattern,"");
        requestUrl = (requestUrl.startsWith("/")) ? requestUrl : "/" + requestUrl;
        return requestUrl;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try {
            PrintWriter out = response.getWriter();

            String requestUrl = getRequestUrl(request);
            Mapping mapping = controleurs.getOrDefault(requestUrl, null);

            if (mapping == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,  "La ressource demandée ["+requestUrl+"] n'est pas disponible");
                return;
            }

            if (!mapping.isMethodAllowed(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            
            // Gestion de reponse
            Object rep = mapping.getResponse(request);
            if(rep == null) {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "Pas de type de retour");
                return;
            }

            if (mapping.isRestapi()) {
                response.setContentType("text/json");
                Gson json = new Gson();
                if (rep instanceof ModelView) {
                    ModelView mv = (ModelView) rep;
                    out.println(json.toJson(mv.getData()));
                } else if (rep instanceof String) {
                    out.println(rep);
                } else {
                    out.println(json.toJson(rep));
                }
            } 
            
            else if(rep instanceof String) {
                out.println(rep.toString());
            } else if (rep instanceof ModelView) {
                ModelView mv = (ModelView) rep;
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrlDestionation());
                mv.setAttributs(request);
                dispatcher.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Type de retour non supporter");
            }

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
            if (controleurs.size() == 0) {
                throw new ServletException("Pas de path trouver");
            }
        } catch (Exception e) {
            throw new ServletException(e);
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
