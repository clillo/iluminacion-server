package cl.clillo.lighting.dmx;

import cl.clillo.lighting.ArtNet;
import cl.clillo.lighting.model.Point;

public class Dmx {
	
	private final NotificablesCollection notificablesCollection;

    
    Dmx(){
    	notificablesCollection = new NotificablesCollection();
	}

	public void send(int channel, int data){
		notificablesCollection.enviar(channel, data);
		ArtNet artNet = ArtNet.getInstance();
		artNet.send(channel, data);
	}

	public void send(Point point){
		send(point.getCanal(), point.getDmx());

	}

	public void registraEnviable(DmxEnviable enviable){
		notificablesCollection.registraEnviable(enviable);
	}
	
	public int obtieneValorActualCanal(int canal){
		return notificablesCollection.obtieneValorActualCanal(canal);
	}
}
