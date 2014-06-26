package entanglement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TraceMisc {

	public static <IdType, ValType, TraceType> List<TraceSet<IdType, ValType, TraceType>> filterTraceSets(
			List<TraceSet<IdType, ValType, TraceType>> traceSets,
			List<Set<Map<IdType, ValType>>> goodTraces,
			List<Set<Map<IdType, ValType>>> badTraces) {
		
		List<TraceSet<IdType, ValType, TraceType>> filtered = new ArrayList<TraceSet<IdType, ValType, TraceType>>();
		for (TraceSet<IdType, ValType, TraceType> set : traceSets) {
			if (filterTraceSet(set, goodTraces, badTraces)) {
				filtered.add(set);
			}
		}
		return filtered;
	}

	// dont do anything with bad subtraces yet
	public static <IdType, ValType, TraceType> boolean filterTraceSet(
			TraceSet<IdType, ValType, TraceType> set,
			List<Set<Map<IdType, ValType>>> goodSubtraces,
			List<Set<Map<IdType, ValType>>> badSubstraces) {
		Set<Map<IdType, ValType>> fullTraces = set.getTraces();

		// check if set has one of each of the good subtraces
		for (Set<Map<IdType, ValType>> subtraces : goodSubtraces) {
			if (subtraces.isEmpty())
				continue;

			boolean matchedSubtrace = false;
			subtrace: for (Map<IdType, ValType> subtrace : subtraces) {
				for (Map<IdType, ValType> fullTrace : fullTraces) {
					boolean matched = true;
					for (IdType key : subtrace.keySet()) {
						if (!subtrace.get(key).equals(
								fullTrace.getOrDefault(key, null))) {
							matched = false;
							break;
						}
					}

					if (matched) {
						matchedSubtrace = true;
						break subtrace;
					}
				}
			}
			if (!matchedSubtrace)
				return false;
		}

		return true;
	}
}
