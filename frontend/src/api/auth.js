import api from './client';

export async function loginRequest(payload) {
  const { data } = await api.post('/api/auth/login', payload);
  return data;
}

// If your AuthController uses a different mapping, change this path.
export async function registerRequest(payload) {
  const { data } = await api.post('/api/auth/register', payload);
  return data;
}