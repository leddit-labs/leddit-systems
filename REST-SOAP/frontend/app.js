async function apiFetch(url, options = {}) {
  const token = document.querySelector("#token").value.trim();
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || `HTTP ${res.status}`);
  return data;
}

function baseUrl() {
  return document.querySelector("#apiBaseUrl").value.replace(/\/$/, "");
}

function status(msg) {
  document.querySelector("#status").textContent = msg;
}

function escapeHtml(v) {
  return String(v)
    .replaceAll("&", "&amp;").replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;").replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

// ── Form helpers ────────────────────────────────────────────────────────────

function getFormData() {
  const val = (id) => { const v = document.querySelector(id).value.trim(); return v || null; };
  const num = (id) => { const v = val(id); return v == null ? null : Number(v); };
  return {
    name:             val("#name"),
    slug:             val("#slug"),
    yearPublished:    num("#yearPublished"),
    bggRating:        num("#bggRating"),
    difficultyRating: num("#difficultyRating"),
    description:      val("#description"),
    playingTime:      num("#playingTime"),
    available:        document.querySelector("#available").checked,
    minPlayers:       num("#minPlayers"),
    maxPlayers:       num("#maxPlayers"),
    minimumAge:       num("#minimumAge"),
    thumbnail:        val("#thumbnail"),
    image:            val("#image"),
  };
}

function clearForm() {
  document.querySelector("#gameForm").reset();
  document.querySelector("#gameId").value = "";
  document.querySelector("#available").checked = true;
}

function fillForm(game) {
  const set = (id, v) => document.querySelector(id).value = v ?? "";
  set("#gameId",          game.id);
  set("#name",            game.name);
  set("#slug",            game.slug);
  set("#yearPublished",   game.yearPublished);
  set("#bggRating",       game.bggRating);
  set("#difficultyRating",game.difficultyRating);
  set("#description",     game.description);
  set("#playingTime",     game.playingTime);
  set("#minPlayers",      game.minPlayers);
  set("#maxPlayers",      game.maxPlayers);
  set("#minimumAge",      game.minimumAge);
  set("#thumbnail",       game.thumbnail);
  set("#image",           game.image);
  document.querySelector("#available").checked = Boolean(game.available);
  window.scrollTo({ top: 0, behavior: "smooth" });
}

// ── API actions ──────────────────────────────────────────────────────────────

async function loadGames(search = "") {
  try {
    const url = new URL(baseUrl());
    if (search) url.searchParams.set("search", search);
    const data = await apiFetch(url.toString());
    const embedded = data?._embedded ? Object.values(data._embedded)[0] ?? [] : [];
    renderGames(embedded);
    status(`Loaded ${embedded.length} games.`);
  } catch (e) { status(e.message); }
}

async function saveGame(e) {
  e.preventDefault();
  try {
    const id = document.querySelector("#gameId").value;
    await apiFetch(id ? `${baseUrl()}/${id}` : baseUrl(), {
      method: id ? "PUT" : "POST",
      body: JSON.stringify(getFormData()),
    });
    clearForm();
    await loadGames();
    status(id ? "Game updated." : "Game created.");
  } catch (e) { status(e.message); }
}

async function deleteGame(game) {
  if (!game._links?.delete?.href) { status("No delete link. Are you authenticated?"); return; }
  if (!confirm(`Delete "${game.name}"?`)) return;
  try {
    await apiFetch(game._links.delete.href, { method: "DELETE" });
    await loadGames();
    status("Game deleted.");
  } catch (e) { status(e.message); }
}

// ── Rendering ────────────────────────────────────────────────────────────────

function renderGames(games) {
  const container = document.querySelector("#games");
  container.innerHTML = "";

  if (!games.length) { container.textContent = "No games found."; return; }

  for (const game of games) {
    const card = document.createElement("article");
    card.className = "game-card";
    const img = game.thumbnail || game.image;
    card.innerHTML = `
      ${img ? `<img src="${escapeHtml(img)}" alt="">` : ""}
      <h3>${escapeHtml(game.name || "Unnamed game")}</h3>
      <p><strong>ID:</strong> ${game.id}</p>
      <p><strong>Year:</strong> ${game.yearPublished ?? "Unknown"}</p>
      <p><strong>Players:</strong> ${game.minPlayers ?? "?"} – ${game.maxPlayers ?? "?"}</p>
      <p><strong>Playing time:</strong> ${game.playingTime ?? "?"} min</p>
      <p><strong>Available:</strong> ${game.available ? "Yes" : "No"}</p>
      <p>${escapeHtml(game.description || "")}</p>
      <div class="game-actions"></div>
    `;

    const actions = card.querySelector(".game-actions");

    const editBtn = document.createElement("button");
    editBtn.textContent = "View / edit";
    editBtn.className = "secondary";
    editBtn.onclick = async () => {
      const href = game._links?.self?.href;
      if (href) {
        try { fillForm(await apiFetch(href)); status(`Loaded game ${game.id}.`); }
        catch (e) { status(e.message); }
      } else {
        fillForm(game);
      }
    };
    actions.appendChild(editBtn);

    if (game._links?.delete?.href) {
      const delBtn = document.createElement("button");
      delBtn.textContent = "Delete";
      delBtn.className = "danger";
      delBtn.onclick = () => deleteGame(game);
      actions.appendChild(delBtn);
    }

    container.appendChild(card);
  }
}

// ── Event listeners ──────────────────────────────────────────────────────────

document.querySelector("#loadGamesBtn").addEventListener("click", () => loadGames());
document.querySelector("#searchForm").addEventListener("submit", (e) => {
  e.preventDefault();
  loadGames(document.querySelector("#searchInput").value.trim());
});
document.querySelector("#gameForm").addEventListener("submit", saveGame);
document.querySelector("#clearFormBtn").addEventListener("click", clearForm);

loadGames();
