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

  return (
    <div className="flex flex-col items-center">
      <Button onClick={startGame} caption="Play" />
      <Button onClick={startLobby} caption="Start Lobby" />
    </div>
  );
}
