package cl.clillo.lighting.external.dmx;

import cl.clillo.lighting.model.QLCPoint;
import cl.clillo.lighting.model.ShowCollection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dmx {

	private ShowCollection showCollection;
	private final ArtNet artNet = ArtNet.getInstance();
	private final List<DmxListener> listeners = new CopyOnWriteArrayList<>();

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

	public interface DmxListener {
		/**
		 * Notificado cuando se envía un valor DMX.
		 * El valor corresponde al valor final transmitido (post-procesado por ShowCollection.getRealDMXValue).
		 */
		void onDmxValueSent(int universe, int dmxChannel, int value);
	}

	public void addListener(final DmxListener listener) {
		if (listener != null) listeners.add(listener);
	}

	public void removeListener(final DmxListener listener) {
		if (listener != null) listeners.remove(listener);
	}

	public void sendForce(final int universe, final int dmxChannel, final int dmxValue){
		artNet.send(universe, dmxChannel, dmxValue);
		invokeListeners(universe, dmxChannel, dmxValue);
	}

	public void send(final int dmxChannel, final int dmxValue){
		send(1, dmxChannel, dmxValue);
	}

	public void send(final int universe, final int dmxChannel, final int dmxValue){
		final int realValue = showCollection.getRealDMXValue(universe, dmxChannel, dmxValue);
		artNet.send(universe, dmxChannel, realValue);
		invokeListeners(universe, dmxChannel, realValue);
	}

	private void invokeListeners(final int universe, final int dmxChannel, final int dmxValue){
		for (DmxListener l : listeners) {
			l.onDmxValueSent(universe, dmxChannel, dmxValue);
		}
	}


	public void send(final QLCPoint point){
		send(point.getUniverse(), point.getDmxChannel(), point.getData());
	}
}
