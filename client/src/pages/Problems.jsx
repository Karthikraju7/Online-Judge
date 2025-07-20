import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext"; // Adjust the path as needed

const Problems = () => {
  const [problems, setProblems] = useState([]);
  const navigate = useNavigate();
  const { authUser } = useAuth(); // 🔥 GET LOGGED-IN USER

  useEffect(() => {
    if (!authUser?.email) return;

    fetch(`http://localhost:8080/problems/all?email=${authUser.email}`)
      .then((res) => res.json())
      .then((data) => {
        setProblems(data);
      })
      .catch((err) => {
        console.error("Error fetching problems:", err);
      });
  }, [authUser]);

  return (
    <div className="px-4 pt-6 pb-4 text-white">
      <h2 className="text-3xl font-bold mb-6 text-center">Problems</h2>

      <div className="max-w-5xl mx-auto font-semibold text-sm flex px-3 py-3 border-b border-gray-700">
        <div className="w-[15%]">S No</div>
        <div className="w-[55%]">Problem Name</div>
        <div className="w-[10%] text-center">Dusted</div>
        <div className="w-[20%]">Difficulty</div>
      </div>

      <div className="max-w-5xl mx-auto overflow-y-auto" style={{ maxHeight: "calc(100vh - 220px)" }}>
        {problems.map((problem, index) => (
          <div
            key={index}
            className="flex items-center text-sm px-3 py-3 border-b border-gray-800 hover:bg-white/5 transition"
          >
            <div className="w-[15%]">{index + 1}</div>
            <div
              className="w-[55%] text-violet-400 font-medium cursor-pointer hover:underline"
              onClick={() => navigate(`/problems/${problem.slug}`)}
            >
              {problem.title}
            </div>
            <div className="w-[10%] text-center">
              {problem.solved && <span className="text-green-500 font-bold">✔</span>}
            </div>
            <div
              className={`w-[20%] font-medium ${
                problem.difficulty === "Easy"
                  ? "text-green-400"
                  : problem.difficulty === "Medium"
                  ? "text-yellow-400"
                  : "text-red-500"
              }`}
            >
              {problem.difficulty}
            </div>
          </div>
        ))}
        <div className="h-6"></div>
      </div>
    </div>
  );
};

export default Problems;
