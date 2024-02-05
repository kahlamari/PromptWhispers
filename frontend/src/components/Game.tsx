import {ChangeEvent, FormEvent, useState} from "react";
import {PromptCreate} from "../types/PromptCreate";

type GameProps = {
    submitPrompt: (promptToSubmit: PromptCreate) => void;
}

export default function Game(props: GameProps) {
    const [prompt, setPrompt] = useState<string>("")

    const onPromptChange = (event: ChangeEvent<HTMLInputElement>) => {
        setPrompt(event.target.value)
    }

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault()

        const promptToSubmit: PromptCreate = {
            prompt: prompt
        }

        props.submitPrompt(promptToSubmit)
        setPrompt("")
    }

    return (
        <>
            <form>
                <input type="text" value={prompt} onChange={onPromptChange}/>
                <button type="submit">Submit</button>
            </form>
        </>
    )
}