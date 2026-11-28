
import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";

import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import AdminDashboard from "./pages/AdminDashboard";
import TransactionHistory from "./pages/TransactionHistory";
import AddAccount from "./pages/AddAccount";

import TransactionForm from "./components/TransactionForm";

import {
  depositRequest,
  withdrawRequest,
  transferRequest
} from "./api/transactions";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Admin Dashboard */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={["ADMIN"]}>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />

      {/* User Dashboard */}
      <Route
        path="/"
        element={
          <ProtectedRoute allowedRoles={["USER"]}>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="accounts" element={<AddAccount />} />

        <Route
          path="deposit"
          element={
            <TransactionForm kind="deposit" submit={depositRequest} />
          }
        />

        <Route
          path="withdraw"
          element={
            <TransactionForm kind="withdraw" submit={withdrawRequest} />
          }
        />

        <Route
          path="transfer"
          element={
            <TransactionForm kind="transfer" submit={transferRequest} />
          }
        />

        <Route path="history" element={<TransactionHistory />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
