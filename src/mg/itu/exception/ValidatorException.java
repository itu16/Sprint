package mg.itu.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidatorException extends Exception {
    protected Map<String, String> erreurs;
    protected Map<String, String> values;

    public ValidatorException() {
        super();
        this.values = new HashMap<>();
        this.erreurs = new HashMap<>();
    }

    public void add(String field, String value, String exception) {
        erreurs.put(field, exception);
        values.put(field, value);
    }

    public Map<String, String> getErreurs() {
        return this.erreurs;
    }

    public boolean issetError() {
        return (erreurs.size() > 0);
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
