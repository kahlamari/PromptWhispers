import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import { PromptCreate } from "../types/PromptCreate.ts";
import { Link, useParams } from "react-router-dom";
import axios from "axios";
import { Turn } from "../types/Turn.ts";
import { Round } from "../types/Round.ts";

export default function Play() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;
  const [prompt, setPrompt] = useState<string>("");
  const [round, setRound] = useState<Round>();
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);
  const [waitingForImage, setWaitingForImage] = useState<boolean>(false);

  const submitPrompt = async (
    promptToSubmit: PromptCreate,
  ): Promise<Round | undefined> => {
    try {
      const response = await axios.post<Round>(
        `/api/games/${gameId}/prompt`,
        promptToSubmit,
      );
      return response.data;
    } catch (e) {
      console.log(e);
      return undefined;
    }
  };

  const requestImageGeneration = async (): Promise<Round | undefined> => {
    try {
      const response = await axios.post<Round>(
        `/api/games/${gameId}/generateImage`,
      );
      return response.data;
    } catch (e) {
      console.log(e);
      return undefined;
    }
  };

  const getRound = (gameId: string) => {
    axios.get<Round>(`/api/games/${gameId}`).then((response) => {
      const freshRound = response.data;
      setRound(freshRound);
      console.log(
        "GetRound: " + freshRound.gameState + inputDisabled + waitingForImage,
      );
      if (
        inputDisabled &&
        freshRound.gameState === "IMAGE_PHASE" &&
        !waitingForImage
      ) {
        console.log("RequestImage");
        setWaitingForImage(true);
        requestImageGeneration();
      } else if (waitingForImage && freshRound.gameState === "PROMPT_PHASE") {
        setPrompt("");
        setInputDisabled(false);
        setWaitingForImage(false);
      }
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
    submitPrompt(promptToSubmit);
  };

  const getLastImage = (): Turn | undefined => {
    if (round) {
      // Due to the game's logic there needs to be a prompt before there can be an image
      if (round.turns.length < 2) {
        return undefined;
      }
      const lastTurn: Turn = round.turns[round.turns.length - 1];
      if (lastTurn.type === "IMAGE") {
        return lastTurn;
      }
    }
    return undefined;
  };

  const isGameFinished = (): boolean => {
    return round?.gameState === "FINISHED";
  };

  useEffect(() => {
    const interval = setInterval(() => {
      if (gameId) {
        getRound(gameId);
      }
    }, 5000);

    return () => {
      clearInterval(interval);
    };
  }, [gameId]);

  return (
    <div className="flex flex-col items-center">
      {getLastImage() && (
        <img
          className="h-128 w-auto rounded-2xl"
          alt="generated based on previous prompt"
          src={getLastImage()?.content}
        />
      )}
      {!isGameFinished() && (
        <>
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
        </>
      )}
      {isGameFinished() && (
        <div className="flex flex-col items-center">
          <h1 className="m-5 text-center text-6xl font-bold text-gray-900">
            Game is completed!
          </h1>
          <Link
            to="/"
            className="w-auto justify-center rounded-2xl bg-indigo-600 px-16 py-6 text-3xl font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          >
            Return to Start!
          </Link>
        </div>
      )}
    </div>
  );
}
