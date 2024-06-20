import {ChangeEvent, FormEvent, KeyboardEvent, useEffect, useState,} from "react";
import {useNavigate, useParams} from "react-router-dom";
import axios from "axios";
import {Turn} from "../types/Turn.ts";
import Button from "../ui-components/Button.tsx";
import Spinner from "../ui-components/Spinner.tsx";
import ImagePlaceholder from "../ui-components/ImagePlaceholder.tsx";
import {Game} from "../types/Game.ts";
import {User} from "../types/User.ts";
import {getNumberOfCompletedImageTurns} from "../libs/gameHelper.ts";

type PlayProps = {
    readonly loggedInUser: User;
};

export default function Play(props: PlayProps) {
    const params = useParams();
    const gameId: string | undefined = params.gameId;

    const [prompt, setPrompt] = useState<string>("");
    const [game, setGame] = useState<Game | undefined | null>(undefined);
    const [roundIndex, setRoundIndex] = useState<number>(-1);
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
            .post<Game>(`/api/games/${gameId}/prompt`, {
                prompt,
            })
            .then((response) => {
                setGame(response.data);
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
            .post<Game>(`/api/games/${gameId}/generateImage`)
            .then((response) => setGame(response.data));
    };

    const getLastImage = (): Turn | undefined => {
        if (game && roundIndex >= 0) {
            // Due to the game's logic there needs to be a prompt before there can be an image
            if (
                game.gameState === "WAIT_FOR_IMAGES" ||
                game.gameState === "WAIT_FOR_PROMPTS" ||
                game.rounds[roundIndex].length < 2
            ) {
                return undefined;
            }

            const lastTurn: Turn = game.rounds[roundIndex][game.rounds[roundIndex].length - 1];
            if (lastTurn.type === "IMAGE") {
                return lastTurn;
            }
        }
        return undefined;
    };

    const isGameFinished = (): boolean => {
        return game?.gameState === "FINISHED";
    };

    useEffect(() => {
        const interval = setInterval(() => {
            const getGame = () => {
                axios
                    .get<Game>(`/api/games/${gameId}`)
                    .then((response) => setGame(response.data));
            };

            if (shouldPoll) {
                getGame();
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
        if (game) {
            const playerIndex = game.players.findIndex(player => player?.id === props.loggedInUser?.id) ?? -1;

            const imageTurns = getNumberOfCompletedImageTurns(game);

            const roundIdx = (playerIndex + imageTurns) % game.players.length;

            setRoundIndex(roundIdx);

            if (
                game.gameState === "REQUEST_NEW_PROMPTS" ||
                game.gameState === "FINISHED"
            ) {
                setShouldPoll(false);
                setPrompt("");
                setInputDisabled(false);
                if (game.gameState === "FINISHED") {
                    setIsGameRunning(false);
                }
            } else {
                setIsImageLoaded(false);
                setShouldPoll(true);
            }
        }
    }, [game, props.loggedInUser?.id]);

    if (!game || roundIndex == -1) {
        return (
            <div className="flex h-96 items-center sm:h-144">
                <Spinner size="xl"/>
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
            {!isImageLoaded && game.rounds[roundIndex].length >= 1 && (
                <div className="aspect-square w-full items-center sm:w-144">
                    <ImagePlaceholder/>
                </div>
            )}
            {!isImageLoaded && game.rounds[roundIndex].length < 1 && (
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
                    game.rounds[roundIndex].length < 1
                        ? "Enter your prompt!"
                        : "Describe the image!"
                }
                autoFocus={true}
                disabled={inputDisabled}
                maxLength={140}
            />
                        <div className="parent flex items-stretch ">
                            <Button type="submit" isDisabled={inputDisabled}>
                                {!getLastImage() && game.rounds[roundIndex].length >= 1 ? (
                                    <Spinner size="md"/>
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
