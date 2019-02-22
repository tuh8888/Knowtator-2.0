/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.model.object;

import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXmlUtil;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.ModelListener;
import edu.ucdenver.ccp.knowtator.model.Savable;
import edu.ucdenver.ccp.knowtator.model.collection.ConceptAnnotationCollection;
import edu.ucdenver.ccp.knowtator.model.collection.GraphSpaceCollection;
import edu.ucdenver.ccp.knowtator.model.collection.SpanCollection;
import edu.ucdenver.ccp.knowtator.model.collection.event.ChangeEvent;
import edu.ucdenver.ccp.knowtator.model.collection.event.SelectionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import javax.swing.JFileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/** The type Text source. */
public class TextSource implements ModelObject<TextSource>, Savable, ModelListener {
  @SuppressWarnings("unused")
  private static Logger log = LogManager.getLogger(TextSource.class);

  private final KnowtatorModel model;
  private final File saveFile;
  private final ConceptAnnotationCollection conceptAnnotationCollection;
  private File textFile;
  private String content;
  private final GraphSpaceCollection graphSpaceCollection;
  private String id;

  /**
   * Instantiates a new Text source.
   *
   * @param model the model
   * @param saveFile the save file
   * @param textFileName the text file name
   */
  public TextSource(KnowtatorModel model, File saveFile, String textFileName) {
    this.model = model;
    this.saveFile =
        saveFile == null
            ? new File(
                model.getAnnotationsLocation().getAbsolutePath(),
                String.format("%s.xml", textFileName.replace(".txt", "")))
            : saveFile;
    this.conceptAnnotationCollection = new ConceptAnnotationCollection(model, this);
    this.graphSpaceCollection = new GraphSpaceCollection(model);
    model.addModelListener(this);

    model.verifyId(FilenameUtils.getBaseName(textFileName), this, true);

    textFile =
        new File(
            model.getArticlesLocation(),
            textFileName.endsWith(".txt") ? textFileName : String.format("%s.txt", textFileName));

    if (!textFile.exists()) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle(
          String.format("Could not find file for %s. Choose file location", id));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try {
          textFile =
              Files.copy(
                      Paths.get(file.toURI()),
                      Paths.get(model.getArticlesLocation().toURI().resolve(file.getName())))
                  .toFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Gets text file.
   *
   * @return the text file
   */
  public File getTextFile() {
    return textFile;
  }

  /**
   * Gets concept annotations.
   *
   * @return the concept annotations
   */
  public ConceptAnnotationCollection getConceptAnnotations() {
    return conceptAnnotationCollection;
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
  public void dispose() {
    conceptAnnotationCollection.dispose();
    graphSpaceCollection.dispose();
  }

  @Override
  public KnowtatorModel getKnowtatorModel() {
    return model;
  }

  @Override
  public String toString() {
    return id;
  }

  /**
   * Gets content.
   *
   * @return the content
   */
  public String getContent() {
    if (content == null) {
      while (true) {
        try {
          content = FileUtils.readFileToString(textFile, "UTF-8");
          return content;
        } catch (IOException e) {
          textFile = new File(model.getArticlesLocation(), String.format("%s.txt", id));
          while (!textFile.exists()) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
              textFile = fileChooser.getSelectedFile();
            }
          }
        }
      }
    } else {
      return content;
    }
  }

  /**
   * Gets graph spaces.
   *
   * @return the graph spaces
   */
  public GraphSpaceCollection getGraphSpaces() {
    return graphSpaceCollection;
  }

  @Override
  public void save() {
    KnowtatorXmlUtil xmlUtil = new KnowtatorXmlUtil();
    xmlUtil.writeFromTextSource(this);
  }

  /**
   * Gets save location.
   *
   * @return the save location
   */
  public File getSaveLocation() {
    return new File(model.getAnnotationsLocation().getAbsolutePath(), saveFile.getName());
  }

  @Override
  public void filterChangedEvent() {}

  @Override
  public void colorChangedEvent() {}

  @Override
  public void modelChangeEvent(ChangeEvent<ModelObject> event) {
    event
        .getNew()
        .filter(modelObject -> !(event instanceof SelectionEvent))
        .filter(modelObject -> modelObject instanceof TextBoundModelObject)
        .map(modelObject -> (TextBoundModelObject) modelObject)
        .filter(textBoundModelObject -> textBoundModelObject.getTextSource().equals(this))
        .ifPresent(modelObject -> save());
    event
        .getOld()
        .filter(modelObject -> !(event instanceof SelectionEvent))
        .filter(modelObject -> modelObject instanceof TextBoundModelObject)
        .map(modelObject -> (TextBoundModelObject) modelObject)
        .filter(textBoundModelObject -> textBoundModelObject.getTextSource().equals(this))
        .ifPresent(modelObject -> save());
  }

  /**
   * Gets selected annotation.
   *
   * @return the selected annotation
   */
  public Optional<ConceptAnnotation> getSelectedAnnotation() {
    return conceptAnnotationCollection.getSelection();
  }

  /**
   * Gets spans.
   *
   * @param loc the loc
   * @return the spans
   */
  public SpanCollection getSpans(Integer loc) {
    return conceptAnnotationCollection.getSpans(loc);
  }

  /** Select next span. */
  public void selectNextSpan() {
    conceptAnnotationCollection.selectNextSpan();
  }

  /** Select previous span. */
  public void selectPreviousSpan() {
    conceptAnnotationCollection.selectPreviousSpan();
  }

  /**
   * Gets annotation.
   *
   * @param annotationID the annotation id
   * @return the annotation
   */
  public Optional<ConceptAnnotation> getAnnotation(String annotationID) {
    return conceptAnnotationCollection.get(annotationID);
  }

  /**
   * Sets selected concept annotation.
   *
   * @param conceptAnnotation the concept annotation
   */
  public void setSelectedConceptAnnotation(ConceptAnnotation conceptAnnotation) {
    conceptAnnotationCollection.setSelection(conceptAnnotation);
  }

  /**
   * Add.
   *
   * @param graphSpace the graph space
   */
  public void add(GraphSpace graphSpace) {
    graphSpaceCollection.add(graphSpace);
  }

  /** Select previous graph space. */
  public void selectPreviousGraphSpace() {
    graphSpaceCollection.selectPrevious();
  }

  /** Select next graph space. */
  public void selectNextGraphSpace() {
    graphSpaceCollection.selectNext();
  }

  /**
   * Gets selected graph space.
   *
   * @return the selected graph space
   */
  public Optional<GraphSpace> getSelectedGraphSpace() {
    return graphSpaceCollection.getSelection();
  }

  /**
   * Contains id boolean.
   *
   * @param id the id
   * @return the boolean
   */
  public boolean containsID(String id) {
    return graphSpaceCollection.containsID(id);
  }

  /**
   * First concept annotation optional.
   *
   * @return the optional
   */
  public Optional<ConceptAnnotation> firstConceptAnnotation() {
    return conceptAnnotationCollection.first();
  }

  /**
   * Gets number of graph spaces.
   *
   * @return the number of graph spaces
   */
  public int getNumberOfGraphSpaces() {
    return graphSpaceCollection.size();
  }

  /**
   * Gets number of concept annotations.
   *
   * @return the number of concept annotations
   */
  public int getNumberOfConceptAnnotations() {
    return conceptAnnotationCollection.size();
  }

  /**
   * Sets selected graph space.
   *
   * @param graphSpace the graph space
   */
  public void setSelectedGraphSpace(GraphSpace graphSpace) {
    graphSpaceCollection.setSelection(graphSpace);
  }

  /** Select next concept annotation. */
  public void selectNextConceptAnnotation() {
    conceptAnnotationCollection.selectNext();
  }
}
