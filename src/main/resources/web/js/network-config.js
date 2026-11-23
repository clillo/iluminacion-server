let selectedIp = null;
let currentArtNetIp = null;

async function loadCurrentConfig() {
    try {
        const response = await fetch('/api/external-config');
        const data = await response.json();
        currentArtNetIp = data.artNet.ipAddress;
        selectedIp = currentArtNetIp;
        document.getElementById('currentArtNetIp').textContent = currentArtNetIp;
    } catch (error) {
        console.error('Error cargando configuración:', error);
    }
}

async function loadInterfaces() {
    try {
        document.getElementById('interfacesList').innerHTML = '<div class="loading">Cargando interfaces de red...</div>';

        const response = await fetch('/api/network/interfaces');
        const data = await response.json();
        const interfaces = data.interfaces || [];

        if (interfaces.length === 0) {
            document.getElementById('interfacesList').innerHTML = 
                '<div class="empty">No se encontraron interfaces de red cableadas con IPs IPv4</div>';
            return;
        }

        let html = '';
        interfaces.forEach((iface, index) => {
            const isSelected = selectedIp === iface.ipAddress;
            html += `
                <div class="interface-card ${isSelected ? 'selected' : ''}" id="interface-${index}">
                    <div class="interface-header">
                        <div>
                            <div class="interface-name">${iface.displayName || iface.name}</div>
                            <div class="interface-ip">${iface.ipAddress}</div>
                        </div>
                        <button class="select-btn ${isSelected ? 'selected' : ''}" onclick="selectInterface('${iface.ipAddress}', ${index})">
                            ${isSelected ? '✓ Guardada' : 'Guardar como IP ArtNet'}
                        </button>
                    </div>
                    <div class="interface-details">
                        <span><strong>Interfaz:</strong> ${iface.name}</span>
                        ${iface.subnetMask ? `<span><strong>Máscara:</strong> ${iface.subnetMask}</span>` : ''}
                        ${iface.macAddress ? `<span><strong>MAC:</strong> ${iface.macAddress}</span>` : ''}
                        <span><strong>Estado:</strong> ${iface.isUp ? 'Activa' : 'Inactiva'}</span>
                    </div>
                </div>
            `;
        });

        document.getElementById('interfacesList').innerHTML = html;
    } catch (error) {
        console.error('Error cargando interfaces:', error);
        document.getElementById('interfacesList').innerHTML = 
            '<div class="empty">Error al cargar las interfaces de red</div>';
    }
}

async function selectInterface(ip, index) {
    try {
        // Guardar en el servidor
        const response = await fetch('/api/external-config/artnet-ip', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ ipAddress: ip })
        });

        const result = await response.json();

        if (response.ok) {
            selectedIp = ip;
            currentArtNetIp = ip;

            // Actualizar UI
            document.querySelectorAll('.interface-card').forEach(card => {
                card.classList.remove('selected');
            });
            document.querySelectorAll('.select-btn').forEach(btn => {
                btn.classList.remove('selected');
                btn.textContent = 'Guardar como IP ArtNet';
            });

            const card = document.getElementById(`interface-${index}`);
            const btn = card.querySelector('.select-btn');
            card.classList.add('selected');
            btn.classList.add('selected');
            btn.textContent = '✓ Guardada';

            // Actualizar IP actual
            document.getElementById('currentArtNetIp').textContent = ip;

            // Mostrar mensaje de éxito
            const messageDiv = document.getElementById('saveMessage');
            messageDiv.textContent = '✓ IP ArtNet actualizada correctamente: ' + ip;
            messageDiv.style.display = 'block';
            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 3000);
        } else {
            alert('Error al guardar: ' + (result.error || 'Error desconocido'));
        }
    } catch (error) {
        console.error('Error al guardar IP:', error);
        alert('Error al guardar la configuración');
    }
}

// Cargar configuración e interfaces al iniciar
loadCurrentConfig().then(() => loadInterfaces());
