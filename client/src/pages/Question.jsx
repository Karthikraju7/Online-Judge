import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import { useAuth } from "../context/AuthContext";
import {
  Panel,
  PanelGroup,
  PanelResizeHandle,
} from "react-resizable-panels";

const Question = () => {
  const { slug } = useParams();
  const [problem, setProblem] = useState(null);
  const [selectedLang, setSelectedLang] = useState("cpp");
  const [code, setCode] = useState("");
  const [output, setOutput] = useState("");
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { authUser, authFetch } = useAuth(); // ✅ Use authFetch

  const defaultTemplates = {
    cpp: `#include <bits/stdc++.h>
using namespace std;

int main() {
    // Hey, Write here
    return 0;
}`,
    java: `import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Hey, Write here
    }
}`,
    python: `# Hey, Write here
print("Hello World")`
  };

  useEffect(() => {
    const fetchProblem = async () => {
      try {
        const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/${slug}`); // ✅ JWT included
        const data = await res.json();
        setProblem(data);

        if (data.sampleInput) {
          setInput(data.sampleInput);
        }
      } catch (err) {
        console.error("Failed to fetch problem:", err);
      }
    };

    fetchProblem();
  }, [slug, authFetch]);

  // Load saved code or default template
  useEffect(() => {
    const savedCode = localStorage.getItem(
      `code_${authUser?.email}_${slug}_${selectedLang}`
    );
    setCode(savedCode || defaultTemplates[selectedLang]);
  }, [selectedLang, authUser, slug]);

  const handleRun = async () => {
    setIsLoading(true);
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/run`, {  // ✅ JWT
        method: "POST",
        body: JSON.stringify({
          language: selectedLang,
          code,
          input,
          slug,
        }),
      });

      const data = await res.json();
      if (data.verdict === "✅ Correct") {
        setOutput("✅ Success");
      } else {
        setOutput(
          `❌ Failed\nYour Output:\n${data.output}\nExpected Output:\n${problem.sampleOutput}`
        );
      }
    } catch (err) {
      console.error("Run failed:", err);
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async () => {
    setIsLoading(true);
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/submit`, { // ✅ JWT
        method: "POST",
        body: JSON.stringify({
          language: selectedLang,
          code,
          slug,
          email: authUser?.email,
        }),
      });

      const data = await res.json();
      setOutput(`🧠 Verdict: ${data.verdict}`);
    } catch (err) {
      console.error("Submit failed:", err);
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
    }
  };

  if (!problem) return <div className="text-white p-4">Loading problem...</div>;

  return (
    <PanelGroup
      direction="horizontal"
      className="h-[calc(100vh-64px-48px)] text-white"
    >
      {/* Problem Panel */}
      <Panel defaultSize={50} minSize={20}>
        <div className="w-full h-full p-6 overflow-y-auto border-r border-gray-700">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold">{problem.title}</h2>
            <span
              className={`font-semibold ${
                problem.difficulty === "Easy"
                  ? "text-green-400"
                  : problem.difficulty === "Medium"
                  ? "text-yellow-400"
                  : "text-red-400"
              }`}
            >
              {problem.difficulty}
            </span>
          </div>

          <pre className="whitespace-pre-wrap text-m mb-6">
            {problem.description}
          </pre>

          <div className="mt-4">
            <h3 className="text-lg font-semibold mb-2">Sample Test Case</h3>
            <div className="mb-4 text-sm bg-black/30 p-3 rounded border border-white/10">
              <div>
                <strong>Input:</strong>
                <pre className="whitespace-pre-wrap">{problem.sampleInput}</pre>
              </div>
              <div>
                <strong>Expected Output:</strong> {problem.sampleOutput}
              </div>
            </div>
          </div>
        </div>
      </Panel>

      <PanelResizeHandle className="w-2 bg-gray-700 cursor-col-resize" />

      {/* Editor + Output */}
      <Panel defaultSize={50} minSize={20}>
        <PanelGroup
          direction="vertical"
          className="w-full h-full p-6"
          id="code-output-group"
        >
          {/* Editor */}
          <Panel defaultSize={90} minSize={30}>
            <div className="flex flex-col h-full">
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
                  className="bg-gray-800 text-white p-2 rounded cursor-pointer ml-1"
                  value={selectedLang}
                  onChange={(e) => setSelectedLang(e.target.value)}
                >
                  <option value="cpp">C++</option>
                  <option value="java">Java</option>
                  <option value="python">Python</option>
                </select>
              </div>

              <div className="flex-1 border border-gray-700 rounded overflow-hidden">
                <Editor
                  height="100%"
                  width="100%"
                  language={
                    ["cpp", "java", "python"].includes(selectedLang)
                      ? selectedLang
                      : "cpp"
                  }
                  value={code}
                  onChange={(newCode) => {
                    setCode(newCode);
                    localStorage.setItem(
                      `code_${authUser?.email}_${slug}_${selectedLang}`,
                      newCode
                    );
                  }}
                  theme="vs-dark"
                  options={{
                    fontSize: 14,
                    fontFamily: "Fira Code, monospace",
                    fontLigatures: true,
                    minimap: { enabled: false },
                    wordWrap: "on",
                    tabSize: 4,
                    automaticLayout: true,
                    lineNumbers: "on",
                  }}
                />
              </div>

              {isLoading && (
                <div className="text-yellow-300 mt-2">⏳ Running Code...</div>
              )}
            </div>
          </Panel>

          <PanelResizeHandle className="h-2 bg-gray-700 cursor-row-resize" />

          {/* Output */}
          <Panel id="output-panel" defaultSize={10} minSize={10}>
            <div className="text-sm bg-black/40 p-3 rounded border border-white/10 h-full overflow-auto">
              <strong>Output:</strong>
              <pre className="text-gray-300 whitespace-pre-wrap break-words">
                {output}
              </pre>
            </div>
          </Panel>
        </PanelGroup>
      </Panel>
    </PanelGroup>
  );
};

export default Question;
