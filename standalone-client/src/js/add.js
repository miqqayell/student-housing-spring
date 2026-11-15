import { API_BASE_URL } from "./api.js";

// Create a housing unit. Uses async/await (no .then()).
// REST: POST to the collection, JSON body, Content-Type + Accept negotiation.
async function createUnit(unit) {
  return await fetch(`${API_BASE_URL}/api/units`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: JSON.stringify(unit),
  });
}

export function initAdd() {
  const form = document.getElementById("add-form");
  const message = document.getElementById("add-message");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    message.innerHTML = "";

    const unit = {
      address: document.getElementById("address").value.trim(),
      unitType: document.getElementById("unitType").value.trim(),
      capacity: Number(document.getElementById("capacity").value),
      rent: Number(document.getElementById("rent").value),
      furnished: document.getElementById("furnished").checked,
    };

    try {
      const response = await createUnit(unit);

      if (response.ok) {
        const created = await response.json();
        // Added successfully -> confirm and clear the form.
        form.reset();
        message.innerHTML =
          `<div class="alert alert-success mb-3">Unit added successfully (id ${created.id}).</div>`;
      } else {
        // Not added -> leave the user's input in the form for resubmission.
        message.innerHTML =
          `<div class="alert alert-danger mb-3">Could not add the unit (HTTP ${response.status}). Please review your input and try again.</div>`;
      }
    } catch (error) {
      // Network/CORS error -> keep the input, inform the user.
      message.innerHTML =
        `<div class="alert alert-danger mb-3">Network error: ${error.message}. Your input was kept.</div>`;
    }
  });
}
