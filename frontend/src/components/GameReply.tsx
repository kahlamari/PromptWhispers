import axios from "axios";
import { Game } from "../types/Game.ts";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

export default function GameReply() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;
  const [game, setGame] = useState<Game | undefined | null>(undefined);
  const getGame = (gameId: string) => {
    axios.get<Game>(`/api/games/${gameId}`).then((response) => {
      setGame(response.data);
    });
  };

  useEffect(() => {
    if (gameId) {
      getGame(gameId);
    }
  }, [gameId]);

  return (
    <div className="flex flex-col items-center">
      {game?.steps.map((step) => (
        <div key={step.id}>
          {step.type === "PROMPT" ? (
            <textarea
              value={step.content}
              rows={2}
              placeholder="The potato king leads an uprising"
              autoFocus={true}
              disabled={true}
              className="mr-4 h-full w-auto resize-none rounded-2xl p-6 text-3xl text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 disabled:opacity-75"
            />
          ) : (
            <img
              className="h-128 w-auto rounded-2xl"
              alt="generated based on previous prompt"
              src={step.content}
            />
          )}
        </div>
      ))}
    </div>
  );
}
