package tn.esprit.services;

import tn.esprit.entities.user;
import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.tools.MyDatabase;
import tn.esprit.utils.SuiviGrossesseCSVUtil;
import tn.esprit.utils.SuiviGrossesseStatsUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class SuiviGrossesseService implements IServices<suiviGrossesse> {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000; // 1 seconde entre les tentatives

    /**
     * Exécute une opération de base de données avec mécanisme de nouvelle tentative
     * @param operation La fonction à exécuter
     * @param <T> Le type de retour
     * @return Le résultat de l'opération
     * @throws SQLException Si toutes les tentatives échouent
     */
    private <T> T executeWithRetry(DatabaseOperation<T> operation) throws SQLException {
        int retryCount = 0;
        SQLException lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // Toujours obtenir une connexion fraîche pour chaque tentative
                Connection conn = MyDatabase.getInstance().getCnx();
                return operation.execute(conn);
            } catch (SQLException e) {
                lastException = e;
                if (isCommunicationError(e)) {
                    retryCount++;
                    System.err.println("Erreur de communication avec la BD (tentative " + retryCount +
                            "/" + MAX_RETRIES + "): " + e.getMessage());
                    if (retryCount < MAX_RETRIES) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * retryCount); // Délai progressif
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    // Si ce n'est pas une erreur de communication, propager immédiatement
                    throw e;
                }
            }
        }

        // Toutes les tentatives ont échoué
        throw new SQLException("Échec de l'opération après " + MAX_RETRIES + " tentatives", lastException);
    }

    /**
     * Vérifie si l'erreur est liée à la communication avec la base de données
     */
    private boolean isCommunicationError(SQLException e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("communications link failure") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("lost connection") ||
                message.contains("broken pipe") ||
                e.getSQLState() != null && (
                        e.getSQLState().equals("08S01") || // Communication link failure
                                e.getSQLState().equals("08003") || // Connection does not exist
                                e.getSQLState().equals("08006")    // Connection failure
                );
    }

    @Override
    public void ajouter(suiviGrossesse suivi) throws SQLException {
        executeWithRetry(conn -> {
            String sql = "INSERT INTO suivi_grossesse(date_suivi, poids, tension, symptomes, etat_grossesse, patient_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, new java.sql.Date(suivi.getDateSuivi().getTime()));
                ps.setDouble(2, suivi.getPoids());
                ps.setDouble(3, suivi.getTension());
                ps.setString(4, suivi.getSymptomes());
                ps.setString(5, suivi.getEtatGrossesse());
                ps.setInt(6, suivi.getPatientId());

                int result = ps.executeUpdate();

                if (result > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            suivi.setId(generatedKeys.getInt(1));
                        }
                    }
                }

                System.out.println("✅ Suivi de grossesse ajouté avec succès (ID: " + suivi.getId() + ")");
                return result;
            }
        });

        try {
            List<suiviGrossesse> all = recuperer();
            System.out.println("Nombre de suivis à écrire dans le CSV : " + all.size());
            
            // Utiliser un chemin absolu dans le dossier du projet
            String projectPath = System.getProperty("user.dir");
            String csvPath = projectPath + "/suivis_grossesse.csv";
            System.out.println("Chemin du fichier CSV : " + csvPath);
            
            // Créer le fichier s'il n'existe pas
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) {
                csvFile.getParentFile().mkdirs();
                csvFile.createNewFile();
                System.out.println("Fichier CSV créé à : " + csvPath);
            }
            
            SuiviGrossesseCSVUtil.saveAllToCSV(all, csvPath);
            System.out.println("CSV écrit avec succès !");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'écriture du CSV : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(suiviGrossesse suivi) throws SQLException {
        executeWithRetry(conn -> {
            String sql = "DELETE FROM suivi_grossesse WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, suivi.getId());
                int result = ps.executeUpdate();
                if (result > 0) {
                    System.out.println("🗑️ Suivi de grossesse supprimé avec succès (ID: " + suivi.getId() + ")");
                } else {
                    System.out.println("⚠️ Aucun suivi trouvé avec l'ID: " + suivi.getId());
                }
                return result;
            }
        });

        try {
            List<suiviGrossesse> all = recuperer();
            SuiviGrossesseCSVUtil.saveAllToCSV(all, "suivis_grossesse.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        executeWithRetry(conn -> {
            String sql = "DELETE FROM suivi_grossesse WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int result = ps.executeUpdate();
                if (result > 0) {
                    System.out.println("🗑️ Suivi de grossesse supprimé avec succès (ID: " + id + ")");
                } else {
                    System.out.println("⚠️ Aucun suivi trouvé avec l'ID: " + id);
                }
                return result;
            }
        });
    }

    @Override
    public void modifier(suiviGrossesse suivi) throws SQLException {
        executeWithRetry(conn -> {
            String sql = "UPDATE suivi_grossesse SET date_suivi = ?, poids = ?, tension = ?, " +
                    "symptomes = ?, etat_grossesse = ?, patient_id = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, new java.sql.Date(suivi.getDateSuivi().getTime()));
                ps.setDouble(2, suivi.getPoids());
                ps.setDouble(3, suivi.getTension());
                ps.setString(4, suivi.getSymptomes());
                ps.setString(5, suivi.getEtatGrossesse());
                ps.setInt(6, suivi.getPatientId());
                ps.setInt(7, suivi.getId());

                int result = ps.executeUpdate();
                if (result > 0) {
                    System.out.println("✏️ Suivi de grossesse mis à jour avec succès (ID: " + suivi.getId() + ")");
                } else {
                    System.out.println("⚠️ Aucun suivi trouvé avec l'ID: " + suivi.getId());
                }
                return result;
            }
        });

        try {
            List<suiviGrossesse> all = recuperer();
            System.out.println("Nombre de suivis à écrire dans le CSV : " + all.size());
            SuiviGrossesseCSVUtil.saveAllToCSV(all, "suivis_grossesse.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<suiviGrossesse> recuperer() throws SQLException {
        return executeWithRetry(conn -> {
            List<suiviGrossesse> suivis = new ArrayList<>();
            String sql = "SELECT * FROM suivi_grossesse ORDER BY date_suivi DESC";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {
                    suiviGrossesse suivi = extractSuiviFromResultSet(rs);
                    suivis.add(suivi);
                }
            }
            return suivis;
        });
    }

    /**
     * Récupère les suivis de grossesse pour un patient spécifique
     * @param patientId L'ID du patient
     * @return La liste des suivis de grossesse du patient
     * @throws SQLException En cas d'erreur SQL
     */
    public List<suiviGrossesse> getSuivisByPatientId(int patientId) throws SQLException {
        return executeWithRetry(conn -> {
            List<suiviGrossesse> suivis = new ArrayList<>();
            String sql = "SELECT * FROM suivi_grossesse WHERE patient_id = ? ORDER BY date_suivi DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        suiviGrossesse suivi = extractSuiviFromResultSet(rs);
                        suivis.add(suivi);
                    }
                }
            }
            return suivis;
        });
    }

    /**
     * Récupère un suivi de grossesse par son ID
     * @param id L'ID du suivi à récupérer
     * @return Le suivi de grossesse ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur SQL
     */
    public suiviGrossesse getById(int id) throws SQLException {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM suivi_grossesse WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return extractSuiviFromResultSet(rs);
                    }
                    return null;
                }
            }
        });
    }

    /**
     * Extrait un objet suiviGrossesse d'un ResultSet
     * @param rs Le ResultSet contenant les données
     * @return Un objet suiviGrossesse
     * @throws SQLException En cas d'erreur d'accès aux données
     */
    private suiviGrossesse extractSuiviFromResultSet(ResultSet rs) throws SQLException {
        return new suiviGrossesse(
                rs.getInt("id"),
                rs.getDate("date_suivi"),
                rs.getDouble("poids"),
                rs.getDouble("tension"),
                rs.getString("symptomes"),
                rs.getString("etat_grossesse"),
                rs.getInt("patient_id")
        );
    }

    // Méthodes supplémentaires pour les statistiques ou rapports

    /**
     * Calcule le poids moyen lors des suivis d'un patient
     * @param patientId L'ID du patient
     * @return Le poids moyen
     * @throws SQLException En cas d'erreur SQL
     */
    public double getPoidsAverage(int patientId) throws SQLException {
        return executeWithRetry(conn -> {
            String sql = "SELECT AVG(poids) as poids_avg FROM suivi_grossesse WHERE patient_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("poids_avg");
                    }
                    return 0.0;
                }
            }
        });
    }

    /**
     * Compte le nombre total de suivis pour un patient
     * @param patientId L'ID du patient
     * @return Le nombre de suivis
     * @throws SQLException En cas d'erreur SQL
     */
    public int countSuivisForPatient(int patientId) throws SQLException {
        return executeWithRetry(conn -> {
            String sql = "SELECT COUNT(*) as count FROM suivi_grossesse WHERE patient_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            }
        });
    }

    // Méthodes à implémenter vides pour respecter l'interface IServices
    @Override
    public void modifier(user user) throws SQLException {
        // Non implémenté - requis par l'interface
    }

    @Override
    public void modifier(suiviBebe suivi) throws SQLException {
        // Non implémenté - requis par l'interface
    }

    /**
     * Interface fonctionnelle pour les opérations de base de données
     */
    @FunctionalInterface
    private interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
    }

    public static List<suiviGrossesse> getSuivisByPatientFromCSV(String filePath, int patientId) throws IOException {
        List<suiviGrossesse> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && Integer.parseInt(parts[6]) == patientId) {
                    suiviGrossesse s = new suiviGrossesse();
                    s.setId(Integer.parseInt(parts[0]));
                    s.setDateSuivi(java.sql.Date.valueOf(parts[1]));
                    s.setPoids(Double.parseDouble(parts[2]));
                    s.setTension(Double.parseDouble(parts[3]));
                    s.setSymptomes(parts[4]);
                    s.setEtatGrossesse(parts[5]);
                    s.setPatientId(Integer.parseInt(parts[6]));
                    result.add(s);
                }
            }
        }
        return result;
    }

    public String getPatientStats(int patientId) throws SQLException {
        List<suiviGrossesse> patientSuivis = getSuivisByPatientId(patientId);
        return SuiviGrossesseStatsUtil.generateStatsForPatient(patientSuivis, patientId);
    }
}