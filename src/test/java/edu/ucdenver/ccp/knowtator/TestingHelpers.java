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

package edu.ucdenver.ccp.knowtator;

import com.google.common.io.Files;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.object.TextSource;
import edu.ucdenver.ccp.knowtator.view.actions.AbstractKnowtatorAction;
import edu.ucdenver.ccp.knowtator.view.actions.ActionUnperformableException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/** The type Testing helpers. */
public class TestingHelpers {

  /** The constant projectFileName. */
  public static final String projectFileName = "test_project";

  /** The constant defaultExpectedTextSources. */
  public static final int defaultExpectedTextSources = 3;

  /** The constant defaultExpectedConceptAnnotations. */
  public static final int defaultExpectedConceptAnnotations = 6;

  /** The constant defaultExpectedSpans. */
  public static final int defaultExpectedSpans = 7;

  /** The constant defaultExpectedGraphSpaces. */
  public static final int defaultExpectedGraphSpaces = 3;

  /** The constant defaultExpectedProfiles. */
  public static final int defaultExpectedProfiles = 2;

  /** The constant defaultExpectedHighlighters. */
  public static final int defaultExpectedHighlighters = 3;

  /** The constant defaultExpectedAnnotationNodes. */
  public static final int defaultExpectedAnnotationNodes = 7;

  /** The constant defaultExpectedTriples. */
  public static final int defaultExpectedTriples = 4;

  /**
   * Gets project file.
   *
   * @param projectName the project name
   * @return the project file
   */
  static File getProjectFile(String projectName) {
    return new File(
        TestingHelpers.class
            .getResource(String.format("/%s/%s.knowtator", projectName, projectName))
            .getFile());
  }

  /**
   * Gets article file.
   *
   * @param projectName the project name
   * @param articleName the article name
   * @return the article file
   */
  public static File getArticleFile(String projectName, String articleName) {
    return new File(
        TestingHelpers.class
            .getResource(String.format("/%s/Articles/%s.txt", projectName, articleName))
            .getFile());
  }

  /**
   * Gets loaded model.
   *
   * @return the loaded model
   * @throws IOException the io exception
   */
  public static KnowtatorModel getLoadedModel() throws IOException {
    File projectDirectory = getProjectFile(projectFileName).getParentFile();
    File tempProjectDir = Files.createTempDir();
    FileUtils.copyDirectory(projectDirectory, tempProjectDir);
    KnowtatorModel model = new KnowtatorModel(tempProjectDir, null);
    model.load();

    return model;
  }

  /**
   * Check default collection values.
   *
   * @param controller the controller
   */
  public static void checkDefaultCollectionValues(KnowtatorModel controller) {
    TestingHelpers.countCollections(
        controller,
        defaultExpectedTextSources,
        defaultExpectedConceptAnnotations,
        defaultExpectedSpans,
        defaultExpectedGraphSpaces,
        defaultExpectedProfiles,
        defaultExpectedHighlighters,
        defaultExpectedAnnotationNodes,
        defaultExpectedTriples);
  }

  private static void countCollections(
      KnowtatorModel controller,
      int expectedTextSources,
      int expectedConceptAnnotations,
      int expectedSpans,
      int expectedGraphSpaces,
      int expectedProfiles,
      int expectedHighlighters,
      int expectedAnnotationNodes,
      int expectedTriples) {

    int actualTextSources = controller.getNumberOfTextSources();
    int actualConceptAnnotations =
        controller.getTextSources().stream()
            .mapToInt(TextSource::getNumberOfConceptAnnotations)
            .sum();
    int actualSpans =
        controller.getTextSources().stream()
            .mapToInt(textSource -> textSource.getSpans(null).size())
            .sum();

    assert actualTextSources == expectedTextSources
        : String.format(
            "There were %d text sources instead of %d", actualTextSources, expectedTextSources);
    assert actualConceptAnnotations == expectedConceptAnnotations
        : String.format(
            "There were %d concept annotations instead of %d",
            actualConceptAnnotations, expectedConceptAnnotations);
    assert actualSpans == expectedSpans
        : String.format("There were %d spans instead of %d", actualSpans, expectedSpans);

    int actualProfiles = controller.getNumberOfProfiles();
    assert actualProfiles == expectedProfiles
        : String.format("There were %d profiles instead of %d", actualProfiles, expectedProfiles);

    int actualHighlighters =
        controller.getProfiles().stream().mapToInt(profile -> profile.getColors().size()).sum();
    assert actualHighlighters == expectedHighlighters
        : String.format(
            "There were %d highlighters instead of %d", actualHighlighters, expectedHighlighters);

    int actualGraphSpaces =
        controller.getTextSources().stream().mapToInt(TextSource::getNumberOfGraphSpaces).sum();
    assert actualGraphSpaces == expectedGraphSpaces
        : String.format(
            "There were %d graph spaces instead of %d", actualGraphSpaces, expectedGraphSpaces);

    int actualAnnotationNodes =
        controller.getTextSources().stream()
            .mapToInt(
                textSource ->
                    textSource.getGraphSpaces().stream()
                        .mapToInt(
                            graphSpace1 ->
                                graphSpace1.getChildVertices(graphSpace1.getDefaultParent()).length)
                        .sum())
            .sum();
    assert actualAnnotationNodes == expectedAnnotationNodes
        : String.format(
            "There were %d vertices instead of %d", actualAnnotationNodes, expectedAnnotationNodes);

    int actualTriples =
        controller.getTextSources().stream()
            .mapToInt(
                textSource ->
                    textSource.getGraphSpaces().stream()
                        .mapToInt(
                            graphSpace1 ->
                                graphSpace1.getChildEdges(graphSpace1.getDefaultParent()).length)
                        .sum())
            .sum();
    assert actualTriples == expectedTriples
        : String.format("There were %d triples instead of %d", actualTriples, expectedTriples);
  }

  /**
   * Test owl action.
   *
   * @param controller the controller
   * @param changes the changes
   * @param expectedTextSources the expected text sources
   * @param expectedConceptAnnotations the expected concept annotations
   * @param expectedSpans the expected spans
   * @param expectedGraphSpaces the expected graph spaces
   * @param expectedProfiles the expected profiles
   * @param expectedHighlighters the expected highlighters
   * @param expectedAnnotationNodes the expected annotation nodes
   * @param expectedTriples the expected triples
   */
  public static void testOwlAction(
      KnowtatorModel controller,
      List<? extends OWLOntologyChange> changes,
      int expectedTextSources,
      int expectedConceptAnnotations,
      int expectedSpans,
      int expectedGraphSpaces,
      int expectedProfiles,
      int expectedHighlighters,
      int expectedAnnotationNodes,
      int expectedTriples) {
    TestingHelpers.checkDefaultCollectionValues(controller);
    controller.getOwlOntologyManager().applyChanges(changes);
    TestingHelpers.countCollections(
        controller,
        expectedTextSources,
        expectedConceptAnnotations,
        expectedSpans,
        expectedGraphSpaces,
        expectedProfiles,
        expectedHighlighters,
        expectedAnnotationNodes,
        expectedTriples);
  }

  /**
   * Test knowtator action.
   *
   * @param controller the controller
   * @param action the action
   * @param expectedTextSources the expected text sources
   * @param expectedConceptAnnotations the expected concept annotations
   * @param expectedSpans the expected spans
   * @param expectedGraphSpaces the expected graph spaces
   * @param expectedProfiles the expected profiles
   * @param expectedHighlighters the expected highlighters
   * @param expectedAnnotationNodes the expected annotation nodes
   * @param expectedTriples the expected triples
   * @throws ActionUnperformableException the action unperformable exception
   */
  public static void testKnowtatorAction(
      KnowtatorModel controller,
      AbstractKnowtatorAction action,
      int expectedTextSources,
      int expectedConceptAnnotations,
      int expectedSpans,
      int expectedGraphSpaces,
      int expectedProfiles,
      int expectedHighlighters,
      int expectedAnnotationNodes,
      int expectedTriples)
      throws ActionUnperformableException {
    TestingHelpers.checkDefaultCollectionValues(controller);
    controller.registerAction(action);
    TestingHelpers.countCollections(
        controller,
        expectedTextSources,
        expectedConceptAnnotations,
        expectedSpans,
        expectedGraphSpaces,
        expectedProfiles,
        expectedHighlighters,
        expectedAnnotationNodes,
        expectedTriples);
    controller.undo();
    TestingHelpers.checkDefaultCollectionValues(controller);
    controller.redo();
    TestingHelpers.countCollections(
        controller,
        expectedTextSources,
        expectedConceptAnnotations,
        expectedSpans,
        expectedGraphSpaces,
        expectedProfiles,
        expectedHighlighters,
        expectedAnnotationNodes,
        expectedTriples);
    controller.undo();
  }
}
