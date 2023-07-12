package cl.clillo.lighting.model;

import cl.clillo.lighting.fixture.qlc.QLCFixture;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QLCPoint {

    private QLCFixture fixture;
    private int channel;
    private int dmxChannel;
    private int data;

    public String getOperationalId(){
        return fixture.getId()+"."+channel;
    }

    public String toString() {
        return "QLCPoint(fixture=" + this.getFixture().getId() + ", channel=" + this.getChannel() + ", dmxChannel=" + this.getDmxChannel() + ", data=" + this.getData() + ")";
    }
}
