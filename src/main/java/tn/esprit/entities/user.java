package tn.esprit.entities;

import java.util.Date;

public class user {
    private int id;
    private String email;
    private String roles; // JSON format (à parser si besoin)
    private String password;
    private String nom;
    private String prenom;
    private String numtel;
    private String nationalite;
    private boolean isBanned;
    private boolean isVerified;
    private String profilePicture;
    private Date updatedAt;
    private String verificationDocument;
    private String verificationStatus;

    // Constructeurs
    public user() {}

    public user(int id, String email, String roles, String password, String nom, String prenom,
                String numtel, String nationalite, boolean isBanned, boolean isVerified,
                String profilePicture, Date updatedAt, String verificationDocument, String verificationStatus) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.password = password;
        this.nom = nom;
        this.prenom = prenom;
        this.numtel = numtel;
        this.nationalite = nationalite;
        this.isBanned = isBanned;
        this.isVerified = isVerified;
        this.profilePicture = profilePicture;
        this.updatedAt = updatedAt;
        this.verificationDocument = verificationDocument;
        this.verificationStatus = verificationStatus;
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

    public String getNumtel() {
        return numtel;
    }

    public void setNumtel(String numtel) {
        this.numtel = numtel;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getVerificationDocument() {
        return verificationDocument;
    }

    public void setVerificationDocument(String verificationDocument) {
        this.verificationDocument = verificationDocument;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    // toString
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", password='" + password + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", numtel='" + numtel + '\'' +
                ", nationalite='" + nationalite + '\'' +
                ", isBanned=" + isBanned +
                ", isVerified=" + isVerified +
                ", profilePicture='" + profilePicture + '\'' +
                ", updatedAt=" + updatedAt +
                ", verificationDocument='" + verificationDocument + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                '}';
    }
}
