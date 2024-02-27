import axios from "axios";
import { Game } from "../types/Game.ts";
import { useNavigate } from "react-router-dom";
import { Lobby } from "../types/Lobby.ts";
import Button from "../ui-components/Button.tsx";

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
    <div className="sm:w-144 m-3 flex h-full w-full flex-col items-center gap-y-3 sm:m-5 sm:gap-y-5">
      <img
        className="w-svw rounded-2xl"
        alt="get players excited"
        src={pickRandomImage()}
      />
      <div className="flex w-full flex-col gap-y-3 sm:flex-row sm:justify-between sm:gap-x-5">
        <Button onClick={startGame}>Play Solo</Button>
        <Button onClick={startLobby}>Play Multiplayer</Button>
      </div>
    </div>
  );
}
