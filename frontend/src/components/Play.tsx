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
  const [round, setRound] = useState<Round | undefined>(undefined);
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);
  const [shouldPoll, setShouldPoll] = useState<boolean>(false);

  const onPromptChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPrompt(event.target.value);
  };

  const submitPrompt = (promptToSubmit: PromptCreate) => {
    axios
      .post<Round>(`/api/games/${gameId}/prompt`, promptToSubmit)
      .then((response) => setRound(response.data));
  };

  const requestImageGeneration = () => {
    axios.post<Round>(`/api/games/${gameId}/generateImage`).then((response) => {
      setRound(response.data);
      setupPage(response.data);
    });
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setInputDisabled(true);

    const promptToSubmit: PromptCreate = {
      prompt,
    };

    submitPrompt(promptToSubmit);
    requestImageGeneration();
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

  const setupPage = (roundToConsider: Round) => {
    if (roundToConsider !== undefined) {
      if (roundToConsider.gameState === "REQUEST_NEW_PROMPTS" && shouldPoll) {
        console.log("REQUEST_NEW_PROMPTS");
        setShouldPoll(false);
        setPrompt("");
        setInputDisabled(false);
      } else if (
        roundToConsider.gameState === "WAIT_FOR_IMAGES" &&
        !shouldPoll
      ) {
        setShouldPoll(true);
        console.log("WAIT_FOR_IMAGES");
      } else if (
        roundToConsider.gameState === "WAIT_FOR_PROMPTS" &&
        !shouldPoll
      ) {
        setShouldPoll(true);
        console.log("WAIT_FOR_PROMPT");
      }
    } else {
      console.log(roundToConsider);
    }
  };

  useEffect(() => {
    const getRound = () => {
      axios.get<Round>(`/api/games/${gameId}`).then((response) => {
        setRound(response.data);
        setupPage(response.data);
      });

      if (shouldPoll) {
        setTimeout(getRound, 5000);
      }
    };

    getRound();

    return () => {
      console.log("useEffect exit");
      setShouldPoll(false);
    };
  }, [gameId, shouldPoll]);

  if (round == undefined) {
    return <div>Loading</div>;
  }

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
