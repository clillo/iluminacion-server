package cl.clillo.lighting.web;

import cl.clillo.lighting.config.FixtureConfig;
import cl.clillo.lighting.config.FixturesConfig;
import cl.clillo.lighting.config.FixturesConfigService;
import cl.clillo.lighting.config.DmxMapService;
import cl.clillo.lighting.config.NetworkConfigService;
import cl.clillo.lighting.config.ExternalConfigService;
import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.external.dmx.Dmx;
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
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
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

    private static final String WEB_RESOURCE_PATH = "/web";
    private static final String WEB_DIR_PROPERTY = "web.dir";

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
        context.setBaseResource(getFrontendResource());
        context.setWelcomeFiles(new String[]{"index.html"});
        context.getMimeTypes().addMimeMapping("html", "text/html;charset=utf-8");
        context.getMimeTypes().addMimeMapping("css", "text/css;charset=utf-8");
        context.getMimeTypes().addMimeMapping("js", "text/javascript;charset=utf-8");
        ServletHolder frontend = new ServletHolder("frontend", DefaultServlet.class);
        frontend.setInitParameter("dirAllowed", "false");
        context.addServlet(frontend, "/");

        server.start();
        log.info("Web server started on http://localhost:{}", port);
        log.info("Access the BeeEye control panel at http://localhost:{}/", port);
    }

    /**
     * Sirve el frontend desde el classpath (/web), o desde la carpeta indicada en -Dweb.dir
     * para editar HTML, CSS y JS sin reiniciar.
     */
    private Resource getFrontendResource() throws IOException {
        String webDir = System.getProperty(WEB_DIR_PROPERTY);
        if (webDir != null) {
            log.info("Serving web frontend from {}", webDir);
            return Resource.newResource(Path.of(webDir));
        }
        return Resource.newClassPathResource(WEB_RESOURCE_PATH);
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
                "name", "Moving Head Hibrid",
                "displayName", "Moving Head Hibrid"
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

        private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            
            PrintWriter out = resp.getWriter();
            objectMapper.writeValue(out, data);
            out.flush();
        }
    }
}

