/**
 * 
 */
package entanglement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import kodkod.ast.Decls;
import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.Relation;
import kodkod.ast.Variable;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.config.ConsoleReporter;
import kodkod.engine.satlab.SATFactory;
import kodkod.instance.Bounds;
import kodkod.instance.Instance;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;
import kodkod.util.ints.IntSet;
import entanglement.trace.Trace;
import entanglement.trace.TraceFilter;
import entanglement.trace.Traces;

/**
 * Given a set of {@linkplain Traces} and a partitioning of 
 * angels into n sets, enumerates all maximal supports for that
 * partitioning in the given traces.   
 * 
 * @author etorlak
 *
 */
final class MaxSupportEnumerator {
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
	public static Iterator<Traces> maxSupports(final Traces traces, final List<IntSet> parts) {
		final int[][] partitioning = new int[parts.size()][];
		for(int i = 0; i < partitioning.length; i++)
			partitioning[i] = parts.get(i).toArray();
		return ( new MaxSupportEnumerator(traces, partitioning) ).maxSupports();
	}
	
	private final Traces traces;
	private final int[][] parts;
	private final Bounds bounds;
	private final Relation[] dom, idx, s;
	
	
	/**
	 * Constructs a max support enumerator for the given traces and partitioning.
	 * This constructor assumes that the given sets partition [0..traces.length).
	 * @requires parts[int][int] = [0..traces.length) 
	 * @requires all p: parts[int] | p.length > 0
	 * @requires all p: parts[int], p': parts[int] - p | no p[int] & p'[int]
	 */
	@SuppressWarnings({ "unchecked" })
	private MaxSupportEnumerator(final Traces traces, int[][] parts) { 
		this.traces = traces;
		this.parts = parts;
			
		final int n = parts.length;
		this.idx = new Relation[n];
		this.dom = new Relation[n];
		this.s = new Relation[n];
		for(int i = 0; i < n; i++)  {
			idx[i] = Relation.binary("i" + i);
			dom[i] = Relation.unary("dom"+i);
			s[i] = Relation.unary("s"+i);
		}
		
		final Set<Trace> atoms = new LinkedHashSet<Trace>();
		final Set<Trace>[] projs = new Set[n];
		for(int i = 0; i < n; i++) { 
			projs[i] = new LinkedHashSet<Trace>();
		}
		for(Trace t : traces) { 
			atoms.add(t);
			for(int i = 0; i < n; i++) {
				projs[i].add(t.project(parts[i]));
			}
		}
		for(Set<Trace> proj : projs) {
			atoms.addAll(proj);
		}
		
		this.bounds = new Bounds(new Universe(atoms));
		final TupleFactory f = bounds.universe().factory();
		
		for(int i = 0; i < n; i++)  {
			bounds.boundExactly(dom[i], f.setOf(projs[i].toArray()));
			bounds.bound(s[i], bounds.upperBound(dom[i]));
			final TupleSet idxBound = f.noneOf(2);
			for(Trace t : traces) { 
				idxBound.add(f.tuple(t, t.project(parts[i])));
			}
			bounds.boundExactly(idx[i], idxBound);
		}
	}
	
	/** @return	some s[0] and ... and some s[n-1] and
	 * (all v1: s[0], ..., vn: s[n-1] | some (idx[0].v1 & ... & idx[n-1].vn)) and
	 * (all v1: dom[0]-s[0], t: idx[0].v1 | t.idx[1] !in s[1] or ... or t.idx[n-1] !in s[n-1]) and
	 * ... and
	 * (all vn: dom[n-1]-s[n-1], t: idx[n-1].vn | t.idx[0] !in s[0] or ... or t.idx[n-2] !in s[n-2] ) 
	 **/
	private final Formula localMaximum() {
		final int n = parts.length;
		final List<Formula> conjuncts = new ArrayList<Formula>(2*n + 1);
		for(Expression e : s) { 
			conjuncts.add(e.some());
		}
		
		final Variable t = Variable.unary("t");
		final Variable[] v = new Variable[n];
		for(int i = 0; i < n; i++) { 
			v[i] = Variable.unary("i");
		}
		final List<Expression> exprs = new ArrayList<Expression>(n);
		for(int i = 0; i < n; i++) { 
			exprs.add(idx[i].join(v[i]));
		}
		Decls decls = v[0].oneOf(s[0]);
		for(int i = 1; i < n; i++) { 
			decls = decls.and(v[i].oneOf(s[i]));
		}
		conjuncts.add(Expression.intersection(exprs).some().forAll(decls));
		
		final List<Formula> disjuncts = new ArrayList<Formula>(n-1);
		for(int i = 0; i < n; i++) { 
			for(int j = 0; j < n; j++) { 
				if (j != i) { 
					disjuncts.add(t.join(idx[i]).in(s[i]).not());
				}
			}
			conjuncts.add( Formula.or(disjuncts).forAll(v[i].oneOf(dom[i].difference(s[i])).and(t.oneOf(idx[i].join(v[i])))) );
		}
		
		return Formula.and(conjuncts);
	}
	
	/**
	 * @return a filter that accepts all traces allowed by the given solution 
	 */
	private TraceFilter filter(final Instance instance) { 
		return new TraceFilter() {
			final TupleFactory f = instance.universe().factory();
			@Override
			public boolean accept(Trace t) {
				for(int i = 0; i < parts.length; i++)  {
					if (!instance.tuples(s[i]).contains(f.tuple(t.project(parts[i]))))
						return false;
				}
				return true;
			}
		};
	}
	/**
	 * Returns an iterator over all locally maximal subsets of this.traces for which a given set of angels
	 * and its complement form the coarsest angel partitioning.  
	 * @requires angels.ints in [0..traces.length) && 0 < angels.size() < traces.length
	 * @return an iterator over all locally maximal subsets of the given traces for which a given set of angels
	 * and its complement form the coarsest angel partitioning.
	 */
	private Iterator<Traces> maxSupports() { 
		final Solver solver = new Solver();
		solver.options().setSolver(SATFactory.MiniSat);
		solver.options().setReporter(new ConsoleReporter());
		return new Iterator<Traces>() {
			
			final Iterator<Solution> sols = solver.solveAll(localMaximum(), bounds);
			Traces next = null;
			
			@Override
			public boolean hasNext() {
				if (next==null && sols.hasNext()) { 
					final Instance sol = sols.next().instance();
					if (sol!=null)
						next = traces.restrict(filter(sol));
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
