package tn.esprit.services;

import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;

import java.sql.SQLException;
import java.util.List;

public interface IServices<T> {
    void ajouter( T t) throws SQLException;
    void supprimer(T t) throws SQLException;
    List<T> recuperer() throws SQLException;

    void modifier(suiviBebe suivi) throws SQLException;
}
