/**
 * 
 */
package entanglement;

import java.util.ArrayList;
import java.util.List;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;
import entanglement.trace.Traces;

/**
 * Partitions angels that generate a given set of traces based on entanglement.  
 * That is, two angels are in the same partition iff they are entangled.
 * 
 * @specfield traces: Traces
 * 
 * @author etorlak
 */
public final class EntanglementDetector {
	/**
	 * Returns the finest entanglement-based partitioning of angels that generate the given traces.
	 * That is, two angels are in the same partition iff they are entangled.
	 * @return finest entanglement-based partitioning of angels that generate the given traces
	 */
	public static List<IntSet> entanglement(Traces traces) { 
		final int k = traces.length();
		final List<IntSet> parts = new ArrayList<IntSet>();
		final IntSet unknown = new IntBitSet(k);
		
		for(int i = 0; i < k; i++) { 
			if (traces.angel(i).maxValue()==0) { // constant angels get their own partition
				parts.add(Ints.singleton(i));
			} else {
				unknown.add(i);
			}
		}
		
		while(!unknown.isEmpty()) { 
			final int[] projection = unknown.toArray();
			final IntSet projectedPartition = PolynomialPartitionFinder.findEntangled(traces.project(projection), 0);
			final IntSet originalPartition = new IntBitSet(k);
			for(IntIterator itr = projectedPartition.iterator(); itr.hasNext(); ) { 
				final int angel = projection[itr.next()];
				originalPartition.add(angel);
				unknown.remove(angel);
			}
			parts.add(originalPartition);
		}
		
		return parts;
	}
	
}
