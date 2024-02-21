export type Turn = {
  id: string;
  type: "PROMPT" | "IMAGE";
  content: string;
  createdAt: Date;
};
