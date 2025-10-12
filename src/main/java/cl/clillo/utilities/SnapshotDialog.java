package cl.clillo.utilities;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SnapshotDialog {
    static class Result {
        SnapshotMeta meta;
        int fixtureId;
        boolean fixtureRobotic;
        int startChannel;
        int dimmerIndex;
        File file;
    }

    static Result show(Component parent, int modelSize) {
        JTextField title = new JTextField("QLCScene.Universe");
        JTextField id = new JTextField("1");
        JTextField type = new JTextField("Scene");
        JTextField path = new JTextField("Universe");
        JTextField name = new JTextField("Snapshot_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        JSpinner fixture = new JSpinner(new SpinnerNumberModel(20, 1, 9999, 1));
        JCheckBox robotic = new JCheckBox("fixture-robotic", false);
        JSpinner startChan = new JSpinner(new SpinnerNumberModel(1, 1, 1024, 1));
        JSpinner dimmerIdx = new JSpinner(new SpinnerNumberModel(-1, -1, Math.max(0, modelSize-1), 1));

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        int r = 0;
        addRow(p, c, r++, "title", title);
        addRow(p, c, r++, "id", id);
        addRow(p, c, r++, "type", type);
        addRow(p, c, r++, "path", path);
        addRow(p, c, r++, "name", name);
        addRow(p, c, r++, "fixture", fixture);
        addRow(p, c, r++, "", robotic);
        addRow(p, c, r++, "start channel (1-based)", startChan);
        addRow(p, c, r++, "dimmer index (0-based, -1 none)", dimmerIdx);

        int ok = JOptionPane.showConfirmDialog(parent, p, "Snapshot XML", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar snapshot XML");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                File chosen = ensureXmlExtension(fc.getSelectedFile());

                if (chosen.exists()) {
                    int ow = JOptionPane.showConfirmDialog(parent,
                            "El archivo ya existe. ¿Deseas sobrescribirlo?\n" + chosen.getAbsolutePath(),
                            "Confirmar sobrescritura", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ow != JOptionPane.YES_OPTION) return null;
                }
                Result res = new Result();
                res.meta = new SnapshotMeta(title.getText(), id.getText(), type.getText(), path.getText(), name.getText());
                res.fixtureId = (Integer) fixture.getValue();
                res.fixtureRobotic = robotic.isSelected();
                res.startChannel = (Integer) startChan.getValue();
                res.dimmerIndex = (Integer) dimmerIdx.getValue();
                res.file = chosen;
                return res;
            }
        }
        return null;
    }

    private static void addRow(JPanel p, GridBagConstraints c, int row, String label, JComponent comp) {
        c.gridy = row; c.gridx = 0; c.weightx = 0;
        if (label != null && !label.isBlank()) {
            p.add(new JLabel(label + ":"), c);
            c.gridx = 1; c.weightx = 1; p.add(comp, c);
        } else { c.gridwidth = 2; p.add(comp, c); c.gridwidth = 1; }
    }

    private static File ensureXmlExtension(File f) {
        String name = f.getName();
        if (!name.toLowerCase().endsWith(".xml")) {
            return new File(f.getParentFile(), name + ".xml");
        }
        return f;
    }
}
