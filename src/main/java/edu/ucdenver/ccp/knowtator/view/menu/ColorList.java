package edu.ucdenver.ccp.knowtator.view.menu;

import edu.ucdenver.ccp.knowtator.model.collection.*;
import edu.ucdenver.ccp.knowtator.model.profile.ColorListener;
import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.view.KnowtatorActions;
import edu.ucdenver.ccp.knowtator.view.KnowtatorComponent;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class ColorList extends JList<Object> implements KnowtatorCollectionListener<Profile>, KnowtatorComponent, ColorListener {

    private KnowtatorView view;
    private ListSelectionListener lsl;

    ColorList(KnowtatorView view) {
        this.view = view;
        setModel(new DefaultListModel<>());

        setCellRenderer(new ColorListRenderer<>());
        lsl = e -> KnowtatorActions.assignColorToClass(view, getSelectedValue());
        view.getController().getProfileCollection().addCollectionListener(this);
        view.getController().getProfileCollection().addColorListener(this);
        setCollection(view.getController().getProfileCollection().getSelection());
    }

    void setCollection(Profile profile) {
        dispose();
        profile.getColors().forEach((object, color) -> ((DefaultListModel<Object>) getModel()).addElement(object));
        reset();
    }

    @Override
    public void selected(SelectionChangeEvent<Profile> event) {
        setCollection(view.getController().getProfileCollection().getSelection());
    }

    @Override
    public void added(AddEvent<Profile> event) {
        setCollection(view.getController().getProfileCollection().getSelection());
    }

    @Override
    public void removed(RemoveEvent<Profile> event) {
        setCollection(view.getController().getProfileCollection().getSelection());
    }

    @Override
    public void changed(ChangeEvent<Profile> event) {

    }

    @Override
    public void emptied() {

    }

    @Override
    public void firstAdded() {

    }

    @Override
    public void reset() {
        addListSelectionListener(lsl);
    }

    @Override
    public void dispose() {
        removeListSelectionListener(lsl);
        setModel(new DefaultListModel<>());
    }

    @Override
    public void colorChanged() {
        setCollection(view.getController().getProfileCollection().getSelection());
    }

    class ColorListRenderer<o> extends JLabel implements ListCellRenderer<o> {

        ColorListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setBackground(view.getController().getProfileCollection().getSelection().getColors().get(value));
            if (value instanceof OWLEntity) {
                setText(view.getController().getOWLModel().getOWLEntityRendering((OWLEntity) value));
            } else {
                setText(value.toString());
            }
            return this;
        }
    }
}
