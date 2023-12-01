package cl.clillo.lighting.fixture.qlc;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@ToString
@Getter
public class QLCRoboticFixture extends QLCFixture{

    private int panDmxChannel;
    private int tiltDmxChannel;
    private int panFineDmxChannel;
    private int tiltFineDmxChannel;

    public QLCRoboticFixture(String manufacturer, String model, String mode, int id, String name, int universe,
                             int address, int channels, QLCFixtureModel fixtureModel) {
        super(manufacturer, model, mode, id, name, universe, address, channels, fixtureModel);

        panDmxChannel = -1;
        tiltDmxChannel = -1;
        panFineDmxChannel = -1;
        tiltFineDmxChannel = -1;

        for (int i=0; i<fixtureModel.getChannels().length; i++){
            String str = fixtureModel.getChannels()[i];
            if ("pan".equalsIgnoreCase(str))
                panDmxChannel = i + address;
            if ("tilt".equalsIgnoreCase(str))
                tiltDmxChannel = i + address;
            if ("pan fine".equalsIgnoreCase(str))
                panFineDmxChannel = i + address;
            if ("tilt fine".equalsIgnoreCase(str))
                tiltFineDmxChannel = i + address;
        }

        if (panDmxChannel==-1){
            System.out.println("Fixture ["+name+"] doesn't have an pan channel");
            System.exit(0);
        }

        if (tiltDmxChannel==-1){
            System.out.println("Fixture ["+name+"] doesn't have an tilt channel");
            System.exit(0);
        }

        if (panFineDmxChannel==-1){
            System.out.println("Fixture ["+name+"] doesn't have an pan fine channel");
            System.exit(0);
        }

        if (tiltFineDmxChannel==-1){
            System.out.println("Fixture ["+name+"] doesn't have an tilt fine channel");
            System.exit(0);
        }
    }

    public static QLCRoboticFixture build(final int id, final int dmxAddress, final QLCFixtureModel fixtureModel){
        String manufacturer = "manufacturer";
        String model = "model";
        String mode = "mode";
        int universe = 0;

        return new QLCRoboticFixture(manufacturer, model, mode, id, "fixture: "+id, universe, dmxAddress-1,
                fixtureModel.getChannels().length, fixtureModel);
    }

    public QLCFixture.ChannelType getChannelType(int dmxChannel){
        if (dmxChannel == panDmxChannel)
             return ChannelType.PAN;

        if (dmxChannel == tiltDmxChannel)
            return ChannelType.TILT;

        if (dmxChannel == panFineDmxChannel)
            return ChannelType.PAN_FINE;

        if (dmxChannel == tiltFineDmxChannel)
            return ChannelType.TILT_FINE;

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QLCRoboticFixture)) return false;
        QLCRoboticFixture that = (QLCRoboticFixture) o;
        return getPanDmxChannel() == that.getPanDmxChannel() && getTiltDmxChannel() == that.getTiltDmxChannel() && getPanFineDmxChannel() == that.getPanFineDmxChannel() && getTiltFineDmxChannel() == that.getTiltFineDmxChannel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPanDmxChannel(), getTiltDmxChannel(), getPanFineDmxChannel(), getTiltFineDmxChannel());
    }
}
