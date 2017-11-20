/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */
package edu.ucdenver.ccp.knowtator.iaa.matcher;

import edu.ucdenver.ccp.knowtator.annotation.Annotation;
import edu.ucdenver.ccp.knowtator.iaa.IAA;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassMatcher implements Matcher {
	/**
	 * This method will return an annotation with the same class and spans. If
	 * one does not exist, then it will return an annotation with the same class
	 * and overlapping spans. If more than one of these exists, then the
	 * shortest annotation with the same class and overlapping spans will be
	 * returned. Otherwise, null is returned.
	 *
	 * @param matchResult
	 *            will be set to NONTRIVIAL_MATCH or NONTRIVIAL_NONMATCH.
	 *            Trivial matches and non-matches are not defined for this
	 *            matcher.
	 * @see edu.ucdenver.ccp.knowtator.iaa.matcher.Matcher#match(Annotation, String, Set,
	 *      IAA, MatchResult)
	 * @see edu.ucdenver.ccp.knowtator.iaa.matcher.MatchResult#NONTRIVIAL_MATCH
	 * @see edu.ucdenver.ccp.knowtator.iaa.matcher.MatchResult#NONTRIVIAL_NONMATCH
	 */

	@SuppressWarnings("Duplicates")
	public Annotation match(Annotation annotation, String compareSetName, Set<Annotation> excludeAnnotations, IAA iaa,
							MatchResult matchResult) {
		Annotation match = match(annotation, compareSetName, iaa, excludeAnnotations);
		if (match != null) {
			matchResult.setResult(MatchResult.NONTRIVIAL_MATCH);
			return match;
		} else {
			matchResult.setResult(MatchResult.NONTRIVIAL_NONMATCH);
			return null;
		}
	}

	private static Annotation match(Annotation annotation, String compareSetName, IAA iaa,
                                    Set<Annotation> excludeAnnotations) {
		Annotation spanAndClassMatch = ClassAndSpanMatcher.match(annotation, compareSetName, iaa, excludeAnnotations);
		if (spanAndClassMatch != null) {
			return spanAndClassMatch;
		}

		Set<Annotation> matches = matches(annotation, compareSetName, iaa, excludeAnnotations);
		if (matches.size() > 0) {
			if (matches.size() == 1)
				return matches.iterator().next();
			else {
				return IAA.getShortestAnnotation(matches);
			}
		} else {
			return null;
		}
	}

	private static Set<Annotation> matches(Annotation annotation, String compareSetName, IAA iaa,
                                           Set<Annotation> excludeAnnotations) {

		Set<Annotation> overlappingAnnotations = iaa.getOverlappingAnnotations(annotation, compareSetName);
		Set<Annotation> annotationsOfSameType = iaa.getAnnotationsOfSameType(annotation, compareSetName);
		Set<Annotation> candidateAnnotations = new HashSet<>(overlappingAnnotations);
		candidateAnnotations.retainAll(annotationsOfSameType);
		candidateAnnotations.removeAll(excludeAnnotations);

		if (candidateAnnotations.size() > 0) {
			return Collections.unmodifiableSet(candidateAnnotations);
		} else {
			return Collections.emptySet();
		}
	}

	public String getName() {
		return "Class matcher";
	}

	public String getDescription() {
		return "Annotations match if they have the same class assignment and their spans overlap.";
	}

	public boolean returnsTrivials() {
		return false;
	}

}
