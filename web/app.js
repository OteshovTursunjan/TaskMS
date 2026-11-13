// SprintFlow web — port of the Android app (app/src/main/java/com/example).
// Room tables -> localStorage, SprintViewModel -> functions below.
'use strict';

// ---------- Constants (TaskEnums.kt, strings.xml) ----------
const STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'];
const STATUS_LABEL = { TODO: 'To Do', IN_PROGRESS: 'In Progress', DONE: 'Done' };
const STATUS_DOT = { TODO: 'var(--on-surface-variant)', IN_PROGRESS: 'var(--secondary)', DONE: 'var(--tertiary)' };
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
const PRIORITY_WEIGHT = { LOW: 0, MEDIUM: 1, HIGH: 2, CRITICAL: 3 };
const PRIORITY_LABEL = { LOW: 'Низкий', MEDIUM: 'Средний', HIGH: 'Высокий', CRITICAL: 'Критичный' };
const PRIORITY_COLOR = {
  LOW: 'color-mix(in srgb, var(--on-surface-variant) 55%, transparent)',
  MEDIUM: 'var(--primary)', HIGH: 'var(--secondary)', CRITICAL: 'var(--error)',
};
const CATEGORIES = ['FEATURE', 'BUG', 'REFACTOR', 'DEVOPS', 'DOCS'];
const CATEGORY_LABEL = { FEATURE: 'Feature', BUG: 'Bug', REFACTOR: 'Refactor', DEVOPS: 'DevOps', DOCS: 'Docs' };
const CATEGORY_COLORS = {
  FEATURE: ['var(--primary-container)', 'var(--on-primary-container)'],
  BUG: ['var(--error-container)', 'var(--on-error-container)'],
  REFACTOR: ['var(--secondary-container)', 'var(--on-secondary-container)'],
  DEVOPS: ['var(--tertiary-container)', 'var(--on-tertiary-container)'],
  DOCS: ['var(--surface-variant)', 'var(--on-surface-variant)'],
};
const STORY_POINTS = [1, 2, 3, 5, 8];
const PHASE = { FOCUS: 25 * 60, SHORT_BREAK: 5 * 60 };
const DEFAULT_SPRINT_NAME = 'Sprint #1';
const DEFAULT_SPRINT_GOAL = 'Сфокусированная поставка ключевых фич';
const FREE_SESSION = 'Свободная сессия';

// ---------- Icons ----------
const ICON = {
  edit: '<path d="M3 17.25V21h3.75L17.8 9.94l-3.75-3.75zM20.7 7.04a1 1 0 0 0 0-1.41l-2.33-2.33a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75z"/>',
  timer: '<path d="M9 2h6v2H9zM12 6a8 8 0 1 0 0 16 8 8 0 0 0 0-16zm1 8.6V9h-2v6.4l4.2 2.5 1-1.7z"/>',
  schedule: '<path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm4.2 14.2L11 13V7h1.5v5.2l4.5 2.7z"/>',
  back: '<path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20z"/>',
  fwd: '<path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/>',
  check: '<path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm-2 15-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8z"/>',
  play: '<path d="M8 5v14l11-7z"/>',
  pause: '<path d="M6 19h4V5H6zm8-14v14h4V5z"/>',
  reset: '<path d="M12 5V1L7 6l5 5V7a6 6 0 1 1-6 6H4a8 8 0 1 0 8-8z"/>',
  skip: '<path d="M6 18l8.5-6L6 6zM16 6v12h2V6z"/>',
  plus: '<path d="M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z"/>',
  search: '<path d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16a6.47 6.47 0 0 0 4.23-1.57l.27.28v.79l5 4.99L20.49 19zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>',
  columns: '<path d="M4 4h4v16H4zM10 4h4v16h-4zM16 4h4v16h-4z"/>',
  list: '<path d="M4 6h16v2H4zM4 11h16v2H4zM4 16h16v2H4z"/>',
  close: '<path d="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>',
  open: '<path d="M19 19H5V5h7V3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7h-2zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3z"/>',
  tune: '<path d="M3 17v2h6v-2zM3 5v2h10V5zm10 16v-2h8v-2h-8v-2h-2v6zM7 9v2H3v2h4v2h2V9zm14 4v-2H11v2zm-6-4h2V7h4V5h-4V3h-2z"/>',
};
const icon = (name) => `<svg viewBox="0 0 24 24" aria-hidden="true">${ICON[name]}</svg>`;

// ---------- Date utils (DateUtils.kt) ----------
const DAY_MS = 86400000;
function startOfDay(ms = Date.now()) { const d = new Date(ms); d.setHours(0, 0, 0, 0); return d.getTime(); }
function addDays(ms, days) { const d = new Date(startOfDay(ms)); d.setDate(d.getDate() + days); return d.getTime(); }
function daysFromToday(ms) { return Math.round((startOfDay(ms) - startOfDay()) / DAY_MS); }
const shortDateFmt = new Intl.DateTimeFormat('ru', { day: 'numeric', month: 'short' });
const timeFmt = new Intl.DateTimeFormat('ru', { hour: '2-digit', minute: '2-digit' });
const formatShortDate = (ms) => shortDateFmt.format(ms);
const formatTime = (ms) => timeFmt.format(ms);
function formatCountdown(sec) {
  const s = Math.max(0, sec);
  return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
}
const isToday = (ms) => startOfDay(ms) === startOfDay();
function toInputDate(ms) {
  const d = new Date(ms);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
function fromInputDate(value) {
  const [y, m, d] = value.split('-').map(Number);
  return new Date(y, m - 1, d).getTime();
}

const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// ---------- Storage (SprintDatabase / SprintRepository) ----------
const DB_KEY = 'sprintflow.db.v1';
const POMO_KEY = 'sprintflow.pomodoro.v1';

function readJson(key) {
  try { return JSON.parse(localStorage.getItem(key)); } catch { return null; }
}
function writeJson(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage unavailable: keep in memory */ }
}

const db = Object.assign({ tasks: [], config: null, sessions: [], nextId: 1 }, readJson(DB_KEY));
const saveDb = () => writeJson(DB_KEY, db);

if (!db.config) {
  const start = startOfDay();
  db.config = { sprintName: DEFAULT_SPRINT_NAME, sprintGoal: DEFAULT_SPRINT_GOAL, durationWeeks: 2, startDateMillis: start, endDateMillis: addDays(start, 13) };
  saveDb();
}

// ---------- UI + Pomodoro state (SprintViewModel) ----------
const ui = { dest: 'kanban', filter: 'ALL', query: '', view: 'COLUMNS' };
const pomo = Object.assign(
  { phase: 'FOCUS', remaining: PHASE.FOCUS, total: PHASE.FOCUS, running: false, endAt: null, taskId: null, banner: null },
  readJson(POMO_KEY),
);
const savePomo = () => writeJson(POMO_KEY, pomo);
let timerHandle = null;

const findTask = (id) => db.tasks.find((t) => t.id === id) || null;

function saveTask(existing, f) {
  const title = f.title.trim();
  if (!title) return;
  const now = Date.now();
  const fields = {
    title,
    description: f.description.trim(),
    status: f.status,
    priority: f.priority,
    category: f.category,
    storyPoints: Math.min(13, Math.max(1, f.storyPoints)),
    deadlineMillis: f.deadlineMillis,
    estimatedPomodoros: Math.min(12, Math.max(1, f.estimatedPomodoros)),
    updatedAtMillis: now,
  };
  if (existing) Object.assign(existing, fields);
  else db.tasks.push({ id: db.nextId++, pomodorosCompleted: 0, createdAtMillis: now, ...fields });
  saveDb();
}

function moveTask(id, status) {
  const t = findTask(id);
  if (!t) return;
  t.status = status;
  t.updatedAtMillis = Date.now();
  saveDb();
}

function deleteTask(id) {
  if (pomo.taskId === id) { pauseTimer(); pomo.taskId = null; savePomo(); }
  db.tasks = db.tasks.filter((t) => t.id !== id);
  saveDb();
}

function saveSprintConfig(name, goal, weeks, startMillis) {
  const w = Math.min(2, Math.max(1, weeks));
  const start = startOfDay(startMillis);
  db.config = {
    sprintName: name.trim() || DEFAULT_SPRINT_NAME,
    sprintGoal: goal.trim(),
    durationWeeks: w,
    startDateMillis: start,
    endDateMillis: addDays(start, w * 7 - 1),
  };
  saveDb();
}

function setPhase(phase) {
  Object.assign(pomo, { phase, remaining: PHASE[phase], total: PHASE[phase], running: false, endAt: null, banner: null });
}

function selectTaskForFocus(id) { pomo.taskId = id; pomo.banner = null; savePomo(); }

function startFocusForTask(task) {
  if (pomo.taskId !== task.id || pomo.phase !== 'FOCUS') {
    pauseTimer();
    setPhase('FOCUS');
    pomo.taskId = task.id;
  }
  if (task.status === 'TODO') moveTask(task.id, 'IN_PROGRESS');
  ui.dest = 'focus';
  startTimer();
}

function switchPhase(phase) { pauseTimer(); setPhase(phase); savePomo(); }
function toggleTimer() { pomo.running ? pauseTimer() : startTimer(); }

function startTimer() {
  if (pomo.running) return;
  if (pomo.remaining <= 0) pomo.remaining = PHASE[pomo.phase];
  pomo.running = true;
  pomo.banner = null;
  pomo.endAt = Date.now() + pomo.remaining * 1000;
  savePomo();
  runTicker();
}

function runTicker() {
  clearInterval(timerHandle);
  timerHandle = setInterval(tick, 250);
  tick();
}

function pauseTimer() {
  clearInterval(timerHandle);
  timerHandle = null;
  if (pomo.running) {
    pomo.remaining = Math.max(0, Math.ceil((pomo.endAt - Date.now()) / 1000));
  }
  pomo.running = false;
  pomo.endAt = null;
  savePomo();
  document.title = 'SprintFlow';
}

function resetTimer() { pauseTimer(); setPhase(pomo.phase); savePomo(); }
function skipPhase() { switchPhase(pomo.phase === 'FOCUS' ? 'SHORT_BREAK' : 'FOCUS'); }

function tick() {
  if (!pomo.running) return;
  pomo.remaining = Math.max(0, Math.ceil((pomo.endAt - Date.now()) / 1000));
  if (pomo.remaining <= 0) { onPhaseFinished(); return; }
  document.title = `${formatCountdown(pomo.remaining)} • SprintFlow`;
  updateTimerViews();
}

function onPhaseFinished() {
  clearInterval(timerHandle);
  timerHandle = null;
  const finished = pomo.phase;
  if (finished === 'FOCUS') {
    const task = findTask(pomo.taskId);
    const now = Date.now();
    if (task) { task.pomodorosCompleted += 1; task.updatedAtMillis = now; }
    db.sessions.unshift({ taskId: task ? task.id : null, taskTitle: task ? task.title : FREE_SESSION, durationMinutes: 25, completedAtMillis: now });
    db.sessions = db.sessions.slice(0, 200);
    saveDb();
  }
  setPhase(finished === 'FOCUS' ? 'SHORT_BREAK' : 'FOCUS');
  pomo.banner = finished;
  savePomo();
  document.title = 'SprintFlow';
  beep();
  render();
}

function beep() {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    [0, 0.35, 0.7].forEach((t) => {
      const o = ctx.createOscillator();
      const g = ctx.createGain();
      o.frequency.value = 880;
      g.gain.setValueAtTime(0.2, ctx.currentTime + t);
      g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + t + 0.25);
      o.connect(g).connect(ctx.destination);
      o.start(ctx.currentTime + t);
      o.stop(ctx.currentTime + t + 0.3);
    });
  } catch { /* audio unavailable */ }
}

// ---------- Derived state ----------
function filteredTasks() {
  const q = ui.query.trim().toLowerCase();
  return db.tasks
    .filter((t) => (ui.filter === 'ALL' || t.status === ui.filter) && (!q
      || t.title.toLowerCase().includes(q)
      || t.description.toLowerCase().includes(q)
      || t.category.toLowerCase().includes(q)))
    .sort((a, b) => b.updatedAtMillis - a.updatedAtMillis)
    .sort((a, b) => STATUSES.indexOf(a.status) - STATUSES.indexOf(b.status)
      || PRIORITY_WEIGHT[b.priority] - PRIORITY_WEIGHT[a.priority]
      || (a.deadlineMillis ?? Infinity) - (b.deadlineMillis ?? Infinity));
}
const countBy = (status) => db.tasks.filter((t) => t.status === status).length;
const totalPomodoros = () => db.tasks.reduce((s, t) => s + t.pomodorosCompleted, 0);
const todayCycles = () => db.sessions.filter((s) => isToday(s.completedAtMillis)).length;

// ---------- Rendering ----------
const $main = document.getElementById('main');
const $fab = document.getElementById('fab');
const $dialog = document.getElementById('dialog');

function render() {
  if (pomo.taskId != null && !findTask(pomo.taskId)) pomo.taskId = null;
  document.querySelectorAll('.nav-item').forEach((b) => b.classList.toggle('active', b.dataset.dest === ui.dest));
  $fab.hidden = ui.dest !== 'kanban';
  $main.innerHTML = ui.dest === 'kanban' ? kanbanHtml() : focusHtml();
  if (ui.dest === 'kanban') renderBoard();
  updateTimerViews();
}

function sprintHeaderHtml() {
  const c = db.config;
  const total = db.tasks.length;
  const done = countBy('DONE');
  const left = daysFromToday(c.endDateMillis);
  const leftText = left < 0 ? 'Спринт завершён' : left === 0 ? 'Последний день' : `Осталось ${left} дн.`;
  const leftColors = left < 0 ? CATEGORY_COLORS.BUG : left <= 2 ? CATEGORY_COLORS.REFACTOR : CATEGORY_COLORS.DEVOPS;
  return `
    <section class="card sprint-header">
      <div class="sprint-top">
        <div>
          <h1 class="sprint-name">${esc(c.sprintName)}</h1>
          <div class="sprint-meta">
            <span class="muted">${formatShortDate(c.startDateMillis)} – ${formatShortDate(c.endDateMillis)} • ${c.durationWeeks === 1 ? '1 неделя' : '2 недели'}</span>
            <span class="pill badge" style="background:${leftColors[0]};color:${leftColors[1]}">${leftText}</span>
          </div>
        </div>
        <button class="icon-btn" data-action="edit-sprint" title="Настроить спринт" aria-label="Настроить спринт">${icon('tune')}</button>
      </div>
      ${c.sprintGoal ? `<p class="sprint-goal">${esc(c.sprintGoal)}</p>` : ''}
      <div class="progress"><div style="width:${total ? (done / total) * 100 : 0}%"></div></div>
      <div class="sprint-stats">
        <span class="muted">Выполнено ${done} из ${total} задач</span>
        <span class="pill" style="background:var(--secondary-container);color:var(--on-secondary-container)">${icon('timer')}${totalPomodoros()} фокус-сессий</span>
      </div>
    </section>`;
}

function kanbanHtml() {
  const counts = { ALL: db.tasks.length, TODO: countBy('TODO'), IN_PROGRESS: countBy('IN_PROGRESS'), DONE: countBy('DONE') };
  const chips = ['ALL', ...STATUSES].map((f) => `
    <button class="chip ${ui.filter === f ? 'selected' : ''}" data-action="filter" data-value="${f}">
      ${f === 'ALL' ? 'Все' : STATUS_LABEL[f]}<span class="count">${counts[f]}</span>
    </button>`).join('');
  const nextView = ui.view === 'COLUMNS' ? 'list' : 'columns';
  return `
    ${sprintHeaderHtml()}
    <div class="toolbar">
      <div class="chips">${chips}</div>
      <label class="search">${icon('search')}<input id="search" type="search" placeholder="Поиск по задачам…" value="${esc(ui.query)}" aria-label="Поиск по задачам"></label>
      <button class="btn btn-sm" data-action="toggle-view" title="Переключить вид доски">${icon(nextView)}${ui.view === 'COLUMNS' ? 'Список' : 'Колонки'}</button>
    </div>
    <div id="board"></div>
    <div id="mini-pomo"></div>`;
}

function renderBoard() {
  const $board = document.getElementById('board');
  if (!$board) return;
  if (db.tasks.length === 0) {
    $board.className = '';
    $board.innerHTML = `
      <div class="card empty">
        <img src="empty.jpg" alt="">
        <h3>Спринт готов к планированию</h3>
        <p>Добавьте задачи разработки, назначьте дедлайны и запускайте 25-минутный Pomodoro-таймер прямо с карточки.</p>
        <button class="btn btn-primary" data-action="add-task" data-status="TODO">${icon('plus')}Добавить первую задачу</button>
      </div>`;
    return;
  }
  const tasks = filteredTasks();
  if (ui.view === 'LIST') {
    $board.className = 'list';
    $board.innerHTML = tasks.length ? tasks.map(taskCardHtml).join('')
      : `<div class="empty-col muted">Ничего не найдено</div>`;
    return;
  }
  const cols = ui.filter === 'ALL' ? STATUSES : [ui.filter];
  $board.className = `board cols-${cols.length}`;
  $board.innerHTML = cols.map((status) => {
    const items = tasks.filter((t) => t.status === status);
    return `
      <section class="column">
        <div class="column-head">
          <span><span class="dot" style="background:${STATUS_DOT[status]}"></span>${STATUS_LABEL[status]} <span class="muted mono">${items.length}</span></span>
          <button class="icon-btn" data-action="add-task" data-status="${status}" aria-label="Добавить задачу в ${STATUS_LABEL[status]}">${icon('plus')}</button>
        </div>
        ${items.length ? items.map(taskCardHtml).join('') : `
          <div class="empty-col"><strong>В колонке «${STATUS_LABEL[status]}» пока пусто</strong><br><span class="muted">Создайте новую задачу или перенесите карточку из другой колонки.</span></div>`}
      </section>`;
  }).join('');
}

function deadlineBadgeHtml(t) {
  const isDone = t.status === 'DONE';
  if (t.deadlineMillis == null) {
    return `<span class="pill badge" style="background:var(--surface-variant);color:var(--on-surface-variant)">${icon('schedule')}Без дедлайна</span>`;
  }
  const diff = daysFromToday(t.deadlineMillis);
  const date = formatShortDate(t.deadlineMillis);
  let text, bg, fg;
  if (isDone) [text, bg, fg] = [date, 'var(--tertiary-container)', 'var(--on-tertiary-container)'];
  else if (diff < 0) [text, bg, fg] = [`Просрочено (${date})`, 'var(--error-container)', 'var(--on-error-container)'];
  else if (diff === 0) [text, bg, fg] = ['Сегодня', 'var(--secondary-container)', 'var(--on-secondary-container)'];
  else if (diff === 1) [text, bg, fg] = ['Завтра', 'var(--secondary-container)', 'var(--on-secondary-container)'];
  else [text, bg, fg] = [`Через ${diff} дн. (${date})`, 'var(--surface-variant)', 'var(--on-surface-variant)'];
  return `<span class="pill badge" style="background:${bg};color:${fg}">${icon('schedule')}${text}</span>`;
}

function categoryPill(cat) {
  const [bg, fg] = CATEGORY_COLORS[cat];
  return `<span class="pill" style="background:${bg};color:${fg}">${CATEGORY_LABEL[cat]}</span>`;
}

function taskCardHtml(t) {
  const prev = { TODO: null, IN_PROGRESS: 'TODO', DONE: 'IN_PROGRESS' }[t.status];
  const next = { TODO: 'IN_PROGRESS', IN_PROGRESS: 'DONE', DONE: null }[t.status];
  const active = pomo.taskId === t.id;
  return `
    <article class="task ${t.status === 'DONE' ? 'done' : ''} ${active ? 'focus-active' : ''}">
      <div class="task-stripe" style="background:${PRIORITY_COLOR[t.priority]}" title="Приоритет: ${PRIORITY_LABEL[t.priority]}"></div>
      <div class="task-body">
        <div class="task-top">
          <div class="task-tags">
            ${categoryPill(t.category)}
            <span class="pill" style="background:var(--surface-variant);color:var(--on-surface-variant)">${t.storyPoints} SP</span>
            ${active ? '<span class="pill" style="background:var(--secondary-container);color:var(--on-secondary-container)">FOCUS</span>' : ''}
          </div>
          <button class="icon-btn" data-action="edit-task" data-id="${t.id}" aria-label="Редактировать задачу">${icon('edit')}</button>
        </div>
        <h3 class="task-title">${esc(t.title)}</h3>
        ${t.description.trim() ? `<p class="task-desc">${esc(t.description)}</p>` : ''}
        <div class="task-badges">
          ${deadlineBadgeHtml(t)}
          <span class="pill badge" style="background:var(--surface-variant);color:var(--on-surface)"><span style="color:var(--secondary);display:inline-flex">${icon('timer')}</span>${t.pomodorosCompleted}/${t.estimatedPomodoros}</span>
        </div>
        <div class="task-actions">
          <div class="task-moves">
            ${prev ? `<button class="btn btn-sm" data-action="move" data-id="${t.id}" data-status="${prev}" aria-label="Переместить в ${STATUS_LABEL[prev]}">${icon('back')}${STATUS_LABEL[prev]}</button>` : ''}
            ${next ? `<button class="btn btn-sm" data-action="move" data-id="${t.id}" data-status="${next}" aria-label="Переместить в ${STATUS_LABEL[next]}">${icon(next === 'DONE' ? 'check' : 'fwd')}${STATUS_LABEL[next]}</button>` : ''}
          </div>
          ${t.status !== 'DONE' ? `<button class="btn btn-sm btn-tonal" data-action="focus-task" data-id="${t.id}">${icon('play')}Фокус 25м</button>` : ''}
        </div>
      </div>
    </article>`;
}

function mainTimerLabel() {
  if (pomo.running) return 'Пауза';
  return pomo.remaining < pomo.total ? 'Продолжить' : 'Старт';
}

function focusHtml() {
  const task = findTask(pomo.taskId);
  const isFocus = pomo.phase === 'FOCUS';
  const ringColor = isFocus ? 'var(--secondary)' : 'var(--tertiary)';
  const banner = pomo.banner && `
    <div class="banner" role="status">
      <p>${pomo.banner === 'FOCUS' ? 'Сессия фокуса завершена! +1 Pomodoro. Время перерыва 5 мин.' : 'Перерыв окончен! Можно начинать новый цикл фокусировки.'}</p>
      <button class="icon-btn" data-action="dismiss-banner" aria-label="Закрыть">${icon('close')}</button>
    </div>`;
  const recent = db.sessions.slice(0, 30);
  return `
    <div class="focus">
      ${banner || ''}
      <section class="card focus-task">
        <div class="focus-task-head">
          <span class="muted">${task ? 'Текущая задача' : 'Выберите задачу для фокусировки'}</span>
          <button class="btn btn-sm" data-action="pick-task">Выбрать задачу</button>
        </div>
        ${task ? `
          <h2>${esc(task.title)}</h2>
          <div class="task-badges">
            ${categoryPill(task.category)}
            <span class="pill badge" style="background:var(--surface-variant);color:var(--on-surface)"><span style="color:var(--secondary);display:inline-flex">${icon('timer')}</span>${task.pomodorosCompleted}/${task.estimatedPomodoros}</span>
            <span class="pill badge" style="background:var(--surface-variant);color:var(--on-surface-variant)">${STATUS_LABEL[task.status]}</span>
          </div>
          ${task.status === 'TODO' ? `<div><button class="btn btn-sm" data-action="move-in-progress" data-id="${task.id}">${icon('fwd')}Перевести в In Progress</button></div>` : ''}
        ` : '<p class="muted" style="margin:0">Таймер запустится без привязки, или выберите задачу из спринта</p>'}
      </section>

      <div class="phase-tabs" role="tablist">
        <button role="tab" class="${isFocus ? 'selected' : ''}" data-action="phase" data-value="FOCUS">Фокус 25:00</button>
        <button role="tab" class="${!isFocus ? 'selected' : ''}" data-action="phase" data-value="SHORT_BREAK">Перерыв 05:00</button>
      </div>

      <section class="card timer">
        <div class="ring">
          <svg viewBox="0 0 120 120" aria-hidden="true">
            <circle class="track" cx="60" cy="60" r="52"/>
            <circle class="bar js-ring" cx="60" cy="60" r="52" style="stroke:${ringColor}" stroke-dasharray="326.73"/>
          </svg>
          <div class="ring-center">
            <span class="ring-label" style="color:${ringColor}">${isFocus ? 'ФОКУС • 25 МИН' : 'ПЕРЕРЫВ • 5 МИН'}</span>
            <span class="ring-time js-time">${formatCountdown(pomo.remaining)}</span>
            <span class="muted">Сегодня циклов: ${todayCycles()}</span>
          </div>
        </div>
        <div class="timer-controls">
          <button class="btn" data-action="reset">${icon('reset')}Сброс</button>
          <button class="btn btn-primary btn-main js-main" data-action="toggle-timer">${icon(pomo.running ? 'pause' : 'play')}${mainTimerLabel()}</button>
          <button class="btn" data-action="skip">${icon('skip')}Пропустить</button>
        </div>
      </section>

      <section class="card history">
        <h3>Журнал фокус-сессий</h3>
        ${recent.length ? `<ul>${recent.map((s) => `
          <li><span>${esc(s.taskTitle)}</span><span class="muted mono">${s.durationMinutes}m • ${formatTime(s.completedAtMillis)}</span></li>`).join('')}</ul>`
          : '<p class="muted" style="margin:0">Завершённые 25-минутные сессии появятся здесь.</p>'}
      </section>
    </div>`;
}

// Cheap per-tick update so inputs keep focus while the timer runs.
function updateTimerViews() {
  document.querySelectorAll('.js-time').forEach((el) => { el.textContent = formatCountdown(pomo.remaining); });
  const ring = document.querySelector('.js-ring');
  if (ring) ring.style.strokeDashoffset = String(326.73 * (1 - (pomo.total ? pomo.remaining / pomo.total : 0)));
  const mini = document.getElementById('mini-pomo');
  if (!mini) return;
  const show = pomo.running || pomo.remaining < pomo.total;
  if (!show) { mini.innerHTML = ''; return; }
  const want = `${pomo.running}|${pomo.taskId}|${pomo.phase}`;
  if (mini.dataset.key !== want) {
    mini.dataset.key = want;
    const task = findTask(pomo.taskId);
    const label = task ? task.title : (pomo.phase === 'FOCUS' ? FREE_SESSION : 'Перерыв');
    mini.innerHTML = `
      <div class="mini-pomo">
        <span class="time js-time">${formatCountdown(pomo.remaining)}</span>
        <span class="title" data-action="open-focus">${esc(label)}</span>
        <button class="icon-btn" data-action="toggle-timer" aria-label="${pomo.running ? 'Пауза' : 'Продолжить'}">${icon(pomo.running ? 'pause' : 'play')}</button>
        <button class="icon-btn" data-action="open-focus" aria-label="Открыть таймер">${icon('open')}</button>
      </div>`;
  }
}

// ---------- Dialogs ----------
function openDialog(html, onOpen) {
  $dialog.innerHTML = html;
  $dialog.showModal();
  onOpen?.($dialog);
}
function closeDialog() { $dialog.close(); $dialog.innerHTML = ''; }
$dialog.addEventListener('click', (e) => { if (e.target === $dialog) closeDialog(); });

function segHtml(name, values, labels, selected) {
  return `<div class="seg" data-seg="${name}">${values.map((v) => `
    <button type="button" class="chip ${String(v) === String(selected) ? 'selected' : ''}" data-value="${v}">${labels(v)}</button>`).join('')}</div>`;
}
function bindSegs(root, state, onChange) {
  root.querySelectorAll('[data-seg]').forEach((seg) => {
    seg.addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-value]');
      if (!btn) return;
      seg.querySelectorAll('button').forEach((b) => b.classList.toggle('selected', b === btn));
      state[seg.dataset.seg] = btn.dataset.value;
      onChange?.(seg.dataset.seg);
    });
  });
}

function openTaskEditor(existing, initialStatus) {
  const sprintEnd = db.config.endDateMillis;
  const s = {
    status: existing?.status ?? initialStatus,
    priority: existing?.priority ?? 'MEDIUM',
    category: existing?.category ?? 'FEATURE',
    storyPoints: existing?.storyPoints ?? 2,
    deadlineMode: existing?.deadlineMillis == null ? 'none' : existing.deadlineMillis === sprintEnd ? 'sprint' : 'date',
    est: existing?.estimatedPomodoros ?? 2,
  };
  const dateValue = toInputDate(existing?.deadlineMillis ?? addDays(Date.now(), 3));
  openDialog(`
    <form class="sheet" method="dialog" novalidate>
      <h2>${existing ? 'Редактировать задачу' : 'Новая задача'}</h2>
      <label class="field"><span>Название задачи</span>
        <input name="title" required maxlength="200" placeholder="Например: Реализовать OAuth2 refresh" value="${esc(existing?.title ?? '')}"></label>
      <label class="field"><span>Технические заметки</span>
        <textarea name="description" rows="3" placeholder="Критерии приёмки, ссылки на PR или детали…">${esc(existing?.description ?? '')}</textarea></label>
      <div class="field"><span>Колонка Канбана</span>${segHtml('status', STATUSES, (v) => STATUS_LABEL[v], s.status)}</div>
      <div class="field"><span>Приоритет</span>${segHtml('priority', PRIORITIES, (v) => PRIORITY_LABEL[v], s.priority)}</div>
      <div class="field"><span>Тег</span>${segHtml('category', CATEGORIES, (v) => CATEGORY_LABEL[v], s.category)}</div>
      <div class="field"><span>Дедлайн</span>
        ${segHtml('deadlineMode', ['none', 'sprint', 'date'], (v) => ({ none: 'Без дедлайна', sprint: `Конец спринта (${formatShortDate(sprintEnd)})`, date: 'Выбрать дату' }[v]), s.deadlineMode)}
        <input type="date" name="deadline" value="${dateValue}" ${s.deadlineMode === 'date' ? '' : 'hidden'}>
      </div>
      <div class="row">
        <div class="field"><span>Story Points</span>${segHtml('storyPoints', STORY_POINTS, (v) => `${v} SP`, s.storyPoints)}</div>
        <div class="field"><span>Оценка Pomodoro (по 25 мин)</span>
          <div class="stepper">
            <button type="button" class="icon-btn" data-step="-1" aria-label="Меньше">−</button>
            <output class="js-est">${s.est}</output>
            <button type="button" class="icon-btn" data-step="1" aria-label="Больше">+</button>
          </div>
        </div>
      </div>
      <div class="sheet-actions">
        ${existing ? '<button type="button" class="btn btn-danger" data-act="delete">Удалить</button>' : ''}
        <span class="spacer"></span>
        <button type="button" class="btn" data-act="cancel">Отмена</button>
        <button type="submit" class="btn btn-primary" data-act="save">Сохранить задачу</button>
      </div>
    </form>`, (root) => {
    const form = root.querySelector('form');
    const titleInput = form.elements.title;
    const saveBtn = form.querySelector('[data-act="save"]');
    const syncSave = () => { saveBtn.disabled = !titleInput.value.trim(); };
    syncSave();
    titleInput.addEventListener('input', syncSave);
    titleInput.focus();
    bindSegs(form, s, (name) => {
      if (name === 'deadlineMode') form.elements.deadline.hidden = s.deadlineMode !== 'date';
    });
    form.querySelectorAll('[data-step]').forEach((b) => b.addEventListener('click', () => {
      s.est = Math.min(12, Math.max(1, s.est + Number(b.dataset.step)));
      form.querySelector('.js-est').textContent = s.est;
    }));
    form.querySelector('[data-act="cancel"]').addEventListener('click', closeDialog);
    form.querySelector('[data-act="delete"]')?.addEventListener('click', () => { deleteTask(existing.id); closeDialog(); render(); });
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      if (!titleInput.value.trim()) return;
      let deadlineMillis = null;
      if (s.deadlineMode === 'sprint') deadlineMillis = sprintEnd;
      if (s.deadlineMode === 'date' && form.elements.deadline.value) deadlineMillis = fromInputDate(form.elements.deadline.value);
      saveTask(existing, {
        title: titleInput.value,
        description: form.elements.description.value,
        status: s.status,
        priority: s.priority,
        category: s.category,
        storyPoints: Number(s.storyPoints),
        deadlineMillis,
        estimatedPomodoros: s.est,
      });
      closeDialog();
      render();
    });
  });
}

function openSprintConfig() {
  const c = db.config;
  const s = { weeks: c.durationWeeks };
  openDialog(`
    <form class="sheet" novalidate>
      <h2>Параметры спринта</h2>
      <label class="field"><span>Название спринта</span><input name="name" maxlength="80" value="${esc(c.sprintName)}"></label>
      <label class="field"><span>Цель спринта</span><textarea name="goal" rows="2">${esc(c.sprintGoal)}</textarea></label>
      <div class="field"><span>Длительность</span>${segHtml('weeks', [1, 2], (v) => (v === 1 ? '1 неделя' : '2 недели'), s.weeks)}</div>
      <div class="field"><span>Дата начала</span>
        <div class="row" style="align-items:center">
          <input type="date" name="start" value="${toInputDate(c.startDateMillis)}" style="flex:1">
          <button type="button" class="btn btn-sm" data-act="today">Начать с сегодня</button>
        </div>
      </div>
      <div class="sheet-actions">
        <button type="button" class="btn" data-act="cancel">Отмена</button>
        <button type="submit" class="btn btn-primary">Применить</button>
      </div>
    </form>`, (root) => {
    const form = root.querySelector('form');
    bindSegs(form, s);
    form.querySelector('[data-act="today"]').addEventListener('click', () => { form.elements.start.value = toInputDate(Date.now()); });
    form.querySelector('[data-act="cancel"]').addEventListener('click', closeDialog);
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const start = form.elements.start.value ? fromInputDate(form.elements.start.value) : Date.now();
      saveSprintConfig(form.elements.name.value, form.elements.goal.value, Number(s.weeks), start);
      closeDialog();
      render();
    });
  });
}

function openTaskPicker() {
  const tasks = db.tasks.filter((t) => t.status !== 'DONE')
    .sort((a, b) => STATUSES.indexOf(a.status) - STATUSES.indexOf(b.status) || PRIORITY_WEIGHT[b.priority] - PRIORITY_WEIGHT[a.priority]);
  openDialog(`
    <div class="sheet">
      <h2>Выберите задачу для фокусировки</h2>
      <div class="picker">
        <button data-id="" class="${pomo.taskId == null ? 'selected' : ''}"><span class="t">${FREE_SESSION}</span></button>
        ${tasks.map((t) => `
          <button data-id="${t.id}" class="${pomo.taskId === t.id ? 'selected' : ''}">
            <span style="min-width:0"><span class="t" style="display:block">${esc(t.title)}</span><span class="muted" style="font-size:12px">${STATUS_LABEL[t.status]}</span></span>
            <span class="mono muted">${t.pomodorosCompleted}/${t.estimatedPomodoros}</span>
          </button>`).join('')}
      </div>
      <div class="sheet-actions"><button class="btn" data-act="cancel">Отмена</button></div>
    </div>`, (root) => {
    root.querySelector('[data-act="cancel"]').addEventListener('click', closeDialog);
    root.querySelectorAll('.picker button').forEach((b) => b.addEventListener('click', () => {
      selectTaskForFocus(b.dataset.id ? Number(b.dataset.id) : null);
      closeDialog();
      render();
    }));
  });
}

// ---------- Events ----------
function addTaskStatusFromFilter() { return ui.filter === 'IN_PROGRESS' || ui.filter === 'DONE' ? ui.filter : 'TODO'; }

document.addEventListener('click', (e) => {
  const nav = e.target.closest('.nav-item');
  if (nav) { ui.dest = nav.dataset.dest; render(); return; }
  if (e.target.closest('#fab')) { openTaskEditor(null, addTaskStatusFromFilter()); return; }
  const el = e.target.closest('[data-action]');
  if (!el || $dialog.contains(el)) return;
  const id = el.dataset.id ? Number(el.dataset.id) : null;
  switch (el.dataset.action) {
    case 'filter': ui.filter = el.dataset.value; render(); break;
    case 'toggle-view': ui.view = ui.view === 'COLUMNS' ? 'LIST' : 'COLUMNS'; render(); break;
    case 'edit-sprint': openSprintConfig(); break;
    case 'add-task': openTaskEditor(null, el.dataset.status); break;
    case 'edit-task': openTaskEditor(findTask(id)); break;
    case 'move': moveTask(id, el.dataset.status); render(); break;
    case 'focus-task': startFocusForTask(findTask(id)); render(); break;
    case 'open-focus': ui.dest = 'focus'; render(); break;
    case 'toggle-timer': toggleTimer(); render(); break;
    case 'reset': resetTimer(); render(); break;
    case 'skip': skipPhase(); render(); break;
    case 'phase': switchPhase(el.dataset.value); render(); break;
    case 'pick-task': openTaskPicker(); break;
    case 'move-in-progress': moveTask(id, 'IN_PROGRESS'); render(); break;
    case 'dismiss-banner': pomo.banner = null; savePomo(); render(); break;
  }
});

document.addEventListener('input', (e) => {
  if (e.target.id === 'search') { ui.query = e.target.value; renderBoard(); }
});

// ---------- Boot ----------
if (pomo.running) {
  // Resume a timer that kept counting while the page was closed.
  if (pomo.endAt && pomo.endAt > Date.now()) runTicker();
  else onPhaseFinished();
}
render();
