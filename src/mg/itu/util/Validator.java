package mg.itu.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import mg.itu.exception.ValidatorException;
import mg.itu.validation.NotBlank;
import mg.itu.validation.NotNull;
import mg.itu.validation.Range;

public class Validator {
    protected static String validateRange(Object object, Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(Range.class)) {
            Range range = field.getAnnotation(Range.class);
            double min = range.min();
            double max = range.max();

            Object value = field.get(object);
            if (value instanceof Number) {
                double doubleValue = Double.parseDouble(value.toString());
                if (doubleValue < min || doubleValue > max) {
                    return "Le champ " + field.getName() + " doit être entre " + min + " et " + max
                            + ". Valeur actuelle : " + doubleValue;
                }
            }
        }
        return null;
    }

    protected static String validateNotNull(Object object, Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(NotNull.class)) {
            Object value = field.get(object);
            if (value == null) {
                return "Le champ " + field.getName() + " ne doit pas être null.";
            }
        }
        return null;
    }

    protected static String validateNotBlank(Object object, Field field) throws IllegalAccessException {
        if (field.isAnnotationPresent(NotBlank.class)) {
            Object value = field.get(object);
            if (value instanceof String && ((String) value).trim().length() == 0) {
                return "Le champ " + field.getName() + " est obligatoire.";
            }
        }
        return null;
    }

    public static ValidatorException controller(String param, Object model) throws IllegalAccessException {
        Class<?> clazz = model.getClass();
        ValidatorException ve = new ValidatorException();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String notNull = Validator.validateNotNull(model, field);
            String range = Validator.validateRange(model, field);
            String notBlank = Validator.validateNotBlank(model, field);
            if (notNull != null) {
                ve.add(field.getName(), null, notNull);
            }
            if (range != null) {
                ve.add(field.getName(), field.get(model).toString(), range);
            }
            if (notBlank != null) {
                ve.add(field.getName(), "", notBlank);
            }
        }
        return ve;
    }

    public static Map<String, ValidatorException> controllerMap(Map<String, Object> models)
            throws IllegalAccessException {
        Map<String, ValidatorException> exceptions = new HashMap<>();
        for (String param : models.keySet()) {
            exceptions.put(param, Validator.controller(param, models.get(param)));
        }
        return exceptions;
    }
}