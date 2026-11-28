import React, { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);

function readSession() {
  try {
    return {
      user: JSON.parse(sessionStorage.getItem('banking_user') || 'null'),
      token: sessionStorage.getItem('banking_token'),
    };
  } catch {
    return { user: null, token: null };
  }
}

export function AuthProvider({ children }) {
  const initial = readSession();
  const [user, setUser] = useState(initial.user);
  const [token, setToken] = useState(initial.token);

  const signIn = (response) => {
    const nextUser = {
      userId: response.userId,
      username: response.username,
      role: response.role,
    };
    setUser(nextUser);
    setToken(response.token);
    sessionStorage.setItem('banking_user', JSON.stringify(nextUser));
    sessionStorage.setItem('banking_token', response.token);
  };

  const signOut = () => {
    setUser(null);
    setToken(null);
    sessionStorage.removeItem('banking_user');
    sessionStorage.removeItem('banking_token');
  };

  const value = useMemo(() => ({ user, token, signIn, signOut }), [user, token]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}