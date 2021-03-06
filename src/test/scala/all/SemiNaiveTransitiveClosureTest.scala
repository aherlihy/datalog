package all

import datalog.dsl.{Program, Relation}
import datalog.execution.{ExecutionEngine, NaiveExecutionEngine, SemiNaiveExecutionEngine}
import datalog.storage.{CollectionsStorageManager, RelationalStorageManager}
import graphs.{Acyclic, MultiIsolatedCycle, SingleCycle}

class SemiNaiveTransitiveClosure extends munit.FunSuite {
  val engine: ExecutionEngine = new SemiNaiveExecutionEngine(new RelationalStorageManager())
  List(
    Acyclic(new Program(new SemiNaiveExecutionEngine(new RelationalStorageManager()))),
    MultiIsolatedCycle(new Program(new SemiNaiveExecutionEngine(new RelationalStorageManager()))),
    SingleCycle(new Program(new SemiNaiveExecutionEngine(new RelationalStorageManager())))
  ).map(graph =>
    graph.queries.map((hint, query) => {
      test(graph.description + "." + query.description) {
        assertEquals(
          query.relation.solve(),
          query.solution,
          hint
        )
      }
    }))
}

class SemiNaiveCollectionTransitiveClosure extends munit.FunSuite {
  List(
    Acyclic(new Program(new SemiNaiveExecutionEngine(new CollectionsStorageManager()))),
    MultiIsolatedCycle(new Program(new SemiNaiveExecutionEngine(new CollectionsStorageManager()))),
    SingleCycle(new Program(new SemiNaiveExecutionEngine(new CollectionsStorageManager())))
  ).map(graph =>
    graph.queries.map((hint, query) => {
      test(graph.description + "." + query.description) {
        assertEquals(
          query.relation.solve(),
          query.solution,
          hint
        )
      }
    }))
}