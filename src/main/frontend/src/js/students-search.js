// Use lodash-es' debounce so the students list filters as the user types,
// without firing a request on every keystroke.
import debounce from "lodash-es/debounce";

export function initStudentsLiveSearch() {
  // Only the /students list page has this form.
  const form = document.querySelector('form[action="/students"]');
  if (!form) return;
  const nameInput = form.querySelector('input[name="nameContains"]');
  if (!nameInput) return;

  // 400ms after the user stops typing, auto-submit the filter form.
  const submit = debounce(() => form.submit(), 400);
  nameInput.addEventListener("input", submit);
}
