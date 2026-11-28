
import React, { useEffect, useState } from "react";
import {
  Activity,
  ArrowDownLeft,
  ArrowUpRight,
  RefreshCw,
  Wallet,
  Users,
  CreditCard,
  TrendingUp,
} from "lucide-react";
import { Link } from "react-router-dom";

import {
  getMyAccounts,
  historyRequest,
} from "../api/transactions";

import { getErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";

const money = (value) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
  }).format(Number(value || 0));

export default function Dashboard() {
  const { user } = useAuth();

  const [accounts, setAccounts] = useState([]);
  const [accountId, setAccountId] = useState("");
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [accountsLoading, setAccountsLoading] = useState(true);
  const [busy, setBusy] = useState(false);

  const storageKey = user?.userId
    ? `banking_account_id_${user.userId}`
    : null;

  // Load the logged-in user's accounts.
  useEffect(() => {
    let cancelled = false;

    const loadAccounts = async () => {
      setAccountsLoading(true);
      setError("");
      setAccounts([]);
      setAccountId("");
      setData(null);

      try {
        const result = await getMyAccounts();

        if (cancelled) return;

        const userAccounts = Array.isArray(result) ? result : [];
        setAccounts(userAccounts);

        const savedId = storageKey
          ? sessionStorage.getItem(storageKey)
          : null;

        const validSavedAccount = userAccounts.find(
          (account) => String(account.accountId) === savedId
        );

        const selectedId = validSavedAccount
          ? String(validSavedAccount.accountId)
          : userAccounts.length > 0
            ? String(userAccounts[0].accountId)
            : "";

        setAccountId(selectedId);

        if (storageKey) {
          if (selectedId) {
            sessionStorage.setItem(storageKey, selectedId);
          } else {
            sessionStorage.removeItem(storageKey);
          }
        }
      } catch (err) {
        if (!cancelled) {
          setAccounts([]);
          setAccountId("");
          setData(null);
          setError(getErrorMessage(err));
        }
      } finally {
        if (!cancelled) {
          setAccountsLoading(false);
        }
      }
    };

    if (user?.userId && storageKey) {
      loadAccounts();
    } else {
      setAccounts([]);
      setAccountId("");
      setData(null);
      setAccountsLoading(false);
    }

    return () => {
      cancelled = true;
    };
  }, [user?.userId]);

  // Load transaction history for the selected account.
  const loadHistory = async () => {
    const accountExists = accounts.some(
      (account) => String(account.accountId) === accountId
    );

    if (!accountId || !user?.userId || !accountExists) {
      setData(null);
      return;
    }

    setBusy(true);
    setError("");

    try {
      const result = await historyRequest({
        userId: user.userId,
        accountId,
        page: 0,
        size: 5,
      });

      setData(result);
    } catch (err) {
      setError(getErrorMessage(err));
      setData(null);
    } finally {
      setBusy(false);
    }
  };

  useEffect(() => {
    if (
      !accountsLoading &&
      accountId &&
      accounts.some(
        (account) => String(account.accountId) === accountId
      )
    ) {
      loadHistory();
    } else {
      setData(null);
    }
  }, [accountId, accounts, accountsLoading, user?.userId]);

  const handleAccountChange = (e) => {
    const selectedId = e.target.value;

    setAccountId(selectedId);
    setData(null);
    setError("");

    if (storageKey) {
      sessionStorage.setItem(storageKey, selectedId);
    }
  };

  const selectedAccount = accounts.find(
    (account) => String(account.accountId) === accountId
  );

  const transactions = data?.transactions || [];

  // Scroll to the section associated with a dashboard card.
  const scrollToSection = (sectionId) => {
    document.getElementById(sectionId)?.scrollIntoView({
      behavior: "smooth",
      block: "center",
    });
  };

  return (
    <>
      {/* Page heading */}
      <div className="page-heading">
        <div>
          <span className="eyebrow">OVERVIEW</span>
          <h1>Your financial snapshot</h1>
          <p>Track your recent activity and manage your money.</p>
        </div>

        <button
          className="ghost-button"
          onClick={loadHistory}
          disabled={busy || !accountId || accountsLoading}
        >
          <RefreshCw size={16} />
          Refresh
        </button>
      </div>

      {/* Welcome banner */}
      <div className="welcome-banner">
        <div>
          <span className="eyebrow">YOUR BANKING SPACE</span>
          <h2>Good to see you, {user?.username}.</h2>
          <p>Your accounts and transactions, all in one place.</p>
        </div>

        <div className="banner-icon">
          <Wallet size={34} />
        </div>
      </div>

      {/* Interactive dashboard cards */}
      <div className="user-stats-grid">
        <button
          className="user-stat-card balance-stat"
          onClick={() => scrollToSection("account-overview")}
          disabled={!selectedAccount}
        >
          <div className="user-stat-top">
            <span>AVAILABLE BALANCE</span>
            <div className="user-stat-icon">
              <Wallet size={20} />
            </div>
          </div>

          <h2>
            {accountsLoading
              ? "Loading..."
              : selectedAccount
                ? money(selectedAccount.balance)
                : "₹0.00"}
          </h2>

          <small>Click to view account details</small>
        </button>

        <button
          className="user-stat-card accounts-stat"
          onClick={() => scrollToSection("my-accounts")}
        >
          <div className="user-stat-top">
            <span>MY ACCOUNTS</span>
            <div className="user-stat-icon">
              <CreditCard size={20} />
            </div>
          </div>

          <h2>{accountsLoading ? "..." : accounts.length}</h2>
          <small>View and manage your accounts</small>
        </button>

        <button
          className="user-stat-card activity-stat"
          onClick={() => scrollToSection("recent-transactions")}
          disabled={!accountId}
        >
          <div className="user-stat-top">
            <span>RECENT ACTIVITY</span>
            <div className="user-stat-icon">
              <Activity size={20} />
            </div>
          </div>

          <h2>{busy ? "..." : transactions.length}</h2>
          <small>Transactions currently displayed</small>
        </button>

        <button
          className="user-stat-card account-stat"
          onClick={() => scrollToSection("account-overview")}
          disabled={!selectedAccount}
        >
          <div className="user-stat-top">
            <span>SELECTED ACCOUNT</span>
            <div className="user-stat-icon">
              <TrendingUp size={20} />
            </div>
          </div>

          <h2>
            {selectedAccount
              ? selectedAccount.accountType
              : "No Account"}
          </h2>

          <small>
            {selectedAccount
              ? selectedAccount.status
              : "Create an account to get started"}
          </small>
        </button>
      </div>

      {/* Account selector */}
      <div
        className="panel account-lookup"
        id="my-accounts"
      >
        <div>
          <strong>My Accounts</strong>
          <small>Select an account to manage your money.</small>
        </div>

        {accountsLoading ? (
          <span>Loading accounts...</span>
        ) : accounts.length > 0 ? (
          <div className="account-actions">
            <select
              value={accountId}
              onChange={handleAccountChange}
              className="account-select"
            >
              {accounts.map((account) => (
                <option
                  key={account.accountId}
                  value={account.accountId}
                >
                  {account.accountNumber} - {account.accountType}
                </option>
              ))}
            </select>

            <Link
              to="/accounts"
              className="add-account-button"
            >
              <span>+</span>
              Add Account
            </Link>
          </div>
        ) : (
          <Link
            to="/accounts"
            className="add-account-button"
          >
            <span>+</span>
            Add Account
          </Link>
        )}
      </div>

      {error && (
        <div className="notice error">{error}</div>
      )}

      {/* Account overview */}
      {selectedAccount && (
        <div
          className="panel account-overview"
          id="account-overview"
        >
          <div className="section-title">
            <div>
              <h2>Account Overview</h2>
              <p>{selectedAccount.accountNumber}</p>
            </div>

            <span
              className={`status ${String(
                selectedAccount.status || ""
              ).toLowerCase()}`}
            >
              {selectedAccount.status}
            </span>
          </div>

          <span className="eyebrow">AVAILABLE BALANCE</span>

          <h1>{money(selectedAccount.balance)}</h1>

          <p>
            {selectedAccount.accountType} Account
            {" · "}
            {selectedAccount.currency || "INR"}
          </p>
        </div>
      )}

      {!accountsLoading && accounts.length === 0 && (
        <div className="panel empty-state">
          <Wallet size={30} />
          <strong>You don't have a bank account yet.</strong>
          <span>Create your first account to start banking.</span>

          <Link to="/accounts" className="primary-button">
            Create Your First Account
          </Link>
        </div>
      )}

      {/* Quick actions */}
      <div className="section-title">
        <div>
          <h2>Quick actions</h2>
          <p>Choose what you'd like to do today.</p>
        </div>
      </div>

      <div className="action-grid">
        <Link to="/deposit" className="action-card">
          <span className="action-icon green">
            <ArrowDownLeft />
          </span>
          <strong>Deposit money</strong>
          <small>Add funds to your account</small>
        </Link>

        <Link to="/withdraw" className="action-card">
          <span className="action-icon amber">
            <ArrowUpRight />
          </span>
          <strong>Withdraw money</strong>
          <small>Access your available funds</small>
        </Link>

        <Link to="/transfer" className="action-card">
          <span className="action-icon blue">
            <Activity />
          </span>
          <strong>Transfer money</strong>
          <small>Send money to another account</small>
        </Link>
      </div>

      {/* Recent transactions */}
      <div
        className="section-title"
        id="recent-transactions"
      >
        <div>
          <h2>Recent transactions</h2>
          <p>Your latest account activity.</p>
        </div>

        {accountId && (
          <Link to="/history" className="text-link">
            View all
          </Link>
        )}
      </div>

      <div className="panel table-panel">
        {!accountId ? (
          <div className="empty-state">
            <Wallet size={30} />
            <strong>No account selected</strong>
            <span>
              Create an account to view your transactions.
            </span>
          </div>
        ) : busy ? (
          <div className="empty-state">
            Loading transactions...
          </div>
        ) : transactions.length === 0 ? (
          <div className="empty-state">
            <Activity size={30} />
            <strong>No transactions yet</strong>
            <span>Your activity will appear here.</span>
          </div>
        ) : (
          <TransactionTable rows={transactions} />
        )}
      </div>
    </>
  );
}

function TransactionTable({ rows }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>TRANSACTION</th>
            <th>DATE</th>
            <th>AMOUNT</th>
            <th>STATUS</th>
          </tr>
        </thead>

        <tbody>
          {rows.map((t) => (
            <tr key={t.transactionId}>
              <td>
                <strong>
                  {t.transactionType || "Transaction"}
                </strong>

                <small className="cell-sub">
                  #{t.transactionId}
                </small>
              </td>

              <td>
                {t.createdAt
                  ? new Date(t.createdAt).toLocaleString("en-IN")
                  : "—"}
              </td>

              <td className="amount-cell">
                {money(t.amount)}
              </td>

              <td>
                <span
                  className={`status ${String(
                    t.status || ""
                  ).toLowerCase()}`}
                >
                  {t.status || "UNKNOWN"}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
