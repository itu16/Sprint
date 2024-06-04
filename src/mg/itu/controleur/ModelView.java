package mg.itu.controleur;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    String urlDestination;
    Map<String, Object> data;

    public ModelView(String view) {
        data = new HashMap<>();
        setUrlDestination(view);
    }

    protected String getUrlDestination() {
        return urlDestination;
    }

    public void setUrlDestination(String urlDestination) {
        this.urlDestination = urlDestination;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addObject(String nom, Object o) {
        this.data.put(nom, o);
    }

    protected void setAttributs(HttpServletRequest request) {
        for (String key : data.keySet()) {
            request.setAttribute(key, data.getOrDefault(key, null));
        }

    }
}
