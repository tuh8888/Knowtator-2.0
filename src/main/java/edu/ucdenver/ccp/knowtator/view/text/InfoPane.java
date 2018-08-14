package edu.ucdenver.ccp.knowtator.view.text;

import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.model.selection.ActiveTextSourceNotSetException;
import edu.ucdenver.ccp.knowtator.model.text.annotation.Span;
import edu.ucdenver.ccp.knowtator.model.text.graph.ActiveGraphSpaceNotSetException;
import edu.ucdenver.ccp.knowtator.model.text.graph.GraphSpace;
import edu.ucdenver.ccp.knowtator.view.ControllerNotSetException;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import edu.ucdenver.ccp.knowtator.view.chooser.AnnotationGraphSpaceChooser;
import edu.ucdenver.ccp.knowtator.view.text.textpane.KnowtatorTextPane;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class InfoPane {
    private final KnowtatorView view;
    private SpanList spanList;
    private AnnotationIDLabel annotationIDLabel;
    private AnnotationClassLabel annotationClassLabel;
    private AnnotatorLabel annotatorLabel;
    private JPanel findPanel;
    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox regexCheckBox;
    private JButton previousMatchButton;
    private JButton nextMatchButton;
    private JTextField matchTextField;
    private JPanel infoPanePanel;
    private AnnotationGraphSpaceChooser graphSpaceChooser;
    private JPanel infoPanel;
    private JLabel infoPanelTitleLabel;

    public InfoPane(KnowtatorView view) {
        this.view = view;
        $$$setupUI$$$();
        makeButtons();
    }

    private void makeButtons() {
        graphSpaceChooser.addActionListener(
                e -> {
                    JComboBox comboBox = (JComboBox) e.getSource();
                    try {
                        if (comboBox.getSelectedItem() != null
                                && comboBox.getSelectedItem()
                                != view.getController().getSelectionManager().getActiveTextSource().getGraphSpaceManager().getActiveGraphSpace()) {
                            view.getGraphViewDialog().setVisible(true);
                            view.getController()
                                    .getSelectionManager()
                                    .getActiveTextSource().getGraphSpaceManager().setSelectedGraphSpace((GraphSpace) comboBox.getSelectedItem());
                        }
                    } catch (ActiveTextSourceNotSetException | ControllerNotSetException | ActiveGraphSpaceNotSetException ignored) {

                    }
                });

        spanList.addListSelectionListener(
                e -> {
                    JList jList = (JList) e.getSource();
                    if (jList.getSelectedValue() != null) {
                        try {
                            view.getController().getSelectionManager().getActiveTextSource().getAnnotationManager().setSelectedSpan((Span) jList.getSelectedValue());
                        } catch (ActiveTextSourceNotSetException | ControllerNotSetException ignored) {

                        }
                    }
                });

        nextMatchButton.addActionListener(
                e -> {
                    String textToFind = matchTextField.getText();
                    KnowtatorTextPane currentKnowtatorTextPane = view.getKnowtatorTextPane();
                    String textToSearch = currentKnowtatorTextPane.getText();
                    if (!caseSensitiveCheckBox.isSelected()) {
                        textToSearch = textToSearch.toLowerCase();
                    }
                    int matchLoc =
                            textToSearch.indexOf(textToFind, currentKnowtatorTextPane.getSelectionStart() + 1);
                    if (matchLoc != -1) {
                        currentKnowtatorTextPane.requestFocusInWindow();
                        currentKnowtatorTextPane.select(matchLoc, matchLoc + textToFind.length());
                    } else {
                        currentKnowtatorTextPane.setSelectionStart(textToSearch.length());
                    }
                });
        previousMatchButton.addActionListener(
                e -> {
                    String textToFind = matchTextField.getText();
                    String textToSearch = view.getKnowtatorTextPane().getText();
                    if (!caseSensitiveCheckBox.isSelected()) {
                        textToSearch = textToSearch.toLowerCase();
                    }
                    int matchLoc =
                            textToSearch.lastIndexOf(textToFind, view.getKnowtatorTextPane().getSelectionStart() - 1);
                    if (matchLoc != -1) {
                        view.getKnowtatorTextPane().requestFocusInWindow();
                        view.getKnowtatorTextPane().select(matchLoc, matchLoc + textToFind.length());
                    } else {
                        view.getKnowtatorTextPane().setSelectionStart(-1);
                    }
                });
    }

    public void createUIComponents() {
        annotationIDLabel = new AnnotationIDLabel(view);
        annotationClassLabel = new AnnotationClassLabel(view);
        annotatorLabel = new AnnotatorLabel(view);
        spanList = new SpanList(view);
        graphSpaceChooser = new AnnotationGraphSpaceChooser(view);
    }

    public void setController(KnowtatorController controller) {
        controller.addViewListener(annotatorLabel);
        controller.addViewListener(annotationIDLabel);
        controller.addViewListener(annotatorLabel);
        controller.addViewListener(spanList);
        controller.addViewListener(graphSpaceChooser);
    }

    public AnnotationClassLabel getAnnotationClassLabel() {
        return annotationClassLabel;
    }


    public void dispose() {
        annotationIDLabel.dispose();
        annotatorLabel.dispose();
        annotationClassLabel.dispose();
        spanList.dispose();
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
        infoPanePanel = new JPanel();
        infoPanePanel.setLayout(new BorderLayout(0, 0));
        infoPanePanel.setMaximumSize(new Dimension(250, 2147483647));
        infoPanePanel.setMinimumSize(new Dimension(250, -1));
        infoPanePanel.setPreferredSize(new Dimension(250, -1));
        findPanel = new JPanel();
        findPanel.setLayout(new BorderLayout(0, 0));
        findPanel.setMaximumSize(new Dimension(250, 100));
        findPanel.setMinimumSize(new Dimension(250, 75));
        findPanel.setPreferredSize(new Dimension(250, 75));
        infoPanePanel.add(findPanel, BorderLayout.NORTH);
        findPanel.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("ui").getString("find")));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        panel1.setMinimumSize(new Dimension(192, 24));
        panel1.setPreferredSize(new Dimension(192, 24));
        findPanel.add(panel1, BorderLayout.CENTER);
        previousMatchButton = new JButton();
        previousMatchButton.setPreferredSize(new Dimension(78, 24));
        this.$$$loadButtonText$$$(previousMatchButton, ResourceBundle.getBundle("ui").getString("previous"));
        panel1.add(previousMatchButton, BorderLayout.WEST);
        nextMatchButton = new JButton();
        nextMatchButton.setPreferredSize(new Dimension(78, 24));
        this.$$$loadButtonText$$$(nextMatchButton, ResourceBundle.getBundle("ui").getString("next"));
        panel1.add(nextMatchButton, BorderLayout.EAST);
        matchTextField = new JTextField();
        matchTextField.setPreferredSize(new Dimension(36, 24));
        panel1.add(matchTextField, BorderLayout.CENTER);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        findPanel.add(panel2, BorderLayout.SOUTH);
        caseSensitiveCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(caseSensitiveCheckBox, ResourceBundle.getBundle("ui").getString("case.sensitive"));
        panel2.add(caseSensitiveCheckBox, BorderLayout.WEST);
        regexCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(regexCheckBox, ResourceBundle.getBundle("ui").getString("regex"));
        panel2.add(regexCheckBox, BorderLayout.EAST);
        infoPanel = new JPanel();
        infoPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 2, new Insets(0, 0, 0, 0), -1, -1));
        infoPanel.setMaximumSize(new Dimension(500, 2147483647));
        infoPanel.setMinimumSize(new Dimension(250, 625));
        infoPanel.setPreferredSize(new Dimension(250, 625));
        infoPanePanel.add(infoPanel, BorderLayout.CENTER);
        infoPanelTitleLabel = new JLabel();
        Font infoPanelTitleLabelFont = this.$$$getFont$$$(null, Font.BOLD, 18, infoPanelTitleLabel.getFont());
        if (infoPanelTitleLabelFont != null) infoPanelTitleLabel.setFont(infoPanelTitleLabelFont);
        infoPanelTitleLabel.setHorizontalAlignment(0);
        infoPanelTitleLabel.setHorizontalTextPosition(0);
        this.$$$loadLabelText$$$(infoPanelTitleLabel, ResourceBundle.getBundle("ui").getString("annotation.information"));
        infoPanelTitleLabel.setVerticalAlignment(0);
        infoPanelTitleLabel.setVerticalTextPosition(0);
        infoPanel.add(infoPanelTitleLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, 25), new Dimension(250, 25), new Dimension(500, 25), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        infoPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(250, 100), new Dimension(250, 425), new Dimension(500, 2147483647), 0, false));
        spanList.setMaximumSize(new Dimension(-1, -1));
        spanList.setMinimumSize(new Dimension(-1, -1));
        spanList.setPreferredSize(new Dimension(-1, -1));
        scrollPane1.setViewportView(spanList);
        infoPanel.add(graphSpaceChooser, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("log4j").getString("graph.spaces.for.annotation"));
        infoPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Annotation ID");
        infoPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Class");
        infoPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        annotationClassLabel.setHorizontalAlignment(2);
        annotationClassLabel.setHorizontalTextPosition(2);
        annotationClassLabel.setVerticalAlignment(1);
        annotationClassLabel.setVerticalTextPosition(1);
        infoPanel.add(annotationClassLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(250, 25), new Dimension(250, 25), new Dimension(500, 25), 0, false));
        annotatorLabel.setHorizontalAlignment(2);
        annotatorLabel.setHorizontalTextPosition(2);
        annotatorLabel.setVerticalAlignment(1);
        annotatorLabel.setVerticalTextPosition(1);
        infoPanel.add(annotatorLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(250, 25), new Dimension(250, 25), new Dimension(500, 25), 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Annotator");
        infoPanel.add(label4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        annotationIDLabel.setHorizontalAlignment(2);
        annotationIDLabel.setHorizontalTextPosition(2);
        annotationIDLabel.setVerticalAlignment(1);
        annotationIDLabel.setVerticalTextPosition(1);
        infoPanel.add(annotationIDLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(250, 25), new Dimension(250, 25), new Dimension(500, 25), 0, false));
        annotationIDLabel.setLabelFor(matchTextField);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return infoPanePanel;
    }
}
