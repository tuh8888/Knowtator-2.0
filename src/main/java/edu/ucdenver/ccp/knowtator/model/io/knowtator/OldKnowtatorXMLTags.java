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

public class OldKnowtatorXMLTags {
    // ******************OLD Tags*****************
    static final String ANNOTATIONS = "annotations";
    public static final String ANNOTATION = "annotation";
    public static final String MENTION = "mention";
    public static final String ANNOTATOR = "annotator";
    static final String SPAN = "span";
    static final String SPANNED_TEXT = "spannedText";
    static final String CLASS_MENTION = "classMention";
    public static final String MENTION_CLASS = "mentionClass";  // The named entity label

    static final String COMPLEX_SLOT_MENTION = "complexSlotMention";
    public static final String MENTION_SLOT = "mentionSlot";
    public static final String COMPLEX_SLOT_MENTION_VALUE = "complexSlotMentionValue";
    public static final String HAS_SLOT_MENTION = "hasSlotMention";
}