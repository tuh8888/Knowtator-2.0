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

package edu.ucdenver.ccp.knowtator.view.actions;

import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.ADD;
import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.REMOVE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.ucdenver.ccp.knowtator.TestingHelpers;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.object.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.object.TextSource;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.ConceptAnnotationAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.GraphSpaceAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.ProfileAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.SpanAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.TextSourceAction;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KnowtatorCollectionActionsTests {

  private KnowtatorModel model;

  @BeforeEach
  void setup() throws IOException {
    model = TestingHelpers.getLoadedModel();
  }

  @Test
  public void addTextSourceActionTest() throws ActionUnperformable {
    TestingHelpers.testKnowtatorAction(
        model,
        new TextSourceAction(
            model,
            ADD,
            TestingHelpers.getArticleFile(TestingHelpers.projectFileName, "document4"),
            null),
        TestingHelpers.defaultExpectedTextSources + 1,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void removeTextSourceActionTest() throws ActionUnperformable {
    TestingHelpers.testKnowtatorAction(
        model,
        new TextSourceAction(model, REMOVE, null, null),
        TestingHelpers.defaultExpectedTextSources - 1,
        TestingHelpers.defaultExpectedConceptAnnotations - 2,
        TestingHelpers.defaultExpectedSpans - 3,
        TestingHelpers.defaultExpectedGraphSpaces - 1,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes - 2,
        TestingHelpers.defaultExpectedTriples - 1,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void addConceptAnnotationActionTest() throws ActionUnperformable {
    TestingHelpers.testKnowtatorAction(
        model,
        new ConceptAnnotationAction(model, ADD, model.getSelectedTextSource().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations + 1,
        TestingHelpers.defaultExpectedSpans + 1,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void removeConceptAnnotationActionTest() throws ActionUnperformable {
    TextSource textSource = model.getSelectedTextSource().get();
    textSource.setSelectedConceptAnnotation(textSource.firstConceptAnnotation().get());
    TestingHelpers.testKnowtatorAction(
        model,
        new ConceptAnnotationAction(model, REMOVE, model.getSelectedTextSource().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations - 1,
        TestingHelpers.defaultExpectedSpans - 1,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes - 1,
        TestingHelpers.defaultExpectedTriples - 1,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void addSpanActionTest() throws ActionUnperformable {
    TextSource textSource = model.getSelectedTextSource().get();
    textSource.setSelectedConceptAnnotation(textSource.firstConceptAnnotation().get());
    TestingHelpers.testKnowtatorAction(
        model,
        new SpanAction(
            model, ADD, model.getSelectedTextSource().get().getSelectedAnnotation().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans + 1,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void removeSpanActionTest() throws ActionUnperformable {
    TextSource textSource = model.getSelectedTextSource().get();
    // First test remove span if there is only one in the collection. This should be equivalent to
    // just removing the annotation
    ConceptAnnotation conceptAnnotation = textSource.firstConceptAnnotation().get();
    textSource.setSelectedConceptAnnotation(conceptAnnotation);
    conceptAnnotation.setSelection(conceptAnnotation.first().get());
    TestingHelpers.testKnowtatorAction(
        model,
        new SpanAction(
            model, REMOVE, model.getSelectedTextSource().get().getSelectedAnnotation().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations - 1,
        TestingHelpers.defaultExpectedSpans - 1,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes - 1,
        TestingHelpers.defaultExpectedTriples - 1,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);

    // Next test remove span if there are multiple spans. This should only remove the span.
    conceptAnnotation.setSelection(conceptAnnotation.first().get());
    textSource.selectNextSpan();
    conceptAnnotation = textSource.getSelectedAnnotation().get();
    conceptAnnotation.setSelection(conceptAnnotation.first().get());
    TestingHelpers.testKnowtatorAction(
        model,
        new SpanAction(
            model, REMOVE, model.getSelectedTextSource().get().getSelectedAnnotation().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans - 1,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void addProfileActionTest() throws ActionUnperformable {
    TestingHelpers.testKnowtatorAction(
        model,
        new ProfileAction(model, ADD, "I'm new here"),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles + 1,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void removeProfileActionTest() throws ActionUnperformable {
    model.getProfiles().setSelection(model.getProfile("profile1").get());
    TestingHelpers.testKnowtatorAction(
        model,
        new ProfileAction(model, REMOVE, null),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations - 2,
        TestingHelpers.defaultExpectedSpans - 3,
        TestingHelpers.defaultExpectedGraphSpaces,
        TestingHelpers.defaultExpectedProfiles - 1,
        TestingHelpers.defaultExpectedHighlighters - 2,
        TestingHelpers.defaultExpectedAnnotationNodes - 2,
        TestingHelpers.defaultExpectedTriples - 2,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);

    assertThrows(ActionUnperformable.class, this::removeDefaultProfileActionTest);
  }

  @SuppressWarnings("unused")
  void removeDefaultProfileActionTest() throws ActionUnperformable {
    // It should not be possible to remove the default profile
    model.getProfiles().setSelection(model.getDefaultProfile());
    new ProfileAction(model, REMOVE, null).execute();
  }

  @Test
  public void addGraphSpaceActionTest() throws ActionUnperformable {
    TestingHelpers.testKnowtatorAction(
        model,
        new GraphSpaceAction(model, ADD, "new_graph_space", model.getSelectedTextSource().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans,
        TestingHelpers.defaultExpectedGraphSpaces + 1,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes,
        TestingHelpers.defaultExpectedTriples,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  @Test
  public void removeGraphSpaceActionTest() throws ActionUnperformable {
    TextSource textSource = model.getSelectedTextSource().get();
    textSource.selectNextGraphSpace();
    TestingHelpers.testKnowtatorAction(
        model,
        new GraphSpaceAction(model, REMOVE, null, model.getSelectedTextSource().get()),
        TestingHelpers.defaultExpectedTextSources,
        TestingHelpers.defaultExpectedConceptAnnotations,
        TestingHelpers.defaultExpectedSpans,
        TestingHelpers.defaultExpectedGraphSpaces - 1,
        TestingHelpers.defaultExpectedProfiles,
        TestingHelpers.defaultExpectedHighlighters,
        TestingHelpers.defaultExpectedAnnotationNodes - 2,
        TestingHelpers.defaultExpectedTriples - 1,
        TestingHelpers.defaultExpectedStructureAnnotations,
        TestingHelpers.defaultExpectedStructureGraphSpaces,
        TestingHelpers.defaultExpectedStructureAnnotationNodes,
        TestingHelpers.defaultExpectedStructureTriples);
  }

  public void setModel(KnowtatorModel model) {
    this.model = model;
  }
}
