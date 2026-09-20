import useTheme from "../hooks/useTheme.js";

export default function ThemeToggle() {
  const { theme, setTheme } = useTheme();

  return (
    <div className="theme-toggle" role="group" aria-label="Tema da interface">
      <button
        type="button"
        className={theme === "light" ? "selected" : ""}
        onClick={() => setTheme("light")}
        aria-label="Ativar modo claro"
        aria-pressed={theme === "light"}
      >
        ☀
      </button>
      <span className="theme-divider" aria-hidden="true" />
      <button
        type="button"
        className={theme === "dark" ? "selected" : ""}
        onClick={() => setTheme("dark")}
        aria-label="Ativar modo escuro"
        aria-pressed={theme === "dark"}
      >
        ◐
      </button>
    </div>
  );
}
