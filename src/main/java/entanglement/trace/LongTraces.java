/**
 * 
 */
package entanglement.trace;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;

/**
 * A set of traces that can need more than 64 bits to represent
 * 
 * @author etorlak
 */
final class LongTraces extends Traces {
	private final BitSet[] bits;
	private final int[] pos;
	private final int[] size;
	/**
	 * Constructs a set of short traces from the given angels and data.
	 * This constructor assumes that each row of data is of length angels.length;
	 * and that the values in the ith column of data range over [0..angels[i].maxValue].
	 * It also assumes that the sum of bits needed to represent the max value of all 
	 * angels is greater than 64, and that each call to data.next() returns a sequence of values
	 * that has not been previously seen.
	 */
	LongTraces(Angel[] angels, Iterator<int[]> data) {
		super(angels);
		final int k = angels.length;
		this.pos = new int[k];
		this.size = new int[k];
		int occupied = 0;
		for(int i = 0; i < k; i++) { 
			final int maxVal = angels[i].maxValue();
			if (maxVal==0) { 
				size[i] = 0;
				pos[i] = 0;
			} else {
				size[i] = 64 - Long.numberOfLeadingZeros(maxVal);
				pos[i] = occupied;
				occupied += size[i];
			}
		}
		final Collection<BitSet> unique = new ArrayList<BitSet>(1000);
		while(data.hasNext()) {
			final int[] trace = data.next();
			final BitSet packed = new BitSet(occupied);
			for(int i = 0; i < k; i++) { 
				int val = trace[i];
				for(int start = pos[i], p = start + size[i] - 1; p >= start; p--) { 
					packed.set(p, (val & 1) != 0);
					val >>>= 1;
				}
			}
			unique.add(packed);
		}
		this.bits = unique.toArray(new BitSet[unique.size()]);
	}
	
	/**
	 * Constructs a projection of the given trace onto the given indices.
	 * @requires indices[int] in [0..other.length)
	 */
	private LongTraces(LongTraces other, int[] indices) { 
		super(other, indices);
		final int k = indices.length;
		this.pos = new int[k];
		this.size = new int[k];
		for(int i = 0; i < k; i++) { 
			this.pos[i] = other.pos[indices[i]];
			this.size[i] = other.size[indices[i]];
		}
		
		if (traceBits()==other.traceBits()) {
			this.bits = other.bits;
		} else {
			final Set<Trace> seen = new LinkedHashSet<Trace>(Math.round(other.size()/.75f)+1);
			final Collection<BitSet> unique = new ArrayList<BitSet>(other.size());
			for(BitSet trace : other.bits) { 
				if (seen.add(trace(trace)))
					unique.add(trace);
			}
			this.bits = unique.toArray(new BitSet[unique.size()]);
		}
	}
	
	/**
	 * Constructs a restriction of the given set onto the given trace indices.
	 * @requires trace in [0..other.size) && some traces
	 */
	private LongTraces(LongTraces other, IntSet traces) { 
		super(other);
		this.pos = other.pos;
		this.size = other.size;
		this.bits = new BitSet[traces.size()];
		final IntIterator itr = traces.iterator();
		for(int i = 0; itr.hasNext(); i++) {
			bits[i] = other.bits[itr.next()];
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#trace(int)
	 */
	final Trace trace(int i) { return trace(bits[i]); }
	
	/** @return returns a trace corresponding to the given bit pattern */
	private Trace trace(final BitSet trace) { 
		return new Trace() {
			@Override
			public int length() { return pos.length; }	
			@Override
			public int get(int angel) {
				int val = 0;
				for(int p = pos[angel], end = p + size[angel]; p < end; p++) { 
					val <<= 1;
					val |= trace.get(p) ? 1 : 0;
				}
				return val;
			}
			@Override
			public Angel angel(int i) { return LongTraces.this.angel(i); }
		};
	}

	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#size()
	 */
	@Override
	public int size() { return bits.length; }

	/**
	 * 
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#project(int[])
	 */
	@Override
	public Traces project(int[] angels) { return new LongTraces(this, angels); }
	
	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#restrict(kodkod.util.ints.IntSet)
	 */
	@Override
	Traces restrict(IntSet traces) {
		assert !traces.isEmpty() : "Cannot restrict to the empty set";
		assert traces.min() >= 0 : "Cannot have negative trace indices: " + traces.min();
		assert traces.max() < bits.length : "Cannot have trace indices >= this.size(): " + traces.max();
		return new LongTraces(this, traces);
	}
}
