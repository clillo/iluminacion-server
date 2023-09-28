package cl.clillo.lighting.dmx;

import cl.clillo.lighting.model.Point;
import cl.clillo.lighting.model.QLCPoint;

public class Dmx {
	
	private final NotificablesCollection notificablesCollection;

	private static final class InstanceHolder {
		private static final Dmx instance = new Dmx();
	}

	public static Dmx getInstance() {
		return Dmx.InstanceHolder.instance;
	}

	private Dmx(){
    	notificablesCollection = new NotificablesCollection();
	}

	public void send(final int channel, final int data){
		//notificablesCollection.enviar(channel, data);
	//	System.out.println(channel+"\t"+data);
		ArtNet artNet = ArtNet.getInstance();
		artNet.send(channel, data);
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
