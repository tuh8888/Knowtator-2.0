package edu.ucdenver.cpbs.mechanic;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationManager;
import edu.ucdenver.cpbs.mechanic.Commands.*;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICGraphViewer;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICProfileViewer;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICTextViewer;
import edu.ucdenver.cpbs.mechanic.xml.XmlUtil;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.dnd.*;
import java.io.*;


/**
 * @author Harrison Pielke-Lombardo
 */
@SuppressWarnings("PackageAccessibility")
public class MechAnICView extends AbstractOWLClassViewComponent implements DropTargetListener {

    private static final Logger log = Logger.getLogger(MechAnICView.class);

    private JSplitPane mainSplitPane;
    private JSplitPane annotationSplitPane;
    private JTabbedPane textViewerTabbedPane;
    private ProfileManager profileManager;

    private TextAnnotationManager textAnnotationManager;
    private MechAnICSelectionModel selectionModel;
    private XmlUtil xmlUtil;

    /**
     *
     */
    //TODO add a POS highlighter
    @Override
    public void initialiseClassView() {

        /*
        Initialize the managers, models, and utils
         */
        selectionModel = new MechAnICSelectionModel();  //helps get the selected OWL API classes
        profileManager = new ProfileManager();  //manipulates profiles and highlighters
        textAnnotationManager = new TextAnnotationManager(this, profileManager);  //manipulates the annotations
        xmlUtil = new XmlUtil(textAnnotationManager);  //reads and writes to XML

        createUI();

        DropTarget dt = new DropTarget(this, this);
        dt.setActive(true);

        setupInitial();

        log.warn("Initialized MechAnIC");
    }

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        selectionModel.setSelectedClass(selectedClass);
        return selectedClass;
    }

    /**
     * TODO add view to see and manage the mechanism Graphs
     */
    private void createUI() {
        setLayout(new BorderLayout());

        /*
        Create a tabbed pane containing the text viewer for each document
         */
        textViewerTabbedPane = new JTabbedPane();
        textViewerTabbedPane.setMinimumSize(new Dimension(100, 50));

        /*
        Create a viewer to see the highlighters for the current profile
         */
        MechAnICProfileViewer profileViewer = new MechAnICProfileViewer(profileManager);
        profileViewer.setMinimumSize(new Dimension(100, 50));
        profileManager.setProfileViewer(profileViewer);
        profileManager.setupDefault();

        /*
        Create a viewer to see the annotations as a graph
         */
        MechAnICGraphViewer graphViewer = new MechAnICGraphViewer();

        /*
        Place the tabbed text viewers and the profile viewer in a split pane
         */
        annotationSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        annotationSplitPane.setOneTouchExpandable(true);
        add(annotationSplitPane);
        annotationSplitPane.add(textViewerTabbedPane);
        annotationSplitPane.add(profileViewer);
        annotationSplitPane.setDividerLocation(1200);


        /*
        Place split pane in another split pane containing the Graph Viewer
         */
        mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setOneTouchExpandable(true);
        add(mainSplitPane);
        mainSplitPane.add(annotationSplitPane);
        mainSplitPane.add(graphViewer);

        /*
        Make the toolbar seen at the top of the view
         */
        createToolBar();
    }

    /**
     * TODO make initial document the last document that was edited
     */
    private void setupInitial() {
        /*
        An initial document to display
         */
        try {
            String fileName = "/file/test_article.txt";
            MechAnICTextViewer textViewer = new MechAnICTextViewer();
            textViewer.setName(fileName);
            JScrollPane sp = new JScrollPane(textViewer);
            textViewerTabbedPane.add(sp);
            textViewerTabbedPane.setTitleAt(0, fileName);

            InputStream is = getClass().getResourceAsStream(fileName);
            textViewer.read(new BufferedReader(new InputStreamReader(is)), fileName);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String fileName = "/file/test_annotations.xml";
            InputStream is = getClass().getResourceAsStream(fileName);
            MechAnICTextViewer textViewer = (MechAnICTextViewer)((JScrollPane) textViewerTabbedPane.getSelectedComponent()).getViewport().getView();
            xmlUtil.loadTextAnnotationsFromXML(is, textViewer);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add buttons corresponding to each of the actions to the toolbar
     */
    private void createToolBar() {
        /*
        Text document related actions
         */
        addAction(new OpenDocumentCommand(textViewerTabbedPane), "A", "A");
        addAction(new CloseDocumentCommand(textViewerTabbedPane), "A", "B");
        addAction(new IncreaseTextSizeCommand(textViewerTabbedPane), "A", "C");
        addAction(new DecreaseTextSizeCommand(textViewerTabbedPane), "A", "D");

        /*
        Text annotation related actions
         */
        addAction(new LoadAnnotationsCommand(textViewerTabbedPane, xmlUtil), "B", "A");
        addAction(new SaveAnnotationsToXmlCommand(textViewerTabbedPane, xmlUtil), "B", "B");
        addAction(new AddTextAnnotationCommand(textAnnotationManager, textViewerTabbedPane, selectionModel), "B", "C");
        addAction(new RemoveTextAnnotationCommand(textAnnotationManager, textViewerTabbedPane, selectionModel), "B", "D");

        /*
        Profile and highlighter related commands
         */
        addAction(new NewProfileCommand(profileManager), "C", "A");
        addAction(new SwitchProfileCommand(profileManager), "C", "B");
        addAction(new NewHighlighterCommand(profileManager), "C", "C");
    }

    @Override
    public void disposeView() {

    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

    }

    public static void main(String[] args) throws IOException { }
}
