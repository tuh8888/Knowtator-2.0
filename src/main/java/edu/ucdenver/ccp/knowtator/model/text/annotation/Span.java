package edu.ucdenver.ccp.knowtator.model.text.annotation;

import edu.ucdenver.ccp.knowtator.io.brat.BratStandoffIO;
import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLIO;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLAttributes;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLTags;
import edu.ucdenver.ccp.knowtator.model.KnowtatorTextBoundObject;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Span implements KnowtatorTextBoundObject, Comparable<Span>, KnowtatorXMLIO, BratStandoffIO {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(KnowtatorController.class);

  private int start;
  private int end;
  private Annotation annotation;
  private String id;
  private TextSource textSource;

  Span(
      String id, int start, int end, TextSource textSource, KnowtatorController controller, Annotation annotation) {

    this.textSource = textSource;
    this.start = start;
    this.end = end;
    this.annotation = annotation;

    controller.verifyId(id, this, false);

    if (start > end) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is greater than the end of it: start="
              + start
              + " end="
              + end);
    }
    if (start < 0) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is less than zero: start=" + start);
    }
  }

  public Span(int start, int end) {
    this.start = start;
    this.end = end;
    if (start > end) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is greater than the end of it: start="
              + start
              + " end="
              + end);
    }
    if (start < 0) {
      throw new IndexOutOfBoundsException(
          "Span is invalid because the start of the Span is less than zero: start=" + start);
    }
  }

  /*
  COMPARISON
   */

  public static boolean intersects(TreeSet<Span> spans1, TreeSet<Span> spans2) {
    for (Span span1 : spans1) {
      for (Span span2 : spans2) {
        if (span1.intersects(span2)) return true;
      }
    }
    return false;
  }

  /**
   * This method assumes that the both lists of spans are sorted the same way and that a Span in one
   * list at the same index as a Span in the other list should be the same.
   *
   * @param spans1 sorted list of c
   * @param spans2 sorted list of spans
   * @return true if the two lists of spans are the same.
   */
  public static boolean spansMatch(TreeSet<Span> spans1, TreeSet<Span> spans2) {
    if (spans1.size() == spans2.size()) {
      Iterator<Span> spans1Iterator = spans1.iterator(), spans2Iterator = spans2.iterator();
      while (spans1Iterator.hasNext()) {
        if (!spans1Iterator.next().equalStartAndEnd(spans2Iterator.next())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public static String substring(String string, Span span) {
    int start = Math.max(0, span.getStart());
    start = Math.min(start, string.length() - 1);
    int end = Math.max(0, span.getEnd());
    end = Math.min(end, string.length() - 1);
    return string.substring(start, end);
  }

  public int compare(Span span2) {
    if (span2 == null) {
      return 1;
    }
    int compare = getStart().compareTo(span2.getStart());
    if (compare == 0) {
      compare = getEnd().compareTo(span2.getEnd());
    }
    if (compare == 0) {
      compare = id.compareTo(span2.getId());
    }
    return compare;
  }

  private boolean equalStartAndEnd(Object object) {
    if (!(object instanceof Span)) {
      return false;
    }
    Span span = (Span) object;
    return Objects.equals(getStart(), span.getStart()) && Objects.equals(getEnd(), span.getEnd());
  }

  public int hashCode() {
    return ((this.start << 16) | (0x0000FFFF | this.end));
  }

  private boolean contains(Span span) {
    return (getStart() <= span.getStart() && span.getEnd() <= getEnd());
  }

  public boolean contains(int i) {
    return (getStart() <= i && i < getEnd());
  }

  /** we need some junit tests */
  public boolean intersects(Span span) {
    int spanStart = span.getStart();
    // either Span's start is in this or this' start is in Span
    return this.contains(span)
        || span.contains(this)
        || (getStart() <= spanStart && spanStart < getEnd()
            || spanStart <= getStart() && getStart() < span.getEnd());
  }

  @Override
  public int compareTo(@NotNull Span o) {
    return compare(o);
  }

  /*
  MODIFIERS
   */

  void shrinkEnd() {
    if (end > start) end -= 1;
  }

  void shrinkStart() {
    if (start < end) start += 1;
  }

  void growEnd(int limit) {
    if (end < limit) end += 1;
  }

  void growStart() {
    if (start > 0) start -= 1;
  }

  /*
  TRANSLATORS
   */

  public String toString() {
    return String.format("Start: %d, End: %d", start, end);
  }

  /*
  GETTERS
   */

  public Annotation getAnnotation() {
    return annotation;
  }

  String getSpannedText() {
    return textSource.getContent().substring(start, end);
  }

  public Integer getStart() {
    return start;
  }

  public Integer getEnd() {
    return end;
  }

  public int getSize() {
    return getEnd() - getStart();
  }


  @Override
  public String getId() {
    return id;
  }

  @Override
  public TextSource getTextSource() {
    return textSource;
  }

  /**
   These methods are intended to correct for Java's handling of supplementary unicode characters.
   */
//  public int getStartCodePoint() { return Character.codePointCount(textSource.getContent(), 0, start); }
//
//  public int getEndCodePoint() { return Character.codePointCount(textSource.getContent(), 0, end); }

  /*
  SETTERS
   */

  @Override
  public void setId(String id) {
    this.id = id;
  }

  /*
  READERS
   */

  @Override
  public void readFromKnowtatorXML(File file, Element parent) {}

  @Override
  public void readFromOldKnowtatorXML(File file, Element parent) {}


  /*
  WRITERS
   */

  @Override
  public void writeToKnowtatorXML(Document dom, Element annotationElem) {
    Element spanElement = dom.createElement(KnowtatorXMLTags.SPAN);
    spanElement.setAttribute(KnowtatorXMLAttributes.SPAN_START, String.valueOf(getStart()));
    spanElement.setAttribute(KnowtatorXMLAttributes.SPAN_END, String.valueOf(getEnd()));
    spanElement.setAttribute(KnowtatorXMLAttributes.ID, id);
    spanElement.setTextContent(getSpannedText());
    annotationElem.appendChild(spanElement);
  }

  @Override
  public void readFromBratStandoff(File file, Map<Character, List<String[]>> annotationMap, String content) {

  }

  @Override
  public void writeToBratStandoff(Writer writer, Map<String, Map<String, String>> annotationsConfig, Map<String, Map<String, String>> visualConfig) throws IOException {
    String[] spanLines = getSpannedText().split("\n");
    int spanStart = getStart();
    for (int j = 0; j < spanLines.length; j++) {
      writer.append(String.format("%d %d", spanStart, spanStart + spanLines[j].length()));
      if (j != spanLines.length -1) {
        writer.append(";");
      }
      spanStart += spanLines[j].length() + 1;
    }

  }

  @Override
  public void dispose() {
  }
}
