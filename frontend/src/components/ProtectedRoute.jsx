import { Navigate } from 'react-router-dom';

/**
 * LAB 8: ProtectedRoute guard - redirects to /login if no token/userId
 */
function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const userId = localStorage.getItem('userId');

  if (!token || !userId) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;

