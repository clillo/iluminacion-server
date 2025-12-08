package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Frame simple para disparar efectos BeeEye.
 * Muestra un botón por cada {@link Show} cuya {@link QLCFunction#getPath()} contenga "Bee Eye".
 */
public class BeeEyeEffectsFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    public BeeEyeEffectsFrame() {
        super("BeeEye Effects");
        initLookAndFeel();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowHandler());
        buildUI();
        pack();
        centerOnScreen();
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore, usar L&F por defecto
        }
    }

    private final Map<Show, JButton> buttonByShow = new LinkedHashMap<>();
    private static final String DIMMER_PATH = "Moving Head Bee Eye Dimmer";

    private void buildUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Panel principal vertical
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.BLACK);

        // Botón de blackout global (solo BeeEye UI, pero usa lógica global)
        JButton blackoutButton = new JButton("BLACKOUT");
        blackoutButton.setFocusPainted(false);
        blackoutButton.setContentAreaFilled(true);
        blackoutButton.setOpaque(true);
        blackoutButton.setBackground(new Color(80, 0, 0));
        blackoutButton.setForeground(Color.WHITE);
        blackoutButton.setBorder(javax.swing.BorderFactory.createLineBorder(Color.RED, 2));
        blackoutButton.setPreferredSize(new Dimension(260, 80));
        blackoutButton.setFont(blackoutButton.getFont().deriveFont(18f));
        blackoutButton.addActionListener(e -> {
            // Apagar solo los efectos BeeEye (por path)
            ShowCollection collection = ShowCollection.getInstance();
            for (Map.Entry<Show, JButton> entry : buttonByShow.entrySet()) {
                Show s = entry.getKey();
                QLCFunction f = s.getFunction();
                String path = f != null ? f.getPath() : null;
                if (path != null && path.contains("Bee Eye")) {
                    s.setExecuting(false);
                    updateButtonState(entry.getValue(), s);
                }
            }

            // Opcional: también asegurarse de que no queden otras escenas BeeEye activas
            for (Show s : collection.getShowList()) {
                QLCFunction f = s.getFunction();
                String path = f != null ? f.getPath() : null;
                if (path != null && path.contains("Bee Eye")) {
                    s.setExecuting(false);
                }
            }
        });

        JPanel blackoutPanel = new JPanel();
        blackoutPanel.setBackground(Color.BLACK);
        blackoutPanel.add(blackoutButton);
        mainPanel.add(blackoutPanel);

        // Agrupar shows por path
        Map<String, List<Show>> showsByPath = new LinkedHashMap<>();
        List<Show> beeEyeShows = getBeeEyeShows();
        for (Show show : beeEyeShows) {
            String path = show.getFunction() != null ? show.getFunction().getPath() : "Unknown";
            if (path == null || path.isBlank()) {
                path = "Unknown";
            }
            showsByPath.computeIfAbsent(path, k -> new ArrayList<>()).add(show);
        }

        // Ordenar paths: primero el dimmer, luego el resto en orden de inserción
        List<String> orderedPaths = new ArrayList<>();
        if (showsByPath.containsKey(DIMMER_PATH)) {
            orderedPaths.add(DIMMER_PATH);
        }
        for (String path : showsByPath.keySet()) {
            if (!path.equals(DIMMER_PATH)) {
                orderedPaths.add(path);
            }
        }

        for (String path : orderedPaths) {
            List<Show> shows = showsByPath.get(path);
            if (shows == null || shows.isEmpty()) continue;

            JPanel pathPanel = new JPanel(new GridLayout(0, 3, 16, 16));
            pathPanel.setBackground(Color.BLACK);
            pathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                    javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY),
                    path,
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    pathPanel.getFont().deriveFont(14f),
                    Color.LIGHT_GRAY
            ));

            for (Show show : shows) {
                JButton button = createShowButton(show);
                pathPanel.add(button);
                buttonByShow.put(show, button);
            }

            mainPanel.add(pathPanel);
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private List<Show> getBeeEyeShows() {
        List<Show> result = new ArrayList<>();
        ShowCollection collection = ShowCollection.getInstance();
        for (Show show : collection.getShowList()) {
            QLCFunction function = show.getFunction();
            if (function == null) {
                continue;
            }
            String path = function.getPath();
            String name = function.getName();
            // Heurística: todos los efectos cuya ruta incluya "Bee Eye"
            if ((path != null && path.contains("Bee Eye")) ||
                    (name != null && name.contains("BeeEye"))) {
                result.add(show);
            }
        }
        result.sort(Comparator.comparingInt(Show::getId));
        return result;
    }

    private JButton createShowButton(final Show show) {
        String label = show.getId() + " - " + show.getName();
        final JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(Color.BLACK);
        button.setForeground(Color.LIGHT_GRAY);
        button.setBorder(javax.swing.BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        button.setPreferredSize(new Dimension(220, 80));
        button.setFont(button.getFont().deriveFont(16f));

        updateButtonState(button, show);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Asegurar que solo un efecto por path esté activo
                QLCFunction function = show.getFunction();
                String path = function != null ? function.getPath() : null;

                if (path != null) {
                    for (Map.Entry<Show, JButton> entry : buttonByShow.entrySet()) {
                        Show otherShow = entry.getKey();
                        if (otherShow == show) {
                            continue;
                        }
                        QLCFunction otherFunction = otherShow.getFunction();
                        if (otherFunction != null && path.equals(otherFunction.getPath())) {
                            otherShow.setExecuting(false);
                            updateButtonState(entry.getValue(), otherShow);
                        }
                    }
                }

                ShowCollection.getInstance().toggleShow(show);

                // Actualizar estados de todos los botones del mismo path
                for (Map.Entry<Show, JButton> entry : buttonByShow.entrySet()) {
                    Show s = entry.getKey();
                    QLCFunction f = s.getFunction();
                    if (f != null && path != null && path.equals(f.getPath())) {
                        updateButtonState(entry.getValue(), s);
                    }
                }
            }
        });

        return button;
    }

    private void updateButtonState(JButton button, Show show) {
        if (show.isExecuting()) {
            button.setBackground(new Color(0, 180, 0));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.BLACK);
            button.setForeground(Color.LIGHT_GRAY);
        }
    }

    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }

    public void start() {
        setVisible(true);
    }

    private class WindowHandler implements WindowListener {
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            // Solo cerrar esta ventana, no la aplicación completa
            dispose();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BeeEyeEffectsFrame frame = new BeeEyeEffectsFrame();
            frame.start();
        });
    }
}


