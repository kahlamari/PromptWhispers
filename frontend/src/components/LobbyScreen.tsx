import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";
import { Lobby } from "../types/Lobby.ts";
import { User } from "../types/User.ts";
import { Round } from "../types/Round.ts";

type LobbyScreenProps = {
  readonly loggedInUser: User;
};

export default function LobbyScreen(props: LobbyScreenProps) {
  const params = useParams();
  const lobbyId: string | undefined = params.lobbyId;
  const [lobby, setLobby] = useState<Lobby | undefined | null>(undefined);
  const navigate = useNavigate();

  const getLobby = (lobbyId: string) => {
    axios.get<Lobby>(`/api/lobbies/${lobbyId}`).then((response) => {
      const lobbyData: Lobby = response.data;
      setLobby(lobbyData);
      if (lobbyData.isGameStarted) {
        navigate(`/play/${lobbyData.gameId}`);
      }
    });
  };

  const joinLobby = () => {
    if (!lobbyId) return null;
    axios
      .put<Lobby>(`/api/lobbies/${lobbyId}/join`)
      .then(() => getLobby(lobbyId));
  };

  const leaveLobby = () => {
    if (!lobbyId) return null;
    axios
      .put<Lobby>(`/api/lobbies/${lobbyId}/leave`)
      .then(() => getLobby(lobbyId));
  };

  const startGame = () => {
    axios
      .post<Round>(`/api/games`, lobby)
      .then((response) => navigate(`/play/${response.data.gameId}`));
  };

  useEffect(() => {
    const interval = setInterval(() => {
      if (lobbyId) {
        getLobby(lobbyId);
      }
    }, 5000);

    return () => {
      clearInterval(interval);
    };
  }, [lobbyId]);

  return (
    <div className="flex flex-col items-center">
      <ul>
        {lobby?.players.map((player) => (
          <li key={player?.id}>{player?.email}</li>
        ))}
      </ul>
      {props.loggedInUser &&
        lobby?.host?.id !== props.loggedInUser?.id &&
        !lobby?.players.some(
          (player) => player?.id === props.loggedInUser?.id,
        ) && (
          <button
            className="w-48 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            type="button"
            onClick={joinLobby}
          >
            Join!
          </button>
        )}
      {props.loggedInUser &&
        lobby?.host?.id !== props.loggedInUser?.id &&
        lobby?.players.some(
          (player) => player?.id === props.loggedInUser?.id,
        ) && (
          <button
            className="w-48 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            type="button"
            onClick={leaveLobby}
          >
            Leave!
          </button>
        )}
      {lobby?.host?.id === props.loggedInUser?.id && (
        <button
          className="w-48 flex-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          type="button"
          onClick={startGame}
        >
          Play!
        </button>
      )}
    </div>
  );
}
