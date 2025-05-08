package tn.esprit.entites;

public class User {
    private int id;
    private String email;
    private String roles; // tu peux parser en List<String> si besoin
    private String password;
    private String nom;
    private String prenom;
    private int numtel;
    private String nationnalite;
    private Boolean isBanned;
    private Boolean isVerified;
    public User() {}

    public User(String email, String roles, String password, String nom, int numtel, String prenom, String nationnalite, Boolean isBanned, Boolean isVerified) {
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.nom = nom;
        this.numtel = numtel;
        this.prenom = prenom;
        this.nationnalite = nationnalite;
        this.isBanned = isBanned;
        this.isVerified = isVerified;
    }

    public User(int id, String email, String roles, String password, String nom, String prenom, int numtel, String nationnalite, Boolean isBanned, Boolean isVerified) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.numtel = numtel;
        this.nationnalite = nationnalite;
        this.isBanned = isBanned;
        this.isVerified = isVerified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getNumtel() {
        return numtel;
    }

    public void setNumtel(int numtel) {
        this.numtel = numtel;
    }

    public String getNationnalite() {
        return nationnalite;
    }

    public void setNationnalite(String nationnalite) {
        this.nationnalite = nationnalite;
    }

    public Boolean getBanned() {
        return isBanned;
    }

    public void setBanned(Boolean banned) {
        isBanned = banned;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }
    @Override
    public String toString() {
        return nom + " " + prenom; // Renvoie le nom complet
    }

}
