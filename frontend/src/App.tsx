import { Route, Routes } from "react-router-dom";
import Home from "./components/Home.tsx";
import Play from "./components/Play.tsx";

function App() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-indigo-50">
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/play/:gameId/" element={<Play />} />
      </Routes>
    </div>
  );
}

export default App;
