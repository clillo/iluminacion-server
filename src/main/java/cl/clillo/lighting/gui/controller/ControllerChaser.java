package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.executor.ChaserExecutor;
import cl.clillo.lighting.executor.ChaserExecutorShowListener;
import cl.clillo.lighting.model.QLCChaser;
import cl.clillo.lighting.model.QLCChaserStep;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.util.Vector;

public class ControllerChaser extends JPanel implements ChangeListener, ListSelectionListener, ChaserExecutorShowListener {

    private final JList<ShowListWrapper> lstChasers;
    private final JList<ChaserListWrapper> lstCollection;
    private Vector<ChaserListWrapper> chaserListWrappers;
    private final RunOrderTypePicker runOrderTypePicker = new RunOrderTypePicker();

    public ControllerChaser() {
        setLayout(null);
        this.setOpaque(true);
        this.setBackground(Color.red);

        final Vector<ShowListWrapper> collectionList = new Vector<>();
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.getFunction() instanceof QLCChaser) {
                collectionList.add(new ShowListWrapper(show));
            }

        lstChasers = buildList(collectionList, 10, 10, 250, 330);
        lstCollection = buildList(new Vector<>(), 280, 10, 250, 330);
        lstCollection.removeListSelectionListener(this);

        final JToggleButton btnRunningShows = new JToggleButton("Run");
        btnRunningShows.setBounds(555, 10, 120, 20);

        this.add(btnRunningShows);
        btnRunningShows.addActionListener(e ->run(btnRunningShows.isSelected()));

        runOrderTypePicker.setBounds(550,60,140,180);
        this.add(runOrderTypePicker);
    }

    private <T> JList<T> buildList(final Vector<T> collectionList, int x, int y, int width, int height){
        final JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setBounds(x, y, width, height);
        final JList<T> list = new JList<>();
        list.setListData(collectionList);

        jScrollPane.setViewportView(list);
        this.add(jScrollPane);
        list.addListSelectionListener(this);
        return list;
    }


    @Override
    public void stateChanged(ChangeEvent e) {

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getSource().equals(lstChasers) || e.getValueIsAdjusting()) {
            chaserListWrappers = null;
            return;
        }

        final ShowListWrapper showSelected = lstChasers.getSelectedValue();

        chaserListWrappers = new Vector<>();
        final QLCChaser chaser = showSelected.getShow().getFunction();

        for (QLCChaserStep show: chaser.getChaserSteps())
            chaserListWrappers.add(new ChaserListWrapper(show));

        runOrderTypePicker.selectShow(showSelected.getShow().getFunction());

        lstCollection.setListData(chaserListWrappers);
    }

    private void run(boolean selected){
        final ShowListWrapper showSelected = lstChasers.getSelectedValue();
        if (showSelected==null)
            return;
        showSelected.getShow().setExecuting(selected);
        ((ChaserExecutor)showSelected.getShow().getStepExecutor()).setChaserExecutorShowListener(this);
        if (!selected) {
            showSelected.getShow().getStepExecutor().stop();
            ((ChaserExecutor)showSelected.getShow().getStepExecutor()).setChaserExecutorShowListener(null);
        }
    }

    private ChaserListWrapper getChaserListWrapper(Show show){
        if (chaserListWrappers==null)
            return null;

        for(ChaserListWrapper chaser: chaserListWrappers)
            if(chaser.getChaserStep().getShow().getId()==show.getId())
                return chaser;

        return null;
    }

    @Override
    public void startExecuting(Show show) {
        final ChaserListWrapper chaser = getChaserListWrapper(show);
        if (chaser==null)
            return;

        lstCollection.setSelectedValue(chaser, true);
    }

    @Override
    public void stopExecuting(Show show) {
        final ChaserListWrapper chaser = getChaserListWrapper(show);
        if (chaser==null)
            return;

        lstCollection.clearSelection();
    }
}