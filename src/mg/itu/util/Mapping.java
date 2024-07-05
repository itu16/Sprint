package mg.itu.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import jakarta.servlet.http.HttpServletRequest;
import mg.itu.annotation.Param;

public class Mapping {
    String className;
    String methodName;
    Parameter[] parameters;
    String[] parameterNames;

    private Paranamer paranamer = new BytecodeReadingParanamer();

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

    private Object cast(Class<?> type, Object value) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
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

    private Object getInstance(Class<?> c) throws Exception {
        return c.getConstructor().newInstance();
    }

    public Object getResponse(HttpServletRequest request) throws Exception {
        Class<?> class1 = Class.forName(this.getClassName());
        Object instance = class1.getConstructor().newInstance();
        Method method = class1.getMethod(methodName, getParameterTypes());

        // instance non Primitive Parameter
        Map<String, Object> mapInstances = new HashMap<>();

        // Argument anle method controleur
        Object[] paramValues = new Object[method.getParameterCount()];

        for (int index = 0; index < parameters.length; index++) {
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

    public String getParameterName(Method method, Parameter param) throws Exception {
        if (param.isAnnotationPresent(Param.class)) {
            return param.getAnnotation(Param.class).value();
        }
        throw new Exception("ETU002532 " + method.getName() + " n'a pas d'annotation @Param");
    }

    private String toSetters(String name) {
        return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private Method getMethod(Class<?> c, String fieldName) throws Exception {
        Class<?> fieldType = c.getDeclaredField(fieldName).getType();
        String fieldSetter = toSetters(fieldName);
        return c.getMethod(fieldSetter, fieldType);
    }

    private Class<?>[] getParameterTypes() {
        Class<?>[] types = new Class[parameters.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameters[i].getType();
        }
        return types;
    }

    // Nouvelle méthode ajoutée
    public Object getParameterValue(HttpServletRequest request, Parameter parameter) throws Exception {
        String paramName = getParameterName(parameter);
        String paramValue = request.getParameter(paramName);
        if (paramValue != null) {
            return cast(parameter.getType(), paramValue);
        }
        return null;
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

    public String[] getParameterNames() throws Exception {
        if (parameterNames == null) {
            Class<?> class1 = Class.forName(this.getClassName());
            Method method = class1.getMethod(methodName, getParameterTypes());
            parameterNames = paranamer.lookupParameterNames(method);
        }
        return parameterNames;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    private String getParameterName(Parameter parameter) throws Exception {
        if (parameter.isAnnotationPresent(Param.class)) {
            return parameter.getAnnotation(Param.class).value();
        }
        throw new Exception("Le paramètre " + parameter.getName() + " n'a pas d'annotation @Param");
    }
}
