package tn.esprit.utils;

import tn.esprit.entities.suiviGrossesse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;

public class SuiviGrossesseCSVUtil {
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 2000;
    private static final String HEADER = "id,date_suivi,poids,tension,symptomes,etat_grossesse,patient_id\n";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static synchronized void saveAllToCSV(List<suiviGrossesse> suivis, String filePath) throws IOException {
        System.out.println("Début de l'écriture du CSV dans : " + filePath);
        System.out.println("Nombre de suivis à écrire : " + suivis.size());

        // Create backup of existing file
        Path originalPath = Paths.get(filePath);
        Path backupPath = Paths.get(filePath + ".backup");
        Path tempPath = Paths.get(filePath + ".tmp");

        // Create parent directories if they don't exist
        Files.createDirectories(originalPath.getParent());

        // First try: Using RandomAccessFile with file channel locking
        if (tryWriteWithRandomAccess(suivis, filePath)) {
            return;
        }

        // Second try: Using temporary file approach
        if (tryWriteWithTempFile(suivis, originalPath, tempPath)) {
            return;
        }

        // Final fallback: Write to backup file
        if (tryWriteToBackup(suivis, backupPath)) {
            System.out.println("Données sauvegardées dans le fichier de backup: " + backupPath);
            return;
        }

        throw new IOException("Impossible d'écrire les données après avoir essayé toutes les méthodes");
    }

    private static boolean tryWriteWithRandomAccess(List<suiviGrossesse> suivis, String filePath) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try (RandomAccessFile file = new RandomAccessFile(filePath, "rw");
                 FileChannel channel = file.getChannel();
                 FileLock lock = channel.tryLock()) {
                
                if (lock != null) {
                    // Clear the file
                    channel.truncate(0);
                    
                    // Write header
                    file.write(HEADER.getBytes(StandardCharsets.UTF_8));
                    
                    // Write data
                    for (suiviGrossesse s : suivis) {
                        String line = formatSuiviLine(s);
                        file.write(line.getBytes(StandardCharsets.UTF_8));
                    }
                    
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Tentative " + (i + 1) + " avec RandomAccessFile échouée: " + e.getMessage());
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return false;
    }

    private static boolean tryWriteWithTempFile(List<suiviGrossesse> suivis, Path originalPath, Path tempPath) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                // Write to temp file
                try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    writer.write(HEADER);
                    for (suiviGrossesse s : suivis) {
                        writer.write(formatSuiviLine(s));
                    }
                }

                // Try to replace original file
                Files.move(tempPath, originalPath, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                System.err.println("Tentative " + (i + 1) + " avec fichier temporaire échouée: " + e.getMessage());
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    System.err.println("Impossible de supprimer le fichier temporaire: " + e.getMessage());
                }
            }
        }
        return false;
    }

    private static boolean tryWriteToBackup(List<suiviGrossesse> suivis, Path backupPath) {
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(backupPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(HEADER);
                for (suiviGrossesse s : suivis) {
                    writer.write(formatSuiviLine(s));
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Échec de l'écriture du fichier de backup: " + e.getMessage());
            return false;
        }
    }

    private static String formatSuiviLine(suiviGrossesse s) {
        return String.format("%d,%s,%.2f,%.2f,%s,%s,%d\n",
                s.getId(),
                DATE_FORMAT.format(s.getDateSuivi()),
                s.getPoids(),
                s.getTension(),
                s.getSymptomes() != null ? s.getSymptomes().replace(",", ";") : "",
                s.getEtatGrossesse() != null ? s.getEtatGrossesse().replace(",", ";") : "",
                s.getPatientId());
    }

    public static synchronized void appendToCSV(suiviGrossesse s, String filePath) throws IOException {
        List<suiviGrossesse> existingSuivis = readAllFromCSV(filePath);
        existingSuivis.add(s);
        saveAllToCSV(existingSuivis, filePath);
    }

    private static List<suiviGrossesse> readAllFromCSV(String filePath) throws IOException {
        List<suiviGrossesse> suivis = new java.util.ArrayList<>();
        Path path = Paths.get(filePath);
        Path backupPath = Paths.get(filePath + ".backup");
        
        // Try reading from original file first
        if (tryReadFromFile(path, suivis)) {
            return suivis;
        }
        
        // If original file fails, try reading from backup
        if (Files.exists(backupPath) && tryReadFromFile(backupPath, suivis)) {
            return suivis;
        }
        
        return suivis;
    }

    private static boolean tryReadFromFile(Path path, List<suiviGrossesse> suivis) {
        if (!Files.exists(path)) {
            return false;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {
                        suiviGrossesse suivi = new suiviGrossesse();
                        suivi.setId(Integer.parseInt(parts[0].trim()));
                        suivi.setDateSuivi(java.sql.Date.valueOf(parts[1].trim()));
                        suivi.setPoids(Double.parseDouble(parts[2].trim()));
                        suivi.setTension(Double.parseDouble(parts[3].trim()));
                        suivi.setSymptomes(parts[4].trim());
                        suivi.setEtatGrossesse(parts[5].trim());
                        suivi.setPatientId(Integer.parseInt(parts[6].trim()));
                        suivis.add(suivi);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la lecture de la ligne: " + line + "\n" + e.getMessage());
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier " + path + ": " + e.getMessage());
            return false;
        }
    }
}