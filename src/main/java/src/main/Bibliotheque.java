package src.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Bibliotheque {
    private List<Abonne> listeAbonnes = new ArrayList<>();
    private Map<String, List<Integer>> exemplairesDisponibles = new HashMap<>();
    private Map<String, Abonne> emprunteurs = new HashMap<>();
    private Map<String, LocalDate> datesRetour = new HashMap<>();
    private Map<String, Queue<Abonne>> filesAttente = new HashMap<>();

    public boolean identification(Abonne a) {
        if (!listeAbonnes.contains(a)) {
            throw new IllegalArgumentException("Abonné non reconnu");
        }
        return true;
    }

    public List<String> rechercher(String categorie) {
        return new ArrayList<>(); 
    }

    public void reservation(Abonne a, String isbn) {
        if (!exemplairesDisponibles.containsKey(isbn)) {
            throw new NoSuchElementException("Ouvrage inexistant");
        }
        
        if (!exemplairesDisponibles.get(isbn).isEmpty()) {
            System.out.println("Ouvrage disponible, veuillez l'emprunter.");
            return;
        }

        filesAttente.computeIfAbsent(isbn, k -> new LinkedList<>()).add(a);
    }

    public void emprunt(Abonne a, String isbn) {
        if (filesAttente.containsKey(isbn) && !filesAttente.get(isbn).peek().equals(a)) {
            throw new IllegalStateException("Vous n'êtes pas premier sur la liste.");
        }

        if (exemplairesDisponibles.get(isbn).isEmpty()) {
            throw new IllegalStateException("Plus d'exemplaire disponible.");
        }

        int numExemplaire = exemplairesDisponibles.get(isbn).remove(0);
        String clef = isbn + "-" + numExemplaire;
        
        emprunteurs.put(clef, a);
        datesRetour.put(clef, LocalDate.now().plusMonths(1));
        
        if (filesAttente.containsKey(isbn)) filesAttente.get(isbn).poll();
    }

    public void retour(String isbn, int numExemplaire) {
        String clef = isbn + "-" + numExemplaire;
        
        if (datesRetour.get(clef).isBefore(LocalDate.now())) {
            System.out.println("Retour en retard !");
        }

        emprunteurs.remove(clef);
        datesRetour.remove(clef);
        exemplairesDisponibles.get(isbn).add(numExemplaire);
    }

    public void ajouterAbonne(Abonne a) { listeAbonnes.add(a); }
    public void ajouterOuvrage(String isbn, int num) {
        exemplairesDisponibles.computeIfAbsent(isbn, k -> new ArrayList<>()).add(num);
    }
}