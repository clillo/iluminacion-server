package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.executor.OS2LStepExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCSequence;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ControllerEditPanel extends JPanel implements ActionListener, ButtonSelectedListener, ChangeDirectionRunOrderListener {

    private final int index;
    private final Map<String, QLCButton> buttonMapByPos;
    private final Map<QLCButton, ButtonGroup> buttonMapGroupId;
    private QLCSequence sequenceSelected;
    private ControllerSeqPanel panelSeq;

    public ControllerEditPanel(final int index, final String fixtureGroupName) {
        setLayout(null);
        this.index = index;

        final PageConfig pageConfig = MidiButtonFunctionRepository.getInstance().getButtonGroupMap(fixtureGroupName);

        if (pageConfig==null)
            System.out.println(index+"\t"+fixtureGroupName);

        buttonMapByPos = new HashMap<>();
        buttonMapGroupId = new HashMap<>();

        assert pageConfig != null;
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

        panelSeq = new ControllerSeqPanel(fixtureGroupName);
        panelSeq.setBounds( 1420, 0,  220, 800);
        this.add(panelSeq);

        sequenceSelected = null;
        panelSeq.setChangeDirectionRunOrderListener(this);
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
        internalSelected(qlcButton,true);

    }

    private void internalSelected(final QLCButton qlcButton, boolean selected){

        final ButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
        if (buttonGroup==null)
            return ;

        if (selected){
            sequenceSelected = null;
            if (qlcButton.getShow().getFunction() instanceof QLCSequence) {
                sequenceSelected = qlcButton.getShow().getFunction();

            }
            panelSeq.setShowSelected(qlcButton.getShow());

        }

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
        internalSelected(qlcButton, false);

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

    @Override
    public void change(final QLCRunOrder runOrder, final QLCDirection direction, final IOS2LEventListener.Type type) {
        if (sequenceSelected == null)
            return;

        sequenceSelected.setDirection(direction);
        sequenceSelected.setRunOrder(runOrder);
        sequenceSelected.setVdjType(type);

        sequenceSelected.getShow().setExecuting(false);

        if (type!= IOS2LEventListener.Type.UNIVERSAL) {
            sequenceSelected.getShow().setVdjType(type);
            sequenceSelected.getShow().setStepExecutor(new OS2LStepExecutor(sequenceSelected.getShow()));
        }else
            sequenceSelected.getShow().setStepExecutor(new QLCSequenceExecutor(sequenceSelected.getShow()));

        sequenceSelected.getShow().setExecuting(true);

    }
}