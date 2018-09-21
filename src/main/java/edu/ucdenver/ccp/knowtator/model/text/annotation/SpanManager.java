package edu.ucdenver.ccp.knowtator.model.text.annotation;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.io.brat.BratStandoffIO;
import edu.ucdenver.ccp.knowtator.io.knowtator.KnowtatorXMLIO;
import edu.ucdenver.ccp.knowtator.model.collection.SpanCollection;
import edu.ucdenver.ccp.knowtator.model.selection.SelectionModel;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpanManager extends SelectionModel<Span> implements KnowtatorManager, BratStandoffIO, KnowtatorXMLIO {
    private SpanCollection spanCollection;
    private KnowtatorController controller;
    private final TextSource textSource;
    private final Annotation annotation;

    SpanManager(KnowtatorController controller, TextSource textSource, Annotation annotation) {
        spanCollection = new SpanCollection(controller);
        this.controller = controller;
        this.textSource = textSource;
        this.annotation = annotation;
    }

    void removeSpan(Span span) {
        spanCollection.remove(span);
        setSelection(null);
    }

    public SpanCollection getSpans() {
        return spanCollection;
    }

    @Override
    public void makeDirectory() {

    }

    @Override
    public void readFromBratStandoff(File file, Map<Character, List<String[]>> annotationMap, String content) {

    }

    @Override
    public void writeToBratStandoff(Writer writer, Map<String, Map<String, String>> annotationConfig, Map<String, Map<String, String>> visualConfig) throws IOException {
        Iterator<Span> spanIterator = spanCollection.iterator();
        StringBuilder spannedText = new StringBuilder();
        for (int i = 0; i < spanCollection.size(); i++) {
            Span span = spanIterator.next();
            span.writeToBratStandoff(writer, annotationConfig, visualConfig);
            String[] spanLines = span.getSpannedText().split("\n");
            for (int j = 0; j < spanLines.length; j++) {
                spannedText.append(spanLines[j]);
                if (j != spanLines.length - 1) {
                    spannedText.append(" ");
                }
            }
            if (i != spanCollection.size() - 1) {
                writer.append(";");
                spannedText.append(" ");
            }
        }
        writer.append(String.format("\t%s\n", spannedText.toString()));
    }

    @Override
    public void writeToKnowtatorXML(Document dom, Element parent) {
        spanCollection.forEach(span -> span.writeToKnowtatorXML(dom, parent));
    }

    @Override
    public void readFromKnowtatorXML(File file, Element parent) {

    }

    @Override
    public void readFromOldKnowtatorXML(File file, Element parent) {

    }

    @Override
    public void dispose() {
        super.dispose();
        spanCollection.forEach(Span::dispose);
        spanCollection.getCollection().clear();
    }

    public Span addSpan(String spanId, int spanStart, int spanEnd) {
        Span newSpan = new Span(spanId, spanStart, spanEnd, textSource, controller, annotation);
        spanCollection.add(newSpan);
        textSource.getAnnotationManager().getAllSpanCollection().add(newSpan);
        setSelection(newSpan);
        return newSpan;
    }
}
