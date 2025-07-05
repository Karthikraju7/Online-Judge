import React, { useState } from "react";
import { useParams } from "react-router-dom";
import { questions } from "../Data/questions.js";

const Question = () => {
  const { slug } = useParams();
  const question = questions.find((q) => q.slug === slug);
  const [selectedLang, setSelectedLang] = useState("C++");

  if (!question) return <div className="text-white p-4">❌ Question not found</div>;

  return (
    <div className="flex h-[calc(100vh-64px-48px)] text-white">
      
      {/* LEFT: Problem */}
      <div className="w-1/2 p-6 overflow-y-auto border-r border-gray-700">
        {/* Title + Difficulty */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">{question.title}</h2>
          <span className={`font-semibold ${question.difficulty === 'Easy' ? 'text-green-400' : question.difficulty === 'Medium' ? 'text-yellow-400' : 'text-red-400'}`}>
            {question.difficulty}
          </span>
        </div>

        {/* Description */}
        <pre className="whitespace-pre-wrap text-sm mb-6">
          {question.description}
        </pre>

        {/* Test Cases */}
        <div className="mt-4">
          <h3 className="text-lg font-semibold mb-2">Test Cases</h3>
          {question.testCases.map((test, idx) => (
            <div key={idx} className="mb-4 text-sm bg-black/30 p-3 rounded border border-white/10">
              <div><strong>Input:</strong> {test.input}</div>
              <div><strong>Expected Output:</strong> {test.expectedOutput}</div>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT: Code */}
      <div className="w-1/2 p-6 flex flex-col">
        
        {/* Top: Header + Buttons */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold">CodeTheCode Here</h2>
          <div className="flex gap-2">
            <button className="bg-blue-600 px-4 py-1 rounded cursor-pointer">Run</button>
            <button className="bg-green-600 px-4 py-1 rounded cursor-pointer">Submit</button>
          </div>
        </div>

        {/* Language Dropdown */}
        <div className="mb-2">
          <select
            className="bg-gray-800 text-white p-2 rounded cursor-pointer"
            value={selectedLang}
            onChange={(e) => setSelectedLang(e.target.value)}
          >
            <option value="cpp">C++</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
            <option value="javascript">JavaScript</option>
          </select>
        </div>

        {/* Code Editor Box (just textarea for now) */}
        <textarea
        className="flex-1 bg-black text-white p-4 rounded resize-none border border-gray-700"
        defaultValue={question.starterCode}
        spellCheck={false} // ✅ this removes the red underline
        />

        {/* Output Area */}
        <div className="mt-4 text-sm bg-black/40 p-3 rounded border border-white/10">
          <strong>Output:</strong> <span className="text-gray-300">[Output will appear here]</span>
        </div>
      </div>
    </div>
  );
};

export default Question;
