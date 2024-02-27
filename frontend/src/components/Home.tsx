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
    <div className="flex flex-col items-center">
      <img
        className="w-auto rounded-2xl p-2"
        alt="get players excited"
        src={pickRandomImage()}
      />
      <Button onClick={startGame} caption="Play" />
      <Button onClick={startLobby} caption="Start Lobby" />
    </div>
  );
}
