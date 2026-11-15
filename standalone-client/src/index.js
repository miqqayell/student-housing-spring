// Entry point: load styles (Bootstrap + icons + custom SCSS) and wire up the features.
import "bootstrap-icons/font/bootstrap-icons.css";
import "./scss/main.scss";

import { initNavigation } from "./js/navigation.js";
import { initSearch } from "./js/search.js";
import { initAdd } from "./js/add.js";

document.addEventListener("DOMContentLoaded", () => {
  initNavigation();
  initSearch();
  initAdd();
});
