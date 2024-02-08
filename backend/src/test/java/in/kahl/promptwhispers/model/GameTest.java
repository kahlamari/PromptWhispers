package in.kahl.promptwhispers.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void withStepTest_whenProvidingStep_thenReturnGameWithStep() {
        // ARRANGE
        Game newGame = new Game();
        Step stepPrompt = new Step(StepType.PROMPT, "A hedge jumps over a sheep");
        Game gameExpected = new Game(newGame.id(), List.of(stepPrompt), newGame.createdAt(), newGame.isFinished());

        // ACT
        Game gameActual = newGame.withStep(stepPrompt);

        // ASSERT
        assertEquals(gameExpected, gameActual);
    }

    @Test
    void withStepTest_whenMaxStepsReached_thenGameIsFinished() {
        // ARRANGE
        Game gameWith2Images = new Game()
                .withStep(new Step(StepType.PROMPT, "1st prompt"))
                .withStep(new Step(StepType.IMAGE, "image1.png"))
                .withStep(new Step(StepType.PROMPT, "2nd prompt"))
                .withStep(new Step(StepType.IMAGE, "image2.png"))
                .withStep(new Step(StepType.PROMPT, "3rd prompt"));

        // ACT
        Game finishedGame = gameWith2Images.withStep(new Step(StepType.IMAGE, "image3.png"));

        // ASSERT
        assertTrue(finishedGame.isFinished());
    }

    @Test
    void withStepTest_whenAddingStepToFinishedGame_thenThrowException() {
        // ARRANGE
        Game finishedGame = new Game()
                .withStep(new Step(StepType.PROMPT, "1st prompt"))
                .withStep(new Step(StepType.IMAGE, "image1.png"))
                .withStep(new Step(StepType.PROMPT, "2nd prompt"))
                .withStep(new Step(StepType.IMAGE, "image2.png"))
                .withStep(new Step(StepType.PROMPT, "3rd prompt"))
                .withStep(new Step(StepType.IMAGE, "image3.png"));

        Step prompt4 = new Step(StepType.PROMPT, "4th prompt");
        // ACT
        assertThrows(IllegalArgumentException.class, () ->
                // ASSERT
                finishedGame.withStep(prompt4));
    }
}