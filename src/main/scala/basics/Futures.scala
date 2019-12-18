package basics

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object Futures extends App {

  def println(x: Any) = Console.println(s"$x (thread id=${Thread.currentThread.getId})")

  def doWork(id: Int, steps: Int): Unit = {
    for (i <- 1 to steps) {
      println(s"$id: $i")
      if (i == 6) throw new IllegalArgumentException()
      Thread.sleep(200)
    }
  }

  def compute(id: Int, n: Int, result: Int): Int = {
    for (i <- 1 to n) {
      println(s"compute $id: $i")
      if (i == 6) throw new IllegalArgumentException()
      Thread.sleep(200)
    }
    result
  }

  def combine(value1: Int, value2: Int): Int = {
    for (i <- 1 to 5) {
      println(s"combine $i")
      Thread.sleep(200)
    }
    value1 + value2
  }

  def sequentialInvocation(): Unit = {
    doWork(1, 5)
    doWork(2, 5)
  }

  def simpleFutures(): Unit = {
    val f1 = Future {
      doWork(1, 5)
    }
    val f2 = Future {
      doWork(2, 5)
    }

    //    Thread.sleep(2000)
    // NEVER DO THIS OUTSIDE OF THIS TEST PROGRAM!!!!!
    Await.ready(f1, Duration.Inf)
    Await.ready(f2, Duration.Inf)
  }

  def futuresWithCallback(): Unit = {
    val f1: Future[Int] = Future {
      compute(1, 5, 10)
    }
    val f2: Future[Int] = Future {
      compute(2, 6, 10)
    }

    f1 foreach (r => println(s"Result = $r"))
    f2.failed foreach (ex => println(s"exception: $ex"))

    f1 onComplete {
      case Success(v) => println(s"Result2 = $v")
      case Failure(ex) => println(s"exception2: $ex")
    }

    Await.ready(f1, Duration.Inf)
    Await.ready(f2, Duration.Inf)
    Thread.sleep(50)
  }

  def futureComposition(): Unit = {
    val f1 = Future {
      compute(1, 5, 32)
    }
    val f2 = Future {
      compute(2, 5, 10)
    }

    val f3 = for (r1 <- f1;
                  r2 <- f2) yield combine(r1, r2)

    //    val f4 = f1 flatMap { r1 => f2 map { r2 => combine(r1, r2)} }

    f3 foreach (r1 => println(s"r1 = $r1"))

    Await.ready(f3, Duration.Inf)
    Thread.sleep(50)
  }

  def sequenceFutures(): Unit = {
  }

  println(s"availableProcessors=${Runtime.getRuntime.availableProcessors}")

  //println("==== sequentialInvocation ====")
  // sequentialInvocation()

  //  println("\n==== simpleFutures ====")
  //  simpleFutures()

  //  println("\n==== futuresWithCallback ====")
  //  futuresWithCallback()

  println("\n==== futureComposition ====")
  futureComposition()
}