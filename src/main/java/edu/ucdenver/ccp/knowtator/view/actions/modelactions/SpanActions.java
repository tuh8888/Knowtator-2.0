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

package edu.ucdenver.ccp.knowtator.view.actions.modelactions;

import edu.ucdenver.ccp.knowtator.model.KnowtatorModel;
import edu.ucdenver.ccp.knowtator.model.object.Span;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.actions.AbstractKnowtatorAction;
import edu.ucdenver.ccp.knowtator.view.textpane.KnowtatorTextPane;

/** The type Span actions. */
public class SpanActions {
  /** The constant GROW. */
  public static final String GROW = "grow";
  /** The constant SHRINK. */
  public static final String SHRINK = "shrink";
  /** The constant START. */
  public static final String START = "start";
  /** The constant END. */
  public static final String END = "end";

  /** The type Modify span action. */
  public static class ModifySpanAction extends AbstractKnowtatorAction {

    private final Span span;
    private final String startOrEnd;
    private final String growOrShrink;
    private boolean spanStartChanged;
    private boolean spanEndChanged;

    /**
     * Instantiates a new Modify span action.
     *
     * @param model the model
     * @param startOrEnd the start or end
     * @param growOrShrink the grow or shrink
     * @param span the span
     */
    public ModifySpanAction(
        KnowtatorModel model, String startOrEnd, String growOrShrink, Span span) {
      super(model, "Modify span");
      this.startOrEnd = startOrEnd;
      this.growOrShrink = growOrShrink;
      this.span = span;

      spanStartChanged = false;
      spanEndChanged = false;
    }

    @Override
    public void execute() {
      int spanStart = span.getStart();
      int spanEnd = span.getEnd();
      int startModification = getStartModification(startOrEnd, growOrShrink);
      int endModification = getEndModification(startOrEnd, growOrShrink);
      span.modify(startModification, endModification);
      spanStartChanged = spanStart != span.getStart();
      spanEndChanged = spanEnd != span.getEnd();
    }

    @Override
    public void undo() {
      super.undo();
      int startModification;
      int endModification;
      startModification =
          spanStartChanged ? (-1) * getStartModification(startOrEnd, growOrShrink) : 0;
      endModification = spanEndChanged ? (-1) * getEndModification(startOrEnd, growOrShrink) : 0;
      span.modify(startModification, endModification);
    }

    @Override
    public void redo() {
      super.redo();
      execute();
    }
  }

  private static int getEndModification(String startOrEnd, String growOrShrink) {
    return startOrEnd.equals(END)
        ? growOrShrink.equals(GROW) ? +1 : growOrShrink.equals(SHRINK) ? -1 : 0
        : 0;
  }

  private static int getStartModification(String startOrEnd, String growOrShrink) {
    return startOrEnd.equals(START)
        ? growOrShrink.equals(GROW) ? -1 : growOrShrink.equals(SHRINK) ? +1 : 0
        : 0;
  }

  /**
   * Modify selection.
   *
   * @param view the view
   * @param startOrEnd the start or end
   * @param growOrShrink the grow or shrink
   */
  public static void modifySelection(KnowtatorView view, String startOrEnd, String growOrShrink) {
    int startModification = getStartModification(startOrEnd, growOrShrink);
    int endModification = getEndModification(startOrEnd, growOrShrink);
    KnowtatorTextPane textPane = view.getTextPane();
    textPane.modifySelection(startModification, endModification);
  }
}
