package controleur.mg;

import mg.itu.annotation.Controleur;
import mg.itu.annotation.GET;

@Controleur
public class Autre {
    @GET("/autre")
    public String autreFonction() {
        return "Tsara be daholo";
    }
}
