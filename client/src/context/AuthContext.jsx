import { createContext, useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [authUser, setAuthUser] = useState(null);
  const navigate = useNavigate();

  const loginOrRegister = async (mode, credentials) => {
    if (
      !credentials.email ||
      !credentials.password ||
      (mode === "register" && !credentials.username)
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

      if (mode === "login") {
          const isJson = res.headers.get("content-type")?.includes("application/json");
          const data = isJson ? await res.json() : await res.text();

          if (res.ok) {
            setAuthUser({ email: data.email, username: data.username });
            toast.success("Login successful");
            navigate("/");
          } else {
            toast.error(data); // plain text error from backend
          }
        }  else {
        const message = await res.text();
        if (res.ok) {
          setAuthUser({ email: credentials.email, username: credentials.username });
          toast.success(message); // e.g. "User registered successfully"
          navigate("/");
        } else {
          toast.error(message);
        }
      }
    } catch (err) {
      console.error(err);
      toast.error("Something went wrong!");
    }
  };

  const logout = () => {
    setAuthUser(null);
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
