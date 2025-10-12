package cl.clillo.utilities;

import cl.clillo.lighting.external.dmx.Dmx;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;

public final class MainFrame extends JFrame {

    private final DmxModel model;
    private final JSpinner channelSpinner;
    private final JLabel status;
    private final UISettings uiSettings = new UISettings();

    public MainFrame(int channels) {
        super("DMX Controller UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLocationByPlatform(true);

        this.model = new DmxModel(channels);
        String[] descs = {
                "pan", "pan fine", "tilt", "tilt fine", "titl/pan speed", "focus", "rotate", "dimmer", "strobe",
                "red.front", "green.front", "blue.front", "white.front", "color temperature", "kinetic diagram", "dynamic graph speed",
                "red.background", "green.background", "blue.background", "white.background", "reset",
                "red.bee.1", "green.bee.1", "blue.bee.1", "white.bee.1",
                "red.bee.2", "green.bee.2", "blue.bee.2", "white.bee.2",
                "red.bee.3", "green.bee.3", "blue.bee.3", "white.bee.3",
                "red.bee.4", "green.bee.4", "blue.bee.4", "white.bee.4",
                "red.bee.5", "green.bee.5", "blue.bee.5", "white.bee.5",
                "red.bee.6", "green.bee.6", "blue.bee.6", "white.bee.6",
                "red.bee.7", "green.bee.7", "blue.bee.7", "white.bee.7"
        };
        model.setDescriptions(descs);
        DmxPanel dmxPanel = new DmxPanel(model, uiSettings);


        this.status = new JLabel("Listo");
        this.status.setBorder(new EmptyBorder(2, 8, 2, 8));

        // Toolbar
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.add(new JButton(new AbstractAction("Reset") {
            @Override public void actionPerformed(ActionEvent e) { model.resetAll(); showStatus("Todos en 0"); }
        }));
        tb.add(new JButton(new AbstractAction("Random") {
            @Override public void actionPerformed(ActionEvent e) { model.randomizeAll(System.nanoTime()); showStatus("Valores aleatorios"); }
        }));
        tb.addSeparator();

        tb.add(new JButton(new AbstractAction("Copiar CSV") {
            @Override public void actionPerformed(ActionEvent e) { copyToClipboard(model.toCsv()); showStatus("Copiado CSV al portapapeles"); }
        }));
        tb.add(new JButton(new AbstractAction("Copiar Espacios") {
            @Override public void actionPerformed(ActionEvent e) { copyToClipboard(model.toSpaceSeparated()); showStatus("Copiado (espacios)"); }
        }));
        tb.add(new JButton(new AbstractAction("Guardar CSV...") {
            @Override public void actionPerformed(ActionEvent e) { saveCsv(); }
        }));

        tb.add(new JButton(new AbstractAction("Snapshot XML...") {
            @Override public void actionPerformed(ActionEvent e) {
                var res = SnapshotDialog.show(MainFrame.this, model.size());
                if (res != null) {
                    try {
                        SnapshotXmlWriter.write(res.file, model, res.meta, res.fixtureId, res.fixtureRobotic, res.startChannel, res.dimmerIndex);
                        showStatus("Snapshot guardado: " + res.file.getName());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this, "Error guardando snapshot: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }));
        
        // Zoom y modo compacto
        tb.add(new JLabel(" Zoom: "));
        JSlider zoom = new JSlider(60, 200, 100); // 60%..200%
        zoom.setPreferredSize(new Dimension(120, 24));
        zoom.addChangeListener(e -> uiSettings.setScale(zoom.getValue() / 100.0));
        tb.add(zoom);

        JToggleButton compactBtn = new JToggleButton("Compacto");
        compactBtn.setSelected(true);
        compactBtn.addActionListener(e -> uiSettings.setCompact(compactBtn.isSelected()));
        tb.add(compactBtn);

        tb.addSeparator();
        tb.addSeparator();

        tb.add(new JLabel(" Canales: "));
        channelSpinner = new JSpinner(new SpinnerNumberModel(model.size(), 1, 512, 1));
        channelSpinner.addChangeListener(ev -> model.resize((Integer) channelSpinner.getValue()));
        tb.add(channelSpinner);

        // Layout
        JPanel content = new JPanel(new BorderLayout());
        content.add(tb, BorderLayout.NORTH);
        content.add(new JScrollPane(dmxPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        content.add(status, BorderLayout.SOUTH);
        setContentPane(content);

        setJMenuBar(buildMenuBar());

        final Dmx dmx = Dmx.getInstance();

        model.addListener((index, newValue) -> dmx.sendForce(1, index, newValue));

        JTabbedPane tabs = new JTabbedPane();
        CueListPanel cuePanel = new CueListPanel(model);
        GroupsPanel groupsPanel = new GroupsPanel(model);
        tabs.addTab("Cuelist", cuePanel);
        tabs.addTab("Groups", groupsPanel);
        this.getContentPane().add(tabs, BorderLayout.EAST);
        pack();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("Archivo");
        JMenu edit = new JMenu("Editar");
        JMenu help = new JMenu("Ayuda");

        JMenuItem save = new JMenuItem("Guardar CSV...");
        save.addActionListener(e -> saveCsv());
        JMenuItem exit = new JMenuItem("Salir");
        exit.addActionListener(e -> dispose());

        JMenuItem reset = new JMenuItem("Reset (0)");
        reset.addActionListener(e -> { model.resetAll(); showStatus("Todos en 0"); });
        JMenuItem random = new JMenuItem("Aleatorio");
        random.addActionListener(e -> { model.randomizeAll(System.nanoTime()); showStatus("Random aplicado"); });

        JMenuItem about = new JMenuItem("Acerca de...");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "DMX Controller UI\nCanales: 1..512\nDesarrollado para generar valores 0..255.",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));


        file.add(save);
        file.addSeparator();
        file.add(exit);

        edit.add(reset);
        edit.add(random);

        help.add(about);

        mb.add(file);
        mb.add(edit);
        mb.add(help);
        return mb;
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void saveCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar valores DMX como CSV");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write(model.toCsv());
                showStatus("Guardado: " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error guardando archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showStatus(String msg) { status.setText(msg); }
}
