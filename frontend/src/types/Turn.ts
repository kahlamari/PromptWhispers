import { User } from "./User.ts";

export type Turn = {
  id: string;
  type: "PROMPT" | "IMAGE";
  content: string;
  player: User;
  createdAt: Date;
};
