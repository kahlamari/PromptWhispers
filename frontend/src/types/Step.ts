export type Step = {
  id: string;
  type: "PROMPT" | "IMAGE";
  content: string;
  createdAt: Date;
};
