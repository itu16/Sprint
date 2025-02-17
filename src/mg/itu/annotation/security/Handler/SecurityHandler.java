package mg.itu.security.handler;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.annotation.Security;
import mg.itu.security.User;

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

    public static void saveUser(User user) {
        if (request != null) {
            request.getSession().setAttribute(SecurityHandler.SESSION_USER, user);
        }
    }

    public boolean isGranted(HttpServletRequest request) throws Exception {
        boolean reqAuth = requireAuth();
        Object userObject = request.getSession().getAttribute(SecurityHandler.SESSION_USER);
        if (!reqAuth) {
            return true;
        } else if (userObject == null && reqAuth) {
            return false;
        }

        try{
            User user = (User) userObject;
            Security sec = method.getAnnotation(Security.class);
            if (user.getLevelUser() < sec.levelUser()) {
                return false;
            }
        } catch(Exception e) {
            throw new Exception(String.format("User doit etre une instance de [%s]", User.class.getName()));
        }
        return true;
    }

}
