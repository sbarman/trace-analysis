package sketch.entanglement.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

import kodkod.util.ints.IntSet;
import sketch.entanglement.DynAngel;
import sketch.entanglement.SimpleEntanglementAnalysis;
import sketch.entanglement.Trace;
import sketch.entanglement.partition.TraceSubset;
import sketch.entanglement.sat.SATEntanglementAnalysis;
import sketch.entanglement.sat.SubtraceFilter;
import sketch.entanglement.sat.TraceConverter;
import entanglement.TraceDisplay;
import entanglement.MaxSupportFinder;
import entanglement.TraceMisc;
import entanglement.TraceSet;
import entanglement.trace.Traces;

public class EntanglementGuiPanel<IdType, ValType, TraceType> extends EntanglementGuiPanelBase implements
        ListSelectionListener, ActionListener
{
    private static final long serialVersionUID = 1L;
//    private final SATEntanglementAnalysis satEA;
//    private final SimpleEntanglementAnalysis ea;
    private final TraceSet<IdType, ValType, TraceType> traces;
//    private final List<DynAngel> angelOrder;

    private final List<PartitionPanel<IdType, ValType, TraceType>> partitionPanels;
    private EntanglementGui<IdType, ValType, TraceType> gui;
//    private EntanglementColoring color;
    private TraceDisplay<IdType, ValType, TraceType> display;

    public EntanglementGuiPanel(EntanglementGui<IdType, ValType, TraceType> gui,
			TraceSet<IdType, ValType, TraceType> traces,
			TraceDisplay<IdType, ValType, TraceType> display) {
        super();
        this.gui = gui;
        this.traces = traces;
        this.display = display;
//        satEA = new SATEntanglementAnalysis(traces);
//        ea = new SimpleEntanglementAnalysis(traces);
//        this.useTrace = useTrace;
//        color = new EntanglementColoring(ea, satEA);

        partitionPanels = new ArrayList<PartitionPanel<IdType, ValType, TraceType>>();
//        angelOrder = new ArrayList<DynAngel>();

        if (traces.size() == 0) {
            return;
        }

//        Trace t = traces.iterator().next();
//        for (Event e : t.getEvents()) {
//            angelOrder.add(e.dynAngel);
//        }

        JEditorPane debugEditor = getDebugEditorPane();
        ((DefaultCaret) debugEditor.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        JEditorPane programEditor = getProgramEditorPane();
        ((DefaultCaret) programEditor.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        JButton refineButton = getRefineAngelButton();
        refineButton.setActionCommand("refineAngel");
        refineButton.addActionListener(this);

        refineButton = getRefineTraceButton();
        refineButton.setActionCommand("refineTrace");
        refineButton.addActionListener(this);

        addSummary();
        addPartitions();
        updateOutput();
    }

	private void addSummary() {
        PartitionSummaryPanel<IdType, ValType, TraceType> panel =
                new PartitionSummaryPanel<>(this.traces);
        JScrollPane entanglementPane = getEntanglementPane();
        entanglementPane.getViewport().add(panel);
    }

    public void addPartitions() {
    	TraceSet<IdType, ValType, TraceType> traces = this.traces;
    	List<IdType> idOrder = traces.idOrder();
    	
        JScrollPane partitionPane = getPartitionPane();
        JPanel partitionPanePanel = new JPanel();
        partitionPanePanel.setLayout(new BoxLayout(partitionPanePanel, BoxLayout.Y_AXIS));
        partitionPanePanel.setMaximumSize(new Dimension(100, 200));

        partitionPane.getViewport().add(partitionPanePanel);

        Set<Set<IdType>> partitions = traces.getEntangledPartitions();
        HashMap<IdType, Set<IdType>> idToPartition =
                new HashMap<IdType, Set<IdType>>();

        for (Set<IdType> partition : partitions) {
            for (IdType id : partition) {
                idToPartition.put(id, partition);
            }
        }

        for (IdType id : idOrder) {
            Set<IdType> partition = idToPartition.get(id);
            if (partitions.contains(partition)) {
                partitions.remove(partition);

                PartitionPanel<IdType, ValType, TraceType> panel = new PartitionPanel<IdType, ValType, TraceType>(partition, traces);
                panel.getList().addListSelectionListener(this);

                partitionPanePanel.add(panel);
                partitionPanels.add(panel);
            }
        }
    }

    public void valueChanged(ListSelectionEvent arg) {
        if (!arg.getValueIsAdjusting()) {
            updateOutput();
        }
    }

    private void updateOutput() {
        Map<IdType, ValType> unorderedTrace = new HashMap<IdType, ValType> ();
        boolean clearOutput = false;

        for (PartitionPanel<IdType, ValType, TraceType> panel : partitionPanels) {
            Map<IdType, ValType> value = panel.getSelectedValue();
            if (value == null) {
                clearOutput = true;
                break;
            }
            unorderedTrace.putAll(value);
        }

        if (!clearOutput) {
            TraceType selectedTrace = traces.getTrace(unorderedTrace);

            if (selectedTrace != null) {
            	display.setUpperPane(getDebugEditorPane(), traces, selectedTrace);
            	display.setLowerPane(getProgramEditorPane(), traces, selectedTrace);
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        if ("refineAngel".equals(event.getActionCommand())) {
            final EntanglementGuiPanel<IdType, ValType, TraceType> parent = this;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DisentanglementGui<IdType, ValType, TraceType> gui =
                            new DisentanglementGui<>(parent, traces.idOrder());
                    gui.setVisible(true);
                }
            });
        } else if ("refineTrace".equals(event.getActionCommand())) {
//            Set<Trace> filteredTraces = new HashSet<Trace>();
//            for (Trace trace : traces) {
//                boolean isBad = false;
//                for (PartitionPanel panel : partitionPanels) {
//                    Set<Trace> badTraces = panel.getBadTraces();
//                    if (badTraces.contains(trace.getSubTrace(panel.getAngelPartition())))
//                    {
//                        isBad = true;
//                        break;
//                    }
//                }
//                if (!isBad) {
//                    filteredTraces.add(trace);
//                }
//            }
        }
    }

    public void partitionTraces(Set<Set<IdType>> subpartitioning) {

//    	List<IntSet> oldSatPartitions = satEA.getEntangledIntSets();
//        Set<Set<DynAngel>> oldPartitions =
//                converter.getDynAngelPartitions(oldSatPartitions);
//
//        Set<Set<DynAngel>> partitioning = new HashSet<Set<DynAngel>>();
//
//        for (Set<DynAngel> partition : oldPartitions) {
//            HashSet<DynAngel> partitionClone = new HashSet<DynAngel>(partition);
//            for (Set<DynAngel> subpartition : subpartitioning) {
//                HashSet<DynAngel> projection = new HashSet<DynAngel>();
//                for (DynAngel angel : subpartition) {
//                    if (partition.contains(angel)) {
//                        projection.add(angel);
//                    }
//                }
//                if (!projection.isEmpty()) {
//                    partitioning.add(projection);
//                    partitionClone.removeAll(projection);
//                }
//            }
//            if (!partitionClone.isEmpty()) {
//                partitioning.add(partitionClone);
//            }
//        }
//
//        List<IntSet> newPartitions = converter.getIntSetPartitions(partitioning);
//
//        // EntanglementSummaryGui newGui =
//        // new EntanglementSummaryGui(sketch, sourceCodeInfo);
//
//        final Set<Trace> goodTraces = new HashSet<Trace>();
//        final Set<Trace> badTraces = new HashSet<Trace>();
//
//        for (PartitionPanel panel : partitionPanels) {
//            goodTraces.addAll(panel.getGoodTraces());
//            badTraces.addAll(panel.getBadTraces());
//        }
//
//        Traces satTraces = converter.getTraces();
//
//        boolean updateTraces = !badTraces.isEmpty();
//
//        if (updateTraces) {
//            satTraces =
//                    satTraces.restrict(new SubtraceFilter(badTraces, converter, true));
//        }
//
//        List<TraceSubset> subsets = new ArrayList<TraceSubset>();
//        int i = 0;
//
//        for (Iterator<Traces> supports =
//                MaxSupportFinder.findMaximalSupports(satTraces, oldSatPartitions,
//                        newPartitions); supports.hasNext();)
//        {
//            Traces support = supports.next();
//            List<Trace> subsetTraces = converter.convert(support);
//            if (goodSubset(subsetTraces, goodTraces)) {
//                subsets.add(new TraceSubset(subsetTraces, "" + i, null));
//            }
//            i++;
//        }

        // newGui.pack();
        // newGui.setVisible(true);
    	
    	// get the supports
        List<TraceSet<IdType, ValType, TraceType>> s = 
        		new ArrayList<TraceSet<IdType, ValType, TraceType>>(traces.getSupports(subpartitioning));
    	
        // lets filter by the good and bad traces as marked
		final List<Set<Map<IdType, ValType>>> goodTraces = new ArrayList<>();
		final List<Set<Map<IdType, ValType>>> badTraces = new ArrayList<>();

		for (PartitionPanel<IdType, ValType, TraceType> panel : partitionPanels) {
			goodTraces.add(panel.getGoodTraces());
			badTraces.add(panel.getBadTraces());
		}
		
		List<TraceSet<IdType, ValType, TraceType>> filtered = TraceMisc.filterTraceSets(s, goodTraces, badTraces);

        EntanglementGui<IdType, ValType, TraceType> gui =
                new EntanglementGui<IdType, ValType, TraceType>(filtered, display);
        gui.setVisible(true);
    }

    public Frame getFrame() {
        return gui;
    }
}
