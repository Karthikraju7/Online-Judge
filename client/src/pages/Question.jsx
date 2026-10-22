import React, { useState, useEffect, useRef } from "react";
import { useParams } from "react-router-dom";
import Editor from "@monaco-editor/react";
import { useAuth } from "../context/AuthContext";
import { Dialog } from "@headlessui/react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";

const Question = () => {
  const { slug } = useParams();
  const { authUser, authFetch } = useAuth();
  const [problem, setProblem] = useState(null);
  const [selectedLang, setSelectedLang] = useState("cpp");
  const [codeMap, setCodeMap] = useState({ cpp: "", java: "", python: "" });
  const [output, setOutput] = useState("");
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [editorRefs, setEditorRefs] = useState({ cpp: null, java: null, python: null });
  const [isResetModalOpen, setIsResetModalOpen] = useState(false);
  const [time, setTime] = useState(null);
  const [memory, setMemory] = useState(null);
  const saveTimeoutRef = useRef(null);
  const [verdict, setVerdict] = useState("");
  const [aiSuggestion, setAiSuggestion] = useState("");
  const [aiLoading, setAiLoading] = useState(false);
  const [loadingType, setLoadingType] = useState(null); // "run" | "submit"
  const [isSubmitMode, setIsSubmitMode] = useState(false); // hides custom input after submit

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
        const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/${slug}`);
        const data = await res.json();
        setProblem(data);
        if (data.sampleInput) setInput(data.sampleInput.replace(/\\n/g, "\n"));
      } catch (err) {
        console.error("Failed to fetch problem:", err);
      }
    };
    fetchProblem();
  }, [slug, authFetch]);

  useEffect(() => {
    if (!authUser) return;
    const fetchSavedCode = async () => {
      const updatedMap = { ...codeMap };
      for (const lang of ["cpp", "java", "python"]) {
        const local = localStorage.getItem(`code_${authUser.email}_${slug}_${lang}`);
        if (local) { updatedMap[lang] = local; continue; }
        try {
          const res = await authFetch(`${import.meta.env.VITE_API_URL}/code?slug=${slug}&language=${lang}`);
          const backendCode = await res.text();
          updatedMap[lang] = backendCode || defaultTemplates[lang];
        } catch {
          updatedMap[lang] = defaultTemplates[lang];
        }
      }
      setCodeMap(updatedMap);
    };
    fetchSavedCode();
  }, [authUser, slug]);

  useEffect(() => {
    if (!authUser) return;
    if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
    saveTimeoutRef.current = setTimeout(() => {
      const code = codeMap[selectedLang];
      if (!code) return;
      localStorage.setItem(`code_${authUser.email}_${slug}_${selectedLang}`, code);
      authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ slug, language: selectedLang, code: code.replace(/\r\n/g, "\n") }),
      }).catch(console.error);
    }, 3000);
    return () => clearTimeout(saveTimeoutRef.current);
  }, [codeMap[selectedLang]]);

  const handleRun = async () => {
    const code = codeMap[selectedLang];
    if (!code.trim()) return setOutput("❌ Code is empty");
    const normalizedInput = input.replace(/\\n/g, "\n");
    setIsLoading(true);
    setLoadingType("run");
    setIsSubmitMode(false);
    setOutput("");
    setVerdict("");
    setTime(null);
    setMemory(null);
    authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ slug, language: selectedLang, code }),
    });
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/run`, {
        method: "POST",
        body: JSON.stringify({ language: selectedLang, code, input: normalizedInput, slug }),
      });
      const data = await res.json();
      setOutput(data.output || "");
      setTime(data.timeUsed || null);
      setMemory(data.memoryUsed || null);
      setVerdict(data.verdict || "");
    } catch {
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
      setLoadingType(null);
    }
  };

  const handleSubmit = async () => {
    const code = codeMap[selectedLang];
    if (!code.trim()) return setOutput("❌ Code is empty");
    setIsLoading(true);
    setLoadingType("submit");
    setIsSubmitMode(true);
    setOutput("");
    setVerdict("");
    setTime(null);
    setMemory(null);
    authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ slug, language: selectedLang, code }),
    });
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/submit`, {
        method: "POST",
        body: JSON.stringify({ language: selectedLang, code, slug, email: authUser?.email }),
      });
      const data = await res.json();
      setOutput(data.output || (data.passed !== undefined ? `Passed ${data.passed}/${data.total} test cases` : ""));
      setTime(data.timeUsed || null);
      setMemory(data.memoryUsed || null);
      setVerdict(data.verdict || "");
    } catch {
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
      setLoadingType(null);
    }
  };

  const confirmReset = () => {
    const updated = { ...codeMap, [selectedLang]: defaultTemplates[selectedLang] };
    setCodeMap(updated);
    localStorage.setItem(`code_${authUser?.email}_${slug}_${selectedLang}`, defaultTemplates[selectedLang]);
    const ref = editorRefs[selectedLang];
    if (ref) {
      ref.setValue(defaultTemplates[selectedLang]);
      ref.getModel()?.pushStackElement();
      ref.getModel()?.pushStackElement();
    }
    authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ slug, language: selectedLang, code: defaultTemplates[selectedLang] }),
    });
    setOutput("🗑️ Code reset to default.");
    setIsResetModalOpen(false);
  };

  const handleEditorMount = (lang, editor) => {
    setEditorRefs((prev) => ({ ...prev, [lang]: editor }));
  };

  const handleCodeChange = (lang, newCode) => {
    setCodeMap((prev) => ({ ...prev, [lang]: newCode }));
    localStorage.setItem(`code_${authUser?.email}_${slug}_${lang}`, newCode);
  };

  const isAccepted = verdict.includes("Accepted") || verdict.includes("Correct");
  const isError = verdict.includes("Error") || verdict.includes("Wrong") || verdict.includes("Exceeded");

  const difficultyStyle = {
    Easy: "bg-green-500/20 text-green-400 border border-green-500/30",
    Medium: "bg-yellow-500/20 text-yellow-400 border border-yellow-500/30",
    Hard: "bg-red-500/20 text-red-400 border border-red-500/30",
  };

  if (!problem) return (
    <div className="flex items-center justify-center h-screen bg-gray-900">
      <div className="text-gray-400 text-lg animate-pulse">Loading problem...</div>
    </div>
  );

  return (
    <>
      {/* Reset Modal */}
      <Dialog open={isResetModalOpen} onClose={() => setIsResetModalOpen(false)} className="fixed z-50 inset-0 flex items-center justify-center">
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" />
        <Dialog.Panel className="bg-gray-800 p-6 rounded-xl shadow-2xl w-[90%] max-w-md text-white border border-gray-700 z-50 relative">
          <Dialog.Title className="text-lg font-semibold mb-2">Reset Code</Dialog.Title>
          <p className="text-gray-400 text-sm mb-5">This will reset your code to the default template. This action cannot be undone.</p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setIsResetModalOpen(false)} className="px-4 py-1.5 text-sm rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors cursor-pointer">
              Cancel
            </button>
            <button onClick={confirmReset} className="px-4 py-1.5 text-sm rounded-lg bg-red-600 hover:bg-red-500 transition-colors cursor-pointer">
              Reset
            </button>
          </div>
        </Dialog.Panel>
      </Dialog>

      <PanelGroup direction="horizontal" className="h-[calc(100vh-64px-48px)] text-white">

        {/* Left Panel — Problem */}
        <Panel defaultSize={50} minSize={20}>
          <div className="w-full h-full p-6 overflow-y-auto border-r border-gray-700/60">

            {/* Title + Difficulty */}
            <div className="flex justify-between items-start mb-4 gap-3">
              <h2 className="text-xl font-bold text-white leading-tight">{problem.title}</h2>
              <span className={`text-xs font-semibold px-2.5 py-1 rounded-full whitespace-nowrap ${difficultyStyle[problem.difficulty] || "text-gray-400"}`}>
                {problem.difficulty}
              </span>
            </div>

            {/* Description */}
            <pre className="whitespace-pre-wrap text-gray-300 text-sm leading-relaxed mb-6">{problem.description}</pre>

            {/* Sample Test Case */}
            <div className="mt-4">
              <h3 className="text-sm font-semibold text-gray-400 uppercase tracking-wider mb-3">Sample Test Case</h3>
              <div className="rounded-lg border border-gray-700 overflow-hidden text-sm">
                <div className="bg-gray-800/60 px-4 py-3 border-b border-gray-700">
                  <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Input</div>
                  <pre className="whitespace-pre-wrap text-gray-200 font-mono">
                    {problem.sampleInput.replace(/\\n/g, "\n")}
                  </pre>
                </div>
                <div className="bg-gray-800/40 px-4 py-3">
                  <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Expected Output</div>
                  <pre className="whitespace-pre-wrap text-gray-200 font-mono">{problem.sampleOutput}</pre>
                </div>
              </div>
            </div>
          </div>
        </Panel>

        <PanelResizeHandle className="w-1 bg-gray-700/60 hover:bg-blue-500/50 transition-colors cursor-col-resize" />

        {/* Right Panel — Editor + Output */}
        <Panel defaultSize={50} minSize={20}>
          <PanelGroup direction="vertical" className="w-full h-full">

            {/* Editor Panel */}
            <Panel defaultSize={70} minSize={30}>
              <div className="flex flex-col h-full p-4 pb-0">

                {/* Toolbar */}
                <div className="flex justify-between items-center mb-3">
                  {/* Language Selector */}
                  <select
                    className="bg-gray-800 text-white text-sm px-3 py-1.5 rounded-lg border border-gray-700 focus:outline-none focus:border-gray-500 cursor-pointer"
                    value={selectedLang}
                    onChange={(e) => setSelectedLang(e.target.value)}
                  >
                    <option value="cpp">C++</option>
                    <option value="java">Java</option>
                    <option value="python">Python</option>
                  </select>

                  {/* Action Buttons */}
                  <div className="flex gap-2">
                    <button
                      onClick={handleRun}
                      disabled={isLoading}
                      className="flex items-center gap-1.5 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed px-4 py-1.5 text-sm rounded-lg transition-colors cursor-pointer font-medium"
                    >
                      {isLoading && loadingType === "run" ? (
                        <><span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block" /> Running...</>
                      ) : "▶ Run"}
                    </button>
                    <button
                      onClick={handleSubmit}
                      disabled={isLoading}
                      className="flex items-center gap-1.5 bg-green-600 hover:bg-green-500 disabled:opacity-50 disabled:cursor-not-allowed px-4 py-1.5 text-sm rounded-lg transition-colors cursor-pointer font-medium"
                    >
                      {isLoading && loadingType === "submit" ? (
                        <><span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block" /> Submitting...</>
                      ) : "Submit"}
                    </button>
                    <button
                      onClick={() => setIsResetModalOpen(true)}
                      className="px-3 py-1.5 text-sm rounded-lg bg-gray-700 hover:bg-gray-600 transition-colors cursor-pointer text-gray-300"
                      title="Reset code"
                    >
                      ↺
                    </button>
                  </div>
                </div>

                {/* Monaco Editor */}
                <div className="flex-1 border border-gray-700 rounded-lg overflow-hidden">
                  {["cpp", "java", "python"].map((lang) => (
                    <div key={lang} style={{ display: selectedLang === lang ? "block" : "none", height: "100%" }}>
                      <Editor
                        height="100%"
                        width="100%"
                        language={lang}
                        value={codeMap[lang]}
                        onChange={(code) => handleCodeChange(lang, code)}
                        onMount={(editor) => handleEditorMount(lang, editor)}
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
                  ))}
                </div>
              </div>
            </Panel>

            <PanelResizeHandle className="h-1 bg-gray-700/60 hover:bg-blue-500/50 transition-colors cursor-row-resize mx-4" />

            {/* Output Panel */}
            <Panel defaultSize={30} minSize={15}>
              <div className="h-full p-4 overflow-auto flex flex-col gap-3">

                {/* Verdict Banner */}
                {verdict && (
                  <div className={`px-4 py-2.5 rounded-lg text-sm font-semibold flex items-center gap-2 ${
                    isAccepted
                      ? "bg-green-500/15 border border-green-500/30 text-green-400"
                      : isError
                      ? "bg-red-500/15 border border-red-500/30 text-red-400"
                      : "bg-yellow-500/15 border border-yellow-500/30 text-yellow-400"
                  }`}>
                    <span>{verdict}</span>
                    {(time || memory) && (
                      <span className="ml-auto flex gap-3 text-xs font-normal opacity-70">
                        {time && <span>⏱ {time}</span>}
                        {memory && memory !== "0KB" && <span>💾 {memory}</span>}
                      </span>
                    )}
                  </div>
                )}

                {/* Output + Input Row */}
                <div className="flex gap-3 flex-1 min-h-0">

                  {/* Output */}
                  <div className="flex flex-col flex-1 min-w-0">
                    <div className="text-xs text-gray-500 uppercase tracking-wider mb-1.5">Output</div>
                    <pre className={`flex-1 bg-gray-800/60 border rounded-lg p-3 text-sm font-mono overflow-auto whitespace-pre-wrap break-words ${
                      output
                        ? output.includes("❌") ? "border-red-500/30 text-red-300" : "border-gray-700 text-gray-200"
                        : "border-gray-700/50 text-gray-600"
                    }`}>
                      {output || "Run your code to see output here."}
                    </pre>
                  </div>

                  {/* Custom Input — hidden after submission */}
                  {!isSubmitMode && (
                    <div className="flex flex-col w-48 shrink-0">
                      <div className="text-xs text-gray-500 uppercase tracking-wider mb-1.5">Custom Input</div>
                      <textarea
                        className="flex-1 bg-gray-800/60 border border-gray-700 rounded-lg p-3 text-sm font-mono text-gray-200 resize-none focus:outline-none focus:border-gray-500"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        placeholder="Enter input..."
                        spellCheck={false}
                      />
                    </div>
                  )}
                </div>

                {/* AI Section */}
                {aiSuggestion && (
                  <div className="flex flex-col gap-1.5">
                    <div className="flex justify-between items-center">
                      <div className="text-xs text-gray-500 uppercase tracking-wider">AI Suggestion</div>
                      <button onClick={() => setAiSuggestion("")} className="text-gray-600 hover:text-gray-400 text-xs transition-colors cursor-pointer">
                        ✕ Dismiss
                      </button>
                    </div>
                    <pre className="bg-purple-900/20 border border-purple-500/20 text-purple-200 p-3 rounded-lg text-sm overflow-auto max-h-32 whitespace-pre-wrap break-words">
                      {aiSuggestion}
                    </pre>
                  </div>
                )}

                {/* AI Button */}
                {output && (
                  <div className="flex justify-start mt-auto">
                    <button
                      onClick={async () => {
                        try {
                          setAiLoading(true);
                          const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/ai/debug`, {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({
                              code: codeMap[selectedLang],
                              output,
                              problemDescription: problem.description,
                              language: selectedLang,
                            }),
                          });
                          const data = await res.json();
                          setAiSuggestion(data.response || "No response from AI");
                        } catch (err) {
                          setAiSuggestion("⚠️ Failed to get AI response");
                        } finally {
                          setAiLoading(false);
                        }
                      }}
                      disabled={aiLoading}
                      className="flex items-center gap-1.5 bg-purple-700 hover:bg-purple-600 disabled:opacity-60 disabled:cursor-not-allowed px-4 py-1.5 text-sm rounded-lg transition-colors cursor-pointer"
                    >
                      {aiLoading
                        ? <><span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block" /> Thinking...</>
                        : "🤖 Ask AI for Help"
                      }
                    </button>
                  </div>
                )}
              </div>
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </>
  );
};

export default Question;