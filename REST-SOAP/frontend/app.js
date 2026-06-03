const apiBaseUrlInput = document.querySelector("#apiBaseUrl");
const tokenInput = document.querySelector("#token");
const loadGamesBtn = document.querySelector("#loadGamesBtn");
const searchForm = document.querySelector("#searchForm");
const searchInput = document.querySelector("#searchInput");
const gameForm = document.querySelector("#gameForm");
const clearFormBtn = document.querySelector("#clearFormBtn");
const gamesContainer = document.querySelector("#games");
const statusBox = document.querySelector("#status");

function getApiBaseUrl() {
  return apiBaseUrlInput.value.replace(/\/$/, "");
}

function getToken() {
  return tokenInput.value.trim();
}

function setStatus(message) {
  statusBox.textContent = message;
}

function authHeaders() {
  const token = getToken();

  if (!token) {
    return {};
  }

  return {
    Authorization: `Bearer ${token}`,
  };
}

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...(options.headers || {}),
    },
  });

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const message = data?.message || `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return data;
}

function getPagedItems(data) {
  if (!data?._embedded) {
    return [];
  }

  const embeddedValues = Object.values(data._embedded);
  return embeddedValues[0] || [];
}

function getFormData() {
  return {
    name: valueOrNull("#name"),
    slug: valueOrNull("#slug"),
    yearPublished: numberOrNull("#yearPublished"),
    bggRating: numberOrNull("#bggRating"),
    difficultyRating: numberOrNull("#difficultyRating"),
    description: valueOrNull("#description"),
    playingTime: numberOrNull("#playingTime"),
    available: document.querySelector("#available").checked,
    minPlayers: numberOrNull("#minPlayers"),
    maxPlayers: numberOrNull("#maxPlayers"),
    minimumAge: numberOrNull("#minimumAge"),
    thumbnail: valueOrNull("#thumbnail"),
    image: valueOrNull("#image"),
  };
}

function valueOrNull(selector) {
  const value = document.querySelector(selector).value.trim();
  return value === "" ? null : value;
}

function numberOrNull(selector) {
  const value = document.querySelector(selector).value.trim();
  return value === "" ? null : Number(value);
}

function clearForm() {
  gameForm.reset();
  document.querySelector("#gameId").value = "";
  document.querySelector("#available").checked = true;
}

function fillForm(game) {
  document.querySelector("#gameId").value = game.id ?? "";
  document.querySelector("#name").value = game.name ?? "";
  document.querySelector("#slug").value = game.slug ?? "";
  document.querySelector("#yearPublished").value = game.yearPublished ?? "";
  document.querySelector("#bggRating").value = game.bggRating ?? "";
  document.querySelector("#difficultyRating").value = game.difficultyRating ?? "";
  document.querySelector("#description").value = game.description ?? "";
  document.querySelector("#playingTime").value = game.playingTime ?? "";
  document.querySelector("#available").checked = Boolean(game.available);
  document.querySelector("#minPlayers").value = game.minPlayers ?? "";
  document.querySelector("#maxPlayers").value = game.maxPlayers ?? "";
  document.querySelector("#minimumAge").value = game.minimumAge ?? "";
  document.querySelector("#thumbnail").value = game.thumbnail ?? "";
  document.querySelector("#image").value = game.image ?? "";

  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadGames(search = "") {
  try {
    const url = new URL(getApiBaseUrl());

    if (search) {
      url.searchParams.set("search", search);
    }

    const data = await apiFetch(url.toString());
    const games = getPagedItems(data);

    renderGames(games);
    setStatus(`Loaded ${games.length} games.`);
  } catch (error) {
    setStatus(error.message);
  }
}

async function loadSingleGame(selfHref) {
  try {
    const game = await apiFetch(selfHref);
    fillForm(game);
    setStatus(`Loaded game ${game.id} into form.`);
  } catch (error) {
    setStatus(error.message);
  }
}

async function saveGame(event) {
  event.preventDefault();

  try {
    const id = document.querySelector("#gameId").value;
    const body = getFormData();

    const url = id ? `${getApiBaseUrl()}/${id}` : getApiBaseUrl();
    const method = id ? "PUT" : "POST";

    await apiFetch(url, {
      method,
      body: JSON.stringify(body),
    });

    clearForm();
    await loadGames();

    setStatus(id ? "Game updated." : "Game created.");
  } catch (error) {
    setStatus(error.message);
  }
}

async function deleteGame(game) {
  const deleteHref = game._links?.delete?.href;

  if (!deleteHref) {
    setStatus("No delete link available. Are you authenticated?");
    return;
  }

  const confirmed = confirm(`Delete "${game.name}"?`);

  if (!confirmed) {
    return;
  }

  try {
    await apiFetch(deleteHref, {
      method: "DELETE",
    });

    await loadGames();
    setStatus("Game deleted.");
  } catch (error) {
    setStatus(error.message);
  }
}

function renderGames(games) {
  gamesContainer.innerHTML = "";

  if (games.length === 0) {
    gamesContainer.textContent = "No games found.";
    return;
  }

  for (const game of games) {
    const card = document.createElement("article");
    card.className = "game-card";

    const imageUrl = game.thumbnail || game.image;

    card.innerHTML = `
      ${imageUrl ? `<img src="${escapeHtml(imageUrl)}" alt="">` : ""}
      <h3>${escapeHtml(game.name || "Unnamed game")}</h3>
      <p><strong>ID:</strong> ${game.id}</p>
      <p><strong>Year:</strong> ${game.yearPublished ?? "Unknown"}</p>
      <p><strong>Players:</strong> ${game.minPlayers ?? "?"} - ${game.maxPlayers ?? "?"}</p>
      <p><strong>Playing time:</strong> ${game.playingTime ?? "?"} minutes</p>
      <p><strong>Available:</strong> ${game.available ? "Yes" : "No"}</p>
      <p>${escapeHtml(game.description || "")}</p>
      <div class="game-actions"></div>
    `;

    const actions = card.querySelector(".game-actions");

    const viewButton = document.createElement("button");
    viewButton.textContent = "View / edit";
    viewButton.className = "secondary";
    viewButton.addEventListener("click", () => {
      const selfHref = game._links?.self?.href;

      if (selfHref) {
        loadSingleGame(selfHref);
      } else {
        fillForm(game);
      }
    });

    actions.appendChild(viewButton);

    if (game._links?.delete?.href) {
      const deleteButton = document.createElement("button");
      deleteButton.textContent = "Delete";
      deleteButton.className = "danger";
      deleteButton.addEventListener("click", () => deleteGame(game));
      actions.appendChild(deleteButton);
    }

    gamesContainer.appendChild(card);
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

loadGamesBtn.addEventListener("click", () => loadGames());

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  loadGames(searchInput.value.trim());
});

gameForm.addEventListener("submit", saveGame);
clearFormBtn.addEventListener("click", clearForm);

loadGames();
