package services.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import models.User;
import models.suiviBebe;
import models.suiviGrossesse;
import utils.MyDatabase;

public class SuiviBebeService implements IServices<suiviBebe> {
    private final Connection cnx;

    public SuiviBebeService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void supprimer(int id) throws SQLException {

    }

    @Override
    public void ajouter(suiviBebe suivi) throws SQLException {
        String sql = "INSERT INTO suivi_bebe(suivi_grossesse_id, date_suivi, poids_bebe, taille_bebe, etat_sante, battement_coeur, appetitBebe) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, suivi.getSuiviGrossesse().getId());
            ps.setDate(2, new java.sql.Date(suivi.getDateSuivi().getTime()));
            ps.setDouble(3, suivi.getPoidsBebe());
            ps.setDouble(4, suivi.getTailleBebe());
            ps.setString(5, suivi.getEtatSante());
            ps.setDouble(6, suivi.getBattementCoeur());
            ps.setString(7, suivi.getAppetitBebe());

            ps.executeUpdate();
            System.out.println("✅ Suivi bébé ajouté avec succès.");
        }
    }

    @Override
    public void modifier(User user) throws SQLException {

    }

    @Override
    public void supprimer(suiviBebe suivi) throws SQLException {
        String sql = "DELETE FROM suivi_bebe WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, suivi.getId());
            ps.executeUpdate();
            System.out.println("🗑️ Suivi bébé supprimé avec succès.");
        }
    }

    @Override
    public List<suiviBebe> recuperer() throws SQLException {
        List<suiviBebe> suivis = new ArrayList<>();
        String sql = "SELECT sb.*, sg.* FROM suivi_bebe sb JOIN suivi_grossesse sg ON sb.suivi_grossesse_id = sg.id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                // Création de l'objet suiviGrossesse complet
                suiviGrossesse sg = new suiviGrossesse();
                sg.setId(rs.getInt("sg.id"));
                // Ajoutez ici les autres propriétés de suiviGrossesse si nécessaire
                // sg.setNom(rs.getString("sg.nom"));
                // etc.

                suiviBebe suivi = new suiviBebe(
                        rs.getInt("sb.id"),
                        sg,
                        rs.getDate("sb.date_suivi"),
                        rs.getDouble("sb.poids_bebe"),
                        rs.getDouble("sb.taille_bebe"),
                        rs.getString("sb.etat_sante"),
                        rs.getDouble("sb.battement_coeur"),
                        rs.getString("sb.appetitBebe")
                );
                suivis.add(suivi);
            }
        }
        return suivis;
    }

    @Override
    public void inscription(User user) throws SQLException {

    }

    @Override
    public void modifier(suiviBebe suivi) throws SQLException {
        String sql = "UPDATE suivi_bebe SET suivi_grossesse_id = ?, date_suivi = ?, poids_bebe = ?, taille_bebe = ?, etat_sante = ?, battement_coeur = ?, appetitBebe = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, suivi.getSuiviGrossesse().getId());
            ps.setDate(2, new java.sql.Date(suivi.getDateSuivi().getTime()));
            ps.setDouble(3, suivi.getPoidsBebe());
            ps.setDouble(4, suivi.getTailleBebe());
            ps.setString(5, suivi.getEtatSante());
            ps.setDouble(6, suivi.getBattementCoeur());
            ps.setString(7, suivi.getAppetitBebe());
            ps.setInt(8, suivi.getId());

            ps.executeUpdate();
            System.out.println("📝 Suivi bébé mis à jour avec succès.");
        }
    }

    @Override
    public void supprimerD(suiviBebe suiviBebe) throws SQLException {

    }

    @Override
    public List<suiviBebe> afficherDisponibilite() throws SQLException {
        return List.of();
    }

    @Override
    public void modifierD(suiviBebe suiviBebe) throws SQLException {

    }

    @Override
    public void modifier(suiviGrossesse suivi) throws SQLException {

    }

    public List<suiviBebe> recupererParSuiviGrossesse(suiviGrossesse sg) {
        List<suiviBebe> suivis = new ArrayList<>();
        String sql = "SELECT sb.* FROM suivi_bebe sb WHERE sb.suivi_grossesse_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, sg.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    suiviBebe suivi = new suiviBebe(
                            rs.getInt("id"),
                            sg,  // Utilisation de l'objet suiviGrossesse passé en paramètre
                            rs.getDate("date_suivi"),
                            rs.getDouble("poids_bebe"),
                            rs.getDouble("taille_bebe"),
                            rs.getString("etat_sante"),
                            rs.getDouble("battement_coeur"),
                            rs.getString("appetitBebe")
                    );
                    suivis.add(suivi);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des suivis bébé: " + e.getMessage());
            e.printStackTrace();
        }
        return suivis;
    }
}