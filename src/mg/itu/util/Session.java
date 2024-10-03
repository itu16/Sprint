package mg.itu.util;

import jakarta.servlet.http.HttpSession;

public class Session {
    private final HttpSession httpSession;

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;
    }
    
    public void setAttribute(String key, Object value) {
        httpSession.setAttribute(key, value);
    }

    public Object getAttribute(String key) {
        return httpSession.getAttribute(key);
    }

    public void removeAttribute(String key) {
        httpSession.removeAttribute(key);
    }

    public void invalidate() {
        httpSession.invalidate();
    }

    public String getId() {
        return httpSession.getId();
    }
}
