package cl.clillo.lighting.web;

import cl.clillo.lighting.config.FixtureConfig;
import cl.clillo.lighting.config.FixturesConfig;
import cl.clillo.lighting.config.FixturesConfigService;
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
                
                sendJsonResponse(resp, Map.of("status", "ok", "maxUniverses", maxUniverses));
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
                
                // Recargar configuración para aplicar cambios
                configService.loadConfig();
                
                sendJsonResponse(resp, Map.of("status", "ok", "fixture", fixtureConfig));
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
                    "                    alert('Máximo de universos actualizado. Reinicie la aplicación para aplicar los cambios.');\n" +
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
                    "                    alert('Fixture actualizado correctamente. Reinicie la aplicación para aplicar los cambios.');\n" +
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
                    "            // Agrupar por path\n" +
                    "            const grouped = {};\n" +
                    "            shows.forEach(show => {\n" +
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
    }
}

