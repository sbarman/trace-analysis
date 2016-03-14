package apps

object analysis {
	import apps.CsvScalaAnalysis
	
	val csv = new CsvScalaAnalysis("/Users/sbarman/Dropbox/Projects/modular-oracles/code/trace-analysis/entanglement/SODA.csv")
                                                  //> csv  : apps.CsvScalaAnalysis = apps.CsvScalaAnalysis@2794eab6
	val info = csv.getSplitInfo(2)            //> java.lang.UnsatisfiedLinkError: Could not load the library libminisat.dylib 
                                                  //| or any of its variants.
                                                  //| 	at kodkod.engine.satlab.NativeSolver.loadLibrary(NativeSolver.java:75)
                                                  //| 	at kodkod.engine.satlab.MiniSat.<clinit>(MiniSat.java:39)
                                                  //| 	at kodkod.engine.satlab.SATFactory$3.instance(SATFactory.java:75)
                                                  //| 	at kodkod.engine.fol2sat.Bool2CNFTranslator.translate(Bool2CNFTranslator
                                                  //| .java:67)
                                                  //| 	at kodkod.engine.fol2sat.Translator.toCNF(Translator.java:592)
                                                  //| 	at kodkod.engine.fol2sat.Translator.toBoolean(Translator.java:561)
                                                  //| 	at kodkod.engine.fol2sat.Translator.translate(Translator.java:412)
                                                  //| 	at kodkod.engine.fol2sat.Translator.translate(Translator.java:130)
                                                  //| 	at kodkod.engine.Solver$SolutionIterator.<init>(Solver.java:266)
                                                  //| 	at kodkod.engine.Solver.solveAll(Solver.java:179)
                                                  //| 	at entanglement.MaxBicliqueEnumerator$2.<init>(MaxBicliqueEnumerator.jav
                                                  //| a:194)
                                                  //| 	at entanglement.MaxBicliqueEnumerator.maxSupports(MaxBicliqueEnumerator.
                                                  //| java:193)
                                                  //| Output exceeds cutoff limit.
	
	val sorted = info.flatMap(f => f.supports).sortWith((x, y) => x.size > y.size)
	val distinct = sorted.distinct
	val bigSets = distinct.filter(x => x.size > 300)
	
	import apps.CsvScalaSet
	CsvScalaSet.openGui(bigSets)
}