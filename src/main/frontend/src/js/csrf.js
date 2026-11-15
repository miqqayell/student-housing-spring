// Reusable CSRF helper. Reads the token + header name injected into the page
// <head> by Thymeleaf (see fragments/head.html) so any AJAX module can authorise
// state-changing requests against the Spring backend.
export function getCsrf() {
  const tokenEl = document.querySelector('meta[name="_csrf"]');
  const headerEl = document.querySelector('meta[name="_csrf_header"]');
  return {
    token: tokenEl ? tokenEl.getAttribute("content") : "",
    header: headerEl ? headerEl.getAttribute("content") : "X-CSRF-TOKEN",
  };
}
