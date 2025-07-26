import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

const Verify = () => {
  const [message, setMessage] = useState("Verifying your account...");
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (!token) {
      setMessage("Invalid verification link.");
      return;
    }

    const verifyAccount = async () => {
      try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/verify?token=${token}`);
        const data = await res.json();

        if (res.ok) {
          setMessage("Your account has been verified successfully! You can now login.");
          toast.success("Account verified successfully!");
          setTimeout(() => navigate("/login"), 3000); // Auto redirect after 3 sec
        } else {
          setMessage(data.message || "Verification failed. Please register again.");
          toast.error(data.message || "Verification failed.");
        }
      } catch (err) {
        console.error(err);
        setMessage("Something went wrong while verifying.");
      }
    };

    verifyAccount();
  }, [navigate]);

  return (
    <div className="flex items-center justify-center h-screen bg-black text-white">
      <div className="bg-gray-900 p-6 rounded-xl text-center shadow-xl">
        <h2 className="text-xl font-bold mb-2">Email Verification</h2>
        <p>{message}</p>
      </div>
    </div>
  );
};

export default Verify;
