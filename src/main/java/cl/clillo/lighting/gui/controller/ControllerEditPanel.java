package cl.clillo.lighting.gui.controller;

import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ControllerEditPanel extends JPanel implements ActionListener, ButtonSelectedListener {

    private final int index;
    private final Map<String, QLCButton> buttonMapByPos;
    private final Map<QLCButton, ButtonGroup> buttonMapGroupId;

    public ControllerEditPanel(final int index, final String fixtureGroupName) {
        setLayout(null);
        this.index = index;

        final PageConfig pageConfig = MidiButtonFunctionRepository.getInstance().getButtonGroupMap(fixtureGroupName);

        if (pageConfig==null)
            System.out.println(index+"\t"+fixtureGroupName);

        buttonMapByPos = new HashMap<>();
        buttonMapGroupId = new HashMap<>();

        for (ButtonGroup group: pageConfig.getButtonGroups())
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

        this.setOpaque(true);

        final JPanel panelSeq = new ControllerSeqPanel(fixtureGroupName);
        panelSeq.setBounds( 1420, 0,  220, 300);
        this.add(panelSeq);
    }

    public void activePanel(){
        log.debug("Activating Panel: "+this.index);

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

    @Override
    public void actionPerformed(final ActionEvent e) {

    }

    @Override
    public void selected(final QLCButton qlcButton) {
        final ButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
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
        final ButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
        if (buttonGroup==null || !buttonGroup.isFinalOffReview())
            return ;

        for (QLCButton qlcButtonIdx: buttonGroup.getButtonList())
            if (qlcButtonIdx.getShow() != null && qlcButtonIdx.getShow().isExecuting())
                return;

        if (buttonGroup.getGlobalOff()!=null)
            buttonGroup.getGlobalOff().getShow().setExecuteOneTime(true);
    }
}