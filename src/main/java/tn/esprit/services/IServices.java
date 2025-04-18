package tn.esprit.services;

import tn.esprit.entites.Disponibilite;

import java.sql.SQLException;
import java.util.List;

public interface IServices<T > {
    void ajouter(T t) throws SQLException;
    void supprimerD(T t) throws SQLException;
    List<T> afficherDisponibilite() throws SQLException;
    void modifierD(T t) throws SQLException;


}
