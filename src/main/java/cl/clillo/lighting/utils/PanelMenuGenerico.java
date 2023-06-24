package cl.clillo.lighting.utils;


import javax.swing.JPanel;

public abstract class PanelMenuGenerico extends JPanel {

    private static final long serialVersionUID = -1858113566909821587L;

    private boolean resizable = false;
    private boolean closeable = true;
    private boolean maximizable  = false;
    private boolean iconifiable = true;
    private String title = "Frame Title";
    private int width = 1280;
    private int height = 800;

    public void configura(int width, int height, String titulo){
        this.height = height;
        this.width = width;
        this.title = titulo;
        this.setBounds(0, 0, width,height);
        this.setLayout(null);
    }

    public boolean isCloseable() {
        return closeable;
    }
    public boolean isIconifiable() {
        return iconifiable;
    }
    public boolean isMaximizable() {
        return maximizable;
    }
    public boolean isResizable() {
        return resizable;
    }
    public String getTitle() {
        return title;
    }
    public void setCloseable(boolean closeable) {
        this.closeable = closeable;
    }
    public void setIconifiable(boolean iconifiable) {
        this.iconifiable = iconifiable;
    }
    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
    }
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }
}
