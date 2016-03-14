/**
 * 
 */
package entanglement;

import static entanglement.util.TraceOps.angels;
import static entanglement.util.TraceOps.difference;
import static entanglement.util.TraceOps.project;
import static entanglement.util.TraceOps.projectionFilter;
import static entanglement.util.TraceOps.toComplementArray;
import static entanglement.util.TraceOps.toSet;
import static entanglement.util.TraceOps.union;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import kodkod.util.collections.Containers;
import kodkod.util.ints.IntSet;
import entanglement.trace.Traces;
/**
 * Let T be a {@linkplain Traces set of traces} and let S be an angel partitioning with k partitions.  
 * A MaxSupportFinder for T and S finds all locally maximal subsets of T for which S is the coarsest 
 * partitioning.
 * 
 * @author etorlak
 */
public final class MaxSupportFinder {
	
	/**
	 * Returns an iterator over all subsets of <tt>traces</tt> which contain the given
	 * <tt>subtraces</tt> and which are locally maximal supports for the given refinement of the finest angel partitioning.  
	 * This method assumes that <tt>parts</tt> partition [0..traces.length); that
	 * <tt>traces</tt> are a superset of <tt>subtraces</tt>; that <tt>subtraces</tt> 
	 * support <tt>refinedParts</tt>; that <tt>refinedParts</tt> form a refinement
	 * of <tt>finestParts</tt>; and that <tt>finestParts</tt> is the
	 * {@linkplain EntanglementDetector#entanglement(Traces) finest angel partitioning} for the given
	 * traces.  Note that this method returns the same subsets as would {@linkplain #findMaximalSupports(Traces, Traces, List)
	 * findMaximalSupports(traces, subtraces, refinedParts)}, but it may be significantly faster..
	 * @requires finestParts = EntanglementDetector.entanglement(traces) 
	 * @requires all p: refinedParts[int] | some fp: finestParts[int] | p.ints in fp.ints
	 * @requires refinedParts[int].ints = [0..traces.length) 
	 * @requires all p: refinedParts[int] | some p.ints
	 * @requires all p: refinedParts[int], p': refinedParts[int] - p | no p.ints & p'.ints
	 * @requires subtraces.traces in traces.traces
	 * @requires let fp = EntanglementDetector.entanglement(subtraces) | 
	 * 				all i: [0..fp.size()) | some j: [0..parts.size()) | fp.get(i).ints in parts.get(i).ints
	 * 				
	 * @return an iterator over all subsets of the given traces which contain the given
	 * subtraces and which are locally maximal supports for the given refinement of the finest angel partitioning.
	 */
	public static Iterator<Traces> findMaximalSupports(final Traces traces, final List<IntSet> finestParts, 
			final Traces subtraces, final List<IntSet> refinedParts) { 
		
		final List<IntSet> refined = difference(refinedParts, finestParts);
		if (refined.isEmpty()) return Containers.iterate(traces);
		
		final int[] proj = union(traces.length(), refined).toArray();
		
		return new Iterator<Traces>() {
			final Iterator<Traces> itr = findMaximalSupports(traces.project(proj), subtraces.project(proj), project(proj, refined));
			
			@Override
			public boolean hasNext() { return itr.hasNext(); }

			@Override
			public Traces next() {
				return traces.restrict(projectionFilter(proj, toSet(itr.next())));
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}

	
	/**
	 * Returns an iterator over locally maximal supports for the given refinement of the 
	 * finest entanglement partitioning for the given traces.  Each of the returned subsets is maximal in the following sense: if any more traces were 
	 * added to that subset, then the given refinement would not be a valid partitioning for the expanded set.  
	 * This method assumes that <tt>refinedParts</tt> form a refinement
	 * of <tt>finestParts</tt>; and that <tt>finestParts</tt> is the
	 * {@linkplain EntanglementDetector#entanglement(Traces) finest angel partitioning} for the given
	 * traces.  Note that this method returns the same subsets as would {@linkplain #findMaximalSupports(Traces, List)
	 * findMaximalSupports(traces, refinedParts)}, but it may be significantly faster.
	 * @requires finestParts = EntanglementDetector.entanglement(traces) 
	 * @requires all p: refinedParts[int] | some fp: finestParts[int] | p.ints in fp.ints
	 * @requires refinedParts[int].ints = [0..traces.length) 
	 * @requires all p: refinedParts[int] | some p.ints
	 * @requires all p: refinedParts[int], p': refinedParts[int] - p | no p.ints & p'.ints
	 * @return an iterator over locally maximal supports for the given refinement of the 
	 * finest entanglement partitioning for the given traces. 
	 */
	public static Iterator<Traces> findMaximalSupports(final Traces traces, 
			final List<IntSet> finestParts, final List<IntSet> refinedParts) { 
		
		return findMaximalSupports(traces, finestParts, Traces.noTraces(angels(traces)), refinedParts);
	}

	/**
	 * Returns an iterator over locally maximal subsets of the given traces with the given angel
	 * partitioning.  That is, if any more traces were added to a returned subset, then the given partitioning
	 * would not be a valid partitioning for the expanded set.  This method assumes that the given sets partition 
	 * [0..traces.length).
	 * @requires parts[int].ints = [0..traces.length) 
	 * @requires all p: parts[int] | some p.ints
	 * @requires all p: parts[int], p': parts[int] - p | no p.ints & p'.ints
	 * @return an iterator over locally maximal subset of the given traces with the given coarsest angel
	 * partitioning. 
	 */
	public static Iterator<Traces> findMaximalSupports(final Traces traces, final List<IntSet> parts) { 
		return findMaximalSupports(traces, Traces.noTraces(angels(traces)), parts);
	}
	
	/**
	 * Returns an iterator over all subsets of the given traces which contain the given
	 * subtraces and which are locally maximal supports for the given angel partitioning.  
	 * This method assumes that <tt>parts</tt> partition [0..traces.length); that
	 * <tt>traces</tt> are a superset of <tt>subtraces</tt>; and that <tt>subtraces</tt> 
	 * @requires parts[int].ints = [0..traces.length) 
	 * @requires all p: parts[int] | some p.ints
	 * @requires all p: parts[int], p': parts[int] - p | no p.ints & p'.ints
	 * @requires subtraces.traces in traces.traces
	 * @requires let fp = EntanglementDetector.entanglement(subtraces) | 
	 * 				all i: [0..fp.size()) | some j: [0..parts.size()) | fp.get(i).ints in parts.get(i).ints
	 * 				
	 * @return an iterator over all subsets of the given traces which contain the given
	 * subtraces and which are locally maximal supports for the given angel partitioning. 
	 */
	public static Iterator<Traces> findMaximalSupports(final Traces traces, final Traces subtraces, final List<IntSet> parts) { 
		if (parts.size()==1) {
			return Containers.iterate(traces);
		} else if (parts.size()==2) { 
			return (MaxBicliqueEnumerator.maxBicliques(traces, subtraces, parts.get(0).toArray(), parts.get(1).toArray()));
		} else {
			return new Iterator<Traces>() {
				
				final int[] head = parts.get(0).toArray();
				final int[] tail = toComplementArray(parts.get(0), traces.length());
				final List<IntSet> rest = project(tail, parts.subList(1, parts.size()));
				
				final Iterator<Traces> hItr = (MaxBicliqueEnumerator.maxBicliques(traces, subtraces, head, tail));
				Iterator<Traces> tItr = Containers.emptyIterator(); 
				Traces hNext, next;
				
				@Override
				public boolean hasNext() {
					if (next==null) {
						while(hItr.hasNext() && !tItr.hasNext()) {
							hNext = hItr.next();
							tItr = findMaximalSupports(hNext.project(tail), subtraces.project(tail), rest);
						}
						if (tItr.hasNext()) {
							next = hNext.restrict(projectionFilter(tail, toSet(tItr.next())));
						}
					}
					return next!=null;
				}

				@Override
				public Traces next() {
					if (!hasNext()) throw new NoSuchElementException();
					final Traces ret = next;
					next = null;
					return ret;
				}

				@Override
				public void remove() { throw new UnsupportedOperationException(); }
			};
		}
	}
	
	
}
