const API_BASE = '/api';
let shows = [];
let activeShowByPath = {};
let selectedGroup = null;
let fixtureGroups = [];
let activeTestView = null;

// Cargar grupos disponibles
async function loadFixtureGroups() {
    try {
        const response = await fetch(API_BASE + '/fixture-groups');
        const data = await response.json();
        fixtureGroups = data.groups || [];
        renderFixtureGroups(fixtureGroups);
    } catch (error) {
        document.getElementById('groupButtons').innerHTML = 
            '<div class="error">Error al cargar grupos: ' + error.message + '</div>';
    }
}

function renderFixtureGroups(groups) {
    const container = document.getElementById('groupButtons');
    let html = '';
    groups.forEach(group => {
        html += '<button class="group-btn" onclick="selectGroup(\'' + group.id + '\')">' + 
            group.displayName + '</button>';
    });
    container.innerHTML = html;
}

function selectGroup(groupId) {
    selectedGroup = groupId;
    document.getElementById('groupSelection').classList.add('hidden');
    document.getElementById('testEntry').classList.add('hidden');
    document.getElementById('mainContent').classList.remove('hidden');

    // Mostrar/ocultar sección BeeEye solo para el grupo bee-eyes
    const beeeyeSection = document.getElementById('beeeyeSection');
    if (groupId === 'bee-eyes') {
        beeeyeSection.classList.remove('hidden');
        loadBeeEyeStatus();
    } else {
        beeeyeSection.classList.add('hidden');
    }

    // Actualizar botones de grupo
    const selectedGroupData = fixtureGroups.find(g => g.id === groupId);
    document.querySelectorAll('.group-btn').forEach(btn => {
        btn.classList.remove('selected');
        if (selectedGroupData && btn.textContent === selectedGroupData.displayName) {
            btn.classList.add('selected');
        }
    });

    loadShows();
}

function hideAllTestViews() {
    document.getElementById('testChaseView').classList.add('hidden');
    document.getElementById('testVdjView').classList.add('hidden');
    document.getElementById('testFixtureConfigView').classList.add('hidden');
}

function openTestChase() {
    activeTestView = 'chase';
    document.getElementById('groupSelection').classList.add('hidden');
    document.getElementById('testEntry').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
    document.getElementById('testContent').classList.remove('hidden');
    hideAllTestViews();
    document.getElementById('testChaseView').classList.remove('hidden');
}

function openTestVirtualDJ() {
    activeTestView = 'vdj';
    document.getElementById('groupSelection').classList.add('hidden');
    document.getElementById('testEntry').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
    document.getElementById('testContent').classList.remove('hidden');
    hideAllTestViews();
    document.getElementById('testVdjView').classList.remove('hidden');
    loadVirtualDJStatus();
}

function openFixtureConfig() {
    activeTestView = 'fixture-config';
    document.getElementById('groupSelection').classList.add('hidden');
    document.getElementById('testEntry').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
    document.getElementById('testContent').classList.remove('hidden');
    hideAllTestViews();
    document.getElementById('testFixtureConfigView').classList.remove('hidden');
    loadFixtures();
}

let maxUniverses = 2;

async function loadFixtures() {
    try {
        const response = await fetch(API_BASE + '/fixtures');
        const data = await response.json();
        maxUniverses = data.maxUniverses || 2;
        document.getElementById('maxUniverses').value = maxUniverses;
        renderFixtures(data.fixtures || []);
    } catch (error) {
        document.getElementById('fixturesList').innerHTML = 
            '<div class="error">Error al cargar fixtures: ' + error.message + '</div>';
    }
}

async function toggleFixture(id, isOn) {
    const toggle = document.getElementById('power-' + id);
    toggle.disabled = true;

    try {
        const endpoint = isOn ? '/turn-on' : '/turn-off';
        const response = await fetch(API_BASE + '/fixtures/' + id + endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        const result = await response.json();
        if (result.status !== 'ok') {
            // Revertir el toggle si hay error
            toggle.checked = !isOn;
            alert('Error: ' + (result.error || 'Error desconocido'));
        }
    } catch (error) {
        // Revertir el toggle si hay error
        toggle.checked = !isOn;
        alert('Error: ' + error.message);
    } finally {
        toggle.disabled = false;
    }
}

async function saveMaxUniverses() {
    const maxUniversesInput = document.getElementById('maxUniverses');
    const value = parseInt(maxUniversesInput.value);

    if (isNaN(value) || value < 1 || value > 15) {
        alert('Por favor ingrese un valor válido entre 1 y 15');
        return;
    }

    try {
        const response = await fetch(API_BASE + '/fixtures/config/update', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ maxUniverses: value })
        });

        const result = await response.json();
        if (result.status === 'ok') {
            maxUniverses = value;
            if (result.reloaded) {
                alert('Máximo de universos actualizado y recargado correctamente.');
            } else {
                alert('Máximo de universos actualizado. Los cambios se aplicarán al reiniciar.');
            }
            // Actualizar los límites de los inputs
            document.querySelectorAll('input[id^="universe-"]').forEach(input => {
                input.setAttribute('max', maxUniverses);
            });
        } else {
            alert('Error al actualizar: ' + (result.error || 'Error desconocido'));
        }
    } catch (error) {
        alert('Error al guardar: ' + error.message);
    }
}

function renderFixtures(fixtures) {
    const container = document.getElementById('fixturesList');
    if (fixtures.length === 0) {
        container.innerHTML = '<div class="loading">No hay fixtures configurados</div>';
        return;
    }

    let html = '';
    fixtures.forEach(fixture => {
        html += '<div class="fixture-item">';
        html += '<div class="fixture-header">' + fixture.name + ' (ID: ' + fixture.id + ')</div>';
        html += '<div class="fixture-form">';
        html += '<label>Universe:</label>';
        html += '<input type="number" id="universe-' + fixture.id + '" value="' + fixture.universe + '" min="1" max="' + maxUniverses + '">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>Dirección DMX:</label>';
        html += '<input type="number" id="address-' + fixture.id + '" value="' + fixture.address + '" min="1" max="512">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>Tipo:</label>';
        html += '<input type="text" value="' + fixture.type + '" disabled style="background: #333; color: #888;">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>Modelo:</label>';
        html += '<input type="text" value="' + fixture.model + '" disabled style="background: #333; color: #888;">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>Activo:</label>';
        html += '<input type="checkbox" id="activo-' + fixture.id + '" ' + (fixture.activo ? 'checked' : '') + ' style="width: auto; height: 20px;">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>ID Escena Encendido:</label>';
        html += '<input type="number" id="sceneIdOn-' + fixture.id + '" value="' + (fixture.sceneIdOn || '') + '" min="0" placeholder="Opcional">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>ID Escena Apagado:</label>';
        html += '<input type="number" id="sceneIdOff-' + fixture.id + '" value="' + (fixture.sceneIdOff || '') + '" min="0" placeholder="Opcional">';
        html += '</div>';
        html += '<div class="fixture-form">';
        html += '<label>Encendido:</label>';
        html += '<label class="toggle-switch">';
        html += '<input type="checkbox" id="power-' + fixture.id + '" onchange="toggleFixture(' + fixture.id + ', this.checked)">';
        html += '<span class="toggle-slider"></span>';
        html += '</label>';
        html += '</div>';
        html += '<button class="save-btn" onclick="saveFixture(' + fixture.id + ')">Guardar cambios</button>';
        html += '</div>';
    });
    container.innerHTML = html;
}

async function saveFixture(id) {
    const universeInput = document.getElementById('universe-' + id);
    const addressInput = document.getElementById('address-' + id);
    const activoInput = document.getElementById('activo-' + id);
    const sceneIdOnInput = document.getElementById('sceneIdOn-' + id);
    const sceneIdOffInput = document.getElementById('sceneIdOff-' + id);

    const universe = parseInt(universeInput.value);
    const address = parseInt(addressInput.value);
    const activo = activoInput.checked;
    const sceneIdOnValue = sceneIdOnInput.value.trim();
    const sceneIdOn = sceneIdOnValue === '' ? null : parseInt(sceneIdOnValue);
    const sceneIdOffValue = sceneIdOffInput.value.trim();
    const sceneIdOff = sceneIdOffValue === '' ? null : parseInt(sceneIdOffValue);

    if (isNaN(universe) || isNaN(address)) {
        alert('Por favor ingrese valores válidos');
        return;
    }

    if (sceneIdOnValue !== '' && (isNaN(sceneIdOn) || sceneIdOn < 0)) {
        alert('El ID de escena encendido debe ser un número válido o estar vacío');
        return;
    }

    if (sceneIdOffValue !== '' && (isNaN(sceneIdOff) || sceneIdOff < 0)) {
        alert('El ID de escena apagado debe ser un número válido o estar vacío');
        return;
    }

    try {
        // Obtener fixture actual
        const getResponse = await fetch(API_BASE + '/fixtures/' + id);
        const fixture = await getResponse.json();

        // Actualizar valores
        fixture.universe = universe;
        fixture.address = address;
        fixture.activo = activo;
        fixture.sceneIdOn = sceneIdOn;
        fixture.sceneIdOff = sceneIdOff;

        // Enviar actualización
        const response = await fetch(API_BASE + '/fixtures/' + id + '/update', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(fixture)
        });

        const result = await response.json();
        if (result.status === 'ok') {
            if (result.reloaded) {
                alert('Fixture actualizado y recargado correctamente.');
            } else {
                alert('Fixture actualizado correctamente. Los cambios se aplicarán al reiniciar.');
            }
        } else {
            alert('Error al actualizar: ' + (result.error || 'Error desconocido'));
        }
    } catch (error) {
        alert('Error al guardar: ' + error.message);
    }
}

function goHome() {
    document.getElementById('testContent').classList.add('hidden');
    document.getElementById('mainContent').classList.add('hidden');
    document.getElementById('groupSelection').classList.remove('hidden');
    document.getElementById('testEntry').classList.remove('hidden');
    document.getElementById('beeeyeSection').classList.add('hidden');
    selectedGroup = null;
    activeTestView = null;
}

document.getElementById('backBtn').addEventListener('click', () => {
    document.getElementById('groupSelection').classList.remove('hidden');
    document.getElementById('testEntry').classList.remove('hidden');
    document.getElementById('mainContent').classList.add('hidden');
    document.getElementById('beeeyeSection').classList.add('hidden');
    selectedGroup = null;
});
document.getElementById('testBackBtn').addEventListener('click', goHome);

async function loadShows() {
    try {
        const url = selectedGroup ? 
            API_BASE + '/shows?group=' + encodeURIComponent(selectedGroup) : 
            API_BASE + '/shows';
        const response = await fetch(url);
        const data = await response.json();
        shows = data.shows || [];
        renderShows();
    } catch (error) {
        document.getElementById('content').innerHTML = 
            '<div class="error">Error al cargar efectos: ' + error.message + '</div>';
    }
}

async function loadVirtualDJStatus() {
    try {
        const resp = await fetch(API_BASE + '/virtualdj/status');
        const data = await resp.json();
        renderVirtualDJStatus(data);
    } catch (e) {
        const el = document.getElementById('vdjStatus');
        if (el) el.innerHTML = '<div class="error">Error: ' + e.message + '</div>';
    }
}

function renderVirtualDJStatus(data) {
    const el = document.getElementById('vdjStatus');
    if (!el) return;
    const connected = data.connected ? 'SI' : 'NO';
    const listening = data.listening ? 'SI' : 'NO';
    const err = data.bindError ? data.bindError : '-';
    const os2lDirectIp = (data.localAddresses && data.localAddresses.length) ? data.localAddresses.join('<br>') : '-';
    const ip = data.lastRemoteIp ? data.lastRemoteIp : '-';
    const ago = (data.lastSeenAgoMs != null) ? (Math.round(data.lastSeenAgoMs/1000) + 's') : '-';
    const bpm = (data.lastBeat && data.lastBeat.bpm != null) ? data.lastBeat.bpm : '-';
    const pos = (data.lastBeat && data.lastBeat.pos != null) ? data.lastBeat.pos : '-';

    el.innerHTML = '' +
      '<div class="kv">' +
        '<div class="k">os2lDirectIp en VirtualDJ</div><div class="v">' + os2lDirectIp + '</div>' +
        '<div class="k">OS2L port</div><div class="v">' + data.os2lPort + '</div>' +
        '<div class="k">Servidor escuchando</div><div class="v">' + listening + '</div>' +
        '<div class="k">Error puerto</div><div class="v">' + err + '</div>' +
        '<div class="k">Conectado</div><div class="v">' + connected + '</div>' +
        '<div class="k">IP VirtualDJ</div><div class="v">' + ip + '</div>' +
        '<div class="k">Último evento</div><div class="v">' + ago + '</div>' +
        '<div class="k">Último BPM</div><div class="v">' + bpm + '</div>' +
        '<div class="k">Última posición</div><div class="v">' + pos + '</div>' +
      '</div>';
}

function renderShows() {
    const content = document.getElementById('content');

    // Agrupar por path
    const grouped = {};
    shows.forEach(show => {
        const path = show.path || 'Sin categoría';
        if (!grouped[path]) grouped[path] = [];
        grouped[path].push(show);
    });

    // Ordenar paths: 'Moving Head Bee Eye Dimmer' primero
    const paths = Object.keys(grouped).sort((a, b) => {
        if (a.includes('Dimmer')) return -1;
        if (b.includes('Dimmer')) return 1;
        return a.localeCompare(b);
    });

    let html = '';
    paths.forEach(path => {
        html += '<div class="group">';
        html += '<div class="group-title">' + path + '</div>';
        html += '<div class="buttons-grid">';

        grouped[path].forEach(show => {
            const isActive = show.executing;
            if (isActive) activeShowByPath[path] = show.id;
            html += '<button class="effect-btn' + (isActive ? ' active' : '') + '" ' +
                    'data-show-id="' + show.id + '" ' +
                    'data-path="' + path + '" ' +
                    'onclick="toggleShow(' + show.id + ', \'' + path + '\')">' +
                '<div class="effect-id">' + show.id + '</div>' +
                '<div class="effect-name">' + show.name + '</div>' +
            '</button>';
        });

        html += '</div></div>';
    });

    content.innerHTML = html;
}

async function toggleShow(id, path) {
    try {
        // Desactivar otros shows del mismo path
        if (activeShowByPath[path]) {
            const otherId = activeShowByPath[path];
            if (otherId !== id) {
                await fetch(API_BASE + '/shows/' + otherId + '/toggle', { method: 'POST' });
                updateButtonState(otherId, false);
            }
        }

        // Toggle del show seleccionado
        const response = await fetch(API_BASE + '/shows/' + id + '/toggle', { method: 'POST' });
        const data = await response.json();

        updateButtonState(id, data.executing);
        if (data.executing) {
            activeShowByPath[path] = id;
        } else {
            delete activeShowByPath[path];
        }
    } catch (error) {
        console.error('Error toggling show:', error);
    }
}

function updateButtonState(id, isActive) {
    const btn = document.querySelector('[data-show-id="' + id + '"]');
    if (btn) {
        if (isActive) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    }
}

async function blackout() {
    try {
        await fetch(API_BASE + '/beeeye/blackout', { method: 'POST' });
        // Recargar estado
        await loadShows();
    } catch (error) {
        console.error('Error executing blackout:', error);
    }
}

document.getElementById('blackoutBtn').addEventListener('click', blackout);

// Funciones para BeeEye
async function loadBeeEyeStatus() {
    try {
        const response = await fetch(API_BASE + '/beeeye/status');
        const data = await response.json();
        renderBeeEyes(data.beeEyes || []);
    } catch (error) {
        console.error('Error loading BeeEye status:', error);
    }
}

function renderBeeEyes(beeEyes) {
    const grid = document.getElementById('beeeyeGrid');
    if (beeEyes.length === 0) {
        grid.innerHTML = '<div class="loading">No hay BeeEye configurados</div>';
        return;
    }

    let html = '';
    beeEyes.forEach(beeEye => {
        html += '<div class="beeeye-container">';
        html += '<div class="beeeye-name">' + beeEye.name + '</div>';
        html += drawBeeEyeSVG(beeEye);
        html += '</div>';
    });
    grid.innerHTML = html;
}

function drawBeeEyeSVG(beeEye) {
    const size = 200;
    const cx = size / 2;
    const cy = size / 2;
    const ringRadius = size * 0.35;
    const ledRadius = size * 0.15;
    const centerRadius = size * 0.18;

    let svg = '<svg class="beeeye-svg" viewBox="0 0 ' + size + ' ' + size + '">';

    // Fondo circular (bezel)
    svg += '<circle cx="' + cx + '" cy="' + cy + '" r="' + (size * 0.45) + '" fill="#1e1e1e"/>';

    // LEDs del anillo (6)
    for (let i = 0; i < 6; i++) {
        const angle = -Math.PI / 2 + i * (Math.PI / 3);
        const x = cx + ringRadius * Math.cos(angle);
        const y = cy + ringRadius * Math.sin(angle);
        const led = beeEye.leds[i];
        const color = 'rgb(' + led.r + ',' + led.g + ',' + led.b + ')';
        svg += drawHexagon(x, y, ledRadius, color);
    }

    // LED central (índice 6)
    const centerLed = beeEye.leds[6];
    const centerColor = 'rgb(' + centerLed.r + ',' + centerLed.g + ',' + centerLed.b + ')';
    svg += drawHexagon(cx, cy, centerRadius, centerColor);

    svg += '</svg>';
    return svg;
}

function drawHexagon(cx, cy, radius, fillColor) {
    let points = '';
    for (let i = 0; i < 6; i++) {
        const angle = -Math.PI / 2 + i * (Math.PI / 3);
        const x = cx + radius * Math.cos(angle);
        const y = cy + radius * Math.sin(angle);
        points += (i > 0 ? ' ' : '') + x + ',' + y;
    }
    return '<polygon points="' + points + '" fill="' + fillColor + '" stroke="rgba(0,0,0,0.6)" stroke-width="2"/>';
}

// Cargar grupos al iniciar
loadFixtureGroups();

// Actualizar estado cada 500ms para BeeEye solo si está visible
setInterval(() => {
    if (selectedGroup === 'bee-eyes') {
        loadBeeEyeStatus();
    }
}, 500);
// Estado VirtualDJ: refrescar solo si estamos en la vista correspondiente
setInterval(() => {
    if (activeTestView === 'vdj') {
        loadVirtualDJStatus();
    }
}, 1000);

// Actualizar estado cada 2 segundos para shows (solo si hay grupo seleccionado)
setInterval(() => {
    if (selectedGroup) {
        loadShows();
    }
}, 2000);
