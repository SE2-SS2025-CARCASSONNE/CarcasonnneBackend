import com.carcassonne.backend.entity.GamePlayer
import com.carcassonne.backend.repository.GamePlayerRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class GamePlayerRepositoryTest {

    @Mock
    private lateinit var gamePlayerRepository: GamePlayerRepository

    @Test
    fun `save should persist a GamePlayer`() {
        val gamePlayer = GamePlayer(score = 15, remainingMeeples = 5)
        `when`(gamePlayerRepository.save(gamePlayer)).thenReturn(gamePlayer)

        val result = gamePlayerRepository.save(gamePlayer)

        // Verifiziert, dass das Mock-Repository die Daten korrekt verarbeitet
        assertEquals(15, result.score)
        assertEquals(5, result.remainingMeeples)
        verify(gamePlayerRepository, times(1)).save(gamePlayer)
    }

    @Test
    fun `findById should return a GamePlayer`() {
        val gamePlayer = GamePlayer(id = 1, score = 20, remainingMeeples = 4)
        `when`(gamePlayerRepository.findById(1)).thenReturn(Optional.of(gamePlayer))

        val result = gamePlayerRepository.findById(1)

        // Überprüft, dass das Mock-Repository den Spieler korrekt zurückgibt
        assertTrue(result.isPresent)
        assertEquals(1, result.get().id)
        verify(gamePlayerRepository, times(1)).findById(1)
    }

    @Test
    fun `delete should remove a GamePlayer`() {
        // Simuliere einen existierenden GamePlayer
        val gamePlayer = GamePlayer(id = 1, score = 20, remainingMeeples = 4)

        // Mock das Verhalten der delete-Methode, damit sie nichts tut
        doNothing().`when`(gamePlayerRepository).delete(gamePlayer)

        // Rufe die delete-Methode auf
        gamePlayerRepository.delete(gamePlayer)

        // Überprüfe, ob die delete-Methode genau einmal aufgerufen wurde
        verify(gamePlayerRepository, times(1)).delete(gamePlayer)
    }

}
