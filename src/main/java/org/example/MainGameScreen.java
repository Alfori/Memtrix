package org.example;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.backends.lwjgl.audio.Mp3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

public class MainGameScreen implements Screen, InputProcessor {
    private Mp3.Sound correctSound;
    private Mp3.Sound incorrectSound;
    private float currentDisplayTime;
    private float displayTimeIncrement = 10f;
    private final MemoryMatrixGame game;
    private Texture blockTexture;
    //private Texture blockTexture = null; // Texture for grid blocks
    public  int gridSize = 2;
    private int sequenceNumber = 1;
    public  int[][] patternMatrix = new int[gridSize][gridSize];
    public  int[][] playerInputMatrix = new int[gridSize][gridSize];
    private float blockSize = 100; // Assuming a fixed block size, adjust as needed
    private float offsetX;
    private float offsetY;
    public int score = 0;
    private String feedbackMessage = "";

    private int inputCount = 0;

    private BitmapFont font;
    // private final boolean[][] patternMatrix = new boolean[gridSize][gridSize];
    public  int[][] originalPatternMatrix = new int[gridSize][gridSize];

    private final float displayTime = 3f;
    private static final Logger logger = Logger.getLogger(MainGameScreen.class.getName());

    // Inside MainGameScreen.java
    public int getScore() {
        return score;
    }


    enum GameState {
        SHOWING_PATTERN,
        AWAITING_INPUT,
        VALIDATING_INPUT
    }
    private GameState currentState = GameState.SHOWING_PATTERN;
    public MainGameScreen(final MemoryMatrixGame game) {
        this.game = game;
        blockTexture = new Texture(Gdx.files.internal("block.png"));
        Gdx.input.setInputProcessor(this);
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        generatePattern();
        correctSound = (Mp3.Sound) Gdx.audio.newSound(Gdx.files.internal("correct-83487.mp3"));
        incorrectSound = (Mp3.Sound) Gdx.audio.newSound(Gdx.files.internal("buzzer-or-wrong-answer-20582.mp3"));
        offsetX = (Gdx.graphics.getWidth() - (gridSize * blockSize + (gridSize - 1) * 5)) / 2;
        offsetY = (Gdx.graphics.getHeight() - (gridSize * blockSize + (gridSize - 1) * 5)) / 2;
        Gdx.input.setInputProcessor(this);
    }

    public void updateScore() {
        score += 100;  // Increment score by 100 points for each correct pattern
        Gdx.app.log("Score", "Current score: " + score);
    }
    public boolean shouldIncreaseLevel() {
        return score % 400 == 0;
    }
    public void increaseLevel() {
        int previousGridSize = gridSize;
        int newGridSize = gridSize + 1;
        if (newGridSize > 4) newGridSize = 4; // Max grid size of 4x4
        if (previousGridSize == 2 && gridSize == 3) {
            currentDisplayTime += 5f;
        }
        resizeMatrices(newGridSize);
        resetForNextRound();
        generatePattern(); // Generate a new pattern with the updated grid size
        Gdx.app.log("Game", "Level increased to " + gridSize + "x" + gridSize);
        feedbackMessage = "Level increased to " + gridSize + "x" + gridSize; // Add this line
    }




    public void generatePattern() {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= gridSize * gridSize; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        int index = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                patternMatrix[i][j] = numbers.get(index);
                // Ensuring deep copy
                if (originalPatternMatrix[i] == null) {
                    originalPatternMatrix[i] = new int[gridSize];
                }
                originalPatternMatrix[i][j] = patternMatrix[i][j];
                index++;
            }
        }
        displayPattern();
        Timer.schedule(new Task(){
            @Override
            public void run() {
                hidePattern();
                currentState = GameState.AWAITING_INPUT;
            }
        }, displayTime);
    }


    private void displayPattern() {

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (patternMatrix[i][j] > 0) {

                    System.out.print("X ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }
    private void resizeMatrices(int newSize) {
        int[][] newPatternMatrix = new int[newSize][newSize];
        int[][] newOriginalPatternMatrix = new int[newSize][newSize];
        int[][] newPlayerInputMatrix = new int[newSize][newSize];

        for (int i = 0; i < Math.min(gridSize, newSize); i++) {
            System.arraycopy(patternMatrix[i], 0, newPatternMatrix[i], 0, Math.min(patternMatrix[i].length, newSize));
            System.arraycopy(originalPatternMatrix[i], 0, newOriginalPatternMatrix[i], 0, Math.min(originalPatternMatrix[i].length, newSize));
            System.arraycopy(playerInputMatrix[i], 0, newPlayerInputMatrix[i], 0, Math.min(playerInputMatrix[i].length, newSize));
        }

        // Assign the newly sized arrays back to the class variables
        patternMatrix = newPatternMatrix;
        originalPatternMatrix = newOriginalPatternMatrix;
        playerInputMatrix = newPlayerInputMatrix;

        // just not necessary
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                if (i >= gridSize || j >= gridSize) {

                    patternMatrix[i][j] = 0;
                    originalPatternMatrix[i][j] = 0;
                    playerInputMatrix[i][j] = 0;
                }
            }
        }
        gridSize = newSize;
    }

    private void hidePattern() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                patternMatrix[i][j] = 0;
            }
        }
    }
    public boolean validatePattern() {
        boolean isValid = true;
        StringBuilder originalPatternLog = new StringBuilder("Original Pattern:\n");
        StringBuilder playerInputLog = new StringBuilder("Player Input:\n");

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                originalPatternLog.append(originalPatternMatrix[i][j]).append(" ");
                playerInputLog.append(playerInputMatrix[i][j]).append(" ");
                // Validate
                if (originalPatternMatrix[i][j] != playerInputMatrix[i][j]) {
                    Gdx.app.log("Validation", "Mismatch found at (" + i + ", " + j + ")");
                    isValid = false; // There is a discrepancy in the input
                }
            }
            originalPatternLog.append("\n");
            playerInputLog.append("\n");
        }

        if (!isValid) {
            Gdx.app.log("Validation", originalPatternLog.toString());
            Gdx.app.log("Validation", playerInputLog.toString());
            return false; // Return false if any mismatch found
        }

        return true; // All inputs match
    }

    public void checkPlayerInput() {
        boolean isCorrect = validatePattern();
        if (isCorrect) {
            feedbackMessage = "Correct pattern!";
            updateScore();  // Update the score for correct pattern
            if (shouldIncreaseLevel()) {  // Check if it's time to increase the level
                increaseLevel();
            } else {
                resetForNextRound();
            }
        } else {
            Gdx.app.log("Game", "Incorrect pattern. Try again!");
            resetForNextRound();
        }
        // Ensure to reset for next round after handling level increase
        Timer.schedule(new Task() {
            @Override
            public void run() {
                resetForNextRound();
            }
        }, 0);
    }




    private void resetForNextRound() {
        for (int i = 0; i < gridSize; i++) {
            Arrays.fill(playerInputMatrix[i], 0); // Clear player inputs
            Arrays.fill(originalPatternMatrix[i], 0); // Clear original pattern
        }
        sequenceNumber = 1;
        inputCount = 0;
        currentState = GameState.SHOWING_PATTERN;
        generatePattern();
        Timer.schedule(new Timer.Task(){
            @Override
            public void run() {
                feedbackMessage = "";
            }
        }, 5); // Delay of 2 seconds before clearing the message
    }
    @Override
    public void show() {
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Reserve space for the message at the bottom of the screen
        int messageSpace = 25; // Adjust the space you want for the message

        // Dynamically calculate block size and offsets to center the grid
        float blockGap = 5; // The gap between blocks
        blockSize = Math.min((screenWidth - (blockGap * (gridSize - 1))) / gridSize,
                (screenHeight - messageSpace - (blockGap * (gridSize - 1))) / gridSize);

        offsetX = (screenWidth - (gridSize * blockSize + (gridSize - 1) * blockGap)) / 2;
        offsetY = (screenHeight - messageSpace - (gridSize * blockSize + (gridSize - 1) * blockGap)) / 2;

        game.batch.begin();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // Correction for vertical flipping of rows
                int displayY = gridSize - 1 - i;
                float x = offsetX + j * (blockSize + blockGap);
                float y = offsetY + displayY * (blockSize + blockGap);
                game.batch.draw(blockTexture, x, y, blockSize, blockSize);
                // Draw numbers centered in blocks
                font.draw(game.batch, Integer.toString(patternMatrix[i][j]),
                        x + blockSize / 2 - font.getSpaceXadvance() / 2,
                        y + blockSize / 2 + font.getCapHeight() / 2);
            }
        }
        font.setColor(Color.BLACK);
        font.draw(game.batch, "Score: " + score, 10, screenHeight - 10);

        if ("Incorrect pattern. Try again!".equals(feedbackMessage)) {
            font.setColor(Color.RED);
        } else if ("Correct pattern!".equals(feedbackMessage)) {
            font.setColor(Color.GREEN);
        } else {
            font.setColor(Color.RED);
        }
        font.draw(game.batch, feedbackMessage, Gdx.graphics.getWidth() / 2f - 50, Gdx.graphics.getHeight() - 5);
        game.batch.end();
    }

    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (currentState != GameState.AWAITING_INPUT) {
            return false; // Ignore clicks if not awaiting player input
        }

        int screenHeight = Gdx.graphics.getHeight();
        int yInverted = screenHeight - screenY;
        int i = (int)((screenX - offsetX) / (blockSize + 5)); // Column index
        int j = gridSize - 1 - (int)((yInverted - offsetY) / (blockSize + 5));


        if (i >= 0 && i < gridSize && j >= 0 && j < gridSize) {
            if (playerInputMatrix[j][i] != 0) {
                Gdx.app.log("Input", "Cell (" + j + ", " + i + ") already selected.");
                feedbackMessage = "Cell already selected!";
                return false; // This cell has already been selected
            }
            playerInputMatrix[j][i] = sequenceNumber++; // Assign the current sequence number
            Gdx.app.log("Input", "Registered touch at (" + i + ", " + j + ") with sequence number " + playerInputMatrix[j][i]);

            if (sequenceNumber > gridSize * gridSize) { // Check if all cells have been touched
                if (validatePattern()) {
                    correctSound.play();
                    Gdx.app.log("Game", "Correct pattern!");
                    feedbackMessage = "Correct pattern!";
                    checkPlayerInput();
                } else {
                    incorrectSound.play();
                    Gdx.app.log("Game", "Incorrect pattern. Try again!");
                    feedbackMessage = "Incorrect pattern. Try again!";
                }

                resetForNextRound();
            }
        }
        return true;
    }


// may be we will use this
    private void logPlayerInputMatrix() {
        StringBuilder matrixString = new StringBuilder();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                matrixString.append(playerInputMatrix[i][j]).append(" ");
            }
            matrixString.append("\n");
        }
        Gdx.app.log("PlayerInputMatrix", "\n" + matrixString.toString());
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    public void dispose() {
        // Dispose the block texture if it was successfully loaded
        if (blockTexture != null) {
            blockTexture.dispose();
        }
        if (blockTexture != null) {
            blockTexture.dispose();
        }
        correctSound.dispose();
        incorrectSound.dispose();
    }
}
