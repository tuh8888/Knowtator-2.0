/*
 * MIT License
 *
 * Copyright (c) 2018 Harrison Pielke-Lombardo
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.ucdenver.ccp.knowtator.model.collection;

import edu.ucdenver.ccp.knowtator.model.BaseModel;
import edu.ucdenver.ccp.knowtator.model.collection.event.SelectionEvent;
import edu.ucdenver.ccp.knowtator.model.object.ModelObject;
import java.util.Optional;
import java.util.TreeSet;

/**
 * The type Selectable collection.
 *
 * @param <K> the type parameter
 */
public abstract class SelectableCollection<K extends ModelObject> extends CyclableCollection<K> {

  private K selection;

  /**
   * Instantiates a new Selectable collection.
   *
   * @param model the model
   * @param collection the collection
   */
  SelectableCollection(BaseModel model, TreeSet<K> collection) {
    super(model, collection);
    selection = null;
  }

  /**
   * Gets selection.
   *
   * @return the selection
   */
  public Optional<K> getSelection() {
    return Optional.ofNullable(selection);
  }

  /** Select next. */
  public void selectNext() {
    if (getSelection().isPresent()) {
      getSelection().ifPresent(selection -> setSelection(getNext(selection)));
    } else {
      first().ifPresent(this::setSelection);
    }
  }

  /** Select previous. */
  public void selectPrevious() {
    if (getSelection().isPresent()) {
      getSelection().ifPresent(selection -> setSelection(getPrevious(selection)));
    } else {
      first().ifPresent(this::setSelection);
    }
  }

  /**
   * Sets selection.
   *
   * @param newSelection the new selection
   */
  public void setSelection(K newSelection) {
    SelectionEvent<ModelObject> selectionEvent =
        new SelectionEvent<>(model, selection, newSelection);
    this.selection = newSelection;
    if (model != null) {
      model.fireModelEvent(selectionEvent);
    }
  }

  @Override
  public void add(K item) {
    setSelection(item);
    super.add(item);
  }

  @Override
  public void remove(K item) {
    super.remove(item);
    this.getSelection()
        .filter(selection -> selection.equals(item))
        .ifPresent(selection -> setSelection(null));
  }
}
