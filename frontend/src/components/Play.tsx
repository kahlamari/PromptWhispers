import {ChangeEvent, FormEvent, useState} from "react";
import {PromptCreate} from "../types/PromptCreate";
import {useParams} from "react-router-dom";
import axios from "axios";

export default function Play() {
    const params = useParams()
    const gameId: string | undefined = params.gameId
    const [prompt, setPrompt] = useState<string>("")

    const submitPrompt = async (promptToSubmit: PromptCreate) => {
        try {
            await axios.post(`/api/play/${gameId}/prompt`, promptToSubmit)

        } catch (e) {
            console.log(e);
        }
    }

    const onPromptChange = (event: ChangeEvent<HTMLInputElement>) => {
        setPrompt(event.target.value)
    }

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        const promptToSubmit: PromptCreate = {
            prompt
        }

        submitPrompt(promptToSubmit)
        setPrompt("")
    }

    return (
        <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
            <h1 className="mt-10 text-center text-3xl font-bold leading-9 tracking-tight text-gray-900 pb-5">
                Enter your prompt!
            </h1>
            <form onSubmit={handleSubmit} className="space-y-6">
                <input type="text" value={prompt} onChange={onPromptChange}
                       className="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"/>
                <button type="submit"
                        className="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600">Submit
                </button>
            </form>
        </div>
    )
}