package mg.itu.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidatorException extends Exception {
    protected String errorPrefix = "";
    protected String errorSuffix = "";
    protected Map<String, String> erreurs;
    protected Map<String, String> values;

    public ValidatorException() {
        super();
        this.values = new HashMap<>();
        this.erreurs = new HashMap<>();
    }

    public void add(String field,String value, String exception) {
        erreurs.put(field, exception);
        values.put(field, value);
    }

    public void setErrorDelimiters(String prefix, String suffix) {
        this.errorPrefix = prefix;
        this.errorSuffix = suffix;
    }
    
    public Map<String,String> getErreurs() {
        return this.erreurs;
    }

    public boolean issetError() {
        return (erreurs.size() > 0);
    }

    public String getInputVal(String paramName) {
        return values.getOrDefault(paramName, "");
    }

    public String getInputError(String paramName) {
        String er = erreurs.getOrDefault(paramName, null);
        if (er != null) {
            return this.getErrorPrefix() + er + this.getErrorSuffix();
        }
        return "";
    }

    public String getErrorPrefix() {
        return errorPrefix;
    }

    public String getErrorSuffix() {
        return errorSuffix;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        for (String key : erreurs.keySet()) {
            sb.append(erreurs.get(key));
            sb.append("\n");
        }
        return sb.toString();
    }    
}
