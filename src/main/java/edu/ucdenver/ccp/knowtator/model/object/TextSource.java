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
import edu.ucdenver.ccp.knowtator.model.BaseModel;
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
  private final ConceptAnnotationCollection conceptAnnotations;
  private File textFile;
  private String content;
  private final GraphSpaceCollection graphSpaces;
  private String id;

  /**
   * Instantiates a new Text source.
   *
   * @param model the model
   * @param file save file or source text file
   */
  public TextSource(KnowtatorModel model, File file, String textSourceName) {
    this.model = model;
    if (file.getName().endsWith(".txt")) {
      this.saveFile = new File(BaseModel.getAnnotationsLocation(model.getProjectLocation()).getAbsolutePath(),
          file.getName().replace(".txt", ".xml"));
      this.textFile = file;
    } else if (file.getName().endsWith(".xml")) {
      this.saveFile = file;
      String textFileName = textSourceName == null ? file.getName().replace(".xml", ".txt") : textSourceName.endsWith(".txt") ? textSourceName : textSourceName.concat(".txt");
      this.textFile = new File(BaseModel.getArticlesLocation(file.getParentFile().getParentFile()).getAbsolutePath(), textFileName);
      if (!this.textFile.exists()) {
        this.textFile = new File(BaseModel.getArticlesLocation(model.getProjectLocation()).getAbsolutePath(), textFileName);
      }

    } else {
      this.saveFile = new File(BaseModel.getArticlesLocation(model.getProjectLocation()).getAbsolutePath(),
          file.getName().concat(".xml"));
      this.textFile = new File(BaseModel.getArticlesLocation(model.getProjectLocation()).getAbsolutePath(),
          file.getName().concat(".txt"));
    }
    this.conceptAnnotations = new ConceptAnnotationCollection(model, this);
    this.graphSpaces = new GraphSpaceCollection(model);
    model.addModelListener(this);

    this.id = model.verifyId(FilenameUtils.getBaseName(this.textFile.getName().replace(".txt", "")), this, true);

    if (!textFile.exists()) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle(
          String.format("Could not find file for %s. Choose file location", id));

      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        File file1 = fileChooser.getSelectedFile();
        try {
          textFile =
              Files.copy(
                      Paths.get(file.toURI()),
                      Paths.get(BaseModel.getArticlesLocation(model.getProjectLocation()).toURI().resolve(file1.getName())))
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
    return conceptAnnotations;
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
    conceptAnnotations.dispose();
    graphSpaces.dispose();
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
          textFile = new File(BaseModel.getArticlesLocation(model.getProjectLocation()), String.format("%s.txt", id));
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
    return graphSpaces;
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
    return new File(BaseModel.getAnnotationsLocation(model.getProjectLocation()).getAbsolutePath(), saveFile.getName());
  }

  @Override
  public void filterChangedEvent() {}

  @Override
  public void colorChangedEvent(Profile profile) {}

  @Override
  public void modelChangeEvent(ChangeEvent<ModelObject> event) {
    event
        .getNew()
        .filter(modelObject -> !(event instanceof SelectionEvent))
        .filter(modelObject -> modelObject instanceof TextBoundModelObject)
        .map(modelObject -> (TextBoundModelObject) modelObject)
        .filter(textBoundModelObject -> textBoundModelObject.getTextSource().equals(this))
        .ifPresent(
            modelObject -> save());
    event
        .getOld()
        .filter(modelObject -> !(event instanceof SelectionEvent))
        .filter(modelObject -> modelObject instanceof TextBoundModelObject)
        .map(modelObject -> (TextBoundModelObject) modelObject)
        .filter(textBoundModelObject -> textBoundModelObject.getTextSource().equals(this))
        .ifPresent(
            modelObject -> save());
  }

  /**
   * Gets selected annotation.
   *
   * @return the selected annotation
   */
  public Optional<ConceptAnnotation> getSelectedAnnotation() {
    return conceptAnnotations.getSelection();
  }

  /**
   * Gets spans.
   *
   * @param loc the loc
   * @return the spans
   */
  public SpanCollection getSpans(Integer loc) {
    return conceptAnnotations.getSpans(loc);
  }

  /** Select next span. */
  public void selectNextSpan() {
    conceptAnnotations.selectNextSpan();
  }

  /** Select previous span. */
  public void selectPreviousSpan() {
    conceptAnnotations.selectPreviousSpan();
  }

  /**
   * Gets annotation.
   *
   * @param annotationID the annotation id
   * @return the annotation
   */
  public Optional<ConceptAnnotation> getAnnotation(String annotationID) {
    return conceptAnnotations.get(annotationID);

  }

  /**
   * Sets selected concept annotation.
   *
   * @param conceptAnnotation the concept annotation
   */
  public void setSelectedConceptAnnotation(ConceptAnnotation conceptAnnotation) {
    conceptAnnotations.setSelection(conceptAnnotation);
  }

  /**
   * Add.
   *
   * @param graphSpace the graph space
   */
  public void add(GraphSpace graphSpace) {
    graphSpaces.add(graphSpace);
  }

  /** Select previous graph space. */
  public void selectPreviousGraphSpace() {
    graphSpaces.selectPrevious();
  }

  /** Select next graph space. */
  public void selectNextGraphSpace() {
    graphSpaces.selectNext();
  }

  /**
   * Gets selected graph space.
   *
   * @return the selected graph space
   */
  public Optional<GraphSpace> getSelectedGraphSpace() {
    return graphSpaces.getSelection();
  }

  /**
   * Contains id boolean.
   *
   * @param id the id
   * @return the boolean
   */
  public boolean containsID(String id) {
    return graphSpaces.containsID(id);
  }

  /**
   * First concept annotation optional.
   *
   * @return the optional
   */
  public Optional<ConceptAnnotation> firstConceptAnnotation() {
    return conceptAnnotations.first();
  }

  /**
   * Gets number of graph spaces.
   *
   * @return the number of graph spaces
   */
  public int getNumberOfGraphSpaces() {
    return graphSpaces.size();
  }

  /**
   * Gets number of concept annotations.
   *
   * @return the number of concept annotations
   */
  public int getNumberOfConceptAnnotations() {
    return conceptAnnotations.size();
  }

  /**
   * Sets selected graph space.
   *
   * @param graphSpace the graph space
   */
  public void setSelectedGraphSpace(GraphSpace graphSpace) {
    graphSpaces.setSelection(graphSpace);
  }

  /** Select next concept annotation. */
  public void selectNextConceptAnnotation() {
    conceptAnnotations.selectNext();
  }
}
