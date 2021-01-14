package cl.clillo.ilumination.executor;

import java.util.LinkedList;

import cl.clillo.ilumination.fixture.dmx.MovingHead;

public class ColaPosiciones implements Runnable  {

	private LinkedList<Punto> lista;
	private MovingHead estacion;
	private ListenerFinMovimiento listenerFinMovimiento;
	
	public ColaPosiciones(MovingHead estacion) {
		this.estacion = estacion;
		lista = new LinkedList<Punto>();
	}

	public synchronized boolean estaMoviendose(){
		return !lista.isEmpty();
	}
	
	public synchronized void agregar() {
		lista.addFirst(new Punto());
		notifyAll();
	}
	
	public synchronized void agregar(Punto punto) {
		lista.addLast(punto);
		notifyAll();
	}
	
	public synchronized void freeze() {
		while (!lista.isEmpty())
			lista.remove();
		notifyAll();
	}

	private synchronized Punto obtener() {
		while (lista.isEmpty()){
			try {
				estacion.setMoviendose(false);
				wait();
			} catch (InterruptedException e) {
			}
		}
		
		Punto ins = lista.getFirst();
		lista.removeFirst();
		
		if (lista.isEmpty()){
			estacion.setMoviendose(false);
			if (listenerFinMovimiento!=null)
				listenerFinMovimiento.finalizaMovimiento();
		}
		return ins;
	}

	public void run() {
		Punto instruccion;
		while (true) {
			instruccion = obtener();
			if (!instruccion.isFake()){
				estacion.saltarA(instruccion.getPosX(), instruccion.getPosY());
				estacion.setMoviendose(true);
			}
			
			try {
				Thread.sleep(instruccion.getVelocidad());
			} catch (InterruptedException e1) {
			}
		}
	}

	public void setListenerFinMovimiento(ListenerFinMovimiento listenerFinMovimiento) {
		this.listenerFinMovimiento = listenerFinMovimiento;
	}
}
