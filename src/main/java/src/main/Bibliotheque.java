package src.main;

import java.time.LocalDate;
import java.util.List;

public class Bibliotheque {

    public boolean identification(Abonne abonne) { return false; }
    public List<String> getEmpruntsEnRetard(Abonne a) { return null; }
    public List<String> getEmpruntsEnRetardAu(Abonne a, LocalDate d) { return null; }
    public boolean emprunter(Abonne a, String isbn) { return false; }
    public String retournerOuvrage(String isbn, LocalDate date) { return ""; }
    public boolean estPremierSurListe(Abonne a, String isbn) { return false; }
    public int getPositionFileAttente(Abonne a, String isbn) { return 0; }

}
