package edu.ucdenver.ccp.knowtator.model.collection;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;

import java.util.TreeSet;

public class TextSourceCollection
		extends CyclableCollection<TextSource, TextSourceCollectionListener> {

	public TextSourceCollection(KnowtatorController controller) {
		super(controller, new TreeSet<>(TextSource::compare));
	}
}
