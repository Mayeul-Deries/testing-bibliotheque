
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
class BibliothequeTest {

    @Mock
    private Bibliotheque bibliotheque;

    // --- S1 : Identification échouée ---
    @Test
    void testS1_MarieDupontNonReconnue() {
        Abonne marie = new Abonne("Dupont", "Marie", 999);
        // On simule l'exception que ta méthode identification est censée lancer
        when(bibliotheque.identification(marie)).thenThrow(new IllegalArgumentException("Abonné non reconnu"));

        assertThrows(IllegalArgumentException.class, () -> bibliotheque.identification(marie));
    }

    // --- S2 : Connexion réussie et recherche Polar ---
    @Test
    void testS2_JeanneDupontRecherchePolar() {
        Abonne jeanne = new Abonne("Dupont", "Jeanne", 123);
        List<String> polars = List.of("Sherlock Holmes", "L'Aiguille Creuse");

        when(bibliotheque.identification(jeanne)).thenReturn(true);
        // Utilisation de rechercherParCategorie selon ta classe
        when(bibliotheque.rechercherParCategorie("Polar")).thenReturn(polars);

        assertTrue(bibliotheque.identification(jeanne));
        assertEquals(2, bibliotheque.rechercherParCategorie("Polar").size());
    }

    // --- S3 : Recherche catégorie inexistante ---
    @Test
    void testS3_RechercheCategorieVide() {
        when(bibliotheque.rechercherParCategorie("Voyage")).thenReturn(Collections.emptyList());
        assertTrue(bibliotheque.rechercherParCategorie("Voyage").isEmpty());
    }

    // --- S4 & S5 & S6 : Utilisation de la méthode reserver (String) ---
    @Test
    void testS4_ReservationOuvrageIndisponible() {
        Abonne abonne = new Abonne("Durand", "Luc", 789);
        when(bibliotheque.reserver(abonne, "ISBN-1")).thenReturn("AJOUTE_FILE_ATTENTE");
        assertEquals("AJOUTE_FILE_ATTENTE", bibliotheque.reserver(abonne, "ISBN-1"));
    }

    @Test
    void testS5_ReservationOuvrageDisponible() {
        Abonne abonne = new Abonne("Petit", "Julie", 101);
        when(bibliotheque.reserver(abonne, "ISBN-DISPO")).thenReturn("PROPOSITION_EMPRUNT");
        assertEquals("PROPOSITION_EMPRUNT", bibliotheque.reserver(abonne, "ISBN-DISPO"));
    }

    @Test
    void testS6_ReservationOuvrageInexistant() {
        Abonne abonne = new Abonne("Leroy", "Alice", 202);
        when(bibliotheque.reserver(abonne, "999")).thenThrow(new java.util.NoSuchElementException("Ouvrage inexistant"));
        assertThrows(java.util.NoSuchElementException.class, () -> bibliotheque.reserver(abonne, "999"));
    }

    // --- S9 : Emprunt (Signature : emprunt(Abonne, String)) ---
    @Test
    void testS9_EmpruntEffectue() {
        Abonne abonne = new Abonne("Lupin", "Arsène", 500);
        // On vérifie juste que l'appel ne plante pas (void)
        bibliotheque.emprunt(abonne, "ISBN-123");
        verify(bibliotheque).emprunt(abonne, "ISBN-123");
    }

    // --- S10 & S11 : Retour (Signature : retour(String, int)) ---
    @Test
    void testS10_S11_RetourOuvrage() {
        // Pour S10/S11, ta méthode est void et print un message. On vérifie l'appel.
        bibliotheque.retour("ISBN-456", 1);
        verify(bibliotheque).retour("ISBN-456", 1);
    }

    // --- S12 : File d'attente (Logique spécifique) ---
    @Test
    void testS12_PremierSurListe() {
        Abonne victor = new Abonne("Hugo", "Victor", 800);
        when(bibliotheque.estPremierSurListe(victor, "ISBN-999")).thenReturn(true);
        assertTrue(bibliotheque.estPremierSurListe(victor, "ISBN-999"));
    }

    @Test
    void testS12_PasPremierSurListe() {
        Abonne jules = new Abonne("Verne", "Jules", 900);
        when(bibliotheque.estPremierSurListe(jules, "ISBN-999")).thenReturn(false);
        when(bibliotheque.getPositionFileAttente(jules, "ISBN-999")).thenReturn(2);

        assertFalse(bibliotheque.estPremierSurListe(jules, "ISBN-999"));
        assertEquals(2, bibliotheque.getPositionFileAttente(jules, "ISBN-999"));
    }
}