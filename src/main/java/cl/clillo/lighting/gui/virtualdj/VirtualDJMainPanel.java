package cl.clillo.lighting.gui.virtualdj;

import cl.clillo.lighting.external.virtualdj.OS2LServer;
import cl.clillo.lighting.external.virtualdj.VDJBMPEvent;
import cl.clillo.lighting.utils.http.HttpClient;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Map;

public class VirtualDJMainPanel extends JPanel implements VDJBMPEvent {

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 600;
    public static final int HEIGHT1 = 300;

    private final HttpClient httpClient = new HttpClient();

    private final JTextField txtBPM = new JTextField();
    private final JTextField txtChange = new JTextField();
    private final JTextField txtPos = new JTextField();
    private final JTextField txtStrength = new JTextField();
    private final JTextField txtBeat = new JTextField();
    private final JTextField txtVirtualDJIP = new JTextField();
    private final JTextField txtSongName = new JTextField();

    private final JTextArea txtCommands = new JTextArea();
    private final JTextArea txtButtons = new JTextArea();

    private String baseUrl;

    private final boolean[] grid16 = new boolean[16];
    private final boolean[] grid8 = new boolean[8];
    private final boolean[] grid4 = new boolean[4];
    private final boolean[] grid2 = new boolean[2];

    private boolean beat;
    private boolean beat2;
    private boolean beat4;
    private boolean beat8;
    private boolean beat16;

    public VirtualDJMainPanel() {
        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);

        txtBPM.setBounds(10,10, 100, 30);
        txtChange.setBounds(10,50, 100, 30);
        txtPos.setBounds(10,90, 100, 30);
        txtStrength.setBounds(10,130, 100, 30);
        txtBeat.setBounds(130,90, 100, 30);
        txtVirtualDJIP.setBounds(200,10, 150, 30);
        txtSongName.setBounds(200,50, 600, 30);

        final JScrollPane pnlCommands = new JScrollPane(txtCommands);
        pnlCommands.setBounds(10,170, 200, 200);

        final JScrollPane pnlButtons = new JScrollPane(txtButtons);
        pnlButtons.setBounds(230,170, 200, 200);
        this.add(txtBPM);
        this.add(txtChange);
        this.add(txtPos);
        this.add(txtStrength);
        this.add(txtBeat);
        this.add(txtVirtualDJIP);
        this.add(txtSongName, BorderLayout.CENTER);
        this.add(pnlCommands, BorderLayout.CENTER);
        this.add(pnlButtons);

        JPanel canvas = new JPanel() {
            private static final long serialVersionUID = 9056031188937687827L;

            public void paint(final Graphics g) {
                super.paint(g);

                if (beat)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.BLACK);
                g.fillRect(420, 10, 20, 20);

                if (beat2)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.BLACK);
                g.fillRect(420, 40, 20, 20);

                if (beat4)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.BLACK);
                g.fillRect(420, 70, 20, 20);

                if (beat8)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.BLACK);
                g.fillRect(420, 100, 20, 20);

                if (beat16)
                    g.setColor(Color.GREEN);
                else
                    g.setColor(Color.BLACK);
                g.fillRect(420, 130, 20, 20);

                int i = 0;
                for (boolean circle : grid16) {
                    if (circle)
                        g.setColor(Color.RED);
                    else
                        g.setColor(Color.BLACK);
                    g.fillOval(i * 26, 10, 20, 20);
                    i++;
                }

                i = 0;
                for (boolean circle : grid8) {
                    if (circle)
                        g.setColor(Color.RED);
                    else
                        g.setColor(Color.BLACK);
                    g.fillOval(i * 26, 40, 20, 20);
                    i++;
                }
                i = 0;
                for (boolean circle : grid4) {
                    if (circle)
                        g.setColor(Color.RED);
                    else
                        g.setColor(Color.BLACK);
                    g.fillOval(i * 26, 70, 20, 20);
                    i++;
                }
                i = 0;
                for (boolean circle : grid2) {
                    if (circle)
                        g.setColor(Color.RED);
                    else
                        g.setColor(Color.BLACK);
                    g.fillOval(i * 26, 100, 20, 20);
                    i++;
                }
                repaint();
            }
        };

        //canvas.setBackground(Color.BLACK);
        canvas.setBounds(600, 180, 500, 200);

        this.add(canvas);
        OS2LServer.getInstance().addListener(this);
    }


    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        txtBPM.setText(String.valueOf(bpm));
        txtChange.setText(String.valueOf(change));
        txtPos.setText(String.valueOf(pos));
        txtStrength.setText(String.valueOf(strength));

        txtBeat.setText(String.valueOf(pos%16 + 1 ));

    }

    @Override
    public void remoteIp(String ip) {
        txtVirtualDJIP.setText(ip);
        baseUrl = "http://"+ip+":80/";

      //  getSongName();
    }

    @Override
    public void command(int id, int param) {
        txtCommands.append(id+"\t"+param+"\n");
    }

    @Override
    public void button(String name, String state) {
        txtSongName.setText(name);
        txtButtons.append(name+"\t"+state+"\n");
    }

    @Override
    public void beat(int beat) {
        Arrays.fill(grid16, false);
        grid16[beat-1]=true;
    }

    @Override
    public void beat() {
        beat=!beat;
    }

    @Override
    public void beatX2() {
        beat2=!beat2;
    }

    @Override
    public void beatX2(int beat) {
        Arrays.fill(grid8, false);
        grid8[beat-1]=true;
    }

    @Override
    public void beatX4() {
        beat4=!beat4;
    }

    @Override
    public void beatX4(int beat) {
        Arrays.fill(grid4, false);
        grid4[beat-1]=true;
    }

    @Override
    public void beatX8() {
        beat8=!beat8;
    }

    @Override
    public void beatX8(int beat) {
        Arrays.fill(grid2, false);
        grid2[beat-1]=true;
    }

    @Override
    public void beatX16() {
        beat16=!beat16;
    }

    private void getSongName(){
        String node = httpClient.getString(baseUrl + "query?script=get_filepath", Map.of(), Map.of());
        txtSongName.setText(node);
    }
}