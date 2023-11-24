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
        this.showList.addAll(showList);

        OS2LServer.getInstance().addListener(this);
        previousCount = System.currentTimeMillis();
    }

    public void setIos2LEventListener(IOS2LEventListener ios2LEventListener) {
        this.ios2LEventListener = ios2LEventListener;
    }

    public void run() {
        while (true) {
            try {
                if (ios2LEventListener!=null ) {
                    ios2LEventListener.changeTimes(time, ((long) (time / 2.0)));

                }
                String a = String.valueOf((long)(time / 16.0));

                if (time<1000 && time>100) {
                    Thread.sleep((long)(time / 8.0));
                    beatX4();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        actualBPM = bpm;
        if (ios2LEventListener!=null ) {//&& actualBPM!=bpm)
            ios2LEventListener.changeBPM(actualBPM);
            ios2LEventListener.pos(pos);
        }
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
       broadcastBeat(IOS2LEventListener.Type.BEAT_X_1);

    }

    @Override
    public void beatX2() {

    }

    @Override
    public void beatX2(int beat) {
        broadcastBeat(IOS2LEventListener.Type.BEAT_X_2);
    }

    @Override
    public void beatX4() {

    }

    @Override
    public void beatX4(int beat) {
        broadcastBeat(IOS2LEventListener.Type.BEAT_X_4);

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

    private void broadcastBeat(IOS2LEventListener.Type type){
        for (Show show : showList) {
            if (show.isExecuting() && show.getVdjType() == type) {
                show.getStepExecutor().executeOS2LScheduler();
            }
        }

        ArtNet.getInstance().broadCast();
    }
}
