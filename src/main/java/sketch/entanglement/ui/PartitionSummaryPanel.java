package sketch.entanglement.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import entanglement.TraceSet;

public class PartitionSummaryPanel<IdType, ValType, TraceType> extends JPanel {
    private static final long serialVersionUID = 1L;
    
	public PartitionSummaryPanel(TraceSet<IdType, ValType, TraceType> traces) {
		List<IdType> idOrder = traces.idOrder();
		Map<IdType, Color> colorMapping = getColorMapping(traces);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        for (IdType id : idOrder) {
            JLabel angelLabel = new JLabel(id.toString(), SwingConstants.CENTER);
            angelLabel.setOpaque(true);
            angelLabel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

            Color c = colorMapping.get(id);
            if (c == null) {
                c = Color.white;
            }
            angelLabel.setBackground(c);
            add(angelLabel);
        }
        setVisible(true);
    }

	public PartitionSummaryPanel() {
		// TODO Auto-generated constructor stub
	}

	public Map<IdType, Color> getColorMapping(TraceSet<IdType, ValType, TraceType> traces) {
        Set<Set<IdType>> partitioning = traces.getEntangledPartitions();
        
        int numColoredPartitions = 0;
        
        for (Set<IdType> partition: partitioning) {
        	if (partition.size() > 1) {
        		numColoredPartitions++;
        	}
        }

        Map<IdType, Color> colors = new HashMap<IdType, Color>();

        int numSeen = 0;
        for (Set<IdType> partition: partitioning) {
        	Color color = null;
            if (partition.size() > 1) {
                color = Color.getHSBColor(numSeen * 1.0f / numColoredPartitions, .7f, .7f);
                numSeen++;
            }
            
            for (IdType id: partition) {
                colors.put(id, color);
            }
        }

        return colors;
    }
}
