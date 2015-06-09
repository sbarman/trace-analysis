/**
 * 
 */
package entanglement.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;
import entanglement.trace.Angel;
import entanglement.trace.Trace;
import entanglement.trace.TraceFilter;
import entanglement.trace.Traces;

/**
 * This class contains utility methods that operate on and return mostly
 * {@linkplain Trace individual traces}, {@linkplain Traces sets of traces},
 * and {@linkplain Angel angels} that generate traces.  
 * 
 * @author etorlak
 */
public final class TraceOps {
	private TraceOps() {}
	
	/**
	 * Returns all angels in the given traces, in the ascending order.
	 * @return all angels in the given traces, in the ascending order
	 */
	public static Angel[] angels(Traces traces) { 
		final Angel[] angels = new Angel[traces.length()];
		for(int i = 0; i < angels.length; i++)
			angels[i] = traces.angel(i);
		return angels;
	}
	
	/**
	 * Returns the angels that are at the given indices of the given Traces object.
	 * The returned set's iterators produce angels in the increasing order of 
	 * indices in the given traces.
	 * @requires idxs.ints in [0..traces.length) 
	 * @return traces.angels[idxs]
	 */
	public static Set<Angel> angels(Traces traces, IntSet idxs) { 
		final Set<Angel> ret = new LinkedHashSet<Angel>();
		for(IntIterator itr = idxs.iterator(); itr.hasNext();)
			ret.add(traces.angel(itr.next()));
		return ret;
	}
	
	/**
	 * Given a set of angel indices drawn from the range [0..numAngels), 
	 * returns its complement.
	 * @requires angels.isEmpty() || 0 <= angels.min() <= angels.max() < numAngels.
	 * @return returns the complement of the given set, with respect to [0..numAngels)
	 */
	public static int[] toComplementArray(IntSet angels, int numAngels) { 
		final int[] complement = new int[numAngels - angels.size()];
		for(int i = 0, j = 0; i < numAngels; i++) { 
			if (!angels.contains(i)) {
				complement[j++] = i;
			}
		}
		return complement;
	}
	
	/**
	 * Returns the contents of the given Traces as object in a Set.
	 * @return contents of the given Traces as object in a Set.
	 */
	public static Set<Trace> toSet(Traces traces) { 
		final Set<Trace> set = new LinkedHashSet<Trace>(Math.round(traces.size()/.75f)+1);
		for(Trace t : traces) {
			set.add(t);
		}
		return set;
	}
	
	/**
	 * Returns a trace filter that accepts all traces whose projection on the
	 * given angels is in the given set.  
	 * @return a trace filter that accepts all traces whose projection on the
	 * given angels is in the given set.
	 */
	public static TraceFilter projectionFilter(final int[] projection, final Set<Trace> traces) { 
		return new TraceFilter() {
			@Override
			public boolean accept(Trace t) {
				return traces.contains(t.project(projection));
			}
		};
	}
	
	/**
	 * Given an array of sorted, non-repeating integers, and a list of subsets
	 * of those integers, returns the result of applying the inverse of the function encoded
	 * by the array to each set returned by the iterator.
	 * @requires #bijection[int] = bijection
	 * @requires all i, j: [0..bijection) | i < j => bijection[i] < bijection[j]
	 * @requires all s: sets | s.ints in bijection[int]
	 * @return { l: List | l.size()=sets.size() && all i: [0..l.size()) | l.get(i).ints = bijection.(sets.get(i).ints) } 
	 */
	public static List<IntSet> project(int[] bijection, List<IntSet> sets) {
		final List<IntSet> ret = new ArrayList<IntSet>(sets.size());
		for(IntSet s : sets) { 
			final IntSet r = new IntBitSet(bijection.length);
			for(IntIterator itr = s.iterator(); itr.hasNext(); ) { 
				final int rank = Arrays.binarySearch(bijection, itr.next());
				assert rank >= 0;
				r.add(rank);
			}
			ret.add( r );
		}
		return ret;
	}
	
	/**
	 * Given an array of sorted, non-repeating integers, and a list of sets drawn from
	 * [0..ranking.length), returns the result of applying the function encoded
	 * by the array to each set returned by the iterator.
	 * @requires #bijection[int] = bijection.length
	 * @requires all i, j: [0..bijection.length) | i < j => bijection[i] < bijection[j]
	 * @requires all s: sets | s.ints in [0..bijection.length)
	 * @return { l: List | l.size()=sets.size() && all i: [0..l.size()) | l.get(i).ints = bijection[sets.get(i).ints] } 
	 */
	public static List<IntSet> lift(int[] bijection, List<IntSet> sets) { 
		final List<IntSet> ret = new ArrayList<IntSet>(sets.size());
		final int ceil = bijection[bijection.length-1] + 1;
		for(IntSet s : sets) { 
			final IntSet r = new IntBitSet(ceil);
			for(IntIterator itr = s.iterator(); itr.hasNext(); ) { 
				r.add(bijection[itr.next()]);
			}
			ret.add( r );
		}
		return ret;
	}
	
	/**
	 * Returns a list that contains all elements in p1 that are not also in p2.
	 * @return a list that contains all elements in p1 that are not also in p2.
	 */
	public static List<IntSet> difference(List<IntSet> p1, List<IntSet> p2) { 
		final Set<IntSet> diff = new LinkedHashSet<IntSet>(p1);
		diff.removeAll(p2); 
		return new ArrayList<IntSet>(diff);
	}
	
	/** 
	 * Returns a new IntSet that is the union of the given sets.  This method assumes that 
	 * all elements in the given sets are less than the specified capacity. 
	 * @return  a new IntSet that is the union of the given sets
	 **/
	public static IntSet union(int capacity, Collection<IntSet> sets) { 
		final IntSet result = new IntBitSet(capacity);
		for(IntSet s : sets)
			result.addAll(s);
		return result;
	}
}
