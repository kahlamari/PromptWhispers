import axios from "axios";
import { Game } from "../types/Game.ts";
import { useNavigate } from "react-router-dom";
import { Lobby } from "../types/Lobby.ts";

export default function Home() {
  const navigate = useNavigate();
  const startGame = () => {
    axios.post<Game>("/api/games/start").then((response) => {
      navigate(`/play/${response.data.id}`);
    });
  };

  const startLobby = () => {
    axios.post<Lobby>("/api/lobbies").then((response) => {
      navigate(`/lobby/${response.data.id}`);
    });
  };

  const pickRandomImage = (): string => {
    const images: string[] = ["fries", "jedibroccoli", "potatoking"];

    const image = images[Math.floor(Math.random() * images.length)];

    return "/starter-images/" + image + ".webp";
  };

  return (
    <div className="flex flex-col items-center justify-between">
      <img
        className="w-auto rounded-2xl p-2"
        alt="image to get players excited"
        src={pickRandomImage()}
      />
      <button
        className="m-2 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        type="button"
        onClick={startGame}
      >
        Play
      </button>
      <button
        className="m-2 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        type="button"
        onClick={startLobby}
      >
        Start Lobby
      </button>
    </div>
  );
}
