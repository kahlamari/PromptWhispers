package in.kahl.promptwhispers.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void withStepTest_whenProvidingStep_thenReturnGameWithStep() {
        // ARRANGE
        Game newGame = new Game();
        Turn turnPrompt = new Turn(TurnType.PROMPT, "A hedge jumps over a sheep");
        Game gameExpected = new Game(newGame.id(), List.of(turnPrompt), newGame.createdAt(), newGame.isFinished());

        // ACT
        Game gameActual = newGame.withTurn(turnPrompt);

        // ASSERT
        assertEquals(gameExpected, gameActual);
    }

    @Test
    void withStepTest_whenMaxStepsReached_thenGameIsFinished() {
        // ARRANGE
        Game gameWith2Images = new Game()
                .withTurn(new Turn(TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image1.png"))
                .withTurn(new Turn(TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(TurnType.IMAGE, "image2.png"))
                .withTurn(new Turn(TurnType.PROMPT, "3rd prompt"));

        // ACT
        Game finishedGame = gameWith2Images.withTurn(new Turn(TurnType.IMAGE, "image3.png"));

        // ASSERT
        assertTrue(finishedGame.isFinished());
    }

    @Test
    void withStepTest_whenAddingStepToFinishedGame_thenThrowException() {
        // ARRANGE
        Game finishedGame = new Game()
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