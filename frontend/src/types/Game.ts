import { Turn } from "./Turn.ts";

export type Game = {
  id: string;
  turns: { [key: number]: Turn[] };
  createdAt: Date;
  gameState: "NEW" | "PROMPT_PHASE" | "IMAGE_PHASE" | "FINISHED";
};
