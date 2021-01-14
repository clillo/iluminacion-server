package cl.clillo.ilumination.executor;

public class Punto {
	
	private long posX;
	private long posY;
	private int velocidad;
	private boolean fake;
	
	public Punto() {
		fake = true;
	}
	
	public Punto(long posX, long posY, int velocidad){
		this.posX = posX;
		this.posY = posY;	
		this.velocidad = velocidad;
		fake = false;
	}

	public int getVelocidad() {
		return velocidad;
	}

	public long getPosX() {
		return posX;
	}

	public long getPosY() {
		return posY;
	}

	public boolean isFake() {
		return fake;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Punto [posX=");
		builder.append(posX);
		builder.append(", posY=");
		builder.append(posY);
		builder.append(", velocidad=");
		builder.append(velocidad);
		builder.append(", fake=");
		builder.append(fake);
		builder.append("]");
		return builder.toString();
	}
}
