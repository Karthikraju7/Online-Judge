import React from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";


const Profile = () => {
  const { authUser, logout } = useAuth();
  const navigate = useNavigate();

  if (!authUser) {
    return (
      <div className="h-screen bg-black text-white flex items-center justify-center px-4">
        <p className="text-gray-400 text-lg">Please login to view your profile.</p>
      </div>
    );
  }

  const displayRole = authUser.role ? authUser.role.toUpperCase() : "USER";

  return (
    <div className="h-screen bg-black text-white flex flex-col items-center justify-center px-4 py-8">
      <div
        className="w-full max-w-md bg-gray-900 p-8 rounded-2xl shadow-lg"
        style={{ transform: "translateY(-10%)" }}
      >
        <div className="flex justify-center items-center gap-2 mb-6">
          <h2 className="text-2xl font-bold text-center">Profile</h2>
        </div>
        <div className="space-y-6 text-sm sm:text-base">
          <div className="flex justify-between border-b border-gray-700 pb-2">
            <span className="text-gray-400">User Name</span>
            <span className="text-white font-medium">{authUser.username}</span>
          </div>

          <div className="flex justify-between border-b border-gray-700 pb-2">
            <span className="text-gray-400">Email</span>
            <span className="text-white font-medium">{authUser.email}</span>
          </div>

          <div className="flex justify-between border-b border-gray-700 pb-2">
            <span className="text-gray-400">Role</span>
            <span className="text-white font-medium">{displayRole}</span>
          </div>

          <div className="flex justify-between items-center">
            <span className="text-gray-400">Password</span>
            <button
              onClick={() => navigate("/change-password")}
              className="text-blue-500 font-semibold hover:underline cursor-pointer"
            >
              Change Password
            </button>
          </div>
        </div>
      </div>

      <div className="mt-4">
        <button
          onClick={logout}
          className="text-red-500 font-semibold hover:underline cursor-pointer"
        >
          Logout
        </button>
      </div>
    </div>
  );
};

export default Profile;