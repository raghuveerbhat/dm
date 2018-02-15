package com.raghuveerdm;

import javax.swing.*;
import javax.swing.table.*;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

class DownloadsTableModel extends AbstractTableModel implements Observer{

    //Names for table's column
    public static final String[] columnNames = {"URL", "Size", "Progress", "Status"};

    //classes for each column's values
    public static final Class[] columnClasses = {String.class, String.class, JProgressBar.class, String.class};

    //list of downloads
    private ArrayList<Download> downloadList = new ArrayList<Download>();

    public void addDownload(Download download) {
        //Register to be notified
        download.addObserver(this);

        downloadList.add(download);

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public Download getDownload(int row) {
        return downloadList.get(row);
    }

    public void clearDownload(int row) {
        downloadList.remove(row);

        fireTableRowsDeleted(row, row);
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    @Override
    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);

        //update notification to table
        fireTableRowsUpdated(index, index);
    }

    @Override
    public int getRowCount() {
        return downloadList.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Download download = downloadList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return download.getURL();
            case 1:
                int size = download.getSize();
                return (size == -1) ? "" : Integer.toString(size);
            case 2:
                return new Float(download.getProgress());
            case 3:
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }
}
