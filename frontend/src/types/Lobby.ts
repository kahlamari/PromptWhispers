import { User } from "./User.ts";

export type Lobby = {
  id: string;
  host: User;
  players: User[];
  gameId: string;
  isGameStarted: boolean;
  isGameFinished: boolean;
  createdAt: Date;
};
