export default function Home() {
    return (
        <div className="items-center justify-center bg-amber-100 md:container md:mx-auto rounded text-center p-3">
            <h1 className="text-3xl font-bold pb-3">
                Prompt Whispers
            </h1>
            <button
                className="rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                type="button">Start Game
            </button>
        </div>
    )
}