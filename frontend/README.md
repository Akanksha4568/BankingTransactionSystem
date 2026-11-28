# Banking Transaction System – React frontend

React + Vite frontend for the existing Spring Boot banking backend.

## Run locally

1. Install Node.js (LTS).
2. Extract this folder.
3. Open a terminal in this folder.
4. Run:

```bash
npm install
```

5. Copy `.env.example` to `.env` (or create `.env`) and set:

```env
VITE_API_BASE_URL=http://localhost:8080
```

6. Start the frontend:

```bash
npm run dev
```

Open the URL printed by Vite, normally http://localhost:5173.

## Backend integration

The frontend is configured for these known backend routes:

- `POST /api/auth/login`
- `POST /api/auth/register` (confirm this route against your controller)
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `POST /api/transactions/transfer`
- `GET /api/transactions/history`

The history request uses `userId`, `accountId`, `page`, and `size`.

Account listing/details endpoints were not present in the supplied project ZIP. To avoid inventing an endpoint, this starter asks the user to enter their account ID where required. The dashboard shows transaction history and account activity using the existing history API.

If your backend uses a different registration path or payload, update `src/api/auth.js`. If CORS blocks the browser, configure Spring Boot to allow the Vite origin `http://localhost:5173`.

## Authentication note

This starter stores the JWT in `sessionStorage` and sends it as `Authorization: Bearer <token>`. For production banking software, prefer secure HttpOnly cookies and derive the user identity from the authenticated server-side principal. The current backend request contracts discussed during development also include `userId`; this frontend sends the logged-in user's returned ID to match that contract.
