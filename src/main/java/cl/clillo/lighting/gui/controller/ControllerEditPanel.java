package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.IOS2LEventListener;
import cl.clillo.lighting.executor.OS2LStepExecutor;
import cl.clillo.lighting.executor.QLCSequenceExecutor;
import cl.clillo.lighting.external.midi.MidiShiftBank;
import cl.clillo.lighting.model.QLCDirection;
import cl.clillo.lighting.model.QLCRunOrder;
import cl.clillo.lighting.model.QLCSequence;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ControllerEditPanel extends JPanel implements ActionListener, ButtonSelectedListener, ChangeDirectionRunOrderListener {

    private final int index;
    private final Map<String, QLCButton> bankAByPos;
    private final Map<String, QLCButton> bankBByPos;
    private final Map<QLCButton, ButtonGroup> buttonMapGroupId;
    private int currentBank = MidiShiftBank.BANK_A;
    private QLCSequence sequenceSelected;
    private ControllerSeqPanel panelSeq;

    public ControllerEditPanel(final int index, final String fixtureGroupName) {
        setLayout(null);
        this.index = index;

        final PageConfig pageConfig = MidiButtonFunctionRepository.getInstance().getButtonGroupMap(fixtureGroupName);

        if (pageConfig == null)
            System.out.println(index + "\t" + fixtureGroupName);

        bankAByPos = new HashMap<>();
        bankBByPos = new HashMap<>();
        buttonMapGroupId = new HashMap<>();

        assert pageConfig != null;
        fillBank(pageConfig, MidiShiftBank.BANK_A, bankAByPos);
        fillBank(pageConfig, MidiShiftBank.BANK_B, bankBByPos);

        for (int matrixX = 0; matrixX < 8; matrixX++)
            for (int matrixY = 0; matrixY < 8; matrixY++) {
                final String key = matrixX + "-" + matrixY;
                ensurePlaceholder(bankAByPos, key, matrixX, matrixY);
                ensurePlaceholder(bankBByPos, key, matrixX, matrixY);

                final QLCButton bankA = bankAByPos.get(key);
                final QLCButton bankB = bankBByPos.get(key);
                add(bankA.getButton());
                add(bankB.getButton());
                bankB.getButton().setVisible(false);
            }

        this.setOpaque(true);

        panelSeq = new ControllerSeqPanel(fixtureGroupName);
        panelSeq.setBounds(1420, 0, 220, 800);
        this.add(panelSeq);

        sequenceSelected = null;
        panelSeq.setChangeDirectionRunOrderListener(this);
    }

    private void fillBank(final PageConfig pageConfig, final int bank, final Map<String, QLCButton> map) {
        for (ButtonGroup group : pageConfig.getButtonGroups(bank))
            for (QLCButton button : group.getButtonList()) {
                map.put(button.getMapKey(), button);
                buttonMapGroupId.put(button, group);
            }
    }

    private static void ensurePlaceholder(final Map<String, QLCButton> map, final String key,
                                          final int matrixX, final int matrixY) {
        if (!map.containsKey(key))
            map.put(key, new QLCButton(matrixX, matrixY, null));
    }

    private Map<String, QLCButton> activeMap() {
        return currentBank == MidiShiftBank.BANK_B ? bankBByPos : bankAByPos;
    }

    public void setBank(final int bank) {
        if (bank == currentBank)
            return;
        currentBank = bank == MidiShiftBank.BANK_B ? MidiShiftBank.BANK_B : MidiShiftBank.BANK_A;
        final boolean showB = currentBank == MidiShiftBank.BANK_B;
        for (int matrixX = 0; matrixX < 8; matrixX++)
            for (int matrixY = 0; matrixY < 8; matrixY++) {
                final String key = matrixX + "-" + matrixY;
                bankAByPos.get(key).getButton().setVisible(!showB);
                bankBByPos.get(key).getButton().setVisible(showB);
            }
        activePanelLightsOnly();
    }

    public void activePanel() {
        log.debug("Activating Panel: " + this.index);

        for (QLCButton button : bankAByPos.values())
            button.setButtonSelectedListener(null);
        for (QLCButton button : bankBByPos.values())
            button.setButtonSelectedListener(null);

        for (QLCButton button : activeMap().values()) {
            button.refresh();
            button.setButtonSelectedListener(this);
        }
    }

    /** Tras cambiar de bank: refresca LEDs MIDI del bank visible sin re-disparar shows. */
    private void activePanelLightsOnly() {
        for (QLCButton button : bankAByPos.values())
            button.setButtonSelectedListener(null);
        for (QLCButton button : bankBByPos.values())
            button.setButtonSelectedListener(null);

        for (QLCButton button : activeMap().values()) {
            button.setText();
            button.refreshMidiLight();
            button.setButtonSelectedListener(this);
        }
    }

    public void toggleButton(int x, int y) {
        activeMap().get(x + "-" + y).toggle();
    }

    @Override
    public String getName() {
        return "Panel " + index;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

    }

    @Override
    public void selected(final QLCButton qlcButton) {
        internalSelected(qlcButton, true);

    }

    private void internalSelected(final QLCButton qlcButton, boolean selected) {

        final ButtonGroup buttonGroup = buttonMapGroupId.get(qlcButton);
        if (buttonGroup == null)
            return;

        if (selected) {
            sequenceSelected = null;
            if (qlcButton.getShow().getFunction() instanceof QLCSequence) {
                sequenceSelected = qlcButton.getShow().getFunction();

            }
            panelSeq.setShowSelected(qlcButton.getShow());

        }

        buttonGroup.addFinalOffReview();
        for (QLCButton qlcButtonIdx : buttonGroup.getButtonList()) {
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
        if (buttonGroup == null || !buttonGroup.isFinalOffReview())
            return;

        for (QLCButton qlcButtonIdx : buttonGroup.getButtonList())
            if (qlcButtonIdx.getShow() != null && qlcButtonIdx.getShow().isExecuting())
                return;

        if (buttonGroup.getGlobalOff() != null)
            buttonGroup.getGlobalOff().getShow().setExecuteOneTime(true);
    }

    @Override
    public void change(final QLCRunOrder runOrder, final QLCDirection direction, final IOS2LEventListener.Type type, int speed) {
        if (sequenceSelected == null)
            return;

        sequenceSelected.setDirection(direction);
        sequenceSelected.setRunOrder(runOrder);
        sequenceSelected.setVdjType(type);

        sequenceSelected.getShow().setExecuting(false);

        if (type != IOS2LEventListener.Type.UNIVERSAL) {
            sequenceSelected.getShow().setVdjType(type);
            sequenceSelected.getShow().setStepExecutor(new OS2LStepExecutor(sequenceSelected.getShow()));
        } else
            sequenceSelected.getShow().setStepExecutor(new QLCSequenceExecutor(sequenceSelected.getShow()));

        sequenceSelected.getShow().setExecuting(true);

    }

    public ControllerSeqPanel getPanelSeq() {
        return panelSeq;
    }
}
