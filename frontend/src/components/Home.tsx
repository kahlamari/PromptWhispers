import axios from "axios";
import {Game} from "../types/Game.tsx";
import {useNavigate} from "react-router-dom";

export default function Home() {
    const navigate = useNavigate()
    const startGame = () => {
        axios.get<Game>("/api/play/start").then(response => {
            navigate(`/play/${response.data.id}`);
        })
    }

    return (
        <div className="sm:mx-auto sm:w-full sm:max-w-sm">
            <h1 className="mt-10 text-center text-3xl font-bold leading-9 tracking-tight text-gray-900 pb-5">
                Prompt Whispers
            </h1>
            <button
                className="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                type="button" onClick={startGame}>Start Game
            </button>
        </div>
    )
}