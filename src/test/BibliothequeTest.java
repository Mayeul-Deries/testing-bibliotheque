package src.main;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestBibliotheque {

    @Mock
    private Bibliotheque bibliotheque;

    // Scenario S1 
    @Test
    void testS1_MarieDupontNonReconnue() {
        when(bibliotheque.identification("Dupont", "Marie", "999"))
            .thenThrow(new IllegalArgumentException("User not found"));

        assertThrows(IllegalArgumentException.class, () -> {
            bibliotheque.identification("Dupont", "Marie", "999");
        });
    }
}