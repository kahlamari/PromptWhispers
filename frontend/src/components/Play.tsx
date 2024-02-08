import { ChangeEvent, FormEvent, useState } from "react";
import { PromptCreate } from "../types/PromptCreate";
import { useParams } from "react-router-dom";
import axios from "axios";

export default function Play() {
  const params = useParams();
  const gameId: string | undefined = params.gameId;
  const [prompt, setPrompt] = useState<string>("");

  const submitPrompt = async (promptToSubmit: PromptCreate) => {
    try {
      await axios.post(`/api/game/${gameId}/prompt`, promptToSubmit);
    } catch (e) {
      console.log(e);
    }
  };

  const onPromptChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPrompt(event.target.value);
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const promptToSubmit: PromptCreate = {
      prompt,
    };

    submitPrompt(promptToSubmit);
    setPrompt("");
  };

  return (
    <div className="">
      <h1 className="pb-5 text-center text-6xl font-bold text-gray-900">
        Enter your prompt!
      </h1>
      <form onSubmit={handleSubmit} className="flex">
        <textarea
          value={prompt}
          onChange={onPromptChange}
          rows={2}
          placeholder="The potato king leads an uprising"
          className="mr-4 h-full w-auto resize-none rounded-2xl p-6 text-3xl text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600"
        />
        <button
          type="submit"
          className="w-auto justify-center rounded-2xl bg-indigo-600 px-10 py-6 text-3xl font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Done
        </button>
      </form>
    </div>
  );
}
