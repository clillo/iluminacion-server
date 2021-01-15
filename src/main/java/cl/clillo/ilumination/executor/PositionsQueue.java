package cl.clillo.ilumination.executor;

import java.util.LinkedList;

import cl.clillo.ilumination.fixture.dmx.MovingHead;

public class PositionsQueue implements Runnable  {

	private LinkedList<Punto> queue;
	private MovingHead fixture;
	private ListenerFinMovimiento listenerFinMovimiento;
	
	public PositionsQueue(MovingHead fixture) {
		this.fixture = fixture;
		queue = new LinkedList<Punto>();
	}

	public synchronized boolean estaMoviendose(){
		return !queue.isEmpty();
	}
	
	public synchronized void agregar() {
		queue.addFirst(new Punto());
		notifyAll();
	}
	
	public synchronized void agregar(Punto punto) {
		queue.addLast(punto);
		notifyAll();
	}
	
	public synchronized void freeze() {
		while (!queue.isEmpty())
			queue.remove();
		notifyAll();
	}

	private synchronized Punto obtener() {
		while (queue.isEmpty()){
			try {
				fixture.setMoviendose(false);
				wait();
			} catch (InterruptedException e) {
			}
		}
		
		final Punto point = queue.getFirst();
		queue.removeFirst();
		
		if (queue.isEmpty()){
			fixture.setMoviendose(false);
			if (listenerFinMovimiento!=null)
				listenerFinMovimiento.finalizaMovimiento();
		}
		return point;
	}

	public void run() {
		Punto point;
		while (true) {
			point = obtener();
			if (!point.isFake()){
				fixture.saltarA(point.getPosX(), point.getPosY());
				fixture.setMoviendose(true);
			}
			
			try {
				Thread.sleep(point.getVelocidad());
			} catch (InterruptedException e1) {
			}
		}
	}

	public void setListenerFinMovimiento(ListenerFinMovimiento listenerFinMovimiento) {
		this.listenerFinMovimiento = listenerFinMovimiento;
	}
}
