package services.User;

import models.User;
import models.suiviBebe;
import models.suiviGrossesse;

import java.sql.SQLException;
import java.util.List;

public interface IServices<T> {
    void ajouter (T t) throws SQLException;

    void modifier (T t) throws SQLException;

    void modifier(User user) throws SQLException;

    void supprimer (T t) throws SQLException;

    void supprimer(int id) throws SQLException;


    List<T> recuperer() throws SQLException;

    void inscription(User user) throws SQLException;

    void modifier(suiviGrossesse suivi) throws SQLException;

    void modifier(suiviBebe suivi) throws SQLException;

    void supprimerD(T t) throws SQLException;
    List<T> afficherDisponibilite() throws SQLException;
    void modifierD(T t) throws SQLException;
}

    //void editprofile(User user) throws SQLException;

