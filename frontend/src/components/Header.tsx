import { User } from "../types/User.ts";

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
    <div className="top-0 flex h-16 w-full flex-row items-center justify-between bg-indigo-600 px-3">
      <div className="text-2xl font-extralight text-indigo-50">
        Prompt Whispers
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
