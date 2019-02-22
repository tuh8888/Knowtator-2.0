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

package edu.ucdenver.ccp.knowtator.view.graph;

import edu.ucdenver.ccp.knowtator.view.KnowtatorComponent;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * The type Graph view dialog.
 */
public class GraphViewDialog extends JDialog implements KnowtatorComponent {
  private JPanel contentPane;
  private GraphView graphView;
  private final KnowtatorView view;

  /**
   * Instantiates a new Graph view dialog.
   *
   * @param view the view
   */
  public GraphViewDialog(KnowtatorView view) {

    this.view = view;
    $$$setupUI$$$();

    setSize(new Dimension(800, 800));
    setLocationRelativeTo(view);

    setContentPane(contentPane);
    setModal(false);

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(
        new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            onCancel();
          }
        });

    JDialog graphViewDialog = this;
    addWindowFocusListener(
        new WindowFocusListener() {
          @Override
          public void windowGainedFocus(WindowEvent e) {
          }

          @Override
          public void windowLostFocus(WindowEvent e) {
            if (e.getOppositeWindow() == null
                || (e.getOppositeWindow() != SwingUtilities.getWindowAncestor(view)
                && e.getOppositeWindow().getOwner() != graphViewDialog)) {
              setAlwaysOnTop(false);
              toBack();
            }
          }
        });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(
        e -> onCancel(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onCancel() {
    // addProfile your code here if necessary
    setVisible(false);
    //        dispose();
  }

  private void createUIComponents() {
    graphView = new GraphView(this, view);
  }

  @Override
  public void reset() {
    graphView.reset();
  }

  @Override
  public void dispose() {
    super.dispose();
    graphView.dispose();
    setVisible(false);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    graphView.setVisible(visible);
  }

  /**
   * Gets graph view.
   *
   * @return the graph view
   */
  public GraphView getGraphView() {
    return graphView;
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout(0, 0));
    contentPane.setPreferredSize(new Dimension(600, 103));
    contentPane.add(graphView.$$$getRootComponent$$$(), BorderLayout.CENTER);
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }

}
