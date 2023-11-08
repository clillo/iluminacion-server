package cl.clillo.lighting.executor;

import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.external.virtualdj.OS2LServer;
import cl.clillo.lighting.external.virtualdj.VDJBMPEvent;
import cl.clillo.lighting.model.Show;

import java.util.ArrayList;
import java.util.List;

public class OS2LScheduler extends Thread implements VDJBMPEvent {

    private final List<Show> showList;
    private long time =0;
    private long previousCount =0;
    private double actualBPM;
    private IOS2LEventListener ios2LEventListener;

    public OS2LScheduler(final List<Show> showList) {
        this.showList = new ArrayList<>();
        for (Show show: showList)
            if (show.getId()==155 || show.getId()==153) {
                show.setStepExecutor(new OS2LStepExecutor(show));
                this.showList.add(show);
            }
        OS2LServer.getInstance().addListener(this);
        previousCount = System.currentTimeMillis();
    }

    public void setIos2LEventListener(IOS2LEventListener ios2LEventListener) {
        this.ios2LEventListener = ios2LEventListener;
    }

    public void run() {
        while (true) {
            try {
                if (ios2LEventListener!=null )
                    ios2LEventListener.changeTimes(time, ((long)(time / 2.0)));
                //System.out.println(time+"\t"+((long)(time / 16.0)));
                if (time<1000 && time>100) {
                    Thread.sleep((long)(time / 8.0));
                    beatX4();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        actualBPM = bpm;
        if (ios2LEventListener!=null )//&& actualBPM!=bpm)
            ios2LEventListener.changeBPM(actualBPM);

    }

    @Override
    public void remoteIp(String ip) {

    }

    @Override
    public void command(int id, int param) {

    }

    @Override
    public void button(String name, String state) {

    }

    @Override
    public void beat(int beat) {
        time = System.currentTimeMillis() - previousCount;
        previousCount = System.currentTimeMillis();


    }

    @Override
    public void beat() {

    }

    @Override
    public void beatX2() {

    }

    @Override
    public void beatX2(int beat) {

    }

    @Override
    public void beatX4() {
        for (Show show : showList) {
            if (show.isExecuting()) {
                show.getStepExecutor().executeOS2LScheduler();
            }
        }


        ArtNet.getInstance().broadCast();
    }

    @Override
    public void beatX4(int beat) {

    }

    @Override
    public void beatX8() {

    }

    @Override
    public void beatX8(int beat) {

    }

    @Override
    public void beatX16() {

    }
}
