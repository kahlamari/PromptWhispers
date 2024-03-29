import {
  ChangeEvent,
  FormEvent,
  KeyboardEvent,
  useEffect,
  useState,
} from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { Turn } from "../types/Turn.ts";
import { Round } from "../types/Round.ts";
import Button from "../ui-components/Button.tsx";
import Spinner from "../ui-components/Spinner.tsx";
import ImagePlaceholder from "../ui-components/ImagePlaceholder.tsx";

export default function Play() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;

  const [prompt, setPrompt] = useState<string>("");
  const [round, setRound] = useState<Round | undefined>(undefined);
  const [inputDisabled, setInputDisabled] = useState<boolean>(false);
  const [shouldPoll, setShouldPoll] = useState<boolean>(true);
  const [isGameRunning, setIsGameRunning] = useState<boolean>(true);
  const [isImageLoaded, setIsImageLoaded] = useState<boolean>(false);
  const navigate = useNavigate();

  const onPromptChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPrompt(event.target.value);
  };

  const submitPrompt = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setInputDisabled(true);

    axios
      .post<Round>(`/api/games/${gameId}/prompt`, {
        prompt,
      })
      .then((response) => {
        setRound(response.data);
        requestImageGeneration();
      });
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      setInputDisabled(true);
      submitPrompt(event as unknown as FormEvent<HTMLFormElement>);
    }
  };

  const requestImageGeneration = () => {
    axios
      .post<Round>(`/api/games/${gameId}/generateImage`)
      .then((response) => setRound(response.data));
  };

  const getLastImage = (): Turn | undefined => {
    if (round) {
      // Due to the game's logic there needs to be a prompt before there can be an image
      if (
        round.gameState === "WAIT_FOR_IMAGES" ||
        round.gameState === "WAIT_FOR_PROMPTS" ||
        round.turns.length < 2
      ) {
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
      const getRound = () => {
        axios
          .get<Round>(`/api/games/${gameId}`)
          .then((response) => setRound(response.data));
      };

      if (shouldPoll) {
        getRound();
      }
    }, 5000);

    if (!isGameRunning) {
      clearInterval(interval);
    }

    return () => {
      clearInterval(interval);
    };
  }, [gameId, shouldPoll, isGameRunning]);

  // update the page based on the game's state
  useEffect(() => {
    if (round) {
      if (
        round.gameState === "REQUEST_NEW_PROMPTS" ||
        round.gameState === "FINISHED"
      ) {
        setShouldPoll(false);
        setPrompt("");
        setInputDisabled(false);
        if (round.gameState === "FINISHED") {
          setIsGameRunning(false);
        }
      } else {
        setIsImageLoaded(false);
        setShouldPoll(true);
      }
    }
  }, [round]);

  if (!round) {
    return (
      <div className="flex h-96 items-center sm:h-144">
        <Spinner size="xl" />
      </div>
    );
  }

  return (
    <div className="flex h-full w-full flex-col items-center gap-y-3 sm:w-144 sm:gap-y-5">
      {getLastImage() && (
        <img
          className={`w-svw rounded-2xl  ${isImageLoaded ? "block" : "hidden"}`}
          alt="generated based on previous prompt"
          src={getLastImage()?.content}
          onLoad={() => setIsImageLoaded(true)}
        />
      )}
      {!isImageLoaded && round.turns.length >= 1 && (
        <div className="aspect-square w-full items-center sm:w-144">
          <ImagePlaceholder />
        </div>
      )}
      {!isImageLoaded && round.turns.length < 1 && (
        <div className="aspect-square h-96 sm:h-144"></div>
      )}
      {!isGameFinished() && (
        <div className="w-full items-center">
          <form
            onSubmit={submitPrompt}
            className="flex w-full flex-col gap-y-3 sm:flex-row sm:gap-x-5"
          >
            <textarea
              className="h-full w-full resize-none overflow-hidden rounded-2xl p-6 text-3xl text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 disabled:opacity-75"
              value={prompt}
              onChange={onPromptChange}
              onKeyDown={handleKeyDown}
              rows={2}
              placeholder={
                round.turns.length < 1
                  ? "Enter your prompt!"
                  : "Describe the image!"
              }
              autoFocus={true}
              disabled={inputDisabled}
              maxLength={140}
            />
            <div className="parent flex items-stretch ">
              <Button type="submit" isDisabled={inputDisabled}>
                {!getLastImage() && round.turns.length >= 1 ? (
                  <Spinner size="md" />
                ) : (
                  "Done"
                )}
              </Button>
            </div>
          </form>
        </div>
      )}
      {isGameFinished() && (
        <div className="flex flex-col items-center">
          <Button onClick={() => navigate(`/games/${gameId}`)}>
            View all turns!
          </Button>
        </div>
      )}
    </div>
  );
}
