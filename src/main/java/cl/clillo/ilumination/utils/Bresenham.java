package cl.clillo.ilumination.utils;

import java.util.LinkedList;
import cl.clillo.ilumination.executor.Punto;

public class Bresenham {
	
	public static LinkedList<Punto> moverA(final int origenX, final int origenY, final int destinoX, final int destinoY) {
		final LinkedList<Punto> lista = new LinkedList<Punto>();
		final int deltax = Math.abs(destinoX - origenX);
		final int deltay = Math.abs(destinoY - origenY);
		final boolean steep = deltay > deltax;
		final int xstep = (origenX < destinoX)?1:-1;
		final int ystep = (origenY < destinoY)?1:-1;
		int fakeX = origenX;
		int fakeY = origenY;

		int i;
		int error = 0;
	
		if (steep) {
			for (i = 0; i < deltay; i++) {
				fakeY+=ystep;

				error = error + deltax;
				if (2 * error >= deltay) {
					 fakeX+=xstep;
					error = error - deltay;
				}
				if (i%100 ==0)
				lista.addLast(Punto.builder()
						.posX(fakeX)
						.posY(fakeY)
						.speed(0)
						.build());
			}
		} else {
			for (i = 0; i < deltax; i++) {
				fakeX+=xstep;
				error = error + deltay;
				if (2 * error >= deltax) {
					fakeY+=ystep;
					error = error - deltax;
				}
				if (i%100 ==0)
				lista.addLast(Punto.builder()
						.posX(fakeX)
						.posY(fakeY)
						.speed(0)
						.build());
			}
		}

		lista.addLast(Punto.builder()
				.posX(destinoX)
				.posY(destinoY)
				.speed(0)
				.build());
		return lista;
	}

}
