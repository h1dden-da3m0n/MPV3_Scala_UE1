package exercise

import java.util.concurrent.Executors

import basics.Futures.println

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

object QuickSort extends App {

  // TODO: Was 1.2 a)
  def quickSort[T](seq: Seq[T])(implicit ord: Ordering[T]): Seq[T] = {
    if (seq.length <= 1) seq
    else {
      val pivot = seq(seq.length / 2)
      Seq.concat(quickSort(seq filter (ord.lt(_, pivot))),
        seq filter (ord.equiv(_, pivot)),
        quickSort(seq filter (ord.gt(_, pivot))))
    }
  }

  case class IntBox(boxedInt: Int)

  object IntBox {
    implicit def descendingOrdering[T <: IntBox]: Ordering[T] = Ordering.fromLessThan((x, y) => x.boxedInt > y.boxedInt)

    val ascendingOrdering: Ordering[IntBox] = Ordering.by(i => i.boxedInt)
  }

  // TODO: Was 1.2 b)
  def quickSortPar[T](seq: Seq[T], threshold: Int = -1)(implicit ord: Ordering[T]): Future[Seq[T]] = {
    if (seq.length <= 1) Future {
      seq
    }
    else {
      val newLen = seq.length / 2

      if (newLen <= threshold) {
        Future {
          quickSort(seq)
        }
      }
      else {
        val pivot = seq(newLen)
        val ltFu = quickSortPar(seq filter (ord.lt(_, pivot)), threshold)
        val gtFu = quickSortPar(seq filter (ord.gt(_, pivot)), threshold)

        for (ltRes <- ltFu;
             rtRes <- gtFu) yield Seq.concat(ltRes, seq filter (ord.equiv(_, pivot)), rtRes)
      }
    }
  }

  def performanceTest[T](func: => T, reps: Int = 100): Unit = {
    val runtime = for (i <- 0 to reps) yield {
      val t0 = System.nanoTime()
      val ignored = func
      val t1 = System.nanoTime()
      t1 - t0
    }
    println(s"Avg. Elapsed time over $reps run(s): ${runtime.sum / runtime.size} ns")
  }

  def performanceTestFuture[T](func: => Future[T], reps: Int = 100)(implicit ctx: ExecutionContext): Unit = {
    Await.ready(func, Duration.Inf)

    val runtime = for (i <- 0 to reps) yield {
      val t0 = System.nanoTime()
      Await.ready(func, Duration.Inf)
      val t1 = System.nanoTime()
      t1 - t0
    }
    println(s"Avg. Elapsed time over $reps run(s): ${runtime.sum / runtime.size} ns")
  }

  // ===== Test =====

  println("==== QuickSort ====")
  println("---- 1.2 a) ----")
  val nValues = 16
  val rngNumbers = Seq.fill(nValues)(IntBox(Random.nextInt(32)))
  val sortedRng = quickSort(rngNumbers)
  val reverseSortRng = quickSort(rngNumbers)(IntBox.ascendingOrdering)
  println(rngNumbers)
  println(sortedRng)
  println(reverseSortRng)

  println("---- 1.2 b) ----")
  val nValues2 = 20480
  val rngNumbers2 = Seq.fill(nValues2)(IntBox(Random.nextInt(nValues2)))

  println(s"QuickSort 100x (with $nValues2 rng sequence)")
  performanceTest(quickSort(rngNumbers2))
  println(s"QuickSortPar 100x no threshold (with $nValues2 rng sequence)")
  performanceTestFuture(quickSortPar(rngNumbers2))

  print('\n')
  println(s"QuickSortPar 100x test threshold (with $nValues2 rng sequence)")
  for (i <- 1 to 128 filter (i => i % 16 == 0)) {
    print(f"Threshold $i%3s > ")
    performanceTestFuture(quickSortPar(rngNumbers2, i))
  }

  print('\n')
  println("QuickSortPar 100x with ExecutionContext")
  performanceTestFuture(quickSortPar(rngNumbers2, nValues2 / 2), 4)
  print(f"CachedThreadPool > \n")
  performanceTestFuture(quickSortPar(rngNumbers2, nValues2 / 2), 4)(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
  print(f"FixedThreadPool(32) > \n")
  performanceTestFuture(quickSortPar(rngNumbers2, nValues2 / 2), 4)(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8)))
  print(f"ScheduledThreadPool(4) > \n")
  performanceTestFuture(quickSortPar(rngNumbers2, nValues2 / 2), 4)(ExecutionContext.fromExecutor(Executors.newScheduledThreadPool(4)))
}
