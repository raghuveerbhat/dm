package com.raghuveerdm;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

class ProgressRenderer extends JProgressBar implements TableCellRenderer {

    //Constructor
    public ProgressRenderer(int min, int max) {
        super(min,max);
    }

    /*-------------xxxxxxx---------------------------xxxxxx----------------------------------xxxxxx---------------*/
    /**
     * Returns this JProgressBar as the renderer
     for the given table cell.
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return this
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //set JProgressBar's percent complete value
        setValue((int) ((Float) value).floatValue());
        return this;
    }
}
