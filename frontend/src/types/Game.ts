import { Step } from "./Step.ts";

export type Game = {
  id: string;
  steps: Step[];
  createdAt: Date;
  isFinished: boolean;
};
