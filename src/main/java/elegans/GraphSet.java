package elegans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sketch.entanglement.DynAngel;
import sketch.entanglement.Trace;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;

import entanglement.EntanglementDetector;
import entanglement.MaxSupportFinder;
import entanglement.TraceSet;
import entanglement.trace.Traces;

import misc.Tuple;

public class GraphSet implements TraceSet<String, String, GraphTrace> {
	protected final List<Tuple<String, ? extends List<String>>> idToDomain;

	public final int[] maxVals;
	public final Set<List<Integer>> simpleTraces;

	private Set<GraphTrace> traceData;

	public GraphSet(Set<GraphTrace> traceData) {
		idToDomain = new ArrayList<Tuple<String, ? extends List<String>>>();

		this.traceData = traceData;

		Set<Map<String, String>> traces = new HashSet<Map<String, String>>();
		for (GraphTrace trace : traceData) {
			traces.add(trace.mapping);
		}

		for (Map<String, String> trace : traces) {
			addToDomain(trace);
		}

		int traceLength = idToDomain.size();
		maxVals = new int[traceLength];
		for (int i = 0, ii = idToDomain.size(); i < ii; ++i) {
			maxVals[i] = idToDomain.get(i)._2.size();
		}

		simpleTraces = new HashSet<List<Integer>>();
		for (Map<String, String> trace : traces) {
			simpleTraces.add(getSimpleTrace(traceLength, trace));
		}
	}

	protected List<Integer> getSimpleTrace(int traceLength,
			Map<String, String> trace) {
		List<Integer> simpleTrace = new ArrayList<Integer>(traceLength);
		for (int i = 0; i < traceLength; ++i) {
			simpleTrace.add(0);
		}

		for (String id : trace.keySet()) {
			int index = indexOf(id);
			List<String> domain = idToDomain.get(index)._2;
			simpleTrace.set(index, domain.indexOf(trace.get(id)));
		}
		return simpleTrace;
	}

	public int indexOf(String id) {
		for (int i = 0, ii = idToDomain.size(); i < ii; ++i) {
			String _id = idToDomain.get(i)._1;
			if (_id.equals(id))
				return i;
		}
		return -1;
	}

	protected void addToDomain(Map<String, String> trace) {
		for (String key : trace.keySet()) {
			int keyIndex = indexOf(key);
			if (keyIndex == -1) {
				idToDomain.add(new Tuple<String, List<String>>(key,
						new ArrayList<String>()));
				keyIndex = idToDomain.size() - 1;
			}

			List<String> domain = idToDomain.get(keyIndex)._2;
			String value = trace.get(key);

			if (!domain.contains(value)) {
				domain.add(value);
			}
		}
	}

	@Override
	public String getName() {
		return "Worm";
	}

	@Override
	public int size() {
		return traceData.size();
	}

	@Override
	public List<String> idOrder() {
		List<String> ids = new ArrayList<String>();
		for (Tuple<String, ? extends List<String>> idDomain : idToDomain) {
			ids.add(idDomain._1);
		}
		return ids;
	}

	@Override
	public Set<Set<String>> getEntangledPartitions() {

		Traces kodkodTraces = Traces.traces(maxVals, simpleTraces);
		List<IntSet> entangledSets = EntanglementDetector
				.entanglement(kodkodTraces);

		Set<Set<String>> partitions = new HashSet<Set<String>>();
		for (IntSet set : entangledSets) {
			Set<String> partition = new HashSet<String>();
			IntIterator it = set.iterator();
			while (it.hasNext()) {
				partition.add(idToDomain.get(it.next())._1);
			}
			partitions.add(partition);
		}

		return partitions;
	}

	@Override
	public Set<Map<String, String>> getValues(Set<String> partition) {
		HashSet<Map<String, String>> values = new HashSet<Map<String, String>>();
		for (GraphTrace t : traceData) {
			Map<String, String> trace = t.mapping;

			Map<String, String> subtrace = new HashMap<String, String>();
			for (String id : partition) {
				subtrace.put(id, trace.get(id));
			}
			values.add(subtrace);
		}

		return values;
	}

	@Override
	public Set<Map<String, String>> getTraces() {
		Set<Map<String, String>> mappings = new HashSet<Map<String, String>>();
		for (GraphTrace trace : traceData) {
			mappings.add(trace.mapping);
		}
		return mappings;
	}

	@Override
	public GraphTrace getTrace(Map<String, String> mapping) {
		for (GraphTrace trace : traceData) {
			Map<String, String> traceMap = trace.mapping;
			if (traceMap.equals(mapping)) {
				return trace;
			}
		}
		return null;
	}

	@Override
	public Set<TraceSet<String, String, GraphTrace>> getSupports(
			Set<Set<String>> subpartitions) {

		Set<Set<String>> oldPartitions = getEntangledPartitions();
		Set<Set<String>> newPartitions = new HashSet<Set<String>>();

		// calculate the intersections of all oldPartitions and the new
		// subpartitions
		for (Set<String> oldPartition : oldPartitions) {
			HashSet<String> partitionClone = new HashSet<String>(oldPartition);
			for (Set<String> subpartition : subpartitions) {
				HashSet<String> projection = new HashSet<String>();
				for (String id : subpartition) {
					if (oldPartition.contains(id)) {
						projection.add(id);
					}
				}
				if (!projection.isEmpty()) {
					newPartitions.add(projection);
					partitionClone.removeAll(projection);
				}
			}
			if (!partitionClone.isEmpty()) {
				newPartitions.add(partitionClone);
			}
		}

		// convert the newPartitions to a List<IntSet>
		List<IntSet> intSetPartitions = new ArrayList<IntSet>();

		for (Set<String> partition : newPartitions) {
			if (partition.size() == 1) {
				intSetPartitions.add(Ints.singleton(indexOf(partition
						.iterator().next())));
			} else {
				int maxValue = -1;
				List<Integer> indexes = new ArrayList<Integer>();
				for (String id : partition) {
					int index = indexOf(id);
					indexes.add(index);
					if (index > maxValue) {
						maxValue = index;
					}
				}
				IntSet partitionIndexes = new IntBitSet(maxValue + 1);
				for (Integer index : indexes) {
					partitionIndexes.add(index);
				}
				intSetPartitions.add(partitionIndexes);
			}
		}

		Traces kodkodTraces = Traces.traces(maxVals, simpleTraces);

		Set<TraceSet<String, String, GraphTrace>> set = new HashSet<TraceSet<String, String, GraphTrace>>();

		for (Iterator<Traces> supports = MaxSupportFinder.findMaximalSupports(
				kodkodTraces, intSetPartitions); supports.hasNext();) {
			Traces support = supports.next();

			Set<GraphTrace> traces = new HashSet<GraphTrace>();
			for (Iterator<entanglement.trace.Trace> it = support.iterator(); it
					.hasNext();) {
				entanglement.trace.Trace t = it.next();
				Map<String, String> mapping = new HashMap<String, String>();
				for (int j = 0; j < t.length(); j++) {
					Tuple<String, ? extends List<String>> idDomain = idToDomain
							.get(j);
					mapping.put(idDomain._1, idDomain._2.get(t.get(j)));
				}
				traces.add(getTrace(mapping));
			}
			set.add(new GraphSet(traces));
		}
		return set;
	}
}
