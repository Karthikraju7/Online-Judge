import React from "react";
import { Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Footer from "./components/Footer";
import Problems from "./pages/Problems";
import Question from "./pages/Question";
import Profile from "./pages/Profile";
import { Toaster } from "react-hot-toast";
import { useAuth } from "./context/AuthContext";

const App = () => {
  const { authUser } = useAuth(); 
  const isLoggedIn = !!authUser;

  return (
    <div className="h-screen flex flex-col">
      <Navbar isLoggedIn={isLoggedIn} />
      <div className="flex-1 overflow-hidden">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/problems" element={<Problems />} />
          <Route path="/problems/:slug" element={<Question />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </div>
      <Footer />
      <Toaster position="top-center" />
    </div>
  );
};


export default App;
