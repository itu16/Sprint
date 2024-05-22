package mg.itu.controleur;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import mg.itu.annotation.Controller;
import mg.itu.annotation.GET;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControleur extends HttpServlet {
    private Map<String, Mapping> mappings = new HashMap<>();

    @Override
    public void init() throws ServletException {
        try {
            scanControllers();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void scanControllers() throws ClassNotFoundException {
        ServletContext sc = getServletContext();
        String cPackage = sc.getInitParameter("packageControleur");

        String path = cPackage.replace(".", "/");
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(path).getFile());

        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = cPackage + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> class1 = Class.forName(className);
                    Annotation annotation = class1.getAnnotation(Controller.class);
                    if (annotation != null) {
                        Method[] methods = class1.getDeclaredMethods();
                        for (Method method : methods) {
                            if (method.isAnnotationPresent(GET.class)) {
                                GET getAnnotation = method.getAnnotation(GET.class);
                                String url = getAnnotation.value();
                                String methodName = method.getName();
                                Mapping mapping = new Mapping(className, methodName);
                                mappings.put(url, mapping);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String url = request.getRequestURI();
            Mapping mapping = mappings.get(url);
            if (mapping != null) {
                out.println("URL : " + url + "<br>");
                out.println("Mapping : " + mapping);
            } else {
                out.println("Aucune méthode associée à cette URL.");
            }
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
    public String getServletInfo() {
        return "FrontControleur";
    }
}
