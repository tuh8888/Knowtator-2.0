package edu.ucdenver.ccp.knowtator.model;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraph;
import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.events.AnnotationChangeEvent;
import edu.ucdenver.ccp.knowtator.io.brat.StandoffTags;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLAttributes;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLTags;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLUtil;
import edu.ucdenver.ccp.knowtator.listeners.AnnotationSelectionListener;
import edu.ucdenver.ccp.knowtator.listeners.GraphSpaceListener;
import edu.ucdenver.ccp.knowtator.model.owl.OWLWorkSpaceNotSetException;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class GraphSpace extends mxGraph
    implements Savable, KnowtatorObject, OWLModelManagerListener, AnnotationSelectionListener {
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(GraphSpace.class);

  private String id;
  private KnowtatorController controller;
  private TextSource textSource;
  private int graphTextStart;
  private int graphTextEnd;
  private List<GraphSpaceListener> listeners;
  private boolean areListenersSet;

  public GraphSpace(KnowtatorController controller, TextSource textSource, String id) {

    this.controller = controller;
    this.textSource = textSource;

    controller.verifyId(id, this, false);
    controller.getSelectionManager().addAnnotationListener(this);

    setCellsResizable(false);
    setEdgeLabelsMovable(false);
    setAllowDanglingEdges(false);
    setCellsEditable(false);
    setConnectableEdges(false);
    setCellsBendable(false);
    setResetEdgesOnMove(true);
    listeners = new ArrayList<>();
    areListenersSet = false;
  }

  public static int compare(GraphSpace graphSpace1, GraphSpace graphSpace2) {
    if (graphSpace1 == graphSpace2) {
      return 0;
    }
    if (graphSpace1 == null) {
      return -1;
    }
    if (graphSpace2 == null) {
      return 1;
    }
    return graphSpace1.getId().toLowerCase().compareTo(graphSpace2.getId().toLowerCase());
  }

  /*
  ADDERS
   */

  public void addGraphSpaceListener(GraphSpaceListener listener) {
    listeners.add(listener);
  }

  public void removeGraphSpaceListener(GraphSpaceListener listener) {
    listeners.add(listener);
  }

  private void addCellToGraph(mxCell cell) {
    getModel().beginUpdate();
    try {
      addCell(cell);
    } finally {
      //      reDrawGraph();
      getModel().endUpdate();
    }

    //    reDrawGraph();
  }

  public AnnotationNode addNode(String id, Annotation annotation) {
    if (annotation != null) {
      id = verifyID(id, "node");

      AnnotationNode newVertex = new AnnotationNode(controller, id, annotation, textSource);
      addCellToGraph(newVertex);

      return newVertex;
    } else {
      return null;
    }
  }

  public void addTriple(
      AnnotationNode source,
      AnnotationNode target,
      String id,
      Profile annotator,
      OWLObjectProperty property,
      String propertyID,
      String quantifier,
      String quantifierValue) {
    id = verifyID(id, "edge");

    Triple newTriple =
        new Triple(
            id,
            source,
            target,
            property,
            propertyID,
            annotator,
            quantifier,
            quantifierValue,
            controller,
            textSource);

    setCellStyles(mxConstants.STYLE_STARTARROW, "dash", new Object[] {newTriple});
    setCellStyles(mxConstants.STYLE_STARTSIZE, "12", new Object[] {newTriple});
    setCellStyles(mxConstants.STYLE_ENDARROW, "block", new Object[] {newTriple});
    setCellStyles(mxConstants.STYLE_VERTICAL_ALIGN, "top", new Object[] {newTriple});
    setCellStyles(mxConstants.STYLE_VERTICAL_LABEL_POSITION, "top", new Object[] {newTriple});
    setCellStyles(mxConstants.STYLE_FONTSIZE, "16", new Object[] {newTriple});

    addCellToGraph(newTriple);
  }

  /*
  REMOVERS
   */

  private String verifyID(String id, String idPrefix) {
    if (id == null) {
      id = String.format("%s_%d", idPrefix, getChildCells(getDefaultParent()).length);
    }
    while (((mxGraphModel) getModel()).getCells().containsKey(id)) {
      int vertexIDIndex = Integer.parseInt(id.split(String.format("%s_", idPrefix))[1]);
      id = String.format("%s_%d", idPrefix, ++vertexIDIndex);
    }

    return id;
  }

  /*
  I/O
   */

  public void removeSelectedCell() {
    //    Object cell = getSelectionModel().getCell();
    //    Arrays.stream(getEdges(cell)).forEach(edge -> getModel().remove(edge));
    removeCells(getSelectionCells(), true);
    //    reDrawGraph();
  }

  @Override
  public void readFromKnowtatorXML(File file, Element parent) {
    for (Node graphVertexNode :
        KnowtatorXMLUtil.asList(parent.getElementsByTagName(KnowtatorXMLTags.VERTEX))) {
      Element graphVertexElem = (Element) graphVertexNode;

      String id = graphVertexElem.getAttribute(KnowtatorXMLAttributes.ID);
      String annotationID = graphVertexElem.getAttribute(KnowtatorXMLTags.ANNOTATION);

      Annotation annotation = this.textSource.getAnnotationManager().getAnnotation(annotationID);
      addNode(id, annotation);
    }

    for (Node tripleNode :
        KnowtatorXMLUtil.asList(parent.getElementsByTagName(KnowtatorXMLTags.TRIPLE))) {
      Element tripleElem = (Element) tripleNode;

      String id = tripleElem.getAttribute(KnowtatorXMLAttributes.ID);
      String annotatorID = tripleElem.getAttribute(KnowtatorXMLAttributes.ANNOTATOR);
      String subjectID = tripleElem.getAttribute(KnowtatorXMLAttributes.TRIPLE_SUBJECT);
      String objectID = tripleElem.getAttribute(KnowtatorXMLAttributes.TRIPLE_OBJECT);
      String propertyID = tripleElem.getAttribute(KnowtatorXMLAttributes.TRIPLE_PROPERTY);
      String quantifier = tripleElem.getAttribute(KnowtatorXMLAttributes.TRIPLE_QUANTIFIER);
      String quantifierValue = tripleElem.getAttribute(KnowtatorXMLAttributes.TRIPLE_VALUE);

      Profile annotator = controller.getProfileManager().addProfile(annotatorID);
      AnnotationNode source =
          (AnnotationNode) ((mxGraphModel) getModel()).getCells().get(subjectID);
      AnnotationNode target = (AnnotationNode) ((mxGraphModel) getModel()).getCells().get(objectID);

      if (target != null && source != null) {
        addTriple(source, target, id, annotator, null, propertyID, quantifier, quantifierValue);
      }
    }
  }

  @Override
  public void readFromOldKnowtatorXML(File file, Element parent) {}

  @Override
  public void readFromBratStandoff(
      File file, Map<Character, List<String[]>> annotationMap, String content) {
    annotationMap
        .get(StandoffTags.RELATION)
        .forEach(
            annotation -> {
              String id = annotation[0];

              String[] relationTriple = annotation[1].split(StandoffTags.relationTripleDelimiter);
              String propertyID = relationTriple[0];
              String subjectAnnotationID =
                  relationTriple[1].split(StandoffTags.relationTripleRoleIDDelimiter)[1];
              String objectAnnotationID =
                  relationTriple[2].split(StandoffTags.relationTripleRoleIDDelimiter)[1];

              Profile annotator = controller.getProfileManager().getDefaultProfile();

              Annotation subjectAnnotation =
                  textSource.getAnnotationManager().getAnnotation(subjectAnnotationID);
              List<Object> subjectAnnotationVertices = getVerticesForAnnotation(subjectAnnotation);
              AnnotationNode source;
              if (subjectAnnotationVertices.size() == 0) {
                source = addNode(null, subjectAnnotation);
              } else {
                source = (AnnotationNode) subjectAnnotationVertices.get(0);
              }

              Annotation objectAnnotation =
                  textSource.getAnnotationManager().getAnnotation(objectAnnotationID);
              List<Object> objectAnnotationVertices = getVerticesForAnnotation(objectAnnotation);
              AnnotationNode target;
              if (objectAnnotationVertices.size() == 0) {
                target = addNode(null, objectAnnotation);
              } else {
                target = (AnnotationNode) objectAnnotationVertices.get(0);
              }

              addTriple(source, target, id, annotator, null, propertyID, "", "");
            });
  }

  @SuppressWarnings("RedundantThrows")
  @Override
  public void writeToBratStandoff(Writer writer) throws IOException {}

  @Override
  public void readFromGeniaXML(Element parent, String content) {}

  @Override
  public void writeToGeniaXML(Document dom, Element parent) {}

  @Override
  public void writeToKnowtatorXML(Document dom, Element textSourceElement) {
    Element graphElem = dom.createElement(KnowtatorXMLTags.GRAPH_SPACE);
    graphElem.setAttribute(KnowtatorXMLAttributes.ID, id);
    Arrays.stream(getChildVertices(getDefaultParent()))
        .forEach(
            vertex -> {
              if (vertex instanceof AnnotationNode) {
                ((AnnotationNode) vertex).writeToKnowtatorXML(dom, graphElem);
              }
            });
    Arrays.stream(getChildEdges(getDefaultParent()))
        .forEach(
            edge -> {
              if (edge instanceof Triple) {
                ((Triple) edge).writeToKnowtatorXML(dom, graphElem);
              }
            });
    textSourceElement.appendChild(graphElem);
  }

  /*
  UPDATE
   */
  public void reDrawGraph() {
    log.warn("Redrawing Graph");
    getModel().beginUpdate();
    try {
      Arrays.stream(getChildVertices(getDefaultParent()))
          .forEach(
              vertex -> {
                if (vertex instanceof AnnotationNode) {
                  setVertexStyle((AnnotationNode) vertex);
                }
                updateCellSize(vertex);

                getView().validateCell(vertex);
              });
      Arrays.stream(getChildEdges(getDefaultParent()))
          .forEach(
              edge -> {
                updateCellSize(edge);
                //                if (edge instanceof Triple) {
                //                    ((Triple) edge).setValue(((Triple) edge).getValue());
                //                }
                getView().validateCell(edge);
              });
    } finally {
      getModel().endUpdate();
      refresh();
    }
  }

  public void setupListeners() {
    // Handle drag and drop
    // Adds the current selected object property as the edge value
    if (!areListenersSet) {
      addListener(
          mxEvent.ADD_CELLS,
          (sender, evt) -> {
            Object[] cells = (Object[]) evt.getProperty("cells");
            if (cells != null && cells.length > 0) {
              for (Object cell : cells) {
                if (getModel().isEdge(cell) && "".equals(((mxCell) cell).getValue())) {
                  mxCell edge = (mxCell) cell;
                  OWLObjectProperty property =
                      controller.getSelectionManager().getSelectedOWLObjectProperty();
                  String propertyID = null;
                  try {
                    propertyID =
                        controller.getOWLAPIDataExtractor().getOWLEntityRendering(property);
                  } catch (OWLWorkSpaceNotSetException ignored) {

                  }
                  if (property != null) {
                    mxICell source = edge.getSource();
                    mxICell target = edge.getTarget();

                    JTextField quantifierField = new JTextField(10);
                    JTextField valueField = new JTextField(10);
                    JPanel restrictionPanel = new JPanel();
                    restrictionPanel.add(new JLabel("Quantifier:"));
                    restrictionPanel.add(quantifierField);
                    restrictionPanel.add(Box.createHorizontalStrut(15));
                    restrictionPanel.add(new JLabel("Value:"));
                    restrictionPanel.add(valueField);

                    String quantifier = "";
                    String value = "";
                    int result =
                        JOptionPane.showConfirmDialog(
                            null,
                            restrictionPanel,
                            "Restriction options",
                            JOptionPane.DEFAULT_OPTION);
                    if (result == JOptionPane.OK_OPTION) {
                      quantifier = quantifierField.getText();
                      value = valueField.getText();
                    }

                    addTriple(
                        (AnnotationNode) source,
                        (AnnotationNode) target,
                        null,
                        controller.getSelectionManager().getActiveProfile(),
                        property,
                        propertyID,
                        quantifier,
                        value);
                  }

                  getModel().remove(edge);
                }
              }

              reDrawGraph();
            }
          });

      addListener(mxEvent.MOVE_CELLS, (sender, evt) -> reDrawGraph());

      addListener(mxEvent.REMOVE_CELLS, (sender, evt) -> reDrawGraph());

      getSelectionModel()
          .addListener(
              mxEvent.CHANGE,
              (sender, evt) -> {
                Collection selectedCells = (Collection) evt.getProperty("removed");
                Collection deselectedCells = (Collection) evt.getProperty("added");
                if (deselectedCells != null && deselectedCells.size() > 0) {
                  for (Object cell : deselectedCells) {
                    setCellStyles(mxConstants.STYLE_STROKEWIDTH, "0", new Object[] {cell});
                  }
                  //                Arrays.stream(graph.getChildVertices(graph.getDefaultParent()))
                  //                    .forEach(
                  //                        cell ->
                  //                            graph.setCellStyles(
                  //                                mxConstants.STYLE_STROKEWIDTH, "0", new Object[]
                  // {cell}));
                  reDrawGraph();
                }

                if (selectedCells != null && selectedCells.size() > 0) {
                  for (Object cell : selectedCells) {
                    if (cell instanceof AnnotationNode) {
                      Annotation annotation = ((AnnotationNode) cell).getAnnotation();

                      controller.getSelectionManager().setSelectedAnnotation(annotation, null);

                      setCellStyles(mxConstants.STYLE_STROKEWIDTH, "4", new Object[] {cell});

                    } else if (cell instanceof Triple) {
                      Object value = ((mxCell) cell).getValue();
                      if (value instanceof OWLObjectProperty) {
                        controller
                            .getSelectionManager()
                            .setSelectedOWLObjectProperty((OWLObjectProperty) value);
                      }
                    }
                  }
                  reDrawGraph();
                }
              });
      areListenersSet = true;
    }
  }

  private void setVertexStyle(AnnotationNode vertex) {
    mxGeometry g = vertex.getGeometry();
    g.setHeight(g.getHeight() + 200);
    g.setWidth(g.getWidth() + 200);

    OWLClass owlClass = vertex.getAnnotation().getOwlClass();
    Profile profile = controller.getSelectionManager().getActiveProfile();
    String color =
        Integer.toHexString(
                profile.getColor(owlClass, vertex.getAnnotation().getOwlClassID()).getRGB())
            .substring(2);
    String shape = mxConstants.SHAPE_RECTANGLE;

    setCellStyles(mxConstants.STYLE_SHAPE, shape, new Object[] {vertex});
    setCellStyles(mxConstants.STYLE_FILLCOLOR, color, new Object[] {vertex});
  }

  /*
  GETTERS, CHECKERS, SETTERS
   */

  public AnnotationNode containsVertexCorrespondingToAnnotation(Annotation selectedAnnotation) {
    for (Object o : getChildVertices(getDefaultParent())) {
      if (o instanceof AnnotationNode
          && ((AnnotationNode) o).getAnnotation().equals(selectedAnnotation)) {
        return (AnnotationNode) o;
      }
    }
    return null;
  }

  public List<Object> getVerticesForAnnotation(Annotation annotation) {
    return Arrays.stream(getChildVertices(getDefaultParent()))
        .filter(
            o ->
                o instanceof AnnotationNode
                    && annotation.equals(((AnnotationNode) o).getAnnotation()))
        .collect(Collectors.toList());
  }

  @Override
  public TextSource getTextSource() {
    return textSource;
  }

  void reassignProperty(OWLEntity oldProperty, OWLEntity newProperty) {
    log.warn(String.format("Old Property: %s, New Property: %s", oldProperty, newProperty));

    List<Triple> edges = getTriplesCorrespondingToProperty(oldProperty);
    edges.forEach(edge -> edge.setValue(newProperty));
    reDrawGraph();
  }

  private List<Triple> getTriplesCorrespondingToProperty(OWLEntity property) {
    List<Triple> edges = new ArrayList<>();
    Arrays.stream(getChildEdges(getDefaultParent()))
        .forEach(
            edge -> {
              if (((Triple) edge).getValue() == property) {
                edges.add((Triple) edge);
              }
            });

    return edges;
  }

  @Override
  public String toString() {
    return id;
  }

  public int getGraphTextStart() {
    return graphTextStart;
  }

  public int getGraphTextEnd() {
    return graphTextEnd;
  }

  public void setGraphText(int start, int end) {
    graphTextStart = start;
    graphTextEnd = end;

    listeners.forEach(listener -> listener.graphTextChanged(textSource, start, end));
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void handleChange(OWLModelManagerChangeEvent event) {
    if (event.isType(EventType.ENTITY_RENDERER_CHANGED)) {
      Arrays.stream(getChildEdges(getDefaultParent()))
          .forEach(
              edge -> {
                if (edge instanceof Triple) {
                  ((Triple) edge).resetValue();
                }
              });
    }
  }

  @Override
  public void selectedAnnotationChanged(AnnotationChangeEvent e) {
    if (e.getNew() != null) {
      setSelectionCells(getVerticesForAnnotation(e.getNew()));
    } else {
      setSelectionCell(null);
    }
  }
}
