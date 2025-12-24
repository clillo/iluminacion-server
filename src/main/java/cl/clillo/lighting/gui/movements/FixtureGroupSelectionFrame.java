package cl.clillo.lighting.gui.movements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * Frame para seleccionar el grupo de fixtures antes de configurar.
 */
public class FixtureGroupSelectionFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum FixtureGroup {
        MOVING_HEAD_SPOT("Moving Head Spot", "Moving Head Spot"),
        MOVING_HEAD_HIBRID("Moving Head Hibrid", "Moving Head Beam + Spot"),
        LASER("Láser", "Laser"),
        DERBY("Derby", "Derby");

        private final String displayName;
        private final String pathFilter;

        FixtureGroup(String displayName, String pathFilter) {
            this.displayName = displayName;
            this.pathFilter = pathFilter;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getPathFilter() {
            return pathFilter;
        }
    }

    private FixtureGroup selectedGroup = null;

    public FixtureGroupSelectionFrame() {
        setTitle("Seleccionar Grupo de Fixtures");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titleLabel = new JLabel("Seleccione el grupo de fixtures a configurar");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Botones para cada grupo
        for (FixtureGroup group : FixtureGroup.values()) {
            JButton button = createGroupButton(group);
            gbc.gridx = (group.ordinal() % 2);
            if (group.ordinal() >= 2) {
                gbc.gridy = 2;
            }
            mainPanel.add(button, gbc);
        }

        add(mainPanel, BorderLayout.CENTER);
        pack();
    }

    private JButton createGroupButton(FixtureGroup group) {
        JButton button = new JButton(group.getDisplayName());
        button.setPreferredSize(new Dimension(250, 100));
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(40, 40, 40));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 2));
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedGroup = group;
                dispose();
                openConfigurationFrame();
            }
        });

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 60, 60));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(40, 40, 40));
            }
        });

        return button;
    }

    private void openConfigurationFrame() {
        if (selectedGroup != null) {
            EFXMConfigureJFrame configFrame = new EFXMConfigureJFrame(selectedGroup);
            configFrame.start();
        }
    }

    public void start() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public FixtureGroup getSelectedGroup() {
        return selectedGroup;
    }
}

