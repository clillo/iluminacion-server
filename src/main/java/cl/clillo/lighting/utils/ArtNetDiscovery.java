package cl.clillo.lighting.utils;

import ch.bildspur.artnet.ArtNet;
import ch.bildspur.artnet.ArtNetException;
import ch.bildspur.artnet.ArtNetNode;
import ch.bildspur.artnet.ArtNetNodeDiscovery;
import ch.bildspur.artnet.events.ArtNetDiscoveryListener;
import ch.bildspur.artnet.packets.ArtDmxPacket;

import java.net.SocketException;
import java.util.List;

public class ArtNetDiscovery {

    private ArtNetManager artnet;

    void setup(){
        artnet = new ArtNetManager();
        artnet.test();

    }

    static class ArtNetManager implements ArtNetDiscoveryListener {

        ArtNetNode openDMX;
        int sequenceID=0;

        public void discoveredNewNode(ArtNetNode node) {
            if (openDMX == null) {
                openDMX = node;
                System.out.println("found openDMX");
            }
            System.out.println("New Nodes added: " + node.getIPAddress());
        }

        public void discoveredNodeDisconnected(ArtNetNode node) {
            System.out.println("node disconnected: " + node);
            if (node == openDMX) {
                openDMX = null;
            }
        }

        public void discoveryCompleted(List<ArtNetNode> nodes) {
            System.out.println(nodes.size() + " nodes found:");
            for (ArtNetNode n : nodes) {
                System.out.println(n.getIPAddress());
                System.out.println(n.getSubNet());
                System.out.println(n.getNodeStatus());

                System.out.println(n.getShortName());

            }
        }

        public void discoveryFailed(Throwable t) {
            System.out.println("discovery failed");
        }

        void update(){

        }

        /** start artnet server and set buffer to light state */

        public void test(){
            ArtNet artnet = new ArtNet();
            try {
                artnet.start();
                ArtNetNodeDiscovery discovery = artnet.getNodeDiscovery();
                discovery.addListener(this);
                discovery.setInterval(1000);
                discovery.start();
                while (true) {
                    //println(openDMX);
                    if (openDMX != null) {
                        ArtDmxPacket dmx = new ArtDmxPacket();
                        dmx.setUniverse(openDMX.getSubNet(),
                                openDMX.getDmxOuts()[0]);
                        dmx.setSequenceID(sequenceID % 255);
                        byte[] buffer = new byte[510];
                        for (int i = 0; i < buffer.length; i++) {
                            buffer[i] =
                                    (byte) (Math.sin(sequenceID * 0.05 + i * 0.8) * 127 + 128);
                        }
                        dmx.setDMX(buffer, buffer.length);
                        artnet.unicastPacket(dmx, openDMX.getIPAddress());
                        dmx.setUniverse(openDMX.getSubNet(),
                                openDMX.getDmxOuts()[1]);
                        artnet.unicastPacket(dmx, openDMX.getIPAddress());
                        sequenceID++;
                    }
                    Thread.sleep(30);
                }
            } catch (SocketException | InterruptedException | ArtNetException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args){
        ArtNetDiscovery artNetDiscovery = new ArtNetDiscovery();
        artNetDiscovery.setup();

    }
}
