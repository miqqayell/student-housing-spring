// Hand-written single-page-application navigation.
// All sections live in one HTML page; we toggle them with element.style.display.
// (No Bootstrap JS / plugins are used.)

const SECTION_IDS = ["search-section", "add-section"];

function showSection(targetId) {
  for (const id of SECTION_IDS) {
    const section = document.getElementById(id);
    if (section) {
      section.style.display = id === targetId ? "block" : "none";
    }
  }

  // reflect the active item in the navbar
  const links = document.querySelectorAll(".nav-link[data-target]");
  for (const link of links) {
    link.classList.toggle("active", link.dataset.target === targetId);
  }
}

export function initNavigation() {
  const links = document.querySelectorAll(".nav-link[data-target]");
  for (const link of links) {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      showSection(link.dataset.target);
    });
  }

  // default view on load
  showSection("search-section");
}
