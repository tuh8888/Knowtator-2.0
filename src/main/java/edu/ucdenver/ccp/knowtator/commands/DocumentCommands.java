package edu.ucdenver.ccp.knowtator.commands;

import com.google.common.io.Files;
import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.ui.KnowtatorIcons;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public class DocumentCommands {

    private KnowtatorManager manager;

    public DocumentCommands(KnowtatorManager manager) {

        this.manager = manager;
    }

    public KnowtatorCommand getCloseDocumentCommand() {
        return new KnowtatorCommand(manager, "Close", KnowtatorIcons.CLOSE_DOCUMENT_ICON, "Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                manager.getKnowtatorView().getTextViewer().closeSelectedDocument();
            }
        };
    }

    public KnowtatorCommand openDocumentCommand() {
        return new KnowtatorCommand(manager, "Open Document", KnowtatorIcons.OPEN_DOCUMENT_ICON, "Open a text document") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                //
                // disable the "All files" option.
                //
                fileChooser.setAcceptAllFileFilterUsed(false);

                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File directory = fileChooser.getSelectedFile();
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            switch (Files.getFileExtension(file.getAbsolutePath())) {
                                case ("txt"):
                                    manager.getKnowtatorView().getTextViewer().addNewDocument(file.getAbsolutePath(), false);
                                case ("xml"):
                                    manager.getXmlUtil().read(file.getAbsolutePath(), false);
                            }
                        }
                    }
                }
            }
        };
    }

    public KnowtatorCommand getLoadTextAnnotationsCommand() {
        return new KnowtatorCommand(manager, "Load Annotations", KnowtatorIcons.LOAD_ANNOTATIONS_ICON, "Load annotations") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileFilter fileFilter = new FileNameExtensionFilter(manager.getConfigProperties().getFormat().toUpperCase(), manager.getConfigProperties().getFormat());
                fileChooser.setFileFilter(fileFilter);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    manager.getXmlUtil().read(fileChooser.getSelectedFile().getAbsolutePath(), false);
                }
            }
        };
    }
}


