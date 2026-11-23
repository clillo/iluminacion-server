let fullDmxMap = {};
let allData = [];
let maxChannels = 512;
let maxUniverses = 2;

async function loadDmxMap() {
    try {
        const response = await fetch('/api/dmx-map');
        const data = await response.json();
        allData = data.dmxMap || [];
        fullDmxMap = data.fullDmxMap || {};

        // Determinar número máximo de universos
        const universes = [...new Set(allData.map(item => item.universe))].sort((a, b) => a - b);
        maxUniverses = universes.length > 0 ? Math.max(...universes) : 2;

        // Máximo de canales por universo es 512
        maxChannels = 512;


        updateStats();
        renderTable();
    } catch (error) {
        console.error('Error cargando mapa DMX:', error);
        document.getElementById('tableBody').innerHTML = 
            '<tr><td colspan="10" class="empty">Error al cargar los datos</td></tr>';
    }
}

function updateStats() {
    const uniqueFixtures = new Set(allData.map(item => item.fixtureId));
    const uniqueUniverses = new Set(allData.map(item => item.universe));

    document.getElementById('totalChannels').textContent = allData.length;
    document.getElementById('totalFixtures').textContent = uniqueFixtures.size;
    document.getElementById('totalUniverses').textContent = uniqueUniverses.size;
}

function renderTable() {
    const searchInput = document.getElementById('searchInput').value.toLowerCase();

    // Construir encabezados de tabla
    const thead = document.getElementById('tableHead');
    let headerRow = '<tr><th>Canal</th>';
    for (let u = 1; u <= maxUniverses; u++) {
        headerRow += `<th>Universo ${u}</th>`;
    }
    headerRow += '</tr>';
    thead.innerHTML = headerRow;

    // Construir filas de la tabla
    const tbody = document.getElementById('tableBody');
    let tableRows = '';

    for (let channel = 1; channel <= maxChannels; channel++) {
        let row = `<tr><td>${channel}</td>`;
        let hasData = false;

        for (let universe = 1; universe <= maxUniverses; universe++) {
            // Los keys del objeto JSON son strings
            const channelMap = fullDmxMap[channel] || fullDmxMap[channel.toString()];
            const entry = channelMap && (channelMap[universe] || channelMap[universe.toString()]);

            if (entry && entry.fixtureName) {
                // Filtrar por búsqueda si hay texto
                if (searchInput && !entry.fixtureName.toLowerCase().includes(searchInput) && 
                    !entry.fixtureModel.toLowerCase().includes(searchInput)) {
                    row += '<td class="empty-channel">-</td>';
                } else {
                    hasData = true;
                    const channelName = entry.channelName ? `<div class="channel-name">${entry.channelName}</div>` : '';
                    row += `<td><div class="fixture-name">${entry.fixtureName}</div>${channelName}</td>`;
                }
            } else {
                row += '<td class="empty-channel">-</td>';
            }
        }

        row += '</tr>';

        // Solo mostrar filas con datos o si no hay filtro de búsqueda
        if (!searchInput || hasData) {
            tableRows += row;
        }
    }

    if (tableRows === '') {
        tbody.innerHTML = `<tr><td colspan="${maxUniverses + 1}" class="empty">No se encontraron canales</td></tr>`;
    } else {
        tbody.innerHTML = tableRows;
    }
}

function exportToCSV() {
    if (!fullDmxMap || Object.keys(fullDmxMap).length === 0) {
        alert('No hay datos para exportar');
        return;
    }

    // Construir encabezados
    let csv = 'Canal';
    for (let u = 1; u <= maxUniverses; u++) {
        csv += `,Universo ${u}`;
    }
    csv += '\n';

    // Construir filas
    for (let channel = 1; channel <= maxChannels; channel++) {
        csv += channel;

        for (let universe = 1; universe <= maxUniverses; universe++) {
            const channelMap = fullDmxMap[channel] || fullDmxMap[channel.toString()];
            const entry = channelMap && (channelMap[universe] || channelMap[universe.toString()]);

            if (entry && entry.fixtureName) {
                let cellValue = entry.fixtureName;
                if (entry.channelName) {
                    cellValue += ` (${entry.channelName})`;
                }
                // Escapar comillas y comas en CSV
                cellValue = cellValue.replace(/"/g, '""');
                if (cellValue.includes(',') || cellValue.includes('"') || cellValue.includes('\n')) {
                    cellValue = `"${cellValue}"`;
                }
                csv += `,${cellValue}`;
            } else {
                csv += ',-';
            }
        }

        csv += '\n';
    }

    // Crear y descargar archivo
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `dmx-map-${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Event listeners
document.getElementById('searchInput').addEventListener('input', renderTable);

// Cargar datos al iniciar
loadDmxMap();

// Recargar cada 30 segundos
setInterval(loadDmxMap, 30000);
