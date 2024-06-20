import {Game} from "../types/Game.ts";
import {Turn} from "../types/Turn.ts";

export const getNumberOfCompletedImageTurns = (game: Game): number => {
    let minTurnNumber = game.players.length;

    if (game.players.length != game.rounds.length) {
        return 0;
    }

    game.rounds.forEach((turns: Turn[]) => {
        minTurnNumber = Math.min(minTurnNumber, turns.filter(turn => turn.type === 'IMAGE').length)
    });

    return minTurnNumber;
}

export const isGameFinished = (game: Game): boolean => {
    return game.gameState == 'FINISHED' || getNumberOfCompletedImageTurns(game) >= game.players.length;
}

