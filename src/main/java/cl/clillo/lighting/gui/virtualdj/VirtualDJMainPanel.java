package cl.clillo.lighting.gui.virtualdj;

import cl.clillo.lighting.external.virtualdj.OS2LServer;
import cl.clillo.lighting.external.virtualdj.VDJBMPEvent;
import cl.clillo.lighting.utils.JsonUtils;
import cl.clillo.lighting.utils.http.HttpClient;
import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.SystemTray;
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

    private String baseUrl;

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

        this.add(txtBPM);
        this.add(txtChange);
        this.add(txtPos);
        this.add(txtStrength);
        this.add(txtBeat);
        this.add(txtVirtualDJIP);
        this.add(txtSongName);

        OS2LServer.getInstance().addListener(this);
    }


    @Override
    public void beat(boolean change, int pos, double bpm, double strength) {
        txtBPM.setText(String.valueOf(bpm));
        txtChange.setText(String.valueOf(change));
        txtPos.setText(String.valueOf(pos));
        txtStrength.setText(String.valueOf(strength));

        txtBeat.setText(String.valueOf(pos%16 + 1 ));
        if (change) {
            txtSongName.setText("");
            getSongName();
        }
    }

    @Override
    public void remoteIp(String ip) {
        txtVirtualDJIP.setText(ip);
        baseUrl = "http://"+ip+":80/";

        getSongName();
    }

    private void getSongName(){
        String node = httpClient.getString(baseUrl + "query?script=get_filepath", Map.of(), Map.of());
        txtSongName.setText(node);
    }
}