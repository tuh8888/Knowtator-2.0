/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.model.io.knowtator;

import edu.ucdenver.ccp.knowtator.model.annotation.Span;
import edu.ucdenver.ccp.knowtator.model.io.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OldKnowatorUtil extends XMLUtil {

    public static List<Span> getSpanInfo(Element annotationElement, String content) {
        List<Span> spans = new ArrayList<>();

        Element spanElement;
        int spanStart;
        int spanEnd;
        String spannedText;
        for (Node spanNode : KnowtatorXMLUtil.asList(annotationElement.getElementsByTagName(OldKnowtatorXMLTags.SPAN))) {
            if (spanNode.getNodeType() == Node.ELEMENT_NODE) {
                spanElement = (Element) spanNode;
                spanStart = Integer.parseInt(spanElement.getAttribute(OldKnowtatorXMLAttributes.SPAN_START));
                spanEnd = Integer.parseInt(spanElement.getAttribute(OldKnowtatorXMLAttributes.SPAN_END));
                spannedText = content.substring(spanStart, spanEnd);

                spans.add(new Span(spanStart, spanEnd, spannedText));
            }
        }
        return spans;
    }

    public static HashMap<String, Element> getClassIDsFromXml(Element textSourceElement) {
        /*
        Next parse classes and add the annotations
         */
        HashMap<String, Element> mentionTracker = new HashMap<>();

        for (Node classNode : KnowtatorXMLUtil.asList(textSourceElement.getElementsByTagName(OldKnowtatorXMLTags.CLASS_MENTION))) {
            if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                Element classElement = (Element) classNode;

                String annotationID = classElement.getAttribute(OldKnowtatorXMLAttributes.ID);
                mentionTracker.put(annotationID, classElement);
            }
        }

        return mentionTracker;
    }

    public static HashMap<String, Element> getComplexSlotsFromXml(Element textSourceElement) {
        HashMap<String, Element> mentionTracker = new HashMap<>();
        for (Node complexSlotNode : asList(textSourceElement.getElementsByTagName(OldKnowtatorXMLTags.COMPLEX_SLOT_MENTION))) {
            if (complexSlotNode.getNodeType() == Node.ELEMENT_NODE) {
                Element complexSlotElement = (Element) complexSlotNode;
                String annotationID = complexSlotElement.getAttribute(OldKnowtatorXMLAttributes.ID);
                mentionTracker.put(annotationID, complexSlotElement);

            }

        }
        return mentionTracker;

    }
}