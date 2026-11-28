import React from "react";
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import Notice from './Notice';
import { getErrorMessage } from '../api/client';

const makeKey = (prefix) => `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;

export default function TransactionForm({ kind, submit }) {
  const { user } = useAuth();
  const [accountId, setAccountId] = useState('');
  const [toAccountId, setToAccountId] = useState('');
  const [amount, setAmount] = useState('');
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const isTransfer = kind === 'transfer';
  const isDeposit = kind === 'deposit';
  const title = isDeposit ? 'Add money' : isTransfer ? 'Transfer money' : 'Withdraw money';
  const description = isDeposit ? 'Move funds into your account.' : isTransfer ? 'Send money between bank accounts.' : 'Withdraw funds from your account.';

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(''); setResult(null);
    if (!accountId || Number(accountId) <= 0) return setError('Enter a valid account ID.');
    if (!amount || Number(amount) <= 0) return setError('Enter an amount greater than zero.');
    if (isTransfer && (!toAccountId || Number(toAccountId) <= 0)) return setError('Enter a valid recipient account ID.');
    if (isTransfer && accountId === toAccountId) return setError('Sender and recipient accounts must be different.');
    const payload = { userId: user.userId, amount: Number(amount), idempotencyKey: makeKey(kind.toUpperCase()) };
    if (isDeposit) payload.accountId = Number(accountId);
    else if (isTransfer) { payload.fromAccountId = Number(accountId); payload.toAccountId = Number(toAccountId); }
    else payload.accountId = Number(accountId);
    setBusy(true);
    try {
      const data = await submit(payload);
      setResult(data || { status: 'SUCCESS' });
      setAmount('');
    } catch (err) {
      setError(getErrorMessage(err));
    } finally { setBusy(false); }
  };

  return <div className="form-layout"><div className="form-intro"><div className="section-icon">{isDeposit ? '↓' : isTransfer ? '⇄' : '↑'}</div><p className="eyebrow">MONEY MOVEMENT</p><h1>{title}</h1><p>{description}</p><div className="secure-note">🔒 <span>Transactions are submitted securely to your bank server.</span></div></div>
    <form className="panel transaction-form" onSubmit={handleSubmit}>
      <div className="panel-heading"><div><h2>Transaction details</h2><p>Enter the required information below.</p></div><span className="step-pill">01 / DETAILS</span></div>
      <label>{isTransfer ? 'From account ID' : 'Account ID'}<input type="number" min="1" value={accountId} onChange={e => setAccountId(e.target.value)} placeholder="e.g. 368" required/></label>
      {isTransfer && <label>To account ID<input type="number" min="1" value={toAccountId} onChange={e => setToAccountId(e.target.value)} placeholder="Recipient account ID" required/></label>}
      <label>Amount (INR)<div className="amount-input"><span>₹</span><input type="number" min="0.01" step="0.01" value={amount} onChange={e => setAmount(e.target.value)} placeholder="0.00" required/></div></label>
      <Notice type="error">{error}</Notice>
      {result && <Notice>Transaction submitted. Status: <strong>{result.status || 'SUCCESS'}</strong>{result.transactionId ? ` · Transaction #${result.transactionId}` : ''}</Notice>}
      <button className="primary-button full" disabled={busy}>{busy ? 'Processing…' : `Confirm ${isDeposit ? 'deposit' : isTransfer ? 'transfer' : 'withdrawal'}`}</button>
      <p className="fine-print">A unique transaction reference is generated automatically for each submission.</p>
    </form>
  </div>;
}