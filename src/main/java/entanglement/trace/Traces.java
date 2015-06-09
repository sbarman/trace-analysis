/**
 * 
 */
package entanglement.trace;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;



/**
 * Stores a set of n {@linkplain Trace traces} produced by k angels. 
 * 
 * @specfield length: int
 * @specfield angels: [0..length) lone->one Angel
 * @specfield traces: set {@linkplain Trace}
 * @invariant all t: traces | t.length = length
 * @invariant all t: traces, i: [0..length) | 0 <= t.get(i) <= angels[i].maxValue
 * 
 * @author etorlak
 */
public abstract class Traces implements Iterable<Trace> {
	private final Angel[] angels;
	private final int maxValue;
	
	/** 
	 * Constructs a set of traces produced by the given angels, where the ith
	 * angel takes on values in the range [0..angels[i].maxValue].  
	 * This constructor assumes that the contents of the given array are not changed during the
	 * lifetime of the constructed object.
	 **/
	Traces(Angel[] angels) { 
		this.angels = angels;
		int tmp = 0;
		for(Angel a : angels)
			tmp = Math.max(tmp, a.maxValue());
		this.maxValue = tmp;
	}
	
	/** 
	 * Constructs a set of traces produced by projecting the given
	 * traces onto the given angel indices.  This method assumes that
	 * the indices are unique and in range [0..other.length).  */
	Traces(Traces other, int[] indices) { 
		this.angels = new Angel[indices.length];
		int tmp = 0;
		for(int i = 0; i < indices.length; i++) {
			angels[i] = other.angels[indices[i]];
			tmp = Math.max(tmp, angels[i].maxValue());
		}
		this.maxValue = tmp;
	}
	
	/** 
	 * Constructs a set of traces produced by the same angels as the given traces.  
	 **/
	Traces(Traces other) { 
		this.angels = other.angels;
		this.maxValue = other.maxValue;
	}
	
	/** @return the minimum number of bits needed to a member of these Traces */
	int traceBits() { return traceBits(angels); }
	
	/**
	 * Returns the ith trace at the given index.  The traces are assumed to
	 * be stored in some (arbitrary) order.
	 * @return the ith trace
	 */
	abstract Trace trace(int i);
	
	/**
	 * Returns the length of each trace.
	 * @return this.length
	 */
	public final int length() { return angels.length; }
	
	/**
	 * Returns the maximum value that occurs in all traces.
	 * @return max(this.traces.elts[int])
	 */
	public final int max()  { return maxValue; }

	/**
	 * Returns the angel at the given index.
	 * @requires 0 <= idx < {@link #length()}
	 * @return this.angels[idx]
	 */
	public final Angel angel(int idx)  { return angels[idx]; }

	/**
	 * Returns the number of traces.
	 * @return #this.traces
	 */
	public abstract int size() ;
	
	/**
	 * Returns an iterator over these traces.
	 * @return an iterator over these traces.
	 */
	public final Iterator<Trace> iterator() {
		return new Iterator<Trace>() {
			final int size = size();
			int next = 0;
			@Override
			public boolean hasNext() { return next < size; }

			@Override
			public Trace next() {
				if (!hasNext()) throw new NoSuchElementException();
				return trace(next++);
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
			
		};
	}
	
	/**
     * Returns a Traces object that consists of the projection of this.traces onto the given angel indices.  
     * This method assumes that no angel indices are repeated.
     * @requires angels.length>0
     * @requires all i, j: [0..angels.length) | i!=j => angels[i] != angels[j]
     * @requires all i: [0..angels.length) | angels[i] in [0..this.length)
     * @return { s: Traces | t.traces = { t: Trace | some t': this.traces | t = t'.project(angels) } }
     */
	public abstract Traces project(final int[] angels) ;
	
	/**
	 * Returns a Traces object comprising all members of this.traces that are accepted by
	 * the given filter.  
	 * @return Traces object comprising all members of this.traces that are accepted by
	 * the given filter.
	 */
	public final Traces restrict(final TraceFilter filter) { 
		final IntSet idxs = new IntBitSet(size());
		for(int i = 0, size = size(); i < size; i++ ) { 
			if (filter.accept(trace(i)))
				idxs.add(i);
		}
		switch(idxs.size()) {
		case 0	: return noTraces(angels);
		case 1	: return restrict(Ints.singleton(idxs.min()));
		default	: return restrict(idxs);
		}
	}
	
	/**
	 * Returns a Traces object comprising the members of this.traces that are at the given indices.  
	 * @requires some traceIdxs && all i: traceIdxs | 0 <= i < this.size()
	 * @return Traces object comprising the members of this.traces that are at the given indices.  
	 */
	abstract Traces restrict(IntSet traceIdxs);
	
	/** 
	 * Returns a string representation of these traces.
	 * @return a string representation of these traces 
	 * */
	public final String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(angels[0].maxValue());
		for(int i = 1; i < angels.length; i++) {
			b.append(",");
			b.append(angels[i].maxValue());
		}
		b.append("\n");
		for(Trace t : this) { 
			b.append(t);
			b.append("\n");
		}
		b.deleteCharAt(b.length()-1);
		return b.toString();
	}
	
	/**
	 * Returns an empty set of traces for the given angels.
	 * @return empty set of traces for the given angels.
	 */
	public static Traces noTraces(final Angel[] angels) { 
		return new Traces(angels) {
			
			@Override
			Trace trace(int i) { throw new IndexOutOfBoundsException(); }
			
			@Override
			public int size() {	return 0; }
			
			@Override
			Traces restrict(IntSet traceIdxs) {
				if (!traceIdxs.isEmpty()) throw new IndexOutOfBoundsException(); 
				return this;
			}
			
			@Override
			public Traces project(int[] indices) {
				final Angel[] proj = new Angel[indices.length];
				for(int i = 0; i < indices.length; i++) {
					proj[i] = angels[indices[i]];
				}
				return noTraces(proj);
			}
		};
	}
	
	/**
	 * Returns the most efficient representation for the given trace and angel data.
	 * This method assumes that each row of data is of length angels.length;
	 * and that the values in the ith column of data range over [0..angels[i].maxValue].
	 * It also assumes that each call to data.next() returns a sequence of values
	 * that has not been previously seen.
	 * @return most efficient representation for the given trace and angel data
	 */
	private static Traces traces(Angel[] angels, Iterator<int[]> data) { 
		if (traceBits(angels) <= 64)
			return new ShortTraces(angels, data);
		else 
			return new LongTraces(angels, data);
	}
	
	/**
	 * Converts the given set of integer sequences into a set of Traces.  This 
	 * method assumes that the ith value in the <tt>maxValues</tt> array is the maximum
	 * value that occurs at the ith index of any list in the given set of lists.  It 
	 * also assumes that all lists in the given set have the same length (which is the
	 * length of <tt>maxValues</tt>), and that the minimum value stored at any index
	 * in any list is 0.
	 * @return traces encoded in the given set of integer sequences
	 */
	public static Traces traces(int[] maxValues, final Set<List<Integer>> traces) { 
		assert maxValues.length > 0 && !traces.isEmpty();
		final Angel[] angels = Angel.angels(maxValues);
		final int k = angels.length;
		final Iterator<int[]> data = new Iterator<int[]>() {
			final int[] vals = new int[k];
			final Iterator<List<Integer>> itr = traces.iterator();
			@Override
			public boolean hasNext() { return itr.hasNext(); }

			@Override
			public int[] next() {
				if (!hasNext()) throw new NoSuchElementException();
				int i = 0;
				for(Integer val : itr.next()) { 
					vals[i++] = val;
				}
				return vals;
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
		return traces(angels, data);
	}
	
	/**
	 * Extracts and returns the traces from the given file.  This 
	 * method assumes that the first line of the file is a comma-separated
	 * list of values, where the ith value is the maximum
	 * value that the ith angel takes on in the subsequent traces.  The method also
	 * assumes that there is one trace per line, given as a comma-separated list of 
	 * non-negative values; that all traces have the same length; and that no two
	 * lines contain the same list of values.
	 * @return traces stored in the given file
	 */
	public static Traces traces(final String file) { 
	
		FileReader fr = null;
		
		try {
			fr = new FileReader(file);
			final LineNumberReader lr = new LineNumberReader(fr);
			
			final String line = lr.readLine();
			if (line==null)
				throw new IllegalArgumentException("Empty trace file: " + file);
			
			final Angel[] angels = Angel.angels(parse(lr.getLineNumber(), line, new int[line.split(",").length]));
			final int k = angels.length;
			if (k==0)
				throw new IllegalArgumentException("Expected the specification of maximum values taken on by angels but found an empty line.");
		
			final Iterator<int[]> data = new Iterator<int[]>() {
				final int[] vals = new int[k];
				String next = lr.readLine();
				
				@Override
				public boolean hasNext() { return next != null; }

				@Override
				public int[] next() {
					if (!hasNext()) throw new NoSuchElementException();
					parse(lr.getLineNumber(), next, vals);
					try {
						next = lr.readLine();
					} catch (IOException e) {
						throw new IllegalArgumentException("Error reading " + file, e);
					}
					return vals;
				}

				@Override
				public void remove() { throw new UnsupportedOperationException(); }
			};
			
			return traces(angels, data);
	
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File not found: " + file, e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error reading " + file, e);
		} finally {
			if (fr != null)
				try { 
					fr.close(); 
				} catch (IOException e) { } // ignore
		}	
	}

	/**
	 * Parses the given comma-separated string into the given array of numbers, and
	 * returns the array.  This method assumes that data.length = trace.split(",").length.
	 * @return array representation of the given string trace
	 */
	private static int[] parse(int line, String trace, int[] vals) { 
		final String[] elts = trace.split(",");
		if (elts.length != vals.length)
			throw new IllegalArgumentException("Expected a trace with " + vals.length + " angels but found " + line);
		for(int i = 0; i < elts.length; i++) { 
			try {
				vals[i] = Integer.parseInt(elts[i]);
			} catch (NumberFormatException nfe) { 
				throw new IllegalArgumentException("Expected a number but found \"" + elts[i] + 
						"\" on line " + line + ", column " + i, nfe);
			}
		}
		return vals;
	}
	
	/** @return the minimum number of bits needed to represent a trace of length maxValues.length,
	 * where the ith angel can take on values in the range [0..maxValues[i]]*/
	private static int traceBits(Angel[] angels) { 
		int bits = 0;
		for(Angel a : angels) { 
			bits += 32 - Integer.numberOfLeadingZeros(a.maxValue());
		}
		return bits;
	}

}
