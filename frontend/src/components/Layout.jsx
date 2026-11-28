
import React, { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import {
  Activity,
  ArrowDownToLine,
  ArrowLeftRight,
  ArrowUpFromLine,
  LayoutDashboard,
  LogOut,
  Menu,
  Wallet,
  X,
  Sun,
  Moon,
} from "lucide-react";

import { useAuth } from "../context/AuthContext";

const links = [
  { to: "/", label: "Overview", icon: LayoutDashboard, end: true },
  { to: "/deposit", label: "Deposit", icon: ArrowDownToLine },
  { to: "/withdraw", label: "Withdraw", icon: ArrowUpFromLine },
  { to: "/transfer", label: "Transfer", icon: ArrowLeftRight },
  { to: "/history", label: "Transactions", icon: Activity },
];

export default function Layout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  const [open, setOpen] = useState(false);
  const [darkMode, setDarkMode] = useState(
    () => localStorage.getItem("user-theme") === "dark"
  );

  useEffect(() => {
    localStorage.setItem("user-theme", darkMode ? "dark" : "light");
  }, [darkMode]);

  const toggleTheme = () => {
    setDarkMode((previous) => !previous);
  };

  const logout = () => {
    signOut();
    navigate("/login");
  };

  return (
    <div className={`app-shell ${darkMode ? "user-theme-dark" : "user-theme-light"}`}>
      <aside className={`sidebar ${open ? "sidebar-open" : ""}`}>
        <div className="brand">
          <span className="brand-mark">
            <Wallet size={21} />
          </span>

          <span>
            Northstar<span className="brand-light">Bank</span>
          </span>

          <button
            className="mobile-close"
            onClick={() => setOpen(false)}
            aria-label="Close menu"
          >
            <X />
          </button>
        </div>

        <div className="nav-caption">WORKSPACE</div>

        <nav>
          {links.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `nav-link ${isActive ? "active" : ""}`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="support-card">
            <div className="support-dot" />
            <div>
              <strong>Secure banking</strong>
              <small>Your activity is protected</small>
            </div>
          </div>

          <button className="nav-link logout" onClick={logout}>
            <LogOut size={18} />
            Sign out
          </button>
        </div>
      </aside>

      {open && (
        <button
          className="scrim"
          aria-label="Close menu"
          onClick={() => setOpen(false)}
        />
      )}

      <main className="main-area">
        <header className="topbar">
          <button
            className="mobile-menu"
            onClick={() => setOpen(true)}
            aria-label="Open menu"
          >
            <Menu />
          </button>

          <div>
            <span className="eyebrow">PERSONAL BANKING</span>
            <strong>
              Welcome back, {user?.username || "Customer"}
            </strong>
          </div>

          <div className="topbar-actions">
            <button
              className="theme-toggle"
              onClick={toggleTheme}
              title={darkMode ? "Switch to light mode" : "Switch to dark mode"}
              aria-label={darkMode ? "Switch to light mode" : "Switch to dark mode"}
            >
              {darkMode ? <Sun size={19} /> : <Moon size={19} />}
              <span>{darkMode ? "Light Mode" : "Dark Mode"}</span>
            </button>

            <div className="profile">
              <div className="avatar">
                {(user?.username || "U").slice(0, 1).toUpperCase()}
              </div>

              <div>
                <strong>{user?.username}</strong>
                <small>{user?.role || "USER"}</small>
              </div>
            </div>
          </div>
        </header>

        <div className="page-content">
          <Outlet />
        </div>

        <footer className="footer">
          © {new Date().getFullYear()} Northstar Bank
          <span>Secure • Simple • Reliable</span>
        </footer>
      </main>
    </div>
  );
}
