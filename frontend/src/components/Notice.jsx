import React from "react";
export default function Notice({ type = 'success', children }) {
  if (!children) return null;
  return <div className={`notice ${type}`} role="status">{children}</div>;
}