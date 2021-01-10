package cl.clillo.ilumination.dmx;

import java.util.ArrayList;
import java.util.HashMap;

public class ListaNotificables {

	private HashMap<Integer, ArrayList<DmxEnviable>> enviables;
    private int valoresActuales[] = new int[513];

	public ListaNotificables() {
		enviables = new HashMap<Integer, ArrayList<DmxEnviable>>();

		for (int i = 0; i < 513; i++)
			enviables.put(i, new ArrayList<DmxEnviable>());
	}

	public void enviar(int canal, int dato) {
		if (canal>512)
			return;
				
		valoresActuales[canal] = dato;

		ArrayList<DmxEnviable> lista = enviables.get(canal);
		
	//	System.out.println("Notificando a: "+canal+","+dato+"\t"+lista);

		for(DmxEnviable e: lista)
			e.actualizaEnvioDmx(canal, dato);
	}
	
	public void registraEnviable(DmxEnviable enviable){
		//System.out.println("Registrando enviable: "+enviable.getClass().getName());
		
		for (int canal: enviable.getCanales()){
			ArrayList<DmxEnviable> lista = enviables.get(canal);
			lista.add(enviable);
		}
		
		for (int canal=1; canal<=512; canal++){
			ArrayList<DmxEnviable> lista = enviables.get(canal);
			
			for(DmxEnviable e: lista)
				e.actualizaEnvioDmx(canal, valoresActuales[canal]);
		}
	}
	
	public int obtieneValorActualCanal(int canal){
		return valoresActuales[canal];
	}
}
