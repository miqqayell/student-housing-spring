// Entry point of the embedded frontend bundle.
// Bundled output -> src/main/resources/static/built/bundle.{js,css},
// loaded by templates/fragments/head.html.

// Styles: customized Bootstrap (SCSS), Bootstrap Icons (plain CSS), and our own SCSS.
import "bootstrap-icons/font/bootstrap-icons.css";
import "./scss/main.scss";

// Bootstrap's JS (dropdowns, collapse) — replaces the CDN bootstrap.bundle.min.js.
import "bootstrap";

// Reusable JS modules.
import { deleteStudentAJAX, quickViewAJAX, updateGpaAJAX } from "./js/students-actions.js";
import { initStudentsLiveSearch } from "./js/students-search.js";
import { initStudentDetailDateFormat } from "./js/student-detail.js";
import { initStudentFormValidation } from "./js/validation/student-form.js";

// Inline th:onclick handlers in templates call these by name; expose them on window
// so the existing markup keeps working while the implementation lives in modules.
window.quickViewAJAX = quickViewAJAX;
window.deleteStudentAJAX = deleteStudentAJAX;
window.updateGpaAJAX = updateGpaAJAX;

document.addEventListener("DOMContentLoaded", () => {
  initStudentsLiveSearch();
  initStudentDetailDateFormat();
  initStudentFormValidation();
});
