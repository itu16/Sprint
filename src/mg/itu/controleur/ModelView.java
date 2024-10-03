package mg.itu.controleur;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    String urlDestination;
    Map<String,Object> data;
    
    public Map<String, Object> getData() {
        return data;
    }

    public ModelView(String view) {
        data = new HashMap<>();
        setUrlDestionation(view);
    }

    public ModelView() {
        data = new HashMap<>();
    }

    protected String getUrlDestionation() {
        return urlDestination;
    }

    private void setUrlDestionation(String view) {
        this.urlDestination = view;
    }

    public void addObject(String nom, Object o) {
        this.data.put(nom, o);
    }

    protected void setAttributs(HttpServletRequest request) {
        for (String  key : data.keySet()) {
            request.setAttribute(key, data.getOrDefault(key, null));
        }
    }

}
