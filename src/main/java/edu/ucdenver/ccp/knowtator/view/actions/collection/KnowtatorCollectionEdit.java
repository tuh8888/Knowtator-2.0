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

package edu.ucdenver.ccp.knowtator.view.actions.collection;

import edu.ucdenver.ccp.knowtator.model.KnowtatorDataObjectInterface;
import edu.ucdenver.ccp.knowtator.model.collection.KnowtatorCollection;
import edu.ucdenver.ccp.knowtator.view.actions.ActionUnperformableException;
import edu.ucdenver.ccp.knowtator.view.actions.KnowtatorEdit;

import java.util.Optional;

public class KnowtatorCollectionEdit<K extends KnowtatorDataObjectInterface> extends KnowtatorEdit {

    private final CollectionActionType actionType;
    private final KnowtatorCollection<K> collection;
    private Optional<K> object;

    KnowtatorCollectionEdit(CollectionActionType actionType, KnowtatorCollection<K> collection, Optional<K> object, String presentationName) {
        super(presentationName);
        this.actionType = actionType;
        this.collection = collection;
        this.object = object;
    }

    @Override
    public void undo() {
        super.undo();
        switch (actionType) {
            case ADD:
                try {
                    collection.remove(object.orElseThrow(ActionUnperformableException::new));
                } catch (ActionUnperformableException ignored) {

                }
                break;
            case REMOVE:
                try {
                    collection.add(object.orElseThrow(ActionUnperformableException::new));
                } catch (ActionUnperformableException ignored) {

                }
                break;
        }
    }

    @Override
    public void redo() {
        super.redo();
        switch (actionType) {
            case ADD:
                try {
                    collection.add(object.orElseThrow(ActionUnperformableException::new));
                } catch (ActionUnperformableException ignored) {
                }
                break;
            case REMOVE:
                    try {
                        collection.remove(object.orElseThrow(ActionUnperformableException::new));
                    } catch (ActionUnperformableException ignored) {

                    }
                break;
        }
    }


    public void setObject(Optional<K> object) {
        this.object = object;
    }
}