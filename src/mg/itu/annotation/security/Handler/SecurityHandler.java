package mg.itu.security.handler;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.annotation.Security;
import mg.itu.security.User;
import mg.itu.util.Session;

public class SecurityHandler {
    private Method method;
    public static String SESSION_USER = "user";
    public static HttpServletRequest request;
    
    public SecurityHandler(Method method) {
        setMethod(method);
    }

    public Method getMethod() {
        return method;
    }
    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean requireAuth() {
        return method.isAnnotationPresent(Security.class);
    }

    public static void saveUser(User user,Session session) {
        if (session != null) {
            session.setAttribute(SESSION_USER, user);
        } else {
        throw new IllegalStateException("La requête HTTP ne peut pas être nulle pour enregistrer l'utilisateur");
    }
    }

    public boolean isGranted(HttpServletRequest request) throws Exception {
        // Si la méthode n'a pas d'annotation @Security, accès libre
        Security methodSec = method.getAnnotation(Security.class);
        Security classSec = method.getDeclaringClass().getAnnotation(Security.class);
        if (methodSec == null && classSec == null) {
            return true;
        }
    
        // Vérifie la session utilisateur
        Object userObject = request.getSession().getAttribute(SESSION_USER);
        if (userObject == null) {
            throw new Exception("Access refusé! Vous devez être connecté.");
        }
    
        User user = (User) userObject;
    
        // Détermine l'annotation effective (priorité méthode)
        Security effectiveSec = (methodSec != null) ? methodSec : classSec;
    
        // Vérifie le niveau requis
        if (user.getLevelUser() < effectiveSec.levelUser()) {
            throw new Exception("Access refusé! Niveau utilisateur insuffisant.");
        }
    
        return true;
    }    

}
