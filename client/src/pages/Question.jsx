import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { questions } from "../Data/questions.js";
import Editor from "@monaco-editor/react";

const Question = () => {
  const { slug } = useParams();
  const question = questions.find((q) => q.slug === slug);
  const [selectedLang, setSelectedLang] = useState("cpp");
  const [code, setCode] = useState(question?.starterCode?.cpp || "");
  const [output, setOutput] = useState("");
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (!question?.testCases?.[0]) return;

        const rawInput = question.testCases[0].input;

        if (selectedLang === "python") {
          try {
            const inputObj = JSON.parse(rawInput); // 👈 directly parse JSON string
            inputObj["expected"] = JSON.parse(question.testCases[0].expectedOutput);
            setInput(JSON.stringify(inputObj));
          } catch (err) {
            console.error("Error parsing input for Python:", err);
            console.log("rawInput:", rawInput);
            setInput(""); // fallback to empty string to prevent crash
          }
        } else {
          setInput(rawInput);
        }
      }, [selectedLang, question]);


  useEffect(() => {
    if (question?.starterCode?.[selectedLang]) {
      setCode(question.starterCode[selectedLang]);
    }
  }, [selectedLang, question]);

  if (!question) return <div className="text-white p-4">Question not found</div>;

  const handleRun = async () => {
  setIsLoading(true);
  try {
    let cleanedCode = code
      .split('\n')
      .filter(line => !line.trim().startsWith('def twoSum'))
      .join('\n');

    console.log("Sending input:", input);
    const res = await fetch("http://localhost:8080/problems/run", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        language: selectedLang,
        code: cleanedCode, // send cleaned code here
        input,
        slug,
      }),
    });

    const data = await res.json();
    setOutput(`${data.output}`);
  } catch (err) {
    setOutput("❌ Server error");
  } finally {
    setIsLoading(false);
  }
};

const handleSubmit = async () => {
  setIsLoading(true);
  try {
    console.log("📤 Submitting code...");
    console.log("Payload:", {
      language: selectedLang,
      code,
      slug,
    });

    const res = await fetch("http://localhost:8080/problems/submit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        language: selectedLang,
        code,
        slug,
      }),
    });

    const data = await res.json();
    console.log("✅ Response from server:", data);

    if (data?.inputs) {
      console.log("🧪 Inputs used during evaluation:");
      data.inputs.forEach((input, index) => {
        console.log(`🔍 Test ${index + 1}:`, input);
      });
    }

    setOutput(`🧠 Verdict: ${data.verdict}`);
  } catch (err) {
    console.error("❌ Submit error:", err);
    setOutput("❌ Server error");
  } finally {
    setIsLoading(false);
  }
};



  return (
    <div className="flex h-[calc(100vh-64px-48px)] text-white">
      {/* LEFT */}
      <div className="w-1/2 p-6 overflow-y-auto border-r border-gray-700">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold">{question.title}</h2>
          <span
            className={`font-semibold ${
              question.difficulty === "Easy"
                ? "text-green-400"
                : question.difficulty === "Medium"
                ? "text-yellow-400"
                : "text-red-400"
            }`}
          >
            {question.difficulty}
          </span>
        </div>

        <pre className="whitespace-pre-wrap text-sm mb-6">
          {question.description}
        </pre>

        <div className="mt-4">
          <h3 className="text-lg font-semibold mb-2">Test Cases</h3>
          {question.testCases.map((test, idx) => (
            <div
              key={idx}
              className="mb-4 text-sm bg-black/30 p-3 rounded border border-white/10"
            >
              <div>
                <strong>Input:</strong> {test.input}
              </div>
              <div>
                <strong>Expected Output:</strong> {test.expectedOutput}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* RIGHT */}
      <div className="w-1/2 p-6 flex flex-col">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold">CodeTheCode Here</h2>
          <div className="flex gap-2">
            <button
              onClick={handleRun}
              className="bg-blue-600 px-4 py-1 rounded hover:bg-blue-700 cursor-pointer"
            >
              Run
            </button>
            <button
              onClick={handleSubmit}
              className="bg-green-600 px-4 py-1 rounded hover:bg-green-700 cursor-pointer"
            >
              Submit
            </button>
          </div>
        </div>

        <div className="mb-2">
          <select
            className="bg-gray-800 text-white p-2 rounded cursor-pointer"
            value={selectedLang}
            onChange={(e) => setSelectedLang(e.target.value)}
          >
            <option value="cpp">C++</option>
            <option value="java">Java</option>
            <option value="python">Python</option>
          </select>
        </div>

        <div className="flex-1 mb-4 border border-gray-700 rounded overflow-hidden">
          <Editor
            height="100%"
            width="100%"
            language={selectedLang.toLowerCase()}
            value={code}
            onChange={(newCode) => setCode(newCode)}
            theme="vs-dark"
            options={{
              fontSize: 14,
              minimap: { enabled: false },
              wordWrap: "on",
              tabSize: 12,
              automaticLayout: true,
              lineNumbers: "on",
            }}
          />
        </div>

        {isLoading && (
          <div className="text-yellow-300 mt-2">⏳ Running Code...</div>
        )}
        <div className="mt-4 text-sm bg-black/40 p-3 rounded border border-white/10">
          <strong>Output:</strong>
          <pre className="text-gray-300 whitespace-pre-wrap">{output}</pre>
        </div>
      </div>
    </div>
  );
};

export default Question;
