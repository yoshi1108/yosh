package uma.view

import java.awt.Color
import java.awt.Component
import java.awt.Font

import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JTable;
import javax.swing.border.BevelBorder
import javax.swing.border.EtchedBorder
import javax.swing.border.LineBorder
import javax.swing.table.TableCellRenderer

class TableRenderer extends JLabel implements TableCellRenderer {
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(value == null){return}
		JLabel label = new JLabel()
		String text;
		if(value instanceof String) {
			text = value;
		}else{
			label.setBackground(((JLabel)value).getBackground());
			label.setForeground(((JLabel)value).getForeground());
			text = ((JLabel)value).getText()
		}
		try {
			label.setText(text);
			label.setOpaque(true);
			// 選択されているかどうかで処理を分ける
			if (isSelected){
				label.setBorder(new BevelBorder(BevelBorder.LOWERED))
				label.setForeground(label.getForeground().darker());
				label.setBackground(label.getBackground().darker())
			} else{
				label.setBorder(new BevelBorder(BevelBorder.RAISED))
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return label;
	}
}
