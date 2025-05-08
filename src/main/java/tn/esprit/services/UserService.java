package tn.esprit.services;

import tn.esprit.entities.user;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements iServices<user> {
    private Connection cnx;

    public UserService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(user user) throws SQLException {
        String sql = "INSERT INTO users (id, nom, prenom, nationnalite, email, password, roles, numtel, isBanned) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.setString(2, user.getNom());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getNationnalite());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getRoles());
            ps.setInt(8, user.getNumtel());
            ps.setBoolean(9, user.isBanned());
            ps.executeUpdate();
            System.out.println("Utilisateur ajouté !");
        }
    }

    @Override
    public void supprimer(user user) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
            System.out.println("Utilisateur supprimé !");
        }
    }

    @Override
    public void modifier(user user) throws SQLException {
        String sql = "UPDATE users SET nom = ?, prenom = ?, nationnalite = ?, email = ?, password = ?, roles = ?, numtel = ?, isBanned = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getNom());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getNationnalite());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRoles());
            ps.setInt(7, user.getNumtel());
            ps.setBoolean(8, user.isBanned());
            ps.setInt(9, user.getId());
            ps.executeUpdate();
            System.out.println("Utilisateur modifié !");
        }
    }

    @Override
    public List<user> recuperer() throws SQLException {
        String sql = "SELECT * FROM users";
        List<user> userList = new ArrayList<>();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                user u = new user(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("nationnalite"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getInt("numtel"),
                        rs.getBoolean("isBanned")
                );
                userList.add(u);
            }
        }
        return userList;
    }

    public user getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new user(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("nationnalite"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getInt("numtel"),
                        rs.getBoolean("isBanned")
                );
            }
        }
        return null;
    }
}
