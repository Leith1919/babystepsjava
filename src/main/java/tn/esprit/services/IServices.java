package tn.esprit.services;

import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.entities.user;

import java.sql.SQLException;
import java.util.List;

public interface IServices<T> {
    void supprimer(int id) throws SQLException;

    void ajouter(T t) throws SQLException;



    void modifier(user user) throws SQLException;

    void supprimer(T t) throws SQLException;
    List<T> recuperer() throws SQLException;

    void modifier(suiviBebe suivi) throws SQLException;

    void modifier(suiviGrossesse suivi) throws SQLException;
}
