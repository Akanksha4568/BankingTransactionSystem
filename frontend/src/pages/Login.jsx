
import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Landmark, ShieldCheck } from "lucide-react";

import { loginRequest } from "../api/auth";
import { getErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const registrationMessage = location.state?.message;

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);

    try {
      const data = await loginRequest({ username, password });

      if (!data?.token || data.userId == null) {
        throw new Error(
          "Login response did not include userId and token."
        );
      }

      signIn(data);

      if (data.role === "ADMIN") {
        navigate("/admin", { replace: true });
      } else {
        navigate("/", { replace: true });
      }

    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-visual">
        <div className="auth-brand">
          <Landmark /> NORTHSTAR BANK
        </div>

        <div className="visual-copy">
          <span className="eyebrow">BANKING, REIMAGINED</span>
          <h1>Your money.<br />Your momentum.</h1>
          <p>
            A clearer view of your finances, with the tools to move
            forward confidently.
          </p>

          <div className="visual-feature">
            <ShieldCheck /> Secure access to your banking workspace
          </div>
        </div>

        <div className="visual-footer">
          A smarter way to manage everyday banking.
        </div>
      </div>

      <div className="auth-content">
        <form className="auth-card" onSubmit={submit}>
          <div className="mobile-auth-brand">
            <Landmark /> NORTHSTAR BANK
          </div>

          <span className="eyebrow">WELCOME BACK</span>
          <h2>Sign in to your account</h2>
          <p className="muted">
            Enter your credentials to continue.
          </p>

          {registrationMessage && (
            <div className="notice">
              {registrationMessage}
            </div>
          )}

          <label>
            Username
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              placeholder="Your username"
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
              placeholder="Your password"
              required
            />
          </label>

          {error && (
            <div className="notice error">{error}</div>
          )}

          <button className="primary-button full" disabled={busy}>
            {busy ? "Signing in…" : "Sign in"}
          </button>

          <p className="auth-switch">
            New to Northstar? <Link to="/register">Create an account</Link>
          </p>
        </form>

        <small className="auth-legal">
          Protected banking access · Never share your password or OTP.
        </small>
      </div>
    </div>
  );
}
