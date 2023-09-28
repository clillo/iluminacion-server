package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;
import cl.clillo.lighting.midi.MidiHandler;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllerEditPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -5869553409971473557L;

    protected final JTextField txtScreenPosition;
    protected final JTextField txtCircleCenterX;
    protected final JTextField txtCircleCenterY;
    protected final JTextField txtCircleWidth;
    protected JTextField txtCircleHeight;
    protected final JTextField txtLineOriginX;
    protected final JTextField txtLineOriginY;
    protected final JTextField txtLineDestinyX;
    protected final JTextField txtLineDestinyY;

    protected JButton btnSave;
    protected JButton btnRun;

    private final MidiHandler midiHandler;
    private final int index;
    private final Map<String, QLCButton> buttonMapByPos;
    private final Map<QLCButton, Integer> buttonMapGroupId;

    public ControllerEditPanel(final MidiHandler midiHandler, final int index) {
        this.midiHandler = midiHandler;
        setLayout(null);
        this.index = index;

        txtScreenPosition = buildTxt(240);
        txtCircleCenterX = buildTxt(280);
        txtCircleCenterY = buildTxt(320);
        txtCircleWidth = buildTxt(360);
        txtCircleHeight = buildTxt(400);

        txtLineOriginX = buildTxt(440);
        txtLineOriginY = buildTxt(480);
        txtLineDestinyX = buildTxt(520);
        txtLineDestinyY = buildTxt(560);

        btnSave = new JButton();
        btnSave.setText("Save");
        btnSave.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 50, 120, 20);

        add(btnSave);

        btnRun = new JButton();
        btnRun.setText("Start/Stop");
        btnRun.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, 10, 120, 20);

        add(btnRun);

        final List<QLCButtonGroup> buttonGroups = MidiButtonFunctionRepository.getInstance().getButtonGroupMap(index);

        buttonMapByPos = new HashMap<>();

        for (QLCButtonGroup group: buttonGroups)
            for (QLCButton button: group.getButtonList())
                buttonMapByPos.put(button.getMapKey(), button);

        buttonMapGroupId = new HashMap<>();

        for (int matrixX=0; matrixX<8; matrixX++)
            for (int matrixY=0; matrixY<8; matrixY++){
                if (!buttonMapByPos.containsKey(matrixX + "-" + matrixY)) {
                    final QLCButton button = new QLCButton(matrixX, matrixY, null);
                    buttonMapByPos.put(matrixX + "-" + matrixY, button);
                }

                add(buttonMapByPos.get(matrixX + "-" + matrixY).getButton());
            }
    }

    public void activePanel(){
        for (int matrixX=0; matrixX<8; matrixX++)
            for (int matrixY=0; matrixY<8; matrixY++){
                final QLCButton button = buttonMapByPos.get(matrixX + "-" + matrixY);
                button.refresh();
            }
    }

    public void toggleButton(int x, int y){
        buttonMapByPos.get(x + "-" + y).toggle();
    }

    @Override
    public String getName() {
        return "Panel "+index;
    }

    protected JTextField buildTxt(final int posY){
        final JTextField txt = new JTextField();
        txt.setBounds(EFXMConfigureMainPanel.WIDTH1+ 20, posY, 120, 20);
        txt.addActionListener(this);
        add(txt);
        return txt;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

    }

}