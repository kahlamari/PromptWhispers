import { User } from "../types/User.ts";
import { Link } from "react-router-dom";
import logoUrl from "./../assets/promptwhispers-logo.webp";

type HeaderProps = {
  user: User;
  logout: () => void;
};

export default function Header(props: HeaderProps) {
  function login() {
    const host =
      window.location.host === "localhost:5173"
        ? "http://localhost:8080"
        : window.location.origin;

    window.open(host + "/oauth2/authorization/google", "_self");
  }

  return (
    <div className="top-0 flex h-16 w-full flex-row items-center justify-between rounded-b-2xl bg-indigo-600 px-3 text-indigo-50">
      <div className="flex h-full items-center font-extralight">
        <Link className="mr-1 flex h-full items-center sm:mr-10" to="/">
          <img className="mr-3 h-12 rounded-full" src={logoUrl} alt="logo" />
          <h1 className="text-xl sm:text-4xl">Prompt Whispers</h1>
        </Link>
        <div className="rounded-2xl px-2 pt-1 text-base hover:bg-indigo-500 sm:pt-0 sm:text-lg">
          {!props.user && <Link to="/games">Games</Link>}
        </div>
      </div>

      {!props.user && (
        <button
          onClick={login}
          className="w-auto justify-center rounded-2xl bg-indigo-50 px-2 py-2 font-light text-indigo-600 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Login
        </button>
      )}
      {!!props.user && (
        <button
          onClick={props.logout}
          className="w-auto justify-center rounded-2xl bg-indigo-50 px-2 py-2 font-light text-indigo-600 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
        >
          Logout
        </button>
      )}
    </div>
  );
}
