import { Turn } from "./Turn.ts";

export type Game = {
  id: string;
  turns: Turn[];
  createdAt: Date;
  isFinished: boolean;
};
