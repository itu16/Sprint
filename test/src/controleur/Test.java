package controleur;

import mg.itu.annotation.Controleur;
import mg.itu.annotation.GET;

@Controleur
public class Test {

    @GET("/")
    public String index() {
        return "<h1>GG be ee</h1>";
    }

    @GET("/home")
    public void home() {
        return;
    }
}
