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

package edu.ucdenver.ccp.knowtator.view.actions.graph;

import com.mxgraph.util.mxEvent;
import edu.ucdenver.ccp.knowtator.model.GraphSpace;
import edu.ucdenver.ccp.knowtator.view.actions.AbstractKnowtatorAction;
import edu.ucdenver.ccp.knowtator.view.actions.ActionUnperformableException;
import edu.ucdenver.ccp.knowtator.view.actions.KnowtatorEdit;

import javax.swing.undo.UndoableEdit;

public abstract class AbstractGraphAction extends AbstractKnowtatorAction {

	final GraphSpace graphSpace;
	private final KnowtatorEdit edit;

	AbstractGraphAction(String presentationName, GraphSpace graphSpace) {
		super(presentationName);
		this.graphSpace = graphSpace;
		edit = new KnowtatorEdit(getPresentationName()) {

		};
	}

	private void prepare() {
		graphSpace.getModel().addListener(mxEvent.UNDO, edit);
	}

	protected abstract void perform() throws ActionUnperformableException;

	private void cleanUp() {
		graphSpace.getModel().removeListener(edit, mxEvent.UNDO);
	}

	@Override
	public void execute() throws ActionUnperformableException {
		prepare();
		perform();
		cleanUp();
	}

	@Override
	public UndoableEdit getEdit() {
		return edit;
	}
}

