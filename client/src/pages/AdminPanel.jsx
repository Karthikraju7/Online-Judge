import React, { useState, useEffect } from "react";
import toast from "react-hot-toast";

const initialForm = {
  title: "",
  slug: "",
  difficulty: "Easy",
  description: "",
  sampleTestCasesJson: [
    { input: "", expectedOutput: "" }
  ],
  hiddenTestCases: [
    { input: "", expectedOutput: "" }
  ]
};

const AdminPanel = () => {
  const [problems, setProblems] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(initialForm);
  const [isEditMode, setIsEditMode] = useState(false);

  const fetchProblems = async () => {
    try {
      const res = await fetch("http://localhost:8080/problems/admin/all");
      const data = await res.json();
      setProblems(data);
    } catch (err) {
      console.error("Error fetching problems:", err);
    }
  };

  useEffect(() => {
    fetchProblems();
  }, []);

  const handleChange = (e, field, index = null) => {
    if (field === "sampleTestCasesJson" || field === "hiddenTestCases") {
      const updated = [...form[field]];
      updated[index][e.target.name] = e.target.value;
      setForm({ ...form, [field]: updated });
    } else {
      setForm({ ...form, [field]: e.target.value });
    }
  };

  const openAddForm = () => {
    setForm(initialForm);
    setIsEditMode(false);
    setShowForm(true);
  };

  const openEditForm = async (slug) => {
  try {
    const res = await fetch(`http://localhost:8080/problems/${slug}`);
    const data = await res.json();

    const filledForm = {
      title: data.title || "",
      slug: data.slug || "",
      difficulty: data.difficulty || "Easy",
      description: data.description || "",
      sampleTestCasesJson: [
        {
          input: data.sampleInput || "",
          expectedOutput: data.sampleOutput || ""
        }
      ],
      hiddenTestCases: Array.isArray(data.hiddenTestCases)
        ? data.hiddenTestCases
        : []
    };

    setForm(filledForm);
    setIsEditMode(true);
    setShowForm(true);
  } catch (err) {
    toast.error("Error loading problem.");
  }
};

  const convertToBackendPayload = () => {
    const { sampleTestCasesJson } = form;
    const { input: sampleInput, expectedOutput: sampleOutput } = sampleTestCasesJson[0] || {};

    return {
      title: form.title,
      slug: form.slug,
      difficulty: form.difficulty,
      description: form.description,
      sampleInput,
      sampleOutput,
      hiddenTestCases: form.hiddenTestCases
    };
  };

  const handleSubmit = async () => {
    const method = isEditMode ? "PUT" : "POST";
    const endpoint = isEditMode
      ? `http://localhost:8080/problems/${form.slug}`
      : "http://localhost:8080/problems/add-full";

    const payload = convertToBackendPayload();

    try {
      const res = await fetch(endpoint, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        toast.success(isEditMode ? "Problem updated" : "Problem added");
        setShowForm(false);
        fetchProblems();
      } else {
        toast.error("Server error");
      }
    } catch (err) {
      console.error(err);
      toast.error("Request failed");
    }
  };

  return (
    <div className="min-h-screen bg-black text-white p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Admin Problem Panel</h1>
        <button className="bg-green-600 px-4 py-2 rounded cursor-pointer" onClick={openAddForm}>
          + Add Problem
        </button>
      </div>

      <div className="grid grid-cols-4 gap-4 font-semibold text-sm border-b pb-2 border-gray-700 mb-2">
        <div>Title</div>
        <div>Slug</div>
        <div>Difficulty</div>
        <div>Action</div>
      </div>

      {problems.map((p, i) => (
        <div
          key={i}
          className="grid grid-cols-4 gap-4 text-sm py-2 border-b border-gray-800"
        >
          <div>{p.title}</div>
          <div>{p.slug}</div>
          <div>{p.difficulty}</div>
          <div>
            <button
              className="text-blue-400 hover:underline cursor-pointer"
              onClick={() => openEditForm(p.slug)}
            >
              Edit
            </button>
          </div>
        </div>
      ))}

      {showForm && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50">
          <div className="bg-gray-900 p-6 rounded w-[700px] max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">
              {isEditMode ? "Edit Problem" : "Add Problem"}
            </h2>

            <div className="space-y-3">
              <input
                type="text"
                placeholder="Title"
                value={form.title}
                onChange={(e) => handleChange(e, "title")}
                className="w-full p-2 bg-gray-800 rounded"
              />
              <input
                type="text"
                placeholder="Slug"
                value={form.slug}
                onChange={(e) => handleChange(e, "slug")}
                className="w-full p-2 bg-gray-800 rounded"
                disabled={isEditMode}
              />
              <select
                value={form.difficulty}
                onChange={(e) => handleChange(e, "difficulty")}
                className="w-full p-2 bg-gray-800 rounded cursor-pointer"
              >
                <option>Easy</option>
                <option>Medium</option>
                <option>Hard</option>
              </select>
              <textarea
                placeholder="Description"
                value={form.description}
                onChange={(e) => handleChange(e, "description")}
                className="w-full p-2 bg-gray-800 rounded h-32"
              />

              {/* ✅ Sample Test Cases */}
              <div>
                <h4 className="font-bold mb-2">Sample Test Case</h4>
                {form.sampleTestCasesJson.map((test, idx) => (
                  <div key={idx} className="mb-2">
                    <input
                      name="input"
                      value={test.input}
                      placeholder="Input"
                      onChange={(e) => handleChange(e, "sampleTestCasesJson", idx)}
                      className="w-full p-2 bg-gray-800 rounded mb-1"
                    />
                    <input
                      name="expectedOutput"
                      value={test.expectedOutput}
                      placeholder="Expected Output"
                      onChange={(e) => handleChange(e, "sampleTestCasesJson", idx)}
                      className="w-full p-2 bg-gray-800 rounded"
                    />
                  </div>
                ))}
              </div>

              {/* ✅ Hidden Test Cases */}
              <div>
                <h4 className="font-bold mb-2">Hidden Test Cases</h4>
                {form.hiddenTestCases.map((test, idx) => (
                  <div key={idx} className="mb-2">
                    <input
                      name="input"
                      value={test.input}
                      placeholder="Hidden Input"
                      onChange={(e) => handleChange(e, "hiddenTestCases", idx)}
                      className="w-full p-2 bg-gray-700 rounded mb-1"
                    />
                    <input
                      name="expectedOutput"
                      value={test.expectedOutput}
                      placeholder="Hidden Expected Output"
                      onChange={(e) => handleChange(e, "hiddenTestCases", idx)}
                      className="w-full p-2 bg-gray-700 rounded"
                    />
                  </div>
                ))}
                <button
                  onClick={() =>
                    setForm({
                      ...form,
                      hiddenTestCases: [...form.hiddenTestCases, { input: "", expectedOutput: "" }]
                    })
                  }
                  className="text-green-400 mt-1 text-sm cursor-pointer"
                >
                  + Add Hidden Test Case
                </button>
              </div>
            </div>

            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setShowForm(false)}
                className="px-4 py-2 bg-gray-700 rounded cursor-pointer"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                className="px-4 py-2 bg-blue-600 rounded cursor-pointer"
              >
                {isEditMode ? "Update" : "Add"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminPanel;
