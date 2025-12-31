package cl.clillo.lighting.web;

import cl.clillo.lighting.config.FixtureConfig;
import cl.clillo.lighting.config.FixturesConfig;
import cl.clillo.lighting.config.FixturesConfigService;
import cl.clillo.lighting.config.DmxMapService;
import cl.clillo.lighting.config.NetworkConfigService;
import cl.clillo.lighting.config.ExternalConfigService;
import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.external.dmx.Dmx;
import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.fixture.qlc.QLCFixture;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.external.dmx.Dmx;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servidor web embebido para controlar efectos desde tablet.
 * Expone una API REST y sirve un frontend HTML.
 */
@Slf4j
public class WebServer {

    private Server server;
    private final int port;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BeeEyeStateManager beeEyeStateManager = new BeeEyeStateManager();
    private final VirtualDJStatusManager virtualDJStatusManager = new VirtualDJStatusManager();

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // API REST endpoints
        context.addServlet(new ServletHolder(new ApiServlet()), "/api/*");
        
        // Frontend estático
        context.addServlet(new ServletHolder(new FrontendServlet()), "/*");

        server.start();
        log.info("Web server started on http://localhost:{}", port);
        log.info("Access the BeeEye control panel at http://localhost:{}/", port);
    }

    public void stop() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
            log.info("Web server stopped");
        }
    }

    /**
     * Servlet para la API REST
     */
    private class ApiServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = req.getPathInfo();
            
            if (path == null || path.equals("/") || path.equals("/shows")) {
                // GET /api/shows?group=xxx - Listar shows filtrados por grupo
                handleGetShows(req, resp);
            } else if (path.startsWith("/shows/")) {
                // GET /api/shows/{id} - Estado de un show específico
                handleGetShow(req, resp, path);
            } else if (path != null && path.equals("/beeeye/status")) {
                // GET /api/beeeye/status - Estado de todos los BeeEye
                handleGetBeeEyeStatus(req, resp);
            } else if (path != null && path.equals("/fixture-groups")) {
                // GET /api/fixture-groups - Listar grupos de fixtures disponibles
                handleGetFixtureGroups(req, resp);
            } else if (path != null && path.equals("/virtualdj/status")) {
                // GET /api/virtualdj/status - Estado de comunicación con VirtualDJ (OS2L)
                handleGetVirtualDJStatus(req, resp);
            } else if (path != null && path.equals("/fixtures")) {
                // GET /api/fixtures - Listar todos los fixtures
                handleGetFixtures(req, resp);
            } else if (path != null && path.equals("/fixtures/config")) {
                // GET /api/fixtures/config - Obtener configuración completa
                handleGetFixturesConfig(req, resp);
            } else if (path != null && path.startsWith("/fixtures/")) {
                // GET /api/fixtures/{id} - Obtener un fixture específico
                handleGetFixture(req, resp, path);
            } else if (path != null && path.equals("/dmx-map")) {
                // GET /api/dmx-map - Obtener mapa completo de canales DMX
                handleGetDmxMap(req, resp);
            } else if (path != null && path.equals("/network/interfaces")) {
                // GET /api/network/interfaces - Obtener interfaces de red cableadas
                handleGetNetworkInterfaces(req, resp);
            } else if (path != null && path.equals("/external-config")) {
                // GET /api/external-config - Obtener configuración externa
                handleGetExternalConfig(req, resp);
            } else if (path != null && path.equals("/scenes")) {
                // GET /api/scenes - Listar todas las escenas
                handleGetScenes(req, resp);
            } else if (path != null && path.startsWith("/scenes/")) {
                // GET /api/scenes/{id} - Obtener una escena específica
                handleGetScene(req, resp, path);
            } else if (path != null && path.equals("/collections")) {
                // GET /api/collections - Listar todas las collections
                handleGetCollections(req, resp);
            } else if (path != null && path.startsWith("/collections/")) {
                // GET /api/collections/{id} - Obtener una collection específica
                handleGetCollection(req, resp, path);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendJsonResponse(resp, Map.of("error", "Not found"));
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = req.getPathInfo();
            
            if (path != null && path.startsWith("/shows/") && path.endsWith("/toggle")) {
                // POST /api/shows/{id}/toggle - Activar/desactivar show
                handleToggleShow(req, resp, path);
            } else if (path != null && path.equals("/beeeye/blackout")) {
                // POST /api/beeeye/blackout - Blackout de BeeEye
                handleBeeEyeBlackout(req, resp);
            } else if (path != null && path.startsWith("/fixtures/") && path.endsWith("/turn-on")) {
                // POST /api/fixtures/{id}/turn-on - Encender un fixture
                handleTurnOnFixture(req, resp, path);
            } else if (path != null && path.startsWith("/fixtures/") && path.endsWith("/turn-off")) {
                // POST /api/fixtures/{id}/turn-off - Apagar un fixture
                handleTurnOffFixture(req, resp, path);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendJsonResponse(resp, Map.of("error", "Not found"));
            }
        }

        @Override
        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = req.getPathInfo();
            
            if (path != null && path.startsWith("/fixtures/") && path.endsWith("/update")) {
                // PUT /api/fixtures/{id}/update - Actualizar configuración de un fixture
                handleUpdateFixture(req, resp, path);
            } else if (path != null && path.equals("/fixtures/config/update")) {
                // PUT /api/fixtures/config/update - Actualizar configuración (maxUniverses)
                handleUpdateFixturesConfig(req, resp);
            } else if (path != null && path.equals("/external-config/artnet-ip")) {
                // PUT /api/external-config/artnet-ip - Actualizar IP de ArtNet
                handleUpdateArtNetIp(req, resp);
            } else if (path != null && path.startsWith("/scenes/") && path.endsWith("/update")) {
                // PUT /api/scenes/{id}/update - Actualizar una escena
                handleUpdateScene(req, resp, path);
            } else if (path != null && path.startsWith("/scenes/") && path.endsWith("/update-positions")) {
                // PUT /api/scenes/{id}/update-positions - Actualizar posiciones de fixtures
                handleUpdateScenePositions(req, resp, path);
            } else if (path != null && path.startsWith("/scenes/") && path.endsWith("/send-point")) {
                // PUT /api/scenes/{id}/send-point - Enviar un punto a ArtNet en tiempo real
                handleSendPointToArtNet(req, resp, path);
            } else if (path != null && path.startsWith("/collections/") && path.endsWith("/update")) {
                // PUT /api/collections/{id}/update - Actualizar una collection
                handleUpdateCollection(req, resp, path);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendJsonResponse(resp, Map.of("error", "Not found"));
            }
        }

        private void handleGetShows(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ShowCollection collection = ShowCollection.getInstance();
            List<Map<String, Object>> shows = new ArrayList<>();
            
            // Obtener parámetro de grupo
            String groupParam = req.getParameter("group");
            
            for (Show show : collection.getShowList()) {
                QLCFunction function = show.getFunction();
                if (function == null) continue;
                
                String path = function.getPath();
                String name = function.getName();
                
                // Filtrar por grupo si está especificado
                if (groupParam != null && !groupParam.isEmpty()) {
                    if (!matchesFixtureGroup(path, name, groupParam)) {
                        continue;
                    }
                }
                
                Map<String, Object> showData = new HashMap<>();
                showData.put("id", show.getId());
                showData.put("name", show.getName());
                showData.put("path", path);
                showData.put("type", function.getType());
                showData.put("executing", show.isExecuting());
                shows.add(showData);
            }
            
            // Agrupar por path
            Map<String, List<Map<String, Object>>> grouped = shows.stream()
                .collect(Collectors.groupingBy(s -> (String) s.get("path")));
            
            sendJsonResponse(resp, Map.of("shows", shows, "grouped", grouped));
        }

        private boolean matchesFixtureGroup(String path, String name, String group) {
            if (path == null) path = "";
            if (name == null) name = "";
            
            switch (group) {
                case "bee-eyes":
                    return (path != null && path.contains("Bee Eye")) ||
                           (name != null && name.contains("BeeEye"));
                case "moving-head-spot":
                    return path.contains("Moving Head Spot") && !path.contains("Moving Head Beam");
                case "moving-head-hibrid":
                    return path.contains("Moving Head Beam + Spot") || 
                           (path.contains("Moving Head Beam") && !path.contains("Moving Head Spot"));
                case "laser":
                    return path.contains("Laser");
                case "derby":
                    return path.contains("Derby");
                case "spiders":
                    return path.contains("Spider");
                default:
                    return false;
            }
        }

        private void handleGetFixtureGroups(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            List<Map<String, Object>> groups = new ArrayList<>();
            
            groups.add(Map.of(
                "id", "bee-eyes",
                "name", "Bee Eyes",
                "displayName", "Bee Eyes"
            ));
            groups.add(Map.of(
                "id", "moving-head-spot",
                "name", "Moving Head Spot",
                "displayName", "Moving Head Spot"
            ));
            groups.add(Map.of(
                "id", "moving-head-hibrid",
                "name", "Moving Head Beam + Spot",
                "displayName", "Moving Head Beam + Spot"
            ));
            groups.add(Map.of(
                "id", "laser",
                "name", "Láser",
                "displayName", "Láser"
            ));
            groups.add(Map.of(
                "id", "derby",
                "name", "Derby",
                "displayName", "Derby"
            ));
            groups.add(Map.of(
                "id", "spiders",
                "name", "Spiders",
                "displayName", "Spiders"
            ));
            
            sendJsonResponse(resp, Map.of("groups", groups));
        }

        private void handleGetVirtualDJStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            sendJsonResponse(resp, virtualDJStatusManager.getStatus());
        }

        private void handleGetFixtures(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            FixturesConfigService configService = FixturesConfigService.getInstance();
            FixturesConfig config = configService.getConfig();
            sendJsonResponse(resp, Map.of(
                "maxUniverses", config.getMaxUniverses(),
                "fixtures", config.getFixtures()
            ));
        }

        private void handleGetFixturesConfig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            FixturesConfigService configService = FixturesConfigService.getInstance();
            FixturesConfig config = configService.getConfig();
            sendJsonResponse(resp, Map.of(
                "maxUniverses", config.getMaxUniverses()
            ));
        }

        private void handleUpdateFixturesConfig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                // Leer el cuerpo de la petición
                StringBuilder body = new StringBuilder();
                String line;
                try (var reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }
                
                // Parsear JSON
                @SuppressWarnings("unchecked")
                Map<String, Object> configData = (Map<String, Object>) objectMapper.readValue(body.toString(), Map.class);
                Integer maxUniverses = (Integer) configData.get("maxUniverses");
                
                if (maxUniverses == null || maxUniverses < 1 || maxUniverses > 15) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "maxUniverses debe estar entre 1 y 15"));
                    return;
                }
                
                // Actualizar configuración
                FixturesConfigService configService = FixturesConfigService.getInstance();
                FixturesConfig config = configService.getConfig();
                config.setMaxUniverses(maxUniverses);
                configService.saveConfig();
                
                // Recargar fixtures en ShowCollection para aplicar cambios
                try {
                    ShowCollection.getInstance().reloadFixtures();
                    log.info("Fixtures recargados después de actualizar maxUniverses");
                } catch (Exception e) {
                    log.warn("No se pudieron recargar los fixtures automáticamente: {}", e.getMessage());
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "maxUniverses", maxUniverses, "reloaded", true));
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error updating config: " + e.getMessage()));
            }
        }

        private void handleGetFixture(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/fixtures/".length());
                int id = Integer.parseInt(idStr);
                
                FixturesConfigService configService = FixturesConfigService.getInstance();
                FixtureConfig fixture = configService.getFixture(id);
                
                if (fixture == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Fixture not found"));
                    return;
                }
                
                sendJsonResponse(resp, fixture);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid fixture ID"));
            }
        }

        private void handleTurnOnFixture(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/fixtures/".length(), path.length() - "/turn-on".length());
                int fixtureId = Integer.parseInt(idStr);
                
                // Obtener la configuración del fixture
                FixturesConfigService configService = FixturesConfigService.getInstance();
                FixtureConfig fixtureConfig = configService.getFixture(fixtureId);
                
                if (fixtureConfig == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Fixture not found"));
                    return;
                }
                
                // Si hay una escena configurada, usarla
                if (fixtureConfig.getSceneIdOn() != null && fixtureConfig.getSceneIdOn() > 0) {
                    ShowCollection collection = ShowCollection.getInstance();
                    Show show = collection.getShow(fixtureConfig.getSceneIdOn());
                    
                    if (show == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        sendJsonResponse(resp, Map.of("error", "Scene not found with ID: " + fixtureConfig.getSceneIdOn()));
                        return;
                    }
                    
                    if (!(show.getFunction() instanceof QLCScene)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        sendJsonResponse(resp, Map.of("error", "Show ID " + fixtureConfig.getSceneIdOn() + " is not a Scene"));
                        return;
                    }
                    
                    // Ejecutar la escena una vez
                    QLCScene scene = (QLCScene) show.getFunction();
                    Dmx dmx = Dmx.getInstance();
                    
                    // Enviar todos los puntos de la escena
                    for (QLCPoint point : scene.getQlcPointList()) {
                        if (point.getFixture() != null && point.getFixture().getId() == fixtureId) {
                            dmx.send(point.getFixture().getUniverse(), point.getDmxChannel(), point.getData());
                        }
                    }
                    
                    // También enviar LedPoints si existen
                    if (scene.getLedPoints() != null && !scene.getLedPoints().isEmpty()) {
                        QLCFixture fixture = collection.getFixture(fixtureId);
                        if (fixture != null) {
                            for (cl.clillo.lighting.model.LedPoint lp : scene.getLedPoints()) {
                                int ledIndex = Math.max(0, lp.getId());
                                int base = 21 + ledIndex * 4;
                                int chR = fixture.getDMXChannel(base + 0);
                                int chG = fixture.getDMXChannel(base + 1);
                                int chB = fixture.getDMXChannel(base + 2);
                                int chW = fixture.getDMXChannel(base + 3);
                                
                                int r = Math.min(255, Math.max(0, lp.getR()));
                                int g = Math.min(255, Math.max(0, lp.getG()));
                                int b = Math.min(255, Math.max(0, lp.getB()));
                                int w = Math.min(255, Math.max(0, lp.getW()));
                                
                                dmx.send(fixture.getUniverse(), chR, r);
                                dmx.send(fixture.getUniverse(), chG, g);
                                dmx.send(fixture.getUniverse(), chB, b);
                                dmx.send(fixture.getUniverse(), chW, w);
                            }
                        }
                    }
                    
                    log.info("Fixture {} encendido usando escena {}", fixtureId, fixtureConfig.getSceneIdOn());
                    sendJsonResponse(resp, Map.of("status", "ok", "on", true));
                } else {
                    // Fallback: usar blackout points con valores 255
                    ShowCollection collection = ShowCollection.getInstance();
                    QLCFixture fixture = collection.getFixture(fixtureId);
                    
                    if (fixture == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        sendJsonResponse(resp, Map.of("error", "Fixture not found in ShowCollection"));
                        return;
                    }
                    
                    Dmx dmx = Dmx.getInstance();
                    List<QLCPoint> blackoutPoints = fixture.getBlackoutPointList();
                    int universe = fixture.getUniverse();
                    
                    for (QLCPoint point : blackoutPoints) {
                        int value = 255;
                        if (point.getChannelType() != null && point.getChannelType() == QLCFixture.ChannelType.STROBE) {
                            value = 0;
                        }
                        dmx.send(universe, point.getDmxChannel(), value);
                    }
                    
                    log.info("Fixture {} ({}) encendido (sin escena configurada)", fixtureId, fixture.getName());
                    sendJsonResponse(resp, Map.of("status", "ok", "on", true));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid fixture ID"));
            } catch (Exception e) {
                log.error("Error al encender fixture", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al encender fixture: " + e.getMessage()));
            }
        }

        private void handleTurnOffFixture(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/fixtures/".length(), path.length() - "/turn-off".length());
                int fixtureId = Integer.parseInt(idStr);
                
                // Obtener la configuración del fixture
                FixturesConfigService configService = FixturesConfigService.getInstance();
                FixtureConfig fixtureConfig = configService.getFixture(fixtureId);
                
                if (fixtureConfig == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Fixture not found"));
                    return;
                }
                
                // Si hay una escena configurada, usarla
                if (fixtureConfig.getSceneIdOff() != null && fixtureConfig.getSceneIdOff() > 0) {
                    ShowCollection collection = ShowCollection.getInstance();
                    Show show = collection.getShow(fixtureConfig.getSceneIdOff());
                    
                    if (show == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        sendJsonResponse(resp, Map.of("error", "Scene not found with ID: " + fixtureConfig.getSceneIdOff()));
                        return;
                    }
                    
                    if (!(show.getFunction() instanceof QLCScene)) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        sendJsonResponse(resp, Map.of("error", "Show ID " + fixtureConfig.getSceneIdOff() + " is not a Scene"));
                        return;
                    }
                    
                    // Ejecutar la escena una vez
                    QLCScene scene = (QLCScene) show.getFunction();
                    Dmx dmx = Dmx.getInstance();
                    
                    // Enviar todos los puntos de la escena
                    for (QLCPoint point : scene.getQlcPointList()) {
                        if (point.getFixture() != null && point.getFixture().getId() == fixtureId) {
                            dmx.send(point.getFixture().getUniverse(), point.getDmxChannel(), point.getData());
                        }
                    }
                    
                    // También enviar LedPoints si existen
                    if (scene.getLedPoints() != null && !scene.getLedPoints().isEmpty()) {
                        QLCFixture fixture = collection.getFixture(fixtureId);
                        if (fixture != null) {
                            for (cl.clillo.lighting.model.LedPoint lp : scene.getLedPoints()) {
                                int ledIndex = Math.max(0, lp.getId());
                                int base = 21 + ledIndex * 4;
                                int chR = fixture.getDMXChannel(base + 0);
                                int chG = fixture.getDMXChannel(base + 1);
                                int chB = fixture.getDMXChannel(base + 2);
                                int chW = fixture.getDMXChannel(base + 3);
                                
                                int r = Math.min(255, Math.max(0, lp.getR()));
                                int g = Math.min(255, Math.max(0, lp.getG()));
                                int b = Math.min(255, Math.max(0, lp.getB()));
                                int w = Math.min(255, Math.max(0, lp.getW()));
                                
                                dmx.send(fixture.getUniverse(), chR, r);
                                dmx.send(fixture.getUniverse(), chG, g);
                                dmx.send(fixture.getUniverse(), chB, b);
                                dmx.send(fixture.getUniverse(), chW, w);
                            }
                        }
                    }
                    
                    log.info("Fixture {} apagado usando escena {}", fixtureId, fixtureConfig.getSceneIdOff());
                    sendJsonResponse(resp, Map.of("status", "ok", "on", false));
                } else {
                    // Fallback: usar blackout points (todos a 0)
                    ShowCollection collection = ShowCollection.getInstance();
                    QLCFixture fixture = collection.getFixture(fixtureId);
                    
                    if (fixture == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        sendJsonResponse(resp, Map.of("error", "Fixture not found in ShowCollection"));
                        return;
                    }
                    
                    Dmx dmx = Dmx.getInstance();
                    List<QLCPoint> blackoutPoints = fixture.getBlackoutPointList();
                    int universe = fixture.getUniverse();
                    
                    for (QLCPoint point : blackoutPoints) {
                        // Enviar 0 para apagar todos los canales
                        dmx.send(universe, point.getDmxChannel(), 0);
                    }
                    
                    log.info("Fixture {} ({}) apagado (sin escena configurada)", fixtureId, fixture.getName());
                    sendJsonResponse(resp, Map.of("status", "ok", "on", false));
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid fixture ID"));
            } catch (Exception e) {
                log.error("Error al apagar fixture", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al apagar fixture: " + e.getMessage()));
            }
        }

        private void handleUpdateFixture(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/fixtures/".length(), path.length() - "/update".length());
                int id = Integer.parseInt(idStr);
                
                // Leer el cuerpo de la petición
                StringBuilder body = new StringBuilder();
                String line;
                try (var reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }
                
                // Parsear JSON
                FixtureConfig fixtureConfig = objectMapper.readValue(body.toString(), FixtureConfig.class);
                
                // Validar que el ID coincida
                if (fixtureConfig.getId() != id) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Fixture ID mismatch"));
                    return;
                }
                
                // Actualizar configuración
                FixturesConfigService configService = FixturesConfigService.getInstance();
                configService.updateFixture(fixtureConfig);
                
                // Recargar fixtures en ShowCollection para aplicar cambios
                try {
                    ShowCollection.getInstance().reloadFixtures();
                    log.info("Fixtures recargados después de actualizar fixture ID: {}", id);
                } catch (Exception e) {
                    log.warn("No se pudieron recargar los fixtures automáticamente: {}", e.getMessage());
                    // Continuar de todas formas, el usuario puede reiniciar manualmente
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "fixture", fixtureConfig, "reloaded", true));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid fixture ID"));
            } catch (IOException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error updating fixture: " + e.getMessage()));
            }
        }

        private void handleGetShow(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/shows/".length());
                int id = Integer.parseInt(idStr);
                
                Show show = ShowCollection.getInstance().getShow(id);
                if (show == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Show not found"));
                    return;
                }
                
                QLCFunction function = show.getFunction();
                Map<String, Object> showData = new HashMap<>();
                showData.put("id", show.getId());
                showData.put("name", show.getName());
                showData.put("path", function != null ? function.getPath() : null);
                showData.put("type", function != null ? function.getType() : null);
                showData.put("executing", show.isExecuting());
                
                sendJsonResponse(resp, showData);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid show ID"));
            }
        }

        private void handleToggleShow(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String idStr = path.substring("/shows/".length(), path.length() - "/toggle".length());
                int id = Integer.parseInt(idStr);
                
                Show show = ShowCollection.getInstance().getShow(id);
                if (show == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Show not found"));
                    return;
                }
                
                ShowCollection.getInstance().toggleShow(show);
                
                Map<String, Object> result = new HashMap<>();
                result.put("id", show.getId());
                result.put("executing", show.isExecuting());
                
                sendJsonResponse(resp, result);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid show ID"));
            }
        }

        private void handleBeeEyeBlackout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ShowCollection collection = ShowCollection.getInstance();
            
            // Apagar todos los BeeEye
            for (Show show : collection.getShowList()) {
                QLCFunction function = show.getFunction();
                if (function == null) continue;
                
                String path = function.getPath();
                if (path != null && path.contains("Bee Eye")) {
                    show.setExecuting(false);
                }
            }
            
            sendJsonResponse(resp, Map.of("status", "ok", "message", "BeeEye blackout executed"));
        }

        private void handleGetBeeEyeStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            List<Map<String, Object>> states = beeEyeStateManager.getAllStates();
            sendJsonResponse(resp, Map.of("beeEyes", states));
        }

        private void handleGetDmxMap(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            DmxMapService dmxMapService = DmxMapService.getInstance();
            List<DmxMapService.DmxChannelEntry> map = dmxMapService.generateDmxMap();
            Map<Integer, Map<Integer, DmxMapService.DmxChannelEntry>> fullMap = dmxMapService.generateFullDmxMap();
            sendJsonResponse(resp, Map.of("dmxMap", map, "fullDmxMap", fullMap));
        }

        private void handleGetNetworkInterfaces(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            NetworkConfigService networkService = NetworkConfigService.getInstance();
            List<NetworkConfigService.NetworkInterfaceInfo> interfaces = networkService.getWiredNetworkInterfaces();
            sendJsonResponse(resp, Map.of("interfaces", interfaces));
        }

        private void handleGetExternalConfig(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ExternalConfigService configService = ExternalConfigService.getInstance();
            String artNetIp = configService.getArtNetIpAddress();
            Map<String, Object> artNetMap = new HashMap<>();
            artNetMap.put("ipAddress", artNetIp);
            Map<String, Object> response = new HashMap<>();
            response.put("artNet", artNetMap);
            sendJsonResponse(resp, response);
        }

        private void handleUpdateArtNetIp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            try {
                Map<String, Object> requestData = objectMapper.readValue(req.getReader(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                String ipAddress = (String) requestData.get("ipAddress");
                
                if (ipAddress == null || ipAddress.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "IP address is required"));
                    return;
                }
                
                ExternalConfigService configService = ExternalConfigService.getInstance();
                configService.setArtNetIpAddress(ipAddress);
                
                // Actualizar la IP en la instancia de ArtNet si está activa
                try {
                    ArtNet artNet = ArtNet.getInstance();
                    if (artNet != null) {
                        artNet.updateArtNetAddress();
                    }
                } catch (Exception e) {
                    log.warn("No se pudo actualizar la dirección ArtNet en la instancia activa: {}", e.getMessage());
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "message", "ArtNet IP actualizada", "ipAddress", ipAddress));
            } catch (Exception e) {
                log.error("Error al actualizar IP de ArtNet", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al actualizar la configuración: " + e.getMessage()));
            }
        }

        private void handleGetScenes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ShowCollection collection = ShowCollection.getInstance();
            List<Map<String, Object>> scenes = new ArrayList<>();
            
            for (Show show : collection.getShowList()) {
                if (show.getFunction() instanceof QLCScene) {
                    QLCScene scene = show.getFunction();
                    Map<String, Object> sceneData = new HashMap<>();
                    sceneData.put("id", scene.getId());
                    sceneData.put("name", scene.getName());
                    sceneData.put("path", scene.getPath());
                    sceneData.put("type", scene.getType());
                    sceneData.put("subType", scene.getSubType());
                    sceneData.put("blackout", scene.isBlackout());
                    sceneData.put("totalBlackout", scene.isTotalBlackout());
                    sceneData.put("initEventTrigger", scene.isInitEventTrigger());
                    sceneData.put("pointCount", scene.getQlcPointList() != null ? scene.getQlcPointList().size() : 0);
                    scenes.add(sceneData);
                }
            }
            
            sendJsonResponse(resp, Map.of("scenes", scenes));
        }

        private void handleGetScene(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid scene ID"));
                    return;
                }
                
                int sceneId = Integer.parseInt(parts[2]);
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCScene foundScene = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCScene && show.getFunction().getId() == sceneId) {
                        foundScene = show.getFunction();
                        break;
                    }
                }
                
                if (foundScene == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Scene not found"));
                    return;
                }
                
                // Si es una escena robotic.position.static, devolver datos especiales
                if ("robotic.position.static".equals(foundScene.getSubType())) {
                    Map<String, Object> sceneData = buildRoboticPositionSceneData(foundScene);
                    sendJsonResponse(resp, sceneData);
                    return;
                }
                
                Map<String, Object> sceneData = new HashMap<>();
                sceneData.put("id", foundScene.getId());
                sceneData.put("name", foundScene.getName());
                sceneData.put("path", foundScene.getPath());
                sceneData.put("type", foundScene.getType());
                sceneData.put("subType", foundScene.getSubType());
                sceneData.put("blackout", foundScene.isBlackout());
                sceneData.put("totalBlackout", foundScene.isTotalBlackout());
                sceneData.put("initEventTrigger", foundScene.isInitEventTrigger());
                
                // Agregar puntos
                List<Map<String, Object>> points = new ArrayList<>();
                if (foundScene.getQlcPointList() != null) {
                    for (QLCPoint point : foundScene.getQlcPointList()) {
                        Map<String, Object> pointData = new HashMap<>();
                        pointData.put("fixtureId", point.getFixture().getId());
                        pointData.put("fixtureName", point.getFixture().getName());
                        pointData.put("channel", point.getChannel());
                        pointData.put("data", point.getData());
                        pointData.put("dmxChannel", point.getDmxChannel());
                        points.add(pointData);
                    }
                }
                sceneData.put("points", points);
                
                sendJsonResponse(resp, sceneData);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid scene ID format"));
            } catch (Exception e) {
                log.error("Error al obtener escena", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al obtener la escena: " + e.getMessage()));
            }
        }

        private Map<String, Object> buildRoboticPositionSceneData(QLCScene scene) {
            Map<String, Object> sceneData = new HashMap<>();
            sceneData.put("id", scene.getId());
            sceneData.put("name", scene.getName());
            sceneData.put("path", scene.getPath());
            sceneData.put("type", scene.getType());
            sceneData.put("subType", scene.getSubType());
            
            // Agrupar puntos por fixture y extraer valores de pan/tilt
            Map<Integer, Map<String, Object>> fixturesMap = new HashMap<>();
            
            if (scene.getQlcPointList() != null) {
                for (QLCPoint point : scene.getQlcPointList()) {
                    if (!point.isMovement()) {
                        continue; // Solo procesar puntos de movimiento
                    }
                    
                    int fixtureId = point.getFixture().getId();
                    fixturesMap.putIfAbsent(fixtureId, new HashMap<>());
                    Map<String, Object> fixtureData = fixturesMap.get(fixtureId);
                    
                    // Inicializar datos del fixture si es la primera vez
                    if (!fixtureData.containsKey("fixtureId")) {
                        fixtureData.put("fixtureId", fixtureId);
                        fixtureData.put("fixtureName", point.getFixture().getName());
                        fixtureData.put("pan", null);
                        fixtureData.put("panFine", null);
                        fixtureData.put("tilt", null);
                        fixtureData.put("tiltFine", null);
                    }
                    
                    // Asignar valores según el tipo de canal
                    QLCFixture.ChannelType channelType = point.getChannelType();
                    if (channelType == QLCFixture.ChannelType.PAN) {
                        fixtureData.put("pan", point.getData());
                    } else if (channelType == QLCFixture.ChannelType.PAN_FINE) {
                        fixtureData.put("panFine", point.getData());
                    } else if (channelType == QLCFixture.ChannelType.TILT) {
                        fixtureData.put("tilt", point.getData());
                    } else if (channelType == QLCFixture.ChannelType.TILT_FINE) {
                        fixtureData.put("tiltFine", point.getData());
                    }
                }
            }
            
            // Convertir a lista y calcular coordenadas x, y
            List<Map<String, Object>> fixtures = new ArrayList<>();
            for (Map<String, Object> fixtureData : fixturesMap.values()) {
                Integer pan = (Integer) fixtureData.get("pan");
                Integer panFine = (Integer) fixtureData.get("panFine");
                Integer tilt = (Integer) fixtureData.get("tilt");
                Integer tiltFine = (Integer) fixtureData.get("tiltFine");
                
                // Calcular coordenadas
                int x = -1;
                int y = -1;
                if (pan != null && panFine != null) {
                    x = pan * 256 + panFine;
                }
                if (tilt != null && tiltFine != null) {
                    y = tilt * 256 + tiltFine;
                }
                
                fixtureData.put("x", x >= 0 ? x : null);
                fixtureData.put("y", y >= 0 ? y : null);
                
                fixtures.add(fixtureData);
            }
            
            // Ordenar por fixtureId
            fixtures.sort((a, b) -> Integer.compare((Integer)a.get("fixtureId"), (Integer)b.get("fixtureId")));
            
            sceneData.put("fixtures", fixtures);
            return sceneData;
        }

        private void handleUpdateScene(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid scene ID"));
                    return;
                }
                
                int sceneId = Integer.parseInt(parts[2]);
                
                Map<String, Object> requestData = objectMapper.readValue(req.getReader(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCScene foundScene = null;
                Show foundShow = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCScene && show.getFunction().getId() == sceneId) {
                        foundScene = show.getFunction();
                        foundShow = show;
                        break;
                    }
                }
                
                if (foundScene == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Scene not found"));
                    return;
                }
                
                // Actualizar campos básicos (nota: algunos campos son final, así que solo actualizamos los modificables)
                String name = (String) requestData.get("name");
                String subType = (String) requestData.get("subType");
                
                if (name != null && !name.trim().isEmpty()) {
                    foundShow.setName(name);
                    // Nota: El nombre en QLCScene es final, así que no podemos cambiarlo directamente
                    // Esto requeriría recrear la escena, lo cual es más complejo
                }
                
                // TODO: Implementar guardado en archivo XML
                // Por ahora solo confirmamos que se recibió la actualización
                log.info("Actualización de escena {} recibida: name={}, subType={}", sceneId, name, subType);
                
                sendJsonResponse(resp, Map.of("status", "ok", "message", "Escena actualizada (guardado en archivo pendiente)", "sceneId", sceneId));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid scene ID format"));
            } catch (Exception e) {
                log.error("Error al actualizar escena", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al actualizar la escena: " + e.getMessage()));
            }
        }

        private void handleUpdateScenePositions(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid scene ID"));
                    return;
                }
                
                int sceneId = Integer.parseInt(parts[2]);
                
                Map<String, Object> requestData = objectMapper.readValue(req.getReader(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> fixturesData = (List<Map<String, Object>>) requestData.get("fixtures");
                
                if (fixturesData == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Fixtures data is required"));
                    return;
                }
                
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCScene foundScene = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCScene && show.getFunction().getId() == sceneId) {
                        foundScene = show.getFunction();
                        break;
                    }
                }
                
                if (foundScene == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Scene not found"));
                    return;
                }
                
                if (!"robotic.position.static".equals(foundScene.getSubType())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Scene is not of type robotic.position.static"));
                    return;
                }
                
                // Actualizar los puntos de la escena y enviar a ArtNet
                Dmx dmx = Dmx.getInstance();
                int updatedCount = 0;
                int sentCount = 0;
                
                for (Map<String, Object> fixtureData : fixturesData) {
                    Integer fixtureId = (Integer) fixtureData.get("fixtureId");
                    Integer pan = (Integer) fixtureData.get("pan");
                    Integer panFine = (Integer) fixtureData.get("panFine");
                    Integer tilt = (Integer) fixtureData.get("tilt");
                    Integer tiltFine = (Integer) fixtureData.get("tiltFine");
                    
                    if (fixtureId == null) continue;
                    
                    // Buscar y actualizar los puntos de este fixture
                    for (QLCPoint point : foundScene.getQlcPointList()) {
                        if (point.getFixture().getId() != fixtureId) continue;
                        if (!point.isMovement()) continue;
                        
                        QLCFixture.ChannelType channelType = point.getChannelType();
                        boolean updated = false;
                        
                        if (channelType == QLCFixture.ChannelType.PAN && pan != null) {
                            point.setData(pan);
                            updated = true;
                            updatedCount++;
                        } else if (channelType == QLCFixture.ChannelType.PAN_FINE && panFine != null) {
                            point.setData(panFine);
                            updated = true;
                            updatedCount++;
                        } else if (channelType == QLCFixture.ChannelType.TILT && tilt != null) {
                            point.setData(tilt);
                            updated = true;
                            updatedCount++;
                        } else if (channelType == QLCFixture.ChannelType.TILT_FINE && tiltFine != null) {
                            point.setData(tiltFine);
                            updated = true;
                            updatedCount++;
                        }
                        
                        // Enviar a ArtNet si se actualizó (equivalente a ejecutar la escena)
                        if (updated) {
                            dmx.send(point);
                            sentCount++;
                        }
                    }
                }
                
                log.info("Escena {}: {} puntos actualizados, {} valores enviados a ArtNet", sceneId, updatedCount, sentCount);
                
                // Guardar el archivo XML
                try {
                    String dir = ShowCollection.getInstance().getDirectory(foundScene);
                    foundScene.writeToConfigFile(dir);
                    log.info("Escena {} guardada en: {}", sceneId, dir);
                } catch (Exception e) {
                    log.error("Error al guardar el archivo XML de la escena {}", sceneId, e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    sendJsonResponse(resp, Map.of("error", "Error al guardar el archivo: " + e.getMessage()));
                    return;
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "message", "Posiciones actualizadas, enviadas a ArtNet y guardadas correctamente", "sceneId", sceneId, "fixturesUpdated", fixturesData.size(), "pointsUpdated", updatedCount, "valuesSentToArtNet", sentCount));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid scene ID format"));
            } catch (Exception e) {
                log.error("Error al actualizar posiciones de escena", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al actualizar las posiciones: " + e.getMessage()));
            }
        }

        private void handleSendPointToArtNet(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid scene ID"));
                    return;
                }
                
                int sceneId = Integer.parseInt(parts[2]);
                
                Map<String, Object> requestData = objectMapper.readValue(req.getReader(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                Integer fixtureId = (Integer) requestData.get("fixtureId");
                Integer pan = (Integer) requestData.get("pan");
                Integer panFine = (Integer) requestData.get("panFine");
                Integer tilt = (Integer) requestData.get("tilt");
                Integer tiltFine = (Integer) requestData.get("tiltFine");
                
                if (fixtureId == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Fixture ID is required"));
                    return;
                }
                
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCScene foundScene = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCScene && show.getFunction().getId() == sceneId) {
                        foundScene = show.getFunction();
                        break;
                    }
                }
                
                if (foundScene == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Scene not found"));
                    return;
                }
                
                if (!"robotic.position.static".equals(foundScene.getSubType())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Scene is not of type robotic.position.static"));
                    return;
                }
                
                // Buscar y actualizar los puntos del fixture, luego enviar a ArtNet
                Dmx dmx = Dmx.getInstance();
                int sentCount = 0;
                
                for (QLCPoint point : foundScene.getQlcPointList()) {
                    if (point.getFixture().getId() != fixtureId) continue;
                    if (!point.isMovement()) continue;
                    
                    QLCFixture.ChannelType channelType = point.getChannelType();
                    boolean shouldSend = false;
                    
                    if (channelType == QLCFixture.ChannelType.PAN && pan != null) {
                        point.setData(pan);
                        shouldSend = true;
                    } else if (channelType == QLCFixture.ChannelType.PAN_FINE && panFine != null) {
                        point.setData(panFine);
                        shouldSend = true;
                    } else if (channelType == QLCFixture.ChannelType.TILT && tilt != null) {
                        point.setData(tilt);
                        shouldSend = true;
                    } else if (channelType == QLCFixture.ChannelType.TILT_FINE && tiltFine != null) {
                        point.setData(tiltFine);
                        shouldSend = true;
                    }
                    
                    // Enviar a ArtNet en tiempo real
                    if (shouldSend) {
                        dmx.send(point);
                        sentCount++;
                    }
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "message", "Valores enviados a ArtNet", "fixtureId", fixtureId, "valuesSent", sentCount));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid scene ID format"));
            } catch (Exception e) {
                log.error("Error al enviar punto a ArtNet", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al enviar a ArtNet: " + e.getMessage()));
            }
        }

        private void handleGetCollections(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ShowCollection collection = ShowCollection.getInstance();
            List<Map<String, Object>> collections = new ArrayList<>();
            
            for (Show show : collection.getShowList()) {
                if (show.getFunction() instanceof QLCCollection) {
                    QLCCollection qlcCollection = show.getFunction();
                    Map<String, Object> collectionData = new HashMap<>();
                    collectionData.put("id", qlcCollection.getId());
                    collectionData.put("name", qlcCollection.getName());
                    collectionData.put("path", qlcCollection.getPath());
                    collectionData.put("type", qlcCollection.getType());
                    collectionData.put("showCount", qlcCollection.getShowList() != null ? qlcCollection.getShowList().size() : 0);
                    
                    // Agregar lista de shows con sus IDs y nombres
                    List<Map<String, Object>> shows = new ArrayList<>();
                    if (qlcCollection.getShowList() != null) {
                        for (Show s : qlcCollection.getShowList()) {
                            Map<String, Object> showData = new HashMap<>();
                            showData.put("id", s.getId());
                            showData.put("name", s.getName());
                            shows.add(showData);
                        }
                    }
                    collectionData.put("shows", shows);
                    collections.add(collectionData);
                }
            }
            
            sendJsonResponse(resp, Map.of("collections", collections));
        }

        private void handleGetCollection(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid collection ID"));
                    return;
                }
                
                int collectionId = Integer.parseInt(parts[2]);
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCCollection foundCollection = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCCollection && show.getFunction().getId() == collectionId) {
                        foundCollection = show.getFunction();
                        break;
                    }
                }
                
                if (foundCollection == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Collection not found"));
                    return;
                }
                
                Map<String, Object> collectionData = new HashMap<>();
                collectionData.put("id", foundCollection.getId());
                collectionData.put("name", foundCollection.getName());
                collectionData.put("path", foundCollection.getPath());
                collectionData.put("type", foundCollection.getType());
                
                // Agregar lista de shows
                List<Map<String, Object>> shows = new ArrayList<>();
                if (foundCollection.getShowList() != null) {
                    for (Show s : foundCollection.getShowList()) {
                        Map<String, Object> showData = new HashMap<>();
                        showData.put("id", s.getId());
                        showData.put("name", s.getName());
                        shows.add(showData);
                    }
                }
                collectionData.put("shows", shows);
                
                sendJsonResponse(resp, collectionData);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid collection ID format"));
            } catch (Exception e) {
                log.error("Error al obtener collection", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al obtener la collection: " + e.getMessage()));
            }
        }

        private void handleUpdateCollection(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
            try {
                String[] parts = path.split("/");
                if (parts.length < 3) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendJsonResponse(resp, Map.of("error", "Invalid collection ID"));
                    return;
                }
                
                int collectionId = Integer.parseInt(parts[2]);
                
                Map<String, Object> requestData = objectMapper.readValue(req.getReader(), 
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                
                ShowCollection collection = ShowCollection.getInstance();
                
                QLCCollection foundCollection = null;
                Show foundShow = null;
                for (Show show : collection.getShowList()) {
                    if (show.getFunction() instanceof QLCCollection && show.getFunction().getId() == collectionId) {
                        foundCollection = show.getFunction();
                        foundShow = show;
                        break;
                    }
                }
                
                if (foundCollection == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    sendJsonResponse(resp, Map.of("error", "Collection not found"));
                    return;
                }
                
                // Actualizar nombre si se proporciona
                String name = (String) requestData.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    foundShow.setName(name);
                }
                
                // Actualizar lista de shows si se proporciona
                @SuppressWarnings("unchecked")
                List<Integer> showIds = (List<Integer>) requestData.get("showIds");
                if (showIds != null) {
                    // Limpiar lista actual
                    foundCollection.getShowList().clear();
                    
                    // Agregar los shows especificados
                    for (Integer showId : showIds) {
                        Show show = collection.getShow(showId);
                        if (show != null) {
                            foundCollection.addShow(show);
                        }
                    }
                }
                
                // Guardar el archivo XML
                try {
                    String dir = ShowCollection.getInstance().getDirectory(foundCollection);
                    foundCollection.writeToConfigFile(dir);
                    log.info("Collection {} guardada en: {}", collectionId, dir);
                } catch (Exception e) {
                    log.error("Error al guardar el archivo XML de la collection {}", collectionId, e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    sendJsonResponse(resp, Map.of("error", "Error al guardar el archivo: " + e.getMessage()));
                    return;
                }
                
                sendJsonResponse(resp, Map.of("status", "ok", "message", "Collection actualizada y guardada correctamente", "collectionId", collectionId));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendJsonResponse(resp, Map.of("error", "Invalid collection ID format"));
            } catch (Exception e) {
                log.error("Error al actualizar collection", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                sendJsonResponse(resp, Map.of("error", "Error al actualizar la collection: " + e.getMessage()));
            }
        }

        private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            
            PrintWriter out = resp.getWriter();
            objectMapper.writeValue(out, data);
            out.flush();
        }
    }

    /**
     * Servlet para servir el frontend HTML
     */
    private class FrontendServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String path = req.getPathInfo();
            
            // Si es la raíz o /index.html, servir el frontend
            if (path == null || path.equals("/") || path.equals("/index.html")) {
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getFrontendHtml());
                out.flush();
            } else if (path != null && path.equals("/dmx-map.html")) {
                // Servir la página del mapa DMX
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getDmxMapHtml());
                out.flush();
            } else if (path != null && path.equals("/network-config.html")) {
                // Servir la página de configuración de red
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getNetworkConfigHtml());
                out.flush();
            } else if (path != null && path.equals("/scenes-config.html")) {
                // Servir la página de configuración de escenas
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getScenesConfigHtml());
                out.flush();
            } else if (path != null && path.equals("/collections-config.html")) {
                // Servir la página de configuración de collections
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getCollectionsConfigHtml());
                out.flush();
            } else if (path != null && path.startsWith("/scene-position-edit.html")) {
                // Servir la página de edición especial de posiciones robóticas
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                String sceneId = req.getParameter("id");
                out.write(getScenePositionEditHtml(sceneId));
                out.flush();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        private String getFrontendHtml() {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">\n" +
                    "    <title>BeeEye Control</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;\n" +
                    "            background: #000;\n" +
                    "            color: #fff;\n" +
                    "            padding: 20px;\n" +
                    "            overflow-x: hidden;\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            text-align: center;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            font-size: 2.5em;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .blackout-btn {\n" +
                    "            width: 100%;\n" +
                    "            max-width: 400px;\n" +
                    "            margin: 0 auto 30px;\n" +
                    "            padding: 25px;\n" +
                    "            font-size: 1.8em;\n" +
                    "            font-weight: bold;\n" +
                    "            background: #500000;\n" +
                    "            color: #fff;\n" +
                    "            border: 3px solid #ff0000;\n" +
                    "            border-radius: 8px;\n" +
                    "            cursor: pointer;\n" +
                    "            touch-action: manipulation;\n" +
                    "            transition: background 0.2s;\n" +
                    "        }\n" +
                    "        .blackout-btn:active {\n" +
                    "            background: #300000;\n" +
                    "        }\n" +
                    "        .group {\n" +
                    "            margin-bottom: 40px;\n" +
                    "        }\n" +
                    "        .group-title {\n" +
                    "            font-size: 1.5em;\n" +
                    "            color: #aaa;\n" +
                    "            margin-bottom: 15px;\n" +
                    "            padding-bottom: 10px;\n" +
                    "            border-bottom: 2px solid #333;\n" +
                    "        }\n" +
                    "        .buttons-grid {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));\n" +
                    "            gap: 15px;\n" +
                    "        }\n" +
                    "        .effect-btn {\n" +
                    "            padding: 20px 15px;\n" +
                    "            font-size: 1.2em;\n" +
                    "            background: #000;\n" +
                    "            color: #aaa;\n" +
                    "            border: 2px solid #444;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            touch-action: manipulation;\n" +
                    "            transition: all 0.2s;\n" +
                    "            min-height: 80px;\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "            justify-content: center;\n" +
                    "            align-items: center;\n" +
                    "        }\n" +
                    "        .effect-btn:hover {\n" +
                    "            border-color: #666;\n" +
                    "        }\n" +
                    "        .effect-btn.active {\n" +
                    "            background: #00b400;\n" +
                    "            color: #fff;\n" +
                    "            border-color: #00ff00;\n" +
                    "        }\n" +
                    "        .effect-id {\n" +
                    "            font-size: 0.9em;\n" +
                    "            opacity: 0.7;\n" +
                    "            margin-bottom: 5px;\n" +
                    "        }\n" +
                    "        .effect-name {\n" +
                    "            font-weight: bold;\n" +
                    "            text-align: center;\n" +
                    "            word-break: break-word;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .error {\n" +
                    "            text-align: center;\n" +
                    "            padding: 20px;\n" +
                    "            color: #f00;\n" +
                    "            background: #300;\n" +
                    "            border-radius: 6px;\n" +
                    "            margin: 20px 0;\n" +
                    "        }\n" +
                    "        .beeeye-section {\n" +
                    "            margin-bottom: 40px;\n" +
                    "        }\n" +
                    "        .beeeye-section-title {\n" +
                    "            font-size: 1.8em;\n" +
                    "            color: #fff;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            text-align: center;\n" +
                    "            padding-bottom: 10px;\n" +
                    "            border-bottom: 2px solid #444;\n" +
                    "        }\n" +
                    "        .beeeye-grid {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                    "            gap: 20px;\n" +
                    "            max-width: 1200px;\n" +
                    "            margin: 0 auto;\n" +
                    "        }\n" +
                    "        .beeeye-container {\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "            align-items: center;\n" +
                    "            padding: 15px;\n" +
                    "            background: #111;\n" +
                    "            border-radius: 8px;\n" +
                    "            border: 1px solid #333;\n" +
                    "        }\n" +
                    "        .beeeye-name {\n" +
                    "            font-size: 1.1em;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            color: #aaa;\n" +
                    "        }\n" +
                    "        .beeeye-svg {\n" +
                    "            width: 200px;\n" +
                    "            height: 200px;\n" +
                    "        }\n" +
                    "        .group-selection {\n" +
                    "            margin-bottom: 40px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .group-selection-title {\n" +
                    "            font-size: 1.8em;\n" +
                    "            color: #fff;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .group-buttons {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
                    "            gap: 20px;\n" +
                    "            max-width: 1000px;\n" +
                    "            margin: 0 auto;\n" +
                    "        }\n" +
                    "        .group-btn {\n" +
                    "            padding: 30px 20px;\n" +
                    "            font-size: 1.3em;\n" +
                    "            font-weight: bold;\n" +
                    "            background: #222;\n" +
                    "            color: #fff;\n" +
                    "            border: 3px solid #555;\n" +
                    "            border-radius: 8px;\n" +
                    "            cursor: pointer;\n" +
                    "            touch-action: manipulation;\n" +
                    "            transition: all 0.3s;\n" +
                    "            min-height: 100px;\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "        }\n" +
                    "        .group-btn:hover {\n" +
                    "            background: #333;\n" +
                    "            border-color: #777;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .group-btn.selected {\n" +
                    "            background: #0066cc;\n" +
                    "            border-color: #0088ff;\n" +
                    "        }\n" +
                    "        .back-btn {\n" +
                    "            padding: 15px 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "            background: #444;\n" +
                    "            color: #fff;\n" +
                    "            border: 2px solid #666;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            margin: 20px auto;\n" +
                    "            display: block;\n" +
                    "            transition: all 0.2s;\n" +
                    "        }\n" +
                    "        .back-btn:hover {\n" +
                    "            background: #555;\n" +
                    "            border-color: #777;\n" +
                    "        }\n" +
                    "        .hidden {\n" +
                    "            display: none;\n" +
                    "        }\n" +
                    "        .test-section {\n" +
                    "            margin: 20px auto 10px;\n" +
                    "            max-width: 1000px;\n" +
                    "            padding: 20px;\n" +
                    "            border: 1px solid #333;\n" +
                    "            border-radius: 10px;\n" +
                    "            background: #0b0b0b;\n" +
                    "        }\n" +
                    "        .test-section-title {\n" +
                    "            font-size: 1.4em;\n" +
                    "            color: #ddd;\n" +
                    "            margin-bottom: 14px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .test-view-title {\n" +
                    "            font-size: 1.8em;\n" +
                    "            color: #fff;\n" +
                    "            margin: 10px 0 18px;\n" +
                    "            text-align: center;\n" +
                    "            padding-bottom: 10px;\n" +
                    "            border-bottom: 2px solid #444;\n" +
                    "        }\n" +
                    "        .card {\n" +
                    "            background: #111;\n" +
                    "            border: 1px solid #333;\n" +
                    "            border-radius: 10px;\n" +
                    "            padding: 16px;\n" +
                    "            margin: 10px auto;\n" +
                    "            max-width: 900px;\n" +
                    "        }\n" +
                    "        .kv {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: 200px 1fr;\n" +
                    "            gap: 10px;\n" +
                    "            align-items: center;\n" +
                    "        }\n" +
                    "        .kv .k { color: #aaa; }\n" +
                    "        .kv .v { color: #fff; word-break: break-word; }\n" +
                    "        .fixture-item {\n" +
                    "            background: #1a1a1a;\n" +
                    "            border: 1px solid #444;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 15px;\n" +
                    "            margin: 10px 0;\n" +
                    "        }\n" +
                    "        .fixture-header {\n" +
                    "            font-size: 1.2em;\n" +
                    "            font-weight: bold;\n" +
                    "            color: #fff;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .fixture-form {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: 150px 1fr;\n" +
                    "            gap: 10px;\n" +
                    "            align-items: center;\n" +
                    "            margin: 8px 0;\n" +
                    "        }\n" +
                    "        .fixture-form label {\n" +
                    "            color: #aaa;\n" +
                    "        }\n" +
                    "        .fixture-form input {\n" +
                    "            padding: 8px;\n" +
                    "            background: #222;\n" +
                    "            border: 1px solid #555;\n" +
                    "            border-radius: 4px;\n" +
                    "            color: #fff;\n" +
                    "            font-size: 1em;\n" +
                    "        }\n" +
                    "        .fixture-form input:focus {\n" +
                    "            outline: none;\n" +
                    "            border-color: #0088ff;\n" +
                    "        }\n" +
                    "        .save-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #0066cc;\n" +
                    "            color: #fff;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-size: 1em;\n" +
                    "            margin-top: 10px;\n" +
                    "            transition: background 0.2s;\n" +
                    "        }\n" +
                    "        .save-btn:hover {\n" +
                    "            background: #0088ff;\n" +
                    "        }\n" +
                    "        .save-btn:disabled {\n" +
                    "            background: #444;\n" +
                    "            cursor: not-allowed;\n" +
                    "        }\n" +
                    "        .toggle-switch {\n" +
                    "            position: relative;\n" +
                    "            display: inline-block;\n" +
                    "            width: 60px;\n" +
                    "            height: 34px;\n" +
                    "            margin-left: 10px;\n" +
                    "        }\n" +
                    "        .toggle-switch input {\n" +
                    "            opacity: 0;\n" +
                    "            width: 0;\n" +
                    "            height: 0;\n" +
                    "        }\n" +
                    "        .toggle-slider {\n" +
                    "            position: absolute;\n" +
                    "            cursor: pointer;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            right: 0;\n" +
                    "            bottom: 0;\n" +
                    "            background-color: #444;\n" +
                    "            transition: .4s;\n" +
                    "            border-radius: 34px;\n" +
                    "        }\n" +
                    "        .toggle-slider:before {\n" +
                    "            position: absolute;\n" +
                    "            content: \"\";\n" +
                    "            height: 26px;\n" +
                    "            width: 26px;\n" +
                    "            left: 4px;\n" +
                    "            bottom: 4px;\n" +
                    "            background-color: white;\n" +
                    "            transition: .4s;\n" +
                    "            border-radius: 50%;\n" +
                    "        }\n" +
                    "        .toggle-switch input:checked + .toggle-slider {\n" +
                    "            background-color: #4CAF50;\n" +
                    "        }\n" +
                    "        .toggle-switch input:checked + .toggle-slider:before {\n" +
                    "            transform: translateX(26px);\n" +
                    "        }\n" +
                    "        .toggle-switch input:disabled + .toggle-slider {\n" +
                    "            opacity: 0.5;\n" +
                    "            cursor: not-allowed;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"header\">\n" +
                    "        <h1>Control de Iluminación</h1>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div id=\"groupSelection\" class=\"group-selection\">\n" +
                    "        <div class=\"group-selection-title\">Seleccione el grupo de fixtures a configurar</div>\n" +
                    "        <div class=\"group-buttons\" id=\"groupButtons\">\n" +
                    "            <div class=\"loading\">Cargando grupos...</div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div id=\"testEntry\" class=\"test-section\">\n" +
                    "        <div class=\"test-section-title\">Modo Test / Configuración final</div>\n" +
                    "        <div class=\"group-buttons\">\n" +
                    "            <button class=\"group-btn\" onclick=\"openTestChase()\">Chasing de fixtures</button>\n" +
                    "            <button class=\"group-btn\" onclick=\"openTestVirtualDJ()\">Estado de comunicación con VirtualDJ</button>\n" +
                    "            <button class=\"group-btn\" onclick=\"openFixtureConfig()\">Configuración de Fixtures</button>\n" +
                    "            <a href=\"/dmx-map.html\" class=\"group-btn\" style=\"text-decoration: none; display: inline-block; text-align: center; color: #fff;\">🗺️ Mapa DMX</a>\n" +
                    "            <a href=\"/network-config.html\" class=\"group-btn\" style=\"text-decoration: none; display: inline-block; text-align: center; color: #fff;\">⚙️ Configuración de Red</a>\n" +
                    "            <a href=\"/scenes-config.html\" class=\"group-btn\" style=\"text-decoration: none; display: inline-block; text-align: center; color: #fff;\">🎭 Configuración de Escenas</a>\n" +
                    "            <a href=\"/collections-config.html\" class=\"group-btn\" style=\"text-decoration: none; display: inline-block; text-align: center; color: #fff;\">📚 Configuración de Collections</a>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div id=\"mainContent\" class=\"hidden\">\n" +
                    "        <button class=\"back-btn\" id=\"backBtn\">← Volver a selección de grupos</button>\n" +
                    "        <button class=\"blackout-btn\" id=\"blackoutBtn\">BLACKOUT</button>\n" +
                    "        \n" +
                    "        <div class=\"beeeye-section hidden\" id=\"beeeyeSection\">\n" +
                    "            <div class=\"beeeye-section-title\">Estado BeeEye</div>\n" +
                    "            <div class=\"beeeye-grid\" id=\"beeeyeGrid\">\n" +
                    "                <div class=\"loading\">Cargando estado BeeEye...</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div id=\"content\">\n" +
                    "            <div class=\"loading\">Cargando efectos...</div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div id=\"testContent\" class=\"hidden\">\n" +
                    "        <button class=\"back-btn\" id=\"testBackBtn\">← Volver al inicio</button>\n" +
                    "        <div id=\"testChaseView\" class=\"hidden\">\n" +
                    "            <div class=\"test-view-title\">Chasing de fixtures</div>\n" +
                    "            <div class=\"card\">\n" +
                    "                <div class=\"loading\" style=\"padding: 10px;\">Preparado para implementar el chasing. Próximo paso: elegir grupo, velocidad y patrón.</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        <div id=\"testVdjView\" class=\"hidden\">\n" +
                    "            <div class=\"test-view-title\">Estado de comunicación con VirtualDJ</div>\n" +
                    "            <div class=\"card\">\n" +
                    "                <div id=\"vdjStatus\" class=\"loading\" style=\"padding: 10px;\">Cargando estado...</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        <div id=\"testFixtureConfigView\" class=\"hidden\">\n" +
                    "            <div class=\"test-view-title\">Configuración de Fixtures</div>\n" +
                    "            <div class=\"card\" style=\"margin-bottom: 20px;\">\n" +
                    "                <div class=\"fixture-header\">Configuración General</div>\n" +
                    "                <div class=\"fixture-form\">\n" +
                    "                    <label>Máximo de Universos:</label>\n" +
                    "                    <input type=\"number\" id=\"maxUniverses\" value=\"2\" min=\"1\" max=\"15\" style=\"width: 100px;\">\n" +
                    "                </div>\n" +
                    "                <button class=\"save-btn\" onclick=\"saveMaxUniverses()\" style=\"margin-top: 10px;\">Guardar máximo de universos</button>\n" +
                    "            </div>\n" +
                    "            <div id=\"fixturesList\" class=\"card\">\n" +
                    "                <div class=\"loading\" style=\"padding: 10px;\">Cargando fixtures...</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        const API_BASE = '/api';\n" +
                    "        let shows = [];\n" +
                    "        let activeShowByPath = {};\n" +
                    "        let selectedGroup = null;\n" +
                    "        let fixtureGroups = [];\n" +
                    "        let activeTestView = null;\n" +
                    "        \n" +
                    "        // Cargar grupos disponibles\n" +
                    "        async function loadFixtureGroups() {\n" +
                    "            try {\n" +
                    "                const response = await fetch(API_BASE + '/fixture-groups');\n" +
                    "                const data = await response.json();\n" +
                    "                fixtureGroups = data.groups || [];\n" +
                    "                renderFixtureGroups(fixtureGroups);\n" +
                    "            } catch (error) {\n" +
                    "                document.getElementById('groupButtons').innerHTML = \n" +
                    "                    '<div class=\"error\">Error al cargar grupos: ' + error.message + '</div>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderFixtureGroups(groups) {\n" +
                    "            const container = document.getElementById('groupButtons');\n" +
                    "            let html = '';\n" +
                    "            groups.forEach(group => {\n" +
                    "                html += '<button class=\"group-btn\" onclick=\"selectGroup(\\'' + group.id + '\\')\">' + \n" +
                    "                    group.displayName + '</button>';\n" +
                    "            });\n" +
                    "            container.innerHTML = html;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function selectGroup(groupId) {\n" +
                    "            selectedGroup = groupId;\n" +
                    "            document.getElementById('groupSelection').classList.add('hidden');\n" +
                    "            document.getElementById('testEntry').classList.add('hidden');\n" +
                    "            document.getElementById('mainContent').classList.remove('hidden');\n" +
                    "            \n" +
                    "            // Mostrar/ocultar sección BeeEye solo para el grupo bee-eyes\n" +
                    "            const beeeyeSection = document.getElementById('beeeyeSection');\n" +
                    "            if (groupId === 'bee-eyes') {\n" +
                    "                beeeyeSection.classList.remove('hidden');\n" +
                    "                loadBeeEyeStatus();\n" +
                    "            } else {\n" +
                    "                beeeyeSection.classList.add('hidden');\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Actualizar botones de grupo\n" +
                    "            const selectedGroupData = fixtureGroups.find(g => g.id === groupId);\n" +
                    "            document.querySelectorAll('.group-btn').forEach(btn => {\n" +
                    "                btn.classList.remove('selected');\n" +
                    "                if (selectedGroupData && btn.textContent === selectedGroupData.displayName) {\n" +
                    "                    btn.classList.add('selected');\n" +
                    "                }\n" +
                    "            });\n" +
                    "            \n" +
                    "            loadShows();\n" +
                    "        }\n" +
                    "        \n" +
                    "        function hideAllTestViews() {\n" +
                    "            document.getElementById('testChaseView').classList.add('hidden');\n" +
                    "            document.getElementById('testVdjView').classList.add('hidden');\n" +
                    "            document.getElementById('testFixtureConfigView').classList.add('hidden');\n" +
                    "        }\n" +
                    "        \n" +
                    "        function openTestChase() {\n" +
                    "            activeTestView = 'chase';\n" +
                    "            document.getElementById('groupSelection').classList.add('hidden');\n" +
                    "            document.getElementById('testEntry').classList.add('hidden');\n" +
                    "            document.getElementById('mainContent').classList.add('hidden');\n" +
                    "            document.getElementById('testContent').classList.remove('hidden');\n" +
                    "            hideAllTestViews();\n" +
                    "            document.getElementById('testChaseView').classList.remove('hidden');\n" +
                    "        }\n" +
                    "        \n" +
                    "        function openTestVirtualDJ() {\n" +
                    "            activeTestView = 'vdj';\n" +
                    "            document.getElementById('groupSelection').classList.add('hidden');\n" +
                    "            document.getElementById('testEntry').classList.add('hidden');\n" +
                    "            document.getElementById('mainContent').classList.add('hidden');\n" +
                    "            document.getElementById('testContent').classList.remove('hidden');\n" +
                    "            hideAllTestViews();\n" +
                    "            document.getElementById('testVdjView').classList.remove('hidden');\n" +
                    "            loadVirtualDJStatus();\n" +
                    "        }\n" +
                    "        \n" +
                    "        function openFixtureConfig() {\n" +
                    "            activeTestView = 'fixture-config';\n" +
                    "            document.getElementById('groupSelection').classList.add('hidden');\n" +
                    "            document.getElementById('testEntry').classList.add('hidden');\n" +
                    "            document.getElementById('mainContent').classList.add('hidden');\n" +
                    "            document.getElementById('testContent').classList.remove('hidden');\n" +
                    "            hideAllTestViews();\n" +
                    "            document.getElementById('testFixtureConfigView').classList.remove('hidden');\n" +
                    "            loadFixtures();\n" +
                    "        }\n" +
                    "        \n" +
                    "        let maxUniverses = 2;\n" +
                    "        \n" +
                    "        async function loadFixtures() {\n" +
                    "            try {\n" +
                    "                const response = await fetch(API_BASE + '/fixtures');\n" +
                    "                const data = await response.json();\n" +
                    "                maxUniverses = data.maxUniverses || 2;\n" +
                    "                document.getElementById('maxUniverses').value = maxUniverses;\n" +
                    "                renderFixtures(data.fixtures || []);\n" +
                    "            } catch (error) {\n" +
                    "                document.getElementById('fixturesList').innerHTML = \n" +
                    "                    '<div class=\"error\">Error al cargar fixtures: ' + error.message + '</div>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function toggleFixture(id, isOn) {\n" +
                    "            const toggle = document.getElementById('power-' + id);\n" +
                    "            toggle.disabled = true;\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const endpoint = isOn ? '/turn-on' : '/turn-off';\n" +
                    "                const response = await fetch(API_BASE + '/fixtures/' + id + endpoint, {\n" +
                    "                    method: 'POST',\n" +
                    "                    headers: { 'Content-Type': 'application/json' }\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                if (result.status !== 'ok') {\n" +
                    "                    // Revertir el toggle si hay error\n" +
                    "                    toggle.checked = !isOn;\n" +
                    "                    alert('Error: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                // Revertir el toggle si hay error\n" +
                    "                toggle.checked = !isOn;\n" +
                    "                alert('Error: ' + error.message);\n" +
                    "            } finally {\n" +
                    "                toggle.disabled = false;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function saveMaxUniverses() {\n" +
                    "            const maxUniversesInput = document.getElementById('maxUniverses');\n" +
                    "            const value = parseInt(maxUniversesInput.value);\n" +
                    "            \n" +
                    "            if (isNaN(value) || value < 1 || value > 15) {\n" +
                    "                alert('Por favor ingrese un valor válido entre 1 y 15');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const response = await fetch(API_BASE + '/fixtures/config/update', {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: { 'Content-Type': 'application/json' },\n" +
                    "                    body: JSON.stringify({ maxUniverses: value })\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                if (result.status === 'ok') {\n" +
                    "                    maxUniverses = value;\n" +
                    "                    if (result.reloaded) {\n" +
                    "                        alert('Máximo de universos actualizado y recargado correctamente.');\n" +
                    "                    } else {\n" +
                    "                        alert('Máximo de universos actualizado. Los cambios se aplicarán al reiniciar.');\n" +
                    "                    }\n" +
                    "                    // Actualizar los límites de los inputs\n" +
                    "                    document.querySelectorAll('input[id^=\"universe-\"]').forEach(input => {\n" +
                    "                        input.setAttribute('max', maxUniverses);\n" +
                    "                    });\n" +
                    "                } else {\n" +
                    "                    alert('Error al actualizar: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                alert('Error al guardar: ' + error.message);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderFixtures(fixtures) {\n" +
                    "            const container = document.getElementById('fixturesList');\n" +
                    "            if (fixtures.length === 0) {\n" +
                    "                container.innerHTML = '<div class=\"loading\">No hay fixtures configurados</div>';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            let html = '';\n" +
                    "            fixtures.forEach(fixture => {\n" +
                    "                html += '<div class=\"fixture-item\">';\n" +
                    "                html += '<div class=\"fixture-header\">' + fixture.name + ' (ID: ' + fixture.id + ')</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Universe:</label>';\n" +
                    "                html += '<input type=\"number\" id=\"universe-' + fixture.id + '\" value=\"' + fixture.universe + '\" min=\"1\" max=\"' + maxUniverses + '\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Dirección DMX:</label>';\n" +
                    "                html += '<input type=\"number\" id=\"address-' + fixture.id + '\" value=\"' + fixture.address + '\" min=\"1\" max=\"512\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Tipo:</label>';\n" +
                    "                html += '<input type=\"text\" value=\"' + fixture.type + '\" disabled style=\"background: #333; color: #888;\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Modelo:</label>';\n" +
                    "                html += '<input type=\"text\" value=\"' + fixture.model + '\" disabled style=\"background: #333; color: #888;\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Activo:</label>';\n" +
                    "                html += '<input type=\"checkbox\" id=\"activo-' + fixture.id + '\" ' + (fixture.activo ? 'checked' : '') + ' style=\"width: auto; height: 20px;\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>ID Escena Encendido:</label>';\n" +
                    "                html += '<input type=\"number\" id=\"sceneIdOn-' + fixture.id + '\" value=\"' + (fixture.sceneIdOn || '') + '\" min=\"0\" placeholder=\"Opcional\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>ID Escena Apagado:</label>';\n" +
                    "                html += '<input type=\"number\" id=\"sceneIdOff-' + fixture.id + '\" value=\"' + (fixture.sceneIdOff || '') + '\" min=\"0\" placeholder=\"Opcional\">';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<div class=\"fixture-form\">';\n" +
                    "                html += '<label>Encendido:</label>';\n" +
                    "                html += '<label class=\"toggle-switch\">';\n" +
                    "                html += '<input type=\"checkbox\" id=\"power-' + fixture.id + '\" onchange=\"toggleFixture(' + fixture.id + ', this.checked)\">';\n" +
                    "                html += '<span class=\"toggle-slider\"></span>';\n" +
                    "                html += '</label>';\n" +
                    "                html += '</div>';\n" +
                    "                html += '<button class=\"save-btn\" onclick=\"saveFixture(' + fixture.id + ')\">Guardar cambios</button>';\n" +
                    "                html += '</div>';\n" +
                    "            });\n" +
                    "            container.innerHTML = html;\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function saveFixture(id) {\n" +
                    "            const universeInput = document.getElementById('universe-' + id);\n" +
                    "            const addressInput = document.getElementById('address-' + id);\n" +
                    "            const activoInput = document.getElementById('activo-' + id);\n" +
                    "            const sceneIdOnInput = document.getElementById('sceneIdOn-' + id);\n" +
                    "            const sceneIdOffInput = document.getElementById('sceneIdOff-' + id);\n" +
                    "            \n" +
                    "            const universe = parseInt(universeInput.value);\n" +
                    "            const address = parseInt(addressInput.value);\n" +
                    "            const activo = activoInput.checked;\n" +
                    "            const sceneIdOnValue = sceneIdOnInput.value.trim();\n" +
                    "            const sceneIdOn = sceneIdOnValue === '' ? null : parseInt(sceneIdOnValue);\n" +
                    "            const sceneIdOffValue = sceneIdOffInput.value.trim();\n" +
                    "            const sceneIdOff = sceneIdOffValue === '' ? null : parseInt(sceneIdOffValue);\n" +
                    "            \n" +
                    "            if (isNaN(universe) || isNaN(address)) {\n" +
                    "                alert('Por favor ingrese valores válidos');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (sceneIdOnValue !== '' && (isNaN(sceneIdOn) || sceneIdOn < 0)) {\n" +
                    "                alert('El ID de escena encendido debe ser un número válido o estar vacío');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (sceneIdOffValue !== '' && (isNaN(sceneIdOff) || sceneIdOff < 0)) {\n" +
                    "                alert('El ID de escena apagado debe ser un número válido o estar vacío');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            try {\n" +
                    "                // Obtener fixture actual\n" +
                    "                const getResponse = await fetch(API_BASE + '/fixtures/' + id);\n" +
                    "                const fixture = await getResponse.json();\n" +
                    "                \n" +
                    "                // Actualizar valores\n" +
                    "                fixture.universe = universe;\n" +
                    "                fixture.address = address;\n" +
                    "                fixture.activo = activo;\n" +
                    "                fixture.sceneIdOn = sceneIdOn;\n" +
                    "                fixture.sceneIdOff = sceneIdOff;\n" +
                    "                \n" +
                    "                // Enviar actualización\n" +
                    "                const response = await fetch(API_BASE + '/fixtures/' + id + '/update', {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: { 'Content-Type': 'application/json' },\n" +
                    "                    body: JSON.stringify(fixture)\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                if (result.status === 'ok') {\n" +
                    "                    if (result.reloaded) {\n" +
                    "                        alert('Fixture actualizado y recargado correctamente.');\n" +
                    "                    } else {\n" +
                    "                        alert('Fixture actualizado correctamente. Los cambios se aplicarán al reiniciar.');\n" +
                    "                    }\n" +
                    "                } else {\n" +
                    "                    alert('Error al actualizar: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                alert('Error al guardar: ' + error.message);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function goHome() {\n" +
                    "            document.getElementById('testContent').classList.add('hidden');\n" +
                    "            document.getElementById('mainContent').classList.add('hidden');\n" +
                    "            document.getElementById('groupSelection').classList.remove('hidden');\n" +
                    "            document.getElementById('testEntry').classList.remove('hidden');\n" +
                    "            document.getElementById('beeeyeSection').classList.add('hidden');\n" +
                    "            selectedGroup = null;\n" +
                    "            activeTestView = null;\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.getElementById('backBtn').addEventListener('click', () => {\n" +
                    "            document.getElementById('groupSelection').classList.remove('hidden');\n" +
                    "            document.getElementById('testEntry').classList.remove('hidden');\n" +
                    "            document.getElementById('mainContent').classList.add('hidden');\n" +
                    "            document.getElementById('beeeyeSection').classList.add('hidden');\n" +
                    "            selectedGroup = null;\n" +
                    "        });\n" +
                    "        document.getElementById('testBackBtn').addEventListener('click', goHome);\n" +
                    "        \n" +
                    "        async function loadShows() {\n" +
                    "            try {\n" +
                    "                const url = selectedGroup ? \n" +
                    "                    API_BASE + '/shows?group=' + encodeURIComponent(selectedGroup) : \n" +
                    "                    API_BASE + '/shows';\n" +
                    "                const response = await fetch(url);\n" +
                    "                const data = await response.json();\n" +
                    "                shows = data.shows || [];\n" +
                    "                renderShows();\n" +
                    "            } catch (error) {\n" +
                    "                document.getElementById('content').innerHTML = \n" +
                    "                    '<div class=\"error\">Error al cargar efectos: ' + error.message + '</div>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function loadVirtualDJStatus() {\n" +
                    "            try {\n" +
                    "                const resp = await fetch(API_BASE + '/virtualdj/status');\n" +
                    "                const data = await resp.json();\n" +
                    "                renderVirtualDJStatus(data);\n" +
                    "            } catch (e) {\n" +
                    "                const el = document.getElementById('vdjStatus');\n" +
                    "                if (el) el.innerHTML = '<div class=\"error\">Error: ' + e.message + '</div>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderVirtualDJStatus(data) {\n" +
                    "            const el = document.getElementById('vdjStatus');\n" +
                    "            if (!el) return;\n" +
                    "            const connected = data.connected ? 'SI' : 'NO';\n" +
                    "            const regOk = data.registered ? 'SI' : 'NO';\n" +
                    "            const err = data.registerError ? data.registerError : '-';\n" +
                    "            const ip = data.lastRemoteIp ? data.lastRemoteIp : '-';\n" +
                    "            const ago = (data.lastSeenAgoMs != null) ? (Math.round(data.lastSeenAgoMs/1000) + 's') : '-';\n" +
                    "            const bpm = (data.lastBeat && data.lastBeat.bpm != null) ? data.lastBeat.bpm : '-';\n" +
                    "            const pos = (data.lastBeat && data.lastBeat.pos != null) ? data.lastBeat.pos : '-';\n" +
                    "            \n" +
                    "            el.innerHTML = '' +\n" +
                    "              '<div class=\"kv\">' +\n" +
                    "                '<div class=\"k\">OS2L port</div><div class=\"v\">' + data.os2lPort + '</div>' +\n" +
                    "                '<div class=\"k\">Listener registrado</div><div class=\"v\">' + regOk + '</div>' +\n" +
                    "                '<div class=\"k\">Error registro</div><div class=\"v\">' + err + '</div>' +\n" +
                    "                '<div class=\"k\">Conectado</div><div class=\"v\">' + connected + '</div>' +\n" +
                    "                '<div class=\"k\">IP VirtualDJ</div><div class=\"v\">' + ip + '</div>' +\n" +
                    "                '<div class=\"k\">Último evento</div><div class=\"v\">' + ago + '</div>' +\n" +
                    "                '<div class=\"k\">Último BPM</div><div class=\"v\">' + bpm + '</div>' +\n" +
                    "                '<div class=\"k\">Última posición</div><div class=\"v\">' + pos + '</div>' +\n" +
                    "              '</div>';\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderShows() {\n" +
                    "            const content = document.getElementById('content');\n" +
                    "            \n" +
                    "            // Filtrar solo escenas para moving-head-hibrid cuyo path comience con 'Moving Head Beam + Spot'\n" +
                    "            let filteredShows = shows;\n" +
                    "            if (selectedGroup === 'moving-head-hibrid') {\n" +
                    "                filteredShows = shows.filter(show => {\n" +
                    "                    return show.type === 'Scene' && \n" +
                    "                           show.path && \n" +
                    "                           show.path.startsWith('Moving Head Beam + Spot');\n" +
                    "                });\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Agrupar por path\n" +
                    "            const grouped = {};\n" +
                    "            filteredShows.forEach(show => {\n" +
                    "                const path = show.path || 'Sin categoría';\n" +
                    "                if (!grouped[path]) grouped[path] = [];\n" +
                    "                grouped[path].push(show);\n" +
                    "            });\n" +
                    "            \n" +
                    "            // Ordenar paths: 'Moving Head Bee Eye Dimmer' primero\n" +
                    "            const paths = Object.keys(grouped).sort((a, b) => {\n" +
                    "                if (a.includes('Dimmer')) return -1;\n" +
                    "                if (b.includes('Dimmer')) return 1;\n" +
                    "                return a.localeCompare(b);\n" +
                    "            });\n" +
                    "            \n" +
                    "            let html = '';\n" +
                    "            paths.forEach(path => {\n" +
                    "                html += '<div class=\"group\">';\n" +
                    "                html += '<div class=\"group-title\">' + path + '</div>';\n" +
                    "                html += '<div class=\"buttons-grid\">';\n" +
                    "                \n" +
                    "                grouped[path].forEach(show => {\n" +
                    "                    const isActive = show.executing;\n" +
                    "                    if (isActive) activeShowByPath[path] = show.id;\n" +
                    "                    html += '<button class=\"effect-btn' + (isActive ? ' active' : '') + '\" ' +\n" +
                    "                            'data-show-id=\"' + show.id + '\" ' +\n" +
                    "                            'data-path=\"' + path + '\" ' +\n" +
                    "                            'onclick=\"toggleShow(' + show.id + ', \\'' + path + '\\')\">' +\n" +
                    "                        '<div class=\"effect-id\">' + show.id + '</div>' +\n" +
                    "                        '<div class=\"effect-name\">' + show.name + '</div>' +\n" +
                    "                    '</button>';\n" +
                    "                });\n" +
                    "                \n" +
                    "                html += '</div></div>';\n" +
                    "            });\n" +
                    "            \n" +
                    "            content.innerHTML = html;\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function toggleShow(id, path) {\n" +
                    "            try {\n" +
                    "                // Desactivar otros shows del mismo path\n" +
                    "                if (activeShowByPath[path]) {\n" +
                    "                    const otherId = activeShowByPath[path];\n" +
                    "                    if (otherId !== id) {\n" +
                    "                        await fetch(API_BASE + '/shows/' + otherId + '/toggle', { method: 'POST' });\n" +
                    "                        updateButtonState(otherId, false);\n" +
                    "                    }\n" +
                    "                }\n" +
                    "                \n" +
                    "                // Toggle del show seleccionado\n" +
                    "                const response = await fetch(API_BASE + '/shows/' + id + '/toggle', { method: 'POST' });\n" +
                    "                const data = await response.json();\n" +
                    "                \n" +
                    "                updateButtonState(id, data.executing);\n" +
                    "                if (data.executing) {\n" +
                    "                    activeShowByPath[path] = id;\n" +
                    "                } else {\n" +
                    "                    delete activeShowByPath[path];\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error toggling show:', error);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function updateButtonState(id, isActive) {\n" +
                    "            const btn = document.querySelector('[data-show-id=\"' + id + '\"]');\n" +
                    "            if (btn) {\n" +
                    "                if (isActive) {\n" +
                    "                    btn.classList.add('active');\n" +
                    "                } else {\n" +
                    "                    btn.classList.remove('active');\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function blackout() {\n" +
                    "            try {\n" +
                    "                await fetch(API_BASE + '/beeeye/blackout', { method: 'POST' });\n" +
                    "                // Recargar estado\n" +
                    "                await loadShows();\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error executing blackout:', error);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.getElementById('blackoutBtn').addEventListener('click', blackout);\n" +
                    "        \n" +
                    "        // Funciones para BeeEye\n" +
                    "        async function loadBeeEyeStatus() {\n" +
                    "            try {\n" +
                    "                const response = await fetch(API_BASE + '/beeeye/status');\n" +
                    "                const data = await response.json();\n" +
                    "                renderBeeEyes(data.beeEyes || []);\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error loading BeeEye status:', error);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderBeeEyes(beeEyes) {\n" +
                    "            const grid = document.getElementById('beeeyeGrid');\n" +
                    "            if (beeEyes.length === 0) {\n" +
                    "                grid.innerHTML = '<div class=\"loading\">No hay BeeEye configurados</div>';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            let html = '';\n" +
                    "            beeEyes.forEach(beeEye => {\n" +
                    "                html += '<div class=\"beeeye-container\">';\n" +
                    "                html += '<div class=\"beeeye-name\">' + beeEye.name + '</div>';\n" +
                    "                html += drawBeeEyeSVG(beeEye);\n" +
                    "                html += '</div>';\n" +
                    "            });\n" +
                    "            grid.innerHTML = html;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function drawBeeEyeSVG(beeEye) {\n" +
                    "            const size = 200;\n" +
                    "            const cx = size / 2;\n" +
                    "            const cy = size / 2;\n" +
                    "            const ringRadius = size * 0.35;\n" +
                    "            const ledRadius = size * 0.15;\n" +
                    "            const centerRadius = size * 0.18;\n" +
                    "            \n" +
                    "            let svg = '<svg class=\"beeeye-svg\" viewBox=\"0 0 ' + size + ' ' + size + '\">';\n" +
                    "            \n" +
                    "            // Fondo circular (bezel)\n" +
                    "            svg += '<circle cx=\"' + cx + '\" cy=\"' + cy + '\" r=\"' + (size * 0.45) + '\" fill=\"#1e1e1e\"/>';\n" +
                    "            \n" +
                    "            // LEDs del anillo (6)\n" +
                    "            for (let i = 0; i < 6; i++) {\n" +
                    "                const angle = -Math.PI / 2 + i * (Math.PI / 3);\n" +
                    "                const x = cx + ringRadius * Math.cos(angle);\n" +
                    "                const y = cy + ringRadius * Math.sin(angle);\n" +
                    "                const led = beeEye.leds[i];\n" +
                    "                const color = 'rgb(' + led.r + ',' + led.g + ',' + led.b + ')';\n" +
                    "                svg += drawHexagon(x, y, ledRadius, color);\n" +
                    "            }\n" +
                    "            \n" +
                    "            // LED central (índice 6)\n" +
                    "            const centerLed = beeEye.leds[6];\n" +
                    "            const centerColor = 'rgb(' + centerLed.r + ',' + centerLed.g + ',' + centerLed.b + ')';\n" +
                    "            svg += drawHexagon(cx, cy, centerRadius, centerColor);\n" +
                    "            \n" +
                    "            svg += '</svg>';\n" +
                    "            return svg;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function drawHexagon(cx, cy, radius, fillColor) {\n" +
                    "            let points = '';\n" +
                    "            for (let i = 0; i < 6; i++) {\n" +
                    "                const angle = -Math.PI / 2 + i * (Math.PI / 3);\n" +
                    "                const x = cx + radius * Math.cos(angle);\n" +
                    "                const y = cy + radius * Math.sin(angle);\n" +
                    "                points += (i > 0 ? ' ' : '') + x + ',' + y;\n" +
                    "            }\n" +
                    "            return '<polygon points=\"' + points + '\" fill=\"' + fillColor + '\" stroke=\"rgba(0,0,0,0.6)\" stroke-width=\"2\"/>';\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Cargar grupos al iniciar\n" +
                    "        loadFixtureGroups();\n" +
                    "        \n" +
                    "        // Actualizar estado cada 500ms para BeeEye solo si está visible\n" +
                    "        setInterval(() => {\n" +
                    "            if (selectedGroup === 'bee-eyes') {\n" +
                    "                loadBeeEyeStatus();\n" +
                    "            }\n" +
                    "        }, 500);\n" +
                    "        // Estado VirtualDJ: refrescar solo si estamos en la vista correspondiente\n" +
                    "        setInterval(() => {\n" +
                    "            if (activeTestView === 'vdj') {\n" +
                    "                loadVirtualDJStatus();\n" +
                    "            }\n" +
                    "        }, 1000);\n" +
                    "        \n" +
                    "        // Actualizar estado cada 2 segundos para shows (solo si hay grupo seleccionado)\n" +
                    "        setInterval(() => {\n" +
                    "            if (selectedGroup) {\n" +
                    "                loadShows();\n" +
                    "            }\n" +
                    "        }, 2000);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }

        private String getDmxMapHtml() {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Mapa DMX - Canales Utilizados</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            max-width: 1400px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background: white;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                    "            padding: 30px;\n" +
                    "        }\n" +
                    "        h1 {\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 2em;\n" +
                    "        }\n" +
                    "        .subtitle {\n" +
                    "            color: #666;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "        }\n" +
                    "        .controls {\n" +
                    "            margin-bottom: 20px;\n" +
                    "            display: flex;\n" +
                    "            gap: 15px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: space-between;\n" +
                    "        }\n" +
                    "        .export-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            font-size: 1em;\n" +
                    "            font-weight: 600;\n" +
                    "            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.3s;\n" +
                    "            box-shadow: 0 2px 4px rgba(0,0,0,0.2);\n" +
                    "        }\n" +
                    "        .export-btn:hover {\n" +
                    "            transform: translateY(-2px);\n" +
                    "            box-shadow: 0 4px 8px rgba(0,0,0,0.3);\n" +
                    "        }\n" +
                    "        .export-btn:active {\n" +
                    "            transform: translateY(0);\n" +
                    "        }\n" +
                    "        .filter-group {\n" +
                    "            display: flex;\n" +
                    "            gap: 10px;\n" +
                    "            align-items: center;\n" +
                    "        }\n" +
                    "        label {\n" +
                    "            font-weight: 600;\n" +
                    "            color: #555;\n" +
                    "        }\n" +
                    "        select, input {\n" +
                    "            padding: 8px 12px;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            font-size: 14px;\n" +
                    "            transition: border-color 0.3s;\n" +
                    "        }\n" +
                    "        select:focus, input:focus {\n" +
                    "            outline: none;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        .stats {\n" +
                    "            display: flex;\n" +
                    "            gap: 20px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .stat-card {\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "            padding: 15px 20px;\n" +
                    "            border-radius: 8px;\n" +
                    "            min-width: 150px;\n" +
                    "        }\n" +
                    "        .stat-label {\n" +
                    "            font-size: 0.9em;\n" +
                    "            opacity: 0.9;\n" +
                    "        }\n" +
                    "        .stat-value {\n" +
                    "            font-size: 1.8em;\n" +
                    "            font-weight: bold;\n" +
                    "            margin-top: 5px;\n" +
                    "        }\n" +
                    "        .table-container {\n" +
                    "            overflow-x: auto;\n" +
                    "            overflow-y: auto;\n" +
                    "            max-height: 80vh;\n" +
                    "            border-radius: 8px;\n" +
                    "            box-shadow: 0 2px 8px rgba(0,0,0,0.1);\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            width: 100%;\n" +
                    "            border-collapse: collapse;\n" +
                    "            background: white;\n" +
                    "        }\n" +
                    "        thead {\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        th {\n" +
                    "            padding: 15px;\n" +
                    "            text-align: center;\n" +
                    "            font-weight: 600;\n" +
                    "            position: sticky;\n" +
                    "            top: 0;\n" +
                    "            z-index: 10;\n" +
                    "        }\n" +
                    "        th:first-child {\n" +
                    "            text-align: right;\n" +
                    "            padding-right: 20px;\n" +
                    "        }\n" +
                    "        tbody tr {\n" +
                    "            border-bottom: 1px solid #eee;\n" +
                    "            transition: background-color 0.2s;\n" +
                    "        }\n" +
                    "        tbody tr:hover {\n" +
                    "            background-color: #f8f9fa;\n" +
                    "        }\n" +
                    "        tbody tr:nth-child(even) {\n" +
                    "            background-color: #fafafa;\n" +
                    "        }\n" +
                    "        tbody tr:nth-child(even):hover {\n" +
                    "            background-color: #f0f0f0;\n" +
                    "        }\n" +
                    "        td {\n" +
                    "            padding: 12px 15px;\n" +
                    "            color: #333;\n" +
                    "            text-align: center;\n" +
                    "            vertical-align: middle;\n" +
                    "        }\n" +
                    "        td:first-child {\n" +
                    "            text-align: right;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #666;\n" +
                    "            font-family: 'Courier New', monospace;\n" +
                    "            padding-right: 20px;\n" +
                    "        }\n" +
                    "        td.empty-channel {\n" +
                    "            color: #ccc;\n" +
                    "            font-style: italic;\n" +
                    "        }\n" +
                    "        .universe-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-weight: 600;\n" +
                    "            font-size: 0.9em;\n" +
                    "        }\n" +
                    "        .channel-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #28a745;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-weight: 600;\n" +
                    "            font-family: 'Courier New', monospace;\n" +
                    "        }\n" +
                    "        .fixture-name {\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .fixture-model {\n" +
                    "            color: #666;\n" +
                    "            font-size: 0.9em;\n" +
                    "            margin-top: 4px;\n" +
                    "        }\n" +
                    "        .channel-name {\n" +
                    "            color: #888;\n" +
                    "            font-size: 0.85em;\n" +
                    "            font-style: italic;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .empty {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "        .back-btn-dmx {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "            text-decoration: none;\n" +
                    "            display: inline-block;\n" +
                    "        }\n" +
                    "        .back-btn-dmx:hover {\n" +
                    "            background: #5a6268;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        @media (max-width: 768px) {\n" +
                    "            .container {\n" +
                    "                padding: 15px;\n" +
                    "            }\n" +
                    "            table {\n" +
                    "                font-size: 0.9em;\n" +
                    "            }\n" +
                    "            th, td {\n" +
                    "                padding: 8px;\n" +
                    "            }\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <a href=\"/\" class=\"back-btn-dmx\">← Volver</a>\n" +
                    "        <h1>🗺️ Mapa DMX</h1>\n" +
                    "        <p class=\"subtitle\">Visualización de canales utilizados en cada universo DMX</p>\n" +
                    "        \n" +
                    "        <div class=\"controls\">\n" +
                    "            <div class=\"filter-group\">\n" +
                    "                <label for=\"searchInput\">Buscar Fixture:</label>\n" +
                    "                <input type=\"text\" id=\"searchInput\" placeholder=\"Nombre del fixture...\">\n" +
                    "            </div>\n" +
                    "            <button class=\"export-btn\" onclick=\"exportToCSV()\">📥 Exportar CSV</button>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"stats\">\n" +
                    "            <div class=\"stat-card\">\n" +
                    "                <div class=\"stat-label\">Total Canales</div>\n" +
                    "                <div class=\"stat-value\" id=\"totalChannels\">0</div>\n" +
                    "            </div>\n" +
                    "            <div class=\"stat-card\">\n" +
                    "                <div class=\"stat-label\">Total Fixtures</div>\n" +
                    "                <div class=\"stat-value\" id=\"totalFixtures\">0</div>\n" +
                    "            </div>\n" +
                    "            <div class=\"stat-card\">\n" +
                    "                <div class=\"stat-label\">Universos</div>\n" +
                    "                <div class=\"stat-value\" id=\"totalUniverses\">0</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"table-container\">\n" +
                    "            <table id=\"dmxTable\">\n" +
                    "                <thead id=\"tableHead\">\n" +
                    "                    <tr>\n" +
                    "                        <th>Canal</th>\n" +
                    "                    </tr>\n" +
                    "                </thead>\n" +
                    "                <tbody id=\"tableBody\">\n" +
                    "                    <tr>\n" +
                    "                        <td colspan=\"10\" class=\"loading\">Cargando datos...</td>\n" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "            </table>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        let fullDmxMap = {};\n" +
                    "        let allData = [];\n" +
                    "        let maxChannels = 512;\n" +
                    "        let maxUniverses = 2;\n" +
                    "        \n" +
                    "        async function loadDmxMap() {\n" +
                    "            try {\n" +
                    "                const response = await fetch('/api/dmx-map');\n" +
                    "                const data = await response.json();\n" +
                    "                allData = data.dmxMap || [];\n" +
                    "                fullDmxMap = data.fullDmxMap || {};\n" +
                    "                \n" +
                    "                // Determinar número máximo de universos\n" +
                    "                const universes = [...new Set(allData.map(item => item.universe))].sort((a, b) => a - b);\n" +
                    "                maxUniverses = universes.length > 0 ? Math.max(...universes) : 2;\n" +
                    "                \n" +
                    "                // Máximo de canales por universo es 512\n" +
                    "                maxChannels = 512;\n" +
                    "                \n" +
                    "                \n" +
                    "                updateStats();\n" +
                    "                renderTable();\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando mapa DMX:', error);\n" +
                    "                document.getElementById('tableBody').innerHTML = \n" +
                    "                    '<tr><td colspan=\"10\" class=\"empty\">Error al cargar los datos</td></tr>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function updateStats() {\n" +
                    "            const uniqueFixtures = new Set(allData.map(item => item.fixtureId));\n" +
                    "            const uniqueUniverses = new Set(allData.map(item => item.universe));\n" +
                    "            \n" +
                    "            document.getElementById('totalChannels').textContent = allData.length;\n" +
                    "            document.getElementById('totalFixtures').textContent = uniqueFixtures.size;\n" +
                    "            document.getElementById('totalUniverses').textContent = uniqueUniverses.size;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderTable() {\n" +
                    "            const searchInput = document.getElementById('searchInput').value.toLowerCase();\n" +
                    "            \n" +
                    "            // Construir encabezados de tabla\n" +
                    "            const thead = document.getElementById('tableHead');\n" +
                    "            let headerRow = '<tr><th>Canal</th>';\n" +
                    "            for (let u = 1; u <= maxUniverses; u++) {\n" +
                    "                headerRow += `<th>Universo ${u}</th>`;\n" +
                    "            }\n" +
                    "            headerRow += '</tr>';\n" +
                    "            thead.innerHTML = headerRow;\n" +
                    "            \n" +
                    "            // Construir filas de la tabla\n" +
                    "            const tbody = document.getElementById('tableBody');\n" +
                    "            let tableRows = '';\n" +
                    "            \n" +
                    "            for (let channel = 1; channel <= maxChannels; channel++) {\n" +
                    "                let row = `<tr><td>${channel}</td>`;\n" +
                    "                let hasData = false;\n" +
                    "                \n" +
                    "                for (let universe = 1; universe <= maxUniverses; universe++) {\n" +
                    "                    // Los keys del objeto JSON son strings\n" +
                    "                    const channelMap = fullDmxMap[channel] || fullDmxMap[channel.toString()];\n" +
                    "                    const entry = channelMap && (channelMap[universe] || channelMap[universe.toString()]);\n" +
                    "                    \n" +
                    "                    if (entry && entry.fixtureName) {\n" +
                    "                        // Filtrar por búsqueda si hay texto\n" +
                    "                        if (searchInput && !entry.fixtureName.toLowerCase().includes(searchInput) && \n" +
                    "                            !entry.fixtureModel.toLowerCase().includes(searchInput)) {\n" +
                    "                            row += '<td class=\"empty-channel\">-</td>';\n" +
                    "                        } else {\n" +
                    "                            hasData = true;\n" +
                    "                            const channelName = entry.channelName ? `<div class=\"channel-name\">${entry.channelName}</div>` : '';\n" +
                    "                            row += `<td><div class=\"fixture-name\">${entry.fixtureName}</div>${channelName}</td>`;\n" +
                    "                        }\n" +
                    "                    } else {\n" +
                    "                        row += '<td class=\"empty-channel\">-</td>';\n" +
                    "                    }\n" +
                    "                }\n" +
                    "                \n" +
                    "                row += '</tr>';\n" +
                    "                \n" +
                    "                // Solo mostrar filas con datos o si no hay filtro de búsqueda\n" +
                    "                if (!searchInput || hasData) {\n" +
                    "                    tableRows += row;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (tableRows === '') {\n" +
                    "                tbody.innerHTML = `<tr><td colspan=\"${maxUniverses + 1}\" class=\"empty\">No se encontraron canales</td></tr>`;\n" +
                    "            } else {\n" +
                    "                tbody.innerHTML = tableRows;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function exportToCSV() {\n" +
                    "            if (!fullDmxMap || Object.keys(fullDmxMap).length === 0) {\n" +
                    "                alert('No hay datos para exportar');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Construir encabezados\n" +
                    "            let csv = 'Canal';\n" +
                    "            for (let u = 1; u <= maxUniverses; u++) {\n" +
                    "                csv += `,Universo ${u}`;\n" +
                    "            }\n" +
                    "            csv += '\\n';\n" +
                    "            \n" +
                    "            // Construir filas\n" +
                    "            for (let channel = 1; channel <= maxChannels; channel++) {\n" +
                    "                csv += channel;\n" +
                    "                \n" +
                    "                for (let universe = 1; universe <= maxUniverses; universe++) {\n" +
                    "                    const channelMap = fullDmxMap[channel] || fullDmxMap[channel.toString()];\n" +
                    "                    const entry = channelMap && (channelMap[universe] || channelMap[universe.toString()]);\n" +
                    "                    \n" +
                    "                    if (entry && entry.fixtureName) {\n" +
                    "                        let cellValue = entry.fixtureName;\n" +
                    "                        if (entry.channelName) {\n" +
                    "                            cellValue += ` (${entry.channelName})`;\n" +
                    "                        }\n" +
                    "                        // Escapar comillas y comas en CSV\n" +
                    "                        cellValue = cellValue.replace(/\"/g, '\"\"');\n" +
                    "                        if (cellValue.includes(',') || cellValue.includes('\"') || cellValue.includes('\\n')) {\n" +
                    "                            cellValue = `\"${cellValue}\"`;\n" +
                    "                        }\n" +
                    "                        csv += `,${cellValue}`;\n" +
                    "                    } else {\n" +
                    "                        csv += ',-';\n" +
                    "                    }\n" +
                    "                }\n" +
                    "                \n" +
                    "                csv += '\\n';\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Crear y descargar archivo\n" +
                    "            const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });\n" +
                    "            const link = document.createElement('a');\n" +
                    "            const url = URL.createObjectURL(blob);\n" +
                    "            link.setAttribute('href', url);\n" +
                    "            link.setAttribute('download', `dmx-map-${new Date().toISOString().split('T')[0]}.csv`);\n" +
                    "            link.style.visibility = 'hidden';\n" +
                    "            document.body.appendChild(link);\n" +
                    "            link.click();\n" +
                    "            document.body.removeChild(link);\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Event listeners\n" +
                    "        document.getElementById('searchInput').addEventListener('input', renderTable);\n" +
                    "        \n" +
                    "        // Cargar datos al iniciar\n" +
                    "        loadDmxMap();\n" +
                    "        \n" +
                    "        // Recargar cada 30 segundos\n" +
                    "        setInterval(loadDmxMap, 30000);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }

        private String getNetworkConfigHtml() {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Configuración de Red - ArtNet</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            max-width: 1000px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background: white;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                    "            padding: 30px;\n" +
                    "        }\n" +
                    "        h1 {\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 2em;\n" +
                    "        }\n" +
                    "        .subtitle {\n" +
                    "            color: #666;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "        }\n" +
                    "        .section {\n" +
                    "            margin-bottom: 40px;\n" +
                    "        }\n" +
                    "        .section-title {\n" +
                    "            font-size: 1.5em;\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            padding-bottom: 10px;\n" +
                    "            border-bottom: 2px solid #eee;\n" +
                    "        }\n" +
                    "        .interface-card {\n" +
                    "            background: #f8f9fa;\n" +
                    "            border: 2px solid #e0e0e0;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 20px;\n" +
                    "            margin-bottom: 15px;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .interface-card:hover {\n" +
                    "            border-color: #667eea;\n" +
                    "            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);\n" +
                    "        }\n" +
                    "        .interface-card.selected {\n" +
                    "            border-color: #28a745;\n" +
                    "            background: #f0fff4;\n" +
                    "        }\n" +
                    "        .interface-header {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            margin-bottom: 10px;\n" +
                    "        }\n" +
                    "        .interface-name {\n" +
                    "            font-size: 1.2em;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .interface-ip {\n" +
                    "            font-size: 1.1em;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #667eea;\n" +
                    "            font-family: 'Courier New', monospace;\n" +
                    "        }\n" +
                    "        .interface-details {\n" +
                    "            color: #666;\n" +
                    "            font-size: 0.9em;\n" +
                    "            margin-top: 10px;\n" +
                    "        }\n" +
                    "        .interface-details span {\n" +
                    "            margin-right: 15px;\n" +
                    "        }\n" +
                    "        .select-btn {\n" +
                    "            padding: 8px 20px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .select-btn:hover {\n" +
                    "            background: #5568d3;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .select-btn.selected {\n" +
                    "            background: #28a745;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .empty {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "        .current-config {\n" +
                    "            background: #e7f3ff;\n" +
                    "            border-left: 4px solid #2196F3;\n" +
                    "            padding: 15px;\n" +
                    "            border-radius: 6px;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .current-config strong {\n" +
                    "            color: #1976D2;\n" +
                    "        }\n" +
                    "        .refresh-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .refresh-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "        }\n" +
                    "        .back-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "            text-decoration: none;\n" +
                    "            display: inline-block;\n" +
                    "        }\n" +
                    "        .back-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <a href=\"/\" class=\"back-btn\">← Volver</a>\n" +
                    "        <h1>⚙️ Configuración de Red</h1>\n" +
                    "        <p class=\"subtitle\">Configuración de interfaces de red para ArtNet</p>\n" +
                    "        \n" +
                    "        <div class=\"current-config\" id=\"currentConfig\">\n" +
                    "            <strong>IP ArtNet actual:</strong> <span id=\"currentArtNetIp\">Cargando...</span>\n" +
                    "        </div>\n" +
                    "        <div id=\"saveMessage\" style=\"display: none; padding: 10px; margin-bottom: 20px; border-radius: 6px; background: #d4edda; color: #155724; border: 1px solid #c3e6cb;\"></div>\n" +
                    "        \n" +
                    "        <div class=\"section\">\n" +
                    "            <div style=\"display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;\">\n" +
                    "                <h2 class=\"section-title\">Interfaces de Red Cableadas</h2>\n" +
                    "                <button class=\"refresh-btn\" onclick=\"loadInterfaces()\">🔄 Actualizar</button>\n" +
                    "            </div>\n" +
                    "            <div id=\"interfacesList\">\n" +
                    "                <div class=\"loading\">Cargando interfaces de red...</div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        let selectedIp = null;\n" +
                    "        let currentArtNetIp = null;\n" +
                    "        \n" +
                    "        async function loadCurrentConfig() {\n" +
                    "            try {\n" +
                    "                const response = await fetch('/api/external-config');\n" +
                    "                const data = await response.json();\n" +
                    "                currentArtNetIp = data.artNet.ipAddress;\n" +
                    "                selectedIp = currentArtNetIp;\n" +
                    "                document.getElementById('currentArtNetIp').textContent = currentArtNetIp;\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando configuración:', error);\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function loadInterfaces() {\n" +
                    "            try {\n" +
                    "                document.getElementById('interfacesList').innerHTML = '<div class=\"loading\">Cargando interfaces de red...</div>';\n" +
                    "                \n" +
                    "                const response = await fetch('/api/network/interfaces');\n" +
                    "                const data = await response.json();\n" +
                    "                const interfaces = data.interfaces || [];\n" +
                    "                \n" +
                    "                if (interfaces.length === 0) {\n" +
                    "                    document.getElementById('interfacesList').innerHTML = \n" +
                    "                        '<div class=\"empty\">No se encontraron interfaces de red cableadas con IPs IPv4</div>';\n" +
                    "                    return;\n" +
                    "                }\n" +
                    "                \n" +
                    "                let html = '';\n" +
                    "                interfaces.forEach((iface, index) => {\n" +
                    "                    const isSelected = selectedIp === iface.ipAddress;\n" +
                    "                    html += `\n" +
                    "                        <div class=\"interface-card ${isSelected ? 'selected' : ''}\" id=\"interface-${index}\">\n" +
                    "                            <div class=\"interface-header\">\n" +
                    "                                <div>\n" +
                    "                                    <div class=\"interface-name\">${iface.displayName || iface.name}</div>\n" +
                    "                                    <div class=\"interface-ip\">${iface.ipAddress}</div>\n" +
                    "                                </div>\n" +
                    "                                <button class=\"select-btn ${isSelected ? 'selected' : ''}\" onclick=\"selectInterface('${iface.ipAddress}', ${index})\">\n" +
                    "                                    ${isSelected ? '✓ Guardada' : 'Guardar como IP ArtNet'}\n" +
                    "                                </button>\n" +
                    "                            </div>\n" +
                    "                            <div class=\"interface-details\">\n" +
                    "                                <span><strong>Interfaz:</strong> ${iface.name}</span>\n" +
                    "                                ${iface.subnetMask ? `<span><strong>Máscara:</strong> ${iface.subnetMask}</span>` : ''}\n" +
                    "                                ${iface.macAddress ? `<span><strong>MAC:</strong> ${iface.macAddress}</span>` : ''}\n" +
                    "                                <span><strong>Estado:</strong> ${iface.isUp ? 'Activa' : 'Inactiva'}</span>\n" +
                    "                            </div>\n" +
                    "                        </div>\n" +
                    "                    `;\n" +
                    "                });\n" +
                    "                \n" +
                    "                document.getElementById('interfacesList').innerHTML = html;\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando interfaces:', error);\n" +
                    "                document.getElementById('interfacesList').innerHTML = \n" +
                    "                    '<div class=\"empty\">Error al cargar las interfaces de red</div>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function selectInterface(ip, index) {\n" +
                    "            try {\n" +
                    "                // Guardar en el servidor\n" +
                    "                const response = await fetch('/api/external-config/artnet-ip', {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: {\n" +
                    "                        'Content-Type': 'application/json'\n" +
                    "                    },\n" +
                    "                    body: JSON.stringify({ ipAddress: ip })\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (response.ok) {\n" +
                    "                    selectedIp = ip;\n" +
                    "                    currentArtNetIp = ip;\n" +
                    "                    \n" +
                    "                    // Actualizar UI\n" +
                    "                    document.querySelectorAll('.interface-card').forEach(card => {\n" +
                    "                        card.classList.remove('selected');\n" +
                    "                    });\n" +
                    "                    document.querySelectorAll('.select-btn').forEach(btn => {\n" +
                    "                        btn.classList.remove('selected');\n" +
                    "                        btn.textContent = 'Guardar como IP ArtNet';\n" +
                    "                    });\n" +
                    "                    \n" +
                    "                    const card = document.getElementById(`interface-${index}`);\n" +
                    "                    const btn = card.querySelector('.select-btn');\n" +
                    "                    card.classList.add('selected');\n" +
                    "                    btn.classList.add('selected');\n" +
                    "                    btn.textContent = '✓ Guardada';\n" +
                    "                    \n" +
                    "                    // Actualizar IP actual\n" +
                    "                    document.getElementById('currentArtNetIp').textContent = ip;\n" +
                    "                    \n" +
                    "                    // Mostrar mensaje de éxito\n" +
                    "                    const messageDiv = document.getElementById('saveMessage');\n" +
                    "                    messageDiv.textContent = '✓ IP ArtNet actualizada correctamente: ' + ip;\n" +
                    "                    messageDiv.style.display = 'block';\n" +
                    "                    setTimeout(() => {\n" +
                    "                        messageDiv.style.display = 'none';\n" +
                    "                    }, 3000);\n" +
                    "                } else {\n" +
                    "                    alert('Error al guardar: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error al guardar IP:', error);\n" +
                    "                alert('Error al guardar la configuración');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Cargar configuración e interfaces al iniciar\n" +
                    "        loadCurrentConfig().then(() => loadInterfaces());\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }

        private String getScenesConfigHtml() {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Configuración de Escenas</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            max-width: 1400px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background: white;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                    "            padding: 30px;\n" +
                    "        }\n" +
                    "        h1 {\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 2em;\n" +
                    "        }\n" +
                    "        .subtitle {\n" +
                    "            color: #666;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "        }\n" +
                    "        .back-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "            text-decoration: none;\n" +
                    "            display: inline-block;\n" +
                    "        }\n" +
                    "        .back-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .controls {\n" +
                    "            margin-bottom: 20px;\n" +
                    "            display: flex;\n" +
                    "            gap: 15px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: space-between;\n" +
                    "        }\n" +
                    "        .refresh-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .refresh-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "        }\n" +
                    "        .table-container {\n" +
                    "            overflow-x: auto;\n" +
                    "            border-radius: 8px;\n" +
                    "            box-shadow: 0 2px 8px rgba(0,0,0,0.1);\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            width: 100%;\n" +
                    "            border-collapse: collapse;\n" +
                    "            background: white;\n" +
                    "        }\n" +
                    "        thead {\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        th {\n" +
                    "            padding: 15px;\n" +
                    "            text-align: left;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        tbody tr {\n" +
                    "            border-bottom: 1px solid #eee;\n" +
                    "            transition: background-color 0.2s;\n" +
                    "        }\n" +
                    "        tbody tr:hover {\n" +
                    "            background-color: #f8f9fa;\n" +
                    "        }\n" +
                    "        tbody tr:nth-child(even) {\n" +
                    "            background-color: #fafafa;\n" +
                    "        }\n" +
                    "        td {\n" +
                    "            padding: 12px 15px;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .id-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-weight: 600;\n" +
                    "            font-size: 0.9em;\n" +
                    "        }\n" +
                    "        .subtype-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #28a745;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-size: 0.85em;\n" +
                    "        }\n" +
                    "        .edit-btn {\n" +
                    "            padding: 6px 12px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 4px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-size: 0.9em;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .edit-btn:hover {\n" +
                    "            background: #5568d3;\n" +
                    "        }\n" +
                    "        .modal {\n" +
                    "            display: none;\n" +
                    "            position: fixed;\n" +
                    "            z-index: 1000;\n" +
                    "            left: 0;\n" +
                    "            top: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            background-color: rgba(0,0,0,0.5);\n" +
                    "        }\n" +
                    "        .modal-content {\n" +
                    "            background-color: white;\n" +
                    "            margin: 5% auto;\n" +
                    "            padding: 30px;\n" +
                    "            border-radius: 12px;\n" +
                    "            width: 90%;\n" +
                    "            max-width: 600px;\n" +
                    "            max-height: 80vh;\n" +
                    "            overflow-y: auto;\n" +
                    "        }\n" +
                    "        .modal-header {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .close {\n" +
                    "            color: #aaa;\n" +
                    "            font-size: 28px;\n" +
                    "            font-weight: bold;\n" +
                    "            cursor: pointer;\n" +
                    "        }\n" +
                    "        .close:hover {\n" +
                    "            color: #000;\n" +
                    "        }\n" +
                    "        .form-group {\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        label {\n" +
                    "            display: block;\n" +
                    "            margin-bottom: 5px;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        input, select, textarea {\n" +
                    "            width: 100%;\n" +
                    "            padding: 10px;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            font-size: 14px;\n" +
                    "        }\n" +
                    "        input:focus, select:focus, textarea:focus {\n" +
                    "            outline: none;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        .save-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #28a745;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-right: 10px;\n" +
                    "        }\n" +
                    "        .save-btn:hover {\n" +
                    "            background: #218838;\n" +
                    "        }\n" +
                    "        .cancel-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        .cancel-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .empty {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "        .message {\n" +
                    "            padding: 10px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            border-radius: 6px;\n" +
                    "            display: none;\n" +
                    "        }\n" +
                    "        .message.success {\n" +
                    "            background: #d4edda;\n" +
                    "            color: #155724;\n" +
                    "            border: 1px solid #c3e6cb;\n" +
                    "        }\n" +
                    "        .message.error {\n" +
                    "            background: #f8d7da;\n" +
                    "            color: #721c24;\n" +
                    "            border: 1px solid #f5c6cb;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <a href=\"/\" class=\"back-btn\">← Volver</a>\n" +
                    "        <h1>🎭 Configuración de Escenas</h1>\n" +
                    "        <p class=\"subtitle\">Gestionar y editar escenas del sistema</p>\n" +
                    "        \n" +
                    "        <div id=\"message\" class=\"message\"></div>\n" +
                    "        \n" +
                    "        <div class=\"controls\">\n" +
                    "            <div class=\"filter-group\" style=\"display: flex; gap: 15px; align-items: center; flex-wrap: wrap;\">\n" +
                    "                <div style=\"display: flex; gap: 10px; align-items: center;\">\n" +
                    "                    <label for=\"pathFilter\" style=\"font-weight: 600; color: #555;\">Filtrar por Path:</label>\n" +
                    "                    <select id=\"pathFilter\" style=\"padding: 8px 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 14px;\">\n" +
                    "                        <option value=\"\">Todos</option>\n" +
                    "                    </select>\n" +
                    "                </div>\n" +
                    "                <div style=\"display: flex; gap: 10px; align-items: center;\">\n" +
                    "                    <label for=\"subTypeFilter\" style=\"font-weight: 600; color: #555;\">Filtrar por SubType:</label>\n" +
                    "                    <select id=\"subTypeFilter\" style=\"padding: 8px 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 14px;\">\n" +
                    "                        <option value=\"\">Todos</option>\n" +
                    "                        <option value=\"robotic.position.static\">robotic.position.static</option>\n" +
                    "                    </select>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            <button class=\"refresh-btn\" onclick=\"loadScenes()\">🔄 Actualizar</button>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"table-container\">\n" +
                    "            <table id=\"scenesTable\">\n" +
                    "                <thead>\n" +
                    "                    <tr>\n" +
                    "                        <th>ID</th>\n" +
                    "                        <th>Nombre</th>\n" +
                    "                        <th>Path</th>\n" +
                    "                        <th>SubType</th>\n" +
                    "                        <th>Puntos</th>\n" +
                    "                        <th>Acciones</th>\n" +
                    "                    </tr>\n" +
                    "                </thead>\n" +
                    "                <tbody id=\"tableBody\">\n" +
                    "                    <tr>\n" +
                    "                        <td colspan=\"6\" class=\"loading\">Cargando escenas...</td>\n" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "            </table>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <!-- Modal para editar escena -->\n" +
                    "    <div id=\"editModal\" class=\"modal\">\n" +
                    "        <div class=\"modal-content\">\n" +
                    "            <div class=\"modal-header\">\n" +
                    "                <h2>Editar Escena</h2>\n" +
                    "                <span class=\"close\" onclick=\"closeModal()\">&times;</span>\n" +
                    "            </div>\n" +
                    "            <form id=\"editForm\">\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label>ID:</label>\n" +
                    "                    <input type=\"text\" id=\"editId\" readonly style=\"background: #f5f5f5;\">\n" +
                    "                </div>\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label>Nombre:</label>\n" +
                    "                    <input type=\"text\" id=\"editName\" required>\n" +
                    "                </div>\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label>Path:</label>\n" +
                    "                    <input type=\"text\" id=\"editPath\" readonly style=\"background: #f5f5f5;\">\n" +
                    "                </div>\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label>SubType:</label>\n" +
                    "                    <input type=\"text\" id=\"editSubType\" placeholder=\"Ej: robotic.position.static\">\n" +
                    "                </div>\n" +
                    "                <div style=\"text-align: right; margin-top: 20px;\">\n" +
                    "                    <button type=\"button\" class=\"cancel-btn\" onclick=\"closeModal()\">Cancelar</button>\n" +
                    "                    <button type=\"submit\" class=\"save-btn\">Guardar</button>\n" +
                    "                </div>\n" +
                    "            </form>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        let scenes = [];\n" +
                    "        let currentEditingScene = null;\n" +
                    "        \n" +
                    "        async function loadScenes() {\n" +
                    "            try {\n" +
                    "                document.getElementById('tableBody').innerHTML = '<tr><td colspan=\"6\" class=\"loading\">Cargando escenas...</td></tr>';\n" +
                    "                \n" +
                    "                const response = await fetch('/api/scenes');\n" +
                    "                const data = await response.json();\n" +
                    "                scenes = data.scenes || [];\n" +
                    "                \n" +
                    "                populatePathFilter();\n" +
                    "                renderTable();\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando escenas:', error);\n" +
                    "                document.getElementById('tableBody').innerHTML = \n" +
                    "                    '<tr><td colspan=\"6\" class=\"empty\">Error al cargar las escenas</td></tr>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function populatePathFilter() {\n" +
                    "            const pathFilter = document.getElementById('pathFilter');\n" +
                    "            const currentValue = pathFilter.value;\n" +
                    "            \n" +
                    "            // Obtener paths únicos de las escenas\n" +
                    "            const paths = [...new Set(scenes.map(scene => scene.path).filter(p => p))].sort();\n" +
                    "            \n" +
                    "            // Limpiar opciones excepto Todos\n" +
                    "            pathFilter.innerHTML = '<option value=\"\">Todos</option>';\n" +
                    "            \n" +
                    "            // Agregar opciones de paths\n" +
                    "            paths.forEach(path => {\n" +
                    "                const option = document.createElement('option');\n" +
                    "                option.value = path;\n" +
                    "                option.textContent = path;\n" +
                    "                pathFilter.appendChild(option);\n" +
                    "            });\n" +
                    "            \n" +
                    "            // Restaurar valor anterior si existe\n" +
                    "            if (currentValue) {\n" +
                    "                pathFilter.value = currentValue;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderTable() {\n" +
                    "            const tbody = document.getElementById('tableBody');\n" +
                    "            const pathFilter = document.getElementById('pathFilter').value;\n" +
                    "            const subTypeFilter = document.getElementById('subTypeFilter').value;\n" +
                    "            \n" +
                    "            // Filtrar escenas por path y subType\n" +
                    "            let filteredScenes = scenes;\n" +
                    "            if (pathFilter) {\n" +
                    "                filteredScenes = filteredScenes.filter(scene => scene.path === pathFilter);\n" +
                    "            }\n" +
                    "            if (subTypeFilter) {\n" +
                    "                filteredScenes = filteredScenes.filter(scene => scene.subType === subTypeFilter);\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (filteredScenes.length === 0) {\n" +
                    "                const filters = [];\n" +
                    "                if (pathFilter) filters.push('path \"' + pathFilter + '\"');\n" +
                    "                if (subTypeFilter) filters.push('subType \"' + subTypeFilter + '\"');\n" +
                    "                const filterText = filters.length > 0 ? ' con ' + filters.join(' y ') : '';\n" +
                    "                tbody.innerHTML = '<tr><td colspan=\"6\" class=\"empty\">No se encontraron escenas' + filterText + '</td></tr>';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            tbody.innerHTML = filteredScenes.map(scene => {\n" +
                    "                const subTypeDisplay = scene.subType ? \n" +
                    "                    `<span class=\"subtype-badge\">${scene.subType}</span>` : \n" +
                    "                    '<span style=\"color: #999;\">-</span>';\n" +
                    "                \n" +
                    "                return `\n" +
                    "                    <tr>\n" +
                    "                        <td><span class=\"id-badge\">${scene.id}</span></td>\n" +
                    "                        <td><strong>${scene.name || '-'}</strong></td>\n" +
                    "                        <td>${scene.path || '-'}</td>\n" +
                    "                        <td>${subTypeDisplay}</td>\n" +
                    "                        <td>${scene.pointCount || 0}</td>\n" +
                    "                        <td>\n" +
                    "                            ${scene.subType === 'robotic.position.static' ? \n" +
                    "                                `<a href=\"/scene-position-edit.html?id=${scene.id}\" class=\"edit-btn\" style=\"text-decoration: none; display: inline-block;\">✏️ Editar Posición</a>` : \n" +
                    "                                `<button class=\"edit-btn\" onclick=\"editScene(${scene.id})\">✏️ Editar</button>`\n" +
                    "                            }\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                `;\n" +
                    "            }).join('');\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function editScene(sceneId) {\n" +
                    "            try {\n" +
                    "                const response = await fetch(`/api/scenes/${sceneId}`);\n" +
                    "                const scene = await response.json();\n" +
                    "                \n" +
                    "                currentEditingScene = scene;\n" +
                    "                document.getElementById('editId').value = scene.id;\n" +
                    "                document.getElementById('editName').value = scene.name || '';\n" +
                    "                document.getElementById('editPath').value = scene.path || '';\n" +
                    "                document.getElementById('editSubType').value = scene.subType || '';\n" +
                    "                \n" +
                    "                document.getElementById('editModal').style.display = 'block';\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando escena:', error);\n" +
                    "                showMessage('Error al cargar la escena', 'error');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function closeModal() {\n" +
                    "            document.getElementById('editModal').style.display = 'none';\n" +
                    "            currentEditingScene = null;\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.getElementById('editForm').addEventListener('submit', async function(e) {\n" +
                    "            e.preventDefault();\n" +
                    "            \n" +
                    "            if (!currentEditingScene) return;\n" +
                    "            \n" +
                    "            const sceneId = currentEditingScene.id;\n" +
                    "            const updateData = {\n" +
                    "                name: document.getElementById('editName').value,\n" +
                    "                subType: document.getElementById('editSubType').value || null\n" +
                    "            };\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const response = await fetch(`/api/scenes/${sceneId}/update`, {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: {\n" +
                    "                        'Content-Type': 'application/json'\n" +
                    "                    },\n" +
                    "                    body: JSON.stringify(updateData)\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (response.ok) {\n" +
                    "                    showMessage('Escena actualizada correctamente', 'success');\n" +
                    "                    closeModal();\n" +
                    "                    loadScenes();\n" +
                    "                } else {\n" +
                    "                    showMessage('Error: ' + (result.error || 'Error desconocido'), 'error');\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error actualizando escena:', error);\n" +
                    "                showMessage('Error al actualizar la escena', 'error');\n" +
                    "            }\n" +
                    "        });\n" +
                    "        \n" +
                    "        function showMessage(text, type) {\n" +
                    "            const messageDiv = document.getElementById('message');\n" +
                    "            messageDiv.textContent = text;\n" +
                    "            messageDiv.className = 'message ' + type;\n" +
                    "            messageDiv.style.display = 'block';\n" +
                    "            setTimeout(() => {\n" +
                    "                messageDiv.style.display = 'none';\n" +
                    "            }, 3000);\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Cerrar modal al hacer clic fuera\n" +
                    "        window.onclick = function(event) {\n" +
                    "            const modal = document.getElementById('editModal');\n" +
                    "            if (event.target == modal) {\n" +
                    "                closeModal();\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Event listeners para los filtros\n" +
                    "        document.getElementById('pathFilter').addEventListener('change', renderTable);\n" +
                    "        document.getElementById('subTypeFilter').addEventListener('change', renderTable);\n" +
                    "        \n" +
                    "        // Cargar escenas al iniciar\n" +
                    "        loadScenes();\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }

        private String getCollectionsConfigHtml() {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Configuración de Collections</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            max-width: 1400px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background: white;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                    "            padding: 30px;\n" +
                    "        }\n" +
                    "        h1 {\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 2em;\n" +
                    "        }\n" +
                    "        .subtitle {\n" +
                    "            color: #666;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "        }\n" +
                    "        .back-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "            text-decoration: none;\n" +
                    "            display: inline-block;\n" +
                    "        }\n" +
                    "        .back-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .controls {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "            gap: 15px;\n" +
                    "        }\n" +
                    "        .filter-group {\n" +
                    "            display: flex;\n" +
                    "            gap: 15px;\n" +
                    "            align-items: center;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .refresh-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .refresh-btn:hover {\n" +
                    "            background: #5568d3;\n" +
                    "        }\n" +
                    "        .table-container {\n" +
                    "            overflow-x: auto;\n" +
                    "            border-radius: 8px;\n" +
                    "            box-shadow: 0 2px 8px rgba(0,0,0,0.1);\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            width: 100%;\n" +
                    "            border-collapse: collapse;\n" +
                    "            background: white;\n" +
                    "        }\n" +
                    "        thead {\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        th {\n" +
                    "            padding: 15px;\n" +
                    "            text-align: left;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        tbody tr {\n" +
                    "            border-bottom: 1px solid #eee;\n" +
                    "            transition: background-color 0.2s;\n" +
                    "        }\n" +
                    "        tbody tr:hover {\n" +
                    "            background-color: #f8f9fa;\n" +
                    "        }\n" +
                    "        tbody tr:nth-child(even) {\n" +
                    "            background-color: #fafafa;\n" +
                    "        }\n" +
                    "        td {\n" +
                    "            padding: 12px 15px;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .id-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-weight: 600;\n" +
                    "            font-size: 0.9em;\n" +
                    "        }\n" +
                    "        .edit-btn {\n" +
                    "            padding: 6px 12px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 4px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-size: 0.9em;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .edit-btn:hover {\n" +
                    "            background: #5568d3;\n" +
                    "        }\n" +
                    "        .modal {\n" +
                    "            display: none;\n" +
                    "            position: fixed;\n" +
                    "            z-index: 1000;\n" +
                    "            left: 0;\n" +
                    "            top: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            background-color: rgba(0,0,0,0.5);\n" +
                    "        }\n" +
                    "        .modal-content {\n" +
                    "            background-color: white;\n" +
                    "            margin: 5% auto;\n" +
                    "            padding: 30px;\n" +
                    "            border-radius: 12px;\n" +
                    "            width: 90%;\n" +
                    "            max-width: 600px;\n" +
                    "            max-height: 80vh;\n" +
                    "            overflow-y: auto;\n" +
                    "        }\n" +
                    "        .modal-header {\n" +
                    "            display: flex;\n" +
                    "            justify-content: space-between;\n" +
                    "            align-items: center;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .close {\n" +
                    "            color: #aaa;\n" +
                    "            font-size: 28px;\n" +
                    "            font-weight: bold;\n" +
                    "            cursor: pointer;\n" +
                    "        }\n" +
                    "        .close:hover {\n" +
                    "            color: #000;\n" +
                    "        }\n" +
                    "        .form-group {\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        label {\n" +
                    "            display: block;\n" +
                    "            margin-bottom: 5px;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        input, select, textarea {\n" +
                    "            width: 100%;\n" +
                    "            padding: 10px;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            font-size: 14px;\n" +
                    "        }\n" +
                    "        input:focus, select:focus, textarea:focus {\n" +
                    "            outline: none;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        .save-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #28a745;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-right: 10px;\n" +
                    "        }\n" +
                    "        .save-btn:hover {\n" +
                    "            background: #218838;\n" +
                    "        }\n" +
                    "        .cancel-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        .cancel-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .empty {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "        .shows-list {\n" +
                    "            max-height: 200px;\n" +
                    "            overflow-y: auto;\n" +
                    "            border: 1px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            padding: 10px;\n" +
                    "        }\n" +
                    "        .show-item {\n" +
                    "            padding: 5px;\n" +
                    "            margin: 5px 0;\n" +
                    "            background: #f8f9fa;\n" +
                    "            border-radius: 4px;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <a href=\"/\" class=\"back-btn\">← Volver</a>\n" +
                    "        <h1>📚 Configuración de Collections</h1>\n" +
                    "        <p class=\"subtitle\">Gestiona las collections y sus shows asociados</p>\n" +
                    "        \n" +
                    "        <div class=\"controls\">\n" +
                    "            <div class=\"filter-group\">\n" +
                    "                <label for=\"pathFilter\" style=\"font-weight: 600; color: #555;\">Filtrar por Path:</label>\n" +
                    "                <select id=\"pathFilter\" style=\"padding: 8px 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 14px;\">\n" +
                    "                    <option value=\"\">Todos</option>\n" +
                    "                </select>\n" +
                    "            </div>\n" +
                    "            <button class=\"refresh-btn\" onclick=\"loadCollections()\">🔄 Actualizar</button>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"table-container\">\n" +
                    "            <table id=\"collectionsTable\">\n" +
                    "                <thead>\n" +
                    "                    <tr>\n" +
                    "                        <th>ID</th>\n" +
                    "                        <th>Nombre</th>\n" +
                    "                        <th>Path</th>\n" +
                    "                        <th>Shows</th>\n" +
                    "                        <th>Acciones</th>\n" +
                    "                    </tr>\n" +
                    "                </thead>\n" +
                    "                <tbody id=\"tableBody\">\n" +
                    "                    <tr>\n" +
                    "                        <td colspan=\"5\" class=\"loading\">Cargando collections...</td>\n" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "            </table>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <!-- Modal de edición -->\n" +
                    "    <div id=\"editModal\" class=\"modal\">\n" +
                    "        <div class=\"modal-content\">\n" +
                    "            <div class=\"modal-header\">\n" +
                    "                <h2>Editar Collection</h2>\n" +
                    "                <span class=\"close\" onclick=\"closeModal()\">&times;</span>\n" +
                    "            </div>\n" +
                    "            <form id=\"editForm\">\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label for=\"editName\">Nombre:</label>\n" +
                    "                    <input type=\"text\" id=\"editName\" required>\n" +
                    "                </div>\n" +
                    "                <div class=\"form-group\">\n" +
                    "                    <label>Shows en la Collection:</label>\n" +
                    "                    <div id=\"showsList\" class=\"shows-list\">\n" +
                    "                        <div class=\"loading\">Cargando shows...</div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                <div style=\"margin-top: 20px;\">\n" +
                    "                    <button type=\"submit\" class=\"save-btn\">💾 Guardar</button>\n" +
                    "                    <button type=\"button\" class=\"cancel-btn\" onclick=\"closeModal()\">Cancelar</button>\n" +
                    "                </div>\n" +
                    "            </form>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        let collections = [];\n" +
                    "        let allShows = [];\n" +
                    "        let currentEditingCollection = null;\n" +
                    "        \n" +
                    "        async function loadCollections() {\n" +
                    "            try {\n" +
                    "                document.getElementById('tableBody').innerHTML = \n" +
                    "                    '<tr><td colspan=\"5\" class=\"loading\">Cargando collections...</td></tr>';\n" +
                    "                \n" +
                    "                const response = await fetch('/api/collections');\n" +
                    "                const data = await response.json();\n" +
                    "                collections = data.collections || [];\n" +
                    "                \n" +
                    "                // Cargar todos los shows disponibles\n" +
                    "                const showsResponse = await fetch('/api/shows');\n" +
                    "                const showsData = await showsResponse.json();\n" +
                    "                allShows = showsData.shows || [];\n" +
                    "                \n" +
                    "                populatePathFilter();\n" +
                    "                renderTable();\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando collections:', error);\n" +
                    "                document.getElementById('tableBody').innerHTML = \n" +
                    "                    '<tr><td colspan=\"5\" class=\"empty\">Error al cargar las collections</td></tr>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function populatePathFilter() {\n" +
                    "            const pathFilter = document.getElementById('pathFilter');\n" +
                    "            const currentValue = pathFilter.value;\n" +
                    "            \n" +
                    "            const paths = [...new Set(collections.map(c => c.path).filter(p => p))].sort();\n" +
                    "            \n" +
                    "            pathFilter.innerHTML = '<option value=\"\">Todos</option>';\n" +
                    "            paths.forEach(path => {\n" +
                    "                const option = document.createElement('option');\n" +
                    "                option.value = path;\n" +
                    "                option.textContent = path;\n" +
                    "                pathFilter.appendChild(option);\n" +
                    "            });\n" +
                    "            \n" +
                    "            if (currentValue) {\n" +
                    "                pathFilter.value = currentValue;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderTable() {\n" +
                    "            const tbody = document.getElementById('tableBody');\n" +
                    "            const pathFilter = document.getElementById('pathFilter').value;\n" +
                    "            \n" +
                    "            let filteredCollections = collections;\n" +
                    "            if (pathFilter) {\n" +
                    "                filteredCollections = filteredCollections.filter(c => c.path === pathFilter);\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (filteredCollections.length === 0) {\n" +
                    "                const filterText = pathFilter ? ' con path \"' + pathFilter + '\"' : '';\n" +
                    "                tbody.innerHTML = '<tr><td colspan=\"5\" class=\"empty\">No se encontraron collections' + filterText + '</td></tr>';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            tbody.innerHTML = filteredCollections.map(collection => {\n" +
                    "                const showsText = collection.shows && collection.shows.length > 0 ? \n" +
                    "                    collection.shows.map(s => s.id + ': ' + (s.name || '-')).join(', ') : \n" +
                    "                    'Ninguno';\n" +
                    "                \n" +
                    "                return `\n" +
                    "                    <tr>\n" +
                    "                        <td><span class=\"id-badge\">${collection.id}</span></td>\n" +
                    "                        <td><strong>${collection.name || '-'}</strong></td>\n" +
                    "                        <td>${collection.path || '-'}</td>\n" +
                    "                        <td style=\"max-width: 400px; overflow: hidden; text-overflow: ellipsis;\" title=\"${showsText}\">${showsText}</td>\n" +
                    "                        <td>\n" +
                    "                            <button class=\"edit-btn\" onclick=\"editCollection(${collection.id})\">Editar</button>\n" +
                    "                        </td>\n" +
                    "                    </tr>\n" +
                    "                `;\n" +
                    "            }).join('');\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function editCollection(collectionId) {\n" +
                    "            try {\n" +
                    "                const response = await fetch(`/api/collections/${collectionId}`);\n" +
                    "                const collection = await response.json();\n" +
                    "                \n" +
                    "                currentEditingCollection = collection;\n" +
                    "                document.getElementById('editName').value = collection.name || '';\n" +
                    "                \n" +
                    "                // Renderizar lista de shows\n" +
                    "                const showsListDiv = document.getElementById('showsList');\n" +
                    "                const collectionShowIds = new Set((collection.shows || []).map(s => s.id));\n" +
                    "                \n" +
                    "                showsListDiv.innerHTML = allShows.map(show => {\n" +
                    "                    const checked = collectionShowIds.has(show.id) ? 'checked' : '';\n" +
                    "                    return `\n" +
                    "                        <div class=\"show-item\">\n" +
                    "                            <label style=\"display: flex; align-items: center; cursor: pointer;\">\n" +
                    "                                <input type=\"checkbox\" value=\"${show.id}\" ${checked} style=\"margin-right: 10px; width: auto;\">\n" +
                    "                                <span><strong>ID ${show.id}:</strong> ${show.name || '-'} (${show.type || '-'})</span>\n" +
                    "                            </label>\n" +
                    "                        </div>\n" +
                    "                    `;\n" +
                    "                }).join('');\n" +
                    "                \n" +
                    "                document.getElementById('editModal').style.display = 'block';\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando collection:', error);\n" +
                    "                alert('Error al cargar la collection');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function closeModal() {\n" +
                    "            document.getElementById('editModal').style.display = 'none';\n" +
                    "            currentEditingCollection = null;\n" +
                    "        }\n" +
                    "        \n" +
                    "        document.getElementById('editForm').addEventListener('submit', async function(e) {\n" +
                    "            e.preventDefault();\n" +
                    "            \n" +
                    "            if (!currentEditingCollection) return;\n" +
                    "            \n" +
                    "            const collectionId = currentEditingCollection.id;\n" +
                    "            const name = document.getElementById('editName').value;\n" +
                    "            \n" +
                    "            // Obtener IDs de shows seleccionados\n" +
                    "            const selectedShowIds = Array.from(document.querySelectorAll('#showsList input[type=\"checkbox\"]:checked'))\n" +
                    "                .map(cb => parseInt(cb.value));\n" +
                    "            \n" +
                    "            const updateData = {\n" +
                    "                name: name,\n" +
                    "                showIds: selectedShowIds\n" +
                    "            };\n" +
                    "            \n" +
                    "            try {\n" +
                    "                const response = await fetch(`/api/collections/${collectionId}/update`, {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: {\n" +
                    "                        'Content-Type': 'application/json'\n" +
                    "                    },\n" +
                    "                    body: JSON.stringify(updateData)\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (response.ok) {\n" +
                    "                    alert('Collection actualizada correctamente');\n" +
                    "                    closeModal();\n" +
                    "                    loadCollections();\n" +
                    "                } else {\n" +
                    "                    alert('Error: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error actualizando collection:', error);\n" +
                    "                alert('Error al actualizar la collection');\n" +
                    "            }\n" +
                    "        });\n" +
                    "        \n" +
                    "        // Cerrar modal al hacer clic fuera\n" +
                    "        window.onclick = function(event) {\n" +
                    "            const modal = document.getElementById('editModal');\n" +
                    "            if (event.target == modal) {\n" +
                    "                closeModal();\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Event listener para el filtro\n" +
                    "        document.getElementById('pathFilter').addEventListener('change', renderTable);\n" +
                    "        \n" +
                    "        // Cargar collections al iniciar\n" +
                    "        loadCollections();\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }

        private String getScenePositionEditHtml(String sceneIdParam) {
            return "<!DOCTYPE html>\n" +
                    "<html lang=\"es\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Editar Posiciones Robóticas</title>\n" +
                    "    <style>\n" +
                    "        * {\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        body {\n" +
                    "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            min-height: 100vh;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            max-width: 1400px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background: white;\n" +
                    "            border-radius: 12px;\n" +
                    "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                    "            padding: 30px;\n" +
                    "        }\n" +
                    "        h1 {\n" +
                    "            color: #333;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-size: 2em;\n" +
                    "        }\n" +
                    "        .subtitle {\n" +
                    "            color: #666;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            font-size: 1.1em;\n" +
                    "        }\n" +
                    "        .back-btn {\n" +
                    "            padding: 10px 20px;\n" +
                    "            background: #6c757d;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            transition: all 0.3s;\n" +
                    "            text-decoration: none;\n" +
                    "            display: inline-block;\n" +
                    "        }\n" +
                    "        .back-btn:hover {\n" +
                    "            background: #5a6268;\n" +
                    "            transform: translateY(-2px);\n" +
                    "        }\n" +
                    "        .scene-info {\n" +
                    "            background: #e7f3ff;\n" +
                    "            border-left: 4px solid #2196F3;\n" +
                    "            padding: 15px;\n" +
                    "            border-radius: 6px;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        .scene-info strong {\n" +
                    "            color: #1976D2;\n" +
                    "        }\n" +
                    "        .table-container {\n" +
                    "            overflow-x: auto;\n" +
                    "            border-radius: 8px;\n" +
                    "            box-shadow: 0 2px 8px rgba(0,0,0,0.1);\n" +
                    "        }\n" +
                    "        table {\n" +
                    "            width: 100%;\n" +
                    "            border-collapse: collapse;\n" +
                    "            background: white;\n" +
                    "        }\n" +
                    "        thead {\n" +
                    "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                    "            color: white;\n" +
                    "        }\n" +
                    "        th {\n" +
                    "            padding: 15px;\n" +
                    "            text-align: left;\n" +
                    "            font-weight: 600;\n" +
                    "        }\n" +
                    "        tbody tr {\n" +
                    "            border-bottom: 1px solid #eee;\n" +
                    "            transition: background-color 0.2s;\n" +
                    "        }\n" +
                    "        tbody tr:hover {\n" +
                    "            background-color: #f8f9fa;\n" +
                    "        }\n" +
                    "        tbody tr:nth-child(even) {\n" +
                    "            background-color: #fafafa;\n" +
                    "        }\n" +
                    "        td {\n" +
                    "            padding: 12px 15px;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .id-badge {\n" +
                    "            display: inline-block;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            padding: 4px 10px;\n" +
                    "            border-radius: 12px;\n" +
                    "            font-weight: 600;\n" +
                    "            font-size: 0.9em;\n" +
                    "        }\n" +
                    "        .coord-value {\n" +
                    "            font-family: 'Courier New', monospace;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #28a745;\n" +
                    "        }\n" +
                    "        .channel-value {\n" +
                    "            font-family: 'Courier New', monospace;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .loading {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .empty {\n" +
                    "            text-align: center;\n" +
                    "            padding: 40px;\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "        .message {\n" +
                    "            padding: 10px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            border-radius: 6px;\n" +
                    "            display: none;\n" +
                    "        }\n" +
                    "        .message.success {\n" +
                    "            background: #d4edda;\n" +
                    "            color: #155724;\n" +
                    "            border: 1px solid #c3e6cb;\n" +
                    "        }\n" +
                    "        .message.error {\n" +
                    "            background: #f8d7da;\n" +
                    "            color: #721c24;\n" +
                    "            border: 1px solid #f5c6cb;\n" +
                    "        }\n" +
                    "        .editor-container {\n" +
                    "            display: grid;\n" +
                    "            grid-template-columns: 1fr 1fr 1fr;\n" +
                    "            gap: 20px;\n" +
                    "            margin-bottom: 30px;\n" +
                    "        }\n" +
                    "        @media (max-width: 1400px) {\n" +
                    "            .editor-container {\n" +
                    "                grid-template-columns: 1fr 1fr;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        @media (max-width: 1000px) {\n" +
                    "            .editor-container {\n" +
                    "                grid-template-columns: 1fr;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        .canvas-section {\n" +
                    "            background: #f8f9fa;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .canvas-wrapper {\n" +
                    "            position: relative;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 8px;\n" +
                    "            background: white;\n" +
                    "            overflow: hidden;\n" +
                    "        }\n" +
                    "        #positionCanvas {\n" +
                    "            display: block;\n" +
                    "            cursor: crosshair;\n" +
                    "            width: 100%;\n" +
                    "            height: auto;\n" +
                    "        }\n" +
                    "        .canvas-controls {\n" +
                    "            margin-top: 15px;\n" +
                    "            display: flex;\n" +
                    "            gap: 10px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .control-btn {\n" +
                    "            padding: 8px 16px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border: none;\n" +
                    "            border-radius: 6px;\n" +
                    "            cursor: pointer;\n" +
                    "            font-weight: 600;\n" +
                    "            transition: all 0.3s;\n" +
                    "        }\n" +
                    "        .control-btn:hover {\n" +
                    "            background: #5568d3;\n" +
                    "        }\n" +
                    "        .control-btn.secondary {\n" +
                    "            background: #6c757d;\n" +
                    "        }\n" +
                    "        .control-btn.secondary:hover {\n" +
                    "            background: #5a6268;\n" +
                    "        }\n" +
                    "        .control-btn.success {\n" +
                    "            background: #28a745;\n" +
                    "        }\n" +
                    "        .control-btn.success:hover {\n" +
                    "            background: #218838;\n" +
                    "        }\n" +
                    "        .info-panel {\n" +
                    "            background: #f8f9fa;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .info-panel h3 {\n" +
                    "            margin-bottom: 15px;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .info-item {\n" +
                    "            margin-bottom: 10px;\n" +
                    "            padding: 10px;\n" +
                    "            background: white;\n" +
                    "            border-radius: 6px;\n" +
                    "            border-left: 3px solid #667eea;\n" +
                    "        }\n" +
                    "        .info-item strong {\n" +
                    "            color: #667eea;\n" +
                    "        }\n" +
                    "        .legend {\n" +
                    "            margin-top: 15px;\n" +
                    "            padding: 10px;\n" +
                    "            background: white;\n" +
                    "            border-radius: 6px;\n" +
                    "            font-size: 0.9em;\n" +
                    "            color: #666;\n" +
                    "        }\n" +
                    "        .control-panel {\n" +
                    "            background: #f8f9fa;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 20px;\n" +
                    "        }\n" +
                    "        .control-panel h3 {\n" +
                    "            margin-bottom: 15px;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .dpad-container {\n" +
                    "            display: flex;\n" +
                    "            flex-direction: column;\n" +
                    "            align-items: center;\n" +
                    "            gap: 10px;\n" +
                    "            margin-bottom: 20px;\n" +
                    "        }\n" +
                    "        .dpad-row {\n" +
                    "            display: flex;\n" +
                    "            gap: 10px;\n" +
                    "        }\n" +
                    "        .dpad-btn {\n" +
                    "            width: 60px;\n" +
                    "            height: 60px;\n" +
                    "            border: none;\n" +
                    "            border-radius: 8px;\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            font-size: 24px;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.2s;\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            justify-content: center;\n" +
                    "            box-shadow: 0 2px 4px rgba(0,0,0,0.2);\n" +
                    "        }\n" +
                    "        .dpad-btn:hover:not(:disabled) {\n" +
                    "            background: #5568d3;\n" +
                    "            transform: scale(1.05);\n" +
                    "        }\n" +
                    "        .dpad-btn:active:not(:disabled) {\n" +
                    "            transform: scale(0.95);\n" +
                    "        }\n" +
                    "        .dpad-btn:disabled {\n" +
                    "            background: #ccc;\n" +
                    "            cursor: not-allowed;\n" +
                    "            opacity: 0.5;\n" +
                    "        }\n" +
                    "        .increment-selector {\n" +
                    "            margin-top: 20px;\n" +
                    "            padding: 15px;\n" +
                    "            background: white;\n" +
                    "            border-radius: 6px;\n" +
                    "        }\n" +
                    "        .increment-selector label {\n" +
                    "            display: block;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .increment-buttons {\n" +
                    "            display: flex;\n" +
                    "            gap: 8px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .increment-btn {\n" +
                    "            flex: 1;\n" +
                    "            min-width: 60px;\n" +
                    "            padding: 10px;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            background: white;\n" +
                    "            color: #333;\n" +
                    "            font-weight: 600;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.2s;\n" +
                    "        }\n" +
                    "        .increment-btn:hover {\n" +
                    "            border-color: #667eea;\n" +
                    "            background: #f0f0ff;\n" +
                    "        }\n" +
                    "        .increment-btn.active {\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        .invert-controls {\n" +
                    "            margin-top: 20px;\n" +
                    "            padding: 15px;\n" +
                    "            background: white;\n" +
                    "            border-radius: 6px;\n" +
                    "        }\n" +
                    "        .invert-controls label {\n" +
                    "            display: block;\n" +
                    "            margin-bottom: 10px;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #333;\n" +
                    "        }\n" +
                    "        .toggle-buttons {\n" +
                    "            display: flex;\n" +
                    "            gap: 10px;\n" +
                    "            flex-wrap: wrap;\n" +
                    "        }\n" +
                    "        .toggle-btn {\n" +
                    "            flex: 1;\n" +
                    "            min-width: 120px;\n" +
                    "            padding: 10px 15px;\n" +
                    "            border: 2px solid #ddd;\n" +
                    "            border-radius: 6px;\n" +
                    "            background: white;\n" +
                    "            color: #333;\n" +
                    "            font-weight: 600;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: all 0.2s;\n" +
                    "        }\n" +
                    "        .toggle-btn:hover {\n" +
                    "            border-color: #667eea;\n" +
                    "            background: #f0f0ff;\n" +
                    "        }\n" +
                    "        .toggle-btn.active {\n" +
                    "            background: #667eea;\n" +
                    "            color: white;\n" +
                    "            border-color: #667eea;\n" +
                    "        }\n" +
                    "        .fixture-selector {\n" +
                    "            margin-top: 20px;\n" +
                    "            padding: 15px;\n" +
                    "            background: white;\n" +
                    "            border-radius: 6px;\n" +
                    "        }\n" +
                    "        .radio-item {\n" +
                    "            display: flex;\n" +
                    "            align-items: center;\n" +
                    "            padding: 8px;\n" +
                    "            margin-bottom: 5px;\n" +
                    "            border-radius: 4px;\n" +
                    "            cursor: pointer;\n" +
                    "            transition: background-color 0.2s;\n" +
                    "        }\n" +
                    "        .radio-item:hover {\n" +
                    "            background-color: #f0f0ff;\n" +
                    "        }\n" +
                    "        .radio-item input[type=\"radio\"] {\n" +
                    "            margin-right: 10px;\n" +
                    "            cursor: pointer;\n" +
                    "        }\n" +
                    "        .radio-item label {\n" +
                    "            cursor: pointer;\n" +
                    "            flex: 1;\n" +
                    "            margin: 0;\n" +
                    "            font-weight: normal;\n" +
                    "        }\n" +
                    "        .radio-item.selected {\n" +
                    "            background-color: #e7f3ff;\n" +
                    "            border-left: 3px solid #667eea;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"container\">\n" +
                    "        <a href=\"/scenes-config.html\" class=\"back-btn\">← Volver a Escenas</a>\n" +
                    "        <h1>🎯 Editar Posiciones Robóticas</h1>\n" +
                    "        <p class=\"subtitle\">Visualización y edición de coordenadas de fixtures robóticos</p>\n" +
                    "        \n" +
                    "        <div id=\"message\" class=\"message\"></div>\n" +
                    "        \n" +
                    "        <div id=\"sceneInfo\" class=\"scene-info\">\n" +
                    "            <div class=\"loading\">Cargando información de la escena...</div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"editor-container\">\n" +
                    "            <div class=\"canvas-section\">\n" +
                    "                <h3 style=\"margin-bottom: 15px; color: #333;\">Editor Gráfico de Posiciones</h3>\n" +
                    "                <div class=\"canvas-wrapper\">\n" +
                    "                    <canvas id=\"positionCanvas\" width=\"800\" height=\"800\"></canvas>\n" +
                    "                </div>\n" +
                    "                <div class=\"canvas-controls\">\n" +
                    "                    <button class=\"control-btn secondary\" onclick=\"resetView()\">🔄 Resetear Vista</button>\n" +
                    "                    <button class=\"control-btn secondary\" onclick=\"centerAll()\">🎯 Centrar Todos</button>\n" +
                    "                    <button class=\"control-btn success\" onclick=\"savePositions()\">💾 Guardar Cambios</button>\n" +
                    "                </div>\n" +
                    "                <div class=\"legend\">\n" +
                    "                    <strong>Instrucciones:</strong> Haz clic y arrastra los puntos azules para mover las posiciones de los fixtures. " +
                    "Los valores se actualizan automáticamente en la tabla.\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"info-panel\">\n" +
                    "                <h3>Información del Fixture Seleccionado</h3>\n" +
                    "                <div id=\"selectedFixtureInfo\">\n" +
                    "                    <div class=\"info-item\">\n" +
                    "                        <p>Selecciona un fixture en el canvas para ver sus detalles</p>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"control-panel\">\n" +
                    "                <h3>Control de Posición</h3>\n" +
                    "                <div class=\"dpad-container\">\n" +
                    "                    <div class=\"dpad-row\">\n" +
                    "                        <div></div>\n" +
                    "                        <button class=\"dpad-btn\" id=\"btnUp\" onclick=\"moveFixture('up')\" disabled>↑</button>\n" +
                    "                        <div></div>\n" +
                    "                    </div>\n" +
                    "                    <div class=\"dpad-row\">\n" +
                    "                        <button class=\"dpad-btn\" id=\"btnLeft\" onclick=\"moveFixture('left')\" disabled>←</button>\n" +
                    "                        <div style=\"width: 60px; height: 60px;\"></div>\n" +
                    "                        <button class=\"dpad-btn\" id=\"btnRight\" onclick=\"moveFixture('right')\" disabled>→</button>\n" +
                    "                    </div>\n" +
                    "                    <div class=\"dpad-row\">\n" +
                    "                        <div></div>\n" +
                    "                        <button class=\"dpad-btn\" id=\"btnDown\" onclick=\"moveFixture('down')\" disabled>↓</button>\n" +
                    "                        <div></div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                <div class=\"invert-controls\">\n" +
                    "                    <label>Invertir Controles:</label>\n" +
                    "                    <div class=\"toggle-buttons\">\n" +
                    "                        <button class=\"toggle-btn\" id=\"toggleInvertPan\" onclick=\"toggleInvert('pan')\">🔄 Invertir Pan</button>\n" +
                    "                        <button class=\"toggle-btn\" id=\"toggleInvertTilt\" onclick=\"toggleInvert('tilt')\">🔄 Invertir Tilt</button>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                <div class=\"increment-selector\">\n" +
                    "                    <label>Incremento (coordenadas):</label>\n" +
                    "                    <div class=\"increment-buttons\">\n" +
                    "                        <button class=\"increment-btn active\" onclick=\"setIncrement(1)\" data-increment=\"1\">1</button>\n" +
                    "                        <button class=\"increment-btn\" onclick=\"setIncrement(10)\" data-increment=\"10\">10</button>\n" +
                    "                        <button class=\"increment-btn\" onclick=\"setIncrement(100)\" data-increment=\"100\">100</button>\n" +
                    "                        <button class=\"increment-btn\" onclick=\"setIncrement(500)\" data-increment=\"500\">500</button>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                <div class=\"fixture-selector\" style=\"margin-top: 20px; padding: 15px; background: white; border-radius: 6px;\">\n" +
                    "                    <label style=\"display: block; margin-bottom: 10px; font-weight: 600; color: #333;\">Seleccionar Fixture:</label>\n" +
                    "                    <div id=\"fixtureRadioList\" style=\"max-height: 200px; overflow-y: auto;\">\n" +
                    "                        <div class=\"loading\">Cargando fixtures...</div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "        \n" +
                    "        <div class=\"table-container\">\n" +
                    "            <table id=\"positionsTable\">\n" +
                    "                <thead>\n" +
                    "                    <tr>\n" +
                    "                        <th>Fixture (ID + Nombre)</th>\n" +
                    "                        <th>Pan</th>\n" +
                    "                        <th>Pan Fine</th>\n" +
                    "                        <th>Tilt</th>\n" +
                    "                        <th>Tilt Fine</th>\n" +
                    "                        <th>X (Pan×256+PanFine)</th>\n" +
                    "                        <th>Y (Tilt×256+TiltFine)</th>\n" +
                    "                    </tr>\n" +
                    "                </thead>\n" +
                    "                <tbody id=\"tableBody\">\n" +
                    "                    <tr>\n" +
                    "                        <td colspan=\"7\" class=\"loading\">Cargando posiciones...</td>\n" +
                    "                    </tr>\n" +
                    "                </tbody>\n" +
                    "            </table>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        const urlParams = new URLSearchParams(window.location.search);\n" +
                    "        const sceneId = urlParams.get('id');\n" +
                    "        let sceneData = null;\n" +
                    "        let fixtures = [];\n" +
                    "        let selectedFixture = null;\n" +
                    "        let isDragging = false;\n" +
                    "        let dragOffset = { x: 0, y: 0 };\n" +
                    "        \n" +
                    "        const canvas = document.getElementById('positionCanvas');\n" +
                    "        const ctx = canvas.getContext('2d');\n" +
                    "        let currentIncrement = 1; // Incremento en coordenadas DMX (0-65535)\n" +
                    "        let invertPan = false; // Estado de inversión de Pan\n" +
                    "        let invertTilt = false; // Estado de inversión de Tilt\n" +
                    "        \n" +
                    "        // Ajustar tamaño del canvas\n" +
                    "        function resizeCanvas() {\n" +
                    "            const wrapper = canvas.parentElement;\n" +
                    "            const maxSize = Math.min(800, wrapper.clientWidth - 40);\n" +
                    "            canvas.width = maxSize;\n" +
                    "            canvas.height = maxSize;\n" +
                    "            drawCanvas();\n" +
                    "        }\n" +
                    "        \n" +
                    "        function setIncrement(value) {\n" +
                    "            currentIncrement = value;\n" +
                    "            // Actualizar botones activos\n" +
                    "            document.querySelectorAll('.increment-btn').forEach(btn => {\n" +
                    "                if (parseInt(btn.dataset.increment) === value) {\n" +
                    "                    btn.classList.add('active');\n" +
                    "                } else {\n" +
                    "                    btn.classList.remove('active');\n" +
                    "                }\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        function toggleInvert(type) {\n" +
                    "            if (type === 'pan') {\n" +
                    "                invertPan = !invertPan;\n" +
                    "                const btn = document.getElementById('toggleInvertPan');\n" +
                    "                if (invertPan) {\n" +
                    "                    btn.classList.add('active');\n" +
                    "                } else {\n" +
                    "                    btn.classList.remove('active');\n" +
                    "                }\n" +
                    "            } else if (type === 'tilt') {\n" +
                    "                invertTilt = !invertTilt;\n" +
                    "                const btn = document.getElementById('toggleInvertTilt');\n" +
                    "                if (invertTilt) {\n" +
                    "                    btn.classList.add('active');\n" +
                    "                } else {\n" +
                    "                    btn.classList.remove('active');\n" +
                    "                }\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        let sendToArtNetTimeout = null;\n" +
                    "        \n" +
                    "        async function sendToArtNetRealTime(fixture) {\n" +
                    "            if (!fixture || !sceneId) return;\n" +
                    "            \n" +
                    "            // Debounce: cancelar envío anterior si existe\n" +
                    "            if (sendToArtNetTimeout) {\n" +
                    "                clearTimeout(sendToArtNetTimeout);\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Enviar después de un pequeño delay para evitar saturar la red\n" +
                    "            sendToArtNetTimeout = setTimeout(async () => {\n" +
                    "                try {\n" +
                    "                    const response = await fetch(`/api/scenes/${sceneId}/send-point`, {\n" +
                    "                        method: 'PUT',\n" +
                    "                        headers: {\n" +
                    "                            'Content-Type': 'application/json'\n" +
                    "                        },\n" +
                    "                        body: JSON.stringify({\n" +
                    "                            fixtureId: fixture.fixtureId,\n" +
                    "                            pan: fixture.pan,\n" +
                    "                            panFine: fixture.panFine,\n" +
                    "                            tilt: fixture.tilt,\n" +
                    "                            tiltFine: fixture.tiltFine\n" +
                    "                        })\n" +
                    "                    });\n" +
                    "                    \n" +
                    "                    if (!response.ok) {\n" +
                    "                        console.warn('Error enviando a ArtNet:', await response.text());\n" +
                    "                    }\n" +
                    "                } catch (error) {\n" +
                    "                    console.error('Error enviando a ArtNet:', error);\n" +
                    "                }\n" +
                    "            }, 50); // 50ms de debounce\n" +
                    "        }\n" +
                    "        \n" +
                    "        function moveFixture(direction) {\n" +
                    "            if (!selectedFixture) {\n" +
                    "                alert('Por favor selecciona un fixture primero');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            if (selectedFixture.x === null || selectedFixture.x === undefined || \n" +
                    "                selectedFixture.y === null || selectedFixture.y === undefined) {\n" +
                    "                alert('El fixture seleccionado no tiene coordenadas válidas');\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Obtener valores DMX actuales\n" +
                    "            let currentDmxX = selectedFixture.x;\n" +
                    "            let currentDmxY = selectedFixture.y;\n" +
                    "            \n" +
                    "            // Calcular nueva posición según dirección (incremento en coordenadas DMX)\n" +
                    "            // Aplicar inversión si está activa\n" +
                    "            let actualDirection = direction;\n" +
                    "            if (direction === 'left' || direction === 'right') {\n" +
                    "                if (invertPan) {\n" +
                    "                    actualDirection = direction === 'left' ? 'right' : 'left';\n" +
                    "                }\n" +
                    "            } else if (direction === 'up' || direction === 'down') {\n" +
                    "                if (invertTilt) {\n" +
                    "                    actualDirection = direction === 'up' ? 'down' : 'up';\n" +
                    "                }\n" +
                    "            }\n" +
                    "            \n" +
                    "            let newDmxX = currentDmxX;\n" +
                    "            let newDmxY = currentDmxY;\n" +
                    "            \n" +
                    "            switch(actualDirection) {\n" +
                    "                case 'up':\n" +
                    "                    newDmxY = Math.max(0, currentDmxY - currentIncrement);\n" +
                    "                    break;\n" +
                    "                case 'down':\n" +
                    "                    newDmxY = Math.min(MAX_DMX, currentDmxY + currentIncrement);\n" +
                    "                    break;\n" +
                    "                case 'left':\n" +
                    "                    newDmxX = Math.max(0, currentDmxX - currentIncrement);\n" +
                    "                    break;\n" +
                    "                case 'right':\n" +
                    "                    newDmxX = Math.min(MAX_DMX, currentDmxX + currentIncrement);\n" +
                    "                    break;\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Usar los valores DMX directamente\n" +
                    "            const dmxX = newDmxX;\n" +
                    "            const dmxY = newDmxY;\n" +
                    "            \n" +
                    "            // Actualizar valores del fixture\n" +
                    "            const panData = dmxToPanTilt(dmxX);\n" +
                    "            const tiltData = dmxToPanTilt(dmxY);\n" +
                    "            \n" +
                    "            selectedFixture.pan = panData.coarse;\n" +
                    "            selectedFixture.panFine = panData.fine;\n" +
                    "            selectedFixture.tilt = tiltData.coarse;\n" +
                    "            selectedFixture.tiltFine = tiltData.fine;\n" +
                    "            selectedFixture.x = dmxX;\n" +
                    "            selectedFixture.y = dmxY;\n" +
                    "            \n" +
                    "            drawCanvas();\n" +
                    "            renderTable(fixtures);\n" +
                    "            updateFixtureInfo(selectedFixture);\n" +
                    "            \n" +
                    "            // Enviar a ArtNet en tiempo real\n" +
                    "            sendToArtNetRealTime(selectedFixture);\n" +
                    "        }\n" +
                    "        \n" +
                    "        function updateDpadButtons() {\n" +
                    "            const hasSelection = selectedFixture !== null;\n" +
                    "            document.getElementById('btnUp').disabled = !hasSelection;\n" +
                    "            document.getElementById('btnDown').disabled = !hasSelection;\n" +
                    "            document.getElementById('btnLeft').disabled = !hasSelection;\n" +
                    "            document.getElementById('btnRight').disabled = !hasSelection;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function selectFixtureFromRadio(fixtureId) {\n" +
                    "            const fixture = fixtures.find(f => f.fixtureId === fixtureId);\n" +
                    "            if (fixture) {\n" +
                    "                selectedFixture = fixture;\n" +
                    "                updateFixtureInfo(fixture);\n" +
                    "                updateDpadButtons();\n" +
                    "                updateRadioSelection();\n" +
                    "                drawCanvas();\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function updateRadioSelection() {\n" +
                    "            document.querySelectorAll('.radio-item').forEach(item => {\n" +
                    "                const fixtureId = parseInt(item.dataset.fixtureId);\n" +
                    "                if (selectedFixture && selectedFixture.fixtureId === fixtureId) {\n" +
                    "                    item.classList.add('selected');\n" +
                    "                    item.querySelector('input[type=\"radio\"]').checked = true;\n" +
                    "                } else {\n" +
                    "                    item.classList.remove('selected');\n" +
                    "                    item.querySelector('input[type=\"radio\"]').checked = false;\n" +
                    "                }\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderFixtureRadioList() {\n" +
                    "            const container = document.getElementById('fixtureRadioList');\n" +
                    "            if (!fixtures || fixtures.length === 0) {\n" +
                    "                container.innerHTML = '<div class=\"empty\">No hay fixtures disponibles</div>';\n" +
                    "                return;\n" +
                    "            }\n" +
                    "            \n" +
                    "            container.innerHTML = fixtures.map(fixture => {\n" +
                    "                const fixtureName = fixture.fixtureName || `Fixture ${fixture.fixtureId}`;\n" +
                    "                return `\n" +
                    "                    <div class=\"radio-item\" data-fixture-id=\"${fixture.fixtureId}\" onclick=\"selectFixtureFromRadio(${fixture.fixtureId})\">\n" +
                    "                        <input type=\"radio\" name=\"fixtureSelect\" id=\"fixture_${fixture.fixtureId}\" value=\"${fixture.fixtureId}\" onchange=\"selectFixtureFromRadio(${fixture.fixtureId})\">\n" +
                    "                        <label for=\"fixture_${fixture.fixtureId}\">\n" +
                    "                            <strong>ID ${fixture.fixtureId}:</strong> ${fixtureName}\n" +
                    "                        </label>\n" +
                    "                    </div>\n" +
                    "                `;\n" +
                    "            }).join('');\n" +
                    "            \n" +
                    "            updateRadioSelection();\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Rango de valores DMX: 0-65535 (pan*256 + panFine)\n" +
                    "        const MAX_DMX = 65535;\n" +
                    "        \n" +
                    "        // Convertir coordenada DMX a posición en canvas\n" +
                    "        function dmxToCanvas(dmxValue) {\n" +
                    "            return (dmxValue / MAX_DMX) * canvas.width;\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Convertir posición en canvas a coordenada DMX\n" +
                    "        function canvasToDmx(canvasPos) {\n" +
                    "            return Math.round((canvasPos / canvas.width) * MAX_DMX);\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Convertir DMX a pan/panFine o tilt/tiltFine\n" +
                    "        function dmxToPanTilt(dmxValue) {\n" +
                    "            const coarse = Math.floor(dmxValue / 256);\n" +
                    "            const fine = dmxValue % 256;\n" +
                    "            return { coarse: coarse, fine: fine };\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Convertir pan/panFine o tilt/tiltFine a DMX\n" +
                    "        function panTiltToDmx(coarse, fine) {\n" +
                    "            return coarse * 256 + fine;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function drawCanvas() {\n" +
                    "            // Limpiar canvas\n" +
                    "            ctx.clearRect(0, 0, canvas.width, canvas.height);\n" +
                    "            \n" +
                    "            // Dibujar cuadrícula\n" +
                    "            ctx.strokeStyle = '#e0e0e0';\n" +
                    "            ctx.lineWidth = 1;\n" +
                    "            const gridSize = canvas.width / 10;\n" +
                    "            for (let i = 0; i <= 10; i++) {\n" +
                    "                const pos = i * gridSize;\n" +
                    "                // Líneas verticales\n" +
                    "                ctx.beginPath();\n" +
                    "                ctx.moveTo(pos, 0);\n" +
                    "                ctx.lineTo(pos, canvas.height);\n" +
                    "                ctx.stroke();\n" +
                    "                // Líneas horizontales\n" +
                    "                ctx.beginPath();\n" +
                    "                ctx.moveTo(0, pos);\n" +
                    "                ctx.lineTo(canvas.width, pos);\n" +
                    "                ctx.stroke();\n" +
                    "            }\n" +
                    "            \n" +
                    "            // Dibujar ejes centrales\n" +
                    "            ctx.strokeStyle = '#999';\n" +
                    "            ctx.lineWidth = 2;\n" +
                    "            const centerX = canvas.width / 2;\n" +
                    "            const centerY = canvas.height / 2;\n" +
                    "            ctx.beginPath();\n" +
                    "            ctx.moveTo(centerX, 0);\n" +
                    "            ctx.lineTo(centerX, canvas.height);\n" +
                    "            ctx.stroke();\n" +
                    "            ctx.beginPath();\n" +
                    "            ctx.moveTo(0, centerY);\n" +
                    "            ctx.lineTo(canvas.width, centerY);\n" +
                    "            ctx.stroke();\n" +
                    "            \n" +
                    "            // Dibujar fixtures\n" +
                    "            fixtures.forEach(fixture => {\n" +
                    "                if (fixture.x === null || fixture.x === undefined || \n" +
                    "                    fixture.y === null || fixture.y === undefined) return;\n" +
                    "                \n" +
                    "                const x = dmxToCanvas(fixture.x);\n" +
                    "                const y = dmxToCanvas(fixture.y);\n" +
                    "                \n" +
                    "                const isSelected = selectedFixture && selectedFixture.fixtureId === fixture.fixtureId;\n" +
                    "                \n" +
                    "                // Círculo del fixture\n" +
                    "                ctx.fillStyle = isSelected ? '#ff6b6b' : '#667eea';\n" +
                    "                ctx.beginPath();\n" +
                    "                ctx.arc(x, y, isSelected ? 12 : 8, 0, Math.PI * 2);\n" +
                    "                ctx.fill();\n" +
                    "                ctx.strokeStyle = 'white';\n" +
                    "                ctx.lineWidth = 2;\n" +
                    "                ctx.stroke();\n" +
                    "                \n" +
                    "                // Etiqueta con ID del fixture\n" +
                    "                ctx.fillStyle = 'white';\n" +
                    "                ctx.font = 'bold 12px Arial';\n" +
                    "                ctx.textAlign = 'center';\n" +
                    "                ctx.textBaseline = 'middle';\n" +
                    "                ctx.fillText(fixture.fixtureId.toString(), x, y);\n" +
                    "            });\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Obtener fixture en una posición del canvas\n" +
                    "        function getFixtureAt(x, y) {\n" +
                    "            const radius = 15;\n" +
                    "            for (let fixture of fixtures) {\n" +
                    "                if (fixture.x === null || fixture.x === undefined || \n" +
                    "                    fixture.y === null || fixture.y === undefined) continue;\n" +
                    "                \n" +
                    "                const fx = dmxToCanvas(fixture.x);\n" +
                    "                const fy = dmxToCanvas(fixture.y);\n" +
                    "                \n" +
                    "                const dx = x - fx;\n" +
                    "                const dy = y - fy;\n" +
                    "                const distance = Math.sqrt(dx * dx + dy * dy);\n" +
                    "                \n" +
                    "                if (distance <= radius) {\n" +
                    "                    return fixture;\n" +
                    "                }\n" +
                    "            }\n" +
                    "            return null;\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Eventos del canvas\n" +
                    "        canvas.addEventListener('mousedown', (e) => {\n" +
                    "            const rect = canvas.getBoundingClientRect();\n" +
                    "            const x = e.clientX - rect.left;\n" +
                    "            const y = e.clientY - rect.top;\n" +
                    "            \n" +
                    "            const fixture = getFixtureAt(x, y);\n" +
                    "            if (fixture) {\n" +
                    "                selectedFixture = fixture;\n" +
                    "                isDragging = true;\n" +
                    "                dragOffset.x = x - dmxToCanvas(fixture.x);\n" +
                    "                dragOffset.y = y - dmxToCanvas(fixture.y);\n" +
                    "                canvas.style.cursor = 'grabbing';\n" +
                    "                updateFixtureInfo(fixture);\n" +
                    "                updateDpadButtons();\n" +
                    "                updateRadioSelection();\n" +
                    "                drawCanvas();\n" +
                    "            }\n" +
                    "        });\n" +
                    "        \n" +
                    "        canvas.addEventListener('mousemove', (e) => {\n" +
                    "            const rect = canvas.getBoundingClientRect();\n" +
                    "            const x = e.clientX - rect.left;\n" +
                    "            const y = e.clientY - rect.top;\n" +
                    "            \n" +
                    "            if (isDragging && selectedFixture) {\n" +
                    "                // Calcular nueva posición\n" +
                    "                const newX = Math.max(0, Math.min(canvas.width, x - dragOffset.x));\n" +
                    "                const newY = Math.max(0, Math.min(canvas.height, y - dragOffset.y));\n" +
                    "                \n" +
                    "                // Convertir a valores DMX\n" +
                    "                const dmxX = canvasToDmx(newX);\n" +
                    "                const dmxY = canvasToDmx(newY);\n" +
                    "                \n" +
                    "                // Actualizar valores del fixture\n" +
                    "                const panData = dmxToPanTilt(dmxX);\n" +
                    "                const tiltData = dmxToPanTilt(dmxY);\n" +
                    "                \n" +
                    "                selectedFixture.pan = panData.coarse;\n" +
                    "                selectedFixture.panFine = panData.fine;\n" +
                    "                selectedFixture.tilt = tiltData.coarse;\n" +
                    "                selectedFixture.tiltFine = tiltData.fine;\n" +
                    "                selectedFixture.x = dmxX;\n" +
                    "                selectedFixture.y = dmxY;\n" +
                    "                \n" +
                    "                drawCanvas();\n" +
                    "                renderTable(fixtures);\n" +
                    "                updateFixtureInfo(selectedFixture);\n" +
                    "                \n" +
                    "                // Enviar a ArtNet en tiempo real mientras se arrastra\n" +
                    "                sendToArtNetRealTime(selectedFixture);\n" +
                    "            } else {\n" +
                    "                const fixture = getFixtureAt(x, y);\n" +
                    "                canvas.style.cursor = fixture ? 'grab' : 'crosshair';\n" +
                    "            }\n" +
                    "        });\n" +
                    "        \n" +
                    "        canvas.addEventListener('mouseup', () => {\n" +
                    "            isDragging = false;\n" +
                    "            canvas.style.cursor = 'crosshair';\n" +
                    "        });\n" +
                    "        \n" +
                    "        canvas.addEventListener('mouseleave', () => {\n" +
                    "            isDragging = false;\n" +
                    "            canvas.style.cursor = 'crosshair';\n" +
                    "        });\n" +
                    "        \n" +
                    "        function updateFixtureInfo(fixture) {\n" +
                    "            if (!fixture) return;\n" +
                    "            \n" +
                    "            const pan = fixture.pan !== null && fixture.pan !== undefined ? fixture.pan : '-';\n" +
                    "            const panFine = fixture.panFine !== null && fixture.panFine !== undefined ? fixture.panFine : '-';\n" +
                    "            const tilt = fixture.tilt !== null && fixture.tilt !== undefined ? fixture.tilt : '-';\n" +
                    "            const tiltFine = fixture.tiltFine !== null && fixture.tiltFine !== undefined ? fixture.tiltFine : '-';\n" +
                    "            const x = fixture.x !== null && fixture.x !== undefined ? fixture.x : '-';\n" +
                    "            const y = fixture.y !== null && fixture.y !== undefined ? fixture.y : '-';\n" +
                    "            \n" +
                    "            document.getElementById('selectedFixtureInfo').innerHTML = `\n" +
                    "                <div class=\"info-item\">\n" +
                    "                    <strong>Fixture:</strong> ${fixture.fixtureId}${fixture.fixtureName ? ' - ' + fixture.fixtureName : ''}\n" +
                    "                </div>\n" +
                    "                <div class=\"info-item\">\n" +
                    "                    <strong>Pan:</strong> ${pan} | <strong>Pan Fine:</strong> ${panFine}\n" +
                    "                </div>\n" +
                    "                <div class=\"info-item\">\n" +
                    "                    <strong>Tilt:</strong> ${tilt} | <strong>Tilt Fine:</strong> ${tiltFine}\n" +
                    "                </div>\n" +
                    "                <div class=\"info-item\">\n" +
                    "                    <strong>X:</strong> ${x} | <strong>Y:</strong> ${y}\n" +
                    "                </div>\n" +
                    "            `;\n" +
                    "        }\n" +
                    "        \n" +
                    "        function resetView() {\n" +
                    "            selectedFixture = null;\n" +
                    "            updateDpadButtons();\n" +
                    "            updateRadioSelection();\n" +
                    "            drawCanvas();\n" +
                    "            document.getElementById('selectedFixtureInfo').innerHTML = \n" +
                    "                '<div class=\"info-item\"><p>Selecciona un fixture en el canvas para ver sus detalles</p></div>';\n" +
                    "        }\n" +
                    "        \n" +
                    "        function centerAll() {\n" +
                    "            const centerDmx = Math.floor(MAX_DMX / 2);\n" +
                    "            fixtures.forEach(fixture => {\n" +
                    "                const panData = dmxToPanTilt(centerDmx);\n" +
                    "                const tiltData = dmxToPanTilt(centerDmx);\n" +
                    "                fixture.pan = panData.coarse;\n" +
                    "                fixture.panFine = panData.fine;\n" +
                    "                fixture.tilt = tiltData.coarse;\n" +
                    "                fixture.tiltFine = tiltData.fine;\n" +
                    "                fixture.x = centerDmx;\n" +
                    "                fixture.y = centerDmx;\n" +
                    "            });\n" +
                    "            drawCanvas();\n" +
                    "            renderTable(fixtures);\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function savePositions() {\n" +
                    "            try {\n" +
                    "                const response = await fetch(`/api/scenes/${sceneId}/update-positions`, {\n" +
                    "                    method: 'PUT',\n" +
                    "                    headers: {\n" +
                    "                        'Content-Type': 'application/json'\n" +
                    "                    },\n" +
                    "                    body: JSON.stringify({ fixtures: fixtures })\n" +
                    "                });\n" +
                    "                \n" +
                    "                const result = await response.json();\n" +
                    "                \n" +
                    "                if (response.ok) {\n" +
                    "                    alert('Posiciones guardadas correctamente');\n" +
                    "                } else {\n" +
                    "                    alert('Error: ' + (result.error || 'Error desconocido'));\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error guardando posiciones:', error);\n" +
                    "                alert('Error al guardar las posiciones');\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        if (!sceneId) {\n" +
                    "            document.getElementById('tableBody').innerHTML = \n" +
                    "                '<tr><td colspan=\"7\" class=\"empty\">ID de escena no proporcionado</td></tr>';\n" +
                    "        } else {\n" +
                    "            loadSceneData();\n" +
                    "        }\n" +
                    "        \n" +
                    "        async function loadSceneData() {\n" +
                    "            try {\n" +
                    "                document.getElementById('tableBody').innerHTML = '<tr><td colspan=\"7\" class=\"loading\">Cargando posiciones...</td></tr>';\n" +
                    "                \n" +
                    "                const response = await fetch(`/api/scenes/${sceneId}`);\n" +
                    "                sceneData = await response.json();\n" +
                    "                \n" +
                    "                // Mostrar información de la escena\n" +
                    "                document.getElementById('sceneInfo').innerHTML = \n" +
                    "                    `<strong>Escena ID:</strong> ${sceneData.id} | ` +\n" +
                    "                    `<strong>Nombre:</strong> ${sceneData.name || '-'} | ` +\n" +
                    "                    `<strong>Path:</strong> ${sceneData.path || '-'}`;\n" +
                    "                \n" +
                    "                // Cargar fixtures\n" +
                    "                if (sceneData.fixtures && sceneData.fixtures.length > 0) {\n" +
                    "                    fixtures = sceneData.fixtures;\n" +
                    "                    renderTable(fixtures);\n" +
                    "                    renderFixtureRadioList();\n" +
                    "                    resizeCanvas();\n" +
                    "                    updateDpadButtons();\n" +
                    "                    drawCanvas();\n" +
                    "                } else {\n" +
                    "                    document.getElementById('tableBody').innerHTML = \n" +
                    "                        '<tr><td colspan=\"7\" class=\"empty\">No se encontraron fixtures con posiciones</td></tr>';\n" +
                    "                }\n" +
                    "            } catch (error) {\n" +
                    "                console.error('Error cargando datos de escena:', error);\n" +
                    "                document.getElementById('tableBody').innerHTML = \n" +
                    "                    '<tr><td colspan=\"7\" class=\"empty\">Error al cargar los datos de la escena</td></tr>';\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function renderTable(fixtures) {\n" +
                    "            const tbody = document.getElementById('tableBody');\n" +
                    "            \n" +
                    "            tbody.innerHTML = fixtures.map(fixture => {\n" +
                    "                const pan = fixture.pan !== null && fixture.pan !== undefined ? fixture.pan : '-';\n" +
                    "                const panFine = fixture.panFine !== null && fixture.panFine !== undefined ? fixture.panFine : '-';\n" +
                    "                const tilt = fixture.tilt !== null && fixture.tilt !== undefined ? fixture.tilt : '-';\n" +
                    "                const tiltFine = fixture.tiltFine !== null && fixture.tiltFine !== undefined ? fixture.tiltFine : '-';\n" +
                    "                const x = fixture.x !== null && fixture.x !== undefined ? fixture.x : '-';\n" +
                    "                const y = fixture.y !== null && fixture.y !== undefined ? fixture.y : '-';\n" +
                    "                \n" +
                    "                const fixtureDisplay = fixture.fixtureName ? \n" +
                    "                    `<span class=\"id-badge\">${fixture.fixtureId}</span> <strong>${fixture.fixtureName}</strong>` : \n" +
                    "                    `<span class=\"id-badge\">${fixture.fixtureId}</span>`;\n" +
                    "                \n" +
                    "                return `\n" +
                    "                    <tr>\n" +
                    "                        <td>${fixtureDisplay}</td>\n" +
                    "                        <td><span class=\"channel-value\">${pan}</span></td>\n" +
                    "                        <td><span class=\"channel-value\">${panFine}</span></td>\n" +
                    "                        <td><span class=\"channel-value\">${tilt}</span></td>\n" +
                    "                        <td><span class=\"channel-value\">${tiltFine}</span></td>\n" +
                    "                        <td><span class=\"coord-value\">${x}</span></td>\n" +
                    "                        <td><span class=\"coord-value\">${y}</span></td>\n" +
                    "                    </tr>\n" +
                    "                `;\n" +
                    "            }).join('');\n" +
                    "        }\n" +
                    "        \n" +
                    "        // Ajustar canvas al redimensionar ventana\n" +
                    "        window.addEventListener('resize', resizeCanvas);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }
}

