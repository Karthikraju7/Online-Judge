import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FiUser } from "react-icons/fi";
import { useAuth } from "../context/AuthContext"

function Navbar({ isLoggedIn }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { authUser } = useAuth();

  const isProfilePage = location.pathname === '/profile';
  const isLoginPage = location.pathname === '/login';
  const isProblemsPage = location.pathname === '/problems';

  // Parse localStorage user role safely
  let isAdmin = false;
  try {
    const user = JSON.parse(localStorage.getItem("authUser"));
    isAdmin = user?.role?.toUpperCase() === "ADMIN";
  } catch (e) {}

  return (
    <nav className="bg-gray-900 text-white relative flex items-center justify-between px-4 py-3 w-full">
      {/* Logo */}
      <div
        className="text-xl font-bold cursor-pointer flex-shrink-0"
        onClick={() => navigate('/')}
      >
        CodeTheCode
      </div>

      {/* Center - Problems Link */}
      {!isProblemsPage && (
        <div
          className="absolute left-1/2 transform -translate-x-1/2 text-base sm:text-lg font-semibold cursor-pointer whitespace-nowrap"
          onClick={() => {
            if (authUser) {
              navigate('/problems');
            } else {
              navigate('/login');
            }
          }}
        >
          Problems
        </div>
      )}

      {/* Right - Auth Buttons */}
      <div className="flex items-center gap-4 flex-shrink-0">
        {isLoggedIn ? (
          <>
            {/* Admin Panel Button */}
            {isAdmin && (
              <button
                className="border border-white rounded px-2 py-1 hover:bg-white hover:text-black transition text-sm sm:text-base cursor-pointer"
                onClick={() => navigate('/admin')}
              >
                Admin Panel
              </button>
            )}

            {!isProfilePage && (
              <FiUser
                title="Profile"
                className="h-6 w-6 cursor-pointer text-white"
                onClick={() => navigate('/profile')}
              />
            )}
          </>
        ) : (
          !isLoginPage && (
            <button
              className="border border-white rounded px-2 py-1 hover:bg-white hover:text-black transition cursor-pointer text-sm sm:text-base"
              onClick={() => navigate('/login')}
            >
              Login
            </button>
          )
        )}
      </div>
    </nav>
  );
}

export default Navbar;
