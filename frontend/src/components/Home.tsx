import axios from "axios";
import { Game } from "../types/Game.ts";
import { useNavigate } from "react-router-dom";

export default function Home() {
  const navigate = useNavigate();
  const startGame = () => {
    axios.post<Game>("/api/game/start").then((response) => {
      navigate(`/play/${response.data.id}`);
    });
  };

  return (
    <div className="flex flex-col items-center">
      <h1 className="flex-auto pb-5 text-center text-6xl font-bold leading-9 tracking-tight text-indigo-600">
        Prompt Whispers
      </h1>
      <button
        className="w-48 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        type="button"
        onClick={startGame}
      >
        Play
      </button>
    </div>
  );
}
