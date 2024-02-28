import axios from "axios";
import { Game } from "../types/Game.ts";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Turn } from "../types/Turn.ts";
import { User } from "../types/User.ts";

export default function GameReplay() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;
  const [game, setGame] = useState<Game | undefined | null>(undefined);
  const [activeRoundTab, setActiveRoundTab] = useState<number>(0);

  const handleTabClick = (tabId: number) => {
    setActiveRoundTab(tabId);
  };

  useEffect(() => {
    if (gameId) {
      axios.get<Game>(`/api/games/${gameId}/all`).then((response) => {
        setGame(response.data);
      });
    }
  }, [gameId]);

  if (game?.rounds) {
    return (
      <div>
        <div className="mb-4 border-b border-gray-200 dark:border-gray-700">
          <ul className="-mb-px flex flex-wrap text-center text-sm font-medium">
            {game?.players.map((player: User, index: number) => (
              <li key={index} className="me-2">
                <button
                  className={`inline-block rounded-t-lg border-b-2 p-4 ${activeRoundTab === index ? "border-indigo-500 text-indigo-600" : "hover:border-gray-300 hover:text-gray-600 dark:hover:text-gray-300"}`}
                  onClick={() => handleTabClick(index)}
                  type="button"
                >
                  {player?.email}
                </button>
              </li>
            ))}
          </ul>
        </div>
        <div>
          {game.rounds.map((turns: Turn[], index: number) => (
            <div
              key={index}
              className={`${activeRoundTab === index ? "block" : "hidden"} flex flex-col gap-y-5 p-4`}
            >
              {turns.map((turn: Turn) => (
                <div key={turn.id}>
                  {turn.type === "PROMPT" ? (
                    <textarea
                      value={turn.content}
                      rows={2}
                      placeholder="The potato king leads an uprising"
                      autoFocus={true}
                      disabled={true}
                      className="h-full w-full resize-none rounded-2xl bg-gray-50 p-6 text-3xl text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 disabled:opacity-75"
                    />
                  ) : (
                    <img
                      className="w-svw rounded-2xl"
                      alt="generated based on previous prompt"
                      src={turn.content}
                    />
                  )}
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    );
  }
}
