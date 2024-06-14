package mg.itu.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.annotation.Param;

public class Mapping {
    String className;
    String methodName;
    Parameter[] parameters;

    public Mapping(String className, String methodName, Parameter[] parameters) {
        setClassName(className);
        setParameters(parameters);
        setMethodName(methodName);
    }

    public Mapping(String className, String methodName) {
        setClassName(className);
        setMethodName(methodName);
    }

    public Mapping() {

    }

    public Object getResponse(HttpServletRequest request) throws Exception {
        Class<?> class1 = Class.forName(this.getClassName());
        Object instance = class1.getConstructor().newInstance();
        Method method = class1.getMethod(methodName, getParameterTypes());

        Object[] params = new Object[method.getParameterCount()];
        Enumeration<String> values = request.getParameterNames();

        while (values.hasMoreElements()) {
            String name = values.nextElement();

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(name)) {
                    params[i] = request.getParameter(name);
                }
            }

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    String val = parameters[i].getAnnotation(Param.class).value();
                    if (val.equals(name)) {
                        params[i] = request.getParameter(name);
                    }
                }
            }
        }

        return method.invoke(instance, params);
    }

    private Class<?>[] getParameterTypes() {
        Class<?>[] types = new Class[parameters.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameters[i].getType();
        }
        return types;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }
}