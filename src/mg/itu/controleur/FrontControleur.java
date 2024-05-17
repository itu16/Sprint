package mg.itu.controleur;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import mg.itu.annotation.Controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControleur extends HttpServlet {
    private boolean isScanned = false;
    private List<String> controleurs;

    private List<String> scannePackage(String cPackage) throws ClassNotFoundException {
        if (cPackage == null) {
            ServletContext sc = getServletContext();
            cPackage = sc.getInitParameter("packageControleur");
        }

        String path = cPackage.replace(".", "/");
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());
        List<String> nameClasses = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = cPackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class class1 = Class.forName(className);
                    Annotation annotation = class1.getAnnotation(Controller.class);
                    if (annotation != null) {
                        nameClasses.add(class1.getName());
                    }
                } else if (file.isDirectory()) {
                    String newPackage = cPackage + "." + file.getName();
                    List<String> newList = scannePackage(newPackage);
                    nameClasses.addAll(newList);
                }
            }
        }
        return nameClasses;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            if (!isScanned) {
                controleurs = this.scannePackage(null);
                isScanned = true;
            }
            for (String string : controleurs) {
                out.println(string + "<br>");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
