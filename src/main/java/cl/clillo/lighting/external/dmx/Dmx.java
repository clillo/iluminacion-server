package cl.clillo.lighting.external.dmx;

import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.ShowCollection;

public class Dmx {

	private ShowCollection showCollection;
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

	private void setShowCollection(ShowCollection showCollection) {
		this.showCollection = showCollection;
	}

	private Dmx(){
	}

	public void sendForce(final int universe, final int dmxChannel, final int dmxValue){
		artNet.send(universe, dmxChannel, dmxValue);
	}

	public void send(final int dmxChannel, final int dmxValue){
		artNet.send(dmxChannel, showCollection.getRealDMXValue(dmxChannel, dmxValue));
	}

	public void send(final int universe, final int dmxChannel, final int dmxValue){
		artNet.send(universe, dmxChannel, showCollection.getRealDMXValue(dmxChannel, dmxValue));
	}

	public void send(final QLCPoint point){
		send(point.getDmxChannel(), point.getData());
	}
}
