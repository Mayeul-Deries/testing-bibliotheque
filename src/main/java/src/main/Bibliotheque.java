package src.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Système d'information pour une petite bibliothèque municipale.
 */
public class Bibliotheque {
    private List<Abonne> listeAbonnes = new ArrayList<>();
    private Map<String, List<Integer>> exemplairesDisponibles = new HashMap<>();
    private Map<String, Abonne> emprunteurs = new HashMap<>();
    private Map<String, LocalDate> datesRetour = new HashMap<>();
    private Map<String, Queue<Abonne>> filesAttente = new HashMap<>();
    private Map<String, String> categoriesParIsbn = new HashMap<>();
    private Map<String, String> titresParIsbn = new HashMap<>();
    
    private java.time.Clock clock = java.time.Clock.systemDefaultZone();

    public void setClock(java.time.Clock clock) {
        this.clock = clock;
    }

    /**
     * Identification d'un abonné.
     */
    public boolean identification(Abonne a) {
        if (!listeAbonnes.contains(a)) {
            throw new IllegalArgumentException("Abonné non reconnu");
        }
        return true;
    }

    /**
     * Recherche par catégorie.
     */
    public List<String> rechercher(String categorie) {
        List<String> resultats = new ArrayList<>();
        for (Map.Entry<String, String> entry : categoriesParIsbn.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(categorie)) {
                resultats.add(titresParIsbn.getOrDefault(entry.getKey(), "Titre inconnu (" + entry.getKey() + ")"));
            }
        }
        return resultats;
    }

    /**
     * Réservation d'un ouvrage.
     */
    public String reserver(Abonne a, String isbn) {
        if (!exemplairesDisponibles.containsKey(isbn)) {
            throw new NoSuchElementException("Ouvrage inexistant");
        }
        
        List<Integer> dispos = exemplairesDisponibles.get(isbn);
        if (dispos != null && !dispos.isEmpty()) {
            return "Ouvrage disponible, veuillez l'emprunter.";
        }

        Queue<Abonne> file = filesAttente.computeIfAbsent(isbn, k -> new LinkedList<>());
        if (!file.contains(a)) {
            file.add(a);
        }
        return "Réservation enregistrée";
    }

    /**
     * Emprunt d'un ouvrage.
     */
    public void emprunt(Abonne a, String isbn) {
        identification(a);
        if (!exemplairesDisponibles.containsKey(isbn)) {
            throw new NoSuchElementException("ISBN inconnu");
        }

        Queue<Abonne> file = filesAttente.get(isbn);
        if (file != null && !file.isEmpty()) {
            if (!file.peek().equals(a)) {
                throw new IllegalStateException("Vous n'êtes pas premier sur la liste.");
            }
            file.poll();
        }

        List<Integer> dispos = exemplairesDisponibles.get(isbn);
        if (dispos == null || dispos.isEmpty()) {
            throw new IllegalStateException("Plus d'exemplaire disponible.");
        }

        int numExemplaire = dispos.remove(0);
        String clef = isbn + "-" + numExemplaire;
        
        emprunteurs.put(clef, a);
        datesRetour.put(clef, LocalDate.now(clock).plusMonths(1));
    }

    /**
     * Retour d'un ouvrage.
     */
    public void retour(String isbn, int numExemplaire) {
        String clef = isbn + "-" + numExemplaire;
        if (!emprunteurs.containsKey(clef)) return;

        if (datesRetour.get(clef).isBefore(LocalDate.now(clock))) {
            System.out.println("Retour en retard !");
        }

        emprunteurs.remove(clef);
        datesRetour.remove(clef);
        
        // S12: Si quelqu'un attend, lui attribuer automatiquement
        Queue<Abonne> file = filesAttente.get(isbn);
        if (file != null && !file.isEmpty()) {
            Abonne premierEnAttente = file.poll();
            emprunteurs.put(clef, premierEnAttente);
            datesRetour.put(clef, LocalDate.now(clock).plusMonths(1));
        } else {
            // Sinon, remettre en stock
            exemplairesDisponibles.get(isbn).add(numExemplaire);
        }
    }

    /**
     * Liste des emprunts en retard pour un abonné.
     */
    public List<String> getEmpruntsEnRetard(Abonne a) {
        List<String> retards = new ArrayList<>();
        LocalDate now = LocalDate.now(clock);
        for (Map.Entry<String, Abonne> entry : emprunteurs.entrySet()) {
            if (entry.getValue().equals(a)) {
                String clef = entry.getKey();
                if (datesRetour.get(clef).isBefore(now)) {
                    int lastDash = clef.lastIndexOf('-');
                    String isbn = lastDash > 0 ? clef.substring(0, lastDash) : clef;
                    retards.add(isbn);
                }
            }
        }
        return retards;
    }


    /**
     * Vérifie la position dans la file d'attente.
     */
    public boolean estPremierSurListe(Abonne a, String isbn) {
        Queue<Abonne> file = filesAttente.get(isbn);
        return file != null && !file.isEmpty() && file.peek().equals(a);
    }

    /**
     * Retourne la position dans la file d'attente (1-based).
     */
    public int getPositionFileAttente(Abonne a, String isbn) {
        Queue<Abonne> file = filesAttente.get(isbn);
        if (file == null) return 0;
        int pos = 1;
        for (Abonne ab : file) {
            if (ab.equals(a)) return pos;
            pos++;
        }
        return 0;
    }

    // Méthodes utilitaires pour peupler la bibliothèque

    public void ajouterAbonne(Abonne a) { 
        listeAbonnes.add(a); 
    }

    /**
     * Retourne le nombre d'exemplaires disponibles pour un ISBN donné.
     */
    public int getNombreExemplairesDisponibles(String isbn) {
        List<Integer> dispos = exemplairesDisponibles.get(isbn);
        return dispos != null ? dispos.size() : 0;
    }
    
    public void ajouterOuvrage(String isbn, int num) {
        exemplairesDisponibles.computeIfAbsent(isbn, k -> new ArrayList<>()).add(num);
    }

    public void ajouterOuvrage(String isbn, int num, String titre, String categorie) {
        ajouterOuvrage(isbn, num);
        titresParIsbn.put(isbn, titre);
        categoriesParIsbn.put(isbn, categorie);
    }
}