package cl.clillo.ilumination.dmx;

import cl.clillo.ilumination.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Dmx {
	
	private ListaNotificables listaNotificables;

	@Autowired
    private ComunicacionHttpDMX dmx;
    
    Dmx(){
    	listaNotificables = new ListaNotificables();
		dmx = new ComunicacionHttpDMX();
	}


	public void enviar(int canal, int dato){
		listaNotificables.enviar(canal, dato);
			
		if (dmx==null)
			return;
		
		dmx.enviar(canal, dato);		
	}

	public void enviar(Point point){
		listaNotificables.enviar(point.getCanal(), point.getDmx());

		if (dmx==null)
			return;

		dmx.enviar(point.getCanal(), point.getDmx());
	}

	public void registraEnviable(DmxEnviable enviable){
		listaNotificables.registraEnviable(enviable);
	}
	
	public int obtieneValorActualCanal(int canal){
		return listaNotificables.obtieneValorActualCanal(canal);
	}
}
