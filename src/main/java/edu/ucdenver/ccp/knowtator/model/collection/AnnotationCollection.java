package edu.ucdenver.ccp.knowtator.model.collection;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.model.text.annotation.Annotation;

import java.util.TreeSet;

public class AnnotationCollection extends CyclableCollection<Annotation, AnnotationCollectionListener> {
	public AnnotationCollection(KnowtatorController controller) {
		super(controller, new TreeSet<>(Annotation::compare));
	}
}
