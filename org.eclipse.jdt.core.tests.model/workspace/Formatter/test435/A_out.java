public class A
{

    public void temp()
    {
        boolean minWidthOn = true;
        if (minWidthOn)
        {
            // If a user - calls this method, turn off auto resize
            this.setAutoResizeMode(AUTO_RESIZE_OFF);
            // int columnCount = getColumnCount();
            int columnCount = this.getColumnModel().getColumnCount();
            for (int i = 0; i < columnCount; i++)
            {
                // Get the column
                TableColumn col = this.getColumn(this.getColumnName(i));
                int tempSize = widestCellColumn(col);
                // Set the minimum width for a specific column
                col.setMinWidth(tempSize);
            }
        }
    }
}