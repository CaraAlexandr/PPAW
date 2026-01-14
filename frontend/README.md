# Password Vault Frontend

React frontend application for the Password Vault system.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

## Features

- **Login**: Authenticate with username/email and password
- **Protected Routes**: Automatic redirect to login if not authenticated
- **Vault Management**: 
  - View all vault items
  - Create new vault items
  - Edit existing vault items
  - Delete vault items
- **Logout**: Clear session and redirect to login

## API Integration

The frontend communicates with the backend API at `http://localhost:8080/api`. 

Authentication tokens are stored in localStorage and automatically included in API requests via axios interceptors.

