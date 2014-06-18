package sketch.entanglement.ui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PartitionSummaryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
	public PartitionSummaryPanel(List<? extends Object> idOrder) {
        //Color[][] color = ec.getColorMatrix();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (Object id : idOrder) {
            JLabel angelLabel = new JLabel(id.toString(), SwingConstants.CENTER);
            angelLabel.setOpaque(true);
            angelLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

//            Color c = color[angel.staticAngelId][angel.execNum];
//            if (c == null) {
//                c = Color.white;
//            }
//            angelLabel.setBackground(c);
            add(angelLabel);
        }
        setVisible(true);
    }
}
