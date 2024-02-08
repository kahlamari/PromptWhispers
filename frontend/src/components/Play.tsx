import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { PromptCreate } from "../types/PromptCreate";
import { useParams } from "react-router-dom";
import axios from "axios";
import { Game } from "../types/Game.tsx";
import { Step } from "../types/Step.tsx";

export default function Play() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;
  const [prompt, setPrompt] = useState<string>("");
  const [game, setGame] = useState<Game>();
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);

  const submitPrompt = async (
    promptToSubmit: PromptCreate,
  ): Promise<Game | undefined> => {
    try {
      const response = await axios.post<Game>(
        `/api/game/${gameId}/prompt`,
        promptToSubmit,
      );
      return response.data;
    } catch (e) {
      console.log(e);
      return undefined;
    }
  };

  const requestImageGeneration = async (): Promise<Game | undefined> => {
    try {
      const response = await axios.post<Game>(
        `/api/game/${gameId}/generateImage`,
      );
      return response.data;
    } catch (e) {
      console.log(e);
      return undefined;
    }
  };

  const getGame = (gameId: string) => {
    axios.get<Game>(`/api/game/${gameId}`).then((response) => {
      setGame(response.data);
    });
  };

  const onPromptChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPrompt(event.target.value);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const promptToSubmit: PromptCreate = {
      prompt,
    };

    setInputDisabled(true);
    submitPrompt(promptToSubmit)
      .then(() => {
        return requestImageGeneration();
      })
      .then((gameWithImage) => {
        setGame(gameWithImage);
        setPrompt("");
        setInputDisabled(false);
      });
  };

  const getLastImage = (): Step | undefined => {
    if (game) {
      // Due to the game's logic there needs to be a prompt before there can be an image
      if (game.steps.length < 2) {
        return undefined;
      }
      const lastStep: Step = game.steps[game.steps.length - 1];
      if (lastStep.type === "IMAGE") {
        return lastStep;
      }
    }
    return undefined;
  };

  useEffect(() => {
    if (gameId) {
      getGame(gameId);
    }
  }, []);

  return (
    <div className="flex flex-col items-center">
      {getLastImage() && (
        <img
          className="h-128 w-auto rounded-2xl"
          src={getLastImage()?.content}
        />
      )}
      <h1 className="m-5 text-center text-6xl font-bold text-gray-900">
        Enter your prompt!
      </h1>
      <form onSubmit={handleSubmit} className="flex">
        <textarea
          value={prompt}
          onChange={onPromptChange}
          rows={2}
          placeholder="The potato king leads an uprising"
          autoFocus={true}
          disabled={inputDisabled}
          className="mr-4 h-full w-auto resize-none rounded-2xl p-6 text-3xl text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 disabled:opacity-75"
        />
        <button
          type="submit"
          disabled={inputDisabled}
          className="w-auto justify-center rounded-2xl bg-indigo-600 px-10 py-6 text-3xl font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:opacity-75"
        >
          Done
        </button>
      </form>
    </div>
  );
}
