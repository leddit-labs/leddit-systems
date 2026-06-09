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
  console.log("Status:", msg);
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

// ── Keycloak Authentication ────────────────────────────────────────────────────

const KEYCLOAK_URL = "http://localhost:8180";
const REALM = "gameapi";
const CLIENT_ID = "gameapi-client";
const REDIRECT_URI = window.location.origin; // http://localhost:5500

console.log("Redirect URI:", REDIRECT_URI);

async function checkForOAuthCallback() {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get("code");

  console.log("Checking for OAuth callback, code present:", !!code);

  if (code) {
    status("Exchanging code for token...");

    try {
      const response = await fetch(`${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
          client_id: CLIENT_ID,
          client_secret: "dev-secret-2024",
          grant_type: "authorization_code",
          code: code,
          redirect_uri: REDIRECT_URI
        })
      });

      console.log("Token response status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Token exchange error:", errorText);
        throw new Error(`Token exchange failed: ${response.status}`);
      }

      const data = await response.json();
      console.log("Token received, expires in:", data.expires_in);

      document.querySelector("#token").value = data.access_token;
      localStorage.setItem("gameapi_token", data.access_token);

      // Update UI
      document.querySelector("#keycloakLoginBtn").style.display = "none";
      document.querySelector("#logoutBtn").style.display = "inline-block";
      status(`Logged in! Token expires in ${data.expires_in} seconds.`);

      // Remove code from URL
      window.history.replaceState({}, document.title, window.location.pathname);

      // Load games with new token
      await loadGames();
    } catch (e) {
      console.error("Login error:", e);
      status(`Login failed: ${e.message}`);
    }
  }
}

// Login with Keycloak
function loginWithKeycloak() {
  console.log("Login button clicked, redirecting to Keycloak...");
  const authUrl = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/auth` +
      `?client_id=${CLIENT_ID}` +
      `&response_type=code` +
      `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
      `&scope=openid%20email%20profile` +
      `&prompt=login`;

  console.log("Auth URL:", authUrl);
  window.location.href = authUrl;
}

// Logout
function logout() {
  console.log("Logout button clicked");

  // Clear token from UI and storage
  document.querySelector("#token").value = "";
  localStorage.removeItem("gameapi_token");

  // Update UI
  document.querySelector("#keycloakLoginBtn").style.display = "inline-block";
  document.querySelector("#logoutBtn").style.display = "none";

  status("Logged out.");

  // Reload games (will now show public view only)
  loadGames();
}

// Check for saved token on page load
function checkSavedToken() {
  const savedToken = localStorage.getItem("gameapi_token");
  console.log("Saved token present:", !!savedToken);
  if (savedToken) {
    document.querySelector("#token").value = savedToken;
    document.querySelector("#keycloakLoginBtn").style.display = "none";
    document.querySelector("#logoutBtn").style.display = "inline-block";
    status("Welcome back! You are logged in.");
  }
}

// ── Event listeners ──────────────────────────────────────────────────────────

document.addEventListener("DOMContentLoaded", () => {
  console.log("DOM loaded, attaching event listeners");

  const loadBtn = document.querySelector("#loadGamesBtn");
  const searchForm = document.querySelector("#searchForm");
  const gameForm = document.querySelector("#gameForm");
  const clearBtn = document.querySelector("#clearFormBtn");
  const loginBtn = document.querySelector("#keycloakLoginBtn");
  const logoutBtn = document.querySelector("#logoutBtn");
  const tokenInput = document.querySelector("#token");

  if (loadBtn) loadBtn.addEventListener("click", () => loadGames());
  if (searchForm) searchForm.addEventListener("submit", (e) => {
    e.preventDefault();
    loadGames(document.querySelector("#searchInput").value.trim());
  });
  if (gameForm) gameForm.addEventListener("submit", saveGame);
  if (clearBtn) clearBtn.addEventListener("click", clearForm);
  if (loginBtn) {
    console.log("Login button found, attaching click handler");
    loginBtn.addEventListener("click", loginWithKeycloak);
  } else {
    console.error("Login button not found!");
  }
  if (logoutBtn) logoutBtn.addEventListener("click", logout);
  if (tokenInput) {
    tokenInput.addEventListener("change", (e) => {
      if (e.target.value.trim()) {
        localStorage.setItem("gameapi_token", e.target.value.trim());
        if (loginBtn) loginBtn.style.display = "none";
        if (logoutBtn) logoutBtn.style.display = "inline-block";
      } else {
        localStorage.removeItem("gameapi_token");
        if (loginBtn) loginBtn.style.display = "inline-block";
        if (logoutBtn) logoutBtn.style.display = "none";
      }
    });
  }

  checkSavedToken();
  checkForOAuthCallback();
  loadGames();
});