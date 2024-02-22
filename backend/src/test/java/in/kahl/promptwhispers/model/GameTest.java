package in.kahl.promptwhispers.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {

    @Test
    void withTurnTest_whenProvidingTurn_thenReturnGameWithTurn() {
        // ARRANGE
        User user = new User("user@example.com");
        Game newGame = new Game(user);
        Turn turnPrompt = new Turn(TurnType.PROMPT, "A hedge jumps over a sheep");
        Game gameExpected = new Game(newGame.id(),
                newGame.players(),
                Map.of(0, List.of(turnPrompt)),
                GameState.REQUEST_NEW_PROMPTS,
                newGame.createdAt());

        // ACT
        Game gameActual = newGame.withTurn(turnPrompt);

        // ASSERT
        assertEquals(gameExpected, gameActual);
    }

    @Test
    void withTurnTest_whenAllRoundsEnded_thenGameIsFinished() {
        // ARRANGE
        User alice = new User("alice@example.com");
        User bob = new User("bob@example.com");
        Game gameWith1Image = new Game(alice)
                .withPlayer(bob)
                .withTurn(new Turn(alice, TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(bob, TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(alice, TurnType.IMAGE, "alice image 1.png"))
                .withTurn(new Turn(bob, TurnType.IMAGE, "bob image 1.png"))
                .withTurn(new Turn(alice, TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(bob, TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(alice, TurnType.IMAGE, "alice image 2.png"));

        // ACT
        Game finishedGame = gameWith1Image.withTurn(new Turn(bob, TurnType.IMAGE, "bob image2.png"));

        // ASSERT
        assertEquals(GameState.FINISHED, finishedGame.gameState());
    }

    @Test
    void withTurnTest_whenAddingTurnToFinishedGame_thenThrowException() {
        // ARRANGE
        User alice = new User("alice@example.com");
        User bob = new User("bob@example.com");
        Game finishedGame = new Game(alice)
                .withPlayer(bob)
                .withTurn(new Turn(alice, TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(bob, TurnType.PROMPT, "1st prompt"))
                .withTurn(new Turn(alice, TurnType.IMAGE, "alice image 1.png"))
                .withTurn(new Turn(bob, TurnType.IMAGE, "bob image 1.png"))
                .withTurn(new Turn(alice, TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(bob, TurnType.PROMPT, "2nd prompt"))
                .withTurn(new Turn(alice, TurnType.IMAGE, "alice image 2.png"))
                .withTurn(new Turn(bob, TurnType.IMAGE, "bob image 2.png"));

        Turn prompt4 = new Turn(alice, TurnType.PROMPT, "4th prompt");
        // ACT
        assertThrows(IllegalArgumentException.class, () ->
                // ASSERT
                finishedGame.withTurn(prompt4));
    }
}