package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.gui.movements.EFXMConfigureMainPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControllerEditPanel extends JPanel implements ActionListener, ButtonSelectedListener {

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

    private final int index;
    private final Map<String, QLCButton> buttonMapByPos;
    private final Map<QLCButton, QLCButtonGroup> buttonMapGroupId;

    public ControllerEditPanel(final int index) {
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
        buttonMapGroupId = new HashMap<>();

        for (QLCButtonGroup group: buttonGroups)
            for (QLCButton button: group.getButtonList()) {
                buttonMapByPos.put(button.getMapKey(), button);
                buttonMapGroupId.put(button, group);
            }

        for (int matrixX=0; matrixX<8; matrixX++)
            for (int matrixY=0; matrixY<8; matrixY++){
                if (!buttonMapByPos.containsKey(matrixX + "-" + matrixY)) {
                    final QLCButton button2 = new QLCButton(matrixX, matrixY, null);
                    buttonMapByPos.put(matrixX + "-" + matrixY, button2);
                }

                QLCButton qlcButton = buttonMapByPos.get(matrixX + "-" + matrixY);
                add(qlcButton.getButton());
            }

     //   this.setOpaque(true);
      //  this.setBackground(Color.black);

    }

    public void activePanel(){
        System.out.println("Activating Panel: "+this.index);
        for (int matrixX=0; matrixX<8; matrixX++)
            for (int matrixY=0; matrixY<8; matrixY++){
                final QLCButton button = buttonMapByPos.get(matrixX + "-" + matrixY);
                button.setButtonSelectedListener(null);
                button.refresh();
                button.setButtonSelectedListener(this);
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

    @Override
    public void selected(final QLCButton qlcButton) {
        final QLCButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
        if (buttonGroup==null)
            return ;

        buttonGroup.addFinalOffReview();
        for (QLCButton qlcButtonIdx: buttonGroup.getButtonList()) {
            if (!qlcButtonIdx.equals(qlcButton) && qlcButtonIdx.getShow() != null && qlcButtonIdx.getShow().isExecuting()) {
                qlcButtonIdx.getButton().setSelected(false);
            }
        }
        buttonGroup.minusFinalOffReview();

    }

    @Override
    public void unSelected(final QLCButton qlcButton) {
        selected(qlcButton);

    }

    @Override
    public void onFinishChange(final QLCButton qlcButton) {
        final QLCButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
        if (buttonGroup==null || !buttonGroup.isFinalOffReview())
            return ;

        for (QLCButton qlcButtonIdx: buttonGroup.getButtonList())
            if (qlcButtonIdx.getShow() != null && qlcButtonIdx.getShow().isExecuting())
                return;

        if (buttonGroup.getGlobalOff()!=null)
            buttonGroup.getGlobalOff().getShow().setExecuteOneTime(true);
    }
}