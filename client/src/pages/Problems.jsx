import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const Problems = () => {
  const [problems, setProblems] = useState([]);
  const [search, setSearch] = useState("");
  const navigate = useNavigate();
  const { authUser, authFetch } = useAuth(); // use authFetch

  useEffect(() => {
    if (!authUser?.email) return;

    authFetch(`${import.meta.env.VITE_API_URL}/problems/all?email=${authUser.email}`)
      .then((res) => {
        if (!res.ok) {
          throw new Error("Unauthorized or failed fetch");
        }
        return res.json();
      })
      .then((data) => setProblems(data))
      .catch((err) => console.error("Error fetching problems:", err));
  }, [authUser, authFetch]);

  const filteredProblems = problems.filter((p) =>
    p.title.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="px-4 pt-6 pb-4 text-white">
      <h2 className="text-4xl font-extrabold mb-8 text-center text-white">
        Problems
      </h2>

      {/* Search Bar */}
      <div className="max-w-5xl mx-auto mb-5">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search problems..."
          className="w-full px-4 py-2 rounded-xl bg-gray-900 text-white placeholder-gray-400 border border-gray-700 focus:outline-none focus:ring-2 focus:ring-purple-500"
        />
      </div>

      {/* Header */}
      <div className="max-w-5xl mx-auto font-bold text-sm flex px-3 py-3 rounded-lg bg-gradient-to-r from-gray-800 to-gray-900 border border-gray-700 shadow-md">
        <div className="w-[15%]">S No</div>
        <div className="w-[55%]">Problem Name</div>
        <div className="w-[10%] text-center">Dusted</div>
        <div className="w-[20%]">Difficulty</div>
      </div>

      {/* Problem List */}
      <div
        className="max-w-5xl mx-auto overflow-y-auto mt-2 rounded-lg border border-gray-800 shadow-inner"
        style={{ maxHeight: "calc(100vh - 220px)" }}
      >
        {filteredProblems.length > 0 ? (
          filteredProblems.map((problem, index) => (
            <div
              key={index}
              className="flex items-center text-sm px-3 py-3 border-b border-gray-800 hover:bg-gradient-to-r hover:from-purple-500/10 hover:to-pink-500/10 transition-all duration-300"
            >
              <div className="w-[15%] text-gray-300">{index + 1}</div>
              <div
                className="w-[55%] text-cyan-400 font-medium cursor-pointer hover:underline"
                onClick={() => navigate(`/problems/${problem.slug}`)}
              >
                {problem.title}
              </div>
              <div className="w-[10%] text-center">
                {problem.solved && <span className="text-green-500 font-bold">✔</span>}
              </div>
              <div
                className={`w-[20%] font-semibold ${
                  problem.difficulty === "Easy"
                    ? "text-green-400"
                    : problem.difficulty === "Medium"
                    ? "text-yellow-400"
                    : "text-red-400"
                }`}
              >
                {problem.difficulty}
              </div>
            </div>
          ))
        ) : (
          <div className="text-center text-gray-500 py-4">No problems found.</div>
        )}
        <div className="h-6"></div>
      </div>
    </div>
  );
};

export default Problems;
