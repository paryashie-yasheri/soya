const GENRES = ['PSYCHEDELIC_ROCK', 'HIP_HOP', 'SOUL', 'POST_ROCK', 'BRIT_POP'];
const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const state = { page: 0, size: 10, totalPages: 0, totalElements: 0, search: null, filters: {}, localRows: null, localMode: false, busy: false };
let toastTimer;

function esc(value) {
  return String(value ?? '').replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[ch]);
}
function showToast(message, error = false) {
  const toast = $('#toast');
  toast.textContent = message;
  toast.classList.toggle('error', error);
  toast.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.remove('show'), 5200);
}
function errorText(payload, status) {
  if (payload && typeof payload === 'object') {
    const parts = [payload.code, payload.message || payload.error].filter(Boolean);
    if (parts.length) return `${status}: ${parts.join(' — ')}`;
  }
  return `${status}: ${typeof payload === 'string' && payload ? payload : 'Ошибка запроса к сервису'}`;
}
async function api(service, path, { method = 'GET', query = {}, body } = {}) {
  const params = new URLSearchParams({ service, path });
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') params.set(key, String(value));
  });
  const options = { method, headers: { Accept: 'application/json' } };
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }
  let response;
  try {
    response = await fetch(`api.php?${params}`, options);
  } catch (cause) {
    throw new Error(`Не удалось связаться с прокси: ${cause.message}`);
  }
  const text = await response.text();
  let data = null;
  if (text) {
    try { data = JSON.parse(text); } catch { data = text; }
  }
  if (!response.ok) throw new Error(errorText(data, response.status));
  return data;
}
function setBusy(busy) {
  state.busy = busy;
  $$('.button').forEach(button => { if (button.id !== 'closeDialog') button.disabled = busy; });
}
function fillGenres() {
  $$('select[name="genre"]').forEach(select => {
    const optional = select.name === 'genre' && select.closest('#filterForm');
    select.innerHTML = `${optional ? '<option value="">Любой</option>' : ''}${GENRES.map(g => `<option value="${g}">${g.replaceAll('_', ' ')}</option>`).join('')}`;
  });
}
function getFilterQuery() {
  const query = { page: state.page, size: state.size };
  if (state.filters.sortField) query.sort = `${state.filters.sortField},${state.filters.sortDirection || 'asc'}`;
  for (const key of ['id', 'name', 'coordinatesX', 'coordinatesY', 'creationDate', 'numberOfParticipants', 'genre', 'labelBands', 'labelSales']) {
    if (state.filters[key]) query[key] = key === 'creationDate'
      ? new Date(state.filters[key]).toISOString()
      : state.filters[key];
  }
  return query;
}
function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString('ru-RU');
}
function renderBands(page) {
  const rows = page?.content || [];
  const metadata = page?.metadata || { page: 0, size: state.size, totalElements: rows.length, totalPages: rows.length ? 1 : 0 };
  state.page = metadata.page;
  state.size = metadata.size;
  state.totalElements = metadata.totalElements;
  state.totalPages = metadata.totalPages;
  $('#tabCount').textContent = metadata.totalElements;
  $('#tableSummary').textContent = state.search ? `${rows.length} совпадений по запросу «${state.search}»` : `Найдено: ${metadata.totalElements}${state.localMode ? ' · фильтрация на клиенте' : ''}`;
  $('#pageLabel').textContent = `Страница ${metadata.page + 1} из ${Math.max(metadata.totalPages, 1)}`;
  $('#prevPage').disabled = state.busy || metadata.page <= 0;
  $('#nextPage').disabled = state.busy || metadata.page + 1 >= metadata.totalPages;
  if (!rows.length) {
    $('#bandsBody').innerHTML = '<tr><td colspan="8" class="empty">Группы не найдены.</td></tr>';
    return;
  }
  $('#bandsBody').innerHTML = rows.map(band => `<tr>
    <td>${esc(band.id)}</td><td class="band-name">${esc(band.name)}</td><td><span class="genre-pill">${esc(band.genre)}</span></td>
    <td>${esc(band.numberOfParticipants)}</td><td>${esc(band.coordinates?.x)}, ${esc(band.coordinates?.y)}</td>
    <td>${band.label ? `${esc(band.label.bands)} групп${band.label.sales == null ? '' : ` · ${esc(band.label.sales)} продаж`}` : '—'}</td><td>${esc(formatDate(band.creationDate))}</td>
    <td><div class="row-actions"><button class="row-button" data-action="details" data-id="${esc(band.id)}">Данные</button><button class="row-button" data-action="edit" data-id="${esc(band.id)}">Изменить</button><button class="row-button danger" data-action="delete" data-id="${esc(band.id)}">Удалить</button></div></td>
  </tr>`).join('');
}
async function loadBands() {
  state.search = null;
  $('#searchName').value = '';
  setBusy(true);
  try {
    if (state.localRows) {
      renderLocalPage();
      return;
    }
    const query = getFilterQuery();
    const activeFilters = ['id', 'name', 'coordinatesX', 'coordinatesY', 'creationDate', 'numberOfParticipants', 'genre', 'labelBands', 'labelSales']
      .some(key => state.filters[key] !== undefined && state.filters[key] !== '');
    if (activeFilters) {
      // Send the documented filtering request first. Some deployed versions of Bands currently
      // ignore @ModelAttribute filters, so fetch the collection and apply the same exact filters
      // locally to keep the UI result correct without changing the protected Bands service.
      await api('bands', '/music-bands', { query });
      const first = await api('bands', '/music-bands', { query: { page: 0, size: 100, sort: query.sort } });
      const pages = await Promise.all(Array.from({ length: Math.max(0, first.metadata.totalPages - 1) }, (_, index) =>
        api('bands', '/music-bands', { query: { page: index + 1, size: 100, sort: query.sort } })));
      state.localRows = [...first.content, ...pages.flatMap(item => item.content)].filter(row => matchesFilters(row, state.filters));
      sortRows(state.localRows, state.filters.sortField || 'id', state.filters.sortDirection || 'asc');
      state.localMode = true;
      renderLocalPage();
    } else {
      state.localRows = null;
      state.localMode = false;
      const data = await api('bands', '/music-bands', { query });
      renderBands(data);
    }
  } catch (error) {
    state.localRows = null;
    state.localMode = false;
    $('#bandsBody').innerHTML = `<tr><td colspan="8" class="empty">${esc(error.message)}</td></tr>`;
    $('#tableSummary').textContent = 'Не удалось загрузить список';
    showToast(error.message, true);
  } finally { setBusy(false); }
}
function matchesFilters(row, filters) {
  const exact = (filter, actual) => filter === undefined || filter === '' || String(actual) === String(filter);
  if (!exact(filters.id, row.id) || !exact(filters.name, row.name) || !exact(filters.genre, row.genre)
    || !exact(filters.numberOfParticipants, row.numberOfParticipants)
    || !exact(filters.coordinatesX, row.coordinates?.x) || !exact(filters.coordinatesY, row.coordinates?.y)
    || !exact(filters.labelBands, row.label?.bands) || !exact(filters.labelSales, row.label?.sales)) return false;
  if (filters.creationDate) {
    const desired = new Date(filters.creationDate).getTime();
    if (Number.isNaN(desired) || new Date(row.creationDate).getTime() !== desired) return false;
  }
  return true;
}
function sortRows(rows, field, direction) {
  const value = row => field === 'coordinatesX' ? row.coordinates?.x
    : field === 'coordinatesY' ? row.coordinates?.y
      : field === 'labelBands' ? row.label?.bands
        : field === 'labelSales' ? row.label?.sales
          : row[field];
  rows.sort((a, b) => {
    const left = value(a), right = value(b);
    if (left == null && right == null) return a.id - b.id;
    if (left == null) return direction === 'asc' ? 1 : -1;
    if (right == null) return direction === 'asc' ? -1 : 1;
    let compare = typeof left === 'number' ? left - right
      : field === 'creationDate' ? new Date(left) - new Date(right)
        : String(left).localeCompare(String(right), 'ru');
    if (compare === 0) compare = a.id - b.id;
    return direction === 'desc' ? -compare : compare;
  });
}
function renderLocalPage() {
  const rows = state.localRows || [];
  const pages = Math.ceil(rows.length / state.size);
  const content = rows.slice(state.page * state.size, (state.page + 1) * state.size);
  renderBands({ content, metadata: { page: state.page, size: state.size, totalElements: rows.length, totalPages: pages } });
}
async function searchBands(substring) {
  state.search = substring;
  state.page = 0;
  state.localRows = null;
  setBusy(true);
  try {
    const rows = await api('bands', '/music-bands/search-by-name', { query: { substring } });
    const pageSize = state.size;
    renderBands({ content: rows, metadata: { page: 0, size: rows.length || 1, totalElements: rows.length, totalPages: rows.length ? 1 : 0 } });
    state.size = pageSize;
  } catch (error) { showToast(error.message, true); }
  finally { setBusy(false); }
}
function currentBandPayload(form) {
  const data = new FormData(form);
  const payload = {
    name: String(data.get('name')).trim(), genre: data.get('genre'),
    numberOfParticipants: Number(data.get('numberOfParticipants')),
    coordinates: { x: Number(data.get('x')), y: Number(data.get('y')) },
  };
  const labelBands = data.get('labelBands');
  const labelSales = data.get('labelSales');
  if (labelBands !== '') payload.label = { bands: Number(labelBands), sales: labelSales === '' ? null : Number(labelSales) };
  return payload;
}
function resetBandForm() {
  $('#bandForm').reset();
  $('#bandForm [name="editId"]').value = '';
  $('#bandFormTitle').textContent = 'Добавить группу';
  $('#saveBand').textContent = 'Создать группу';
  $('#cancelEdit').classList.add('hidden');
}
async function startEdit(id) {
  try {
    const band = await api('bands', `/music-bands/${id}`);
    const form = $('#bandForm');
    for (const key of ['name', 'genre', 'numberOfParticipants']) form.elements[key].value = band[key];
    form.elements.x.value = band.coordinates.x;
    form.elements.y.value = band.coordinates.y;
    form.elements.labelBands.value = band.label?.bands ?? '';
    form.elements.labelSales.value = band.label?.sales ?? '';
    form.elements.editId.value = band.id;
    $('#bandFormTitle').textContent = `Изменить группу #${band.id}`;
    $('#saveBand').textContent = 'Сохранить изменения';
    $('#cancelEdit').classList.remove('hidden');
    form.scrollIntoView({ behavior: 'smooth', block: 'center' });
  } catch (error) { showToast(error.message, true); }
}
async function onBandAction(event) {
  const button = event.target.closest('[data-action]');
  if (!button) return;
  const { action, id } = button.dataset;
  try {
    if (action === 'details') {
      const band = await api('bands', `/music-bands/${id}`);
      $('#detailsContent').textContent = JSON.stringify(band, null, 2);
      $('#detailsDialog').showModal();
    } else if (action === 'edit') await startEdit(id);
    else if (action === 'delete' && confirm(`Удалить группу «${button.closest('tr').children[1].textContent}» (#${id})?`)) {
      await api('bands', `/music-bands/${id}`, { method: 'DELETE' });
      showToast(`Группа #${id} удалена`);
      state.localRows = null;
      if (state.page > 0 && state.page * state.size >= state.totalElements - 1) state.page -= 1;
      await loadBands();
    }
  } catch (error) { showToast(error.message, true); }
}
function loadHistory() {
  try { return JSON.parse(localStorage.getItem('grammy-history') || '[]'); } catch { return []; }
}
function saveHistory(entry) {
  const history = [entry, ...loadHistory()].slice(0, 40);
  localStorage.setItem('grammy-history', JSON.stringify(history));
  renderHistory();
}
function renderHistory() {
  const history = loadHistory();
  $('#historyBody').innerHTML = history.length ? history.map(row => `<tr><td>${esc(formatDate(row.time))}</td><td>${esc(row.operation)}</td><td>${esc(row.bandId)}</td><td><code>${esc(JSON.stringify(row.response))}</code></td></tr>`).join('') : '<tr><td colspan="4" class="empty">Операций пока нет.</td></tr>';
}
async function checkServices() {
  const results = await Promise.allSettled([
    api('bands', '/music-bands', { query: { page: 0, size: 1 } }),
    api('grammy', '/grammy/bands/1/singles/add', { method: 'GET' }),
  ]);
  const bandsOk = results[0].status === 'fulfilled';
  const grammyResult = results[1];
  const grammyOk = grammyResult.status === 'fulfilled' || /^(405|404):/.test(grammyResult.reason?.message || '');
  $('#statusDot').classList.toggle('ok', bandsOk && grammyOk);
  $('#statusDot').classList.toggle('bad', !bandsOk || !grammyOk);
  $('#statusText').textContent = `Bands ${bandsOk ? 'доступен' : 'недоступен'} · Grammy ${grammyOk ? 'доступен' : 'недоступен'}`;
}

function init() {
  fillGenres();
  renderHistory();
  loadBands();
  checkServices();
  $$('.tab').forEach(tab => tab.addEventListener('click', () => {
    $$('.tab').forEach(item => item.classList.toggle('active', item === tab));
    $$('.panel').forEach(panel => panel.classList.toggle('active', panel.id === `panel-${tab.dataset.tab}`));
  }));
  $('#reloadBands').addEventListener('click', () => { state.page = 0; state.localRows = null; loadBands(); });
  $('#filterForm').addEventListener('submit', event => {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    state.page = 0; state.size = Number(data.size); state.filters = data; state.localRows = null; loadBands();
  });
  $('#filterForm').addEventListener('reset', () => setTimeout(() => { state.filters = {}; state.page = 0; state.size = 10; state.localRows = null; loadBands(); }, 0));
  $('#searchForm').addEventListener('submit', event => { event.preventDefault(); searchBands($('#searchName').value.trim()); });
  $('#clearSearch').addEventListener('click', () => { $('#searchName').value = ''; state.page = 0; state.localRows = null; loadBands(); });
  $('#prevPage').addEventListener('click', () => { if (!state.search && state.page > 0) { state.page--; loadBands(); } });
  $('#nextPage').addEventListener('click', () => { if (!state.search && state.page + 1 < state.totalPages) { state.page++; loadBands(); } });
  $('#bandsBody').addEventListener('click', onBandAction);
  $('#bandForm').addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const editId = form.elements.editId.value;
    try {
      const result = await api('bands', editId ? `/music-bands/${editId}` : '/music-bands', { method: editId ? 'PUT' : 'POST', body: currentBandPayload(form) });
      showToast(editId ? `Группа #${editId} обновлена` : `Создана группа #${result.id}`);
      resetBandForm(); state.page = 0; state.localRows = null; await loadBands();
    } catch (error) { showToast(error.message, true); }
  });
  $('#cancelEdit').addEventListener('click', resetBandForm);
  $('#averageButton').addEventListener('click', async () => {
    try { const result = await api('bands', '/music-bands/average-number-of-participants'); $('#metricResult').textContent = `Среднее: ${result.average}`; }
    catch (error) { showToast(error.message, true); }
  });
  $('#countButton').addEventListener('click', async () => {
    try { const value = Number($('#countValue').value); const result = await api('bands', '/music-bands/count-by-number-of-participants', { query: { value } }); $('#metricResult').textContent = `Найдено: ${result.count}`; }
    catch (error) { showToast(error.message, true); }
  });
  $('#singleForm').addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form); const bandId = data.get('bandId');
    try {
      const result = await api('grammy', `/grammy/bands/${bandId}/singles/add`, { method: 'POST', query: { 'single-name': String(data.get('singleName')).trim() } });
      saveHistory({ time: new Date().toISOString(), operation: 'Добавление сингла', bandId, response: result });
      showToast(`Сингл добавлен для группы #${bandId}`); form.reset();
    } catch (error) { saveHistory({ time: new Date().toISOString(), operation: 'Добавление сингла · ошибка', bandId, response: error.message }); showToast(error.message, true); }
  });
  $('#nominationForm').addEventListener('submit', async event => {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form); const bandId = data.get('bandId'); const genre = data.get('genre');
    try {
      const result = await api('grammy', `/grammy/bands/${bandId}/nominate/${encodeURIComponent(genre)}`, { method: 'POST' });
      saveHistory({ time: new Date().toISOString(), operation: 'Номинация Grammy', bandId, response: result });
      showToast(`Группа #${bandId} номинирована`); form.reset();
    } catch (error) { saveHistory({ time: new Date().toISOString(), operation: 'Номинация Grammy · ошибка', bandId, response: error.message }); showToast(error.message, true); }
  });
  $('#clearHistory').addEventListener('click', () => { localStorage.removeItem('grammy-history'); renderHistory(); });
  $('#closeDialog').addEventListener('click', () => $('#detailsDialog').close());
}
document.addEventListener('DOMContentLoaded', init);
