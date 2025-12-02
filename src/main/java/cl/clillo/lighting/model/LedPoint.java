package cl.clillo.lighting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un punto de color para un LED específico.
 * id: índice de LED (0..n) dentro del fixture BeeEye (anillo de 6, centro ignorado si procede)
 * name: nombre descriptivo (opcional)
 * r,g,b,w: intensidades 0..255
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedPoint {
	private int id;
	private String name;
	private int r;
	private int g;
	private int b;
	private int w;
}
