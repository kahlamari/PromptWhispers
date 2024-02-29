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
  const [visibleCount, setVisibleCount] = useState<number>(1);

  const handleTabClick = (tabId: number) => {
    setActiveRoundTab(tabId);
    if (activeRoundTab !== tabId) {
      setVisibleCount(1);
    }
  };

  useEffect(() => {
    if (gameId) {
      axios.get<Game>(`/api/games/${gameId}/all`).then((response) => {
        setGame(response.data);
      });
    }
  }, [gameId]);

  useEffect(() => {
    if (game) {
      const timer = setInterval(() => {
        setVisibleCount((prevCount) => {
          if (prevCount < game?.rounds[0].length) {
            return prevCount + 1;
          } else {
            clearInterval(timer);
            return prevCount;
          }
        });
      }, 2000);

      return () => clearInterval(timer);
    }
  }, [game, activeRoundTab]);

  if (game?.rounds) {
    return (
      <div>
        <div className="mb-4 border-b border-gray-200 dark:border-gray-700">
          <ul className="-mb-px flex flex-wrap text-center text-sm font-medium">
            {game?.players.map((player: User, index: number) => (
              <li key={player?.id} className="me-2">
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
              key={turns[index].id}
              className={`${activeRoundTab === index ? "block" : "hidden"} flex flex-col gap-y-5`}
            >
              {turns.slice(0, visibleCount).map((turn: Turn) => (
                <div key={turn.id}>
                  {turn.type === "PROMPT" ? (
                    <div className="flex items-start gap-2.5">
                      <img
                        className="h-12 w-12 rounded-full"
                        src={
                          game.players
                            .filter((user) => user?.id === turn.player?.id)
                            .pop()?.profilePicUrl
                        }
                        alt="Profile Pic"
                      />
                      <div className="flex flex-col gap-1">
                        <div className="leading-1.5 flex w-full flex-col rounded-e-2xl rounded-es-2xl border-gray-200 bg-gray-50 p-4">
                          <div className="mb-2 flex items-center space-x-2">
                            <span className="text-sm font-semibold text-gray-900">
                              {
                                game.players
                                  .filter(
                                    (user) => user?.id === turn.player?.id,
                                  )
                                  .pop()?.email
                              }
                            </span>
                          </div>
                          <p className="text-3xl font-normal text-gray-900">
                            {turn.content}
                          </p>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="flex items-start gap-2.5">
                      <img
                        className="h-12 w-12 rounded-full"
                        src="/openai-logo.png"
                        alt="Profile Pic"
                      />
                      <div className="flex flex-col gap-1">
                        <div className="leading-1.5 flex w-full flex-col rounded-e-2xl rounded-es-2xl border-gray-200 bg-gray-50 p-4">
                          <div className="mb-2 flex items-center space-x-2">
                            <span className="text-sm font-semibold text-gray-900">
                              Dall-E
                            </span>
                          </div>
                          <div className="my-1.5">
                            <img
                              src={turn.content}
                              className="rounded-2xl"
                              alt="generated"
                            />
                          </div>
                        </div>
                      </div>
                    </div>
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
