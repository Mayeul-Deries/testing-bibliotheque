package src.main;

import java.time.LocalDate;
import java.util.List;

public class Bibliotheque {

    public boolean identification(Abonne abonne) {
        throw new UnsupportedOperationException("S1-S2: Méthode identification non implémentée");
    }

    public List<String> getEmpruntsEnRetard(Abonne a) {
        throw new UnsupportedOperationException("S7: Méthode getEmpruntsEnRetard non implémentée");
    }

    public List<String> getEmpruntsEnRetardAu(Abonne a, LocalDate d) {
        throw new UnsupportedOperationException("S8: Méthode getEmpruntsEnRetardAu non implémentée");
    }

    public boolean emprunter(Abonne a, String isbn) {
        throw new UnsupportedOperationException("S9-S12: Méthode emprunter non implémentée");
    }

    public String retournerOuvrage(String isbn, LocalDate date) {
        throw new UnsupportedOperationException("S10-S11: Méthode retournerOuvrage non implémentée");
    }

    public boolean estPremierSurListe(Abonne a, String isbn) {
        throw new UnsupportedOperationException("S12: Méthode estPremierSurListe non implémentée");
    }

    public int getPositionFileAttente(Abonne a, String isbn) {
        throw new UnsupportedOperationException("S12: Méthode getPositionFileAttente non implémentée");
    }
    
    public List<String> rechercherParCategorie(String categorie) {
        throw new UnsupportedOperationException("S2-S3: Méthode rechercherParCategorie non implémentée");
    }
}