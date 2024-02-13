import { Game } from "../types/Game.ts";
import axios from "axios";

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function GameHistory() {
  const navigate = useNavigate();
  const [games, setGames] = useState<Game[]>([]);

  function getGames() {
    axios.get<Game[]>("/api/games").then((response) => {
      setGames(response.data);
    });
  }

  function deleteGame(gameId: string) {
    axios.delete(`/api/games/${gameId}`).then(() => getGames());
  }

  useEffect(() => {
    getGames();
  }, []);
  return (
    <div className="relative overflow-x-auto">
      <table className="w-full text-left text-sm text-gray-500 rtl:text-right dark:text-gray-400">
        <thead className="bg-gray-50 text-xs uppercase text-gray-700 dark:bg-gray-700 dark:text-gray-400">
          <tr>
            <th scope="col" className="px-6 py-3">
              Game
            </th>
            <th scope="col" className="px-6 py-3">
              Steps
            </th>
            <th scope="col" className="px-6 py-3">
              Finished
            </th>
            <th scope="col" className="px-6 py-3">
              Delete
            </th>
          </tr>
        </thead>
        <tbody>
          {games.map((game) => (
            <tr
              key={game.id}
              onClick={() => navigate(`${game.id}`)}
              className="cursor-pointer border-b bg-white dark:border-gray-700 dark:bg-gray-800"
            >
              <th
                scope="row"
                className="whitespace-nowrap px-6 py-4 font-medium text-gray-900 dark:text-white"
              >
                {game.id}
              </th>
              <td className="px-6 py-4">{game.steps.length}</td>
              <td className="px-6 py-4">
                {game.isFinished ? "Completed" : "Not finished"}
              </td>
              <td className="px-6 py-4">
                <button onClick={() => deleteGame(game.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
