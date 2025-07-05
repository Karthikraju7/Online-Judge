import React from "react";
import { useNavigate } from "react-router-dom";

const problems = [
  { id: 1, name: "Two Sum", slug: "two-sum", difficulty: "Easy", submitted: true },
  { id: 2, name: "Reverse Linked List", slug: "reverse-linked-list", difficulty: "Medium", submitted: false },
  { id: 3, name: "Merge Intervals", slug: "merge-intervals", difficulty: "Medium", submitted: true },
  { id: 4, name: "Trapping Rain Water", slug: "trapping-rain-water", difficulty: "Hard", submitted: false },
  { id: 5, name: "Course Schedule", slug: "course-schedule", difficulty: "Medium", submitted: true },
  { id: 6, name: "Valid Parentheses", slug: "valid-parentheses", difficulty: "Easy", submitted: true },
  { id: 7, name: "Word Break", slug: "word-break", difficulty: "Medium", submitted: false },
  { id: 8, name: "LRU Cache", slug: "lru-cache", difficulty: "Hard", submitted: false },
  { id: 9, name: "Binary Tree Zigzag", slug: "binary-tree-zigzag", difficulty: "Medium", submitted: false },
  { id: 10, name: "Kth Largest Element", slug: "kth-largest-element", difficulty: "Medium", submitted: true },
  { id: 11, name: "Maximum Subarray", slug: "maximum-subarray", difficulty: "Easy", submitted: true },
  { id: 12, name: "Clone Graph", slug: "clone-graph", difficulty: "Medium", submitted: false },
  { id: 13, name: "Find Median from Data Stream", slug: "find-median", difficulty: "Hard", submitted: false },
  { id: 14, name: "Longest Palindromic Substring", slug: "longest-palindromic-substring", difficulty: "Medium", submitted: true },
  { id: 15, name: "Subsets II", slug: "subsets-ii", difficulty: "Medium", submitted: false },
];

const Problems = () => {
  const navigate = useNavigate();

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
            key={problem.id}
            className="flex items-center text-sm px-3 py-3 border-b border-gray-800 hover:bg-white/5 transition"
          >
            <div className="w-[15%]">{index + 1}</div>
            <div
              className="w-[55%] text-violet-400 font-medium cursor-pointer hover:underline"
              onClick={() => navigate(`/problems/${problem.slug}`)}
            >
              {problem.name}
            </div>
            <div className="w-[10%] text-center">
              {problem.submitted && <span className="text-green-500 font-bold">✔</span>}
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
