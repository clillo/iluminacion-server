package cl.clillo.lighting.web;

import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.model.QLCFunction;
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
                // GET /api/shows - Listar todos los shows BeeEye
                handleGetShows(req, resp);
            } else if (path.startsWith("/shows/")) {
                // GET /api/shows/{id} - Estado de un show específico
                handleGetShow(req, resp, path);
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
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendJsonResponse(resp, Map.of("error", "Not found"));
            }
        }

        private void handleGetShows(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            ShowCollection collection = ShowCollection.getInstance();
            List<Map<String, Object>> shows = new ArrayList<>();
            
            for (Show show : collection.getShowList()) {
                QLCFunction function = show.getFunction();
                if (function == null) continue;
                
                String path = function.getPath();
                String name = function.getName();
                
                // Filtrar solo BeeEye
                if ((path != null && path.contains("Bee Eye")) ||
                    (name != null && name.contains("BeeEye"))) {
                    
                    Map<String, Object> showData = new HashMap<>();
                    showData.put("id", show.getId());
                    showData.put("name", show.getName());
                    showData.put("path", path);
                    showData.put("type", function.getType());
                    showData.put("executing", show.isExecuting());
                    shows.add(showData);
                }
            }
            
            // Agrupar por path
            Map<String, List<Map<String, Object>>> grouped = shows.stream()
                .collect(Collectors.groupingBy(s -> (String) s.get("path")));
            
            sendJsonResponse(resp, Map.of("shows", shows, "grouped", grouped));
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
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"header\">\n" +
                    "        <h1>BeeEye Control</h1>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <button class=\"blackout-btn\" id=\"blackoutBtn\">BLACKOUT</button>\n" +
                    "    \n" +
                    "    <div id=\"content\">\n" +
                    "        <div class=\"loading\">Cargando efectos...</div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <script>\n" +
                    "        const API_BASE = '/api';\n" +
                    "        let shows = [];\n" +
                    "        let activeShowByPath = {};\n" +
                    "        \n" +
                    "        async function loadShows() {\n" +
                    "            try {\n" +
                    "                const response = await fetch(API_BASE + '/shows');\n" +
                    "                const data = await response.json();\n" +
                    "                shows = data.shows || [];\n" +
                    "                renderShows();\n" +
                    "            } catch (error) {\n" +
                    "                document.getElementById('content').innerHTML = \n" +
                    "                    '<div class=\"error\">Error al cargar efectos: ' + error.message + '</div>';\n" +
                    "            }\n" +
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
                    "        // Cargar shows al iniciar\n" +
                    "        loadShows();\n" +
                    "        \n" +
                    "        // Actualizar estado cada 2 segundos\n" +
                    "        setInterval(loadShows, 2000);\n" +
                    "    </script>\n" +
                    "</body>\n" +
                    "</html>";
        }
    }
}

