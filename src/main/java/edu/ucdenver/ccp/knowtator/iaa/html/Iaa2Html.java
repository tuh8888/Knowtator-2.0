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

package edu.ucdenver.ccp.knowtator.iaa.html;

import edu.ucdenver.ccp.knowtator.iaa.AnnotationSpanIndex;
import edu.ucdenver.ccp.knowtator.iaa.Iaa;
import edu.ucdenver.ccp.knowtator.iaa.matcher.Matcher;
import edu.ucdenver.ccp.knowtator.model.object.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.model.object.Span;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** The type Iaa 2 html. */
@SuppressWarnings("Duplicates")
public class Iaa2Html {

  /**
   * Print iaa.
   *
   * @param iaa the iaa
   * @param matcher the matcher
   * @param directory the directory
   * @param numberOfDocs the number of docs
   * @throws Exception the exception
   */
  public static void printIaa(
      Iaa iaa,
      Matcher matcher,
      File directory,
      int numberOfDocs)
      throws Exception {
    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
    percentageFormat.setMinimumFractionDigits(2);

    String fileName = matcher.getName();

    PrintStream html =
        new PrintStream(new File(directory, String.format("%s.html", fileName)), "UTF-8");

    initHTML(html, matcher.getName(), null, null, matcher.getDescription());

    printIntro(html, iaa, numberOfDocs, fileName, matcher);
    html.println("<p>");
    printTitleRowForAllwayIAA(html, matcher);

    PrintStream tabular =
        new PrintStream(new File(directory, String.format("%s.dat", fileName)), "UTF-8");
    tabular.printf(
        "This file is provided to facilitate cut-n-paste into a spreadsheet.%n"
            + "If you cannot directly copy the data below into a spreadsheet "
            + "without it all going into a single cell,%n"
            + "then try copying to a text editor first "
            + "and then copy it from there.  "
            + "There is typically a 'paste special'%noption under the Edit menu that will "
            + "allow you to paste the copied data as text.  This will also work.%n%n%n%n");

    if (matcher.returnsTrivials())
      tabular.println(
          "type\t"
              + "matches\t"
              + "trivial matches\t"
              + "non-trivial matches\t"
              + "non-matches\t"
              + "trivial non-matches\t"
              + "non-trivial non-matches");
    else tabular.println("type\t" + "matches\t" + "non-matches");

    Set<String> classes = iaa.getAnnotationClasses();
    Set<String> sets = iaa.getSetNames();

    Map<String, Set<ConceptAnnotation>> allwayMatches = iaa.getAllwayMatches();
    Map<String, Set<ConceptAnnotation>> nontrivialAllwayMatches = iaa.getNontrivialAllwayMatches();
    Map<String, Set<ConceptAnnotation>> trivialAllwayMatches = iaa.getTrivialAllwayMatches();
    Map<String, Set<ConceptAnnotation>> allwayNonmatches = iaa.getAllwayNonmatches();
    Map<String, Set<ConceptAnnotation>> nontrivialAllwayNonmatches =
        iaa.getNontrivialAllwayNonmatches();
    Map<String, Set<ConceptAnnotation>> trivialAllwayNonmatches = iaa.getTrivialAllwayNonmatches();

    Set<ConceptAnnotation> allwayMatchesSingleSet = getSingleSet(allwayMatches);
    Set<ConceptAnnotation> trivialAllwayMatchesSingleSet = getSingleSet(trivialAllwayMatches);
    Set<ConceptAnnotation> nontrivialAllwayMatchesSingleSet = getSingleSet(nontrivialAllwayMatches);
    Set<ConceptAnnotation> allwayNonmatchesSingleSet = getSingleSet(allwayNonmatches);
    Set<ConceptAnnotation> trivialAllwayNonmatchesSingleSet = getSingleSet(trivialAllwayNonmatches);
    Set<ConceptAnnotation> nontrivialAllwayNonmatchesSingleSet =
        getSingleSet(nontrivialAllwayNonmatches);

    AnnotationSpanIndex spanIndex = new AnnotationSpanIndex(allwayNonmatchesSingleSet);

    int totalAllwayMatches = allwayMatchesSingleSet.size();
    int totalTrivialAllwayMatches = trivialAllwayMatchesSingleSet.size();
    int totalNontrivialAllwayMatches = nontrivialAllwayMatchesSingleSet.size();
    int totalAllwayNonmatches = allwayNonmatchesSingleSet.size();
    int totalTrivialAllwayNonmatches = trivialAllwayNonmatchesSingleSet.size();
    int totalNontrivialAllwayNonmatches = nontrivialAllwayNonmatchesSingleSet.size();

    double iaaScore =
        (double) totalAllwayMatches
            / ((double) totalAllwayMatches + (double) totalAllwayNonmatches);
    double stingyIAAScore =
        (double) totalNontrivialAllwayMatches
            / ((double) totalAllwayMatches + (double) totalAllwayNonmatches);
    double respectableIAAScore =
        (double) totalNontrivialAllwayMatches
            / ((double) totalNontrivialAllwayMatches + (double) totalAllwayNonmatches);
    double nontrivialIAAScore =
        (double) totalNontrivialAllwayMatches
            / ((double) totalNontrivialAllwayMatches + (double) totalNontrivialAllwayNonmatches);

    if (matcher.returnsTrivials()) {
      html.printf(
          "<tr><td><b>All classes</b></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td></tr>%n",
          percentageFormat.format(iaaScore),
          percentageFormat.format(stingyIAAScore),
          percentageFormat.format(respectableIAAScore),
          percentageFormat.format(nontrivialIAAScore),
          totalAllwayMatches,
          totalTrivialAllwayMatches,
          totalNontrivialAllwayMatches,
          totalAllwayNonmatches,
          totalTrivialAllwayNonmatches,
          totalNontrivialAllwayNonmatches);
      tabular.printf(
          "All classes\t%d\t%d\t%d\t%d\t%d\t%d%n",
          totalAllwayMatches,
          totalTrivialAllwayMatches,
          totalNontrivialAllwayMatches,
          totalAllwayNonmatches,
          totalTrivialAllwayNonmatches,
          totalNontrivialAllwayNonmatches);
    } else {
      html.printf(
          "<tr><td><b>All classes</b></td><td>%s</td><td>%d</td><td>%d</td></tr>%n",
          percentageFormat.format(iaaScore), totalAllwayMatches, totalAllwayNonmatches);
      tabular.printf("All classes\t%d\t%d%n", totalAllwayMatches, totalAllwayNonmatches);
    }

    Map<String, Set<ConceptAnnotation>> sortedAllwayMatches =
        sortByType(classes, allwayMatchesSingleSet);
    Map<String, Set<ConceptAnnotation>> sortedAllwayTrivialMatches =
        sortByType(classes, trivialAllwayMatchesSingleSet);
    Map<String, Set<ConceptAnnotation>> sortedAllwayNontrivialMatches =
        sortByType(classes, nontrivialAllwayMatchesSingleSet);
    Map<String, Set<ConceptAnnotation>> sortedAllwayNonmatches =
        sortByType(classes, allwayNonmatchesSingleSet);
    Map<String, Set<ConceptAnnotation>> sortedAllwayTrivialNonmatches =
        sortByType(classes, trivialAllwayNonmatchesSingleSet);
    Map<String, Set<ConceptAnnotation>> sortedAllwayNontrivialNonmatches =
        sortByType(classes, nontrivialAllwayNonmatchesSingleSet);

    List<String> sortedTypes = new ArrayList<>(classes);
    Collections.sort(sortedTypes);

    for (String type : sortedTypes) {
      int classMatches = sortedAllwayMatches.get(type).size();
      int classTrivialMatches = sortedAllwayTrivialMatches.get(type).size();
      int classNontrivialMatches = sortedAllwayNontrivialMatches.get(type).size();
      int classNonmatches = sortedAllwayNonmatches.get(type).size();
      int classTrivialNonmatches = sortedAllwayTrivialNonmatches.get(type).size();
      int classNontrivialNonmatches = sortedAllwayNontrivialNonmatches.get(type).size();

      iaaScore = (double) classMatches / ((double) classMatches + (double) classNonmatches);
      stingyIAAScore =
          (double) classNontrivialMatches / ((double) classMatches + (double) classNonmatches);
      respectableIAAScore =
          (double) classNontrivialMatches
              / ((double) classNontrivialMatches + (double) classNonmatches);
      nontrivialIAAScore =
          (double) classNontrivialMatches
              / ((double) classNontrivialMatches + (double) classNontrivialNonmatches);

      if (type.startsWith("<") && type.endsWith(">")) {
        type = type.substring(1, type.length()- 2);
      }
      if (matcher.returnsTrivials()) {
        html.printf(
            "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td><td>%d</td></tr>%n",
            type,
            percentageFormat.format(iaaScore),
            percentageFormat.format(stingyIAAScore),
            percentageFormat.format(respectableIAAScore),
            percentageFormat.format(nontrivialIAAScore),
            classMatches,
            classTrivialMatches,
            classNontrivialMatches,
            classNonmatches,
            classTrivialNonmatches,
            classNontrivialNonmatches);
        tabular.printf(
            "%s\t%d\t%d\t%d\t%d\t%d\t%d%n",
            type,
            classMatches,
            classTrivialMatches,
            classNontrivialMatches,
            classNonmatches,
            classTrivialNonmatches,
            classNontrivialNonmatches);
      } else {
        html.printf(
            "<tr><td>%s</td><td>%s</td><td>%d</td><td>%d</td></tr>%n",
            type,
            percentageFormat.format(iaaScore),
            classMatches,
            classNonmatches);
        tabular.printf(
            "%s\t%d\t%d%n",
            type,
            classMatches,
            classNonmatches);
      }
    }
    html.println("</table>");

    printMatchData(
        html,
        sets,
        fileName,
        directory,
        allwayMatches,
        classes,
        sortedTypes,
        matcher,
        trivialAllwayMatches,
        nontrivialAllwayMatches,
        iaa);

    printNonmatchData(
        html,
        sets,
        fileName,
        directory,
        allwayNonmatches,
        classes,
        spanIndex,
        sortedTypes,
        matcher,
        trivialAllwayNonmatches,
        nontrivialAllwayNonmatches);

    Map<String, Map<String, Set<ConceptAnnotation>>> pairwiseMatches = iaa.getPairwiseMatches();
    Map<String, Map<String, Set<ConceptAnnotation>>> pairwiseNonmatches =
        iaa.getPairwiseNonmatches();

    printPairwiseAgreement(html, sets, pairwiseMatches, pairwiseNonmatches, percentageFormat);

    html.flush();
    html.close();
    tabular.flush();
    tabular.close();
  }

  private static void printMatchData(
      PrintStream html,
      Set<String> sets,
      String fileName,
      File directory,
      Map<String, Set<ConceptAnnotation>> allwayMatches,
      Set<String> classes,
      List<String> sortedTypes,
      Matcher matcher,
      Map<String, Set<ConceptAnnotation>> trivialAllwayMatches,
      Map<String, Set<ConceptAnnotation>> nontrivialAllwayMatches,
      Iaa iaa)
      throws Exception {
    html.println("<h2>match data</h2>");
    html.println("<ul>");

    Map<ConceptAnnotation, Set<ConceptAnnotation>> matchSets = iaa.getAllwayMatchSets();

    for (String set : sets) {
      String matchesFileName = String.format("%s.matches.%s.html", fileName, set);
      html.printf("<li><a href=\"%s\">matches for %s</a></li>%n", matchesFileName, set);
      PrintStream matchesStream = new PrintStream(new File(directory, matchesFileName), "UTF-8");
      Set<ConceptAnnotation> matches = allwayMatches.get(set);
      Map<String, Set<ConceptAnnotation>> sortedMatches = sortByType(classes, matches);

      initHTML(
          matchesStream,
          String.format("Matches for %s", set),
          String.format("%s.html", fileName),
          fileName,
          "Each concept that was considered a match is shown in the text that it was found in.  The matching annotations from the other concept sets are also shown.");
      printInstances(
          matchesStream,
          sortedMatches,
          sortedTypes,
          matchSets);
      matchesStream.flush();
      matchesStream.close();

      if (matcher.returnsTrivials()) {
        String trivialMatchesFileName = String.format("%s.trivial.matches.%s.html", fileName, set);
        html.printf(
            "<li><a href=\"%s\">trivial matches for %s</a></li>%n", trivialMatchesFileName, set);
        PrintStream trivialMatchesStream =
            new PrintStream(new File(directory, trivialMatchesFileName), "UTF-8");
        Set<ConceptAnnotation> trivialMatches = trivialAllwayMatches.get(set);
        Map<String, Set<ConceptAnnotation>> sortedTrivialMatches =
            sortByType(classes, trivialMatches);
        initHTML(
            trivialMatchesStream,
            String.format("Trivial matches for %s", set),
            String.format("%s.html", fileName),
            fileName,
            "Each concept that was considered a trival match is shown in the text that it was found in.  The matching annotations from the other concept sets are also shown.");
        printInstances(
            trivialMatchesStream,
            sortedTrivialMatches,
            sortedTypes,
            matchSets);
        trivialMatchesStream.flush();
        trivialMatchesStream.close();

        String nontrivialMatchesFileName =
            String.format("%s.nontrivial.matches.%s.html", fileName, set);
        html.printf(
            "<li><a href=\"%s\">non-trivial matches for %s</a></li>%n",
            nontrivialMatchesFileName, set);
        PrintStream nontrivialMatchesStream =
            new PrintStream(new File(directory, nontrivialMatchesFileName), "UTF-8");
        Set<ConceptAnnotation> nontrivialMatches = nontrivialAllwayMatches.get(set);
        Map<String, Set<ConceptAnnotation>> sortedNontrivialMatches =
            sortByType(classes, nontrivialMatches);
        initHTML(
            nontrivialMatchesStream,
            String.format("non-trivial non-matches for %s", set),
            String.format("%s.html", fileName),
            fileName,
            "Each concept that was considered a non-trival match is shown in the text that it was found in.  The matching from the other concept sets are also shown.");
        printInstances(
            nontrivialMatchesStream,
            sortedNontrivialMatches,
            sortedTypes,
            matchSets);
        nontrivialMatchesStream.flush();
        nontrivialMatchesStream.close();
      }
    }
    html.println("</ul><hr>");
  }

  private static void printNonmatchData(
      PrintStream html,
      Set<String> sets,
      String fileName,
      File directory,
      Map<String, Set<ConceptAnnotation>> allwayNonmatches,
      Set<String> classes,
      AnnotationSpanIndex spanIndex,
      List<String> sortedTypes,
      Matcher matcher,
      Map<String, Set<ConceptAnnotation>> trivialAllwayNonmatches,
      Map<String, Set<ConceptAnnotation>> nontrivialAllwayNonmatches)
      throws Exception {
    html.println("<h2>non-match data</h2>");
    html.println("<ul>");

    for (String set : sets) {
      String errorsFileName = String.format("%s.nonmatches.%s.html", fileName, set);
      html.printf("<li><a href=\"%s\">non-matches for %s</a></li>%n", errorsFileName, set);
      PrintStream errors = new PrintStream(new File(directory, errorsFileName), "UTF-8");
      Set<ConceptAnnotation> nonmatches = allwayNonmatches.get(set);
      Map<String, Set<ConceptAnnotation>> sortedNonmatches = sortByType(classes, nonmatches);

      Map<ConceptAnnotation, Set<ConceptAnnotation>> comparisonAnnotations = new HashMap<>();
      for (ConceptAnnotation nonmatch : nonmatches) {
        comparisonAnnotations.put(nonmatch, getCandidateAnnotations(nonmatch, spanIndex));
      }

      initHTML(
          errors,
          String.format("Non-matches for %s", set),
          String.format("%s.html", fileName),
          fileName,
          "Each concept that was considered a non-match is shown in the text that it was found in.  Overlapping annotations from the other concept sets are also shown.");
      printInstances(
          errors,
          sortedNonmatches,
          sortedTypes,
          comparisonAnnotations);
      errors.flush();
      errors.close();

      if (matcher.returnsTrivials()) {
        String trivialNonMatchesFileName =
            String.format("%s.trivial.nonmatches.%s.html", fileName, set);
        html.printf(
            "<li><a href=\"%s\">trivial non-matches for %s</a></li>%n",
            trivialNonMatchesFileName, set);
        PrintStream trivialErrors =
            new PrintStream(new File(directory, trivialNonMatchesFileName), "UTF-8");
        Set<ConceptAnnotation> trivialNonmatches = trivialAllwayNonmatches.get(set);
        Map<String, Set<ConceptAnnotation>> sortedTrivialNonmatches =
            sortByType(classes, trivialNonmatches);
        initHTML(
            trivialErrors,
            String.format("Trivial non-matches for %s", set),
            String.format("%s.html", fileName),
            fileName,
            "Each concept that was considered a trival non-match is shown in the text that it was found in.  Overlapping annotations from the other concept sets are also shown.");
        printInstances(
            trivialErrors,
            sortedTrivialNonmatches,
            sortedTypes,
            comparisonAnnotations);
        trivialErrors.flush();
        trivialErrors.close();

        String nontrivialNonMatchesFileName =
            String.format("%s.nontrivial.nonmatches.%s.html", fileName, set);
        html.printf(
            "<li><a href=\"%s\">non-trivial non-matches for %s</a></li>%n",
            nontrivialNonMatchesFileName, set);
        PrintStream nontrivialErrors =
            new PrintStream(new File(directory, nontrivialNonMatchesFileName), "UTF-8");
        Set<ConceptAnnotation> nontrivialNonmatches = nontrivialAllwayNonmatches.get(set);
        Map<String, Set<ConceptAnnotation>> sortedNontrivialNonmatches =
            sortByType(classes, nontrivialNonmatches);
        initHTML(
            nontrivialErrors,
            String.format("non-trivial non-matches for %s", set),
            String.format("%s.html", fileName),
            fileName,
            "Each concept that was considered a non-trival non-match is shown in the text that it was found in.  Overlapping annotations from the other concept sets are also shown.");
        printInstances(
            nontrivialErrors,
            sortedNontrivialNonmatches,
            sortedTypes,
            comparisonAnnotations);
        nontrivialErrors.flush();
        nontrivialErrors.close();
      }
    }
    html.println("</ul><hr>");
  }

  private static Set<ConceptAnnotation> getCandidateAnnotations(
      ConceptAnnotation conceptAnnotation, AnnotationSpanIndex spanIndex) {
    Set<ConceptAnnotation> candidateConceptAnnotations = new HashSet<>();
    String set = conceptAnnotation.getAnnotator().getId();
    String docID = conceptAnnotation.getTextSource().getId();

    Set<ConceptAnnotation> overlappingConceptAnnotations =
        spanIndex.getOverlappingAnnotations(conceptAnnotation);
    for (ConceptAnnotation overlappingConceptAnnotation : overlappingConceptAnnotations) {
      String candidateAnnotationSet = overlappingConceptAnnotation.getAnnotator().getId();
      if (!candidateAnnotationSet.equals(set)) {
        String candidateDocID = overlappingConceptAnnotation.getTextSource().getId();
        if (candidateDocID.equals(docID)) {
          candidateConceptAnnotations.add(overlappingConceptAnnotation);
        }
      }
    }
    return candidateConceptAnnotations;
  }

  private static void printInstances(
      PrintStream out,
      Map<String, Set<ConceptAnnotation>> sortedAnnotations,
      List<String> sortedTypes,
      Map<ConceptAnnotation, Set<ConceptAnnotation>> comparisonAnnotations) {
    for (String type : sortedTypes) {

      out.printf("<h2>%s</h2>%n", type.startsWith("<") && type.endsWith(">") ? type.substring(1, type.length()- 1) : type);
      Set<ConceptAnnotation> typeConceptAnnotations = sortedAnnotations.get(type);
      for (ConceptAnnotation conceptAnnotation : typeConceptAnnotations) {
        writeAnnotationTextSourceHTML(out, conceptAnnotation);
        out.println("<ul><li>");
        printAnnotationHTML(out, conceptAnnotation);

        Set<ConceptAnnotation> comparisons = comparisonAnnotations.get(conceptAnnotation);
        if (comparisons != null) {
          for (ConceptAnnotation comparisonConceptAnnotation : comparisons) {
            if (!comparisonConceptAnnotation.equals(conceptAnnotation)) {
              out.println("<li>");
              printAnnotationHTML(out, comparisonConceptAnnotation);
            }
          }
        }
        out.println("</ul>");
      }
    }
  }

  /**
   * Gets single set.
   *
   * @param annotations the annotations
   * @return the single set
   */
  static Set<ConceptAnnotation> getSingleSet(Map<String, Set<ConceptAnnotation>> annotations) {
    Set<ConceptAnnotation> returnValues = new HashSet<>();
    for (Map.Entry<String, Set<ConceptAnnotation>> setName : annotations.entrySet()) {
      returnValues.addAll(setName.getValue());
    }
    return returnValues;
  }

  private static void initHTML(
      PrintStream html, String title, String link, String linkLabel, String description) {
    html.println("<html>");
    html.printf("<head><title>%s</title></head>%n", title);
    html.println("<body>");
    if (link != null) html.printf("<a href=\"%s\">%s</a>%n", link, linkLabel);
    html.printf("<h1>%s</h1>%n", title);
    html.println(description);
    html.println("<hr>");
  }

  /**
   * Sort by type map.
   *
   * @param types the types
   * @param conceptAnnotations the concept annotations
   * @return the map
   */
  static Map<String, Set<ConceptAnnotation>> sortByType(
      Set<String> types, Collection<ConceptAnnotation> conceptAnnotations) {
    Map<String, Set<ConceptAnnotation>> sortedAnnotations = new HashMap<>();

    for (String type : types) {
      sortedAnnotations.put(type, new HashSet<>());
    }
    for (ConceptAnnotation conceptAnnotation : conceptAnnotations) {
      String type = conceptAnnotation.getOwlClassRendering();
      sortedAnnotations.get(type).add(conceptAnnotation);
    }
    return sortedAnnotations;
  }

  private static void writeAnnotationTextSourceHTML(PrintStream out, ConceptAnnotation conceptAnnotation) {
    StringBuilder html = new StringBuilder("<hr><p>");
    String content = conceptAnnotation.getTextSource().getContent();

    html.append("Text source docID = ").append(conceptAnnotation.getTextSource().getId()).append("<p>");

    TreeSet<Span> spans = conceptAnnotation.getCollection();

    int frontBuffer = 150;
    int endBuffer = 150;

    int mark = Math.max(spans.first().getStart() - frontBuffer, 0);

    for (Span span : spans) {
      html.append(content, mark, span.getStart());
      mark = span.getEnd();
      html.append("<b>");
      html.append(content, span.getStart(), mark);
      html.append("</b>");
    }
    html.append(content, mark, Math.min(mark + endBuffer, content.length()));
    out.println(html);
  }

  private static void printAnnotationHTML(
      PrintStream out, ConceptAnnotation conceptAnnotation) {

    String html = conceptAnnotation.toString() +
        "  " + conceptAnnotation.toHtml();
    out.print(html);
  }

  private static void printIntro(
      PrintStream html, Iaa iaa, int numberOfDocs, String fileName, Matcher matcher) {
    html.printf(
        "<p>For more detailed documentation on Iaa please see the <a href=\"http://knowtator.sourceforge.net//iaa.shtml\">Iaa documentation</a>.%n");

    html.println("<p>");
    html.printf("<h2>%d-way Iaa Results</h2>%n", iaa.getSetNames().size());
    html.printf("Iaa calculated on %d documents.%n", numberOfDocs);
    html.printf("<p><a href=\"%s.dat\">tabular data</a>%n", fileName);
    html.println("<p>all annotations = matches + non-matches");
    html.println("<br> Iaa = matches / all annotations");
    if (matcher.returnsTrivials()) {
      html.println("<br>stingy Iaa = non-trivial matches / (matches + non-matches)");
      html.println(
          "<br>respectable Iaa = non-trivial matches / (non-trivial matches + non-matches)");
      html.println(
          "<br>non-trivial Iaa = non-trivial matches / (non-trivial matches + non-trivial non-matches)");
    }
  }

  private static void printTitleRowForAllwayIAA(PrintStream html, Matcher matcher) {
    html.printf("<table border=1><tr><td><b>Type</b></td><td><b>Iaa</b></td>%n");
    if (matcher.returnsTrivials()) {
      html.printf(
          "<td><b>stingy Iaa</b></td><td><b>respectable Iaa</b></td><td><b>non-trivial Iaa</b></td>%n");
    }
    html.println("<td><b>matches</b></td>");
    if (matcher.returnsTrivials()) {
      html.printf("<td><b>trivial matches</b></td><td><b>non-trivial matches</b></td>%n");
    }
    html.println("<td><b>non-matches</b></td>");
    if (matcher.returnsTrivials()) {
      html.printf("<td><b>trivial non-matches</b></td><td><b>non-trivial non-matches</b></td>%n");
    }
    html.println("</tr>");
  }

  /**
   * Init html string.
   *
   * @param title the title
   * @param description the description
   * @return the string
   */
  static String initHTML(String title, String description) {
    return String.format(
        "<html>%n<head><title>%s</title></head>%n<body>%n<h1>%s</h1>%n%s For more detailed documentation on Iaa please see the <a href=\"http://knowtator.sourceforge.net//iaa.shtml\">Iaa documentation</a>.%n",
        title, title, description);
  }

  /**
   * Print match data.
   *
   * @param html the html
   * @param sets the sets
   * @param fileName the file name
   * @param directory the directory
   * @param allwayMatches the allway matches
   * @param classes the classes
   * @param iaa the iaa
   * @throws IOException the io exception
   */
  static void printMatchData(
      PrintStream html,
      Set<String> sets,
      String fileName,
      File directory,
      Map<String, Set<ConceptAnnotation>> allwayMatches,
      Set<String> classes,
      Iaa iaa)
      throws IOException {

    html.println("<h2>match data</h2>");
    html.println("<ul>");

    Map<ConceptAnnotation, Set<ConceptAnnotation>> matchSets = iaa.getAllwayMatchSets();
    List<String> sortedTypes = new ArrayList<>(classes);
    Collections.sort(sortedTypes);

    for (String set : sets) {
      String matchesFileName = String.format("%s.matches.%s.html", fileName, set);
      html.printf("<li><a href=\"%s\">matches for %s</a></li>%n", matchesFileName, set);
      PrintStream matchesStream = new PrintStream(new File(directory, matchesFileName), "UTF-8");
      Set<ConceptAnnotation> matches = allwayMatches.get(set);
      Map<String, Set<ConceptAnnotation>> sortedMatches = Iaa2Html.sortByType(classes, matches);

      matchesStream.println(
          initHTML(
              String.format("Matches for %s", set),
              "Each concept that was considered a match is shown in the text that it was found in.  The matching annotations from the other concept sets are also shown."));
      Iaa2Html.printInstances(
          matchesStream,
          sortedMatches,
          sortedTypes,
          matchSets);
      matchesStream.flush();
      matchesStream.close();
    }
    html.println("</ul><hr>");
  }

  /**
   * Print nonmatch data.
   *
   * @param html the html
   * @param sets the sets
   * @param fileName the file name
   * @param directory the directory
   * @param allwayNonmatches the allway nonmatches
   * @param spanIndex the span index
   * @param classes the classes
   * @throws IOException the io exception
   */
  static void printNonmatchData(
      PrintStream html,
      Set<String> sets,
      String fileName,
      File directory,
      Map<String, Set<ConceptAnnotation>> allwayNonmatches,
      AnnotationSpanIndex spanIndex,
      Set<String> classes)
      throws IOException {
    html.println("<h2>non-match data</h2>");
    html.println("<ul>");

    List<String> sortedTypes = new ArrayList<>(classes);
    Collections.sort(sortedTypes);

    for (String set : sets) {
      String errorsFileName = String.format("%s.nonmatches.%s.html", fileName, set);
      html.printf("<li><a href=\"%s\">non-matches for %s</a></li>%n", errorsFileName, set);
      PrintStream errors = new PrintStream(new File(directory, errorsFileName), "UTF-8");
      Set<ConceptAnnotation> nonmatches = allwayNonmatches.get(set);
      Map<String, Set<ConceptAnnotation>> sortedNonmatches =
          Iaa2Html.sortByType(classes, nonmatches);

      Map<ConceptAnnotation, Set<ConceptAnnotation>> comparisonAnnotations = new HashMap<>();
      for (ConceptAnnotation nonmatch : nonmatches) {
        comparisonAnnotations.put(nonmatch, Iaa2Html.getCandidateAnnotations(nonmatch, spanIndex));
      }

      errors.println(
          initHTML(
              String.format("Non-matches for %s", set),
              "Each concept that was considered a non-match is shown in the text that it was found in.  Overlapping annotations from the other concept sets are also shown."));
      Iaa2Html.printInstances(
          errors,
          sortedNonmatches,
          sortedTypes,
          comparisonAnnotations);
      errors.flush();
      errors.close();
    }
    html.println("</ul><hr>");
  }

  /**
   * Print pairwise agreement.
   *
   * @param html the html
   * @param sets the sets
   * @param pairwiseMatches the pairwise matches
   * @param pairwiseNonmatches the pairwise nonmatches
   * @param percentageFormat the percentage format
   */
  static void printPairwiseAgreement(
      PrintStream html,
      Set<String> sets,
      Map<String, Map<String, Set<ConceptAnnotation>>> pairwiseMatches,
      Map<String, Map<String, Set<ConceptAnnotation>>> pairwiseNonmatches,
      NumberFormat percentageFormat) {
    html.println("<h2>Pair-wise agreement</h2>");
    html.printf(
        "<table border=1><tr><td><b>Gold standard set</b></td><td><b>compared set</b></td><td><b>true positives</b></td><td><b>false positives</b></td><td><b>false negatives</b></td><td><b>precision</b></td><td><b>recall</b></td><td><b>F-score</b></td></tr>%n");

    for (String setName : sets) {
      for (String setName2 : sets) {

        if (!setName.equals(setName2)) {
          Set<ConceptAnnotation> truePositives = pairwiseMatches.get(setName).get(setName2);
          Set<ConceptAnnotation> falseNegatives = pairwiseNonmatches.get(setName).get(setName2);
          Set<ConceptAnnotation> falsePositives = pairwiseNonmatches.get(setName2).get(setName);
          double precision =
              (double) truePositives.size()
                  / ((double) truePositives.size() + (double) falsePositives.size());
          double recall =
              (double) truePositives.size()
                  / ((double) truePositives.size() + (double) falseNegatives.size());
          double f_score = ((double) 2 * precision * recall) / (recall + precision);

          html.printf(
              "<tr><td>%s</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>%n",
              setName,
              setName2,
              truePositives.size(),
              falsePositives.size(),
              falseNegatives.size(),
              percentageFormat.format(precision),
              percentageFormat.format(recall),
              percentageFormat.format(f_score));
        }
      }
    }
    html.println("</table>");
    html.println("Precision and recall are given equal weight for the F-score.");
  }
}