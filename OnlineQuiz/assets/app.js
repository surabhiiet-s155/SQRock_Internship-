const state = {
    jobs: [],
    user: null,
    selectedRole: "ROLE_CANDIDATE",
    csrfToken: null,
    keyword: "",
    location: ""
};

const demoAccounts = {
    candidate: { role: "ROLE_CANDIDATE", email: "candidate@jobportal.demo", password: "candidate123" },
    employer: { role: "ROLE_EMPLOYER", email: "employer@jobportal.demo", password: "employer123" },
    admin: { role: "ROLE_ADMIN", email: "admin@jobportal.demo", password: "admin123" }
};

const elements = {
    jobGrid: document.querySelector("#jobGrid"),
    jobCount: document.querySelector("#jobCount"),
    emptyJobs: document.querySelector("#emptyJobs"),
    loginModal: document.querySelector("#loginModal"),
    loginForm: document.querySelector("#loginForm"),
    loginError: document.querySelector("#loginError"),
    loginSubmit: document.querySelector("#loginSubmit"),
    dashboardSection: document.querySelector("#dashboardSection"),
    dashboardContent: document.querySelector("#dashboardContent"),
    dashboardTitle: document.querySelector("#dashboardTitle"),
    dashboardSubtitle: document.querySelector("#dashboardSubtitle"),
    toast: document.querySelector("#toast")
};

document.querySelector("#year").textContent = new Date().getFullYear();
document.querySelector("#loginOpen").addEventListener("click", openLogin);
document.querySelector("#howLoginButton").addEventListener("click", openLogin);
document.querySelector("#loginClose").addEventListener("click", closeLogin);
document.querySelector("#browseJobsButton").addEventListener("click", () =>
    document.querySelector("#jobs").scrollIntoView({ behavior: "smooth" }));
document.querySelector("#logoutButton").addEventListener("click", logout);
document.querySelector("#searchForm").addEventListener("submit", event => {
    event.preventDefault();
    state.keyword = document.querySelector("#keywordInput").value.trim().toLowerCase();
    state.location = document.querySelector("#locationInput").value.trim().toLowerCase();
    renderJobs();
    document.querySelector("#jobs").scrollIntoView({ behavior: "smooth" });
});
document.querySelector("#sortJobs").addEventListener("change", renderJobs);
document.querySelectorAll(".role-option").forEach(button => {
    button.addEventListener("click", () => selectRole(button.dataset.role));
});
document.querySelectorAll("[data-demo]").forEach(button => {
    button.addEventListener("click", () => fillDemo(button.dataset.demo));
});
elements.loginForm.addEventListener("submit", submitLogin);
elements.loginModal.addEventListener("click", event => {
    if (event.target === elements.loginModal) closeLogin();
});
document.addEventListener("keydown", event => {
    if (event.key === "Escape" && !elements.loginModal.hidden) closeLogin();
});

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body) headers.set("Content-Type", "application/json");
    if (options.method && options.method !== "GET") {
        if (!state.csrfToken) {
            const csrfResponse = await fetch("/api/auth/csrf");
            if (!csrfResponse.ok) throw new Error("Could not start a secure session. Please refresh and try again.");
            state.csrfToken = (await csrfResponse.json()).token;
        }
        headers.set("X-XSRF-TOKEN", state.csrfToken);
    }
    const response = await fetch(path, { ...options, headers, credentials: "same-origin" });
    if (response.status === 204) return null;
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const message = body.message || body.detail || (response.status === 401
            ? "Please sign in to continue." : "Something went wrong. Please try again.");
        throw new Error(message);
    }
    return body;
}

async function loadJobs() {
    try {
        state.jobs = await api("/api/jobs");
        renderJobs();
    } catch (error) {
        elements.jobGrid.innerHTML = `<div class="error-card">${escapeHtml(error.message)}</div>`;
        elements.jobCount.textContent = "Jobs unavailable";
    }
}

async function restoreSession() {
    try {
        const user = await api("/api/auth/me");
        if (user && user.role) {
            state.user = user;
            await showDashboard();
        }
    } catch {
        // An anonymous visitor does not have a session to restore.
    }
}

function renderJobs() {
    const searchTerm = `${state.keyword} ${state.location}`.trim();
    const jobs = state.jobs.filter(job => {
        const searchable = `${job.title} ${job.company} ${job.location} ${job.type}`.toLowerCase();
        return (!state.keyword || searchable.includes(state.keyword)) &&
            (!state.location || `${job.location} ${job.remote ? "remote" : ""}`.toLowerCase().includes(state.location));
    });
    if (document.querySelector("#sortJobs").value === "title") {
        jobs.sort((left, right) => left.title.localeCompare(right.title));
    }
    elements.jobCount.textContent = searchTerm
        ? `${jobs.length} ${jobs.length === 1 ? "role" : "roles"} found`
        : `${jobs.length} open roles`;
    elements.emptyJobs.hidden = jobs.length > 0;
    elements.jobGrid.hidden = jobs.length === 0;
    elements.jobGrid.innerHTML = jobs.map(jobCard).join("");
    elements.jobGrid.querySelectorAll("[data-apply]").forEach(button => {
        button.addEventListener("click", () => applyToJob(Number(button.dataset.apply)));
    });
}

function jobCard(job) {
    const initials = job.company.split(/\s+/).map(word => word[0]).join("").slice(0, 2).toUpperCase();
    return `<article class="job-card">
        <div class="job-card-top">
            <span class="company-mark company-mark-${Math.abs(hash(job.company)) % 4}">${escapeHtml(initials)}</span>
            <span class="job-posted">${job.remote ? "Remote friendly" : "New opportunity"}</span>
        </div>
        <h3>${escapeHtml(job.title)}</h3>
        <p class="company-name">${escapeHtml(job.company)}</p>
        <div class="job-meta">
            <span><svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M19 10c0 5-7 11-7 11S5 15 5 10a7 7 0 1 1 14 0Z" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="10" r="2" stroke="currentColor" stroke-width="1.7"/></svg>${escapeHtml(job.location)}</span>
            <span><svg viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M4 7h16v13H4zM8 7V4h8v3M4 12h16" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/></svg>${escapeHtml(job.type)}</span>
        </div>
        <div class="salary-line"><span>${escapeHtml(job.salary)}</span><span class="salary-period"> / year</span></div>
        <p class="job-description">${escapeHtml(job.description)}</p>
        <div class="job-card-bottom">
            <span class="job-tag">${job.remote ? "Remote" : "On-site"}</span>
            <button class="button button-primary apply-button" type="button" data-apply="${job.id}">Apply now <span aria-hidden="true">→</span></button>
        </div>
    </article>`;
}

function openLogin() {
    elements.loginError.hidden = true;
    elements.loginModal.hidden = false;
    document.body.classList.add("modal-open");
    document.querySelector("#emailInput").focus();
}

function closeLogin() {
    elements.loginModal.hidden = true;
    document.body.classList.remove("modal-open");
}

function selectRole(role) {
    state.selectedRole = role;
    document.querySelectorAll(".role-option").forEach(button => {
        const selected = button.dataset.role === role;
        button.classList.toggle("is-selected", selected);
        button.setAttribute("aria-pressed", String(selected));
    });
}

function fillDemo(kind) {
    const account = demoAccounts[kind];
    if (!account) return;
    selectRole(account.role);
    document.querySelector("#emailInput").value = account.email;
    document.querySelector("#passwordInput").value = account.password;
    elements.loginError.hidden = true;
    document.querySelector("#emailInput").focus();
}

async function submitLogin(event) {
    event.preventDefault();
    elements.loginError.hidden = true;
    elements.loginSubmit.disabled = true;
    elements.loginSubmit.textContent = "Signing in…";
    try {
        state.user = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({
                email: document.querySelector("#emailInput").value.trim(),
                password: document.querySelector("#passwordInput").value,
                role: state.selectedRole
            })
        });
        closeLogin();
        await showDashboard();
        showToast(`Welcome back, ${state.user.name.split(" ")[0]}.`);
    } catch (error) {
        elements.loginError.textContent = error.message;
        elements.loginError.hidden = false;
    } finally {
        elements.loginSubmit.disabled = false;
        elements.loginSubmit.innerHTML = 'Sign in <span aria-hidden="true">→</span>';
    }
}

async function logout() {
    try {
        await api("/api/auth/logout", { method: "POST" });
        state.user = null;
        elements.dashboardSection.hidden = true;
        document.querySelector("#loginOpen").hidden = false;
        document.querySelector("#logoutButton").hidden = true;
        document.querySelector("#signedInLabel").hidden = true;
        showToast("You have signed out.");
    } catch (error) {
        showToast(error.message);
    }
}

async function showDashboard() {
    if (!state.user) return;
    try {
        const data = await api("/api/dashboard");
        document.querySelector("#loginOpen").hidden = true;
        document.querySelector("#logoutButton").hidden = false;
        const label = document.querySelector("#signedInLabel");
        label.textContent = `${state.user.name} · ${roleLabel(state.user.role)}`;
        label.hidden = false;
        elements.dashboardSection.hidden = false;
        elements.dashboardTitle.textContent = `${roleLabel(state.user.role)} dashboard`;
        elements.dashboardSubtitle.textContent = dashboardSubtitle(state.user.role);
        elements.dashboardContent.innerHTML = dashboardMarkup(data, state.user.role);
        wireDashboard(data);
    } catch (error) {
        showToast(error.message);
        if (error.message.includes("sign in")) {
            state.user = null;
            document.querySelector("#loginOpen").hidden = false;
            document.querySelector("#logoutButton").hidden = true;
        }
    }
}

function dashboardSubtitle(role) {
    if (role === "ROLE_ADMIN") return "A clear view of the people and opportunities on JobPortal.";
    if (role === "ROLE_EMPLOYER") return "Manage your open roles and meet your next great hire.";
    return "Your applications and next opportunities, all in one place.";
}

function dashboardMarkup(data, role) {
    if (role === "ROLE_ADMIN") {
        return `<div class="stats-grid">
            ${statCard("Community members", data.totalUsers, "People using the portal", "♙")}
            ${statCard("Job providers", data.totalEmployers, "Employer accounts", "▣")}
            ${statCard("Job seekers", data.totalCandidates, "Candidate accounts", "↗")}
            ${statCard("Open opportunities", data.totalJobs, "Published job listings", "✳")}
        </div>
        <div class="dashboard-panel">
            <div class="panel-heading"><div><span class="eyebrow eyebrow-blue">PLATFORM ACTIVITY</span><h3>Recent applications</h3></div><span class="panel-count">${data.totalApplications} total</span></div>
            ${applicationTable(data.recentApplications, true)}
        </div>`;
    }
    if (role === "ROLE_EMPLOYER") {
        return `<div class="stats-grid stats-grid-two">
            ${statCard("Active jobs", data.activeJobs, "Roles you are hiring for", "▣")}
            ${statCard("Applications received", data.applicationsReceived, "People interested in your roles", "↗")}
        </div>
        <div class="dashboard-two-col">
            <div class="dashboard-panel">
                <div class="panel-heading"><div><span class="eyebrow eyebrow-blue">YOUR OPEN ROLES</span><h3>Posted jobs</h3></div></div>
                ${jobList(data.myJobs)}
            </div>
            <div class="dashboard-panel">
                <div class="panel-heading"><div><span class="eyebrow eyebrow-blue">YOUR TALENT PIPELINE</span><h3>Recent applicants</h3></div></div>
                ${applicationTable(data.applications, false)}
            </div>
        </div>
        <div class="dashboard-panel post-job-panel">
            <div class="panel-heading"><div><span class="eyebrow eyebrow-blue">GROW YOUR TEAM</span><h3>Post a new opportunity</h3></div></div>
            <form class="post-job-form" id="postJobForm">
                <label>Job title<input name="title" maxlength="120" placeholder="e.g. Product Designer" required></label>
                <label>Company<input name="company" maxlength="120" value="${escapeHtml(state.user.company || "")}" required></label>
                <label>Location<input name="location" maxlength="120" placeholder="City or Remote" required></label>
                <label>Employment type<select name="type"><option>Full-time</option><option>Part-time</option><option>Contract</option><option>Internship</option></select></label>
                <label>Salary range<input name="salary" maxlength="80" placeholder="$90k - $120k" required></label>
                <label class="form-wide">Description<textarea name="description" maxlength="1200" rows="3" placeholder="What will this person work on?" required></textarea></label>
                <label class="checkbox-label"><input name="remote" type="checkbox"> Remote friendly</label>
                <button class="button button-primary post-job-submit" type="submit">Publish opportunity <span aria-hidden="true">→</span></button>
                <p class="form-error form-wide" id="postJobError" role="alert" hidden></p>
            </form>
        </div>`;
    }
    return `<div class="stats-grid stats-grid-two">
        ${statCard("Applications sent", data.applicationsSent, "Your applications in progress", "↗")}
        ${statCard("Open jobs", state.jobs.length, "Roles you can explore", "✳")}
    </div>
    <div class="dashboard-panel">
        <div class="panel-heading"><div><span class="eyebrow eyebrow-blue">YOUR JOB SEARCH</span><h3>Recent activity</h3></div><span class="panel-count">${data.applicationsSent} applied</span></div>
        ${applicationTable(data.applications, false)}
    </div>`;
}

function statCard(label, value, caption, icon) {
    return `<article class="stat-card"><span class="stat-icon">${icon}</span><div><span class="stat-label">${escapeHtml(label)}</span><strong class="stat-value">${escapeHtml(String(value))}</strong><span class="stat-caption">${escapeHtml(caption)}</span></div></article>`;
}

function applicationTable(applications = [], showCandidate) {
    if (!applications.length) {
        return `<div class="dashboard-empty"><span class="empty-icon">✳</span><p>No applications to show just yet.</p></div>`;
    }
    const rows = applications.map(application => `<tr>
        <td><strong>${escapeHtml(application.jobTitle)}</strong><small>${escapeHtml(application.company)}</small></td>
        ${showCandidate ? `<td>${escapeHtml(application.candidateName)}<small>${escapeHtml(application.candidateEmail)}</small></td>` : ""}
        <td><span class="status-pill">${escapeHtml(application.status)}</span></td>
        <td>${formatDate(application.appliedAt)}</td>
    </tr>`).join("");
    return `<div class="table-scroll"><table class="data-table"><thead><tr><th>Opportunity</th>${showCandidate ? "<th>Candidate</th>" : ""}<th>Status</th><th>Applied</th></tr></thead><tbody>${rows}</tbody></table></div>`;
}

function jobList(jobs = []) {
    if (!jobs.length) return `<div class="dashboard-empty"><p>You have not posted any jobs yet. Share your first opportunity below.</p></div>`;
    return `<div class="posted-job-list">${jobs.map(job => `<article class="posted-job">
        <span class="company-mark company-mark-${Math.abs(hash(job.company)) % 4}">${escapeHtml(job.company.slice(0, 1).toUpperCase())}</span>
        <div><strong>${escapeHtml(job.title)}</strong><span>${escapeHtml(job.company)} · ${escapeHtml(job.location)}</span></div>
        <span class="job-tag">${escapeHtml(job.type)}</span>
    </article>`).join("")}</div>`;
}

function wireDashboard() {
    const form = document.querySelector("#postJobForm");
    if (form) form.addEventListener("submit", postJob);
}

async function postJob(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const error = document.querySelector("#postJobError");
    error.hidden = true;
    const formData = new FormData(form);
    const job = Object.fromEntries(formData.entries());
    job.remote = formData.has("remote");
    try {
        await api("/api/jobs", { method: "POST", body: JSON.stringify(job) });
        await loadJobs();
        await showDashboard();
        showToast("Your new opportunity is live.");
    } catch (exception) {
        error.textContent = exception.message;
        error.hidden = false;
    }
}

async function applyToJob(jobId) {
    if (!state.user) {
        openLogin();
        showToast("Sign in as a job seeker to apply.");
        return;
    }
    if (state.user.role !== "ROLE_CANDIDATE") {
        showToast("Applications are available to job seeker accounts.");
        return;
    }
    try {
        const result = await api(`/api/jobs/${jobId}/applications`, { method: "POST" });
        showToast(result.message);
        await showDashboard();
    } catch (error) {
        showToast(error.message);
    }
}

function roleLabel(role) {
    return ({
        ROLE_ADMIN: "Admin",
        ROLE_EMPLOYER: "Job provider",
        ROLE_CANDIDATE: "Job seeker"
    })[role] || "Member";
}

function formatDate(date) {
    if (!date) return "—";
    return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", year: "numeric" })
        .format(new Date(date));
}

function hash(value) {
    return [...value].reduce((total, character) => total + character.charCodeAt(0), 0);
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
}

let toastTimeout;
function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.classList.add("is-visible");
    window.clearTimeout(toastTimeout);
    toastTimeout = window.setTimeout(() => elements.toast.classList.remove("is-visible"), 3200);
}

loadJobs();
restoreSession();
