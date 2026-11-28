
import React, { useEffect, useState } from 'react';
import { historyRequest } from '../api/transactions';
import { getErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';

const money = (value) =>
  new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
  }).format(Number(value || 0));

export default function TransactionHistory() {
  const { user } = useAuth();

  const storageKey = `banking_account_id_${user?.userId}`;

  const [accountId, setAccountId] = useState(
    sessionStorage.getItem(storageKey) || ''
  );

  const [activeAccountId, setActiveAccountId] = useState(
    sessionStorage.getItem(storageKey) || ''
  );

  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [reload, setReload] = useState(0);

  useEffect(() => {
    if (!activeAccountId || !user?.userId) {
      setData(null);
      return;
    }

    let cancelled = false;

    const loadHistory = async () => {
      setBusy(true);
      setError('');

      try {
        const result = await historyRequest({
          userId: user.userId,
          accountId: activeAccountId,
          page,
          size: 10,
        });

        if (!cancelled) {
          setData(result);
        }
      } catch (err) {
        if (!cancelled) {
          setData(null);
          setError(getErrorMessage(err));
        }
      } finally {
        if (!cancelled) {
          setBusy(false);
        }
      }
    };

    loadHistory();

    return () => {
      cancelled = true;
    };
  }, [activeAccountId, page, reload, user?.userId]);

  const handleSearch = (e) => {
    e.preventDefault();

    if (!accountId || Number(accountId) <= 0) {
      setError('Please enter a valid account ID.');
      return;
    }

    sessionStorage.setItem(storageKey, accountId);

    setPage(0);
    setActiveAccountId(accountId);
    setReload((previous) => previous + 1);
  };

  const rows = data?.transactions || [];
  const pages = data?.totalPages ?? 0;
  const totalTransactions = data?.totalTransactions ?? 0;

  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">ACCOUNT ACTIVITY</span>
          <h1>Transaction History</h1>
          <p>Review your deposits, withdrawals and transfers.</p>
        </div>
      </div>

      <form
        className="account-lookup panel"
        onSubmit={handleSearch}
      >
        <div>
          <strong>Account Filter</strong>
          <small>Enter the account ID you want to review.</small>
        </div>

        <input
          type="number"
          min="1"
          value={accountId}
          onChange={(e) => setAccountId(e.target.value)}
          placeholder="Account ID"
          required
        />

        <button
          type="submit"
          className="primary-button"
          disabled={busy}
        >
          Search
        </button>
      </form>

      {error && (
        <div className="notice error">
          {error}
        </div>
      )}

      <div className="panel table-panel">
        {busy ? (
          <div className="empty-state">
            Loading transactions...
          </div>
        ) : rows.length === 0 ? (
          <div className="empty-state">
            {activeAccountId
              ? 'No transactions found for this account.'
              : 'Select an account ID to view your transactions.'}
          </div>
        ) : (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>TYPE</th>
                  <th>FROM</th>
                  <th>TO</th>
                  <th>AMOUNT</th>
                  <th>DATE</th>
                  <th>STATUS</th>
                </tr>
              </thead>

              <tbody>
                {rows.map((transaction) => (
                  <tr key={transaction.transactionId}>
                    <td>#{transaction.transactionId}</td>

                    <td>
                      <strong>{transaction.transactionType}</strong>
                      {transaction.description && (
                        <small className="cell-sub">
                          {transaction.description}
                        </small>
                      )}
                    </td>

                    <td>
                      {transaction.fromAccountId ?? '—'}
                    </td>

                    <td>
                      {transaction.toAccountId ?? '—'}
                    </td>

                    <td className="amount-cell">
                      {money(transaction.amount)}
                    </td>

                    <td>
                      {transaction.createdAt
                        ? new Date(transaction.createdAt).toLocaleString('en-IN')
                        : '—'}
                    </td>

                    <td>
                      <span
                        className={`status ${String(
                          transaction.status || ''
                        ).toLowerCase()}`}
                      >
                        {transaction.status || '—'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="pagination">
        <span>
          Page {page + 1} of {Math.max(pages, 1)}
          {' · '}
          {totalTransactions} transactions
        </span>

        <div>
          <button
            type="button"
            className="ghost-button"
            disabled={page === 0 || busy}
            onClick={() => setPage((previous) => previous - 1)}
          >
            Previous
          </button>

          <button
            type="button"
            className="ghost-button"
            disabled={!pages || page + 1 >= pages || busy}
            onClick={() => setPage((previous) => previous + 1)}
          >
            Next
          </button>
        </div>
      </div>
    </>
  );
}
