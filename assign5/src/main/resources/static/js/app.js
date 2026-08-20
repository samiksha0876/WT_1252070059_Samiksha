const API = "/api";

// ---------- Tab switching ----------
document.querySelectorAll(".tab-btn").forEach(btn => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".panel").forEach(p => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(btn.dataset.tab).classList.add("active");
    if (btn.dataset.tab === "marks") { loadStudentsIntoSelect("m_student"); loadSubjectsIntoSelect("m_subject"); }
    if (btn.dataset.tab === "result") { loadStudentsIntoSelect("r_student"); }
  });
});

function showMsg(elId, text, ok) {
  const el = document.getElementById(elId);
  el.textContent = text;
  el.className = "msg " + (ok ? "success" : "error");
}

// ---------- Students ----------
async function loadStudents() {
  const res = await fetch(`${API}/students`);
  const students = await res.json();
  const tbody = document.querySelector("#studentTable tbody");
  tbody.innerHTML = "";
  students.forEach(s => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${s.id}</td><td>${s.name}</td><td>${s.prn}</td><td>${s.branch}</td><td>${s.semester}</td>`;
    tbody.appendChild(tr);
  });
  return students;
}

async function loadStudentsIntoSelect(selectId) {
  const students = await loadStudents();
  const sel = document.getElementById(selectId);
  sel.innerHTML = students.map(s => `<option value="${s.id}">${s.name} (${s.prn})</option>`).join("");
}

document.getElementById("studentForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    name: document.getElementById("s_name").value,
    prn: document.getElementById("s_prn").value,
    branch: document.getElementById("s_branch").value,
    semester: parseInt(document.getElementById("s_semester").value)
  };
  const res = await fetch(`${API}/students`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (res.ok) {
    showMsg("studentMsg", "Student saved successfully.", true);
    e.target.reset();
    loadStudents();
  } else {
    showMsg("studentMsg", "Could not save student. Check the PRN is unique.", false);
  }
});

// ---------- Subjects ----------
async function loadSubjectsIntoSelect(selectId) {
  const res = await fetch(`${API}/subjects`);
  const subjects = await res.json();
  const sel = document.getElementById(selectId);
  sel.innerHTML = subjects.map(s => `<option value="${s.id}">${s.name} (${s.code})</option>`).join("");
}

// ---------- Marks ----------
document.getElementById("marksForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = {
    studentId: parseInt(document.getElementById("m_student").value),
    subjectId: parseInt(document.getElementById("m_subject").value),
    mseMarks: parseFloat(document.getElementById("m_mse").value),
    eseMarks: parseFloat(document.getElementById("m_ese").value)
  };
  const res = await fetch(`${API}/marks`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (res.ok) {
    showMsg("marksMsg", "Marks saved successfully.", true);
    e.target.reset();
  } else {
    showMsg("marksMsg", "Could not save marks.", false);
  }
});

// ---------- Result ----------
document.getElementById("resultForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const studentId = document.getElementById("r_student").value;
  const res = await fetch(`${API}/result/${studentId}`);
  const sheet = document.getElementById("marksheet");

  if (!res.ok) {
    const msg = await res.text();
    showMsg("resultMsg", msg || "Could not generate result.", false);
    sheet.classList.add("hidden");
    return;
  }

  const data = await res.json();
  showMsg("resultMsg", "", true);
  sheet.classList.remove("hidden");

  document.getElementById("rs_name").textContent = data.studentName;
  document.getElementById("rs_prn").textContent = data.prn;
  document.getElementById("rs_branch").textContent = data.branch;
  document.getElementById("rs_sem").textContent = data.semester;

  const tbody = document.querySelector("#marksheetTable tbody");
  tbody.innerHTML = "";
  data.subjects.forEach(sub => {
    const tr = document.createElement("tr");
    tr.innerHTML = `<td>${sub.subjectName}</td><td>${sub.subjectCode}</td>
      <td>${sub.mseMarks}</td><td>${sub.eseMarks}</td>
      <td>${sub.totalMarks}</td><td>${sub.grade}</td>`;
    tbody.appendChild(tr);
  });

  document.getElementById("rs_total").textContent = `${data.totalMarksObtained} / ${data.maxTotalMarks}`;
  document.getElementById("rs_pct").textContent = `${data.percentage}%`;
  document.getElementById("rs_grade").textContent = data.overallGrade;

  const statusEl = document.getElementById("rs_status");
  statusEl.textContent = data.resultStatus;
  statusEl.className = data.resultStatus === "PASS" ? "pass" : "fail";
});

// ---------- Init ----------
loadStudents();
