import { Route, Routes } from "react-router-dom";
import Home from "./components/Home.tsx";
import Play from "./components/Play.tsx";
import { useEffect, useState } from "react";
import { User } from "./types/User.ts";
import axios from "axios";
import Header from "./components/Header.tsx";

function App() {
  const [user, setUser] = useState<User>(null);

  function getCurrentUser() {
    axios.get<User>("/api/users").then((response) => {
      setUser(response.data);
      console.log(response.data);
      console.log(user);
    });
  }

  useEffect(() => {
    getCurrentUser();
  }, []);

  return (
    <>
      <Header />
      <div className="flex min-h-screen items-center justify-center bg-indigo-50">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/play/:gameId/" element={<Play />} />
        </Routes>
      </div>
    </>
  );
}

export default App;
