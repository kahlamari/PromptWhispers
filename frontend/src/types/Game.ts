import {Turn} from "./Turn.ts";
import {User} from "./User.ts";

export type Game = {
  id: string;
  rounds: Turn[][];
  players: User[];
  createdAt: Date;
  gameState: | "NEW"
      | "REQUEST_NEW_PROMPTS"
      | "WAIT_FOR_PROMPTS"
      | "WAIT_FOR_IMAGES"
      | "FINISHED";
};
