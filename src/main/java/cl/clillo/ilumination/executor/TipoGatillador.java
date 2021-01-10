package cl.clillo.ilumination.executor;

public enum TipoGatillador {
	VDJBeat      ("VDJ Beat"),
	VDJMedioBeat ("VDJ Medio Beat"),
	VDJCuartoBeat("VDJ Cuarto Beat"),
	RelojInterno ("Reloj Interno"),
	RelojExterno ("Reloj Externo");
	
	private String nombre;
	
	private TipoGatillador( String nombre) {
		this.nombre = nombre;
	}
	
	@Override
	public String toString() {
		return nombre;
	}
}
