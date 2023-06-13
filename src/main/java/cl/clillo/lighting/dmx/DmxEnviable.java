package cl.clillo.lighting.dmx;

public interface DmxEnviable {

	public int[] getCanales();
	
	public void actualizaEnvioDmx(int canal, int valor);
}
