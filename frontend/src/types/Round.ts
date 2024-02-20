import { Turn } from "./Turn.ts";

export type Round = {
  gameId: string;
  turns: Turn[];
  gameState: "NEW" | "PROMPT_PHASE" | "IMAGE_PHASE" | "FINISHED";
};
