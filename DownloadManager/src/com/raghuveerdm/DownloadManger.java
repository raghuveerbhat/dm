package com.raghuveerdm;


import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
/**
 * Download Manager v1.0
 */

public class DownloadManger extends JFrame implements Observer {

    //download text field
    private JTextField addTextField;

    //table's data model
    private DownloadsTableModel tableModel;

    //Table listing downloads
    private JTable table;

    //Buttons to manage downloads
    private JButton pauseButton, resumeButton, cancelButton, clearButton;

    //currently selected download
    private Download selectedDownload;

    //flag for whether table selection is being cleared
    private boolean clearing;


    public DownloadManger() {
        //set app title
        setTitle("Download Manager");
        //System.out.println("Title set");
        //set window size
        setSize(640, 480);
        System.out.println("Title & size set");
        //Handle window closing events.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("closing window");
                actionExit();
            }
        });

        //Set up file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("exit menu action");
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        System.out.println("File menu set");

        //set up add panel
        JPanel addPanel = new JPanel();
        addTextField = new JTextField(30);
        addPanel.add(addTextField);
        JButton addButton = new JButton("Add Download");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("add button pressed");
                actionAdd();
            }
        });
        addPanel.add(addButton);
        System.out.println("panel buttons set");

        //Set up Downloads table
        tableModel = new DownloadsTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("Table selection changed");
                tableSelectionChanged();
            }
        });
        //allow only one row at a time to be selected
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        System.out.println("Downloads table set");

        //set up Progress bar
        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        //show progress text
        renderer.setStringPainted(true);
        table.setDefaultRenderer(JProgressBar.class, renderer);
        //Set table's row height to fit JProgressBar
        table.setRowHeight((int) renderer.getPreferredSize().getHeight());
        System.out.println("Progress bar set");

        //Set up downloads panel
        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        System.out.println("downloads panel set");

        //set up buttons panel
        JPanel buttonsPanel = new JPanel();
        //pause button
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Pressed pause");
                actionPause();
            }
        });
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton);
        System.out.println("Pause button set");
        //resume button
        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("resume button pressed");
                actionResume();
            }
        });
        resumeButton.setEnabled(false);
        buttonsPanel.add(resumeButton);
        System.out.println("Resume button set");
        //cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Cancel button pressed");
                actionCancel();
            }
        });
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);
        System.out.println("Cancel button set");
        //clear button
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Clear button pressed");
                actionClear();
            }
        });
        clearButton.setEnabled(false);
        buttonsPanel.add(clearButton);
        System.out.println("Clear button set");

        //add panel to display
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(addPanel, BorderLayout.NORTH);
        getContentPane().add(downloadsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        System.out.println("Added Panel to display");

    }

    private void actionExit() {
        System.out.println("Action: Exiting....");
        System.exit(0);
    }

    private void actionAdd() {
        URL verifiedUrl = verifyUrl(addTextField.getText());
        if(verifiedUrl != null) {
            System.out.println("Action: URL verified...");
            System.out.println("Action: Creating new Download object "+verifiedUrl+" ...");
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText("");//reset add text field
        } else {
            JOptionPane.showMessageDialog(this,"Invalid Download URL", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private URL verifyUrl(String url) {
        //only http and https
        if(!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://"))
                return null;

        //Verify format of URL
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }
        //check whether url speifies a file
        if(verifiedUrl.getFile().length() < 2)
            return null;

        return verifiedUrl;
    }

    /**
     * Called when table row selection changes
     */

    private void tableSelectionChanged() {
        //unregister
        if(selectedDownload != null) {
            selectedDownload.deleteObserver(DownloadManger.this);
        }
        /* If not in the middle of clearing a download,
            set the selected download and register to
            receive notifications from it. */
        if (!clearing && table.getSelectedRow() > -1) {
            selectedDownload = tableModel.getDownload(table.getSelectedRow());
            selectedDownload.addObserver(DownloadManger.this);
            updateButtons();
        }
    }

    /**
     * Pause the selected download
     */
    private void actionPause() {
        selectedDownload.pause();
        updateButtons();
    }

    /**
     * Resume selected download
     */
    private void actionResume() {
        selectedDownload.resume();
        updateButtons();
    }

    /**
     * Cancel the selected download
     */
    private void actionCancel() {
        selectedDownload.cancel();
        updateButtons();
    }

    /**
     * clear the selected download
     */
    private void actionClear() {
        clearing = true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing = false;
        selectedDownload = null;
        updateButtons();
    }

    /**
     * Update each buttons state based on
     * selected button status
     */
    private void updateButtons() {
        if(selectedDownload != null) {
            int status = selectedDownload.getStatus();
            switch (status) {
                case Download.DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.PAUSED:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.ERROR:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
                default: //complete or cancelled
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
            }
        } else {
            //no download is selected in table
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                cancelButton.setEnabled(false);
                clearButton.setEnabled(false);
        }
    }

    /**
     * Update is called when a Download
     * notifies its observers of any changes
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        // Update buttons if the selected download has changed.
        if (selectedDownload != null && selectedDownload.equals(o))
            updateButtons();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DownloadManger manger = new DownloadManger();
                manger.setVisible(true);
            }
        });
    }
}
