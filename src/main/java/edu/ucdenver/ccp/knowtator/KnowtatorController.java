package edu.ucdenver.ccp.knowtator;

import edu.ucdenver.ccp.knowtator.listeners.*;
import edu.ucdenver.ccp.knowtator.model.annotation.Annotation;
import edu.ucdenver.ccp.knowtator.model.annotation.Span;
import edu.ucdenver.ccp.knowtator.model.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.model.owl.OWLAPIDataExtractor;
import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.model.profile.ProfileManager;
import edu.ucdenver.ccp.knowtator.model.project.ProjectManager;
import edu.ucdenver.ccp.knowtator.model.selection.SelectionManager;
import edu.ucdenver.ccp.knowtator.model.textsource.TextSource;
import edu.ucdenver.ccp.knowtator.model.textsource.TextSourceManager;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;


/**
 * @author Harrison Pielke-Lombardo
 * @version 2.0.7
 */

public class KnowtatorController {
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(KnowtatorController.class);

    private final Preferences prefs = Preferences.userRoot().node("knowtator");
    private ProjectManager projectManager;
    private TextSourceManager textSourceManager;
    private ProfileManager profileManager;
    private SelectionManager selectionManager;
    private OWLAPIDataExtractor owlDataExtractor;

    private Set<TextSourceListener> textSourceListeners;
    private Set<ProfileListener> profileListeners;
    private Set<AnnotationListener> annotationListeners;
    private Set<SpanListener> spanListeners;
    private Set<GraphListener> graphListeners;
    private Set<ProjectListener> projectListeners;
    private KnowtatorView view;

    public KnowtatorController() {
        initListeners();
        initManagers();
    }

    /**
     * @param view The view that spawned this controller
     */
    public KnowtatorController(KnowtatorView view) {
        this();
        this.view = view;
    }

    private void initManagers() {
        selectionManager = new SelectionManager(this);
        textSourceManager = new TextSourceManager(this);
        profileManager = new ProfileManager(this);  //manipulates profiles and colors
        projectManager = new ProjectManager(this);  //reads and writes to XML
        owlDataExtractor = new OWLAPIDataExtractor();
    }

    private void initListeners() {
        projectListeners = new HashSet<>();
        textSourceListeners = new HashSet<>();
        profileListeners = new HashSet<>();
        annotationListeners = new HashSet<>();
        spanListeners = new HashSet<>();
        graphListeners = new HashSet<>();
    }

    public static void main(String[] args) {

    }

    public void close(File file) {
        initManagers();
        projectManager.loadProject(file);
    }

    public void setUpOWL(OWLWorkspace owlWorkspace, OWLModelManager owlModelManager) {
        owlDataExtractor.setUpOWL(owlWorkspace, owlModelManager);
    }
    public ProfileManager getProfileManager() {
        return profileManager;
    }
    public ProjectManager getProjectManager() {
        return projectManager;
    }

    /**
     * GETTERS
     */

    public OWLAPIDataExtractor getOWLAPIDataExtractor() {
        return owlDataExtractor;
    }
    public TextSourceManager getTextSourceManager() {
        return textSourceManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * ADDERS
     */

    public void addConceptAnnotationListener(AnnotationListener listener) {
        annotationListeners.add(listener);
    }

    public void addSpanListener(SpanListener listener) {
        spanListeners.add(listener);
    }

    public void addProfileListener(ProfileListener listener) {
        profileListeners.add(listener);
    }

    public void addProjectListener(ProjectListener listener) {
        projectListeners.add(listener);
    }

    public void addGraphListener(GraphListener listener) {
        graphListeners.add(listener);
    }

    public void addAnnotationListener(AnnotationListener listener) {
        annotationListeners.add(listener);
    }

    public void addTextSourceListener(TextSourceListener listener) {
        textSourceListeners.add(listener);
    }
    public void profileAddedEvent(Profile profile) {
        profileListeners.forEach(profileListener -> profileListener.profileAdded(profile));
    }
    public void spanSelectionChangedEvent(Span span) {
        spanListeners.forEach(spanListener -> spanListener.spanSelectionChanged(span));
    }
    public void profileSelectionChangedEvent(Profile profile) {
        profileListeners.forEach(profileListener -> profileListener.profileSelectionChanged(profile));
    }
    public void annotationAddedEvent(Annotation newAnnotation) {
        annotationListeners.forEach(listener -> listener.annotationAdded(newAnnotation));
    }

    /**
     * EVENTS
     */

    public void textSourceAddedEvent(TextSource textSource) {
        textSourceListeners.forEach(textSourceListener -> textSourceListener.textSourceAdded(textSource));
    }
    public void annotationRemovedEvent(Annotation removedAnnotation) {
        selectionManager.setSelectedAnnotation(null, null);
        annotationListeners.forEach(listener -> listener.annotationRemoved(removedAnnotation));
    }

    public void spanAddedEvent(Span newSpan) {
        spanListeners.forEach(spanListener -> spanListener.spanAdded(newSpan));
    }

    public void spanRemovedEvent() {
        spanListeners.forEach(SpanListener::spanRemoved);
    }
    public void profileRemovedEvent() {
        profileListeners.forEach(ProfileListener::profileRemoved);
    }

    public void profileFilterChangedEvent(boolean filterByProfile) {
        selectionManager.setSelectedAnnotation(null, null);
        profileListeners.forEach(profileListener -> profileListener.profileFilterSelectionChanged(filterByProfile));
    }
    public void colorChangedEvent() {
        profileListeners.forEach(ProfileListener::colorChanged);
    }

    public void removeGraphEvent(GraphSpace graphSpace) {
        graphListeners.forEach(listener -> listener.graphSpaceRemoved(graphSpace));
    }
    public void newGraphEvent(GraphSpace graphSpace) {
        graphListeners.forEach(listener -> listener.newGraph(graphSpace));
    }
    public void projectLoadedEvent() {
        projectListeners.forEach(ProjectListener::projectLoaded);
    }

    public void annotationSelectionChangedEvent(Annotation annotation) {
        if (annotation != null) {
            if (view != null) {
                if (annotation.isOwlClass()) {
                    view.owlEntitySelectionChanged((OWLClass) annotation.getOwlClass());
                }
                view.getGraphViewer().goToAnnotationVertex(selectionManager.getActiveGraphSpace(), annotation);
            }
            annotationListeners.forEach(listener -> listener.annotationSelectionChanged(annotation));
        }
    }

    public void propertyChangedEvent(Object value) {
        if (value instanceof OWLProperty) {
            view.owlEntitySelectionChanged((OWLObjectProperty) value);
        } else if (value instanceof String) {
            view.owlEntitySelectionChanged(owlDataExtractor.getOWLObjectPropertyByID((String) value));
        }
    }

    public void activeTextSourceChangedEvent(TextSource textSource) {
        selectionManager.setSelectedAnnotation(null, null);
        textSourceListeners.forEach(listenter -> listenter.activeTextSourceChanged(textSource));
    }

    public KnowtatorView getView() {
        return view;
    }

    public Preferences getPrefs() {
        return prefs;
    }
}
