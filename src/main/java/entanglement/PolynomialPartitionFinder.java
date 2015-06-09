/**
 * 
 */
package entanglement;

import static entanglement.util.TraceOps.toComplementArray;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;
import entanglement.trace.Trace;
import entanglement.trace.Traces;
import entanglement.util.TraceOps;

/**
 * Given a set T of {@linkplain Traces} and an angel A, finds the 
 * set of all angels that are entangled with A in T in polynomial time.
 * 
 * @author etorlak
 */
final class PolynomialPartitionFinder {
	/**
	 * Returns the indices of all angels that are entangled with the angel at the given index in
	 * the given traces.  The returned set always includes the given angel.
	 * @requires 0 <= angel < traces.length
	 * @return indices of all angels that are entangled with the angel at the given index in
	 * the given traces
	 */
	public static IntSet findEntangled(Traces traces, int angel) { 
		assert 0 <= angel && angel < traces.length();

		if (traces.angel(angel).maxValue()==0) // constant-valued angel
			return Ints.singleton(angel);

		return (new PolynomialPartitionFinder(traces)).entanglement(angel);
	}

	private final Traces traces;

	/**
	 * Creates a new partition finder for the given traces.
	 * This constructor assumes that traces.max() > 0.  Otherwise,
	 * all angels are trivially un-entangled so entanglement queries
	 * don't make sense for them.
	 */
	private PolynomialPartitionFinder(Traces traces) { 
		this.traces = traces;
	}

	private IntSet entangled(IntSet headSet) { 
		if (headSet.size()==traces.length()) return Ints.EMPTY_SET;
		final Map<Trace, Set<Trace>> split = new LinkedHashMap<Trace, Set<Trace>>();
		final int[] head = headSet.toArray();
		final int[] tail = toComplementArray(headSet, traces.length());
		
		for(Trace t : traces) { 
			final Trace h = t.project(head);
			Set<Trace> tails = split.get(h);
			if (tails==null) { 
				tails = new LinkedHashSet<Trace>();
				split.put(h, tails);
			}
			tails.add(t.project(tail));
		}
		
		for(Trace iHead : split.keySet()) { 
			final Set<Trace> iTails = split.get(iHead);
			for(Trace jHead : split.keySet()) { 
				if (iHead==jHead) continue;
				final Set<Trace> jTails = split.get(jHead);
				for(Trace jTail : jTails) { 
					
					if (!iTails.contains(jTail)) { 
						
						return TraceOps.lift(tail, Collections.singletonList(minimumDistance(jTail, iTails))).get(0);
					}
				}
			}
		}
		return Ints.EMPTY_SET;
	}
	
	/**
	 * Returns the indices of the minimum set of angels that can be changed to make
	 * the given trace safe, according to the given set of safe traces.
	 * @requires trace !in safe and !safe.isEmpty and all t: safe | t.angels = trace.angels
	 * @return indices of the minimum set of angels that can be changed to make
	 * the given trace safe, according to the given set of safe traces.
	 */
	private final IntSet minimumDistance(Trace trace, Set<Trace> safe) { 
		final int k = trace.length();
		final IntSet min = new IntBitSet(k);
		min.addAll(Ints.rangeSet(Ints.range(0, k-1)));
		
		final IntSet diff = new IntBitSet(k);
		for(Trace t : safe) {
			for(int i = 0; i < k; i++) { 
				if (trace.get(i)!=t.get(i))
					diff.add(i);
			}
			if (diff.size()<min.size()) { 
				min.clear();
				min.addAll(diff);
			}
			diff.clear();
		}
		return min;
	}

	/**
	 * Returns the indices of all angels that are entangled with the angel at the given index.
	 * @requires 0 <= angel < this.traces.length() 
	 * @requires this.traces.max(angel) > 0
	 * @return indices of all angels that are entangled with the angel at the given index.
	 */
	private final IntSet entanglement(int angel) { 
		final int k = traces.length();
		final IntSet entangled = new IntBitSet(k);
		entangled.add(angel);
		
//		System.out.println("Looking for partition of " + angel);
		for(IntSet witness = entangled(entangled); !witness.isEmpty(); witness = entangled(entangled)) {
			entangled.addAll(witness);
//			System.out.println(" inflated to " + entangled);
		}
		
		
		return entangled;
	}
}
