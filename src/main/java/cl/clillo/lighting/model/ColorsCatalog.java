package cl.clillo.lighting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catálogo global de colores cargado desde resources/qlc/effects/colors.json.
 * Permite obtener valores RGBW por nombre (ej: "green.full", "red.trail1", "off").
 */
@Slf4j
public class ColorsCatalog {

	private static final Map<String, ColorEntry> NAME_TO_COLOR = new ConcurrentHashMap<>();
	private static volatile boolean loaded = false;

	public static void loadFromClasspath() {
		if (loaded) return;
		synchronized (ColorsCatalog.class) {
			if (loaded) return;
			try (InputStream in = ColorsCatalog.class.getClassLoader()
					.getResourceAsStream("qlc/effects/colors.json")) {
				if (in == null) {
					log.warn("colors.json not found on classpath at qlc/effects/colors.json");
					loaded = true;
					return;
				}
				final ObjectMapper mapper = new ObjectMapper();
				final ColorsFile colors = mapper.readValue(in, ColorsFile.class);
				if (colors.colors != null) {
					NAME_TO_COLOR.clear();
					NAME_TO_COLOR.putAll(colors.colors);
					log.info("Loaded {} colors from colors.json", NAME_TO_COLOR.size());
				} else {
					log.warn("colors.json loaded but empty 'colors' map");
				}
				loaded = true;
			} catch (Exception e) {
				log.error("Error loading colors.json", e);
				System.exit(0);
				loaded = true; // evita reintentos en bucle
			}
		}
	}

	public static Map<String, ColorEntry> getAll() {
		return Collections.unmodifiableMap(NAME_TO_COLOR);
	}

	public static ColorEntry get(String name) {
		return NAME_TO_COLOR.get(name);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class ColorsFile {
		public Map<String, ColorEntry> colors;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class ColorEntry {
		private int r;
		private int g;
		private int b;
		private int w; // opcional, 0 si no definido
	}
}



