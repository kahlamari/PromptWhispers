package in.kahl.promptwhispers.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {

    @Test
    void withStepTest_whenProvidingStep_thenReturnGameWithStep() {
        // ARRANGE
        Game newGame = new Game(null);
        Turn turnPrompt = new Turn(TurnType.PROMPT, "A hedge jumps over a sheep");
        Game gameExpected = new Game(newGame.id(),
                newGame.players(),
                Map.of(0, List.of(turnPrompt)),
                newGame.gameState(),
                newGame.createdAt());

        // ACT
        Game gameActual = newGame.withTurn(turnPrompt);

        // ASSERT
        assertEquals(gameExpected, gameActual);
    }

    @Test
    void withStepTest_whenMaxStepsReached_thenGameIsFinished() {
        // ARRANGE
        Game gameWith2Images = new Game(null)
                .withTurn(new Turn(TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image1.png"))
                .withTurn(new Turn(TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image2.png"))
                .withTurn(new Turn(TurnType.PROMPT, "3rd prompt"));

        // ACT
        Game finishedGame = gameWith2Images.withTurn(new Turn(TurnType.IMAGE, "image3.png"));

        // ASSERT
        assertEquals(GameState.FINISHED, finishedGame.gameState());
    }

    @Test
    void withStepTest_whenAddingStepToFinishedGame_thenThrowException() {
        // ARRANGE
        Game finishedGame = new Game(null)
                .withTurn(new Turn(TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image1.png"))
                .withTurn(new Turn(TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image2.png"))
                .withTurn(new Turn(TurnType.PROMPT, "3rd prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image3.png"));

        Turn prompt4 = new Turn(TurnType.PROMPT, "4th prompt");
        // ACT
        assertThrows(IllegalArgumentException.class, () ->
                // ASSERT
                finishedGame.withTurn(prompt4));
    }
}