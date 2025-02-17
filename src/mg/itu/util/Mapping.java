package mg.itu.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import mg.itu.annotation.Param;
import mg.itu.annotation.Restapi;
import mg.itu.controleur.ModelView;
import mg.itu.exception.ValidatorException;

public class Mapping {
    public static final String ATR_VALIDATION = "validation";
    List<VerbAction> verbActions = new ArrayList<>();

    public void addVerbAction(String verb,Class <?> cls, Method method) throws Exception {
        VerbAction va = getVerbAction(verb);
        if (va != null) {
            throw new Exception("Duplicate Method dans ["+ cls.getName() +"."+ method.getName() + "] et [" + va.getCls().getName() +"."+ va.getMethod().getName() + "]");
        }
        verbActions.add(new VerbAction(verb, cls, method));
    }

    private Object cast(Class<?> type, Object value) {
        String typeName = type.getSimpleName().toLowerCase();
        try {
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
        } catch (Exception e) {
            if (typeName.contains("int")) {
                return 0;
            } else if (typeName.equals("double")) {
                return 0;
            } else if (typeName.equals("float")) {
                return 0;
            } else if (typeName.equals("boolean")) {
                return Boolean.FALSE;
            }
            return value;
        }
    }


    private void injectSession(Object instance, HttpServletRequest request) throws IllegalArgumentException, IllegalAccessException {
        for ( Field field : instance.getClass().getDeclaredFields()) {
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

    protected void injectPartOnModel(HttpServletRequest request, Map<String,Object> models) throws Exception {
        try{
            Collection<Part> parts =  request.getParts();
            for (Part part : parts) {
                String fileName = part.getName();
                String[] data = fileName.split("\\.");

                //Object 
                if (data.length > 1) {
                    Object model = models.get(data[0]);
                    Method m = getMethod(model.getClass(), data[1]); 
                    try {
                        m.invoke(model, new Fichier(part));
                    } catch(Exception e) {
                        continue;
                    }
                }
            }
        } catch(Exception e) {
            return;
        }
    }

    public HttpServletRequest validerMapObject(Map<String, Object> models, HttpServletRequest request) throws IllegalAccessException {
        Map<String,ValidatorException> validations = Validator.controllerMap(models);
        ValidatorException errors = new ValidatorException();
        boolean error = false;
        for (String param : validations.keySet()) {
            ValidatorException ve = validations.get(param);
            if (ve.issetError()) {
                error = true;
            }
            for (String arg : ve.getErreurs().keySet()) {
                System.out.println(param+"."+arg+": "+ve.getInputVal(arg)+" error: "+ve.getInputError(arg));
                errors.add(param+"."+arg, ve.getInputVal(arg), ve.getInputError(arg));
            }
        }
        if (error) {
            request.setAttribute(ATR_VALIDATION, errors);
            HttpServletRequestWrapper req = new HttpServletRequestWrapper (request) {
                @Override
                public String getMethod() {
                    return "GET";
                }
            };
            req.setAttribute(ATR_VALIDATION, errors);
            return req;
        }
        return null;
    }

    public Object getResponse(HttpServletRequest request) throws Exception {
        VerbAction va = getVerbAction(request.getMethod());
        Method method = va.getMethod();
        if (!va.getSecurity().isGranted(request)) {
            throw new Exception("Access refusé!");
        }

        Object instance = getInstance(va.getCls());
        Parameter[] parameters = method.getParameters();

        injectSession(instance, request);

        // instance non Primitive Parameter
        Map<String, Object> mapInstances =  new HashMap<>();
        //Argument anle method controleur
        Object[] paramValues = new Object[method.getParameterCount()];

        for (int index = 0; index < parameters.length; index++) {
            if (parameters[index].getType().equals(Session.class)) {
                paramValues[index] = new Session(request.getSession());
                continue;
            }

            if (parameters[index].getType().equals(Fichier.class)) {
                Part p = request.getPart(getParameterName(method, parameters[index]));
                paramValues[index] = new Fichier(p);
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
        System.out.println("\n"+request.getParameter("emp.name"));
        while (values.hasMoreElements()) {
            String reqKey = values.nextElement();
            System.out.println("\n\n"+reqKey);
            String[] data = reqKey.split("\\.");

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getType().equals(Session.class)) {
                    continue;
                }

                String paramKey = getParameterName(method, parameters[i]);

                //Object 
                if (paramKey.equals(data[0]) && data.length > 1) {
                    Object model = mapInstances.get(data[0]);
                    Method m = getMethod(model.getClass(), data[1]);
                    System.out.println(m.getName() +"Value["+ reqKey +"]: "+ request.getParameter(reqKey));
                    Object value = cast(m.getParameterTypes()[0], request.getParameter(reqKey));
                    System.out.println(m.getName()+ value.toString());
                    m.invoke(model, value);
                } else if(paramKey.equals(reqKey)) {
                    paramValues[i] = cast(parameters[i].getType(), request.getParameter(reqKey));
                }
            }
        }
        injectPartOnModel(request, mapInstances);

        Object rep = method.invoke(instance, paramValues);
        Object repValidation = validerMapObject(mapInstances, request);
        if (repValidation != null) {
            return repValidation;
        }

        if (rep instanceof ModelView mv) {
            request.getSession().setAttribute(ATR_VALIDATION, mv.getCallbackValidation());
            if (request.getAttribute(ATR_VALIDATION) == null) mv.addObject(ATR_VALIDATION, new ValidatorException());
        }
        return rep;
    }

    private String getParameterName(Method method, Parameter param) throws Exception {
        if (param.isAnnotationPresent(Param.class)) {
            return param.getAnnotation(Param.class).value();
        } else if (param.getType().equals(Session.class)) {
            return "session";
        } else {
            throw new Exception("ETU002532:Erreur annotation - Paramètre non reconnu : " + param.getType().getName());
        }
    }

    private Method getMethod(Class<?> c, String fieldName) throws Exception {
        Class<?> fieldType = c.getDeclaredField(fieldName).getType();
        String fieldSetter = toSetters(fieldName);
        return c.getMethod(fieldSetter, fieldType);
    }

    public boolean isRestapi(String verb) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Method method = getVerbAction(verb).getMethod();
        return method.isAnnotationPresent(Restapi.class);
    }

    private String toSetters(String name) {
        return "set" + name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public boolean isMethodAllowed(String method) {
        for (VerbAction verbAction: verbActions) {
            if (verbAction.isVerbAllowed(method)) {
                return true;
            }
        }
        return false;
    }
}