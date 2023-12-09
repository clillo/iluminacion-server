package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCChaser;
import cl.clillo.lighting.model.QLCChaserStep;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.util.Vector;

public class ControllerChaser extends JPanel implements ChangeListener, ListSelectionListener {

    private final JList<ShowListWrapper> lstChasers;
    private final JList<ChaserListWrapper> lstCollection;

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

        final JButton btnRunningShows = new JButton("Capture");
        btnRunningShows.setBounds(670, 10, 120, 20);

        this.add(btnRunningShows);
        //btnRunningShows.addActionListener(e ->runningShows());

        final JButton btnAddToCollection = new JButton("Add");
        btnAddToCollection.setBounds(670, 40, 120, 20);

        this.add(btnAddToCollection);
        //btnAddToCollection.addActionListener(e ->addToCollection());
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
        if (!e.getSource().equals(lstChasers) || e.getValueIsAdjusting())
            return;

        ShowListWrapper showSelected = lstChasers.getSelectedValue();

        Vector<ChaserListWrapper> functionList = new Vector<>();
        for (QLCChaserStep show: ((QLCChaser) showSelected.getShow().getFunction()).getChaserSteps())
            functionList.add(new ChaserListWrapper(show));

        lstCollection.setListData(functionList);
    }
}