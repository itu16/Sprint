package mg.itu.security;

public class User {
    private Integer levelUser = 1; // Niveau par défaut
    private String role;

    public User(String role) {
        this.role = role;
        setLevelUserByRole(role);
    }

    public Integer getLevelUser() {
        return this.levelUser;
    }

    public void setLevelUser(Integer level) {
        this.levelUser = level;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        setLevelUserByRole(role);
    }

    private void setLevelUserByRole(String role) {
        switch (role.trim().toLowerCase()) {
            case "admin":
                this.levelUser = 2; // Niveau 2 pour admin
                break;
            case "user":
                this.levelUser = 1; // Niveau 1 pour user
                break;
            default:
                this.levelUser = 0; // Niveau 0 pour inconnu
                break;
        }
    }
}
