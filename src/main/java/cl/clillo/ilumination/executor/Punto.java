package cl.clillo.ilumination.executor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Punto {
	
	private int posX;
	private int posY;
	private int speed;

}
