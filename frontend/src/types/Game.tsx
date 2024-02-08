export type Game = {
  id: string;
  steps: { [key: number]: [value: string] };
  createdAt: Date;
  isFinished: boolean;
};
