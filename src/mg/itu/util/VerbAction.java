package mg.itu.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.swing.GroupLayout.ParallelGroup;

public class VerbAction {
    Class<?> cls;
    private Method method;
    private String verb;

    public VerbAction(Class<?> cls, Method method, String verb) {
        this.cls = cls;
        this.method = method;
        this.verb = verb;
    }

    public Class<?> getCls() {
        return cls;
    }

    public void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public boolean isVerbAllowed(String verb) {
        return this.verb.equals(verb);
    }

}
