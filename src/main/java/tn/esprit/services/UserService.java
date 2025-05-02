package tn.esprit.services;
import tn.esprit.entites.User;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.util.*;

public class UserService implements IServices<User> {
    Connection cnx;
    public UserService() {
        cnx= MyDataBase.getInstance().getCnx();
    }
    public List<User> getAllMedecins() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE roles LIKE '%ROLE_MEDECIN%'"; // Vérifie que les rôles sont bien stockés sous cette forme

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setNom(rs.getString("nom"));
            u.setPrenom(rs.getString("prenom"));
            // Ajoute l'utilisateur dans la liste
            list.add(u);
        }

        return list;
    }
    public User getOneById(int id) throws SQLException {
        User user = null;
        String sql = "SELECT * FROM user WHERE id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            user = new User();
            user.setId(rs.getInt("id"));
            user.setNom(rs.getString("nom"));
            user.setPrenom(rs.getString("prenom"));
            // Tu peux ajouter d'autres champs si nécessaire, comme les rôles
            // user.setRoles(rs.getString("roles"));
        }

        return user;
    }
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";

        System.out.println("Récupération de l'utilisateur avec ID: " + id);

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setRoles(rs.getString("roles"));
                    user.setPassword(rs.getString("password"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));

                    // Récupération explicite du numéro de téléphone
                    int numtel = rs.getInt("numtel");
                    user.setNumtel(numtel);

                    System.out.println("Utilisateur trouvé: " + user.getNom() + " " + user.getPrenom() + ", numtel: " + numtel);

                    user.setNationnalite(rs.getString("nationnalite"));
                    user.setBanned(rs.getBoolean("is_banned"));
                    user.setVerified(rs.getBoolean("is_verified"));

                    return user;
                }
                throw new SQLException("Aucun utilisateur trouvé avec l'ID: " + id);
            }
        }
    }




    @Override
    public void ajouter(User user) throws SQLException {

    }

    @Override
    public void supprimerD(User user) throws SQLException {

    }

    @Override
    public List<User> afficherDisponibilite() throws SQLException {
        return List.of();
    }

    @Override
    public void modifierD(User user) throws SQLException {

    }


}
