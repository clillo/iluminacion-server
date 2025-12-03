package cl.clillo.lighting.gui.controller;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import cl.clillo.lighting.external.dmx.Dmx;
import lombok.Getter;
import lombok.Setter;

/**
 * Panel de visualización para un fixture tipo BeeEye.
 * Dispone 7 LEDs hexagonales: 6 en anillo y 1 central.
 * Cada LED acepta color en formato RGBW con componentes en rango [0..255].
 * <p>
 * Índices de LED:
 * 0..5 = anillo (en sentido horario, empezando en la parte superior)
 * 6    = centro
 */
public class BeeEyePanel extends JPanel implements Dmx.DmxListener {

	private static final int LED_COUNT = 7;
	private static final float OUTLINE_SCALE = 1.04f;
	private static final int DEFAULT_SIZE = 260;
	@Setter
	private int baseChannel = 21;
	@Setter
	@Getter
	private int universe = 1;

	@Getter
	private String name = "BeeEye";

	private final int[] red = new int[LED_COUNT];
	private final int[] green = new int[LED_COUNT];
	private final int[] blue = new int[LED_COUNT];
	private final int[] white = new int[LED_COUNT];

	public BeeEyePanel() {
		this.setOpaque(false);
		this.setToolTipText("");
	}


	public void setName(String name) {
		if (name != null && !name.isBlank()) {
			this.name = name;
			repaint();
		}
	}

	/**
	 * Ajusta un valor DMX a partir de un canal absoluto y lo mapea a (LED, componente).
	 * Mapeo: desde baseChannel, cada LED ocupa 4 canales en orden R,G,B,W.
	 * LED i empieza en baseChannel + i*4.
	 *
	 * @param channel canal absoluto
	 * @param value   valor [0..255]
	 */
	public void setValue(int channel, int value) {
		int offset = channel - baseChannel;
		if (offset < 0) return;
		int totalChannels = LED_COUNT * 4;
		if (offset >= totalChannels) return;
		int ledIndex = offset / 4;
		int component = offset % 4; // 0=R,1=G,2=B,3=W
		int v = clamp255(value);

		//if (v==255 && "BeeEye 1".equals(name))
	//		System.out.println(channel+"\t"+offset+"\t"+ledIndex+"\t"+component+"\t"+v);

		switch (component) {
			case 0: red[ledIndex] = v; break;
			case 1: green[ledIndex] = v; break;
			case 2: blue[ledIndex] = v; break;
			case 3: white[ledIndex] = v; break;
			default: return;
		}
		repaint();
	}

	@Override
	public void addNotify() {
		super.addNotify();
		Dmx.getInstance().addListener(this);
	}

	@Override
	public void removeNotify() {
		Dmx.getInstance().removeListener(this);
		super.removeNotify();
	}

	@Override
	public void onDmxValueSent(int universe, int dmxChannel, int value) {
		if (this.universe != universe) return;
		if (SwingUtilities.isEventDispatchThread()) {
			setValue(dmxChannel, value);
		} else {
			SwingUtilities.invokeLater(() -> setValue(dmxChannel, value));
		}
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		// Geometría coherente con el pintado
		int w = getWidth();
		int h = getHeight();
		int size = Math.min(w, h);
		double cx = w / 2.0;
		double cy = h / 2.0;
		double ringRadius = size * 0.35;
		double ledRadius = size * 0.15;
		double centerRadius = size * 0.18;

		// Construye polígonos de los 6 del anillo
		for (int i = 0; i < 6; i++) {
			double angle = -Math.PI / 2 + i * (Math.PI / 3);
			Point2D.Double pos = new Point2D.Double(
					cx + ringRadius * Math.cos(angle),
					cy + ringRadius * Math.sin(angle)
			);
			Polygon poly = buildRegularPolygon(pos, ledRadius, 6, -Math.PI / 2);
			if (poly.contains(e.getPoint())) {
				return buildLedTooltip(i);
			}
		}
		// Centro
		Polygon center = buildRegularPolygon(new Point2D.Double(cx, cy), centerRadius, 6, -Math.PI / 2);
		if (center.contains(e.getPoint())) {
			return buildLedTooltip(6);
		}
		return null;
	}

	private String buildLedTooltip(int ledIndex) {
		int rCh = baseChannel + ledIndex * 4 + 0;
		int gCh = baseChannel + ledIndex * 4 + 1;
		int bCh = baseChannel + ledIndex * 4 + 2;
		int wCh = baseChannel + ledIndex * 4 + 3;
		// HTML para multilínea
		return "<html>"
				+ "<b>" + name + "</b><br/>"
				+ "LED " + ledIndex + "<br/>"
				+ "R: ch " + rCh + " = " + red[ledIndex] + "<br/>"
				+ "G: ch " + gCh + " = " + green[ledIndex] + "<br/>"
				+ "B: ch " + bCh + " = " + blue[ledIndex] + "<br/>"
				+ "W: ch " + wCh + " = " + white[ledIndex]
				+ "</html>";
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(DEFAULT_SIZE, DEFAULT_SIZE);
	}

	/**
	 * Establece el color RGBW de un LED.
	 *
	 * @param index índice del LED [0..6]
	 * @param r     rojo [0..255]
	 * @param g     verde [0..255]
	 * @param b     azul [0..255]
	 * @param w     blanco [0..255]
	 */
	public void setLedColor(int index, int r, int g, int b, int w) {
		if (index < 0 || index >= LED_COUNT) return;
		red[index] = clamp255(r);
		green[index] = clamp255(g);
		blue[index] = clamp255(b);
		white[index] = clamp255(w);
		repaint();
	}

	/**
	 * Establece de una vez todos los colores RGBW.
	 * Cada arreglo debe tener tamaño 7 (o se ignora).
	 */
	public void setAllColors(int[] r, int[] g, int[] b, int[] w) {
		if (!validAll(r, g, b, w)) return;
		for (int i = 0; i < LED_COUNT; i++) {
			red[i] = clamp255(r[i]);
			green[i] = clamp255(g[i]);
			blue[i] = clamp255(b[i]);
			white[i] = clamp255(w[i]);
		}
		repaint();
	}

	private boolean validAll(int[] r, int[] g, int[] b, int[] w) {
		return r != null && g != null && b != null && w != null
				&& r.length == LED_COUNT && g.length == LED_COUNT
				&& b.length == LED_COUNT && w.length == LED_COUNT;
	}

	/**
	 * Devuelve el color visible (RGB) mezclando el canal blanco.
	 * Simple aproximación: R' = min(255, R + W) (igual para G', B').
	 */
	private Color getDisplayColor(int index) {
		int r = clamp255(red[index] + white[index]);
		int g = clamp255(green[index] + white[index]);
		int b = clamp255(blue[index] + white[index]);
		return new Color(r, g, b);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		try {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth();
			int h = getHeight();
			int size = Math.min(w, h);

			// Centro y radios
			double cx = w / 2.0;
			double cy = h / 2.0;

			// Radio del anillo y del LED
			double ringRadius = size * 0.35;      // distancia del centro a cada LED del anillo
			double ledRadius = size * 0.15;       // tamaño del hexágono de cada LED del anillo
			double centerRadius = size * 0.18;    // tamaño del hexágono central

			// Fondo circular sutil (bezel)
			g2.setColor(new Color(30, 30, 30));
			g2.fillOval((int) (cx - size * 0.45), (int) (cy - size * 0.45), (int) (size * 0.9), (int) (size * 0.9));

			// Dibuja LEDs del anillo (6)
			for (int i = 0; i < 6; i++) {
				double angle = -Math.PI / 2 + i * (Math.PI / 3); // comienza en arriba y avanza horario
				Point2D.Double pos = new Point2D.Double(
						cx + ringRadius * Math.cos(angle),
						cy + ringRadius * Math.sin(angle)
				);
				drawLedHexagon(g2, pos, ledRadius, getDisplayColor(i));
			}

			// LED central (índice 6)
			drawLedHexagon(g2, new Point2D.Double(cx, cy), centerRadius, getDisplayColor(6));

		} finally {
			g2.dispose();
		}
	}

	private void drawLedHexagon(Graphics2D g2, Point2D.Double center, double radius, Color fill) {
		Polygon hex = buildRegularPolygon(center, radius, 6, -Math.PI / 2);

		// Relleno
		g2.setColor(fill);
		g2.fillPolygon(hex);

		// Borde exterior leve
		g2.setStroke(new BasicStroke(Math.max(1f, (float) (radius * 0.06))));
		g2.setColor(new Color(0, 0, 0, 160));
		Polygon outline = buildRegularPolygon(center, radius * OUTLINE_SCALE, 6, -Math.PI / 2);
		g2.drawPolygon(outline);

		// Brillo interior sutil
		g2.setStroke(new BasicStroke(Math.max(1f, (float) (radius * 0.02))));
		g2.setColor(new Color(255, 255, 255, 60));
		g2.drawPolygon(hex);
	}

	private static Polygon buildRegularPolygon(Point2D.Double center, double radius, int sides, double startAngle) {
		Polygon poly = new Polygon();
		for (int i = 0; i < sides; i++) {
			double a = startAngle + i * (2 * Math.PI / sides);
			int x = (int) Math.round(center.x + radius * Math.cos(a));
			int y = (int) Math.round(center.y + radius * Math.sin(a));
			poly.addPoint(x, y);
		}
		return poly;
	}

	private static int clamp255(int v) {
		if (v < 0) return 0;
        return Math.min(v, 255);
    }
}


