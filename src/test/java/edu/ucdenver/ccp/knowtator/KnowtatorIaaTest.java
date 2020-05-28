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
import edu.ucdenver.ccp.knowtator.iaa.IaaException;
import edu.ucdenver.ccp.knowtator.iaa.KnowtatorIaa;
import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KnowtatorIaaTest {

  private static KnowtatorIaa knowtatorIAA;
  private static File outputDir;
  private static File goldStandardDir;
  static KnowtatorModel controller;
  private final int defaultExpectedTextSources = 4;
  private final int defaultExpectedConceptAnnotations = 456;
  private final int defaultExpectedGraphSpaces = 4;
  private final int defaultExpectedSpans = 456;
  private final int defaultExpectedProfiles = 3;
  private final int defaultExpectedHighlighters = 0;
  private final int defaultExpectedAnnotationNodes = 0;
  private final int defaultExpectedTriples = 0;
  private final int defaultExpectedStructureAnnotations = 0;

  @BeforeAll
  static void makeProjectTest() throws IaaException, IOException {
    String projectFileName = "iaa_test_project";
    File projectDirectory = TestingHelpers.getProjectFile(projectFileName).getParentFile();
    File tempProjectDir = Files.createTempDir();
    FileUtils.copyDirectory(projectDirectory, tempProjectDir);

    controller = new KnowtatorModel(tempProjectDir, null);
    controller.load();

    goldStandardDir = new File(controller.getProjectLocation(), "iaa");

    outputDir = new File(controller.getProjectLocation(), "iaa_results");

    boolean created = outputDir.mkdir();
    if (created) {
      knowtatorIAA = new KnowtatorIaa(outputDir, controller);
    }
  }

  @Test
  void runClassIaaTest() throws IOException, IaaException {
    TestingHelpers.countCollections(
        controller,
        defaultExpectedTextSources,
        defaultExpectedConceptAnnotations,
        defaultExpectedSpans,
        defaultExpectedGraphSpaces,
        defaultExpectedProfiles,
        defaultExpectedHighlighters,
        defaultExpectedAnnotationNodes,
        defaultExpectedTriples,
        defaultExpectedStructureAnnotations);
    knowtatorIAA.runClassIaa();
    // TODO: Rerun test data because concept annotations no longer store owl class label

    try {
      assert FileUtils.contentEqualsIgnoreEOL(
          new File(outputDir, "Class matcher.dat"),
          new File(goldStandardDir, "Class matcher.dat"),
          "utf-8");
    } catch (AssertionError e) {
      System.out.println(String.format("Gold: %s\nThis: %s\n",
          new File(goldStandardDir, "index.html").getAbsolutePath(),
          new File(outputDir, "index.html").getAbsolutePath()));
      throw e;
    }
  }

  @Test
  void runSpanIaaTest() throws IaaException, IOException {
    TestingHelpers.countCollections(
        controller,
        defaultExpectedTextSources,
        defaultExpectedConceptAnnotations,
        defaultExpectedSpans,
        defaultExpectedGraphSpaces,
        defaultExpectedProfiles,
        defaultExpectedHighlighters,
        defaultExpectedAnnotationNodes,
        defaultExpectedTriples,
        defaultExpectedStructureAnnotations);
    knowtatorIAA.runSpanIaa();
    assert FileUtils.contentEqualsIgnoreEOL(
        new File(outputDir, "Span matcher.dat"),
        new File(goldStandardDir, "Span matcher.dat"),
        "utf-8");
  }

  @Test
  void runClassAndSpanIaaTest() throws IaaException, IOException {
    TestingHelpers.countCollections(
        controller,
        defaultExpectedTextSources,
        defaultExpectedConceptAnnotations,
        defaultExpectedSpans,
        defaultExpectedGraphSpaces,
        defaultExpectedProfiles,
        defaultExpectedHighlighters,
        defaultExpectedAnnotationNodes,
        defaultExpectedTriples,
        defaultExpectedStructureAnnotations);
    knowtatorIAA.runClassAndSpanIaa();

    assert FileUtils.contentEqualsIgnoreEOL(
        new File(outputDir, "Class and span matcher.dat"),
        new File(goldStandardDir, "Class and span matcher.dat"),
        "utf-8");
  }
}
