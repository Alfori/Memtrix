package memoryMatrix;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.example.MainGameScreen;
import org.example.MemoryMatrixGame;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainGameScreenTest {
    private static MainGameScreen screen;
    private static MemoryMatrixGame game;
    private static SpriteBatch mockBatch;
    private static SpriteBatch mockTexture;

    @BeforeAll
    public static void setUp() {
        // Mock the LibGDX environment
        Gdx.gl = (GL20) mock(GL20.class);
        Gdx.gl20 = (GL20) mock(GL20.class);
        Gdx.graphics = mock(Graphics.class);
        Gdx.files = (Files) mock(Files.class);

        // Mock specific behavior if necessary (e.g., file handling)
        FileHandle mockFileHandle = (FileHandle) mock(FileHandle.class);
        assert Gdx.files != null;
        Object clone;

        assert mockFileHandle != null;

        // Ensure you mock any other interactions expected during the test

        // Setup the game and screens
        game = new MemoryMatrixGame();
        screen = new MainGameScreen(game);
    }

    private static Object when(byte[] internal) {
        return null;
    }

    private static Graphics mock(Object spriteBatchClass) {
        return null;
    }

    @Test
    public void testPatternGeneration() {
        screen.generatePattern();
        boolean isNotEmpty = false;
        for (int i = 0; i < screen.gridSize; i++) {
            for (int j = 0; j < screen.gridSize; j++) {
                if (screen.patternMatrix[i][j] != 0) {
                    isNotEmpty = true;
                    break;
                }
            }
        }
        assertTrue(isNotEmpty, "Pattern matrix should be populated with non-zero values");
    }

    @Test
    public void testValidatePatternCorrectInput() {
        screen.generatePattern();
        for (int i = 0; i < screen.gridSize; i++) {
            System.arraycopy(screen.patternMatrix[i], 0, screen.playerInputMatrix[i], 0, screen.gridSize);
        }
        assertTrue(screen.validatePattern(), "Validation should pass when input matches pattern");
    }

    @Test
    public void testValidatePatternIncorrectInput() {
        screen.generatePattern();
        if (screen.gridSize > 0) {
            screen.playerInputMatrix[0][0] = screen.patternMatrix[0][0] + 1; // Change the first cell to be incorrect
        }
        assertFalse(screen.validatePattern(), "Validation should fail when input does not match pattern");
    }
}