package mg.itu.controleur;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import mg.itu.util.Mapping;
import mg.itu.util.MySession;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.annotation.Controleur;
import mg.itu.annotation.GET;
import mg.itu.util.MySession;

public class FrontControleur extends HttpServlet {
    private Map<String, Mapping> controleurs = new HashMap<>();

    // scanne les packages donné pour trouver toutes les classes annotées avec
    // @Controleur.
    private void scannePackage(String cPackage) throws Exception {
        if (cPackage == null) {
            ServletContext sc = getServletContext();
            cPackage = sc.getInitParameter("packageControleur");
        }

        // Convertit le nom du package en un chemin de fichier et tente de trouver ce
        // chemin dans les ressources du classpath.
        String path = cPackage.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new Exception("Le package [" + cPackage + "] n'existe pas");
        }

        // Vérifie si le répertoire existe
        File directory = new File(url.getFile());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            // il charge la classe et vérifie si elle est annotée avec @Controleur
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
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

    // parcourt toutes les méthodes d'une classe et vérifie si elles sont annotées
    // avec @GET
    private void setMapping(Class<?> c) throws Exception {
        Method[] methodes = c.getMethods();
        for (int j = 0; j < methodes.length; j++) {
            GET annotGet = methodes[j].getAnnotation(GET.class);
            // Si oui, elle crée un mapping entre l'URL et la méthode et le stocke dans la
            // map controleurs.
            if (annotGet != null) {
                String url = (annotGet.value().charAt(0) == '/') ? annotGet.value() : "/" + annotGet.value();

                if (controleurs.containsKey(url)) {
                    throw new Exception("Duplicate url [" + url + "] dans " + c.getName() + " et "
                            + controleurs.get(url).getClassName());
                }
                Mapping map = new Mapping(c.getName(), methodes[j].getName(), methodes[j].getParameters());
                controleurs.put(url, map);
            }
        }
    }

    private String getRequestUrl(HttpServletRequest request) {
        String urlPattern = request.getHttpServletMapping().getPattern().replace("*", "");
        String requestUrl = request.getRequestURI()
                .replace(request.getContextPath(), "")
                .replace(urlPattern, "");
        requestUrl = (requestUrl.startsWith("/")) ? requestUrl : "/" + requestUrl;
        return requestUrl;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String requestUrl = getRequestUrl(request);
            Mapping mapping = controleurs.getOrDefault(requestUrl, null);
            if (mapping == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "La ressource demandée [" + requestUrl + "] n'est pas disponible");
                return;
            }

            // Récupération de la classe et de la méthode à partir du mapping.
            Class<?> controllerClass = Class.forName(mapping.getClassName());
            Method method = null;
            for (Method m : controllerClass.getMethods()) {
                if (m.getName().equals(mapping.getMethodName())) {
                    method = m;
                    break;
                }
            }
            if (method == null) {
                throw new ServletException("Méthode non trouvée: " + mapping.getMethodName());
            }

            // Vérification des annotations @Param
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                mapping.getParameterName(method, parameter);
            }

            // Gestion de la réponse
            Object rep = mapping.getResponse(request);
            if (rep == null) {
                response.sendError(HttpServletResponse.SC_NO_CONTENT, "Pas de type de retour");
                return;
            }

            if (rep.getClass().getTypeName().equals(String.class.getTypeName())) {
                out.println(rep.toString());
            } else if (rep.getClass().getTypeName().equals(ModelView.class.getTypeName())) {
                ModelView mv = (ModelView) rep;
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrlDestionation());
                mv.setAttributs(request); // Utilisation de la méthode setAttributs
                dispatcher.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Type de retour non supporté");
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private MySession createMySession(HttpServletRequest request) {
        return new MySession(request.getSession());
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
            throw new ServletException(e.getMessage(), e.getCause());
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
