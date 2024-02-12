import { Game } from "../types/Game.ts";
import axios from "axios";

import { useEffect, useState } from "react";

export default function GameHistory() {
  const [games, setGames] = useState<Game[]>([]);

  function getGames() {
    axios.get<Game[]>("/api/games").then((response) => {
      setGames(response.data);
    });
  }

  useEffect(() => {
    getGames();
  }, []);
  return (
    <ul role="list" className="divide-y divide-gray-100">
      {games.map((game) => (
        <li key={game.id} className="flex justify-between gap-x-6 py-5">
          <div className="min-w-0 flex-auto">
            <p className="text-sm font-semibold leading-6 text-gray-900">
              {game.id}
            </p>
          </div>
        </li>
      ))}
    </ul>
  );
}
