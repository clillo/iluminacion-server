package cl.clillo.lighting.external.dmx;

public interface DmxEnviable {

	public int[] getCanales();
	
	public void actualizaEnvioDmx(int canal, int valor);
}
