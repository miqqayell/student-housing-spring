// Migrated from the inline <script> previously in templates/students/list.html.
// Three AJAX helpers used by the students list page; the entry module exposes
// them on window so inline th:onclick handlers in the template keep working.
import { getCsrf } from "./csrf.js";

export async function quickViewAJAX(studentId) {
  try {
    const response = await fetch(`/api/students/${studentId}`, {
      method: "GET",
      headers: { Accept: "application/json" },
    });

    if (response.ok) {
      const student = await response.json();
      const displayGpa = student.gpa !== null ? student.gpa : "Hidden 🔒";
      alert(`QUICK VIEW\nName: ${student.name}\nGPA: ${displayGpa}\nYear: ${student.yearOfStudy}`);
    } else if (response.status === 404) {
      alert("Student not found in the database!");
    } else {
      alert("Server error occurred.");
    }
  } catch (error) {
    console.error("Fetch error:", error);
  }
}

export async function deleteStudentAJAX(studentId, buttonElement) {
  if (!confirm("Are you sure you want to delete this student?")) return;
  const { token, header } = getCsrf();

  try {
    const response = await fetch(`/api/students/${studentId}`, {
      method: "DELETE",
      headers: { Accept: "application/json", [header]: token },
    });

    if (response.status === 204) {
      const cardContainer = buttonElement.closest(".col");
      cardContainer.style.transition = "opacity 0.5s ease";
      cardContainer.style.opacity = "0";
      setTimeout(() => cardContainer.remove(), 500);
    } else if (response.status === 404) {
      alert("Error: Student already deleted or not found.");
    } else {
      alert("Security Error: You are not authorized to delete this student.");
    }
  } catch (error) {
    console.error("Delete error:", error);
  }
}

export async function updateGpaAJAX(studentId) {
  const input = document.getElementById(`gpa-input-${studentId}`);
  if (!input) return;
  const { token, header } = getCsrf();

  try {
    const response = await fetch(`/api/students/${studentId}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        [header]: token,
      },
      body: JSON.stringify({ gpa: parseFloat(input.value) }),
    });

    if (response.ok) {
      alert("GPA Updated Successfully!");
    } else if (response.status === 400) {
      alert("Validation Error: GPA must be between 0.0 and 4.0");
    } else {
      alert("Security Error: You do not have permission to edit this.");
    }
  } catch (error) {
    console.error("Update error:", error);
  }
}
