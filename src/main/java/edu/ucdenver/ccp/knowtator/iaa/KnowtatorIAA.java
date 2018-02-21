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
package edu.ucdenver.ccp.knowtator.iaa;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.iaa.html.IAA2HTML;
import edu.ucdenver.ccp.knowtator.iaa.html.SpanMatcherHTML;
import edu.ucdenver.ccp.knowtator.iaa.matcher.ClassAndSpanMatcher;
import edu.ucdenver.ccp.knowtator.iaa.matcher.ClassMatcher;
import edu.ucdenver.ccp.knowtator.iaa.matcher.SpanMatcher;
import edu.ucdenver.ccp.knowtator.model.annotation.Annotation;
import edu.ucdenver.ccp.knowtator.model.annotation.TextSource;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KnowtatorIAA {
	private File outputDirectory;

	//KnowtatorFilter filter;

	private Set<TextSource> textSources;

	//Project project;

	private KnowtatorManager manager;

	//KnowtatorProjectUtil kpu;

	//MentionUtil mentionUtil;

//	FilterUtil filterUtil;

	private Map<Annotation, String> annotationTexts;

	private Map<Annotation, String> annotationTextNames;

	private Map<TextSource, Set<Annotation>> textSourceAnnotationsMap;

	private PrintStream html;

	private Set<String> setNames;

	@SuppressWarnings("unused")
	public KnowtatorIAA(File outputDirectory,
						//KnowtatorFilter filter,
						//Project project,
						KnowtatorManager manager
						//MentionUtil mentionUtil,
						//FilterUtil filterUtil
						) throws IAAException {

		this.outputDirectory = outputDirectory;
		//this.filter = filter;
		this.textSources = new HashSet<>(manager.getTextSourceManager().getTextSources().values());

		this.manager = manager;
		annotationTexts = new HashMap<>();
		annotationTextNames = new HashMap<>();

		initSetNames();
		initTextSourceAnnotations();
		initHTML();
	}

	private void initSetNames() {
		setNames = manager.getProfileManager().getProfiles().keySet();
	}

	private void initHTML() throws IAAException {
		try {
			html = new PrintStream(new File(outputDirectory, "index.html"));
			html.println("<html><head><title>Inter-Profile Agreement</title></head>");
			html.println("<body><ul>");
		} catch (IOException ioe) {
			throw new IAAException(ioe);
		}
	}

	public void closeHTML() {
		html.println("</ul>");
		html.println("</body></html>");
		html.flush();
		html.close();
	}

	private void initTextSourceAnnotations() {
		textSourceAnnotationsMap = new HashMap<>();
		for (TextSource textSource : textSources) {
			textSourceAnnotationsMap.put(textSource, textSource.getAnnotationManager().getAllAnnotations());
		}
	}


//	@SuppressWarnings("SameParameterValue")
//	private static int convertMatchSpans(String matchSpans) throws IAAException {
//		switch (matchSpans) {
//			case "SpansMatchExactly":
//				return annotation.SPANS_EXACT_COMPARISON;
//			case "SpansOverlap":
//				return annotation.SPANS_OVERLAP_COMPARISON;
//			case "IgnoreSpans":
//				return annotation.IGNORE_SPANS_COMPARISON;
//			default:
//				throw new IAAException(
//						"Span match criteria of slot matcher must be one of SpansMatchExactly, SpansOverlap, or IgnoreSpans");
//		}
//	}
//
//	public static FeatureMatcher createFeatureMatcher(String matcherName) throws IAAException {
//		FeatureMatcher featureMatcher = new FeatureMatcher(matcherName);
//
//		featureMatcher.setMatchClasses(true);
//

//		featureMatcher.setMatchSpans(convertMatchSpans("SpansMatchExactly"));


//		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
//				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());
//
//		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
//			if (slotMatchCriterium.getDirectType().equals(kpu.getSimpleSlotMatchCriteriaCls())) {
//				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
//				featureMatcher.addComparedSimpleFeatures(slotMatcherSlot.getBrowserText());
//			} else if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
//				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
//				Boolean b = (Boolean) slotMatchCriterium.getOwnSlotValue(kpu.getClassMatchCriteriaSlot());
//				boolean matchSlotClasses = b != null ? b.booleanValue() : false;
//
//				String str = (String) slotMatchCriterium.getOwnSlotValue(kpu.getSpanMatchCriteriaSlot());
//				if (str == null)
//					throw new IAAException("Slot matcher must specify how to compare spans of complex slot "
//							+ slotMatcherSlot.getBrowserText());
//				int matchSlotSpans = convertMatchSpans(str);
//
//				Collection<Slot> comparedSimpleSlots = (Collection<Slot>) slotMatchCriterium.getOwnSlotValues(kpu
//						.getSlotMatcherSimpleSlotsSlot());
//				Set<String> comparedSimpleFeatures = new HashSet<>();
//				for (Slot comparedSimpleSlot : comparedSimpleSlots) {
//					comparedSimpleFeatures.add(comparedSimpleSlot.getBrowserText());
//				}
//
//				Boolean propogateTrivialMatch = (Boolean) slotMatchCriterium.getOwnSlotValue(kpu
//						.getPropogateTrivialMatchSlot());
//				boolean trivialSimpleFeatureMatchesCauseTrivialMatch = propogateTrivialMatch != null ? propogateTrivialMatch
//						.booleanValue()
//						: false;
//
//				ComplexFeatureMatchCriteria matchCriteria = new ComplexFeatureMatchCriteria(matchSlotClasses,
//						matchSlotSpans, comparedSimpleFeatures, trivialSimpleFeatureMatchesCauseTrivialMatch);
//				featureMatcher.addComparedComplexFeature(slotMatcherSlot.getBrowserText(), matchCriteria);
//			}
//		}
//
//		return featureMatcher;
//	}

//	public void runFeatureMatcherIAA() throws IAAException {
//		runFeatureMatcherIAA("Feature Matcher");
//	}

//	public void runFeatureMatcherIAA(String matcherName) throws IAAException {
//		try {
//			FeatureMatcher featureMatcher = createFeatureMatcher(matcherName);
//			IAA featureIAA = new IAA(setNames);
//			for (Set<annotation> annotations : textSourceAnnotationsMap.values()) {
//				featureIAA.setAnnotations(annotations);
//				featureIAA.allwayIAA(featureMatcher);
//				featureIAA.pairwiseIAA(featureMatcher);
//			}
//
//			IAA2HTML.printIAA(featureIAA, featureMatcher, outputDirectory, textSources.size(), annotationTexts,
//					annotationTextNames);
//			html.println("<li><a href=\"" + featureMatcher.getDocID() + ".html\">" + featureMatcher.getDocID()
//					+ "</a></li>");
//		} catch (Exception exception) {
//			throw new IAAException(exception);
//		}
//	}

	public void runClassIAA() throws IAAException {
		try {
			ClassMatcher classMatcher = new ClassMatcher();
			IAA classIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				classIAA.setAnnotations(annotations);
				classIAA.allwayIAA(classMatcher);
				classIAA.pairwiseIAA(classMatcher);
			}

			IAA2HTML.printIAA(classIAA, classMatcher, outputDirectory, textSources.size(), annotationTexts,
					annotationTextNames);
			html.println("<li><a href=\"" + classMatcher.getName() + ".html\">" + classMatcher.getName() + "</a></li>");
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	public void runSpanIAA() throws IAAException {
		try {
			SpanMatcher spanMatcher = new SpanMatcher();
			IAA spanIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				spanIAA.setAnnotations(annotations);
				spanIAA.allwayIAA(spanMatcher);
				spanIAA.pairwiseIAA(spanMatcher);
			}
			SpanMatcherHTML.printIAA(spanIAA, spanMatcher, outputDirectory, textSources.size(), annotationTexts,
					annotationTextNames);
			html.println("<li><a href=\"" + spanMatcher.getName() + ".html\">" + spanMatcher.getName() + "</a></li>");
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	public void runClassAndSpanIAA() throws IAAException {
		try {
			ClassAndSpanMatcher classAndSpanMatcher = new ClassAndSpanMatcher();
			IAA classAndSpanIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				classAndSpanIAA.setAnnotations(annotations);
				classAndSpanIAA.allwayIAA(classAndSpanMatcher);
				classAndSpanIAA.pairwiseIAA(classAndSpanMatcher);
			}
			IAA2HTML.printIAA(classAndSpanIAA, classAndSpanMatcher, outputDirectory, textSources.size(),
					annotationTexts, annotationTextNames);
			html.println("<li><a href=\"" + classAndSpanMatcher.getName() + ".html\">" + classAndSpanMatcher.getName()
					+ "</a></li>");
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

//	public void runSubclassIAA() throws IAAException {
//		try {
//			Set<Cls> topLevelClses = getTopLevelClses();
//			Set<Cls> parentClses = new HashSet<Cls>();
//			for (Cls topLevelCls : topLevelClses) {
//				parentClses.add(topLevelCls);
//				Collection subclasses = topLevelCls.getSubclasses();
//				if (subclasses != null) {
//					Iterator subclassesItr = subclasses.iterator();
//					while (subclassesItr.hasNext()) {
//						Cls subclass = (Cls) subclassesItr.next();
//						Collection subsubclasses = subclass.getSubclasses();
//						if (subsubclasses != null && subsubclasses.size() > 0) {
//							parentClses.add(subclass);
//						}
//					}
//				}
//			}
//
//			html.println("<li><a href=\"subclassMatcher.html\">subclass matcher</a></li>");
//
//			PrintStream subclassHTML = new PrintStream(new File(outputDirectory, "subclassMatcher.html"));
//			subclassHTML.println(IAA2HTML.initHTML("Subclass Matcher", ""));
//			subclassHTML.println("Subclass matcher");
//			subclassHTML.println("<table border=1>\n");
//			subclassHTML
//					.println("<tr><td><b>Class</b></td><td><b>IAA</b></td><td><b>matches</b></td><td><b>non-matches</b></td></tr>");
//
//			SubclassMatcher subclassMatcher = new SubclassMatcher(createClassHierarchy(topLevelClses));
//			IAA subclassIAA = new IAA(setNames);
//
//			NumberFormat percentageFormat = NumberFormat.getPercentInstance();
//			percentageFormat.setMinimumFractionDigits(2);
//
//			for (Cls parentCls : parentClses) {
//				calculateSubclassIAA(parentCls, subclassMatcher, subclassIAA, textSourceAnnotationsMap);
//				SubclassMatcherHTML.printIAA(subclassIAA, subclassMatcher, outputDirectory, textSources.size(),
//						annotationTexts, annotationTextNames);
//
//				Map<String, Set<annotation>> allwayMatches = subclassIAA.getNontrivialAllwayMatches();
//				Set<annotation> matches = IAA2HTML.getSingleSet(allwayMatches);
//
//				Map<String, Set<annotation>> allwayNonmatches = subclassIAA.getNontrivialAllwayNonmatches();
//				Set<annotation> nonmatches = IAA2HTML.getSingleSet(allwayNonmatches);
//
//				double subclsIAA = (double) matches.size() / ((double) matches.size() + (double) nonmatches.size());
//
//				subclassHTML.println("<tr><td><a href=\"" + subclassMatcher.getDocID() + ".html\">"
//						+ parentCls.getDocID() + "</a></td>" + "<td>" + percentageFormat.format(subclsIAA) + "</td><td>"
//						+ matches.size() + "</td><td>" + nonmatches.size() + "</td></tr>");
//			}
//			subclassHTML.println("</table>");
//			subclassHTML.println("</body></html>");
//			subclassHTML.flush();
//			subclassHTML.close();
//		} catch (Exception e) {
//			throw new IAAException(e);
//		}
//	}

//	private static void calculateSubclassIAA(Cls cls, SubclassMatcher subclassMatcher,
//			IAA subclassIAA, Map<String, Set<annotation>> textSourceAnnotationsMap)
//			throwsedu.ucdenver.ccp.knowtator.iaa.IAAException {
//		subclassIAA.reset();
//		subclassMatcher.setIAAClass(cls.getDocID());
//		for (Set<annotation> annotations : textSourceAnnotationsMap.values()) {
//			subclassIAA.setAnnotations(annotations);
//			subclassIAA.allwayIAA(subclassMatcher);
//			subclassIAA.pairwiseIAA(subclassMatcher);
//		}
//	}

//	public static Set<Slot> getSimpleSlotsFromMatcherConfig(SimpleInstance slotMatcherConfig, KnowtatorProjectUtil kpu) {
//		Set<Slot> returnValues = new HashSet<Slot>();
//
//		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
//				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());
//
//		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
//			if (slotMatchCriterium.getDirectType().equals(kpu.getSimpleSlotMatchCriteriaCls())) {
//				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
//				returnValues.add(slotMatcherSlot);
//			} else if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
//				Collection<Slot> comparedSimpleSlots = (Collection<Slot>) slotMatchCriterium.getOwnSlotValues(kpu
//						.getSlotMatcherSimpleSlotsSlot());
//				if (comparedSimpleSlots != null)
//					returnValues.addAll(comparedSimpleSlots);
//			}
//		}
//		return returnValues;
//	}

//	public static Set<Slot> getComplexSlotsFromMatcherConfig(SimpleInstance slotMatcherConfig, KnowtatorProjectUtil kpu) {
//		Set<Slot> returnValues = new HashSet<Slot>();
//
//		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
//				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());
//
//		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
//			if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
//				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
//				returnValues.add(slotMatcherSlot);
//			}
//		}
//		return returnValues;
//	}
}
