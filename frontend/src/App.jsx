import {Route, Routes} from "react-router-dom";
import Home from "./components/Home.tsx";
import Game from "./components/Game.tsx";
import {PromptCreate} from "./types/PromptCreate";
import axios from "axios";


function App() {

    const submitPrompt = async (promptToSubmit: PromptCreate) => {
        try {
            const response = await axios.post("/api/", promptToSubmit)

        } catch (e) {

        }
    }
    return (
        <div className="flex min-h-screen w-screen flex-col items-center justify-center">
            <Routes>
                <Route path="/" element={<Home/>}/>
                <Route path="/play" element={<Game submitPrompt={}/>}/>
            </Routes>
        </div>
    )
}

export default App
