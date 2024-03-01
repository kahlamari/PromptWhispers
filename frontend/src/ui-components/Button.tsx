import { ReactNode } from "react";

type ButtonProps = {
  children: ReactNode;
  onClick?: () => void;
  isDisabled?: boolean;
  type?: "submit" | "reset" | "button";
};

export default function Button(props: Readonly<ButtonProps>) {
  return (
    <button
      className="justify-center rounded-2xl bg-indigo-600 px-12 py-6 text-3xl font-semibold text-indigo-50 shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:bg-indigo-300"
      disabled={props.isDisabled ?? false}
      onClick={props.onClick}
      type={props.type ?? "button"}
    >
      {props.children}
    </button>
  );
}
