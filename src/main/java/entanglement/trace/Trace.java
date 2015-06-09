/**
 * 
 */
package entanglement.trace;

import kodkod.util.ints.Ints;

/**
 * Represents an angelic trace produced by k angels.
 * 
 * @specfield length: int
 * @specfield angels: [0..length) ->one Angel
 * @specfield elts: [0..length) ->one int
 * @invariant length > 0
 * @invariant all i: [0..length) | elts[i] >= 0
 * 
 * @author etorlak
 *
 */
public abstract class Trace {
	
	/**
	 * Returns the ith angel.
	 * @requires 0 <= i < this.length
	 * @return this.angels[i]
	 */
	public abstract Angel angel(int i);
	
	/**
     * Returns the number of elements in this vector.       
     * @return this.length
     */
    public abstract int length();
    
    /**
     * Returns the value taken on by the angel at the given index in this trace.
     * @requires 0 <= i < length
     * @return this.elts[i]
     */
    public abstract int get(int i);
    
    /**
     * Returns a new Trace that is the projection of this trace onto the given angel indices.  
     * @requires angels.length>0 && all i: [0..angels.length) | angels[i] in [0..this.length)
     * @return { t: Trace | t.length = angels.length && t.elts = angels.(this.elts) }
     */
    public Trace project(final int[] angels) {
    	if (angels.length==0) 
    		throw new IllegalArgumentException("Cannot project " + this + " onto the empty array of angels.");
    	return new Trace() {
			@Override
			public int length() { return angels.length; }
			@Override
			public int get(int i) { return Trace.this.get(angels[i]); }
			@Override
			public Angel angel(int i) { return Trace.this.angel(angels[i]); }
		};
    }

    
    /**
     * Returns the hash code value for this vector.  
     *
     * @return the hash code value for this vector.
     * @see Object#hashCode()
     * @see Object#equals(Object)
     * @see #equals(Object)
     */
	public final int hashCode() {
		final int length = length();
		int hash = length;
		for(int i = 0; i < length; i++) {
			hash = Ints.superFastHash(angel(i), hash);
			hash = Ints.superFastHashIncremental(get(i), hash);
		}
		return Ints.superFastHashAvalanche(hash);
 	}

	/**
     * Compares the specified object with this trace for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also a trace, both
     * traces have the same size, and all corresponding angels and values in
     * the two traces are <i>equal</i>.  
     * 
     * @return <tt>true</tt> if the specified object is equal to this trace.
     */
	public final boolean equals(Object o) {
		if (o==this) return true;
		if (o instanceof Trace) {
			final Trace t = (Trace) o;
			final int length = length();
			if (t.length()==length) {
				for (int i = 0; i < length; i++) {
					if (get(i) != t.get(i)) 
						return false;
					if (!angel(i).equals(t.angel(i)))
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append(get(0));
		for(int i = 1, length = length(); i < length; i++) {
			buf.append(",");
			buf.append(get(i));
		}
		return buf.toString();
	}
}
