package edu.ucdenver.ccp.knowtator.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.mxgraph.swing.util.mxGraphTransferable;
import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.model.collection.*;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.concept.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.text.concept.span.Span;
import edu.ucdenver.ccp.knowtator.view.annotation.*;
import edu.ucdenver.ccp.knowtator.view.graph.GraphViewDialog;
import edu.ucdenver.ccp.knowtator.view.search.SearchActions;
import edu.ucdenver.ccp.knowtator.view.search.SearchTextField;
import edu.ucdenver.ccp.knowtator.view.textsource.TextSourceActions;
import edu.ucdenver.ccp.knowtator.view.textsource.TextSourceChooser;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class KnowtatorView extends AbstractOWLClassViewComponent implements DropTargetListener, KnowtatorComponent {

    private static final Logger log = Logger.getLogger(KnowtatorView.class);
    private final Preferences preferences = Preferences.userRoot().node("knowtator");
    private final KnowtatorController controller;
    private GraphViewDialog graphViewDialog;
    private JComponent panel1;
    private JButton showGraphViewerButton;
    private JButton removeAnnotationButton;
    private JButton growStartButton;
    private JButton shrinkEndButton;
    private JButton growEndButton;
    private JButton shrinkStartButton;
    private JButton addAnnotationButton;
    private JButton previousTextSourceButton;
    private JButton nextTextSourceButton;
    private JButton assignColorToClassButton;
    private TextSourceChooser textSourceChooser;
    private JButton findTextInOntologyButton;
    private JButton addTextSourceButton;
    private JButton removeTextSourceButton;
    private JButton menuButton;
    private JButton previousMatchButton;
    private SearchTextField searchTextField;
    private JButton nextMatchButton;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox onlyAnnotationsCheckBox;
    private JSlider fontSizeSlider;
    private JPanel searchPanel;
    private JCheckBox regexCheckBox;
    private KnowtatorTextPane knowtatorTextPane;
    private SpanList spanList;
    private GraphSpaceList graphSpaceList;
    private JButton nextSpanButton;
    private JButton previousSpanButton;
    private AnnotationIDLabel annotationIDLabel;
    private AnnotationAnnotatorLabel annotationAnnotatorLabel;
    private AnnotationClassLabel annotationClassLabel;

    private List<JComponent> textSourceButtons;
    private List<JButton> annotationButtons;
    private Map<JButton, ActionListener> spanSizeButtons;
    private Map<JButton, ActionListener> selectionSizeButtons;

    private KnowtatorCollectionListener<ConceptAnnotation> conceptAnnotationCollectionListener;
    private KnowtatorCollectionListener<Span> spanCollectionListener;
    private List<KnowtatorComponent> knowtatorComponents;


    public KnowtatorView() {
        controller = new KnowtatorController();

        $$$setupUI$$$();

        makeButtons();

        // This is necessary to force OSGI to load the mxGraphTransferable class to allow node dragging.
        // It is kind of a hacky fix, but it works for now.

        log.warn("Don't worry about the following exception. Just forcing loading of a class needed by mxGraph");
        try {
            mxGraphTransferable.dataFlavor =
                    new DataFlavor(
                            DataFlavor.javaJVMLocalObjectMimeType
                                    + "; class=com.mxgraph.swing.util.mxGraphTransferable",
                            null,
                            mxGraphTransferable.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        setUpOWL();
    }

    public Preferences getPreferences() {
        return preferences;
    }

    private void setUpOWL() {
        if (!controller.getOWLModel().isWorkSpaceSet()) {
            if (getOWLWorkspace() != null) {
                controller.getOWLModel().setOwlWorkSpace(getOWLWorkspace());
                controller.getOWLModel().addOWLModelManagerListener(annotationClassLabel);

            }
        }
    }

    public KnowtatorController getController() {
        return controller;
    }

    @Override
    public void initialiseClassView() {
    }

    private void createUIComponents() {
        DropTarget dt = new DropTarget(this, this);
        dt.setActive(true);

        panel1 = this;

        knowtatorComponents = new ArrayList<>();

        knowtatorTextPane = new KnowtatorTextPane(this);
        graphViewDialog = new GraphViewDialog(this);
        textSourceChooser = new TextSourceChooser(this);

        spanList = new SpanList(this);
        graphSpaceList = new GraphSpaceList(this);
        annotationAnnotatorLabel = new AnnotationAnnotatorLabel(this);
        annotationClassLabel = new AnnotationClassLabel(this);
        annotationIDLabel = new AnnotationIDLabel(this);

        searchTextField = new SearchTextField(this);

        knowtatorComponents.add(spanList);
        knowtatorComponents.add(graphSpaceList);
        knowtatorComponents.add(annotationAnnotatorLabel);
        knowtatorComponents.add(annotationClassLabel);
        knowtatorComponents.add(annotationIDLabel);
        knowtatorComponents.add(knowtatorTextPane);
        knowtatorComponents.add(graphViewDialog);
        knowtatorComponents.add(textSourceChooser);
        knowtatorComponents.add(searchTextField);


        KnowtatorView view = this;
        addAncestorListener(
                new AncestorListener() {
                    @Override
                    public void ancestorAdded(AncestorEvent event) {
                        Window ancestor = SwingUtilities.getWindowAncestor(view);
                        ancestor.addWindowFocusListener(
                                new WindowFocusListener() {
                                    @Override
                                    public void windowGainedFocus(WindowEvent e) {
                                        graphViewDialog.setAlwaysOnTop(true);
                                    }

                                    @Override
                                    public void windowLostFocus(WindowEvent e) {
                                        if (e.getOppositeWindow() != graphViewDialog) {
                                            graphViewDialog.setAlwaysOnTop(false);
                                            graphViewDialog.toBack();
                                        }
                                    }
                                });
                    }

                    @Override
                    public void ancestorRemoved(AncestorEvent event) {
                    }

                    @Override
                    public void ancestorMoved(AncestorEvent event) {
                    }
                });
    }

    private void makeButtons() {
        makeMenuButtons();
        makeTextSourceButtons();
        makeAnnotationButtons();
        makeSpanButtons();
        makeSearchButtons();

        // Disable
        disableTextSourceButtons();
    }

    private void makeMenuButtons() {
        menuButton.addActionListener(e -> KnowtatorActions.showMainMenuDialog(this));
        assignColorToClassButton.addActionListener(e -> KnowtatorActions.assignColorToClass(this, getController().getOWLModel().getSelectedOWLEntity()));
    }

    private void makeTextSourceButtons() {
        fontSizeSlider.setValue(knowtatorTextPane.getFont().getSize());
        fontSizeSlider.addChangeListener(e -> TextSourceActions.setFontSize(this, fontSizeSlider.getValue()));
        showGraphViewerButton.addActionListener(e -> KnowtatorActions.showGraphViewer(graphViewDialog));
        previousTextSourceButton.addActionListener(e -> TextSourceActions.selectPreviousTextSource(this));
        nextTextSourceButton.addActionListener(e -> TextSourceActions.selectNextTextSource(this));
        addTextSourceButton.addActionListener(e -> TextSourceActions.addTextSource(this));
        removeTextSourceButton.addActionListener(e -> TextSourceActions.removeTextSource(this));

        textSourceButtons = new ArrayList<>();
        textSourceButtons.add(fontSizeSlider);
        textSourceButtons.add(showGraphViewerButton);
        textSourceButtons.add(previousTextSourceButton);
        textSourceButtons.add(nextTextSourceButton);
        textSourceButtons.add(addTextSourceButton);
        textSourceButtons.add(removeTextSourceButton);
    }

    private void makeAnnotationButtons() {
        annotationButtons = new ArrayList<>();
        addAnnotationButton.addActionListener(e -> {
            try {
                AnnotationActions.addAnnotation(this, getController().getTextSourceCollection().getSelection());
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        removeAnnotationButton.addActionListener(e -> {
            try {
                AnnotationActions.removeAnnotation(this, getController().getTextSourceCollection().getSelection(), getController().getTextSourceCollection().getSelection().getConceptAnnotationCollection().getSelection());
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        nextSpanButton.addActionListener(e -> {
            try {
                SpanActions.selectNextSpan(getController().getTextSourceCollection().getSelection());
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        previousSpanButton.addActionListener(e -> {
            try {
                SpanActions.selectPreviousSpan(getController().getTextSourceCollection().getSelection());
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });

        annotationButtons.add(addAnnotationButton);
        annotationButtons.add(removeAnnotationButton);
        annotationButtons.add(nextSpanButton);
        annotationButtons.add(previousSpanButton);
    }

    private void makeSpanButtons() {

        spanSizeButtons = new HashMap<>();
        spanSizeButtons.put(shrinkEndButton, e -> {
            try {
                SpanActions.modifySpan(getController().getTextSourceCollection().getSelection().getConceptAnnotationCollection().getSelection().getSpanCollection().getSelection(),
                        SpanActions.END,
                        SpanActions.SHRINK);
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        spanSizeButtons.put(shrinkStartButton, e -> {
            try {
                SpanActions.modifySpan(getController().getTextSourceCollection().getSelection().getConceptAnnotationCollection().getSelection().getSpanCollection().getSelection(), SpanActions.START, SpanActions.SHRINK);
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        spanSizeButtons.put(growEndButton, e -> {
            try {
                SpanActions.modifySpan(getController().getTextSourceCollection().getSelection().getConceptAnnotationCollection().getSelection().getSpanCollection().getSelection(), SpanActions.END, SpanActions.GROW);
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });
        spanSizeButtons.put(growStartButton, e -> {
            try {
                SpanActions.modifySpan(getController().getTextSourceCollection().getSelection().getConceptAnnotationCollection().getSelection().getSpanCollection().getSelection(), SpanActions.START, SpanActions.GROW);
            } catch (NoSelectionException e1) {
                e1.printStackTrace();
            }
        });

        selectionSizeButtons = new HashMap<>();
        selectionSizeButtons.put(shrinkEndButton, e -> SpanActions.modifySelection(this, SpanActions.END, SpanActions.SHRINK));
        selectionSizeButtons.put(shrinkStartButton, e -> SpanActions.modifySelection(this, SpanActions.START, SpanActions.SHRINK));
        selectionSizeButtons.put(growEndButton, e -> SpanActions.modifySelection(this, SpanActions.END, SpanActions.GROW));
        selectionSizeButtons.put(growStartButton, e -> SpanActions.modifySelection(this, SpanActions.START, SpanActions.GROW));
    }

    private void makeSearchButtons() {
        findTextInOntologyButton.addActionListener(e -> SearchActions.findText(this, searchTextField.getText()));
        nextMatchButton.addActionListener(e -> SearchActions.findNextMatch(this, searchTextField.getText(), caseSensitiveCheckBox.isSelected(), onlyAnnotationsCheckBox.isSelected(), regexCheckBox.isSelected()));
        previousMatchButton.addActionListener(e -> SearchActions.findPreviousMatch(this, searchTextField.getText(), caseSensitiveCheckBox.isSelected(), onlyAnnotationsCheckBox.isSelected(), regexCheckBox.isSelected()));
    }

    private void setupListeners() {
        KnowtatorCollectionListener<TextSource> textSourceCollectionListener = new KnowtatorCollectionListener<TextSource>() {

            @Override
            public void selected(SelectionChangeEvent<TextSource> event) {
                if (event.getOld() != null) {
                    event.getOld().getConceptAnnotationCollection().removeCollectionListener(conceptAnnotationCollectionListener);
                }
                if (event.getNew() != null) {
                    event.getNew().getConceptAnnotationCollection().addCollectionListener(conceptAnnotationCollectionListener);
                }
            }

            @Override
            public void added(AddEvent<TextSource> event) {
            }

            @Override
            public void removed(RemoveEvent<TextSource> event) {
            }

            @Override
            public void changed(ChangeEvent<TextSource> event) {
            }

            @Override
            public void emptied() {
                disableTextSourceButtons();
            }

            @Override
            public void firstAdded() {
                enableTextSourceButtons();
                addAnnotationButton.setEnabled(true);
            }
        };
        conceptAnnotationCollectionListener = new KnowtatorCollectionListener<ConceptAnnotation>() {

            @Override
            public void selected(SelectionChangeEvent<ConceptAnnotation> event) {
                if (event.getOld() != null) {
                    event.getOld().getSpanCollection().removeCollectionListener(spanCollectionListener);
                }
                if (event.getNew() == null) {
                    removeAnnotationButton.setEnabled(false);
                } else {
                    event.getNew().getSpanCollection().addCollectionListener(spanCollectionListener);
                    enableAnnotationButtons();
                }
            }

            @Override
            public void added(AddEvent<ConceptAnnotation> event) {
            }

            @Override
            public void removed(RemoveEvent<ConceptAnnotation> event) {
            }

            @Override
            public void changed(ChangeEvent<ConceptAnnotation> event) {
            }

            @Override
            public void emptied() {
                disableAnnotationButtons();
            }

            @Override
            public void firstAdded() {
                enableAnnotationButtons();
            }
        };

        spanCollectionListener = new KnowtatorCollectionListener<Span>() {

            @Override
            public void selected(SelectionChangeEvent<Span> event) {
                if (event.getNew() == null) {
                    disableSpanButtons();
                } else {
                    enableSpanButtons();
                }
            }

            @Override
            public void added(AddEvent<Span> event) {
            }

            @Override
            public void removed(RemoveEvent<Span> event) {
            }

            @Override
            public void changed(ChangeEvent<Span> event) {
            }

            @Override
            public void emptied() {
                disableSpanButtons();
            }

            @Override
            public void firstAdded() {
                enableSpanButtons();
            }
        };

        controller.getTextSourceCollection().addCollectionListener(textSourceCollectionListener);
    }

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        if (controller != null) {
            setUpOWL();
        }
        return selectedClass;
    }

    @Override
    public void reset() {
        disposeView();
        setupListeners();
        knowtatorComponents.forEach(KnowtatorComponent::reset);
        controller.reset(getOWLWorkspace());
    }


    private void enableTextSourceButtons() {
        textSourceButtons.forEach(button -> button.setEnabled(true));
    }

    private void disableTextSourceButtons() {
        textSourceButtons.forEach(button -> button.setEnabled(false));
        disableAnnotationButtons();
    }

    private void enableAnnotationButtons() {
        annotationButtons.forEach(button -> button.setEnabled(true));
    }

    private void disableAnnotationButtons() {
        annotationButtons.forEach(button -> button.setEnabled(false));
        disableSpanButtons();
    }

    private void enableSpanButtons() {
        selectionSizeButtons.forEach(AbstractButton::removeActionListener);
        spanSizeButtons.forEach(AbstractButton::removeActionListener);
        spanSizeButtons.forEach(AbstractButton::addActionListener);
    }

    private void disableSpanButtons() {
        spanSizeButtons.forEach(AbstractButton::removeActionListener);
        selectionSizeButtons.forEach(AbstractButton::removeActionListener);
        selectionSizeButtons.forEach(AbstractButton::addActionListener);
    }

    public GraphViewDialog getGraphViewDialog() {
        return graphViewDialog;
    }


    public KnowtatorTextPane getKnowtatorTextPane() {
        return knowtatorTextPane;
    }

    @Override
    public void disposeView() {
        controller.dispose();

        knowtatorComponents.forEach(KnowtatorComponent::dispose);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent e) {
    }

    @Override
    public void dragExit(DropTargetEvent e) {
    }

    @Override
    public void drop(DropTargetDropEvent e) {
    }

    JTextComponent getSearchTextField() {
        return searchTextField;
    }

    @Override
    public void dragEnter(DropTargetDragEvent e) {
    }

    @Override
    public void dragOver(DropTargetDragEvent e) {
    }

    public JButton getAddTextSourceButton() {
        return addTextSourceButton;
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        panel1.setLayout(new BorderLayout(0, 0));
        panel2.add(panel1, BorderLayout.CENTER);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        panel3.setPreferredSize(new Dimension(672, 150));
        panel1.add(panel3, BorderLayout.NORTH);
        searchPanel = new JPanel();
        searchPanel.setLayout(new GridLayoutManager(3, 5, new Insets(0, 0, 0, 0), -1, -1));
        searchPanel.setAlignmentX(0.0f);
        panel3.add(searchPanel, BorderLayout.CENTER);
        menuButton = new JButton();
        menuButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-menu-24.png")));
        menuButton.setText("");
        menuButton.setVerticalAlignment(0);
        searchPanel.add(menuButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        previousMatchButton = new JButton();
        previousMatchButton.setText("Previous");
        searchPanel.add(previousMatchButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nextMatchButton = new JButton();
        nextMatchButton.setText("Next");
        searchPanel.add(nextMatchButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        searchPanel.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        findTextInOntologyButton = new JButton();
        this.$$$loadButtonText$$$(findTextInOntologyButton, ResourceBundle.getBundle("log4j").getString("find.in.ontology1"));
        searchPanel.add(findTextInOntologyButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        showGraphViewerButton = new JButton();
        showGraphViewerButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-tree-structure-32.png")));
        showGraphViewerButton.setText("");
        searchPanel.add(showGraphViewerButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        searchPanel.add(toolBar1, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        assignColorToClassButton = new JButton();
        assignColorToClassButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-color-dropper-filled-50 (Custom).png")));
        assignColorToClassButton.setText("");
        toolBar1.add(assignColorToClassButton);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator1);
        addAnnotationButton = new JButton();
        addAnnotationButton.setHorizontalTextPosition(0);
        addAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
        addAnnotationButton.setText("");
        addAnnotationButton.setVerticalAlignment(0);
        addAnnotationButton.setVerticalTextPosition(3);
        toolBar1.add(addAnnotationButton);
        removeAnnotationButton = new JButton();
        removeAnnotationButton.setHorizontalTextPosition(0);
        removeAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
        removeAnnotationButton.setText("");
        removeAnnotationButton.setVerticalTextPosition(3);
        toolBar1.add(removeAnnotationButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator2);
        growStartButton = new JButton();
        growStartButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-exit-32 (reversed).png")));
        growStartButton.setText("");
        toolBar1.add(growStartButton);
        shrinkStartButton = new JButton();
        shrinkStartButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-enter-32.png")));
        shrinkStartButton.setText("");
        toolBar1.add(shrinkStartButton);
        shrinkEndButton = new JButton();
        shrinkEndButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-enter-32 (reversed).png")));
        shrinkEndButton.setText("");
        toolBar1.add(shrinkEndButton);
        growEndButton = new JButton();
        growEndButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-exit-32.png")));
        growEndButton.setText("");
        toolBar1.add(growEndButton);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        searchPanel.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.add(searchTextField, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 25), new Dimension(-1, 25), 0, false));
        onlyAnnotationsCheckBox = new JCheckBox();
        onlyAnnotationsCheckBox.setText("Only in Annotations");
        panel4.add(onlyAnnotationsCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        caseSensitiveCheckBox = new JCheckBox();
        caseSensitiveCheckBox.setText("Case Sensitive");
        panel4.add(caseSensitiveCheckBox, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        regexCheckBox = new JCheckBox();
        regexCheckBox.setText("Regex");
        panel4.add(regexCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, BorderLayout.SOUTH);
        previousTextSourceButton = new JButton();
        previousTextSourceButton.setText("Previous");
        panel5.add(previousTextSourceButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fontSizeSlider = new JSlider();
        fontSizeSlider.setInverted(false);
        fontSizeSlider.setMajorTickSpacing(8);
        fontSizeSlider.setMaximum(28);
        fontSizeSlider.setMinimum(8);
        fontSizeSlider.setMinorTickSpacing(1);
        fontSizeSlider.setSnapToTicks(true);
        fontSizeSlider.setValue(16);
        panel5.add(fontSizeSlider, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel5.add(textSourceChooser, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addTextSourceButton = new JButton();
        addTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-plus-24.png")));
        addTextSourceButton.setText("");
        panel5.add(addTextSourceButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeTextSourceButton = new JButton();
        removeTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-delete-24.png")));
        removeTextSourceButton.setText("");
        panel5.add(removeTextSourceButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        nextTextSourceButton = new JButton();
        nextTextSourceButton.setText("Next");
        panel5.add(nextTextSourceButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(648);
        splitPane1.setMinimumSize(new Dimension(0, 0));
        panel1.add(splitPane1, BorderLayout.CENTER);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setMinimumSize(new Dimension(0, 100));
        scrollPane1.setPreferredSize(new Dimension(500, 100));
        splitPane1.setLeftComponent(scrollPane1);
        knowtatorTextPane.setEditable(false);
        scrollPane1.setViewportView(knowtatorTextPane);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel6.setMaximumSize(new Dimension(200, 2147483647));
        panel6.setMinimumSize(new Dimension(200, 158));
        splitPane1.setRightComponent(panel6);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridBagLayout());
        panel6.add(panel7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nextSpanButton = new JButton();
        nextSpanButton.setHorizontalAlignment(0);
        nextSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24.png")));
        nextSpanButton.setText("");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(nextSpanButton, gbc);
        previousSpanButton = new JButton();
        previousSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-advance-24 (reversed).png")));
        previousSpanButton.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel7.add(previousSpanButton, gbc);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("ID");
        panel6.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, Font.BOLD, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Annotator");
        panel6.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, Font.BOLD, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Class");
        panel6.add(label3, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$(null, Font.BOLD, 18, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Spans");
        panel6.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        panel6.add(scrollPane2, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane2.setViewportView(spanList);
        final JLabel label5 = new JLabel();
        Font label5Font = this.$$$getFont$$$(null, Font.BOLD, 18, label5.getFont());
        if (label5Font != null) label5.setFont(label5Font);
        label5.setText("Graph Spaces");
        panel6.add(label5, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        panel6.add(scrollPane3, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane3.setViewportView(graphSpaceList);
        final Spacer spacer3 = new Spacer();
        panel6.add(spacer3, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        annotationIDLabel.setText("");
        panel6.add(annotationIDLabel, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel6.add(annotationAnnotatorLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        annotationClassLabel.setText("");
        panel6.add(annotationClassLabel, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }
}
