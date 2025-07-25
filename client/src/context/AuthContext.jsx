import { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [authUser, setAuthUser] = useState(() => {
    const savedUser = localStorage.getItem("authUser");
    return savedUser ? JSON.parse(savedUser) : null;
  });

  const navigate = useNavigate();

  const loginOrRegister = async (mode, credentials) => {
    if (
      !credentials.email ||
      !credentials.password ||
      (mode === "register" && (!credentials.username || !credentials.confirmPassword))
    ) {
      toast.error("Please fill in all required fields.");
      return;
    }

    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/${mode}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
      });

      if (mode === "login" || mode === "admin-login") {
        const isJson = res.headers.get("content-type")?.includes("application/json");
        const data = isJson ? await res.json() : await res.text();
        console.log(`${mode} response:`, data);

        if (res.ok) {
          const user = { email: data.email, username: data.username, role: data.role };
          setAuthUser(user);
          localStorage.setItem("authUser", JSON.stringify(user));
          toast.success(`${mode === "admin-login" ? "Admin login" : "Login"} successful`);
          navigate("/");
        } else {
          const errorMessage = typeof data === "string" ? data : data.message || "Login failed";
          toast.error(errorMessage);
        }
      } else if (mode === "register") {
        const data = await res.json();
        if (res.ok) {
          const user = { email: data.email, username: data.username, role: data.role };
          setAuthUser(user);
          localStorage.setItem("authUser", JSON.stringify(user));
          toast.success("User registered successfully");
          navigate("/");
        } else {
          const errorMessage = typeof data === "string" ? data : data.message || "Registration failed";
          toast.error(errorMessage);
        }
      }
    } catch (err) {
      console.error(err);
      toast.error("Something went wrong!");
    }
  };

  const logout = () => {
    setAuthUser(null);
    localStorage.removeItem("authUser");
    toast.success("Logged out successfully.");
    navigate("/");
  };

  return (
    <AuthContext.Provider value={{ authUser, loginOrRegister, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
