import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import VaultPage from './pages/VaultPage';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/vault"
          element={
            <ProtectedRoute>
              <VaultPage />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/vault" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

