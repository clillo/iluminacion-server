package cl.clillo.lighting.external.dmx;

import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.ShowCollection;

public class Dmx {

	private ShowCollection showCollection;
	private final NotificablesCollection notificablesCollection;
	private final ArtNet artNet = ArtNet.getInstance();

	private static final class InstanceHolder {
		private static Dmx instance;

		public static Dmx getInstance() {
			if (instance == null) {
				instance = new Dmx();
				instance.setShowCollection(ShowCollection.getInstance());
			}
			return instance;
		}

	}

	public static Dmx getInstance() {
		return Dmx.InstanceHolder.getInstance();
	}

	public void setShowCollection(ShowCollection showCollection) {
		this.showCollection = showCollection;
	}

	private Dmx(){
		notificablesCollection = new NotificablesCollection();
	}

	public void send(final int dmxChannel, final int dmxValue){
		artNet.send(dmxChannel, showCollection.getRealDMXValue(dmxChannel, dmxValue));

	}

	public void send(final Point point){
		send(point.getCanal(), point.getDmx());

	}

	public void send(final QLCPoint point){
		send(point.getDmxChannel(), point.getData());

	}

	public void registraEnviable(DmxEnviable enviable){
		notificablesCollection.registraEnviable(enviable);
	}
	
	public int obtieneValorActualCanal(int canal){
		return notificablesCollection.obtieneValorActualCanal(canal);
	}
}
