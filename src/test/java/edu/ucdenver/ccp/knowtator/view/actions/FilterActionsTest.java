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

import static edu.ucdenver.ccp.knowtator.model.FilterType.OWLCLASS;
import static edu.ucdenver.ccp.knowtator.model.FilterType.PROFILE;

import edu.ucdenver.ccp.knowtator.TestingHelpers;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.object.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.object.TextSource;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.FilterAction;
import edu.ucdenver.ccp.knowtator.view.actions.modelactions.ReassignOwlClassAction;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilterActionsTest {
  private static KnowtatorModel model;

  @BeforeAll
  static void setup() {
    try {
      model = TestingHelpers.getLoadedModel();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void filterActionTest() throws ActionUnperformable {
    TestingHelpers.checkDefaultCollectionValues(model);
    TestingHelpers.testKnowtatorAction(model, new FilterAction(model, PROFILE, true),
        TestingHelpers.defaultCounts.copy(0, -2, -3, 0, 0, 0, 0, 0, 0));
    TestingHelpers.testKnowtatorAction(model, new FilterAction(model, PROFILE, false),
        TestingHelpers.defaultCounts);

    model.getTextSources().setSelection(model.getTextSources().get("document1").get());
    TextSource textSource = model.getSelectedTextSource().get();
    ConceptAnnotation conceptAnnotation = textSource.firstConceptAnnotation().get();
    textSource.setSelectedConceptAnnotation(conceptAnnotation);
    model.registerAction(
        new ReassignOwlClassAction(model, conceptAnnotation, model.getSelectedOwlClass().get()));

    TestingHelpers.testKnowtatorAction(
        model,
        new FilterAction(model, OWLCLASS, true),
        TestingHelpers.defaultCounts.copy(0, -4, -5, 0, 0, 0, 0, 0, 0));
    TestingHelpers.testKnowtatorAction(
        model,
        new FilterAction(model, OWLCLASS, false),
        TestingHelpers.defaultCounts);
  }
}
