/**
 * 
 */
package entanglement.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;

/**
 * A set of traces that can be represented with 64 bits.
 * 
 * @author etorlak
 */
final class ShortTraces extends Traces {
	private final long[] bits;
	private final long[] mask;
	private final int[] shift;
	/**
	 * Constructs a set of short traces from the given angels and data.
	 * This constructor assumes that each row of data is of length angels.length;
	 * and that the values in the ith column of data range over [0..angels[i].maxValue].
	 * It also assumes that the sum of bits needed to represent the max value of all 
	 * angels is no more than 64, and that each call to data.next() returns a sequence of values
	 * that has not been previously seen.
	 */
	ShortTraces(Angel[] angels, Iterator<int[]> data) {
		super(angels);
		final int k = angels.length;
		this.mask = new long[k];
		this.shift = new int[k];
		for(int i = 0, occupied = 0; i < k; i++) { 
			final int maxVal = angels[i].maxValue();
			if (maxVal==0) { 
				mask[i] = 0;
				shift[i] = 0;
			} else {
				final int zeros = Long.numberOfLeadingZeros(maxVal);
				final long pattern = -1L >>> zeros;
				final int size = 64 - zeros;
				mask[i] = pattern << occupied;
				shift[i] = occupied;
				occupied += size;
			}
		}
		
		final Collection<Long> unique = new ArrayList<Long>(1000);
		while(data.hasNext()) {
			final int[] trace = data.next();
			long packed = 0L;
			for(int i = 0; i < k; i++) { 
				packed |= (((long)trace[i])<<shift[i]);
			}
			unique.add(packed);
		}
		this.bits = toPrimitiveArray(unique);
	}
		
	/**
	 * Constructs a projection of the given trace onto the given indices.
	 * @requires indices[int] in [0..other.length)
	 */
	private ShortTraces(ShortTraces other, int[] indices) { 
		super(other, indices);
		final int k = indices.length;
		this.mask = new long[k];
		this.shift = new int[k];
		long composite = 0;
		for(int i = 0; i < k; i++) { 
			this.mask[i] = other.mask[indices[i]];
			composite |= this.mask[i];
			this.shift[i] = other.shift[indices[i]];
		}
		if (traceBits()==other.traceBits()) {
			this.bits = other.bits;
		} else {
			final Set<Long> unique = new LinkedHashSet<Long>(Math.round(other.size()/.75f)+1);
			for(long trace : other.bits) { 
				unique.add(trace & composite);
			}
			this.bits = toPrimitiveArray(unique);
		}
	}
	
	/**
	 * Constructs a restriction of the given set onto the given trace indices.
	 * @requires trace in [0..other.size) && some traces
	 */
	private ShortTraces(ShortTraces other, IntSet traces) { 
		super(other);
		this.mask = other.mask;
		this.shift = other.shift;
		this.bits = new long[traces.size()];
		final IntIterator itr = traces.iterator();
		for(int i = 0; itr.hasNext(); i++) {
			bits[i] = other.bits[itr.next()];
		}
	}
	
	/** @return primitive array representation of the given collection */
	private static long[] toPrimitiveArray(Collection<Long> longs) { 
		final long[] bits = new long[longs.size()];
		int i = 0;
		for(Long trace : longs) { 
			bits[i++] = trace;
		}
		return bits;
	}
	
	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#trace(int)
	 */
	final Trace trace(int i) { return trace(bits[i]); }
	
	/** @return returns a trace corresponding to the given bit pattern */
	private Trace trace(final long trace) { 
		return new Trace() {
			@Override
			public int length() { return mask.length; }	
			@Override
			public int get(int angel) {
				return (int) ( (trace & mask[angel]) >>> shift[angel]);
			}
			@Override
			public Angel angel(int i) { return ShortTraces.this.angel(i); }
		};
	}

	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#size()
	 */
	@Override
	public int size() { return bits.length; }
	
	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#project(int[])
	 */
	@Override
	public Traces project(int[] angels) { return new ShortTraces(this, angels); }

	/**
	 * {@inheritDoc}
	 * @see entanglement.trace.Traces#restrict(kodkod.util.ints.IntSet)
	 */
	@Override
	Traces restrict(IntSet traces) {
		assert !traces.isEmpty() : "Cannot restrict to the empty set";
		assert traces.min() >= 0 : "Cannot have negative trace indices: " + traces.min();
		assert traces.max() < bits.length : "Cannot have trace indices >= this.size(): " + traces.max();
		return new ShortTraces(this, traces);
	}
}
