package sketch.entanglement.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import entanglement.TraceSet;

public class PartitionPanel<IdType, ValType, TraceType> extends JPanel implements ActionListener {

    private enum Marking {
        GOOD, UNKNOWN, BAD
    }

    class ValueListElement {

        private final Map<IdType, ValType> trace;
        private Marking marking;
		private ArrayList<IdType> ids;

        public ValueListElement(Map<IdType, ValType> trace) {
            this.ids = new ArrayList<IdType>(trace.keySet());
        	this.trace = trace;
            marking = Marking.UNKNOWN;
        }

        public Map<IdType, ValType> getTrace() {
            return trace;
        }

        @Override
        public String toString() {
            String returnString = "";
            switch (marking) {
                case GOOD:
                    returnString += "+";
                    break;
                case BAD:
                    returnString += "-";
                    break;
                default:
            }
            
            for (IdType id : ids) {
            	returnString += id + "->" + trace.get(id) + ":";
            }
            return returnString;
        }

        public void mark(Marking m) {
            marking = m;
            System.out.println(marking);
        }
    }

    class PopupListener extends MouseAdapter {
        final private JPopupMenu popup;
        final private JList list;

        PopupListener(JList list, JPopupMenu popup) {
            this.list = list;
            this.popup = popup;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private List<IdType> ids;
    private Set<Map<IdType, ValType>> values;

//    private Set<Trace> goodValues;
//    private Set<Trace> badValues;

    private JList list;
    private JLabel label;
    private PartitionListModel model;
    private Vector<ValueListElement> listValues;
	private Set<IdType> partition;

    public PartitionPanel(Set<IdType> partition, TraceSet<IdType, ValType, TraceType> traces) {
        ids = traces.idOrder();
        this.partition = partition;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        addLabel(this);

        values = traces.getValues(partition);
        addList(this);
        addPopupMenu();
    }

    private void addLabel(JPanel panel) {
    	Set<IdType> partition = this.partition;
    	
        String text = "Attributes: ";
        for (int i = 0; i < ids.size(); i++) {
        	IdType id = ids.get(i);
        	
        	if (partition.contains(id)) {
	            text += id + " ";
	                
	        }
        }
        label = new JLabel(text);
        label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(label);
    }

    private void addList(JPanel panel) {
        listValues = new Vector<ValueListElement>();
        
        for (Map<IdType, ValType> value : values) {
            listValues.add(new ValueListElement(value));
        }

        label.setText("[" + listValues.size() + "X" + partition.size() + "]" + label.getText());

        model = new PartitionListModel(listValues);
        list = new JList(model);
        list.setSelectedIndex(0);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        list.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        panel.add(list);
    }

    private void addPopupMenu() {
        JMenuItem menuItem;

        // Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        menuItem = new JMenuItem("Mark as good");
        menuItem.setActionCommand("good");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Mark as bad");
        menuItem.setActionCommand("bad");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Reset");
        menuItem.setActionCommand("reset");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        // Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(list, popup);
        list.addMouseListener(popupListener);
    }

    public Map<IdType, ValType> getSelectedValue() {
        Object values[] = list.getSelectedValues();
        if (values.length == 1) {
            return ((ValueListElement) values[0]).getTrace();
        }
        return null;
    }

    public JList getList() {
        return list;
    }

    public void actionPerformed(ActionEvent event) {
        ValueListElement selected = (ValueListElement) list.getSelectedValue();
        JMenuItem item = (JMenuItem) event.getSource();
        if ("good".equals(item.getActionCommand())) {
            selected.mark(Marking.GOOD);
        } else if ("bad".equals(item.getActionCommand())) {
            selected.mark(Marking.BAD);
        } else if ("reset".equals(item.getActionCommand())) {
            selected.mark(Marking.UNKNOWN);
        }
        int index = list.getSelectedIndex();
        model.updateIndex(index);
    }

    public Set<Map<IdType, ValType>> getGoodTraces() {
        HashSet<Map<IdType, ValType>> goodTraces = new HashSet<Map<IdType, ValType>>();
        for (ValueListElement listValue : listValues) {
            if (listValue.marking == Marking.GOOD) {
                goodTraces.add(listValue.trace);
            }
        }
        return goodTraces;
    }

    public Set<Map<IdType, ValType>> getBadTraces() {
        HashSet<Map<IdType, ValType>> badTraces = new HashSet<Map<IdType, ValType>>();
        for (ValueListElement listValue : listValues) {
            if (listValue.marking == Marking.BAD) {
                badTraces.add(listValue.trace);
            }
        }
        return badTraces;
    }

    public Set<IdType> getAngelPartition() {
        return new HashSet<IdType>(ids);
    }
}
