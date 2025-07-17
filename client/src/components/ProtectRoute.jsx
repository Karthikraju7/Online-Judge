import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ProtectedRoute = ({ children, role }) => {
  const { authUser } = useAuth();

  // If not logged in, redirect to login page
  if (!authUser) return <Navigate to="/login" />;

  // If role is specified and does not match logged-in user's role, redirect home
  if (role && authUser.role?.toLowerCase() !== role.toLowerCase()) {
    return <Navigate to="/" />;
  }

  // Else render children components (protected content)
  return children;
};

export default ProtectedRoute;
