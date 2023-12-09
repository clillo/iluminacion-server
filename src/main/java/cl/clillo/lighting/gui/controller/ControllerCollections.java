package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import lombok.extern.log4j.Log4j2;

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
import static javax.swing.JOptionPane.showMessageDialog;

public class ControllerCollections extends JPanel implements ChangeListener, ListSelectionListener {

    private final JList<ShowListWrapper> lstCollection;
    private final JList<ShowListWrapper> lstScenes;
    private final JList<FunctionListWrapper> lstActualRunningShows;

    public ControllerCollections() {
        setLayout(null);
        this.setOpaque(true);
        this.setBackground(Color.blue);

        final Vector<ShowListWrapper> collectionList = new Vector<>();
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.getFunction() instanceof QLCCollection) {
                collectionList.add(new ShowListWrapper(show));
            }

        lstCollection = buildList(collectionList, 10, 10, 200, 330);
        lstScenes = buildList(new Vector<>(), 230, 10, 200, 330);
        lstActualRunningShows = buildList(new Vector<>(), 460, 10, 200, 330);

        final JButton btnRunningShows = new JButton("Capture");
        btnRunningShows.setBounds(670, 10, 120, 20);

        this.add(btnRunningShows);
        btnRunningShows.addActionListener(e ->runningShows());

        final JButton btnAddToCollection = new JButton("Add");
        btnAddToCollection.setBounds(670, 40, 120, 20);

        this.add(btnAddToCollection);
        btnAddToCollection.addActionListener(e ->addToCollection());
    }

    private void runningShows(){
        Vector<FunctionListWrapper> functionList = new Vector<>();
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.isExecuting())
                functionList.add(new FunctionListWrapper(show.getFunction()));

        lstActualRunningShows.setListData(functionList);
    }

    private void addToCollection(){
        ShowListWrapper showSelected = lstCollection.getSelectedValue();

        if (showSelected==null){
            showMessageDialog(null, "Select Collection First!");
            return;
        }

        final QLCCollection collection = showSelected.getShow().getFunction();

        final Vector<FunctionListWrapper> functionList = new Vector<>();
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.isExecuting()) {
                functionList.add(new FunctionListWrapper(show.getFunction()));
                collection.addShow(show);
            }

        lstActualRunningShows.setListData(functionList);
        collection.save();
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
        if (!e.getSource().equals(lstCollection) || e.getValueIsAdjusting())
            return;

        ShowListWrapper showSelected = lstCollection.getSelectedValue();

        Vector<ShowListWrapper> functionList = new Vector<>();
        for (Show show: ((QLCCollection) showSelected.getShow().getFunction()).getShowList())
            functionList.add(new ShowListWrapper(show));

        lstScenes.setListData(functionList);
    }
}