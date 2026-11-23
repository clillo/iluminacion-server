let scenes = [];
let currentEditingScene = null;

async function loadScenes() {
    try {
        document.getElementById('tableBody').innerHTML = '<tr><td colspan="6" class="loading">Cargando escenas...</td></tr>';

        const response = await fetch('/api/scenes');
        const data = await response.json();
        scenes = data.scenes || [];

        renderTable();
    } catch (error) {
        console.error('Error cargando escenas:', error);
        document.getElementById('tableBody').innerHTML = 
            '<tr><td colspan="6" class="empty">Error al cargar las escenas</td></tr>';
    }
}

function renderTable() {
    const tbody = document.getElementById('tableBody');
    const subTypeFilter = document.getElementById('subTypeFilter').value;

    // Filtrar escenas por subType
    let filteredScenes = scenes;
    if (subTypeFilter) {
        filteredScenes = scenes.filter(scene => scene.subType === subTypeFilter);
    }

    if (filteredScenes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty">No se encontraron escenas' + (subTypeFilter ? ' con subType "' + subTypeFilter + '"' : '') + '</td></tr>';
        return;
    }

    tbody.innerHTML = filteredScenes.map(scene => {
        const subTypeDisplay = scene.subType ? 
            `<span class="subtype-badge">${scene.subType}</span>` : 
            '<span style="color: #999;">-</span>';

        return `
            <tr>
                <td><span class="id-badge">${scene.id}</span></td>
                <td><strong>${scene.name || '-'}</strong></td>
                <td>${scene.path || '-'}</td>
                <td>${subTypeDisplay}</td>
                <td>${scene.pointCount || 0}</td>
                <td>
                    ${scene.subType === 'robotic.position.static' ? 
                        `<a href="/scene-position-edit.html?id=${scene.id}" class="edit-btn" style="text-decoration: none; display: inline-block;">✏️ Editar Posición</a>` : 
                        `<button class="edit-btn" onclick="editScene(${scene.id})">✏️ Editar</button>`
                    }
                </td>
            </tr>
        `;
    }).join('');
}

async function editScene(sceneId) {
    try {
        const response = await fetch(`/api/scenes/${sceneId}`);
        const scene = await response.json();

        currentEditingScene = scene;
        document.getElementById('editId').value = scene.id;
        document.getElementById('editName').value = scene.name || '';
        document.getElementById('editPath').value = scene.path || '';
        document.getElementById('editSubType').value = scene.subType || '';

        document.getElementById('editModal').style.display = 'block';
    } catch (error) {
        console.error('Error cargando escena:', error);
        showMessage('Error al cargar la escena', 'error');
    }
}

function closeModal() {
    document.getElementById('editModal').style.display = 'none';
    currentEditingScene = null;
}

document.getElementById('editForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    if (!currentEditingScene) return;

    const sceneId = currentEditingScene.id;
    const updateData = {
        name: document.getElementById('editName').value,
        subType: document.getElementById('editSubType').value || null
    };

    try {
        const response = await fetch(`/api/scenes/${sceneId}/update`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        const result = await response.json();

        if (response.ok) {
            showMessage('Escena actualizada correctamente', 'success');
            closeModal();
            loadScenes();
        } else {
            showMessage('Error: ' + (result.error || 'Error desconocido'), 'error');
        }
    } catch (error) {
        console.error('Error actualizando escena:', error);
        showMessage('Error al actualizar la escena', 'error');
    }
});

function showMessage(text, type) {
    const messageDiv = document.getElementById('message');
    messageDiv.textContent = text;
    messageDiv.className = 'message ' + type;
    messageDiv.style.display = 'block';
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

// Cerrar modal al hacer clic fuera
window.onclick = function(event) {
    const modal = document.getElementById('editModal');
    if (event.target == modal) {
        closeModal();
    }
}

// Event listener para el filtro
document.getElementById('subTypeFilter').addEventListener('change', renderTable);

// Cargar escenas al iniciar
loadScenes();
