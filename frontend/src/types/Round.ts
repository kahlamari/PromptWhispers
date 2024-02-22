import { Turn } from "./Turn.ts";

export type Round = {
  gameId: string;
  turns: Turn[];
  gameState:
    | "NEW"
    | "REQUEST_NEW_PROMPTS"
    | "WAIT_FOR_PROMPTS"
    | "WAIT_FOR_IMAGES"
    | "FINISHED";
};
