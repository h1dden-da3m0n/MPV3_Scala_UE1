package exercise

import basics.Futures.{compute, doWork, println}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}

object FutureBasics extends App {
  def doInParallel(block1: => Unit, block2: => Unit): Future[Unit] = {
    val f1 = Future {
      block1
    }
    val f2 = Future {
      block2
    }

    //    for(x <- f1;
    //        y <- f2) yield ()
    //    f1 flatMap { _ => f2 flatMap { _ => Future() } }
    f1 flatMap { _ => f2 map { _ => () } }
  }

  def doInParallel[U, V](f1: Future[U], f2: Future[V]): Future[(U, V)] = {
    //    for(u <- f1;
    //        v <- f2) yield (x, y)
    //    f1 flatMap { u => f2 flatMap { v => Future(u, v) } }
    f1 flatMap { u => f2 map { v => (u, v) } }
  }

  // ===== Test =====

  println("==== FutureBasics ====")
  println("---- 1.1 a) ----")

  def printCompleted[T](future: Future[T]): Unit = {
    future onComplete {
      case Success(_) => println("✔ doInParallel finished Successful")
      case Failure(ex) => println(s"❌ doInParallel finished with exception: $ex")
    }
    Await.ready(future, Duration.Inf)
    Thread.sleep(50)
  }

  printCompleted(doInParallel(doWork(1, 5), doWork(2, 5)))
  printCompleted(doInParallel(doWork(1, 6), doWork(2, 5)))

  println("---- 1.1 b) ----")
  val fReturn = doInParallel(Future(compute(1, 3, 32)), Future(compute(2, 5, 10)))
  fReturn onComplete {
    case Success(uv) => println(s"✔ doInParallel finished Successful (u,v) = $uv")
    case Failure(ex) => println(s"❌ doInParallel finished with exception: $ex")
  }

  Await.ready(fReturn, Duration.Inf)
  Thread.sleep(50)

  println("---- 1.1 c) ----")

  def max(t: (Int, Int)): Int = {
    if (t._1 >= t._2) t._1
    else t._2
  }

  val n = 8
  val n2 = n / 2
  val rngNumbers = Seq.fill(n)(Random.nextInt(16))
  println(s"rngNumbers = ${rngNumbers.mkString("[", ", ", "]")}")
  val splitRngNums = rngNumbers.splitAt(n2)
  println(s"splitRngNums = [${splitRngNums._1.mkString("[", ", ", "]")}, ${splitRngNums._2.mkString("[", ", ", "]")}]")
  val fMax = doInParallel(
    Future(compute(1, n2, splitRngNums._1.max)),
    Future(compute(2, n2, splitRngNums._2.max))
  )
  fMax onComplete {
    case Success(uv) => println(s"✔ doInParallel finished Successful max((u, v)) = ${max(uv)}")
    case Failure(ex) => println(s"❌ doInParallel finished with exception: $ex")
  }
  Await.ready(fMax, Duration.Inf)
  Thread.sleep(50)
}
