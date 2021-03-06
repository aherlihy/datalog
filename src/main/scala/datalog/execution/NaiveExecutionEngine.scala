package datalog.execution

import datalog.dsl.{Atom, Constant, Term, Variable}
import datalog.storage.{SimpleStorageManager, StorageManager, debug}

import scala.collection.mutable

class NaiveExecutionEngine(val storageManager: StorageManager) extends ExecutionEngine {
  import storageManager.EDB
  val precedenceGraph = new PrecedenceGraph(storageManager.ns)

  def initRelation(rId: Int, name: String): Unit = {
    storageManager.ns(rId) = name
    storageManager.initRelation(rId, name)
  }

  def insertIDB(rId: Int, rule: Seq[Atom]): Unit = {
    precedenceGraph.addNode(rule)
    storageManager.insertIDB(rId, rule)
  }

  def insertEDB(rule: Atom): Unit = {
    storageManager.insertEDB(rule)
  }

  def evalRule(rId: Int, queryId: Int, prevQueryId: Int): EDB = {
    val keys = storageManager.getOperatorKeys(rId)
    storageManager.naiveSPJU(rId, keys, prevQueryId)
  }

  /**
   * Take the union of each evalRule for each IDB predicate
   */
  def eval(rId: Int, relations: Seq[Int], queryId: Int, prevQueryId: Int): EDB = {
    relations.foreach(r => {
      val res = evalRule(r, queryId, prevQueryId)
      storageManager.resetIncrEDB(r, queryId, res)
    })
    storageManager.getIncrementDB(rId, queryId)
  }

  def solve(rId: Int): Set[Seq[Term]] = {
    val relations = precedenceGraph.getTopSort.flatten.filter(r => storageManager.idb(r).nonEmpty)
    val pQueryId = storageManager.initEvaluation()
    val prevQueryId = storageManager.initEvaluation()
    var count = 0

    var setDiff = true
    while (setDiff) {
      count += 1
      val p = eval(rId, relations, pQueryId, prevQueryId)

      setDiff = !storageManager.compareIncrDBs(pQueryId, prevQueryId)
      storageManager.swapIncrDBs(prevQueryId, pQueryId)
    }
    storageManager.getResult(rId, pQueryId)
  }
}
