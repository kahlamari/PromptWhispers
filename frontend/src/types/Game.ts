import { Turn } from "./Turn.ts";

export type Game = {
  id: string;
  rounds: Turn[][];
  createdAt: Date;
  gameState: "NEW" | "PROMPT_PHASE" | "IMAGE_PHASE" | "FINISHED";
};
