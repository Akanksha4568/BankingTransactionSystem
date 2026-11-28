
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Wallet,
  PiggyBank,
  Building2,
  ShieldCheck,
  ArrowLeft,
  CheckCircle2
} from 'lucide-react';

import { createAccountRequest } from '../api/transactions';
import { getErrorMessage } from '../api/client';

export default function AddAccount() {
  const [accountType, setAccountType] = useState('SAVINGS');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [createdAccount, setCreatedAccount] = useState(null);
  const [busy, setBusy] = useState(false);

  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setBusy(true);

    try {
      const data = await createAccountRequest(accountType);

      setCreatedAccount(data);
      setSuccess('Your bank account has been created successfully!');

      setTimeout(() => {
        navigate('/');
      }, 1800);

    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="add-account-page">

      <Link to="/" className="back-dashboard-link">
        <ArrowLeft size={17} />
        Back to Dashboard
      </Link>

      <div className="add-account-heading">
        <span className="eyebrow">ACCOUNT SERVICES</span>
        <h1>Open a New Account</h1>
        <p>
          Choose an account that suits your banking needs.
          Your new account will be linked to your existing profile.
        </p>
      </div>

      <div className="add-account-card">

        <div className="add-account-card-heading">
          <div className="add-account-icon">
            <Wallet size={25} />
          </div>
          <div>
            <h2>Choose Account Type</h2>
            <p>Select one of the account options below.</p>
          </div>
        </div>

        {error && (
          <div className="add-account-error">{error}</div>
        )}

        {success && (
          <div className="add-account-success">
            <CheckCircle2 size={19} />
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit}>

          <div className="account-type-options">

            <button
              type="button"
              className={`account-type-card ${accountType === 'SAVINGS' ? 'selected' : ''}`}
              onClick={() => setAccountType('SAVINGS')}
            >
              <div className="account-type-top">
                <div className="account-type-icon savings-icon">
                  <PiggyBank size={27} />
                </div>
                <span className="account-radio">
                  {accountType === 'SAVINGS' && <span />}
                </span>
              </div>

              <h3>Savings Account</h3>
              <p>
                A simple account for everyday banking
                and building your savings.
              </p>

              <span className="account-type-tag">PERSONAL BANKING</span>
            </button>

            <button
              type="button"
              className={`account-type-card ${accountType === 'CURRENT' ? 'selected' : ''}`}
              onClick={() => setAccountType('CURRENT')}
            >
              <div className="account-type-top">
                <div className="account-type-icon current-icon">
                  <Building2 size={27} />
                </div>
                <span className="account-radio">
                  {accountType === 'CURRENT' && <span />}
                </span>
              </div>

              <h3>Current Account</h3>
              <p>
                An account option designed for
                frequent business transactions.
              </p>

              <span className="account-type-tag">BUSINESS BANKING</span>
            </button>

          </div>

          <div className="account-preview">
            <div className="preview-left">
              <span>SELECTED ACCOUNT</span>
              <strong>{accountType} ACCOUNT</strong>
              <small>Initial balance: ₹0.00</small>
            </div>

            <div className="preview-wallet">
              <Wallet size={27} />
            </div>
          </div>

          <div className="account-security-note">
            <ShieldCheck size={19} />
            <span>
              Your new account will be securely linked to your
              existing customer profile.
            </span>
          </div>

          <button
            type="submit"
            className="create-account-submit"
            disabled={busy || !!createdAccount}
          >
            {busy
              ? 'Creating Your Account...'
              : createdAccount
                ? 'Account Created Successfully'
                : 'Create My Account'}
          </button>

        </form>

        {createdAccount && (
          <div className="created-account-details">
            <strong>New account details</strong>
            <span>Account Number: {createdAccount.accountNumber}</span>
            <span>Account Type: {createdAccount.accountType}</span>
          </div>
        )}

      </div>

      <p className="add-account-footer">
        Secure Banking · Simple Account Management
      </p>

    </div>
  );
}
