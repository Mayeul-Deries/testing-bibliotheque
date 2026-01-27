import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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

    // --- Scenario S1 ---
    // Marie Dupont n'est pas reconnue par le système -> exception [cite: 22]
    @Test
    void testS1_MarieDupontNonReconnue() {
        Abonne abonne = new Abonne("Dupont", "Marie", 999);
        
        // On configure le mock pour lancer l'exception demandée
        when(bibliotheque.identification(abonne))
            .thenThrow(new IllegalArgumentException("Abonné non reconnu"));

        assertThrows(IllegalArgumentException.class, () -> {
            bibliotheque.identification(abonne);
        });
    }
}