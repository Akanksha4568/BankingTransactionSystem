
import api from './client';

// Fetch all accounts belonging to the logged-in user
export async function getMyAccounts() {
  const { data } = await api.get('/api/accounts/my-accounts');
  return data;
}

export async function depositRequest(payload) {
  const { data } = await api.post('/api/transactions/deposit', payload);
  return data;
}

export async function withdrawRequest(payload) {
  const { data } = await api.post('/api/transactions/withdraw', payload);
  return data;
}

export async function transferRequest(payload) {
  const { data } = await api.post('/api/transactions/transfer', payload);
  return data;
}

export async function historyRequest({ userId, accountId, page = 0, size = 10 }) {
  const { data } = await api.get('/api/transactions/history', {
    params: { userId, accountId, page, size },
  });
  return data;
}

export async function createAccountRequest(accountType) {
  const { data } = await api.post('/api/accounts/create', {
    accountType
  });

  return data;
}
