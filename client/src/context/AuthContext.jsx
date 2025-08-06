import { createContext, useContext, useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [authUser, setAuthUser] = useState(() => {
    const savedUser = localStorage.getItem("authUser");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const navigate = useNavigate();
  const logoutTimer = useRef(null);

  const decodeToken = (token) => {
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return payload;
    } catch {
      return null;
    }
  };

  const setAutoLogout = (token) => {
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) return;

    const expiryTime = decoded.exp * 1000 - Date.now();
    if (expiryTime <= 0) {
      logout();
    } else {
      logoutTimer.current = setTimeout(() => {
        toast.error("Session expired. Please login again.");
        logout();
      }, expiryTime);
    }
  };

  useEffect(() => {
    if (authUser?.token) {
      console.log("🔐 Token present, setting auto logout timer...");
      setAutoLogout(authUser.token);
    }
    return () => clearTimeout(logoutTimer.current);
  }, [authUser]);

  const loginOrRegister = async (mode, credentials) => {
    if (
      !credentials.email ||
      !credentials.password ||
      (mode === "register" &&
        (!credentials.username || !credentials.confirmPassword))
    ) {
      toast.error("Please fill in all required fields.");
      return;
    }

    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/${mode}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
      });

      const data = await res.json();
      console.log(`${mode} response:`, data);

      if (mode === "login" || mode === "admin-login") {
        if (res.ok) {
          const user = {
            email: data.email,
            username: data.username,
            role: data.role,
            token: data.token,
          };

          setAuthUser(user);
          localStorage.setItem("authUser", JSON.stringify(user));
          setAutoLogout(data.token);

          toast.success(
            `${mode === "admin-login" ? "Admin login" : "Login"} successful`
          );
          navigate("/");
        } else {
          toast.error(data.message || "Login failed");
        }
      } else if (mode === "register") {
        if (res.ok) {
          toast.success(
            "Registration successful! Please check your email to verify your account."
          );
          navigate("/login");
        } else {
          toast.error(data.message || "Registration failed");
        }
      }
    } catch (err) {
      console.error("❌ loginOrRegister error:", err);
      toast.error("Something went wrong!");
    }
  };

  const logout = () => {
    clearTimeout(logoutTimer.current);
    setAuthUser(null);
    localStorage.removeItem("authUser");
    toast.success("Logged out successfully.");
    navigate("/");
  };

  const authFetch = async (url, options = {}) => {
    const token = authUser?.token;
    if (!token) console.warn("⚠️ No token found in authUser");

    const headers = {
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.method && options.method !== "GET"
        ? { "Content-Type": "application/json" }
        : {}),
    };

    console.log("📤 authFetch URL:", url);
    console.log("🔐 Token:", token);
    console.log("📦 Headers:", headers);
    console.log("🧾 Body:", options.body);

    try {
      const res = await fetch(url, {
        ...options,
        headers,
        credentials: "include", // IMPORTANT for some CORS issues
      });
      return res;
    } catch (err) {
      console.error("❌ authFetch failed:", err);
      throw err;
    }
  };

  return (
    <AuthContext.Provider
      value={{ authUser, loginOrRegister, logout, authFetch }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
