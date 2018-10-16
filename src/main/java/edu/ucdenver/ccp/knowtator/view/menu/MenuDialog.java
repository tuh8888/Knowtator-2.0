package edu.ucdenver.ccp.knowtator.view.menu;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import edu.ucdenver.ccp.knowtator.view.KnowtatorView;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

public class MenuDialog extends JDialog {
    private static final Logger log = Logger.getLogger(MenuDialog.class);

    private JPanel contentPane;
    private JList<String> menuOptionsList;
    private JPanel menuDisplayPane;
    private final KnowtatorView view;

    public MenuDialog(Window parent, KnowtatorView view) {
        super(parent);
        this.view = view;
        $$$setupUI$$$();
        setLocationRelativeTo(view);
        setContentPane(contentPane);
        setModal(true);
        setSize(500, 200);
        setTitle("Knowtator Menu");
        try {
            setIconImage(ImageIO.read(getClass().getResource("/ccp_logo.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        menuOptionsList.addListSelectionListener(e -> {
            switch (menuOptionsList.getSelectedValue()) {
                case "Open":
                    showOpenPane();
                    break;
                case "New":
                    showNewPane();
                    break;
                case "Import":
                    showImportPane();
                    break;
                case "Export":
                    showExportPane();
                    break;
                case "Profile":
                    showProfilePane();
                    break;
                case "IAA":
                    showIAAPane();
                    break;
            }
        });


    }

    private void showNewPane() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select project root");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        NewPane newPane = new NewPane();
        newPane.getFileChooserPane().add(fileChooser, BorderLayout.CENTER);

        menuDisplayPane.removeAll();
        menuDisplayPane.add(newPane.getContentPane(), BorderLayout.CENTER);
        pack();

        fileChooser.addActionListener(e -> {
            String projectName = newPane.getTextField1().getText();
            if (!projectName.equals("") && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                dispose();
                File projectDirectory = new File(fileChooser.getSelectedFile(), projectName);
                view.reset();
                view.getController().newProject(projectDirectory);

                view.getPreferences().put("Last Project", view.getController().getProjectLocation().getAbsolutePath());
            }
        });
    }

    private void showOpenPane() {
        String lastProjectFileName = view.getPreferences().get("Last Project", null);

        JFileChooser fileChooser = new JFileChooser();
        if (lastProjectFileName != null) {
            File lastProjectFile = new File(lastProjectFileName);
            if (lastProjectFile.exists()) {
                fileChooser.setCurrentDirectory(lastProjectFile.getParentFile());
                fileChooser.setSelectedFile(lastProjectFile);
            }
        }
        FileFilter fileFilter = new FileNameExtensionFilter("Knowtator", "knowtator");
        fileChooser.setFileFilter(fileFilter);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        menuDisplayPane.removeAll();
        menuDisplayPane.add(fileChooser, BorderLayout.CENTER);
        pack();
        fileChooser.addActionListener(e -> {
            if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
                File file = fileChooser.getSelectedFile();
                dispose();
                view.reset();
                try {
                    view.getController().setSaveLocation(file.getParentFile());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                log.warn(String.format("Opening from %s", file.getAbsolutePath()));
                view.getController().loadProject();
                view.getKnowtatorTextPane().refreshHighlights();
                view.getAddTextSourceButton().setEnabled(true);

                view.getPreferences().put("Last Project", file.getAbsolutePath());

                try {
                    view.getPreferences().flush();
                } catch (BackingStoreException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private void showImportPane() {
        menuDisplayPane.removeAll();
        menuDisplayPane.add(new ImportPane(this, view).getContentPane(), BorderLayout.CENTER);
        pack();
    }

    private void showExportPane() {
        menuDisplayPane.removeAll();
        menuDisplayPane.add(new ExportPane(view).getContentPane(), BorderLayout.CENTER);
        pack();
    }

    private void showIAAPane() {
        menuDisplayPane.removeAll();
        menuDisplayPane.add(new IAAPane(view).getContentPane(), BorderLayout.CENTER);
        pack();
    }

    private void showProfilePane() {
        menuDisplayPane.removeAll();
        menuDisplayPane.add(new ProfilePane(view).getContentPane(), BorderLayout.CENTER);
        pack();
    }

    private void createUIComponents() {
        menuOptionsList = new JList<>(new String[]{"Open", "New", "Import", "Export", "Profile", "IAA"});
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
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setPreferredSize(new Dimension(700, 500));
        final JSplitPane splitPane1 = new JSplitPane();
        contentPane.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        menuDisplayPane = new JPanel();
        menuDisplayPane.setLayout(new BorderLayout(0, 0));
        splitPane1.setRightComponent(menuDisplayPane);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        splitPane1.setLeftComponent(panel1);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, BorderLayout.CENTER);
        menuOptionsList.setBackground(new Color(-1118482));
        menuOptionsList.setFocusCycleRoot(true);
        Font menuOptionsListFont = this.$$$getFont$$$(null, -1, 16, menuOptionsList.getFont());
        if (menuOptionsListFont != null) menuOptionsList.setFont(menuOptionsListFont);
        menuOptionsList.setForeground(new Color(-16777216));
        scrollPane1.setViewportView(menuOptionsList);
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, BorderLayout.SOUTH);
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
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
