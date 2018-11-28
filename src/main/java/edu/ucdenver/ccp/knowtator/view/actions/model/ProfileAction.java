/*
 *  MIT License
 *
 *  Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.view.actions.model;

import edu.ucdenver.ccp.knowtator.model.profile.Profile;
import edu.ucdenver.ccp.knowtator.model.text.TextSource;
import edu.ucdenver.ccp.knowtator.model.text.concept.ConceptAnnotation;
import edu.ucdenver.ccp.knowtator.view.KnowtatorColorPalette;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.actions.ActionUnperformableException;
import edu.ucdenver.ccp.knowtator.view.actions.collection.AbstractKnowtatorCollectionAction;
import edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.ucdenver.ccp.knowtator.view.actions.collection.CollectionActionType.REMOVE;

public class ProfileAction extends AbstractKnowtatorCollectionAction<Profile> {
	private final String profileId;

	public ProfileAction(CollectionActionType actionType, String profileId) {
		super(actionType, "Add profile", KnowtatorView.MODEL.getProfileCollection());
		this.profileId = profileId;
	}

	@Override
	public void prepareRemove() {
		collection.get(profileId).ifPresent(this::setObject);

	}

	@Override
	protected void prepareAdd() {
		Profile profile = new Profile(KnowtatorView.MODEL, profileId);
		setObject(profile);
	}

	@Override
	protected void cleanUpRemove() throws ActionUnperformableException {
		for (TextSource textSource : KnowtatorView.MODEL.getTextSources()) {
			// Cast to array to avoid concurrent modification exceptions
			Object[] array = textSource.getConceptAnnotationCollection().getCollection().toArray();
			for (Object o : array) {
				ConceptAnnotation conceptAnnotation = (ConceptAnnotation) o;

				if (object.map(object -> conceptAnnotation.getAnnotator().equals(object)).orElse(false)) {
					ConceptAnnotationAction action = new ConceptAnnotationAction(REMOVE, conceptAnnotation.getTextSource());
					action.setObject(conceptAnnotation);
					action.execute();
					edit.addKnowtatorEdit(action.getEdit());
				}
			}
		}
	}

	@Override
	public void cleanUpAdd() {

	}

	public static void assignColorToClass(KnowtatorView view, OWLClass owlClass) {
		Optional<OWLClass> objectOptional = Optional.ofNullable(owlClass);

		if (!objectOptional.isPresent()) {
			objectOptional = KnowtatorView.MODEL.getTextSource()
							.flatMap(textSource -> textSource.getConceptAnnotationCollection().getSelection()
									.map(ConceptAnnotation::getOwlClass));
		}
		objectOptional.ifPresent(_owlClass -> {
			Set<OWLClass> owlClasses = new HashSet<>();
			owlClasses.add(_owlClass);

			JColorChooser colorChooser = new KnowtatorColorPalette();

			final Optional[] finalC = new Optional[]{Optional.empty()};
			JDialog dialog = JColorChooser.createDialog(view, "Pick a color for " + _owlClass, true, colorChooser,
					e -> finalC[0] = Optional.ofNullable(colorChooser.getColor()), null);


			dialog.setVisible(true);

			Optional c = finalC[0];
			if (c.isPresent() && c.get() instanceof Color) {
				Color color = (Color) c.get();


				KnowtatorView.MODEL.getProfileCollection().getSelection()
						.ifPresent(profile -> profile.addColor(_owlClass, color));

				if (JOptionPane.showConfirmDialog(view, "Assign color to descendants of " + _owlClass + "?") == JOptionPane.OK_OPTION) {
					owlClasses.addAll(KnowtatorView.MODEL.getDescendants(_owlClass));
				}

				KnowtatorView.MODEL.getProfileCollection().getSelection()
						.ifPresent(profile -> KnowtatorView.MODEL.registerAction(new ColorChangeAction(profile, owlClasses, color)));


			}

		});
	}
}