import { Step } from "./Step.tsx";

export type Game = {
  id: string;
  steps: Step[];
  createdAt: Date;
  isFinished: boolean;
};
