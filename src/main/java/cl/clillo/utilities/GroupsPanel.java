package cl.clillo.utilities;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroupsPanel extends JPanel {

    private final DmxModel model;
    private final DefaultListModel<FixtureGroup> listModel = new DefaultListModel<>();
    private final JList<FixtureGroup> groupList = new JList<>(listModel);

    private final JTextField groupName = new JTextField("Grupo 1");
    private final JTextField channelsCsv = new JTextField("1,2,3,4");
    private final JSlider submaster = new JSlider(0, 100, 100);

    public GroupsPanel(DmxModel model) {
        super(new BorderLayout());
        this.model = model;

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Nombre grupo:"));
        form.add(groupName);
        form.add(new JLabel("Canales (CSV):"));
        form.add(channelsCsv);

        JButton addBtn = new JButton("Agregar/Actualizar");
        JButton delBtn = new JButton("Eliminar");
        JPanel btns = new JPanel();
        JButton loadBtn = new JButton("Load XML");
        JButton saveBtn = new JButton("Save XML");
        btns.add(addBtn);
        btns.add(delBtn);
        btns.add(loadBtn);
        btns.add(saveBtn);

        JPanel left = new JPanel(new BorderLayout());
        left.add(form, BorderLayout.NORTH);
        left.add(btns, BorderLayout.SOUTH);

        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, new JScrollPane(groupList)), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JLabel("Submaster (%)"), BorderLayout.WEST);
        bottom.add(submaster, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(this::addOrUpdate);
        delBtn.addActionListener(e -> deleteSelected());
        groupList.addListSelectionListener(e -> onSelect());

        loadBtn.addActionListener(e -> loadXml());
        saveBtn.addActionListener(e -> saveXml());
        submaster.addChangeListener(e -> updateSubmaster());
    }
    private void saveXml() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                new FixtureGroupsXmlIO().write(f, modelGetGroups());
                JOptionPane.showMessageDialog(this, "Guardado: " + f.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadXml() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                java.util.List<FixtureGroup> loaded = new FixtureGroupsXmlIO().read(f);
                listModel.clear();
                for (FixtureGroup g : loaded) {
                    listModel.addElement(g);
                }
                modelSetGroups(loaded);
                JOptionPane.showMessageDialog(this, "Cargado: " + f.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.List<FixtureGroup> modelGetGroups() {
        try {
            java.lang.reflect.Method m = model.getClass().getMethod("getGroups");
            return (java.util.List<FixtureGroup>) m.invoke(model);
        } catch (Exception ignored) {}
        // si el modelo no implementa getGroups, tomamos los de la UI
        java.util.List<FixtureGroup> fallback = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) fallback.add(listModel.get(i));
        return fallback;
    }

    private void modelSetGroups(java.util.List<FixtureGroup> groups) {
        try {
            // si el modelo expone setGroups(List)
            java.lang.reflect.Method m = model.getClass().getMethod("setGroups", java.util.List.class);
            m.invoke(model, groups);
        } catch (Exception ex) {
            // si no, limpiamos y volvemos a agregar con addGroup/removeGroup
            try {
                java.lang.reflect.Method getM = model.getClass().getMethod("getGroups");
                java.util.List<FixtureGroup> curr = (java.util.List<FixtureGroup>) getM.invoke(model);
                java.util.ArrayList<FixtureGroup> copy = new java.util.ArrayList<>(curr);
                for (FixtureGroup g : copy) {
                    java.lang.reflect.Method rem = model.getClass().getMethod("removeGroup", FixtureGroup.class);
                    rem.invoke(model, g);
                }
                for (FixtureGroup g : groups) {
                    java.lang.reflect.Method add = model.getClass().getMethod("addGroup", FixtureGroup.class);
                    add.invoke(model, g);
                }
            } catch (Exception ignored) {}
        }
        model.flushIfNeeded();
    }

    private void addOrUpdate(ActionEvent e) {
        String name = groupName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre requerido");
            return;
        }
        FixtureGroup g = findByName(name);
        if (g == null) {
            g = new FixtureGroup(name);
            listModel.addElement(g);
            modelAddGroup(g);
        }
        g.addChannels(parseChannels(channelsCsv.getText()));
        groupList.repaint();
    }

    private void deleteSelected() {
        int idx = groupList.getSelectedIndex();
        if (idx >= 0) {
            FixtureGroup g = listModel.remove(idx);
            modelRemoveGroup(g);
        }
    }

    private void onSelect() {
        FixtureGroup g = groupList.getSelectedValue();
        if (g != null) {
            groupName.setText(g.name());
            channelsCsv.setText(joinChannels(g));
            submaster.setValue((int) Math.round(g.submaster() * 100.0));
        }
    }

    private void updateSubmaster() {
        FixtureGroup g = groupList.getSelectedValue();
        if (g != null) {
            g.setSubmaster(submaster.getValue() / 100.0);
            model.flushIfNeeded();
            groupList.repaint();
        }
    }

    private FixtureGroup findByName(String name) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).name().equals(name)) return listModel.get(i);
        }
        return null;
    }

    private List<Integer> parseChannels(String csv) {
        List<Integer> out = new ArrayList<>();
        for (String s : csv.split(",")) {
            s = s.trim();
            if (s.isEmpty()) continue;
            try { out.add(Integer.parseInt(s)); } catch (NumberFormatException ignored) {}
        }
        return out;
    }

    private String joinChannels(FixtureGroup g) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Integer ch : g.channels()) {
            if (!first) sb.append(",");
            sb.append(ch);
            first = false;
        }
        return sb.toString();
    }

    // Métodos puente al modelo (para no romper si aún no implementas grupos en DmxModel)
    private void modelAddGroup(FixtureGroup g) {
        try {
            java.lang.reflect.Method m = model.getClass().getMethod("addGroup", FixtureGroup.class);
            m.invoke(model, g);
        } catch (Exception ignored) {}
    }

    private void modelRemoveGroup(FixtureGroup g) {
        try {
            java.lang.reflect.Method m = model.getClass().getMethod("removeGroup", FixtureGroup.class);
            m.invoke(model, g);
        } catch (Exception ignored) {}
    }
}