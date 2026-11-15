// Use dayjs to render the birth date in a human-friendly format on the
// student detail page. The Thymeleaf template renders the raw ISO value into
// a data-birthdate attribute; we replace the visible text on the client.
import dayjs from "dayjs";

export function initStudentDetailDateFormat() {
  const el = document.querySelector("[data-birthdate]");
  if (!el) return;

  const value = el.dataset.birthdate;
  const parsed = dayjs(value);
  if (parsed.isValid()) {
    el.textContent = parsed.format("MMMM D, YYYY");
  }
}
