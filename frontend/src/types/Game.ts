import { Turn } from "./Turn.ts";
import { User } from "./User.ts";

export type Game = {
  id: string;
  rounds: Turn[][];
  players: User[];
  createdAt: Date;
  gameState: "NEW" | "PROMPT_PHASE" | "IMAGE_PHASE" | "FINISHED";
};
