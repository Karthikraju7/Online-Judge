import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { FiEye, FiEyeOff } from "react-icons/fi";
import { useNavigate } from "react-router-dom";

const Login = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [isAdminMode, setIsAdminMode] = useState(false);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [username, setUsername] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const navigate = useNavigate();

  const { loginOrRegister } = useAuth();

  const toggleForm = () => setIsLogin(!isLogin);

  const handleUserTypeChange = (type) => {
    setIsAdminMode(type === "admin");
    setIsLogin(true); // always login mode for admin
    setEmail("");
    setPassword("");
    setConfirmPassword("");
    setUsername("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const role = isAdminMode ? "admin" : "user";

    const credentials = {
      email,
      password,
      role,
      ...(isLogin || isAdminMode ? {} : { username, confirmPassword }),
    };

    // Use admin-login endpoint if admin mode, else login or register
    const endpoint = isAdminMode ? "admin-login" : isLogin ? "login" : "register";

    await loginOrRegister(endpoint, credentials);
  };

  return (
    <div className="h-full flex items-center justify-center bg-black text-white px-4">
      <div className="bg-gray-900 rounded-2xl shadow-xl w-full max-w-md p-8">
        {/* Admin/User toggle */}
        <div className="flex justify-center gap-4 mb-6">
          <button
            className={`px-4 py-2 rounded-xl cursor-pointer ${
              isAdminMode ? "bg-blue-600 text-white" : "bg-gray-800 text-gray-400"
            }`}
            onClick={() => handleUserTypeChange("admin")}
          >
            Admin
          </button>
          <button
            className={`px-4 py-2 rounded-xl cursor-pointer ${
              !isAdminMode ? "bg-blue-600 text-white" : "bg-gray-800 text-gray-400"
            }`}
            onClick={() => handleUserTypeChange("user")}
          >
            User
          </button>
        </div>

        <h2 className="text-2xl font-bold mb-6 text-center">
          {isLogin ? "Login to Your Account" : "Create an Account"}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">
          {!isLogin && !isAdminMode && (
            <input
              type="text"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
          )}

          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600"
          />

          {/* Password */}
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-2 pr-10 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
            <div
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer"
            >
              {showPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
            </div>
          </div>

          {/* Confirm Password */}
          {!isLogin && !isAdminMode && (
            <div className="relative">
              <input
                type={showConfirmPassword ? "text" : "password"}
                placeholder="Confirm Password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="w-full px-4 py-2 pr-10 bg-gray-800 border border-gray-700 rounded-xl text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
              <div
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer"
              >
                {showConfirmPassword ? <FiEyeOff size={18} /> : <FiEye size={18} />}
              </div>
            </div>
          )}

          <button
            type="submit"
            className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-xl transition cursor-pointer"
          >
            {isLogin ? "Login" : "Register"}
          </button>
          {isLogin && !isAdminMode && (
            <div className="text-right text-sm text-blue-500 hover:underline cursor-pointer">
              <span onClick={() => navigate("/forgot-password")}>Forgot Password?</span>
            </div>
          )}
        </form>

        {!isAdminMode && (
          <p className="text-center mt-4 text-sm text-gray-400">
            {isLogin ? "Don't have an account?" : "Already have an account?"}{" "}
            <button
              onClick={toggleForm}
              className="text-blue-500 font-semibold hover:underline cursor-pointer"
            >
              {isLogin ? "Register" : "Login"}
            </button>
          </p>
        )}
      </div>
    </div>
  );
};

export default Login;
