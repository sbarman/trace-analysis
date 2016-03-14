/**
 * 
 */
package entanglement;

import static entanglement.util.TraceOps.toComplementArray;
import static kodkod.ast.Formula.and;
import static kodkod.ast.Formula.or;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kodkod.ast.Expression;
import kodkod.ast.Formula;
import kodkod.ast.IntConstant;
import kodkod.ast.Relation;
import kodkod.engine.Proof;
import kodkod.engine.Solution;
import kodkod.engine.Solver;
import kodkod.engine.satlab.SATFactory;
import kodkod.engine.ucore.RCEStrategy;
import kodkod.instance.Bounds;
import kodkod.instance.Instance;
import kodkod.instance.Tuple;
import kodkod.instance.TupleFactory;
import kodkod.instance.TupleSet;
import kodkod.instance.Universe;
import kodkod.util.ints.IntBitSet;
import kodkod.util.ints.IntIterator;
import kodkod.util.ints.IntSet;
import kodkod.util.ints.Ints;
import entanglement.trace.Trace;
import entanglement.trace.Traces;
/**
 * Given a set T of {@linkplain Traces} and an angel A, finds the 
 * set of all angels that are entangled with A in T.
 * 
 * @author etorlak
 */
final class PartitionFinder {
	
	/**
	 * Returns the indices of all angels that are entangled with the angel at the given index in
	 * the given traces.  The returned set always includes the given angel.
	 * @requires 0 <= angel < traces.length
	 * @return indices of all angels that are entangled with the angel at the given index in
	 * the given traces
	 */
	public static IntSet findEntangled(Traces traces, int angel) { 
		assert 0 <= angel && angel < traces.length();
		
		if (traces.angel(angel).maxValue()==0) // constant-valued angel
			return Ints.singleton(angel);
		
		return (new PartitionFinder(traces)).entanglement(angel);
	}
	
	
	private final Traces traces;
	private final Relation[] angels;
	private final Formula[][] equalities;
	private final Bounds bounds;
	private final Solver solver;
	
	/**
	 * Creates a new partition finder for the given traces.
	 * This constructor assumes that traces.max() > 0.  Otherwise,
	 * all angels are trivially un-entangled so entanglement queries
	 * don't make sense for them.
	 */
	private PartitionFinder(Traces traces) { 
		assert traces.max() > 0;
		
		this.traces = traces;
		this.angels = angels(traces);
		this.equalities = equalities(angels, traces);
		this.bounds = bounds(angels, traces);
		this.solver = new Solver();
		solver.options().setBitwidth(bounds.universe().size()+1);
	}
	
	/** @return an array of k unary relations that represent the angels in the given traces **/
	private static Relation[] angels(Traces traces) { 
		final Relation[] angels = new Relation[traces.length()];
		for(int i = 0; i < angels.length; i++) { 
			angels[i] = Relation.unary(traces.angel(i).id());
		}
		return angels;
	}
	
	/**
	 * @return an array of formulas that compare the ith angel in the given array, where
	 * 0 <= i < traces.length() = angels.length, to all the values in [0..traces.max(i)]  
	 */
	private static Formula[][] equalities(Relation[] angels, Traces traces) { 
		final int k = traces.length(), max = traces.max();
		final Expression[] values = new Expression[max+1];
		for(int i = 0; i <= max; i++) { 
			values[i] = IntConstant.constant(i).toBitset();
		}
		final Formula[][] equalities = new Formula[angels.length][];
		for(int i = 0; i < k; i++) { 
			final int iMax = traces.angel(i).maxValue();
			equalities[i] = new Formula[iMax + 1];
			for(int j = 0; j <= iMax; j++) { 
				equalities[i][j] = angels[i].eq(values[j]);
			}
		}
		return equalities;
	}
	
	/**
	 * @return bounds on the unary relations in the given angels array, which represent the angels
	 * in the given set of traces
	 */
	private static Bounds bounds(final Relation[] angels, final Traces traces) { 
		final Universe u = universe(traces.max());
		final Bounds b = new Bounds(u);
		final TupleFactory f = u.factory();
		for(int i = 0, k = traces.length(); i < k; i++) { 
			final int iMax = traces.angel(i).maxValue();
			if (iMax==0)
				b.boundExactly(angels[i], f.noneOf(1));
			else
				b.bound(angels[i], f.range(f.tuple(1), f.tuple(Integer.highestOneBit(iMax))));
		}
		for(Object bit : u)
			b.boundExactly((Integer) bit, f.setOf(bit));
		return b;
	}
	
	/** @return a universe that consists of all powers of two that are less than or equal to the given integer */
	private static Universe universe(int max) { 
		final int pow2 = 32 - Integer.numberOfLeadingZeros(max);
		final Object[] atoms = new Object[pow2];
		for(int i = 0; i < pow2; i++) {
			atoms[i] = 1<<i;
		}
		return new Universe(atoms);
	}
	
	/**
	 * Returns a minimal unsatisfiable core of the given formula with respect to the given bounds.
	 * This method assumes that the given formula and bounds are unsatisfiable.
	 * @return  a minimal unsatisfiable core of the given formula with respect to the given bounds
	 */
	private Set<Formula> minimalCore(Formula formula, Bounds bounds) { 
		solver.options().setLogTranslation(1);
		solver.options().setSolver(SATFactory.MiniSatProver);
		
		final Solution sol = solver.solve(formula, bounds);
//		System.out.println("CORE: "+sol);
		assert sol.instance()==null;
		final Proof proof = sol.proof();
		proof.minimize(new RCEStrategy(proof.log()));
		return proof.highLevelCore().keySet();
	}
	
	/**
	 * Returns an array view of a trace that satisfies the given formula and bounds, or null if no such trace exists.
	 * This method assumes that the given formula and bounds are expressed over the relations in 
	 * this.angels.
	 * @return an array view of a trace that satisfies the given formula and bounds, or null if no such trace exists.
	 */
	private int[] witness(Formula formula, Bounds bounds) { 
		solver.options().setLogTranslation(0);
		solver.options().setSolver(SATFactory.MiniSat);
		
		final Solution sol = solver.solve(formula, bounds);
//		System.out.println("WITNESS: "+sol);
		final Instance instance = sol.instance();
		if (instance==null) return null;
		
		final int k = traces.length();
		final int[] witness = new int[k];
		for(int j = 0; j < k; j++) {
			for(Tuple t : instance.tuples(angels[j]))
				witness[j] += (Integer) t.atom(0);
		}
		return witness;
	}
	
	/**
	 * Returns an array representation of a trace t such that t is not in this.traces, but
	 * there are t' and t'' in this.traces for which the following constraints hold:
	 * (1) t and t' have the same projection on the given indices, and (2) t and t'' have
	 * the same projection on the complement of the given indices.
	 * Returns null if no such trace exists.
	 * @requires some angels and angels in [0..this.traces.length())
	 * @return a trace t as described above
	 */
	private int[] entanglementWitness(IntSet headSet) { 
		if (headSet.size()==traces.length()) return null;
		
		final class FormulaCache { // local helper class for computing and caching sub-trace constraints
			final Map<Trace,Formula> cache = new LinkedHashMap<Trace, Formula>();
			final int[] indices;
			final Formula[] conjuncts;
			
			FormulaCache(int[] indices) { 
				this.indices = indices;
				this.conjuncts = new Formula[this.indices.length];
			}
			
			/** @return all cached formulas */
			final Collection<Formula> cached() { return cache.values(); }
		
			/** @return a formula that encodes the projection of the given trace onto this.indices */
			final Formula get(Trace trace) { 
				final Trace projected = trace.project(indices);
				Formula formula = cache.get(projected);
				if (formula==null) { 
					for(int j = 0; j < indices.length; j++) {
						conjuncts[j] = equalities[indices[j]][trace.get(indices[j])];
					}
					formula = and(conjuncts);
					cache.put(projected, formula);
				}
				return formula;
			}
		};
		
		final List<Formula> traceFormulas = new ArrayList<Formula>(traces.size());
		final FormulaCache headFormulas = new FormulaCache(headSet.toArray());
		final FormulaCache tailFormulas = new FormulaCache(toComplementArray(headSet, traces.length()));
		for(Trace trace : traces) { 
			traceFormulas.add(headFormulas.get(trace).and(tailFormulas.get(trace)));
		}
		final Formula formula = and(or(traceFormulas).not(), or(headFormulas.cached()), or(tailFormulas.cached()));
		return witness(formula, bounds);
	}
	
	/** @return a formula in disjunctive normal form that represents all traces in this.traces */
	private Formula allTraceConstraints() { 
		final List<Formula> disjuncts = new ArrayList<Formula>(traces.size());
		final int k = traces.length();
		final Formula[] conjuncts = new Formula[k];
		for(Trace trace : traces) { 
			for(int j = 0; j < k; j++) {
				conjuncts[j] = equalities[j][trace.get(j)];
			}
			disjuncts.add(and(conjuncts));
		}
		return or(disjuncts);
	}
	
	/** @return a representation of the given integer as a set of tuples generated by the given factory.
	 * This method assumes that f.universe contains the powers of 2 that make up the given integer. */
	private static TupleSet asTupleSet(TupleFactory f, int i) { 
		final TupleSet s = f.noneOf(1);
		for(int bit = Integer.highestOneBit(i); bit != 0; i &= ~bit, bit = Integer.highestOneBit(i)) { 
			s.add(f.tuple(bit));
		}
		return s;
	}
	
	/**
	 * Returns the indices of all angels that are entangled with the angel at the given index.
	 * @requires 0 <= angel < this.traces.length() 
	 * @requires this.traces.max(angel) > 0
	 * @return indices of all angels that are entangled with the angel at the given index.
	 */
	private IntSet entanglement(int angel) { 
		final int k = traces.length();
		final IntSet entangled = new IntBitSet(k);
		entangled.add(angel);
		
		int[] witness = entanglementWitness(entangled);
		if (witness==null) return entangled;
		
		final Formula[] conjuncts = new Formula[k+1];
		conjuncts[k] = allTraceConstraints();
		
		final Bounds b = bounds.clone();
		final TupleFactory f = bounds.universe().factory();
		
		do { 	
			// update the conjuncts and bounds based on the current witness
			for(IntIterator itr = entangled.iterator(); itr.hasNext();  ) {
				final int entangledAngel = itr.next();
				b.boundExactly(angels[entangledAngel], asTupleSet(f, witness[entangledAngel]));
			}
			
			for(int i = 0; i < k; i++) {
				conjuncts[i] = equalities[i][witness[i]];
			}

			// update the entangled set based on the core derived from the updated conjuncts and bounds
			final Set<Formula> minCore = minimalCore(and(conjuncts), b);
			for(int i = 0; i < k; i++) {
				if (minCore.contains(conjuncts[i]))
					entangled.add(i);
			}
			
			// get a new witness for the expanded entanglement set
			witness = entanglementWitness(entangled);
			
		} while (witness != null);
		
		return entangled;
	}
	
}
