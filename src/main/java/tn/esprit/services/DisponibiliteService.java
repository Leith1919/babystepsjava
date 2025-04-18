package tn.esprit.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tn.esprit.entites.Disponibilite;
import tn.esprit.tools.MyDataBase;

import java.lang.reflect.Type;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DisponibiliteService implements IServices<Disponibilite>{
    Connection cnx;
    public DisponibiliteService() {
        cnx= MyDataBase.getInstance().getCnx();
    }
    public Disponibilite trouverDisponibilite(int medecinId, LocalDate jour, String heureString) throws SQLException {
        String sql = "SELECT * FROM disponibilite WHERE id_medecin_id = ? AND jour = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ps.setDate(2, Date.valueOf(jour));
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String heuresJson = rs.getString("heures_disp");
            List<String> heuresList = new Gson().fromJson(heuresJson, List.class);
            if (heuresList.contains(heureString)) {
                Disponibilite dispo = new Disponibilite();
                dispo.setId(rs.getInt("id"));
                dispo.setJour(rs.getDate("jour").toLocalDate());
                dispo.setHeuresDisp(heuresList);
                dispo.setStatutDisp(rs.getString("statut_disp"));
                dispo.setIdMedecin(rs.getInt("id_medecin_id"));
                return dispo;
            }
        }

        return null;
    }


    @Override
    public void ajouter(Disponibilite disponibilite) throws SQLException {
        String sql = "insert into disponibilite(jour,heures_disp,statut_disp,id_medecin_id)" +
                "values (?,?,?,?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        Gson gson = new GsonBuilder().create();
        String heuresJson = gson.toJson(disponibilite.getHeuresDisp());

        ps.setDate(1, Date.valueOf(disponibilite.getJour()));
        ps.setString(2, heuresJson);
        ps.setString(3, disponibilite.getStatutDisp());
        ps.setInt(4, disponibilite.getIdMedecin());

        ps.executeUpdate();
        System.out.println("Disponibilité ajoutée avec succès !");

    }


    @Override
    public List<Disponibilite> afficherDisponibilite() throws SQLException{
        List<Disponibilite> disponibilites = new ArrayList<>();
        String sql = "SELECT * FROM disponibilite";


            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            Gson gson = new Gson();

            while (rs.next()) {
                Disponibilite d = new Disponibilite();
                d.setId(rs.getInt("id")); // assure-toi que tu as un champ id
                d.setJour(rs.getDate("jour").toLocalDate());
                d.setStatutDisp(rs.getString("statut_disp"));
                d.setIdMedecin(rs.getInt("id_medecin_id"));

                // convert JSON string to List<String>
                String heuresJson = rs.getString("heures_disp");
                List<String> heuresList = gson.fromJson(heuresJson, List.class);
                d.setHeuresDisp(heuresList);

                disponibilites.add(d);
            }

        return disponibilites;
    }


    public void supprimerD(Disponibilite disponibilite) throws SQLException {
        String sql = "DELETE FROM disponibilite WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, disponibilite.getId());
            ps.executeUpdate();
            System.out.println("Disponibilité supprimée avec succès !");

    }

    @Override
    public void modifierD(Disponibilite disponibilite) throws SQLException {
        String sql = "UPDATE disponibilite SET jour = ?, heures_disp = ?, statut_disp = ?, id_medecin_id = ? WHERE id = ?";

            PreparedStatement ps = cnx.prepareStatement(sql);
            Gson gson = new Gson();
            String heuresJson = gson.toJson(disponibilite.getHeuresDisp());

            ps.setDate(1, Date.valueOf(disponibilite.getJour()));
            ps.setString(2, heuresJson);
            ps.setString(3, disponibilite.getStatutDisp());
            ps.setInt(4, disponibilite.getIdMedecin());
            ps.setInt(5, disponibilite.getId());

            ps.executeUpdate();
            System.out.println("Disponibilité modifiée avec succès !");

    }
}
