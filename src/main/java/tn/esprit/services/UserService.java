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
