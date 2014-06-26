package sketch.entanglement.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;

@SuppressWarnings("serial")
public class DisentanglementGui<IdType, ValType, TraceType> extends DisentanglementGuiBase implements ActionListener {

    private List<IdType> idOrder;
    private List<IdType> origIdOrder;

    private List<Set<IdType>> partitions;

    private JList chooseList;
    private JList partitionList;
    private EntanglementGuiPanel<IdType, ValType, TraceType> parent;

    public DisentanglementGui(EntanglementGuiPanel<IdType, ValType, TraceType> parent,
            List<IdType> ids)
    {
        super(parent.getFrame(), false);
        this.parent = parent;

        partitions = new ArrayList<Set<IdType>>();
        origIdOrder = new ArrayList<IdType>(ids);
        idOrder = new ArrayList<IdType>(ids);

        chooseList = getChooseList();
        chooseList.setListData(idOrder.toArray());

        partitionList = getPartitionList();

        JButton createButton = getCreateButton();
        createButton.setActionCommand("create");
        createButton.addActionListener(this);

        JButton removeButton = getRemoveButton();
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);

        JButton refineButton = getRefineButton();
        refineButton.setActionCommand("refine");
        refineButton.addActionListener(this);
    }

    @SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent event) {
        if ("create".equals(event.getActionCommand())) {
            Object[] selected = chooseList.getSelectedValues();
            if (selected.length > 0) {
                Set<IdType> partition = new HashSet<IdType>();
                for (Object id : selected) {
                    partition.add((IdType) id);
                }
                idOrder.removeAll(partition);
                partitions.add(partition);

                chooseList.setListData(idOrder.toArray());
                partitionList.setListData(partitions.toArray());
            }
        } else if ("remove".equals(event.getActionCommand())) {
            Object[] selected = partitionList.getSelectedValues();
            if (selected.length > 0) {
                HashSet<IdType> addedIds = new HashSet<IdType>();
                for (Object partition : selected) {
                    HashSet<IdType> partitionSet = (HashSet<IdType>) partition;
                    addedIds.addAll(partitionSet);
                    partitions.remove(partitionSet);
                }
                Vector<IdType> newIdOrder = new Vector<IdType>();
                for (IdType id : origIdOrder) {
                    if (idOrder.contains(id) || addedIds.contains(id)) {
                        newIdOrder.add(id);
                    }
                }

                idOrder = newIdOrder;
                chooseList.setListData(idOrder.toArray());
                partitionList.setListData(partitions.toArray());
            }
        } else if ("refine".equals(event.getActionCommand())) {
            parent.partitionTraces(new HashSet<Set<IdType>>(partitions));
            setVisible(false);
            dispose();
        }
    }
}
