
import api from "./client";

// Fetch all bank accounts
export async function getAllAccounts() {
  const { data } = await api.get("/api/admin/accounts");
  return data;
}

// Fetch all customers
export async function getAllCustomers() {
  const { data } = await api.get("/api/admin/customers");
  return data;
}

// Fetch admin dashboard statistics
export async function getDashboardStatistics() {
  const { data } = await api.get("/api/admin/statistics");
  return data;
}
// Fetch all transactions for admin charts
export async function getAllTransactions() {
  const { data } = await api.get("/api/admin/transactions");
  return data;
}

// Fetch all failed transactions with failure reasons
export async function getAllFailedTransactions() {
  const { data } = await api.get("/api/admin/failed-transactions");
  return data;
}