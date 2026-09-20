import ThemeToggle from "./ThemeToggle.jsx";

export default function Header() {
  return (
    <header className="app-header">
      <div className="brand">
        <div className="brand-mark" aria-hidden="true">♞</div>
        <div>
          <h1>Chess Analysis</h1>
          <p>Stockfish-powered game review</p>
        </div>
      </div>

      <div className="header-actions">
        <ThemeToggle />
      </div>
    </header>
  );
}
