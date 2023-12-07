package cl.clillo.lighting.gui.controller;

import cl.clillo.lighting.model.QLCCollection;
import cl.clillo.lighting.model.QLCFunction;
import cl.clillo.lighting.model.QLCScene;
import cl.clillo.lighting.model.Show;
import cl.clillo.lighting.model.ShowCollection;
import cl.clillo.lighting.utils.FileUtils;
import lombok.extern.log4j.Log4j2;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Log4j2
public class ControllerCollections extends JPanel implements ActionListener, ChangeListener, ListSelectionListener {

    private JList<Show> lstCollection;
    private JList<QLCFunction> lstScenes;

    public ControllerCollections() {
        setLayout(null);

        Vector<Show> collectionList = new Vector<>();
        for (Show show: ShowCollection.getInstance().getShowList())
            if (show.getFunction() instanceof QLCCollection) {
                collectionList.add(show);
            }

        lstCollection = buildList(collectionList, 10, 10, 280, 350);
        lstScenes = buildList(new Vector<>(), 300, 10, 280, 350);
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
    public void actionPerformed(ActionEvent e) {

    }


    @Override
    public void stateChanged(ChangeEvent e) {

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getSource().equals(lstCollection) || e.getValueIsAdjusting())
            return;
        Show showSelected = lstCollection.getSelectedValue();

        Vector<QLCFunction> functionList = new Vector<>(((QLCCollection) showSelected.getFunction()).getQlcFunctionList());

        lstScenes.setListData(functionList);
    }
}