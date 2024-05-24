package mg.itu.controleur;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void scanControllers() throws ClassNotFoundException, IOException {
        ServletContext sc = getServletContext();
        String cPackage = sc.getInitParameter("packageControleur");

        String path = cPackage.replace('.', '/');
        URL directoryURL = Thread.currentThread().getContextClassLoader().getResource(path);

        if (directoryURL == null) {
            throw new ClassNotFoundException("Package directory not found: " + path);
        }

        File directory = new File(directoryURL.getFile());

        if (directory.exists() && directory.isDirectory()) {
            File[] files = Objects.requireNonNull(directory.listFiles());
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
        } else {
            throw new ClassNotFoundException("Package directory not found or is not a directory: " + path);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String url = request.getRequestURI().substring(request.getContextPath().length());
            Mapping mapping = mappings.get(url);
            if (mapping != null) {
                out.println("<h1>URL: " + url + "</h1>");
                out.println("<p>Class: " + mapping.getClassName() + "</p>");
                out.println("<p>Method: " + mapping.getMethodName() + "</p>");
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
