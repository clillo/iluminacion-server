package cl.clillo.utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Panel de control para crear/editar y reproducir una CueList.
 * Requiere una instancia de DmxModel y ChasePlayer (inyectados).
 */
public class CueListPanel extends JPanel {

    private final DmxModel model;
    private final ChasePlayer player;

    private final DefaultListModel<Cue> listModel = new DefaultListModel<>();
    private final JList<Cue> cueJList = new JList<>(listModel);
    private final JTextField nameField = new JTextField("Show 1");
    private final JCheckBox loopChk = new JCheckBox("Loop", true);
    private CueList cueList = new CueList("Show 1", true);

    private final JTextField cueName = new JTextField("Cue");
    private final JSpinner fadeMs = new JSpinner(new SpinnerNumberModel(500, 0, 600000, 50));
    private final JSpinner holdMs = new JSpinner(new SpinnerNumberModel(0, 0, 600000, 50));

    public CueListPanel(DmxModel model) {
        super(new BorderLayout());
        this.model = model;
        this.player = new ChasePlayer(model);
        this.player.setCueList(cueList);

        cueJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Top: Title + loop + file ops
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST;
        top.add(new JLabel("CueList name:"), gc);
        gc.gridx++;
        nameField.setColumns(18);
        top.add(nameField, gc);
        gc.gridx++;
        top.add(loopChk, gc);
        gc.gridx++;
        JButton loadBtn = new JButton("Load XML");
        JButton saveBtn = new JButton("Save XML");
        top.add(loadBtn, gc);
        gc.gridx++;
        top.add(saveBtn, gc);

        // Center: list + controls
        JPanel center = new JPanel(new BorderLayout());
        center.add(new JScrollPane(cueJList), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add (from current)");
        JButton delBtn = new JButton("Delete");
        JButton upBtn = new JButton("Up");
        JButton downBtn = new JButton("Down");
        JButton playBtn = new JButton("Play");
        JButton stopBtn = new JButton("Stop");
        JButton nextBtn = new JButton("Next");
        controls.add(addBtn);
        controls.add(delBtn);
        controls.add(upBtn);
        controls.add(downBtn);
        controls.add(playBtn);
        controls.add(stopBtn);
        controls.add(nextBtn);

        JPanel cueEditor = new JPanel(new GridLayout(1, 6, 6, 6));
        cueEditor.add(new JLabel("Cue name:"));
        cueEditor.add(cueName);
        cueEditor.add(new JLabel("Fade (ms):"));
        cueEditor.add(fadeMs);
        cueEditor.add(new JLabel("Hold (ms):"));
        cueEditor.add(holdMs);

        center.add(cueEditor, BorderLayout.NORTH);
        center.add(controls, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // Actions
        loopChk.addActionListener(e -> cueList.setLoop(loopChk.isSelected()));
        nameField.addActionListener(e -> renameCueList());
        saveBtn.addActionListener(this::saveXml);
        loadBtn.addActionListener(this::loadXml);
        addBtn.addActionListener(this::addCueFromCurrent);
        delBtn.addActionListener(e -> deleteSelected());
        upBtn.addActionListener(e -> moveSelected(-1));
        downBtn.addActionListener(e -> moveSelected(1));
        playBtn.addActionListener(e -> {
            if (!player.isRunning()) {
                player.setCueList(currentFromUI());
                player.start();
            }
        });
        stopBtn.addActionListener(e -> player.stop());
        nextBtn.addActionListener(e -> player.nextCue());
    }

    private void renameCueList() {
        cueList = currentFromUI();
    }

    private CueList currentFromUI() {
        CueList cl = new CueList(nameField.getText(), loopChk.isSelected());
        for (int i = 0; i < listModel.size(); i++) {
            cl.addCue(listModel.get(i));
        }
        return cl;
    }

    private void addCueFromCurrent(ActionEvent e) {
        String id = UUID.randomUUID().toString();
        String n = cueName.getText().isBlank() ? ("Cue " + (listModel.size() + 1)) : cueName.getText();
        int f = (Integer) fadeMs.getValue();
        int h = (Integer) holdMs.getValue();
        Map<Integer,Integer> snap = new HashMap<>(model.getCurrentUniverseSnapshot());
        Cue cue = new Cue(id, n, snap, f, h);
        listModel.addElement(cue);
    }

    private void deleteSelected() {
        int idx = cueJList.getSelectedIndex();
        if (idx >= 0) listModel.remove(idx);
    }

    private void moveSelected(int delta) {
        int idx = cueJList.getSelectedIndex();
        if (idx < 0) return;
        int newIdx = idx + delta;
        if (newIdx < 0 || newIdx >= listModel.size()) return;
        Cue c = listModel.get(idx);
        listModel.remove(idx);
        listModel.add(newIdx, c);
        cueJList.setSelectedIndex(newIdx);
    }

    private void saveXml(ActionEvent e) {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                new CueListXmlIO().write(f, currentFromUI());
                JOptionPane.showMessageDialog(this, "Guardado: " + f.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadXml(ActionEvent e) {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                CueList loaded = new CueListXmlIO().read(f);
                nameField.setText(loaded.name());
                loopChk.setSelected(loaded.loop());
                listModel.clear();
                for (Cue c : loaded.cues()) listModel.addElement(c);
                JOptionPane.showMessageDialog(this, "Cargado: " + f.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}