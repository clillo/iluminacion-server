package cl.clillo.lighting;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import cl.clillo.lighting.external.dmx.ArtNet;
import cl.clillo.lighting.model.ColorsCatalog;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Test {

    private void writeDMXMap(){
        new Thread(){
            @Override
            public void run() {
                ArtNet artNet = ArtNet.getInstance();
                //  for (int u=6; u<20; u++)
                //    artNet.send(u, 255);

                int i=0;
                while(true){
                    i++;
                    artNet.send(2, 100 + 1, i%255);
                    artNet.send(2, 102 + 1, i%255);

                    System.out.println(i%255);
                    artNet.broadCast();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        ArtNet.setMode(ArtNet.ArtNetMode.DIRECT_ART_NET);
        ColorsCatalog.loadFromClasspath();
        ShowCollection collection = ShowCollection.getInstance();

        Show spidersOn = null;
        Show spidersOff = null;
        Show spidersFront = null;
        Show derbyOn = null;
        Show derbyOff = null;

        Show mBeanYellow = null;
        Show mBeanCircle = null;
        Show mBeanOnOff = null;

        for (Show show : collection.getShowList()) {
            if ("Spider All On".equalsIgnoreCase(show.getName()))
                spidersOn = show;
            if ("Spider All Off".equalsIgnoreCase(show.getName()))
                spidersOff = show;
            if ("Spider - Center".equalsIgnoreCase(show.getName()))
                spidersFront = show;
            if ("White Rotating".equalsIgnoreCase(show.getName()))
                derbyOn = show;
            if ("Derby Off".equalsIgnoreCase(show.getName()))
                derbyOff = show;

            if (show.getId()==3104)
                mBeanYellow = show;

            if (show.getId()==3511)
                mBeanCircle = show;

            if (show.getId()==3000)
                mBeanOnOff = show;
        }

        spidersFront.setExecuting(true);
        mBeanCircle.setExecuting(true);
        mBeanYellow.setExecuting(true);

        while(true) {

            spidersOn.setExecuting(true);
            spidersOff.setExecuting(false);

            derbyOn.setExecuting(false);
            derbyOff.setExecuting(true);

            mBeanOnOff.setExecuting(true);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            spidersOn.setExecuting(false);
            spidersOff.setExecuting(true);

            derbyOn.setExecuting(true);
            derbyOff.setExecuting(false);

            mBeanOnOff.setExecuting(false);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

       //new Test().writeDMXMap();
    }
}
