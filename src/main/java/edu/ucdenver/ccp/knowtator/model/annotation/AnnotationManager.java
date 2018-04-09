package edu.ucdenver.ccp.knowtator.model.annotation;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.io.brat.StandoffTags;
import edu.ucdenver.ccp.knowtator.io.knowtator.*;
import edu.ucdenver.ccp.knowtator.listeners.ProjectListener;
import edu.ucdenver.ccp.knowtator.model.Savable;
import edu.ucdenver.ccp.knowtator.model.graph.AnnotationNode;
import edu.ucdenver.ccp.knowtator.model.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.model.graph.GraphSpaces;
import edu.ucdenver.ccp.knowtator.model.graph.Triple;
import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.model.textsource.TextSource;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class AnnotationManager implements Savable, ProjectListener {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AnnotationManager.class);

    private final KnowtatorController controller;

    private TreeSet<Span> spanTreeSet;
    private TextSource textSource;
    private Map<String, Annotation> annotationMap;
    private GraphSpaces graphSpaces;

    public AnnotationManager(KnowtatorController controller, TextSource textSource) {
        this.controller = controller;
        this.textSource = textSource;
        annotationMap = new HashMap<>();
        spanTreeSet = new TreeSet<>(Span::compare);
        graphSpaces = new GraphSpaces();
    }

    void addAnnotation(Annotation newAnnotation) {
        String id = newAnnotation.getID();
        if (id == null || annotationMap.containsKey(id)) {
            id = String.format("mention_%d", annotationMap.size());
        }

        while (annotationMap.containsKey(id)) {
            int annotationIDIndex = Integer.parseInt(id.split("mention_")[1]);
            id = String.format("mention_%d", ++annotationIDIndex);
        }
        newAnnotation.setID(id);
        annotationMap.put(newAnnotation.getID(), newAnnotation);

        spanTreeSet.addAll(newAnnotation.getSpans());

        controller.getSelectionManager().setSelectedAnnotation(newAnnotation, null);
    }

    public void addSpanToAnnotation(Annotation annotation, Span newSpan) {
        annotation.addSpan(newSpan);
        spanTreeSet.add(newSpan);
    }

    void removeAnnotation(Annotation annotationToRemove) {
        annotationMap.remove(annotationToRemove.getID());
        for (Span span : annotationToRemove.getSpans()) {
            spanTreeSet.remove(span);
        }
        for (GraphSpace graphSpace : graphSpaces.getGraphSpaces()) {
            for (Object vertex : graphSpace.getVerticesForAnnotation(annotationToRemove)) {
                graphSpace.setSelectionCell(vertex);
                graphSpace.removeSelectedCell();
            }
        }
        controller.getSelectionManager().setSelectedAnnotation(null, null);
    }

    public void removeSpanFromAnnotation(Annotation annotation, Span span) {
        annotation.removeSpan(span);
        spanTreeSet.remove(span);
    }

    public Collection<Annotation> getAnnotations() {
        return annotationMap.values();
    }

    public Set<Annotation> getAllAnnotations() {
        return annotationMap.values().stream().filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * @param loc     Location filter
     */
    public TreeSet<Span> getSpanSet(Integer loc) {
        Supplier<TreeSet<Span>> supplier = () -> new TreeSet<>(Span::compare);
        return spanTreeSet.stream().filter(span ->
                (loc == null || span.contains(loc)) &&
                        (!controller.getSelectionManager().isFilterByProfile() ||
                                span.getAnnotation().getAnnotator().equals(controller.getSelectionManager().getActiveProfile())))
                .collect(Collectors.toCollection(supplier));
    }

    public void growSelectedSpanStart() {
        Span span = controller.getSelectionManager().getSelectedSpan();
        spanTreeSet.remove(span);
        span.growStart();
        spanTreeSet.add(span);
        controller.getSelectionManager().setSelectedSpan(span);
    }

    public void growSelectedSpanEnd() {
        Span span = controller.getSelectionManager().getSelectedSpan();
        spanTreeSet.remove(span);
        span.growEnd(textSource.getContent().length());
        spanTreeSet.add(controller.getSelectionManager().getSelectedSpan());
        controller.getSelectionManager().setSelectedSpan(span);
    }

    public void shrinkSelectedSpanEnd() {
        Span span = controller.getSelectionManager().getSelectedSpan();
        spanTreeSet.remove(span);
        span.shrinkEnd();
        spanTreeSet.add(span);
        controller.getSelectionManager().setSelectedSpan(span);
    }

    public void shrinkSelectedSpanStart() {
        Span span = controller.getSelectionManager().getSelectedSpan();
        spanTreeSet.remove(span);
        span.shrinkStart();
        spanTreeSet.add(span);
        controller.getSelectionManager().setSelectedSpan(span);
    }

    public void addAnnotation(OWLClass owlClass, String owlClassID, Span span) {
        if (owlClass != null) {
            Annotation newAnnotation = new Annotation(
                    controller, null, owlClass,
                    owlClassID,
                    controller.getSelectionManager().getActiveProfile(), "identity", textSource
            );
            newAnnotation.addSpan(span);
            addAnnotation(newAnnotation);
        }
    }

    @SuppressWarnings("unused")
    public void findOverlaps() {
        List<Span> overlappingSpans = new ArrayList<>();
        spanTreeSet.forEach(span -> {
            List<Span> toRemove = new ArrayList<>();
            overlappingSpans.forEach(span1 -> {
                if (span.intersects(span1)) {
                    span.getAnnotation().addOverlappingAnnotation(span1.getAnnotation());
                    span1.getAnnotation().addOverlappingAnnotation(span.getAnnotation());
                } else {
                    toRemove.add(span1);
                }
            });
            overlappingSpans.removeAll(toRemove);

            overlappingSpans.add(span);
        });
    }

    public Annotation getAnnotation(String annotationID) {
        return annotationMap.get(annotationID);
    }

    public GraphSpace addGraphSpace(String title) {
        GraphSpace newGraphSpace = new GraphSpace(controller, textSource, title);
        graphSpaces.add(newGraphSpace);
        controller.getSelectionManager().setActiveGraphSpace(newGraphSpace);
        return newGraphSpace;
    }

    public void writeToKnowtatorXML(Document dom, Element textSourceElement) {
        getAnnotations().forEach(annotation -> annotation.writeToKnowtatorXML(dom, textSourceElement));
        graphSpaces.getGraphSpaces().forEach(graphSpace -> graphSpace.writeToKnowtatorXML(dom, textSourceElement));
    }

    @Override
    public void readFromKnowtatorXML(File file, Element parent, String content) {
        for (Node annotationNode : KnowtatorXMLUtil.asList(parent.getElementsByTagName(KnowtatorXMLTags.ANNOTATION))) {
            Element annotationElement = (Element) annotationNode;

            String annotationID = annotationElement.getAttribute(KnowtatorXMLAttributes.ID);
            String profileID = annotationElement.getAttribute(KnowtatorXMLAttributes.ANNOTATOR);
            String type = annotationElement.getAttribute(KnowtatorXMLAttributes.TYPE);

            Profile profile = controller.getProfileManager().addProfile(profileID);
            String owlClassID = ((Element) annotationElement.getElementsByTagName(KnowtatorXMLTags.CLASS).item(0)).getAttribute(KnowtatorXMLAttributes.ID);

            Annotation newAnnotation = new Annotation(controller, annotationID, null, owlClassID, profile, type, textSource);
            newAnnotation.readFromKnowtatorXML(null, annotationElement, content);

            addAnnotation(newAnnotation);
        }

        for (Node graphSpaceNode : KnowtatorXMLUtil.asList(parent.getElementsByTagName(KnowtatorXMLTags.GRAPH_SPACE))) {
            Element graphSpaceElem = (Element) graphSpaceNode;


            String id = graphSpaceElem.getAttribute(KnowtatorXMLAttributes.ID);
            GraphSpace graphSpace = addGraphSpace(id);

//            log.warn("\t\tXML: " + graphSpace);
            graphSpace.readFromKnowtatorXML(null, graphSpaceElem, content);
        }
    }

    @Override
    public void readFromOldKnowtatorXML(File file, Element parent, String content) {

        Map<String, Element> slotToClassIDMap = KnowtatorXMLUtil.getslotsFromXml(parent);
        Map<String, Element> classMentionToClassIDMap = KnowtatorXMLUtil.getClassIDsFromXml(parent);
        Map<Annotation, Element> annotationToSlotMap = new HashMap<>();

        for (Node annotationNode : KnowtatorXMLUtil.asList(parent.getElementsByTagName(OldKnowtatorXMLTags.ANNOTATION))) {
            Element annotationElement = (Element) annotationNode;

            Profile profile;
            try {
                String profileID = annotationElement.getElementsByTagName(OldKnowtatorXMLTags.ANNOTATOR).item(0).getTextContent();
                profile = controller.getProfileManager().addProfile(profileID);
            } catch (NullPointerException npe) {
                profile = controller.getProfileManager().getDefaultProfile();
            }

            String annotationID = ((Element) annotationElement.getElementsByTagName(OldKnowtatorXMLTags.MENTION).item(0)).getAttribute(OldKnowtatorXMLAttributes.ID);
            Element classElement = classMentionToClassIDMap.get(annotationID);

            String owlClassID = ((Element) classElement.getElementsByTagName(OldKnowtatorXMLTags.MENTION_CLASS).item(0)).getAttribute(OldKnowtatorXMLAttributes.ID);

            Annotation newAnnotation = new Annotation(controller, annotationID, null, owlClassID, profile, "identity", textSource);

            newAnnotation.readFromOldKnowtatorXML(null, annotationElement, content);

            // No need to keep annotations with no spanTreeSet
            if (!newAnnotation.getSpans().isEmpty()) {
                addAnnotation(newAnnotation);

//                log.warn("OLD KNOWTATOR: added ANNOTATION " + newAnnotation);

                for (Node slotMentionNode : KnowtatorXMLUtil.asList(classElement.getElementsByTagName(OldKnowtatorXMLTags.HAS_SLOT_MENTION))) {
                    Element slotMentionElement = (Element) slotMentionNode;
                    String slotMentionID = slotMentionElement.getAttribute(OldKnowtatorXMLAttributes.ID);
                    Element slotElement = slotToClassIDMap.get(slotMentionID);
                    annotationToSlotMap.put(newAnnotation, slotElement);
                }
            }
        }

        GraphSpace oldKnowtatorGraphSpace = addGraphSpace("Old Knowtator Relations");

        annotationToSlotMap.forEach((annotation, slot) -> {
            List<Object> vertices = oldKnowtatorGraphSpace.getVerticesForAnnotation(annotation);

            AnnotationNode source;
            if (vertices.isEmpty()) {
                source = oldKnowtatorGraphSpace.addNode(null, annotation);
            } else {
                source = (AnnotationNode) vertices.get(0);
            }

            String property = ((Element) slot.getElementsByTagName(OldKnowtatorXMLTags.MENTION_SLOT).item(0)).getAttribute(OldKnowtatorXMLAttributes.ID);
            for (Node slotMentionValueNode : OldKnowatorUtil.asList(slot.getElementsByTagName(OldKnowtatorXMLTags.COMPLEX_SLOT_MENTION_VALUE))) {
                Element slotMentionValueElement = (Element) slotMentionValueNode;
                String value = slotMentionValueElement.getAttribute(OldKnowtatorXMLAttributes.VALUE);
                Annotation annotation1 = getAnnotation(value);

                List<Object> vertices1 = oldKnowtatorGraphSpace.getVerticesForAnnotation(annotation1);

                AnnotationNode target;
                if (vertices1.isEmpty()) {
                    target = oldKnowtatorGraphSpace.addNode(null, annotation1);
//                        log.warn("OLD KNOWTATOR: added NODE: " + target);
                } else {
                    target = (AnnotationNode) vertices1.get(0);
                }

                oldKnowtatorGraphSpace.addTriple(
                        source,
                        target,
                        null,
                        controller.getSelectionManager().getActiveProfile(),
                        property,
                        "",
                        "");
//                log.warn("OLD KNOWTATOR: added TRIPLE: " + triple);
            }
        });

    }

    @Override
    public void readFromBratStandoff(File file, Map<Character, List<String[]>> annotationMap, String content) {

        Profile profile = controller.getProfileManager().getDefaultProfile();

        annotationMap.get(StandoffTags.TEXTBOUNDANNOTATION).forEach(annotation -> {
            Annotation newAnnotation = new Annotation(controller, annotation[0], null, annotation[1].split(StandoffTags.textBoundAnnotationTripleDelimiter)[0], profile, "identity", textSource);
            Map<Character, List<String[]>> map = new HashMap<>();
            List<String[]> list = new ArrayList<>();
            list.add(annotation);
            map.put(StandoffTags.TEXTBOUNDANNOTATION, list);
            newAnnotation.readFromBratStandoff(null, map, content);

            addAnnotation(newAnnotation);
        });

        GraphSpace newGraphSpace = addGraphSpace("Brat Relation Graph");
        newGraphSpace.readFromBratStandoff(null, annotationMap, null);
    }


    @Override
    public void writeToBratStandoff(Writer writer) throws IOException {
        Iterator<Annotation> annotationIterator = annotationMap.values().iterator();
        for (int i=0; i<annotationMap.values().size(); i++) {
            Annotation annotation = annotationIterator.next();
            annotation.setBratID(String.format("T%d", i));
            writer.append(String.format("%s\t%s ", annotation.getBratID(), annotation.getOwlClass()));
            annotation.writeToBratStandoff(writer);

            writer.append(String.format("\t%s\n", annotation.getSpans().first().getSpannedText()));
        }

        int lastNumTriples = 0;
        for (GraphSpace graphSpace : graphSpaces.getGraphSpaces()) {
            Object[] edges = graphSpace.getChildEdges(graphSpace.getDefaultParent());
            int bound = edges.length;
            for (int i = 0; i < bound; i++) {
                Object edge = edges[i];
                Triple triple = (Triple) edge;
                triple.setBratID(String.format("R%d", lastNumTriples + i));
                writer.append(String.format("%s\t%s Arg1:%s Arg2:%s\n",
                        triple.getBratID(),
                        triple.getValue(),
                        ((AnnotationNode) triple.getSource()).getAnnotation().getBratID(),
                        ((AnnotationNode) triple.getTarget()).getAnnotation().getBratID())
                );
            }
        }
    }

    @Override
    public void readFromGeniaXML(Element parent, String content) {

    }

    @Override
    public void writeToGeniaXML(Document dom, Element parent) {

    }

    public GraphSpaces getGraphSpaces() {
        return graphSpaces;
    }

    public void removeGraphSpace(GraphSpace graphSpace) {
        graphSpaces.remove(graphSpace);
    }

    public void getPreviousSpan() {
        Span selectedSpan = controller.getSelectionManager().getSelectedSpan();
        TreeSet<Span> annotationMap = textSource.getAnnotationManager().getSpanSet(null);
        Span previousSpan;
        try {
            previousSpan = annotationMap.contains(selectedSpan) ? annotationMap.lower(selectedSpan) : annotationMap.floor(selectedSpan);
        } catch (NullPointerException npe) {
            previousSpan = null;
        }
        if (previousSpan == null) previousSpan = annotationMap.last();

        controller.getSelectionManager().setSelectedSpan(previousSpan);
    }

    public void getNextSpan() {
        Span selectedSpan = controller.getSelectionManager().getSelectedSpan();
        TreeSet<Span> annotationMap = textSource.getAnnotationManager().getSpanSet(null);

        Span nextSpan;
        try {
            nextSpan = annotationMap.contains(selectedSpan) ? annotationMap.higher(selectedSpan) : annotationMap.ceiling(selectedSpan);
        } catch (NullPointerException npe) {
            nextSpan = null;
        }
        if (nextSpan == null) nextSpan = annotationMap.first();

        controller.getSelectionManager().setSelectedSpan(nextSpan);
    }

    public void connectToOWLModelManager() {
        for (Annotation annotation : annotationMap.values()) {
            //noinspection ResultOfMethodCallIgnored
            annotation.getOwlClass();
        }
        for (GraphSpace graphSpace : graphSpaces.getGraphSpaces()) {
            graphSpace.connectEdgesToProperties();
        }
    }

    @Override
    public void projectLoaded() {
        connectToOWLModelManager();
    }

    public GraphSpace getPreviousGraphSpace() {
        GraphSpace graphSpace = controller.getSelectionManager().getActiveGraphSpace();

        GraphSpace previousGraphSpace;
        try {
            previousGraphSpace = graphSpaces.getGraphSpaces().contains(graphSpace) ? graphSpaces.getGraphSpaces().lower(graphSpace) : graphSpaces.getGraphSpaces().floor(graphSpace);
        } catch (NullPointerException npe) {
            previousGraphSpace = null;
        }
        if (previousGraphSpace == null) previousGraphSpace = graphSpaces.getGraphSpaces().last();

        return previousGraphSpace;
    }

    public GraphSpace getNextGraphSpace() {
        GraphSpace graphSpace = controller.getSelectionManager().getActiveGraphSpace();

        GraphSpace nextGraphSpace;
        try {
            nextGraphSpace = graphSpaces.getGraphSpaces().contains(graphSpace) ? graphSpaces.getGraphSpaces().higher(graphSpace) : graphSpaces.getGraphSpaces().ceiling(graphSpace);
        } catch (NullPointerException npe) {
            nextGraphSpace = null;
        }
        if (nextGraphSpace == null) nextGraphSpace = graphSpaces.getGraphSpaces().first();

        return nextGraphSpace;
    }

    public void removeSelectedAnnotation() {
        removeAnnotation(controller.getSelectionManager().getSelectedAnnotation());
    }

    public void addSelectedAnnotation() {
        int start = controller.getSelectionManager().getStart();
        int end = controller.getSelectionManager().getEnd();
        String spannedText = textSource.getContent().substring(start, end);
        Span newSpan = new Span(start, end, spannedText);

        OWLClass owlClass = controller.getSelectionManager().getSelectedOWLClass();
        Profile annotator = controller.getSelectionManager().getActiveProfile();
        Annotation newAnnotation = new Annotation(controller, null, owlClass, null, annotator, "identity", textSource);

        addSpanToAnnotation(newAnnotation, newSpan);
    }
}
