import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;

@SuppressWarnings("unused")

public class Hover extends MouseAdapter {
    private JLabel rightside;
	private JTextField textField; // Unused

	public Hover(JLabel rightside, JTextField textField ) {
        this.rightside = rightside;
		this.textField = textField;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		JLabel lbl = (JLabel) e.getComponent();
        lbl.setBackground(lbl.getBackground().darker());
        lbl.setOpaque(true);
        rightside.setText("<html>"+lbl.getText().replaceAll("\\n", "<br/>")+"</html>");
		//textField.setText(lbl.getText()); // Removed for Ease Of Use
	}

	@Override
	public void mouseExited(MouseEvent e) {
		JLabel lbl = (JLabel) e.getComponent();
        lbl.setBackground(lbl.getBackground().brighter());
	}
}