package edu.ucdenver.ccp.knowtator.view;

import com.mxgraph.swing.util.mxGraphTransferable;
import edu.ucdenver.ccp.knowtator.KnowtatorController;
import edu.ucdenver.ccp.knowtator.listeners.OWLClassSelectionListener;
import edu.ucdenver.ccp.knowtator.listeners.OWLObjectPropertySelectionListener;
import edu.ucdenver.ccp.knowtator.model.Profile;
import edu.ucdenver.ccp.knowtator.model.Span;
import edu.ucdenver.ccp.knowtator.model.TextSource;
import edu.ucdenver.ccp.knowtator.model.owl.OWLWorkSpaceNotSetException;
import edu.ucdenver.ccp.knowtator.view.chooser.ProfileChooser;
import edu.ucdenver.ccp.knowtator.view.chooser.TextSourceChooser;
import edu.ucdenver.ccp.knowtator.view.menu.ProjectMenu;
import edu.ucdenver.ccp.knowtator.view.textpane.KnowtatorTextPane;
import edu.ucdenver.ccp.knowtator.view.textpane.MainKnowtatorTextPane;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.Set;

public class KnowtatorView extends AbstractOWLClassViewComponent
		implements DropTargetListener, OWLClassSelectionListener, OWLObjectPropertySelectionListener {

	private static final Logger log = Logger.getLogger(KnowtatorView.class);
	private KnowtatorController controller;
	private GraphViewDialog graphViewDialog;
	private JMenu projectMenu;
	private JComponent panel1;
	private JButton previousMatchButton;
	private JButton nextMatchButton;
	private JTextField matchTextField;
	private JCheckBox caseSensitiveCheckBox;
	private JCheckBox regexCheckBox;
	private JButton showGraphViewerButton;
	private JButton removeAnnotationButton;
	private JButton growSelectionStartButton;
	private JButton shrinkSelectionEndButton;
	private JButton growSelectionEndButton;
	private JButton shrinkSelectionStartButton;
	private JButton addAnnotationButton;
	private JButton decreaseFontSizeButton;
	private JButton increaseFontSizeButton;
	private JButton previousTextSourceButton;
	private JButton nextTextSourceButton;
	private JButton nextSpanButton;
	private JButton previousSpanButton;
	private JButton assignColorToClassButton;
	private JCheckBox profileFilterCheckBox;
	private KnowtatorTextPane knowtatorTextPane;
	private JToolBar textSourceToolBar;
	private JToolBar annotationToolBar;
	private JPanel findPanel;
	private JPanel textPanel;
	private JSplitPane infoPane;
	private JSplitPane mainPane;
	private JPanel infoPanel;
	private JLabel infoPanelTitleLabel;
	private SpanList spanList;
	private AnnotatorLabel annotatorLabel;
	private AnnotationIDLabel annotationIDLabel;
	private AnnotationClassLabel annotationClassLabel;
	private TextSourceChooser textSourceChooser;
	private ProfileChooser profileChooser;
	private JButton textToGraphButton;
	private JButton findTextButton;

	public KnowtatorView() {
		makeController();
		$$$setupUI$$$();
		makeButtons();

		controller.getSelectionManager().addOWLClassListener(this);
		controller.getSelectionManager().addOWLObjectPropertyListener(this);

		// This is necessary to force OSGI to load the mxGraphTransferable class to allow node dragging.
		// It is kind of a hacky fix, but it works for now.

		log.warn("Don't worry about the following exception. Just forcing loading of a class needed by mxGraph");
		try {
			mxGraphTransferable.dataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=com.mxgraph.swing.util.mxGraphTransferable", null, mxGraphTransferable.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void makeController() {
		log.warn("KnowtatorView: Making controller");
		controller = new KnowtatorController();

		setUpOWL();
	}

	private void setUpOWL() {
		OWLWorkspace workspace = null;
		try {
			workspace = controller.getOWLAPIDataExtractor().getWorkSpace();
		} catch (OWLWorkSpaceNotSetException ignored) {

		}
		if (workspace == null) {
			if (getOWLWorkspace() != null) {
				controller.getOWLAPIDataExtractor().setUpOWL(getOWLWorkspace());
				log.warn("Adding class label as renderer listener");
				getOWLWorkspace().getOWLModelManager().addListener(annotationClassLabel);
			}
		}

	}

	public KnowtatorController getController() {
		return controller;
	}

	@Override
	public void initialiseClassView() {
	}

	private void createUIComponents() {
		DropTarget dt = new DropTarget(this, this);
		dt.setActive(true);

		panel1 = this;
		projectMenu = new ProjectMenu(this);
		knowtatorTextPane = new MainKnowtatorTextPane(this);
		graphViewDialog = new GraphViewDialog(this);
		annotationIDLabel = new AnnotationIDLabel(this);
		annotationClassLabel = new AnnotationClassLabel(this);
		annotatorLabel = new AnnotatorLabel(this);
		spanList = new SpanList(this);
		textSourceChooser = new TextSourceChooser(this);
		profileChooser = new ProfileChooser(this);
	}

	private void makeButtons() {

		findTextButton.addActionListener(e -> {
			try {
				controller.getOWLAPIDataExtractor().searchForString(knowtatorTextPane.getSelectedText());
			} catch (OWLWorkSpaceNotSetException ignored) {

			}
		});

		assignColorToClassButton.addActionListener(
				e -> {
					OWLClass owlClass = controller.getSelectionManager().getSelectedOWLClass();
					if (owlClass == null) {
						if (controller.getProjectManager().isProjectLoaded()) {
							owlClass = controller.getSelectionManager().getSelectedAnnotation().getOwlClass();
						}
					}
					if (owlClass != null) {
						Color c = JColorChooser.showDialog(this, "Pick a color for " + owlClass, Color.CYAN);
						if (c != null) {
							controller.getSelectionManager().getActiveProfile().addColor(owlClass, c);

							if (JOptionPane.showConfirmDialog(
									this, "Assign color to descendants of " + owlClass + "?")
									== JOptionPane.OK_OPTION) {
								try {
									Set<OWLClass> descendants =
											controller.getOWLAPIDataExtractor().getDescendants(owlClass);

									for (OWLClass descendant : descendants) {
										controller.getSelectionManager().getActiveProfile().addColor(descendant, c);
									}
								} catch (OWLWorkSpaceNotSetException ignored) {
								}
							}
						}
					}
				});

		profileFilterCheckBox.addChangeListener(controller.getSelectionManager());

		growSelectionStartButton.addActionListener(
				(ActionEvent e) -> {
					if (controller.getSelectionManager().getSelectedSpan() == null) {
						knowtatorTextPane.growStart();
					} else {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.growSelectedSpanStart();
					}
				});
		shrinkSelectionStartButton.addActionListener(
				(ActionEvent e) -> {
					if (controller.getSelectionManager().getSelectedSpan() == null) {
						knowtatorTextPane.shrinkStart();
					} else {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.shrinkSelectedSpanStart();
					}
				});
		shrinkSelectionEndButton.addActionListener(
				(ActionEvent e) -> {
					if (controller.getSelectionManager().getSelectedSpan() == null) {
						knowtatorTextPane.shrinkEnd();
					} else {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.shrinkSelectedSpanEnd();
					}
				});
		growSelectionEndButton.addActionListener(
				(ActionEvent e) -> {
					if (controller.getSelectionManager().getSelectedSpan() == null) {
						knowtatorTextPane.growEnd();
					} else {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.growSelectedSpanEnd();
					}
				});

		previousSpanButton.addActionListener(
				(ActionEvent e) -> controller.getSelectionManager().getPreviousSpan());
		nextSpanButton.addActionListener(
				(ActionEvent e) -> controller.getSelectionManager().getNextSpan());

		showGraphViewerButton.addActionListener(e -> {
			if (controller.getProjectManager().isProjectLoaded()) {
				graphViewDialog.setVisible(true);
			}
		});

		removeAnnotationButton.addActionListener(
				e -> {
					if (controller.getProjectManager().isProjectLoaded()
							&& JOptionPane.showConfirmDialog(
							this,
							"Are you sure you want to remove the selected annotation?",
							"Remove Annotation",
							JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION) {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.removeSelectedAnnotation();
					}
				});
		addAnnotationButton.addActionListener(
				e -> {
					if (controller.getProjectManager().isProjectLoaded()) {
						controller
								.getSelectionManager()
								.getActiveTextSource()
								.getAnnotationManager()
								.addSelectedAnnotation();
					}
				});

		previousTextSourceButton.addActionListener(
				e -> controller.getSelectionManager().getPreviousTextSource());
		nextTextSourceButton.addActionListener(
				e -> controller.getSelectionManager().getNextTextSource());

		decreaseFontSizeButton.addActionListener(
				(ActionEvent e) -> knowtatorTextPane.decreaseFontSize());
		increaseFontSizeButton.addActionListener(
				(ActionEvent e) -> knowtatorTextPane.increaseFindSize());
		textSourceChooser.addActionListener(
				e -> {
					JComboBox comboBox = (JComboBox) e.getSource();
					if (comboBox.getSelectedItem() != null) {
						controller.getSelectionManager().setSelectedTextSource((TextSource) comboBox.getSelectedItem());
					}
				});
		profileChooser.addActionListener(
				e -> {
					JComboBox comboBox = (JComboBox) e.getSource();
					if (comboBox.getSelectedItem() != null) {
						controller.getSelectionManager().setSelectedProfile((Profile) comboBox.getSelectedItem());
					}
				});
		spanList.addListSelectionListener(
				e -> {
					JList jList = (JList) e.getSource();
					if (jList.getSelectedValue() != null) {
						controller.getSelectionManager().setSelectedSpan((Span) jList.getSelectedValue());
					}
				});
		nextMatchButton.addActionListener(
				e -> {
					String textToFind = matchTextField.getText();
					KnowtatorTextPane currentKnowtatorTextPane = getKnowtatorTextPane();
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
					String textToSearch = knowtatorTextPane.getText();
					if (!caseSensitiveCheckBox.isSelected()) {
						textToSearch = textToSearch.toLowerCase();
					}
					int matchLoc =
							textToSearch.lastIndexOf(textToFind, knowtatorTextPane.getSelectionStart() - 1);
					if (matchLoc != -1) {
						knowtatorTextPane.requestFocusInWindow();
						knowtatorTextPane.select(matchLoc, matchLoc + textToFind.length());
					} else {
						knowtatorTextPane.setSelectionStart(-1);
					}
				});

		textToGraphButton.addActionListener(
				e -> {
					if (controller.getProjectManager().isProjectLoaded()) {
						controller
								.getSelectionManager()
								.getActiveGraphSpace()
								.setGraphText(
										knowtatorTextPane.getSelectionStart(), knowtatorTextPane.getSelectionEnd());
					}
				});
	}

	private void owlEntitySelectionChanged(OWLEntity owlEntity) {
		if (getView() != null) {
			if (getView().isSyncronizing()) {
				getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(owlEntity);
			}
		}
	}

	@Override
	protected OWLClass updateView(OWLClass selectedClass) {
		setUpOWL();
		controller.getSelectionManager().setSelectedOWLClass(selectedClass);
		return selectedClass;
	}

	@Override
	public void disposeView() {
		if (controller.getProjectManager().isProjectLoaded()
				&& JOptionPane.showConfirmDialog(
				this,
				"Save changes to Knowtator project?",
				"Save Project",
				JOptionPane.YES_NO_OPTION)
				== JOptionPane.YES_OPTION) {
			controller.getProjectManager().saveProject();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent e) {
	}

	@Override
	public void dragExit(DropTargetEvent e) {
	}

	@Override
	public void drop(DropTargetDropEvent e) {
	}

	public KnowtatorTextPane getKnowtatorTextPane() {
		return knowtatorTextPane;
	}

	@Override
	public void dragEnter(DropTargetDragEvent e) {
	}

	@Override
	public void dragOver(DropTargetDragEvent e) {
	}

	public JMenu getProjectMenu() {
		return projectMenu;
	}

	public GraphViewDialog getGraphViewDialog() {
		return graphViewDialog;
	}


	@Override
	public void owlClassChanged(OWLClass owlClass) {
		owlEntitySelectionChanged(owlClass);
	}

	@Override
	public void owlObjectPropertyChanged(OWLObjectProperty owlObjectProperty) {
		owlEntitySelectionChanged(owlObjectProperty);
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
		panel1.setLayout(new BorderLayout(0, 0));
		mainPane = new JSplitPane();
		mainPane.setDividerLocation(1536);
		mainPane.setOneTouchExpandable(true);
		panel1.add(mainPane, BorderLayout.CENTER);
		infoPane = new JSplitPane();
		infoPane.setOrientation(0);
		mainPane.setRightComponent(infoPane);
		findPanel = new JPanel();
		findPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.setLeftComponent(findPanel);
		findPanel.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("ui").getString("find")));
		nextMatchButton = new JButton();
		this.$$$loadButtonText$$$(nextMatchButton, ResourceBundle.getBundle("ui").getString("next"));
		findPanel.add(nextMatchButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		previousMatchButton = new JButton();
		this.$$$loadButtonText$$$(previousMatchButton, ResourceBundle.getBundle("ui").getString("previous"));
		findPanel.add(previousMatchButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		matchTextField = new JTextField();
		findPanel.add(matchTextField, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		caseSensitiveCheckBox = new JCheckBox();
		this.$$$loadButtonText$$$(caseSensitiveCheckBox, ResourceBundle.getBundle("ui").getString("case.sensitive"));
		findPanel.add(caseSensitiveCheckBox, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		regexCheckBox = new JCheckBox();
		this.$$$loadButtonText$$$(regexCheckBox, ResourceBundle.getBundle("ui").getString("regex"));
		findPanel.add(regexCheckBox, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		infoPanel = new JPanel();
		infoPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
		infoPane.setRightComponent(infoPanel);
		infoPanelTitleLabel = new JLabel();
		Font infoPanelTitleLabelFont = this.$$$getFont$$$(null, Font.BOLD, 18, infoPanelTitleLabel.getFont());
		if (infoPanelTitleLabelFont != null) infoPanelTitleLabel.setFont(infoPanelTitleLabelFont);
		infoPanelTitleLabel.setHorizontalAlignment(0);
		infoPanelTitleLabel.setHorizontalTextPosition(0);
		this.$$$loadLabelText$$$(infoPanelTitleLabel, ResourceBundle.getBundle("ui").getString("annotation.information"));
		infoPanelTitleLabel.setVerticalAlignment(0);
		infoPanelTitleLabel.setVerticalTextPosition(0);
		infoPanel.add(infoPanelTitleLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		infoPanel.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane1.setViewportView(spanList);
		annotationIDLabel.setHorizontalAlignment(2);
		annotationIDLabel.setHorizontalTextPosition(2);
		annotationIDLabel.setVerticalAlignment(1);
		annotationIDLabel.setVerticalTextPosition(1);
		infoPanel.add(annotationIDLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		annotationClassLabel.setHorizontalAlignment(2);
		annotationClassLabel.setHorizontalTextPosition(2);
		annotationClassLabel.setVerticalAlignment(1);
		annotationClassLabel.setVerticalTextPosition(1);
		infoPanel.add(annotationClassLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		annotatorLabel.setHorizontalAlignment(2);
		annotatorLabel.setHorizontalTextPosition(2);
		annotatorLabel.setVerticalAlignment(1);
		annotatorLabel.setVerticalTextPosition(1);
		infoPanel.add(annotatorLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTHWEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		textPanel = new JPanel();
		textPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
		mainPane.setLeftComponent(textPanel);
		textPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null));
		textSourceToolBar = new JToolBar();
		textSourceToolBar.setFloatable(false);
		textPanel.add(textSourceToolBar, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
		decreaseFontSizeButton = new JButton();
		decreaseFontSizeButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-Decrease Font (Custom).png")));
		decreaseFontSizeButton.setText("");
		decreaseFontSizeButton.setToolTipText(ResourceBundle.getBundle("ui").getString("decrease.font.size"));
		textSourceToolBar.add(decreaseFontSizeButton);
		increaseFontSizeButton = new JButton();
		increaseFontSizeButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-Increase Font (Custom).png")));
		increaseFontSizeButton.setText("");
		increaseFontSizeButton.setToolTipText(ResourceBundle.getBundle("ui").getString("increase.font.size"));
		textSourceToolBar.add(increaseFontSizeButton);
		previousTextSourceButton = new JButton();
		previousTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/Previous Document (Custom).png")));
		previousTextSourceButton.setText("");
		previousTextSourceButton.setToolTipText(ResourceBundle.getBundle("ui").getString("previous.text.source"));
		textSourceToolBar.add(previousTextSourceButton);
		nextTextSourceButton = new JButton();
		nextTextSourceButton.setIcon(new ImageIcon(getClass().getResource("/icon/Next Document (Custom).png")));
		nextTextSourceButton.setText("");
		nextTextSourceButton.setToolTipText(ResourceBundle.getBundle("ui").getString("next.text.source"));
		textSourceToolBar.add(nextTextSourceButton);
		final JLabel label1 = new JLabel();
		this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("ui").getString("document"));
		textSourceToolBar.add(label1);
		textSourceToolBar.add(textSourceChooser);
		final JLabel label2 = new JLabel();
		this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("ui").getString("profile"));
		textSourceToolBar.add(label2);
		textSourceToolBar.add(profileChooser);
		annotationToolBar = new JToolBar();
		annotationToolBar.setFloatable(false);
		textPanel.add(annotationToolBar, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
		showGraphViewerButton = new JButton();
		showGraphViewerButton.setIcon(new ImageIcon(getClass().getResource("/icon/Show Graph Viewer (Custom).png")));
		showGraphViewerButton.setText("");
		showGraphViewerButton.setToolTipText(ResourceBundle.getBundle("ui").getString("show.graph.viewer"));
		annotationToolBar.add(showGraphViewerButton);
		textToGraphButton = new JButton();
		this.$$$loadButtonText$$$(textToGraphButton, ResourceBundle.getBundle("ui").getString("text.to.graph"));
		textToGraphButton.setToolTipText(ResourceBundle.getBundle("ui").getString("send.selected.text.to.graph"));
		annotationToolBar.add(textToGraphButton);
		findTextButton = new JButton();
		this.$$$loadButtonText$$$(findTextButton, ResourceBundle.getBundle("log4j").getString("find.in.ontology"));
		annotationToolBar.add(findTextButton);
		addAnnotationButton = new JButton();
		addAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/Add annotation Node (Custom).png")));
		addAnnotationButton.setText("");
		addAnnotationButton.setToolTipText(ResourceBundle.getBundle("ui").getString("add.annotation"));
		annotationToolBar.add(addAnnotationButton);
		removeAnnotationButton = new JButton();
		removeAnnotationButton.setIcon(new ImageIcon(getClass().getResource("/icon/Remove Annotation (Custom).png")));
		removeAnnotationButton.setText("");
		removeAnnotationButton.setToolTipText(ResourceBundle.getBundle("ui").getString("remove.annotation"));
		annotationToolBar.add(removeAnnotationButton);
		previousSpanButton = new JButton();
		previousSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/Previous Annotation (Custom).png")));
		previousSpanButton.setText("");
		previousSpanButton.setToolTipText(ResourceBundle.getBundle("ui").getString("previous.span"));
		annotationToolBar.add(previousSpanButton);
		growSelectionStartButton = new JButton();
		growSelectionStartButton.setIcon(new ImageIcon(getClass().getResource("/icon/Increase Span Left (Custom).png")));
		growSelectionStartButton.setText("");
		growSelectionStartButton.setToolTipText(ResourceBundle.getBundle("ui").getString("grow.selection.start"));
		annotationToolBar.add(growSelectionStartButton);
		shrinkSelectionStartButton = new JButton();
		shrinkSelectionStartButton.setIcon(new ImageIcon(getClass().getResource("/icon/Decrease Span Left (Custom).png")));
		shrinkSelectionStartButton.setText("");
		shrinkSelectionStartButton.setToolTipText(ResourceBundle.getBundle("ui").getString("shrink.selection.start"));
		annotationToolBar.add(shrinkSelectionStartButton);
		shrinkSelectionEndButton = new JButton();
		shrinkSelectionEndButton.setIcon(new ImageIcon(getClass().getResource("/icon/Decrease Span Right (Custom).png")));
		shrinkSelectionEndButton.setText("");
		shrinkSelectionEndButton.setToolTipText(ResourceBundle.getBundle("ui").getString("shrink.selection.end"));
		annotationToolBar.add(shrinkSelectionEndButton);
		growSelectionEndButton = new JButton();
		growSelectionEndButton.setIcon(new ImageIcon(getClass().getResource("/icon/Increase Span Right (Custom).png")));
		growSelectionEndButton.setText("");
		growSelectionEndButton.setToolTipText(ResourceBundle.getBundle("ui").getString("grow.selection.end"));
		annotationToolBar.add(growSelectionEndButton);
		nextSpanButton = new JButton();
		nextSpanButton.setIcon(new ImageIcon(getClass().getResource("/icon/Next Annotation (Custom).png")));
		nextSpanButton.setText("");
		nextSpanButton.setToolTipText(ResourceBundle.getBundle("ui").getString("next.span"));
		annotationToolBar.add(nextSpanButton);
		assignColorToClassButton = new JButton();
		assignColorToClassButton.setIcon(new ImageIcon(getClass().getResource("/icon/icons8-color-dropper-filled-50 (Custom).png")));
		assignColorToClassButton.setText("");
		assignColorToClassButton.setToolTipText(ResourceBundle.getBundle("ui").getString("assign.color.to.class"));
		annotationToolBar.add(assignColorToClassButton);
		profileFilterCheckBox = new JCheckBox();
		this.$$$loadButtonText$$$(profileFilterCheckBox, ResourceBundle.getBundle("ui").getString("profile.filter"));
		profileFilterCheckBox.setToolTipText(ResourceBundle.getBundle("ui").getString("filter.annotations.by.profile"));
		annotationToolBar.add(profileFilterCheckBox);
		final JScrollPane scrollPane2 = new JScrollPane();
		textPanel.add(scrollPane2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		knowtatorTextPane.setBackground(new Color(-1));
		knowtatorTextPane.setEditable(false);
		knowtatorTextPane.setFocusTraversalPolicyProvider(true);
		knowtatorTextPane.setFocusable(false);
		knowtatorTextPane.setForeground(new Color(-16777216));
		knowtatorTextPane.setText("");
		scrollPane2.setViewportView(knowtatorTextPane);
		final JMenuBar menuBar1 = new JMenuBar();
		menuBar1.setLayout(new BorderLayout(0, 0));
		panel1.add(menuBar1, BorderLayout.NORTH);
		projectMenu.setSelected(false);
		this.$$$loadButtonText$$$(projectMenu, ResourceBundle.getBundle("ui").getString("knowator.project"));
		menuBar1.add(projectMenu, BorderLayout.WEST);
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
		return panel1;
	}
}
