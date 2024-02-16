import { User } from "./User.ts";

export type Lobby = {
  id: string;
  host: User;
  players: User[];
  isGameStarted: boolean;
  isGameFinished: boolean;
  createdAt: Date;
};
