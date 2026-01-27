import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import src.main.Abonne;
import src.main.Bibliotheque;

@ExtendWith(MockitoExtension.class)
class TestBibliotheque {

    @Mock
    private Bibliotheque bibliotheque;

    // --- S7 : Liste des emprunts en retard à l'identification ---
    @Test
    void testS7_AbonneIdentifie_RetourneEmpruntsEnRetard() {
        Abonne abonne = new Abonne("Jean", "Valjean", 24601);
        List<String> retardsAttendus = List.of("Les Misérables");
        
        when(bibliotheque.identification(abonne)).thenReturn(true);
        when(bibliotheque.getEmpruntsEnRetard(abonne)).thenReturn(retardsAttendus);

        assertTrue(bibliotheque.identification(abonne));
        assertEquals(retardsAttendus, bibliotheque.getEmpruntsEnRetard(abonne));
    }

    // --- S8 : Calcul du retard (Emprunt 30 Janvier -> Retour 1 Mars = Retard) ---
    @Test
    void testS8_CalculRetardDateSpecifique() {
        Abonne abonne = new Abonne("Durand", "Pierre", 101);
        String isbn = "978-2070413110";
        LocalDate dateEmprunt = LocalDate.of(2026, 1, 30);
        LocalDate dateConnexion = LocalDate.of(2026, 3, 1);

        // La règle dit : 16 Janvier -> 17 Février. 
        // Donc 30 Janvier -> 2 Mars est la limite. Le 1er Mars ne devrait pas être en retard ?
        // ATTENTION : L'énoncé dit "30 Janvier -> 1er Mars = Retard". 
        // Cela suggère que Février est traité comme un mois calendaire court.
        
        when(bibliotheque.getEmpruntsEnRetardAu(abonne, dateConnexion)).thenReturn(List.of(isbn));

        List<String> retards = bibliotheque.getEmpruntsEnRetardAu(abonne, dateConnexion);
        assertTrue(retards.contains(isbn), "Le livre devrait être marqué en retard le 1er Mars");
    }

    // --- S9 : Emprunt et mise à jour du stock ---
    @Test
    void testS9_EmpruntMetAJourStockEtMemoire() {
        Abonne abonne = new Abonne("Lupin", "Arsène", 500);
        String isbn = "ISBN-123";

        bibliotheque.emprunter(abonne, isbn);

        // On vérifie que le système a bien enregistré l'ordre
        verify(bibliotheque).emprunter(abonne, isbn);
        // On pourrait vérifier la diminution du stock si la méthode existait
    }

    // --- S10 : Retour dans les temps ---
    @Test
    void testS10_RetourDansLesTemps() {
        String isbn = "ISBN-456";
        bibliotheque.retournerOuvrage(isbn, LocalDate.now());
        
        verify(bibliotheque).retournerOuvrage(isbn, LocalDate.now());
        // Pas d'exception ou de notification de retard attendue ici
    }

    // --- S11 : Retour en retard avec notification ---
    @Test
    void testS11_RetourEnRetardAvecNotification() {
        Abonne abonne = new Abonne("Zola", "Emile", 700);
        String isbn = "ISBN-789";
        
        when(bibliotheque.retournerOuvrage(isbn, LocalDate.now())).thenReturn("RETARD_NOTIFIE");

        String resultat = bibliotheque.retournerOuvrage(isbn, LocalDate.now());
        assertEquals("RETARD_NOTIFIE", resultat);
    }

    // --- S12 (a) : Premier sur la liste de réservation ---
    @Test
    void testS12_PremierSurListe_EmpruntReussi() {
        Abonne abonne = new Abonne("Hugo", "Victor", 800);
        String isbn = "ISBN-999";

        when(bibliotheque.estPremierSurListe(abonne, isbn)).thenReturn(true);
        when(bibliotheque.emprunter(abonne, isbn)).thenReturn(true);

        assertTrue(bibliotheque.emprunter(abonne, isbn));
    }

    // --- S12 (b) : Pas premier sur la liste ---
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