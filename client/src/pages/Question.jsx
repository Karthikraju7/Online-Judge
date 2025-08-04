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
  const saveTimeoutRef = useRef(null);

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

  // Fetch problem details
  useEffect(() => {
    const fetchProblem = async () => {
      try {
        const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/${slug}`);
        const data = await res.json();
        setProblem(data);
        if (data.sampleInput) setInput(data.sampleInput);
      } catch (err) {
        console.error("Failed to fetch problem:", err);
      }
    };

    fetchProblem();
  }, [slug, authFetch]);

  // Fetch code from backend or fallback to default/localStorage
  useEffect(() => {
  if (!authUser) return;

  const fetchSavedCode = async () => {
    const updatedMap = { ...codeMap };

    for (const lang of ["cpp", "java", "python"]) {
      const local = localStorage.getItem(`code_${authUser.email}_${slug}_${lang}`);

      if (local) {
        updatedMap[lang] = local;
        continue;
      }

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

  // Auto-save to backend + localStorage
  useEffect(() => {
    if (!authUser) return;

    if (saveTimeoutRef.current) clearTimeout(saveTimeoutRef.current);
    saveTimeoutRef.current = setTimeout(() => {
      const code = codeMap[selectedLang];
      if (!code) return;

      // Save to localStorage
      localStorage.setItem(`code_${authUser.email}_${slug}_${selectedLang}`, code);

      // Save to backend
      authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
        method: "POST",
        body: JSON.stringify({ slug, language: selectedLang, code }),
      });
    }, 3000);

    return () => clearTimeout(saveTimeoutRef.current);
  }, [codeMap[selectedLang]]);

  const handleRun = async () => {
    const code = codeMap[selectedLang];
    if (!code.trim()) return setOutput("❌ Code is empty");

    // Save to backend before run
    await authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      body: JSON.stringify({ slug, language: selectedLang, code }),
    });

    setIsLoading(true);
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/run`, {
        method: "POST",
        body: JSON.stringify({ language: selectedLang, code, input, slug }),
      });
      const data = await res.json();
      if (data.verdict === "✅ Correct") setOutput("✅ Success");
      else setOutput(`❌ Failed\nYour Output:\n${data.output}\nExpected Output:\n${problem.sampleOutput}`);
    } catch (err) {
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async () => {
    const code = codeMap[selectedLang];
    if (!code.trim()) return setOutput("❌ Code is empty");

    // Save before submit
    await authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      body: JSON.stringify({ slug, language: selectedLang, code }),
    });

    setIsLoading(true);
    try {
      const res = await authFetch(`${import.meta.env.VITE_API_URL}/problems/submit`, {
        method: "POST",
        body: JSON.stringify({ language: selectedLang, code, slug, email: authUser?.email }),
      });
      const data = await res.json();
      setOutput(`🧠 Verdict: ${data.verdict}`);
    } catch (err) {
      setOutput("❌ Server error");
    } finally {
      setIsLoading(false);
    }
  };

  const handleReset = () => {
    setIsResetModalOpen(true);
  };

  const confirmReset = () => {
    const updatedMap = { ...codeMap, [selectedLang]: defaultTemplates[selectedLang] };
    setCodeMap(updatedMap);
    localStorage.setItem(`code_${authUser?.email}_${slug}_${selectedLang}`, defaultTemplates[selectedLang]);

    const ref = editorRefs[selectedLang];
    if (ref) {
      ref.setValue(defaultTemplates[selectedLang]);
      ref.getModel()?.pushStackElement();
      ref.getModel()?.pushStackElement();
    }

    // Save reset to backend
    authFetch(`${import.meta.env.VITE_API_URL}/code/save`, {
      method: "POST",
      body: JSON.stringify({
        slug,
        language: selectedLang,
        code: defaultTemplates[selectedLang],
      }),
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

  if (!problem) return <div className="text-white p-4">Loading problem...</div>;

  return (
    <>
      <Dialog open={isResetModalOpen} onClose={() => setIsResetModalOpen(false)} className="fixed z-50 inset-0 flex items-center justify-center">
        <div className="fixed inset-0 bg-black bg-opacity-40" />
        <Dialog.Panel className="bg-gray-800 p-6 rounded-lg shadow-lg w-[90%] max-w-md text-white border border-gray-700 z-50 relative">
          <Dialog.Title className="text-xl font-bold mb-4">Reset Code</Dialog.Title>
          <p className="mb-4">Are you sure you want to reset your code to the default template?</p>
          <div className="flex justify-end gap-2">
            <button onClick={() => setIsResetModalOpen(false)} className="bg-gray-600 px-4 py-1 rounded hover:bg-gray-700 cursor-pointer">
              Cancel
            </button>
            <button onClick={confirmReset} className="bg-red-600 px-4 py-1 rounded hover:bg-red-700 cursor-pointer">
              Reset
            </button>
          </div>
        </Dialog.Panel>
      </Dialog>

      <PanelGroup direction="horizontal" className="h-[calc(100vh-64px-48px)] text-white">
        <Panel defaultSize={50} minSize={20}>
          <div className="w-full h-full p-6 overflow-y-auto border-r border-gray-700">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-2xl font-bold">{problem.title}</h2>
              <span className={`font-semibold ${problem.difficulty === "Easy" ? "text-green-400" : problem.difficulty === "Medium" ? "text-yellow-400" : "text-red-400"}`}>
                {problem.difficulty}
              </span>
            </div>

            <pre className="whitespace-pre-wrap text-m mb-6">{problem.description}</pre>

            <div className="mt-4">
              <h3 className="text-lg font-semibold mb-2">Sample Test Case</h3>
              <div className="mb-4 text-sm bg-black/30 p-3 rounded border border-white/10">
                <div><strong>Input:</strong><pre className="whitespace-pre-wrap">{problem.sampleInput}</pre></div>
                <div><strong>Expected Output:</strong> {problem.sampleOutput}</div>
              </div>
            </div>
          </div>
        </Panel>

        <PanelResizeHandle className="w-2 bg-gray-700 cursor-col-resize" />

        <Panel defaultSize={50} minSize={20}>
          <PanelGroup direction="vertical" className="w-full h-full p-6">
            <Panel defaultSize={90} minSize={30}>
              <div className="flex flex-col h-full">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-bold">CodeTheCode Here</h2>
                  <div className="flex gap-2">
                    <button onClick={handleRun} disabled={isLoading} className="bg-blue-600 px-4 py-1 rounded hover:bg-blue-700 cursor-pointer">Run</button>
                    <button onClick={handleSubmit} disabled={isLoading} className="bg-green-600 px-4 py-1 rounded hover:bg-green-700 cursor-pointer">Submit</button>
                    <button onClick={handleReset} className="bg-red-600 px-4 py-1 rounded hover:bg-red-700 cursor-pointer">Reset</button>
                  </div>
                </div>

                <div className="mb-2">
                  <select className="bg-gray-800 text-white p-2 rounded cursor-pointer ml-1" value={selectedLang} onChange={(e) => setSelectedLang(e.target.value)}>
                    <option value="cpp">C++</option>
                    <option value="java">Java</option>
                    <option value="python">Python</option>
                  </select>
                </div>

                <div className="flex-1 border border-gray-700 rounded overflow-hidden relative">
                  {["cpp", "java", "python"].map((lang) => (
                    <div key={lang} style={{ display: selectedLang === lang ? "block" : "none", height: "100%" }}>
                      <Editor
                        height="100%"
                        width="100%"
                        language={lang}
                        value={codeMap[lang]}
                        onChange={(newCode) => handleCodeChange(lang, newCode)}
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

                {isLoading && (
                  <div className="text-yellow-300 mt-2">⏳ Running Code...</div>
                )}
              </div>
            </Panel>

            <PanelResizeHandle className="h-2 bg-gray-700 cursor-row-resize" />

            <Panel defaultSize={10} minSize={10}>
              <div className="text-sm bg-black/40 p-3 rounded border border-white/10 h-full overflow-auto">
                <strong>Output:</strong>
                <pre className="text-gray-300 whitespace-pre-wrap break-words">{output}</pre>
              </div>
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </>
  );
};

export default Question;
