import { Route, Routes } from "react-router-dom";
import Home from "./components/Home.tsx";
import Play from "./components/Play.tsx";
import { useEffect, useState } from "react";
import { User } from "./types/User.ts";
import axios from "axios";
import Header from "./components/Header.tsx";
import GameHistory from "./components/GameHistory.tsx";
import GameReplay from "./components/GameReplay.tsx";
import LobbyScreen from "./components/LobbyScreen.tsx";

function App() {
  const [user, setUser] = useState<User>(null);

  const getCurrentUser = () => {
    axios.get<User>("/api/users").then((response) => {
      setUser(response.data);
    });
  };

  useEffect(() => {
    getCurrentUser();
  }, []);

  const logout = () => {
    axios.post("/api/users/logout").then(() => getCurrentUser());
  };

  return (
    <div>
      <Header user={user} logout={logout} />
      <div className="flex h-svh justify-center gap-y-3 p-3 text-lg xs:text-base sm:p-5">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/play/:gameId/" element={<Play />} />
          <Route
            path="/lobby/:lobbyId/"
            element={<LobbyScreen loggedInUser={user} />}
          />
          <Route path="/games" element={<GameHistory />} />
          <Route path="/games/:gameId/" element={<GameReplay />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
