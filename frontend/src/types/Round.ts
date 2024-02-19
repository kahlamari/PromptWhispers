import { Turn } from "./Turn.ts";

export type Round = {
  gameId: string;
  turns: Turn[];
  isGameFinished: boolean;
};
