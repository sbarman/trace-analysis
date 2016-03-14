/**
 * 
 */
package entanglement;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.satlab.SATFactory;
import kodkod.instance.Bounds;
import kodkod.instance.Instance;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;
import entanglement.trace.Trace;
import entanglement.trace.TraceFilter;
import entanglement.trace.Traces;

/**
 * Given a set of {@linkplain Traces} and a partitioning of 
 * angels into 2 sets, enumerates all maximal supports for that
 * partitioning in the given traces.  This is equivalent to 
 * finding all maximal bicliques in a bipartite graph that
 * represents the given set of traces, when expressed in 
 * terms of projections onto the given partitions.
 * 
 * @author etorlak
 *
 */
final class MaxBicliqueEnumerator {
	
	/**
	 * Returns an iterator over all subsets of the given traces which are locally
	 * maximal supports for the given angel partitioning.  This method assumes that the arrays p1 and p2 partition [0..traces.length)
	 * @requires (p1 + p2).ints = [0..traces.length) 
	 * @requires no p1.ints & p2.ints
	 * @requires some p1.ints && some p2.ints
	 * @return an iterator over all subsets of the given traces which are locally
	 * maximal supports for the given angel partitioning
	 */
	public static Iterator<Traces> maxBicliques(final Traces traces, final int[] p1, final int[] p2) { 
		return (new MaxBicliqueEnumerator(traces, p1, p2)).maxSupports();
	}
	
	/**
	 * Returns an iterator over all subsets of the given traces which contain the given
	 * subtraces and which are locally maximal supports for the given angel partitioning.  
	 * This method assumes that the arrays p1 and p2 partition [0..traces.length); that
	 * <tt>traces</tt> are a superset of <tt>subtraces</tt>; and that <tt>subtraces</tt> 
	 * support {p1, p2}.
	 * @requires (p1 + p2).ints = [0..traces.length) 
	 * @requires no p1.ints & p2.ints
	 * @requires some p1.ints && some p2.ints
	 * @requires subtraces.traces in traces.traces
	 * @requires let fp = EntanglementDetector.entanglement(subtraces) | 
	 * 				all i: [0..fp.size()) | fp.get(i).ints in p1[int] or fp.get(i).ints in p2[int]
	 * 				
	 * @return an iterator over all subsets of the given traces which contain the given
	 * subtraces and which are locally maximal supports for the given angel partitioning. 
	 */
	public static Iterator<Traces> maxBicliques(final Traces traces, final Traces subtraces, final int[] p1, final int[] p2) { 
		return (new MaxBicliqueEnumerator(traces, subtraces, p1, p2)).maxSupports();
	}
	
	private final Traces traces;
	private final int[] p1, p2;
	private final Bounds bounds;
	private final Relation g, s1, s2, dom, ran;
	
	private final Solver solver;
	
	/**
	 * Constructs a support finder for the given traces and angel partitions.
	 * This constructor assumes that the arrays p1 and p2 partition [0..traces.length)
	 * @requires (p1 + p2).ints = [0..traces.length) 
	 * @requires no p1.ints & p2.ints
	 * @requires some p1.ints && some p2.ints
	 */
	private MaxBicliqueEnumerator(Traces traces, final int[] p1, final int[] p2) { 
		this.traces = traces;
		this.g = Relation.binary("g");
		this.dom = Relation.unary("dom");
		this.ran = Relation.unary("ran");
		this.s1 = Relation.unary("s1");
		this.s2 = Relation.unary("s2");
		this.p1 = p1;
		this.p2 = p2;
		
		this.bounds = new Bounds(universe(traces, p1, p2));
		
		final TupleFactory f = bounds.universe().factory();
		final TupleSet gb = f.noneOf(2), db = f.noneOf(1), rb = f.noneOf(1);
		
		for(Trace t : traces) { 
			final Trace p1t = t.project(p1), p2t = t.project(p2);
			db.add(f.tuple(p1t));
			rb.add(f.tuple(p2t));
			gb.add(f.tuple(p1t, p2t));
		}
		
		this.bounds.boundExactly(g, gb);
		this.bounds.boundExactly(dom, db);
		this.bounds.boundExactly(ran, rb);
		this.bounds.bound(s1, db);
		this.bounds.bound(s2, rb);
		this.solver = new Solver();
		this.solver.options().setSolver(SATFactory.MiniSat);
	}
	
	/**
	 * Constructs a support finder for the given traces, subtraces, and angel partitions.   
	 * This method assumes that the arrays p1 and p2 partition [0..traces.length); that
	 * <tt>traces</tt> are a superset of <tt>subtraces</tt>; and that <tt>subtraces</tt> 
	 * support {p1, p2}.
	 * @requires (p1 + p2).ints = [0..traces.length) 
	 * @requires no p1.ints & p2.ints
	 * @requires some p1.ints && some p2.ints
	 * @requires subtraces.traces in traces.traces
	 * @requires let fp = EntanglementDetector.entanglement(subtraces) | 
	 * 				all i: [0..fp.size()) | fp.get(i).ints in p1[int] or fp.get(i).ints in p2[int]			
	 */
	private MaxBicliqueEnumerator(Traces traces, Traces subtraces, final int[] p1, final int[] p2) { 
		this(traces, p1, p2);
		final TupleFactory f = bounds.universe().factory();
		final TupleSet lb1 = f.noneOf(1), lb2 = f.noneOf(1);
		for(Trace t : subtraces) { 
			lb1.add(f.tuple(t.project(p1)));
			lb2.add(f.tuple(t.project(p2)));
		}
		bounds.bound(s1, lb1, bounds.upperBound(dom));
		bounds.bound(s2, lb2, bounds.upperBound(ran));
	}
	
	/**
	 * @return a Universe that consists of the projection of the given traces
	 * onto the given angel indices; this method assumes that the head and tail
	 * arrays partiton [0..traces.length)
	 */
	private static Universe universe(Traces traces, int[] head, int[] tail) { 
		final Set<Trace> headSet = new LinkedHashSet<Trace>();
		final Set<Trace> tailSet = new LinkedHashSet<Trace>();
		for(Trace t : traces) { 
			headSet.add(t.project(head));
			tailSet.add(t.project(tail));
		}
		headSet.addAll(tailSet);
		return new Universe(headSet);
	}
	
	/**
	 * @return a filter that accepts all traces t such t.project(p1) is in ts1 and t.project(p2) is in ts2.
	 */
	private TraceFilter filter(final TupleSet ts1, final TupleSet ts2) { 
		return new TraceFilter() {
			final TupleFactory f = bounds.universe().factory();
			@Override
			public boolean accept(Trace t) {
				return ts1.contains(f.tuple(t.project(p1))) && ts2.contains(f.tuple(t.project(p2)));
			}
		};
	}
	
	/** @return let x  = s1 -> s2 | 
	 * 	x in g && 
	 * 	some x && 
	 * 	(all v1: dom - s1| v1->s2 !in g) && 
	 *  (all v2: ran - s2 | s1->v2 !in g) 
	 **/
	private final Formula localMaximum() {
		final Expression x = s1.product(s2);
		final Variable v1 = Variable.unary("v1"), v2 = Variable.unary("v2");
		final Formula df = v1.product(s2).in(g).not().forAll(v1.oneOf(dom.difference(s1)));
		final Formula rf = s1.product(v2).in(g).not().forAll(v2.oneOf(ran.difference(s2)));
		return Formula.and(x.some(), x.in(g), df, rf);
	}
	
	/**
	 * Returns an iterator over all locally maximal subsets of this.traces for which a given set of angels
	 * and its complement form the coarsest angel partitioning.  
	 * @requires angels.ints in [0..traces.length) && 0 < angels.size() < traces.length
	 * @return an iterator over all locally maximal subsets of the given traces for which a given set of angels
	 * and its complement form the coarsest angel partitioning.
	 */
	private Iterator<Traces> maxSupports() { 
		return new Iterator<Traces>() {
			final Iterator<Solution> sols = solver.solveAll(localMaximum(), bounds);
			Traces next = null;
			
			@Override
			public boolean hasNext() {
				if (next==null && sols.hasNext()) { 
					final Instance sol = sols.next().instance();
					if (sol!=null)
						next = traces.restrict(filter(sol.tuples(s1), sol.tuples(s2)));
				}
				return next!=null;
			}

			@Override
			public Traces next() {
				if (!hasNext()) throw new NoSuchElementException();
				final Traces cur = next;
				next = null;
				return cur;
			}

			@Override
			public void remove() { throw new UnsupportedOperationException(); }
		};
	}
}
