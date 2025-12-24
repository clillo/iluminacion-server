package cl.clillo.lighting.gui.movements;

import cl.clillo.lighting.executor.QLCEfxExecutor;
import cl.clillo.lighting.model.QLCEfxCircle;
import cl.clillo.lighting.model.QLCEfxLine;
import cl.clillo.lighting.model.QLCEfxMultiLine;
import cl.clillo.lighting.model.QLCEfxSpline;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class EFXMConfigureMainPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1424;
    public static final int HEIGHT1 = 900;

    private final List<EffectEditPanel> pnlList;
    private final FixtureGroupSelectionFrame.FixtureGroup fixtureGroup;

    public EFXMConfigureMainPanel() {
        this(null);
    }

    public EFXMConfigureMainPanel(FixtureGroupSelectionFrame.FixtureGroup fixtureGroup) {
        this.fixtureGroup = fixtureGroup;
        pnlList = new ArrayList<>();

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
       // tabbedPane.setLayout(null);
        add(tabbedPane);

        for (Show show : ShowCollection.getInstance().getShowList()) {
            // Filtrar por grupo si está especificado
            if (fixtureGroup != null && !matchesFixtureGroup(show, fixtureGroup)) {
                continue;
            }

            final EffectEditPanel editPanel = buildPanel(show);
            if (editPanel != null) {
                editPanel.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
                tabbedPane.addTab(editPanel.getQlcEf().getName(), editPanel);
                pnlList.add(editPanel);

                ((QLCEfxExecutor)show.getStepExecutor()).setRoboticNotifiable(editPanel);
            }
        }

        this.setBounds(0, 0, WIDTH1 + 200, HEIGHT1);
        this.setLayout(null);
    }

    private boolean matchesFixtureGroup(Show show, FixtureGroupSelectionFrame.FixtureGroup group) {
        QLCFunction function = show.getFunction();
        if (function == null) return false;
        
        String path = function.getPath();
        if (path == null) return false;
        
        String filter = group.getPathFilter();
        
        // Para Moving Head Hibrid, incluir "Moving Head Beam + Spot" y también "Moving Head Beam" solo
        if (group == FixtureGroupSelectionFrame.FixtureGroup.MOVING_HEAD_HIBRID) {
            return path.contains("Moving Head Beam + Spot") || 
                   (path.contains("Moving Head Beam") && !path.contains("Moving Head Spot"));
        }
        
        return path.contains(filter);
    }

    private EffectEditPanel buildPanel(final Show show) {
        final QLCFunction qlcEfx = show.getFunction();
        if (qlcEfx instanceof QLCEfxCircle)
            return new EffectCircleEditPanel((QLCEfxCircle) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxLine)
            return new EffectLineEditPanel((QLCEfxLine) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxMultiLine)
            return new EffectMultiLineEditPanel((QLCEfxMultiLine) qlcEfx, show);
        if (qlcEfx instanceof QLCEfxSpline)
            return new EffectSplineEditPanel((QLCEfxSpline) qlcEfx, show);

        return null;
    }
}