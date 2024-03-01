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

  const copyCurrentUrlToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(window.location.href);
      console.log("URL copied to clipboard!");
    } catch (err) {
      console.error("Failed to copy URL:", err);
    }
  };

  if (!lobby) {
    return (
      <div className="flex h-96 items-center sm:h-144">
        <Spinner size="xl" />
      </div>
    );
  }

  return (
    <div className="flex w-full flex-col items-center gap-y-3 sm:w-144 sm:gap-y-5">
      <ul className="flex min-h-96 flex-col items-center justify-center divide-y divide-indigo-200 text-lg font-light sm:min-h-144">
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
        ) && <Button onClick={joinLobby}>Join</Button>}
      {props.loggedInUser &&
        lobby?.host?.id !== props.loggedInUser?.id &&
        lobby?.players.some(
          (player) => player?.id === props.loggedInUser?.id,
        ) && <Button onClick={leaveLobby}>Leave</Button>}
      {lobby?.host?.id === props.loggedInUser?.id && (
        <div className="flex w-full flex-col items-center justify-between gap-y-3 sm:flex-row sm:justify-between sm:gap-x-5">
          <div className="w-full">
            <Button onClick={startGame}>Play</Button>
          </div>
          <div className="flex w-full flex-row flex-nowrap justify-center">
            <Button onClick={copyCurrentUrlToClipboard}>
              Invite
              <span id="default-icon">
                <svg
                  className="h-8 w-8"
                  aria-hidden="true"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="currentColor"
                  viewBox="0 0 18 20"
                >
                  <path d="M16 1h-3.278A1.992 1.992 0 0 0 11 0H7a1.993 1.993 0 0 0-1.722 1H2a2 2 0 0 0-2 2v15a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V3a2 2 0 0 0-2-2Zm-3 14H5a1 1 0 0 1 0-2h8a1 1 0 0 1 0 2Zm0-4H5a1 1 0 0 1 0-2h8a1 1 0 1 1 0 2Zm0-5H5a1 1 0 0 1 0-2h2V2h4v2h2a1 1 0 1 1 0 2Z" />
                </svg>
              </span>
              <span
                id="success-icon"
                className="inline-flex hidden items-center"
              >
                <svg
                  className="h-8 w-8 text-blue-700 dark:text-blue-500"
                  aria-hidden="true"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 16 12"
                >
                  <path
                    stroke="currentColor"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M1 5.917 5.724 10.5 15 1.5"
                  />
                </svg>
              </span>
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
