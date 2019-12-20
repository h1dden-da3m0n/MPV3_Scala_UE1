package exercise

import akka.actor.Scheduler

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{Failure, Random, Success}

object AdvancedFutureConcepts extends App {
  def parallelMax1(intList: Seq[Int], segmentCnt: Int)(implicit ec: ExecutionContext): Future[Int] = {
    val segmentSize = Future {
      if (intList.isEmpty) throw new IllegalArgumentException("ERROR: intList must not be empty")
      if (segmentCnt <= 0) throw new IllegalArgumentException("ERROR: segmentCnt must not be 0 or below")
      intList.size / segmentCnt
    }
    val segments = segmentSize map { s => intList.grouped(s).toList }

    val futureMaxLists = segments map {
      _ map { segment =>
        Future {
          segment.max
        }
      }
    }
    val maxLists = futureMaxLists flatMap { lists => Future.sequence(lists) }

    maxLists map { list => list.max }
  }

  //  private def futureSequence[T, TO](in: List[Future[T]])(implicit ec: ExecutionContext): Future[TO] = {
  //    Future {
  //      TO
  //    }
  //  }
  //
  //  def parallelMax2(intList: Seq[Int], segmentCnt: Int)(implicit ec: ExecutionContext): Future[Int] = {
  //    val segmentSize = Future {
  //      if (intList.isEmpty) throw new IllegalArgumentException("ERROR: intList must not be empty")
  //      if (segmentCnt <= 0) throw new IllegalArgumentException("ERROR: segmentCnt must not be 0 or below")
  //      intList.size / segmentCnt
  //    }
  //    val segments = segmentSize map { s => intList.grouped(s).toList }
  //
  //    val futureMaxLists = segments map {
  //      _ map { segment =>
  //        Future {
  //          segment.max
  //        }
  //      }
  //    }
  //    val maxLists = futureMaxLists flatMap { lists => futureSequence(lists) }
  //
  //    maxLists map { list => list.max }
  //  }

  private def compute[T](promise: Promise[T], computation: => T, delay: FiniteDuration, retries: Int)(implicit ec: ExecutionContext, s: Scheduler): Unit = {
    Future {
      if (retries <= 0) promise failure new RuntimeException("ERROR: Computation ran out of retry attempts.")
      else {
        val f = Future {
          computation
        }
        f.foreach(x => promise success x)
        f.failed.foreach(_ => {
          Console.println(s"INFO: Computation failed but has $retries retry(s) left to succeed")
          s.scheduleOnce(delay) {
            compute(promise, computation, delay, retries - 1)
          }
        })
      }
    }
  }

  def retryAsync[T](computation: => T, delay: FiniteDuration, retries: Int)(implicit ec: ExecutionContext, s: Scheduler): Future[T] = {
    val promise = Promise[T]()
    compute(promise, computation, delay, retries)
    promise.future
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  // ===== Test =====
  println("==== AdvancedFutureConcepts ====")
  println("---- 1.3 a) ----")

  def test13a[T](future: Future[T]): Unit = {
    future onComplete {
      case Success(v) => println(s"✔ Max is: $v")
      case Failure(ex) => println(s"❌ Max Failed with: $ex")
    }
    Await.ready(future, Duration.Inf)
    Thread.sleep(50)
  }

  val nValues = 32
  val rngNumbers = Seq.fill(nValues)(Random.nextInt(nValues * 2))
  println(rngNumbers.mkString("[", ", ", "]"))
  println()

  test13a(parallelMax1(rngNumbers, 4))
  test13a(parallelMax1(rngNumbers, -4))
  test13a(parallelMax1(Seq.empty, 4))

  println("\n---- 1.3 b) ----")

  println("\n---- 1.3 c) ----")

  def failSometimes(): Int = {
    Thread.sleep(200) // simulate computation time
    if (Random.nextDouble() <= 0.15) Random.nextInt(42)
    else throw new UnsupportedOperationException("ERROR: I Failed Sometimes :P")
  }

  def neverFail(): Int = {
    Thread.sleep(200) // simulate computation time
    Random.nextInt(42)
  }

  def test13c[T](func: => T, retries: Int = 3)(implicit s: Scheduler): Unit = {
    val future = retryAsync(func, 250.millis, retries)
    future.foreach(x => println(s"Success with result $x"))
    future.failed.foreach(ex => println(s"Failed with exception $ex"))
    Await.ready(future, Duration.Inf)
    Thread.sleep(50)
  }

  val system = akka.actor.ActorSystem()

  test13c(neverFail())(system.scheduler)
  test13c(failSometimes())(system.scheduler)
  test13c(failSometimes())(system.scheduler)
  test13c(failSometimes())(system.scheduler)

  Await.ready(system.terminate(), Duration.Inf)
}
