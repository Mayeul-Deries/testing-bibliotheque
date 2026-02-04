package src.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import src.main.Abonne;
import src.main.Bibliotheque;

/**
 * Tests d'intégration / acceptation pour la classe Bibliothèque.
 */
public class TestBibliotheque {

    private Bibliotheque bibliotheque;
    private Abonne mockAbonne;
    private final String ISBN_POLAR = "123-1";
    private final String TITRE_POLAR = "Sherlock Holmes";
    private final String CATEGORIE_POLAR = "Polar";

    @BeforeEach
    void setUp() {
        bibliotheque = new Bibliotheque();
        mockAbonne = mock(Abonne.class);
    }

    // S1
    @Test
    void testS1_IdentificationAbonneInconnu() {
        Abonne marie = new Abonne("Dupont", "Marie", 999);
        assertThrows(IllegalArgumentException.class, () -> {
            bibliotheque.identification(marie);
        });
    }

    //  S2
    @Test
    void testS2_IdentificationEtRecherchePolar() {
        Abonne jeanne = new Abonne("Dupont", "Jeanne", 2);
        bibliotheque.ajouterAbonne(jeanne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1, TITRE_POLAR, CATEGORIE_POLAR);

        assertTrue(bibliotheque.identification(jeanne));
        List<String> titres = bibliotheque.rechercher(CATEGORIE_POLAR);
        assertTrue(titres.contains(TITRE_POLAR));
    }

    // S3
    @Test
    void testS3_RechercheCategorieInexistante() {
        Abonne abonne = new Abonne("Jhon", "Doe", 10);
        bibliotheque.ajouterAbonne(abonne);
        assertTrue(bibliotheque.identification(abonne));

        List<String> resultats = bibliotheque.rechercher("Voyage");
        assertTrue(resultats.isEmpty());
    }

    // S4
    @Test
    void testS4_ReservationOuvrageIndisponible() {
        Abonne boris = new Abonne("Theron", "Boris", 3);
        bibliotheque.ajouterAbonne(boris);
        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        bibliotheque.emprunt(boris, ISBN_POLAR);

        bibliotheque.reserver(mockAbonne, ISBN_POLAR);
        assertEquals(1, bibliotheque.getPositionFileAttente(mockAbonne, ISBN_POLAR));
    }

    // S5
    @Test
    void testS5_ReservationOuvrageDisponible() {
        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        String res = bibliotheque.reserver(mockAbonne, ISBN_POLAR);
        assertEquals("Ouvrage disponible, veuillez l'emprunter.", res);
    }

    // S6
    @Test
    void testS6_ReservationOuvrageInexistant() {
        bibliotheque.ajouterAbonne(mockAbonne);
        assertThrows(NoSuchElementException.class, () -> {
            bibliotheque.reserver(mockAbonne, "111-1");
        });
    }

    // S7
    @Test
    void testS7_ListeEmpruntsEnRetard() {
        Clock startClock = Clock.fixed(Instant.parse("2026-01-30T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(startClock);

        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        // Emprunt le 30 janvier (date de retour = 28 février)
        bibliotheque.emprunt(mockAbonne, ISBN_POLAR);

        // Vérifier qu'il n'y a pas de retard immédiatement après l'emprunt
        List<String> retardsAvant = bibliotheque.getEmpruntsEnRetard(mockAbonne);
        assertTrue(retardsAvant.isEmpty(), "Pas de retard immédiatement après l'emprunt");

        // Avancer le temps au 1er mars (après la date de retour)
        Clock futureClock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(futureClock);

        // S7: L'abonné s'identifie et voit ses emprunts en retard
        assertTrue(bibliotheque.identification(mockAbonne));
        List<String> retards = bibliotheque.getEmpruntsEnRetard(mockAbonne);
        
        assertEquals(1, retards.size(), "Il devrait y avoir 1 emprunt en retard");
        assertTrue(retards.contains(ISBN_POLAR), "L'ISBN en retard devrait être " + ISBN_POLAR);
    }

    // S8
    @Test
    void testS8_RetardApresNouvelleIdentification() {
        Clock startClock = Clock.fixed(Instant.parse("2026-01-30T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(startClock);

        Abonne abonne = new Abonne("Martin", "Jean", 50);
        bibliotheque.ajouterAbonne(abonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        // S8: Emprunt le 30 janvier
        bibliotheque.emprunt(abonne, ISBN_POLAR);

        // Première identification : pas de retard
        assertTrue(bibliotheque.identification(abonne));
        List<String> retardsAvant = bibliotheque.getEmpruntsEnRetard(abonne);
        assertTrue(retardsAvant.isEmpty(), "Pas de retard le 30 janvier");

        // S8: L'abonné s'identifie à nouveau le 1er mars
        Clock marchClock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(marchClock);

        assertTrue(bibliotheque.identification(abonne));
        List<String> retardsApres = bibliotheque.getEmpruntsEnRetard(abonne);
        
        // Le livre doit figurer dans la liste des emprunts en retard
        assertEquals(1, retardsApres.size(), "Il devrait y avoir 1 emprunt en retard");
        assertTrue(retardsApres.contains(ISBN_POLAR), "Le livre emprunté devrait être en retard");
    }

    // S9
    @Test
    void testS9_StockMisAJourApresEmprunt() {
        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        bibliotheque.emprunt(mockAbonne, ISBN_POLAR);

        Abonne autre = new Abonne("John", "Doe", 100);
        bibliotheque.ajouterAbonne(autre);
        assertThrows(IllegalStateException.class, () -> {
            bibliotheque.emprunt(autre, ISBN_POLAR);
        });
    }

    // S10
    @Test
    void testS10_RetourOuvrageMetAJourStock() {
        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        // Vérifier stock initial : 1 exemplaire disponible
        assertEquals(1, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));
        bibliotheque.emprunt(mockAbonne, ISBN_POLAR);

        // Vérifier stock après emprunt : 0 exemplaire disponible
        assertEquals(0, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        bibliotheque.retour(ISBN_POLAR, 1);

        // Vérifier stock après retour : 1 exemplaire disponible à nouveau
        assertEquals(1, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        assertDoesNotThrow(() -> bibliotheque.emprunt(mockAbonne, ISBN_POLAR));
    }

    // S11
    @Test
    void testS11_RetourEnRetardEtNotification() {
        Clock startClock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(startClock);

        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        // Vérifier stock initial : 1 exemplaire disponible
        assertEquals(1, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        bibliotheque.emprunt(mockAbonne, ISBN_POLAR);

        // Vérifier stock après emprunt : 0 exemplaire disponible
        assertEquals(0, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        // Avancer le temps de 2 mois (date retour = 1er février, on est le 1er mars = retard)
        Clock futureClock = Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneId.systemDefault());
        bibliotheque.setClock(futureClock);

        // Vérifier que l'abonnné est notifié
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        bibliotheque.retour(ISBN_POLAR, 1);

        // Vérifier stock après retour : 1 exemplaire disponible à nouveau
        assertEquals(1, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        // Vérifier qu'une notification de retard a été émise
        String output = outputStream.toString();
        assertTrue(output.contains("Retour en retard"),
            "La notification de retard devrait être affichée");

        assertDoesNotThrow(() -> bibliotheque.emprunt(mockAbonne, ISBN_POLAR));
    }

    // S12a
    @Test
    void testS12a_EmpruntApresReservationSucces() {
        Abonne paul = new Abonne("Martin", "Paul", 3);
        bibliotheque.ajouterAbonne(paul);
        bibliotheque.ajouterAbonne(mockAbonne);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        // Vérifier stock initial : 1 exemplaire
        assertEquals(1, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        bibliotheque.emprunt(paul, ISBN_POLAR);

        // Stock après emprunt : 0
        assertEquals(0, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        bibliotheque.reserver(mockAbonne, ISBN_POLAR);

        // mockAbonne est premier sur la liste
        assertEquals(1, bibliotheque.getPositionFileAttente(mockAbonne, ISBN_POLAR));

        bibliotheque.retour(ISBN_POLAR, 1);

        // Après retour, le livre est automatiquement attribué à mockAbonne
        // Donc le stock reste à 0 (pas remis en disponible)
        assertEquals(0, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        // Vérifier qu'un autre abonné ne peut pas emprunter (déjà attribué à mockAbonne)
        Abonne autre = new Abonne("Autre", "Personne", 999);
        bibliotheque.ajouterAbonne(autre);
        assertThrows(IllegalStateException.class, () -> {
            bibliotheque.emprunt(autre, ISBN_POLAR);
        });
    }

    // S12b
    @Test
    void testS12b_EmpruntApresReservationEchecCarPasPremier() {
        // S12: Le livre est automatiquement attribué au premier, pas au second
        Abonne paul = new Abonne("Martin", "Paul", 3);
        Abonne res1 = new Abonne("R1", "U1", 101);
        Abonne res2 = new Abonne("R2", "U2", 102);

        bibliotheque.ajouterAbonne(paul);
        bibliotheque.ajouterAbonne(res1);
        bibliotheque.ajouterAbonne(res2);
        bibliotheque.ajouterOuvrage(ISBN_POLAR, 1);

        bibliotheque.emprunt(paul, ISBN_POLAR);
        bibliotheque.reserver(res1, ISBN_POLAR);
        bibliotheque.reserver(res2, ISBN_POLAR);

        // res1 est premier, res2 est second
        assertEquals(1, bibliotheque.getPositionFileAttente(res1, ISBN_POLAR));
        assertEquals(2, bibliotheque.getPositionFileAttente(res2, ISBN_POLAR));

        bibliotheque.retour(ISBN_POLAR, 1);

        // Après retour, le livre est automatiquement attribué à res1
        assertEquals(0, bibliotheque.getNombreExemplairesDisponibles(ISBN_POLAR));

        // res2 essaie d'emprunter mais res1 l'a déjà (automatiquement attribué)
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            bibliotheque.emprunt(res2, ISBN_POLAR);
        });
        assertTrue(ex.getMessage().contains("Plus d'exemplaire disponible") ||
                   ex.getMessage().contains("Vous n'êtes pas premier sur la liste."));
    }
}
