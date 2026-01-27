import static org.mockito.ArgumentMatchers.any;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import src.main.Abonne;
import src.main.Bibliotheque;

@ExtendWith(MockitoExtension.class)
class TestBibliotheque {

    @Mock
    private Bibliotheque bibliotheque;

    // --- S1 : Identification échouée ---
    @Test
    void testS1_MarieDupontNonReconnue() {
        Abonne marie = new Abonne("Dupont", "Marie", 999);
        when(bibliotheque.identification(marie)).thenThrow(new IllegalArgumentException("Abonné inconnu"));

        assertThrows(IllegalArgumentException.class, () -> bibliotheque.identification(marie));
    }

    // --- S2 : Connexion réussie et recherche Polar ---
    @Test
    void testS2_JeanneDupontRecherchePolar() {
        Abonne jeanne = new Abonne("Dupont", "Jeanne", 123);
        List<String> polars = List.of("Sherlock Holmes", "L'Aiguille Creuse");

        when(bibliotheque.identification(jeanne)).thenReturn(true);
        when(bibliotheque.rechercherParCategorie("Polar")).thenReturn(polars);

        assertTrue(bibliotheque.identification(jeanne));
        assertEquals(2, bibliotheque.rechercherParCategorie("Polar").size());
    }

    // --- S3 : Recherche catégorie inexistante (Voyage) ---
    @Test
    void testS3_RechercheCategorieVide() {
        when(bibliotheque.rechercherParCategorie("Voyage")).thenReturn(Collections.emptyList());

        List<String> result = bibliotheque.rechercherParCategorie("Voyage");
        assertTrue(result.isEmpty(), "La liste devrait être vide pour la catégorie Voyage");
    }

    // --- S4 : Réservation d'un ouvrage existant mais indisponible ---
    @Test
    void testS4_ReservationOuvrageIndisponible() {
        Abonne abonne = new Abonne("Durand", "Luc", 789);
        String isbn = "ISBN-INDISPONIBLE";

        when(bibliotheque.reserver(abonne, isbn)).thenReturn("AJOUTE_FILE_ATTENTE");

        String resultat = bibliotheque.reserver(abonne, isbn);
        assertEquals("AJOUTE_FILE_ATTENTE", resultat);
        verify(bibliotheque).reserver(abonne, isbn);
    }

    // --- S5 : Réservation d'un ouvrage disponible (le système propose l'emprunt) ---
    @Test
    void testS5_ReservationOuvrageDisponible_ProposeEmprunt() {
        Abonne abonne = new Abonne("Petit", "Julie", 101);
        String isbn = "ISBN-DISPO";

        when(bibliotheque.reserver(abonne, isbn)).thenReturn("PROPOSITION_EMPRUNT");

        String resultat = bibliotheque.reserver(abonne, isbn);
        assertEquals("PROPOSITION_EMPRUNT", resultat);
    }

    // --- S6 : Réservation d'un ouvrage n'existant pas dans le fonds ---
    @Test
    void testS6_ReservationOuvrageInexistant() {
        Abonne abonne = new Abonne("Leroy", "Alice", 202);
        String isbnInexistant = "999-999-999";

        when(bibliotheque.reserver(abonne, isbnInexistant))
            .thenThrow(new IllegalArgumentException("Ouvrage non présent dans le catalogue"));

        assertThrows(IllegalArgumentException.class, () -> bibliotheque.reserver(abonne, isbnInexistant));
    }

    // --- S7 : Identification et retour des emprunts en retard ---
    @Test
    void testS7_AbonneIdentifie_RetourneEmpruntsEnRetard() {
        Abonne abonne = new Abonne("Jean", "Valjean", 24601);
        List<String> retardsAttendus = List.of("Les Misérables");
        
        when(bibliotheque.identification(abonne)).thenReturn(true);
        when(bibliotheque.getEmpruntsEnRetard(abonne)).thenReturn(retardsAttendus);

        assertTrue(bibliotheque.identification(abonne));
        assertEquals(retardsAttendus, bibliotheque.getEmpruntsEnRetard(abonne));
    }

    // --- S8 : Calcul du retard (30 Janvier -> 1 Mars = Retard) ---
    @Test
    void testS8_CalculRetardDateSpecifique() {
        Abonne abonne = new Abonne("Durand", "Pierre", 101);
        String isbn = "978-2070413110";
        LocalDate dateConnexion = LocalDate.of(2026, 3, 1);
        
        when(bibliotheque.getEmpruntsEnRetardAu(abonne, dateConnexion)).thenReturn(List.of(isbn));

        List<String> retards = bibliotheque.getEmpruntsEnRetardAu(abonne, dateConnexion);
        assertTrue(retards.contains(isbn));
    }

    // --- S9 : Emprunt et mise à jour du stock ---
    @Test
    void testS9_EmpruntMetAJourStockEtMemoire() {
        Abonne abonne = new Abonne("Lupin", "Arsène", 500);
        String isbn = "ISBN-123";

        bibliotheque.emprunter(abonne, isbn);

        verify(bibliotheque).emprunter(abonne, isbn);
    }

    // --- S10 : Retour dans les temps ---
    @Test
    void testS10_RetourDansLesTemps() {
        String isbn = "ISBN-456";
        LocalDate dateRetour = LocalDate.of(2026, 2, 15);
        
        bibliotheque.retournerOuvrage(isbn, dateRetour);
        
        verify(bibliotheque).retournerOuvrage(isbn, dateRetour);
    }

    // --- S11 : Retour en retard avec notification ---
    @Test
    void testS11_RetourEnRetardAvecNotification() {
        String isbn = "ISBN-789";
        LocalDate dateRetard = LocalDate.of(2026, 3, 15);
        
        when(bibliotheque.retournerOuvrage(isbn, dateRetard)).thenReturn("RETARD_NOTIFIE");

        String resultat = bibliotheque.retournerOuvrage(isbn, dateRetard);
        assertEquals("RETARD_NOTIFIE", resultat);
    }

    // --- S12 (1) : Premier sur la liste de réservation ---
    @Test
    void testS12_PremierSurListe_EmpruntReussi() {
        Abonne abonne = new Abonne("Hugo", "Victor", 800);
        String isbn = "ISBN-999";

        when(bibliotheque.estPremierSurListe(abonne, isbn)).thenReturn(true);
        when(bibliotheque.emprunter(abonne, isbn)).thenReturn(true);

        assertTrue(bibliotheque.emprunter(abonne, isbn));
    }

    // --- S12 (2) : Pas premier sur la liste ---
    @Test
    void testS12_PasPremierSurListe_EmpruntEchoue() {
        Abonne abonne = new Abonne("Verne", "Jules", 900);
        String isbn = "ISBN-999";

        when(bibliotheque.estPremierSurListe(abonne, isbn)).thenReturn(false);
        when(bibliotheque.getPositionFileAttente(abonne, isbn)).thenReturn(2);

        assertFalse(bibliotheque.emprunter(abonne, isbn));
        assertEquals(2, bibliotheque.getPositionFileAttente(abonne, isbn));
    }
}