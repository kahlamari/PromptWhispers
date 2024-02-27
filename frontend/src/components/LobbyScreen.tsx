import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import { Lobby } from "../types/Lobby.ts";
import { User } from "../types/User.ts";
import { Round } from "../types/Round.ts";
import Spinner from "../ui-components/Spinner.tsx";
import Button from "../ui-components/Button.tsx";

type LobbyScreenProps = {
  readonly loggedInUser: User;
};

export default function LobbyScreen(props: LobbyScreenProps) {
  const params = useParams();
  const lobbyId: string | undefined = params.lobbyId;
  const [lobby, setLobby] = useState<Lobby | undefined | null>(undefined);
  const navigate = useNavigate();

  const joinLobby = () => {
    if (!lobbyId) return null;
    axios
      .put<Lobby>(`/api/lobbies/${lobbyId}/join`)
      .then((response) => setLobby(response.data));
  };

  const leaveLobby = () => {
    if (!lobbyId) return null;
    axios
      .put<Lobby>(`/api/lobbies/${lobbyId}/leave`)
      .then((response) => setLobby(response.data));
  };

  const startGame = () => {
    axios
      .post<Round>(`/api/games`, lobby)
      .then((response) => navigate(`/play/${response.data.gameId}`));
  };

  useEffect(() => {
    const interval = setInterval(() => {
      axios
        .get<Lobby>(`/api/lobbies/${lobbyId}`)
        .then((response) => setLobby(response.data));
    }, 5000);

    return () => {
      clearInterval(interval);
    };
  }, [lobbyId]);

  useEffect(() => {
    if (lobby?.isGameStarted) {
      navigate(`/play/${lobby.gameId}`);
    }
  }, [lobby, navigate]);

  if (!lobby) {
    return <Spinner size="xl" />;
  }

  return (
    <div className="flex flex-col items-center">
      <ul className="mb-5 divide-y divide-indigo-200 text-lg font-light">
        {lobby?.players.map((player) => (
          <li key={player?.id}>{player?.email}</li>
        ))}
        <li className="flex items-center gap-2">
          <Spinner size="sm" />
          waiting for players
        </li>
      </ul>
      {props.loggedInUser &&
        lobby?.host?.id !== props.loggedInUser?.id &&
        !lobby?.players.some(
          (player) => player?.id === props.loggedInUser?.id,
        ) && <Button onClick={joinLobby} caption="Join!" />}
      {props.loggedInUser &&
        lobby?.host?.id !== props.loggedInUser?.id &&
        lobby?.players.some(
          (player) => player?.id === props.loggedInUser?.id,
        ) && <Button onClick={leaveLobby} caption="Leave!" />}
      {lobby?.host?.id === props.loggedInUser?.id && (
        <Button onClick={startGame} caption="Play!" />
      )}
    </div>
  );
}
