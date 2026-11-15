import { API_BASE_URL } from "./api.js";

// Escape user/data text before injecting into the DOM.
function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

// Fetch matching students. Uses async/await (no .then()).
// REST: GET collection with a query parameter, Accept: application/json.
async function searchStudents(name) {
  const url = `${API_BASE_URL}/api/students?name=${encodeURIComponent(name)}`;
  const response = await fetch(url, {
    headers: { Accept: "application/json" },
  });

  if (!response.ok) {
    throw new Error(`Search failed (HTTP ${response.status}).`);
  }

  return await response.json();
}

function renderResults(students) {
  const container = document.getElementById("search-results");

  // No results -> tell the user.
  if (students.length === 0) {
    container.innerHTML = `<p class="text-muted mb-0">No records were found.</p>`;
    return;
  }

  // At least one record -> show them in a table.
  const rows = students
    .map((student) => `
      <tr>
        <td>${escapeHtml(student.id)}</td>
        <td>${escapeHtml(student.name)}</td>
        <td>${escapeHtml(student.yearOfStudy)}</td>
        <td>${student.international ? "Yes" : "No"}</td>
      </tr>`)
    .join("");

  container.innerHTML = `
    <table class="table table-striped result-table align-middle">
      <thead>
        <tr><th>ID</th><th>Name</th><th>Year</th><th>International</th></tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>`;
}

export function initSearch() {
  const form = document.getElementById("search-form");
  const input = document.getElementById("search-input");
  const container = document.getElementById("search-results");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const students = await searchStudents(input.value.trim());
      renderResults(students);
    } catch (error) {
      container.innerHTML = `<div class="alert alert-danger mb-0">${escapeHtml(error.message)}</div>`;
    }
  });
}
