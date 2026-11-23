const urlParams = new URLSearchParams(window.location.search);
const sceneId = urlParams.get('id');
let sceneData = null;
let fixtures = [];
let selectedFixture = null;
let isDragging = false;
let dragOffset = { x: 0, y: 0 };

const canvas = document.getElementById('positionCanvas');
const ctx = canvas.getContext('2d');
let currentIncrement = 1; // Incremento en coordenadas DMX (0-65535)
let invertPan = false; // Estado de inversión de Pan
let invertTilt = false; // Estado de inversión de Tilt

// Ajustar tamaño del canvas
function resizeCanvas() {
    const wrapper = canvas.parentElement;
    const maxSize = Math.min(800, wrapper.clientWidth - 40);
    canvas.width = maxSize;
    canvas.height = maxSize;
    drawCanvas();
}

function setIncrement(value) {
    currentIncrement = value;
    // Actualizar botones activos
    document.querySelectorAll('.increment-btn').forEach(btn => {
        if (parseInt(btn.dataset.increment) === value) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

function toggleInvert(type) {
    if (type === 'pan') {
        invertPan = !invertPan;
        const btn = document.getElementById('toggleInvertPan');
        if (invertPan) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    } else if (type === 'tilt') {
        invertTilt = !invertTilt;
        const btn = document.getElementById('toggleInvertTilt');
        if (invertTilt) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    }
}

let sendToArtNetTimeout = null;

async function sendToArtNetRealTime(fixture) {
    if (!fixture || !sceneId) return;

    // Debounce: cancelar envío anterior si existe
    if (sendToArtNetTimeout) {
        clearTimeout(sendToArtNetTimeout);
    }

    // Enviar después de un pequeño delay para evitar saturar la red
    sendToArtNetTimeout = setTimeout(async () => {
        try {
            const response = await fetch(`/api/scenes/${sceneId}/send-point`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    fixtureId: fixture.fixtureId,
                    pan: fixture.pan,
                    panFine: fixture.panFine,
                    tilt: fixture.tilt,
                    tiltFine: fixture.tiltFine
                })
            });

            if (!response.ok) {
                console.warn('Error enviando a ArtNet:', await response.text());
            }
        } catch (error) {
            console.error('Error enviando a ArtNet:', error);
        }
    }, 50); // 50ms de debounce
}

function moveFixture(direction) {
    if (!selectedFixture) {
        alert('Por favor selecciona un fixture primero');
        return;
    }

    if (selectedFixture.x === null || selectedFixture.x === undefined || 
        selectedFixture.y === null || selectedFixture.y === undefined) {
        alert('El fixture seleccionado no tiene coordenadas válidas');
        return;
    }

    // Obtener valores DMX actuales
    let currentDmxX = selectedFixture.x;
    let currentDmxY = selectedFixture.y;

    // Calcular nueva posición según dirección (incremento en coordenadas DMX)
    // Aplicar inversión si está activa
    let actualDirection = direction;
    if (direction === 'left' || direction === 'right') {
        if (invertPan) {
            actualDirection = direction === 'left' ? 'right' : 'left';
        }
    } else if (direction === 'up' || direction === 'down') {
        if (invertTilt) {
            actualDirection = direction === 'up' ? 'down' : 'up';
        }
    }

    let newDmxX = currentDmxX;
    let newDmxY = currentDmxY;

    switch(actualDirection) {
        case 'up':
            newDmxY = Math.max(0, currentDmxY - currentIncrement);
            break;
        case 'down':
            newDmxY = Math.min(MAX_DMX, currentDmxY + currentIncrement);
            break;
        case 'left':
            newDmxX = Math.max(0, currentDmxX - currentIncrement);
            break;
        case 'right':
            newDmxX = Math.min(MAX_DMX, currentDmxX + currentIncrement);
            break;
    }

    // Usar los valores DMX directamente
    const dmxX = newDmxX;
    const dmxY = newDmxY;

    // Actualizar valores del fixture
    const panData = dmxToPanTilt(dmxX);
    const tiltData = dmxToPanTilt(dmxY);

    selectedFixture.pan = panData.coarse;
    selectedFixture.panFine = panData.fine;
    selectedFixture.tilt = tiltData.coarse;
    selectedFixture.tiltFine = tiltData.fine;
    selectedFixture.x = dmxX;
    selectedFixture.y = dmxY;

    drawCanvas();
    renderTable(fixtures);
    updateFixtureInfo(selectedFixture);

    // Enviar a ArtNet en tiempo real
    sendToArtNetRealTime(selectedFixture);
}

function updateDpadButtons() {
    const hasSelection = selectedFixture !== null;
    document.getElementById('btnUp').disabled = !hasSelection;
    document.getElementById('btnDown').disabled = !hasSelection;
    document.getElementById('btnLeft').disabled = !hasSelection;
    document.getElementById('btnRight').disabled = !hasSelection;
}

function selectFixtureFromRadio(fixtureId) {
    const fixture = fixtures.find(f => f.fixtureId === fixtureId);
    if (fixture) {
        selectedFixture = fixture;
        updateFixtureInfo(fixture);
        updateDpadButtons();
        updateRadioSelection();
        drawCanvas();
    }
}

function updateRadioSelection() {
    document.querySelectorAll('.radio-item').forEach(item => {
        const fixtureId = parseInt(item.dataset.fixtureId);
        if (selectedFixture && selectedFixture.fixtureId === fixtureId) {
            item.classList.add('selected');
            item.querySelector('input[type="radio"]').checked = true;
        } else {
            item.classList.remove('selected');
            item.querySelector('input[type="radio"]').checked = false;
        }
    });
}

function renderFixtureRadioList() {
    const container = document.getElementById('fixtureRadioList');
    if (!fixtures || fixtures.length === 0) {
        container.innerHTML = '<div class="empty">No hay fixtures disponibles</div>';
        return;
    }

    container.innerHTML = fixtures.map(fixture => {
        const fixtureName = fixture.fixtureName || `Fixture ${fixture.fixtureId}`;
        return `
            <div class="radio-item" data-fixture-id="${fixture.fixtureId}" onclick="selectFixtureFromRadio(${fixture.fixtureId})">
                <input type="radio" name="fixtureSelect" id="fixture_${fixture.fixtureId}" value="${fixture.fixtureId}" onchange="selectFixtureFromRadio(${fixture.fixtureId})">
                <label for="fixture_${fixture.fixtureId}">
                    <strong>ID ${fixture.fixtureId}:</strong> ${fixtureName}
                </label>
            </div>
        `;
    }).join('');

    updateRadioSelection();
}

// Rango de valores DMX: 0-65535 (pan*256 + panFine)
const MAX_DMX = 65535;

// Convertir coordenada DMX a posición en canvas
function dmxToCanvas(dmxValue) {
    return (dmxValue / MAX_DMX) * canvas.width;
}

// Convertir posición en canvas a coordenada DMX
function canvasToDmx(canvasPos) {
    return Math.round((canvasPos / canvas.width) * MAX_DMX);
}

// Convertir DMX a pan/panFine o tilt/tiltFine
function dmxToPanTilt(dmxValue) {
    const coarse = Math.floor(dmxValue / 256);
    const fine = dmxValue % 256;
    return { coarse: coarse, fine: fine };
}

// Convertir pan/panFine o tilt/tiltFine a DMX
function panTiltToDmx(coarse, fine) {
    return coarse * 256 + fine;
}

function drawCanvas() {
    // Limpiar canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // Dibujar cuadrícula
    ctx.strokeStyle = '#e0e0e0';
    ctx.lineWidth = 1;
    const gridSize = canvas.width / 10;
    for (let i = 0; i <= 10; i++) {
        const pos = i * gridSize;
        // Líneas verticales
        ctx.beginPath();
        ctx.moveTo(pos, 0);
        ctx.lineTo(pos, canvas.height);
        ctx.stroke();
        // Líneas horizontales
        ctx.beginPath();
        ctx.moveTo(0, pos);
        ctx.lineTo(canvas.width, pos);
        ctx.stroke();
    }

    // Dibujar ejes centrales
    ctx.strokeStyle = '#999';
    ctx.lineWidth = 2;
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    ctx.beginPath();
    ctx.moveTo(centerX, 0);
    ctx.lineTo(centerX, canvas.height);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(0, centerY);
    ctx.lineTo(canvas.width, centerY);
    ctx.stroke();

    // Dibujar fixtures
    fixtures.forEach(fixture => {
        if (fixture.x === null || fixture.x === undefined || 
            fixture.y === null || fixture.y === undefined) return;

        const x = dmxToCanvas(fixture.x);
        const y = dmxToCanvas(fixture.y);

        const isSelected = selectedFixture && selectedFixture.fixtureId === fixture.fixtureId;

        // Círculo del fixture
        ctx.fillStyle = isSelected ? '#ff6b6b' : '#667eea';
        ctx.beginPath();
        ctx.arc(x, y, isSelected ? 12 : 8, 0, Math.PI * 2);
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 2;
        ctx.stroke();

        // Etiqueta con ID del fixture
        ctx.fillStyle = 'white';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(fixture.fixtureId.toString(), x, y);
    });
}

// Obtener fixture en una posición del canvas
function getFixtureAt(x, y) {
    const radius = 15;
    for (let fixture of fixtures) {
        if (fixture.x === null || fixture.x === undefined || 
            fixture.y === null || fixture.y === undefined) continue;

        const fx = dmxToCanvas(fixture.x);
        const fy = dmxToCanvas(fixture.y);

        const dx = x - fx;
        const dy = y - fy;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= radius) {
            return fixture;
        }
    }
    return null;
}

// Eventos del canvas
canvas.addEventListener('mousedown', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const fixture = getFixtureAt(x, y);
    if (fixture) {
        selectedFixture = fixture;
        isDragging = true;
        dragOffset.x = x - dmxToCanvas(fixture.x);
        dragOffset.y = y - dmxToCanvas(fixture.y);
        canvas.style.cursor = 'grabbing';
        updateFixtureInfo(fixture);
        updateDpadButtons();
        updateRadioSelection();
        drawCanvas();
    }
});

canvas.addEventListener('mousemove', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    if (isDragging && selectedFixture) {
        // Calcular nueva posición
        const newX = Math.max(0, Math.min(canvas.width, x - dragOffset.x));
        const newY = Math.max(0, Math.min(canvas.height, y - dragOffset.y));

        // Convertir a valores DMX
        const dmxX = canvasToDmx(newX);
        const dmxY = canvasToDmx(newY);

        // Actualizar valores del fixture
        const panData = dmxToPanTilt(dmxX);
        const tiltData = dmxToPanTilt(dmxY);

        selectedFixture.pan = panData.coarse;
        selectedFixture.panFine = panData.fine;
        selectedFixture.tilt = tiltData.coarse;
        selectedFixture.tiltFine = tiltData.fine;
        selectedFixture.x = dmxX;
        selectedFixture.y = dmxY;

        drawCanvas();
        renderTable(fixtures);
        updateFixtureInfo(selectedFixture);

        // Enviar a ArtNet en tiempo real mientras se arrastra
        sendToArtNetRealTime(selectedFixture);
    } else {
        const fixture = getFixtureAt(x, y);
        canvas.style.cursor = fixture ? 'grab' : 'crosshair';
    }
});

canvas.addEventListener('mouseup', () => {
    isDragging = false;
    canvas.style.cursor = 'crosshair';
});

canvas.addEventListener('mouseleave', () => {
    isDragging = false;
    canvas.style.cursor = 'crosshair';
});

function updateFixtureInfo(fixture) {
    if (!fixture) return;

    const pan = fixture.pan !== null && fixture.pan !== undefined ? fixture.pan : '-';
    const panFine = fixture.panFine !== null && fixture.panFine !== undefined ? fixture.panFine : '-';
    const tilt = fixture.tilt !== null && fixture.tilt !== undefined ? fixture.tilt : '-';
    const tiltFine = fixture.tiltFine !== null && fixture.tiltFine !== undefined ? fixture.tiltFine : '-';
    const x = fixture.x !== null && fixture.x !== undefined ? fixture.x : '-';
    const y = fixture.y !== null && fixture.y !== undefined ? fixture.y : '-';

    document.getElementById('selectedFixtureInfo').innerHTML = `
        <div class="info-item">
            <strong>Fixture:</strong> ${fixture.fixtureId}${fixture.fixtureName ? ' - ' + fixture.fixtureName : ''}
        </div>
        <div class="info-item">
            <strong>Pan:</strong> ${pan} | <strong>Pan Fine:</strong> ${panFine}
        </div>
        <div class="info-item">
            <strong>Tilt:</strong> ${tilt} | <strong>Tilt Fine:</strong> ${tiltFine}
        </div>
        <div class="info-item">
            <strong>X:</strong> ${x} | <strong>Y:</strong> ${y}
        </div>
    `;
}

function resetView() {
    selectedFixture = null;
    updateDpadButtons();
    updateRadioSelection();
    drawCanvas();
    document.getElementById('selectedFixtureInfo').innerHTML = 
        '<div class="info-item"><p>Selecciona un fixture en el canvas para ver sus detalles</p></div>';
}

function centerAll() {
    const centerDmx = Math.floor(MAX_DMX / 2);
    fixtures.forEach(fixture => {
        const panData = dmxToPanTilt(centerDmx);
        const tiltData = dmxToPanTilt(centerDmx);
        fixture.pan = panData.coarse;
        fixture.panFine = panData.fine;
        fixture.tilt = tiltData.coarse;
        fixture.tiltFine = tiltData.fine;
        fixture.x = centerDmx;
        fixture.y = centerDmx;
    });
    drawCanvas();
    renderTable(fixtures);
}

async function savePositions() {
    try {
        const response = await fetch(`/api/scenes/${sceneId}/update-positions`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fixtures: fixtures })
        });

        const result = await response.json();

        if (response.ok) {
            alert('Posiciones guardadas correctamente');
        } else {
            alert('Error: ' + (result.error || 'Error desconocido'));
        }
    } catch (error) {
        console.error('Error guardando posiciones:', error);
        alert('Error al guardar las posiciones');
    }
}

if (!sceneId) {
    document.getElementById('tableBody').innerHTML = 
        '<tr><td colspan="7" class="empty">ID de escena no proporcionado</td></tr>';
} else {
    loadSceneData();
}

async function loadSceneData() {
    try {
        document.getElementById('tableBody').innerHTML = '<tr><td colspan="7" class="loading">Cargando posiciones...</td></tr>';

        const response = await fetch(`/api/scenes/${sceneId}`);
        sceneData = await response.json();

        // Mostrar información de la escena
        document.getElementById('sceneInfo').innerHTML = 
            `<strong>Escena ID:</strong> ${sceneData.id} | ` +
            `<strong>Nombre:</strong> ${sceneData.name || '-'} | ` +
            `<strong>Path:</strong> ${sceneData.path || '-'}`;

        // Cargar fixtures
        if (sceneData.fixtures && sceneData.fixtures.length > 0) {
            fixtures = sceneData.fixtures;
            renderTable(fixtures);
            renderFixtureRadioList();
            resizeCanvas();
            updateDpadButtons();
            drawCanvas();
        } else {
            document.getElementById('tableBody').innerHTML = 
                '<tr><td colspan="7" class="empty">No se encontraron fixtures con posiciones</td></tr>';
        }
    } catch (error) {
        console.error('Error cargando datos de escena:', error);
        document.getElementById('tableBody').innerHTML = 
            '<tr><td colspan="7" class="empty">Error al cargar los datos de la escena</td></tr>';
    }
}

function renderTable(fixtures) {
    const tbody = document.getElementById('tableBody');

    tbody.innerHTML = fixtures.map(fixture => {
        const pan = fixture.pan !== null && fixture.pan !== undefined ? fixture.pan : '-';
        const panFine = fixture.panFine !== null && fixture.panFine !== undefined ? fixture.panFine : '-';
        const tilt = fixture.tilt !== null && fixture.tilt !== undefined ? fixture.tilt : '-';
        const tiltFine = fixture.tiltFine !== null && fixture.tiltFine !== undefined ? fixture.tiltFine : '-';
        const x = fixture.x !== null && fixture.x !== undefined ? fixture.x : '-';
        const y = fixture.y !== null && fixture.y !== undefined ? fixture.y : '-';

        const fixtureDisplay = fixture.fixtureName ? 
            `<span class="id-badge">${fixture.fixtureId}</span> <strong>${fixture.fixtureName}</strong>` : 
            `<span class="id-badge">${fixture.fixtureId}</span>`;

        return `
            <tr>
                <td>${fixtureDisplay}</td>
                <td><span class="channel-value">${pan}</span></td>
                <td><span class="channel-value">${panFine}</span></td>
                <td><span class="channel-value">${tilt}</span></td>
                <td><span class="channel-value">${tiltFine}</span></td>
                <td><span class="coord-value">${x}</span></td>
                <td><span class="coord-value">${y}</span></td>
            </tr>
        `;
    }).join('');
}

// Ajustar canvas al redimensionar ventana
window.addEventListener('resize', resizeCanvas);
