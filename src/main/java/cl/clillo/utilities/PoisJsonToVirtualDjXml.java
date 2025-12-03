package cl.clillo.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class PoisJsonToVirtualDjXml {

    private static final String OUTPUT_FILE = "vdj_pois_snippets.xml";

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Uso: java PoisJsonToVirtualDjXml <carpeta_raiz_pois_json>");
            System.out.println("Ej:  java PoisJsonToVirtualDjXml /Users/carlos/Music/VirtualDJ");
            return;
        }

        Path root = Paths.get(args[0]).toAbsolutePath().normalize();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            System.err.println("La ruta " + root + " no existe o no es una carpeta.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
            System.out.println("Buscando archivos *.pois.json en: " + root);

            Files.walk(root)
                    .filter(p -> p.toString().endsWith(".pois.json"))
                    .forEach(jsonPath -> {
                        try {
                            System.out.println("Procesando: " + jsonPath);
                            JsonNode rootNode = mapper.readTree(jsonPath.toFile());

                            String filePath = getText(rootNode, "file");
                            double duration = getDouble(rootNode, "duration", 0.0);

                            // Generamos el bloque <Song>...</Song> para este tema
                            String songXml = buildSongXml(filePath, duration, rootNode);

                            writer.write(songXml);
                            writer.newLine();
                            writer.newLine();

                        } catch (Exception e) {
                            System.err.println("Error procesando " + jsonPath + ": " + e.getMessage());
                        }
                    });

            System.out.println("Listo. Bloques XML generados en: " + OUTPUT_FILE);
        }
    }

    private static String buildSongXml(String filePath, double duration, JsonNode rootNode) {
        StringBuilder sb = new StringBuilder();

        // NOTA: FileSize lo ponemos en 0 por ahora, si quieres puedes calcularlo desde filePath.
        String escapedPath = xmlEscape(filePath);

        sb.append("<Song FilePath=\"").append(escapedPath).append("\" FileSize=\"0\">").append("\n");

        // Infos con duración en segundos
        sb.append("  <Infos SongLength=\"").append(String.format(java.util.Locale.US, "%.3f", duration)).append("\" />\n");

        // POIs
        JsonNode poisNode = rootNode.path("pois");
        if (poisNode.isArray()) {
            int poiIndex = 0;
            for (Iterator<JsonNode> it = poisNode.elements(); it.hasNext(); ) {
                JsonNode poi = it.next();
                double time = getDouble(poi, "time", 0.0);
                String type = getText(poi, "type");
                String label = getText(poi, "label");

                // Nombre del POI en VirtualDJ
                String poiName = (label != null && !label.isEmpty())
                        ? type + "_" + label
                        : type;

                // POIs como Type="action" con Action="nop" (para que luego puedas editarlo)
                // Num="-1" para que no aparezcan como hotcues numerados.
                sb.append("  <Poi")
                        .append(" Name=\"").append(xmlEscape(poiName)).append("\"")
                        .append(" Pos=\"").append(String.format(java.util.Locale.US, "%.3f", time)).append("\"")
                        .append(" Num=\"-1\"")
                        .append(" Type=\"action\"")
                        .append(" Action=\"nop\"")
                        .append(" />\n");

                poiIndex++;
            }
        }

        sb.append("</Song>");
        return sb.toString();
    }

    private static String getText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return null;
        return n.asText();
    }

    private static double getDouble(JsonNode node, String field, double defaultValue) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull() || !n.isNumber()) return defaultValue;
        return n.asDouble();
    }

    private static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
