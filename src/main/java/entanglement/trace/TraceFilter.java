/**
 * 
 */
package entanglement.trace;

/**
 * A filter for {@linkplain Trace traces}.
 * 
 * @author etorlak
 */
public interface TraceFilter {
	/**
	 * Returns true if the given trace satisfies the predicate
	 * encoded by this filter.  Otherwise returns false.
	 * @return true if the given trace satisfies the predicate
	 * encoded by this filter
	 */
	public abstract boolean accept(Trace t);
}
