

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import src.main.Abonne;
import src.main.Bibliotheque;

@ExtendWith(MockitoExtension.class)
class TestBibliotheque {

    @Mock
    private Bibliotheque bibliotheque;

    @BeforeEach
    void setUp() {
        bibliotheque = new Bibliotheque();
    }

    // Scenario S1 
    @Test
    void testS1_MarieDupontNonReconnue() {
        Abonne abonne = new Abonne("Dupont", "Marie", 999);
        when(bibliotheque.identification(abonne))
            .thenThrow(new IllegalArgumentException("User not found"));

        assertThrows(IllegalArgumentException.class, () -> {
            bibliotheque.identification(abonne);
        });
    }
}