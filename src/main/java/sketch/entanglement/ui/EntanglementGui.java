package sketch.entanglement.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTabbedPane;

import entanglement.TraceDisplay;
import entanglement.TraceSet;

public class EntanglementGui<IdType, ValType, TraceType> extends EntanglementGuiBase implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    private JTabbedPane tabbedPane;
    private JButton removeTabButton;
    
    private TraceDisplay<IdType, ValType, TraceType> display;

    public EntanglementGui(List<? extends TraceSet<IdType, ValType, TraceType>> subsets, TraceDisplay<IdType, ValType, TraceType> display) {
        super();
        this.display = display;
        tabbedPane = getTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        removeTabButton = getRemoveTabButton();
        removeTabButton.hide();
        removeTabButton.setActionCommand("removeTab");
        removeTabButton.addActionListener(this);

        for (TraceSet<IdType, ValType, TraceType> subset : subsets) {
            addTraceSet(subset);
        }

        pack();
    }

    private void addTraceSet(TraceSet<IdType, ValType, TraceType> subset) {
        EntanglementGuiPanel<IdType, ValType, TraceType> panel =
                new EntanglementGuiPanel<IdType, ValType, TraceType>(this, subset, display);
        tabbedPane.addTab(subset.getName(), panel);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "removeTab") {
            int tab = tabbedPane.getSelectedIndex();
            if (tab != -1) {
                tabbedPane.remove(tab);
            }
        }
    }
}
