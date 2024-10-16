package mg.itu.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.annotation.Param;
import mg.itu.annotation.Restapi;

public class Mapping {
    List<VerbAction> verbActions = new ArrayList<>();

    public void addVerbAction(String verb, Class<?> cls, Method method) throws Exception {
        VerbAction va = getVerbAction(verb);
        if (va != null) {
            throw new Exception("Duplicate Method dans [" + cls.getName() + "." + method.getName() + "] et ["
                    + va.getCls().getName() + "." + va.getMethod().getName() + "]");
        }
        verbActions.add(new VerbAction(cls, method, verb));
    }

    public Mapping() {

    }

    private Object cast(Class<?> type, Object value) {
        String typeName = type.getSimpleName().toLowerCase();
        if (typeName.contains("int")) {
            return Integer.parseInt(value.toString());
        } else if (typeName.equals("double")) {
            return Double.parseDouble(value.toString());
        } else if (typeName.equals("float")) {
            return Float.parseFloat(value.toString());
        } else if (typeName.equals("boolean")) {
            return Boolean.parseBoolean(value.toString());
        }
        return value;
    }

    private void injectSession(Object instance, HttpServletRequest request)
            throws IllegalArgumentException, IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.getType().equals(Session.class)) {
                field.setAccessible(true);
                field.set(instance, new Session(request.getSession()));
            }
        }
    }

    private VerbAction getVerbAction(String verb) {
        for (VerbAction va : verbActions) {
            if (va.isVerbAllowed(verb)) {
                return va;
            }
        }
        return null;
    }

    private Object getInstance(Class<?> c) throws Exception {
        return c.getConstructor().newInstance();
    }

    public Object getResponse(HttpServletRequest request) throws Exception {
        VerbAction va = getVerbAction(request.getMethod());
        Object instance = getInstance(va.getCls());
        Method method = va.getMethod();
        Parameter[] parameters = method.getParameters();

        injectSession(instance, request);

        // instance non Primitive Parameter
        Map<String, Object> mapInstances = new HashMap<>();

        // Argument anle method controleur
        Object[] paramValues = new Object[method.getParameterCount()];

        for (int index = 0; index < parameters.length; index++) {
            if (parameters[index].getType().getName().equals(Session.class.getName())) {
                paramValues[index] = new Session(request.getSession());
                continue;
            }
            if (parameters[index].getType().isPrimitive()) {
                continue;
            }

            String key = getParameterName(method, parameters[index]);
            Object model = getInstance(parameters[index].getType());
            mapInstances.put(key, model);
            paramValues[index] = model;
        }

        Enumeration<String> values = request.getParameterNames();
        while (values.hasMoreElements()) {
            String reqKey = values.nextElement();
            String[] data = reqKey.split("\\.");

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getType().getName().equals(Session.class.getName())) {
                    continue;
                }
                String paramKey = getParameterName(method, parameters[i]);
                // Object
                if (paramKey.equals(data[0]) && data.length > 1) {
                    Object model = mapInstances.get(data[0]);
                    Method m = getMethod(model.getClass(), data[1]);
                    Object value = cast(m.getParameterTypes()[0], request.getParameter(reqKey));
                    m.invoke(model, value);
                } else if (paramKey.equals(reqKey)) {
                    paramValues[i] = cast(parameters[i].getType(), request.getParameter(reqKey));
                }
            }
        }

        return method.invoke(instance, paramValues);
    }

    private String getParameterName(Method method, Parameter param) throws Exception {
        if (param.isAnnotationPresent(Param.class)) {
            return param.getAnnotation(Param.class).value();
        } else if (param.getType().getName().equals(Session.class.getName())) {
            return "";
        } else {
            throw new Exception("ETU002643:Erreur annotation");
        }
        // return param.getName();
    }

    public boolean isRestapi(String verb) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Method method = getVerbAction(verb).getMethod();
        return method.isAnnotationPresent(Restapi.class);
    }

    private String toSetters(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private Method getMethod(Class<?> c, String fieldName) throws Exception {
        Class<?> fieldType = c.getDeclaredField(fieldName).getType();
        String fieldSetter = toSetters(fieldName);
        return c.getMethod(fieldSetter, fieldType);
    }

    public boolean isMethodAllowed(String method) {
        for (VerbAction verbAction : verbActions) {
            if (verbAction.isVerbAllowed(method)) {
                return true;
            }
        }
        return false;
    }
}