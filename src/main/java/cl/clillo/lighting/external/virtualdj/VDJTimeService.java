package cl.clillo.lighting.external.virtualdj;

import java.util.ArrayList;
import java.util.List;

public class VDJTimeService extends Thread  implements VDJBMPEvent {

    private int count =0;
    private long minTime = 1000;
    private long nextTime = 0;
    private final List<VDJBMPEvent> listeners = new ArrayList<>();
    @Override
    public void run() {
        super.run();
        long time = 0;
        nextTime = System.currentTimeMillis()+minTime;
        while(true){

            try {
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            time+=10;
            if (System.currentTimeMillis()>nextTime) {
                tic();
                nextTime = System.currentTimeMillis()+minTime;
            }
        }
    }

    public void addListener(VDJBMPEvent vdjbmpEvent){
        listeners.add(vdjbmpEvent);
    }

    private void tic(){
        for (VDJBMPEvent vdjbmpEvent : listeners) {
            try {
                vdjbmpEvent.beat();
                count ++;
                if (count>16)
                    count = 1;
                vdjbmpEvent.beat(count);
                switch (Math.abs(count%16)+1) {
                    case 1:
                        vdjbmpEvent.beatX2(1);
                        vdjbmpEvent.beatX4(1);
                        vdjbmpEvent.beatX8(1);
                        break;
                    case 3:
                        vdjbmpEvent.beatX2(2);
                        break;
                    case 5:
                        vdjbmpEvent.beatX2(3);
                        vdjbmpEvent.beatX4(2);
                        break;
                    case 7:
                        vdjbmpEvent.beatX2(4);
                        break;
                    case 9:
                        vdjbmpEvent.beatX2(5);
                        vdjbmpEvent.beatX4(3);
                        vdjbmpEvent.beatX8(2);
                        break;
                    case 11:
                        vdjbmpEvent.beatX2(6);
                        break;
                    case 13:
                        vdjbmpEvent.beatX2(7);
                        vdjbmpEvent.beatX4(4);
                        break;
                    case 15:
                        vdjbmpEvent.beatX2(8);
                        break;
                }
                if (count%2==0)
                    vdjbmpEvent.beatX2();
                if (count%4==0)
                    vdjbmpEvent.beatX4();
                if (count%8==0)
                    vdjbmpEvent.beatX8();
                if (count%16==0)
                    vdjbmpEvent.beatX16();


            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        long myMinTime = (long)((bpm/240.0)*1000.0);
        if (myMinTime!=minTime){
            //tic();
            minTime +=  (myMinTime>minTime?10:-10);
      //      minTime = myMinTime;
            System.out.println(minTime+"\t"+myMinTime);
           // if (myMinTime!=minTime)
            nextTime = System.currentTimeMillis();
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

    @Override
    public void beatD2() {

    }

    @Override
    public void beatD4() {

    }
}
