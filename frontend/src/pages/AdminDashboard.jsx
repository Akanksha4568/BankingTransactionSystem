
import React, { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext";

import {
  getAllAccounts,
  getAllCustomers,
  getDashboardStatistics,
  getAllTransactions,
  getAllFailedTransactions,
} from "../api/admin";

import { getErrorMessage } from "../api/client";

import {
  Users,
  Wallet,
  TrendingUp,
  Activity,
  CheckCircle,
  XCircle,
  Landmark,
  RefreshCw,
  LogOut,
  Sun,
  Moon,
  ArrowDownToLine,
  ArrowUpFromLine,
  ArrowLeftRight,
} from "lucide-react";

import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  LineChart,
  Line,
} from "recharts";

import "./AdminDashboard.css";

const CHART_COLORS = ["#16a34a", "#dc2626", "#f59e0b", "#6366f1"];

const formatCurrency = (amount) => {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(Number(amount) || 0);
};

const formatDate = (dateValue) => {
  if (!dateValue) return "—";

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) return "—";

  return date.toLocaleString("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  });
};

const getStatusClass = (status) => {
  if (status === "SUCCESS" || status === "ACTIVE") {
    return "status-active";
  }

  if (status === "FAILED" || status === "INACTIVE") {
    return "status-inactive";
  }

  return "";
};

const getTransactionType = (transaction) => {
  return (
    transaction.transactionType ||
    transaction.type ||
    "OTHER"
  ).toUpperCase();
};

const getFailureReason = (failure) => {
  return (
    failure.reason ||
    failure.failureReason ||
    failure.errorMessage ||
    failure.message ||
    "Reason not available"
  );
};

export default function AdminDashboard() {
  const { user, signOut } = useAuth();

  const [accounts, setAccounts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [failedTransactions, setFailedTransactions] = useState([]);
  const [statistics, setStatistics] = useState({});

  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  // Theme preference is remembered after refreshing the browser.
  const [darkMode, setDarkMode] = useState(
    () => localStorage.getItem("admin-theme") === "dark"
  );

  useEffect(() => {
    localStorage.setItem(
      "admin-theme",
      darkMode ? "dark" : "light"
    );
  }, [darkMode]);

  // Load all admin dashboard data.
  const loadDashboard = async (isRefresh = false) => {
    try {
      if (isRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }

      setError("");

      const [
        accountsData,
        customersData,
        statisticsData,
        transactionsData,
        failedData,
      ] = await Promise.all([
        getAllAccounts(),
        getAllCustomers(),
        getDashboardStatistics(),
        getAllTransactions(),
        getAllFailedTransactions(),
      ]);

      setAccounts(
        Array.isArray(accountsData) ? accountsData : []
      );

      setCustomers(
        Array.isArray(customersData) ? customersData : []
      );

      setStatistics(statisticsData || {});

      setTransactions(
        Array.isArray(transactionsData) ? transactionsData : []
      );

      setFailedTransactions(
        Array.isArray(failedData) ? failedData : []
      );
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  // Scroll to a particular dashboard section.
  const scrollToSection = (sectionId) => {
    document.getElementById(sectionId)?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  };

  const handleLogout = () => {
    signOut();
  };

  // Calculate active accounts.
  const activeAccounts = useMemo(() => {
    return accounts.filter(
      (account) => account.status === "ACTIVE"
    );
  }, [accounts]);

  // Match accounts with their customers.
  const customerAccountMap = useMemo(() => {
    const map = {};

    accounts.forEach((account) => {
      const customerId = account.customerId;

      if (!map[customerId]) {
        map[customerId] = [];
      }

      map[customerId].push(account);
    });

    return map;
  }, [accounts]);

  // Transaction status chart.
  const statusChartData = useMemo(() => {
    const successful = transactions.filter(
      (transaction) => transaction.status === "SUCCESS"
    ).length;

    const failed = transactions.filter(
      (transaction) => transaction.status === "FAILED"
    ).length;

    const pending = transactions.filter(
      (transaction) => transaction.status === "PENDING"
    ).length;

    return [
      { name: "Successful", value: successful },
      { name: "Failed", value: failed },
      { name: "Pending", value: pending },
    ].filter((item) => item.value > 0);
  }, [transactions]);

  // Transaction type chart.
  const transactionTypeData = useMemo(() => {
    const counts = {};

    transactions.forEach((transaction) => {
      const type = getTransactionType(transaction);

      counts[type] = (counts[type] || 0) + 1;
    });

    return Object.entries(counts).map(([name, value]) => ({
      name,
      value,
    }));
  }, [transactions]);

  // Account balance chart.
  const accountBalanceData = useMemo(() => {
    const balances = {};

    accounts.forEach((account) => {
      const type = account.accountType || "OTHER";

      balances[type] =
        (balances[type] || 0) + Number(account.balance || 0);
    });

    return Object.entries(balances).map(([name, balance]) => ({
      name,
      balance,
    }));
  }, [accounts]);

  // Daily transaction activity chart.
  const dailyTransactionData = useMemo(() => {
    const dailyCounts = {};

    transactions.forEach((transaction) => {
      if (!transaction.createdAt) return;

      const date = new Date(transaction.createdAt);

      if (Number.isNaN(date.getTime())) return;

      const dateLabel = date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
      });

      dailyCounts[dateLabel] = (dailyCounts[dateLabel] || 0) + 1;
    });

    return Object.entries(dailyCounts).map(([date, count]) => ({
      date,
      count,
    }));
  }, [transactions]);

  // Dashboard statistic cards.
  const statisticCards = [
    {
      title: "Total Customers",
      value: statistics.totalCustomers ?? customers.length,
      icon: Users,
      color: "blue",
      target: "customers-section",
      description: "Registered account holders",
    },
    {
      title: "Total Accounts",
      value: statistics.totalAccounts ?? accounts.length,
      icon: Wallet,
      color: "purple",
      target: "accounts-section",
      description: "All registered bank accounts",
    },
    {
      title: "Total Bank Balance",
      value: formatCurrency(statistics.totalBalance),
      icon: Landmark,
      color: "green",
      target: "accounts-section",
      description: "Combined account balance",
    },
    {
      title: "Total Transactions",
      value: statistics.totalTransactions ?? transactions.length,
      icon: Activity,
      color: "orange",
      target: "transactions-section",
      description: "All banking transactions",
    },
    {
      title: "Successful Transactions",
      value:
        statistics.successfulTransactions ??
        transactions.filter((t) => t.status === "SUCCESS").length,
      icon: CheckCircle,
      color: "green",
      target: "transactions-section",
      description: "Successfully completed",
    },
    {
      title: "Failed Transactions",
      value:
        statistics.failedTransactions ??
        transactions.filter((t) => t.status === "FAILED").length,
      icon: XCircle,
      color: "red",
      target: "failed-transactions-section",
      description: "Transactions requiring attention",
    },
    {
      title: "Active Accounts",
      value: activeAccounts.length,
      icon: TrendingUp,
      color: "blue",
      target: "accounts-section",
      description: "Currently active accounts",
    },
  ];

  if (loading) {
    return (
      <div className="admin-loading">
        <div className="admin-spinner"></div>
        <p>Loading admin dashboard...</p>
      </div>
    );
  }

  return (
    <div
      className={`admin-dashboard ${
        darkMode ? "dark-mode" : "light-mode"
      }`}
    >
      {/* HEADER */}
      <header className="admin-header">
        <div className="admin-brand">
          <div className="brand-icon">
            <Landmark size={25} />
          </div>

          <div>
            <h2>Banking Admin</h2>
            <span>Management Dashboard</span>
          </div>
        </div>

        <div className="admin-header-right">
          <span className="admin-welcome">
            Welcome, {user?.username || "Admin"}
          </span>

          <button
            type="button"
            className="theme-toggle-button"
            onClick={() => setDarkMode((previous) => !previous)}
          >
            {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            {darkMode ? "Light Mode" : "Dark Mode"}
          </button>

          <button
            type="button"
            className="admin-refresh-button"
            onClick={() => loadDashboard(true)}
            disabled={refreshing}
          >
            <RefreshCw
              size={17}
              className={refreshing ? "spin-icon" : ""}
            />
            {refreshing ? "Refreshing..." : "Refresh"}
          </button>

          <button
            type="button"
            className="admin-logout-button"
            onClick={handleLogout}
          >
            <LogOut size={17} />
            Logout
          </button>
        </div>
      </header>

      <main className="admin-main">
        {error && (
          <div className="admin-error">
            <XCircle size={18} />
            {error}
          </div>
        )}

        {/* PAGE HEADING */}
        <div className="admin-page-heading">
          <div>
            <span className="section-eyebrow">
              ADMINISTRATION
            </span>

            <h1>Dashboard Overview</h1>

            <p>
              Monitor your banking system, customers, accounts
              and transactions from one place.
            </p>
          </div>

          <div className="live-indicator">
            <span></span>
            Dashboard Overview
          </div>
        </div>

        {/* STATISTICS CARDS */}
        <section className="statistics-grid">
          {statisticCards.map((item) => {
            const Icon = item.icon;

            return (
              <button
                type="button"
                className={`stat-card stat-${item.color} clickable-stat-card`}
                key={item.title}
                onClick={() => scrollToSection(item.target)}
                title={`View ${item.title}`}
              >
                <div className="stat-card-top">
                  <div>
                    <p className="stat-label">{item.title}</p>
                    <h2>{item.value}</h2>
                  </div>

                  <div className="stat-icon">
                    <Icon size={24} />
                  </div>
                </div>

                <div className="stat-card-bottom">
                  <span>{item.description}</span>
                  <span className="stat-view-link">
                    View details →
                  </span>
                </div>
              </button>
            );
          })}
        </section>

        {/* ANALYTICS */}
        <section className="analytics-section">
          <div className="analytics-heading">
            <div>
              <span className="section-eyebrow">
                PERFORMANCE INSIGHTS
              </span>
              <h2>Banking Analytics</h2>
              <p>
                Visual overview of transactions and account activity.
              </p>
            </div>
          </div>

          <div className="analytics-grid">
            {/* TRANSACTION STATUS */}
            <div className="chart-panel">
              <div className="chart-panel-heading">
                <h3>Transaction Status</h3>
                <p>Success, failed and pending transactions</p>
              </div>

              {statusChartData.length > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <PieChart>
                    <Pie
                      data={statusChartData}
                      dataKey="value"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={65}
                      outerRadius={95}
                      paddingAngle={5}
                      label
                    >
                      {statusChartData.map((entry, index) => (
                        <Cell
                          key={entry.name}
                          fill={CHART_COLORS[index % CHART_COLORS.length]}
                        />
                      ))}
                    </Pie>

                    <Tooltip />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <div className="chart-empty">
                  No transaction data available.
                </div>
              )}
            </div>

            {/* TRANSACTION TYPES */}
            <div className="chart-panel">
              <div className="chart-panel-heading">
                <h3>Transaction Categories</h3>
                <p>Breakdown by transaction type</p>
              </div>

              {transactionTypeData.length > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={transactionTypeData}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      vertical={false}
                    />
                    <XAxis dataKey="name" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Bar
                      dataKey="value"
                      fill="#6366f1"
                      radius={[8, 8, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="chart-empty">
                  No transaction categories available.
                </div>
              )}
            </div>

            {/* ACCOUNT BALANCE */}
            <div className="chart-panel">
              <div className="chart-panel-heading">
                <h3>Account Balance</h3>
                <p>Total balance by account type</p>
              </div>

              {accountBalanceData.length > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={accountBalanceData}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      vertical={false}
                    />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip
                      formatter={(value) => formatCurrency(value)}
                    />
                    <Bar
                      dataKey="balance"
                      fill="#0d9488"
                      radius={[8, 8, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <div className="chart-empty">
                  No account balance data available.
                </div>
              )}
            </div>

            {/* DAILY ACTIVITY */}
            <div className="chart-panel">
              <div className="chart-panel-heading">
                <h3>Daily Transaction Activity</h3>
                <p>Transactions recorded by date</p>
              </div>

              {dailyTransactionData.length > 0 ? (
                <ResponsiveContainer width="100%" height={280}>
                  <LineChart data={dailyTransactionData}>
                    <CartesianGrid
                      strokeDasharray="3 3"
                      vertical={false}
                    />
                    <XAxis dataKey="date" />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Line
                      type="monotone"
                      dataKey="count"
                      stroke="#8b5cf6"
                      strokeWidth={3}
                      dot={{ r: 5 }}
                      activeDot={{ r: 8 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="chart-empty">
                  No daily activity available.
                </div>
              )}
            </div>
          </div>
        </section>

        {/* REGISTERED CUSTOMERS */}
        <section
          className="data-panel"
          id="customers-section"
        >
          <div className="data-panel-heading">
            <div>
              <span className="section-eyebrow">
                CUSTOMER MANAGEMENT
              </span>
              <h2>Registered Account Holders</h2>
              <p>
                View registered customers and their banking details.
              </p>
            </div>

            <span className="record-count">
              {customers.length} Customers
            </span>
          </div>

          <div className="table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Customer ID</th>
                  <th>Customer Name</th>
                  <th>Phone Number</th>
                  <th>Address</th>
                  <th>Bank Accounts</th>
                  <th>Registration Date</th>
                </tr>
              </thead>

              <tbody>
                {customers.map((customer) => {
                  const customerAccounts =
                    customerAccountMap[customer.customerId] || [];

                  return (
                    <tr key={customer.customerId}>
                      <td>
                        #{customer.customerId}
                      </td>

                      <td className="customer-name">
                        {customer.fullName || "—"}
                      </td>

                      <td>
                        {customer.phone || "—"}
                      </td>

                      <td>
                        {customer.address || "—"}
                      </td>

                      <td>
                        <span className="record-count">
                          {customerAccounts.length}
                        </span>
                      </td>

                      <td>
                        {formatDate(customer.createdAt)}
                      </td>
                    </tr>
                  );
                })}

                {customers.length === 0 && (
                  <tr>
                    <td colSpan="6" className="empty-table">
                      No registered customers found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* BANK ACCOUNTS */}
        <section
          className="data-panel"
          id="accounts-section"
        >
          <div className="data-panel-heading">
            <div>
              <span className="section-eyebrow">
                ACCOUNT MANAGEMENT
              </span>
              <h2>Bank Account Details</h2>
              <p>
                Monitor customer accounts, balances and account status.
              </p>
            </div>

            <span className="record-count">
              {accounts.length} Accounts
            </span>
          </div>

          <div className="table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Account ID</th>
                  <th>Account Number</th>
                  <th>Customer ID</th>
                  <th>Account Type</th>
                  <th>Balance</th>
                  <th>Currency</th>
                  <th>Status</th>
                  <th>Created At</th>
                </tr>
              </thead>

              <tbody>
                {accounts.map((account) => (
                  <tr key={account.accountId}>
                    <td>#{account.accountId}</td>

                    <td className="account-number">
                      {account.accountNumber}
                    </td>

                    <td>
                      #{account.customerId}
                    </td>

                    <td>
                      <span className="type-pill">
                        {account.accountType}
                      </span>
                    </td>

                    <td className="balance-text">
                      {formatCurrency(account.balance)}
                    </td>

                    <td>
                      {account.currency || "INR"}
                    </td>

                    <td>
                      <span
                        className={`status-pill ${getStatusClass(
                          account.status
                        )}`}
                      >
                        {account.status}
                      </span>
                    </td>

                    <td>
                      {formatDate(account.createdAt)}
                    </td>
                  </tr>
                ))}

                {accounts.length === 0 && (
                  <tr>
                    <td colSpan="8" className="empty-table">
                      No bank accounts found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* ALL TRANSACTIONS */}
        <section
          className="data-panel"
          id="transactions-section"
        >
          <div className="data-panel-heading">
            <div>
              <span className="section-eyebrow">
                TRANSACTION MANAGEMENT
              </span>
              <h2>All Transactions</h2>
              <p>
                Review all successful and unsuccessful banking transactions.
              </p>
            </div>

            <span className="record-count">
              {transactions.length} Transactions
            </span>
          </div>

          <div className="table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Transaction ID</th>
                  <th>Type</th>
                  <th>From Account</th>
                  <th>To Account</th>
                  <th>Amount</th>
                  <th>Status</th>
                  <th>Date</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map((transaction, index) => (
                  <tr
                    key={
                      transaction.transactionId ??
                      `transaction-${index}`
                    }
                  >
                    <td>
                      #{transaction.transactionId ?? "—"}
                    </td>

                    <td>
                      {getTransactionType(transaction)}
                    </td>

                    <td>
                      {transaction.fromAccountId ?? "—"}
                    </td>

                    <td>
                      {transaction.toAccountId ?? "—"}
                    </td>

                    <td className="balance-text">
                      {formatCurrency(transaction.amount)}
                    </td>

                    <td>
                      <span
                        className={`status-pill ${getStatusClass(
                          transaction.status
                        )}`}
                      >
                        {transaction.status || "UNKNOWN"}
                      </span>
                    </td>

                    <td>
                      {formatDate(transaction.createdAt)}
                    </td>
                  </tr>
                ))}

                {transactions.length === 0 && (
                  <tr>
                    <td colSpan="7" className="empty-table">
                      No transactions found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* FAILED TRANSACTIONS */}
        <section
          className="data-panel failed-transactions-panel"
          id="failed-transactions-section"
        >
          <div className="data-panel-heading">
            <div>
              <span className="section-eyebrow failure-eyebrow">
                TRANSACTION MONITORING
              </span>

              <h2>Failed Transactions</h2>

              <p>
                Review failed transactions and understand why they failed.
              </p>
            </div>

            <span className="failure-record-count">
              {failedTransactions.length} Failed
            </span>
          </div>

          <div className="table-wrapper">
            <table className="admin-table failure-table">
              <thead>
                <tr>
                  <th>Failure ID</th>
                  <th>Transaction ID</th>
                  <th>Failure Reason</th>
                  <th>Failure Date</th>
                </tr>
              </thead>

              <tbody>
                {failedTransactions.map((failure, index) => (
                  <tr
                    key={
                      failure.failureId ??
                      failure.failedTransactionId ??
                      index
                    }
                  >
                    <td className="failure-id">
                      #
                      {failure.failureId ??
                        failure.failedTransactionId ??
                        "—"}
                    </td>

                    <td>
                      #{failure.transactionId ?? "—"}
                    </td>

                    <td className="failure-reason">
                      {getFailureReason(failure)}
                    </td>

                    <td>
                      {formatDate(
                        failure.createdAt ||
                        failure.failedAt ||
                        failure.failureTime
                      )}
                    </td>
                  </tr>
                ))}

                {failedTransactions.length === 0 && (
                  <tr>
                    <td colSpan="4" className="empty-table">
                      <div className="no-failures">
                        <CheckCircle size={22} />
                        No failed transactions found.
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </section>

        {/* FOOTER */}
        <footer className="admin-footer">
          <p>
            Banking Transaction System | Admin Management Portal
          </p>
        </footer>
      </main>
    </div>
  );
}
