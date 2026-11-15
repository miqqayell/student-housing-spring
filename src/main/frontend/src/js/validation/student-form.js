// Client-side validation for the Add Student form (POST /students/add).
// Powered by the 'validator' npm package; runs BEFORE the form submits so
// users get immediate feedback without a round trip to the server.
import validator from "validator";

const todayIso = () => new Date().toISOString().slice(0, 10);

const FIELDS = [
  {
    id: "name",
    validate: (v) => validator.isLength(v.trim(), { min: 2, max: 50 }),
    message: "Name must be between 2 and 50 characters.",
  },
  {
    id: "birthDate",
    validate: (v) => Boolean(v) && validator.isDate(v) && validator.isBefore(v, todayIso()),
    message: "Birth date is required and must be in the past.",
  },
  {
    id: "yearOfStudy",
    validate: (v) => validator.isInt(String(v), { min: 1, max: 6 }),
    message: "Year of study must be an integer between 1 and 6.",
  },
  {
    id: "gpa",
    // GPA is optional in the form; when provided it must be a float in [0.0, 4.0].
    validate: (v) => v === "" || v === null || validator.isFloat(String(v), { min: 0, max: 4 }),
    message: "GPA must be a number between 0.0 and 4.0.",
  },
];

function setError(input, message) {
  input.classList.add("is-invalid");
  let feedback = input.parentElement.querySelector(".invalid-feedback");
  if (!feedback) {
    feedback = document.createElement("div");
    feedback.className = "invalid-feedback";
    input.parentElement.appendChild(feedback);
  }
  feedback.textContent = message;
}

function clearError(input) {
  input.classList.remove("is-invalid");
}

export function initStudentFormValidation() {
  const form = document.querySelector('form[action="/students/add"]');
  if (!form) return;

  form.addEventListener("submit", (event) => {
    let firstInvalid = null;

    for (const field of FIELDS) {
      const input = form.querySelector(`#${field.id}`) || form.querySelector(`[name="${field.id}"]`);
      if (!input) continue;
      if (field.validate(input.value)) {
        clearError(input);
      } else {
        setError(input, field.message);
        firstInvalid = firstInvalid || input;
      }
    }

    if (firstInvalid) {
      event.preventDefault();
      firstInvalid.focus();
    }
  });
}
