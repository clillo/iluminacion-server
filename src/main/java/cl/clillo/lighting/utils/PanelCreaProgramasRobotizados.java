package cl.clillo.lighting.utils;

public class PanelCreaProgramasRobotizados extends PanelMenuGenerico{

    private static final long serialVersionUID = -5869553409971473557L;

    public static final int WIDTH1 = 1024;
    public static final int HEIGHT1 = 900;

    private final PanelEdicionFiguras pnlMovingHead1 = new PanelEdicionFiguras();

    public PanelCreaProgramasRobotizados() {
        this.configura(WIDTH1+200, HEIGHT1, "Generación de Programas Robotizados");
        this.setLayout(null);

        pnlMovingHead1.setBounds(0, 0, WIDTH1+200, HEIGHT1);
        add(pnlMovingHead1);
    }

    public PanelEdicionFiguras getPnlMovingHead1() {
        return pnlMovingHead1;
    }
}