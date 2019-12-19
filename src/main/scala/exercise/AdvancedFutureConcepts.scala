package exercise

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Random, Success}

object AdvancedFutureConcepts extends App {
  def parallelMax1(intList: Seq[Int], segmentCnt: Int): Future[Int] = {
    val segmentSize = Future {
      intList.size / segmentCnt
    }
    val segments = segmentSize map { s => intList.grouped(s).toList }

    val futureMaxLists = segments map {
      _ map { segment => Future {
        segment.max
      }
      }
    }
    val maxLists = futureMaxLists flatMap { lists => Future.sequence(lists) }

    maxLists map { list => list.max }
  }

  // ===== Test =====
  println("==== AdvancedFutureConcepts ====")
  println("---- 1.3 a) ----")

  val nValues = 64
  val rngNumbers = Seq.fill(nValues)(Random.nextInt(nValues * 2))
  println(rngNumbers.mkString("[", ", ", "]"))
  println()

  val f1 = parallelMax1(rngNumbers, 4)
  f1 onComplete {
    case Success(v) => println(s"✔ Max is: $v")
    case Failure(ex) => println(s"❌ Max Failed with: $ex")
  }
  Await.ready(f1, Duration.Inf)
  Thread.sleep(50)

  val f2 = parallelMax1(rngNumbers, -4)
  f2 onComplete {
    case Success(v) => println(s"✔ Max is: $v")
    case Failure(ex) => println(s"❌ Max Failed with: $ex")
  }
  Await.ready(f2, Duration.Inf)
  Thread.sleep(50)

  val f3 = parallelMax1(Seq.empty, 4)
  f3 onComplete {
    case Success(v) => println(s"✔ Max is: $v")
    case Failure(ex) => println(s"❌ Max Failed with: $ex")
  }
  Await.ready(f3, Duration.Inf)
  Thread.sleep(50)
}
