package basics

import scala.util.{Failure, Success, Try}

object Monads extends App {
  def traditionalErrorHandling(): Unit = {
    for (s <- Seq("2", "5", "x", "0")) {
      try {
        var res = 10 / Integer.parseInt(s)
        println(s"$s -> $res")
      }
      catch {
        case ex: Throwable => println(s"$s -> exception: $ex")
      }
    }
  }

  def tryMonad(): Unit = {
    def toInt(s: String): Try[Int] = Try {
      s.trim.toInt
    }

    def divide(a: Int, b: Int): Try[Int] = Try {
      a / b
    }

    println("--- ToInt(s) ---")
    for (s <- Seq("2", "5", "x")) {
      val res = toInt(s)
      println(s"$s -> $res")
    }

    println("--- ToInt(s) foreach ---")
    for (s <- Seq("2", "5", "x")) {
      toInt(s) foreach (r => println(s"$s -> $r"))
    }

    println("--- ToInt(s).failed foreach ---")
    for (s <- Seq("2", "5", "x")) {
      toInt(s).failed foreach (r => println(s"$s -> $r"))
    }

    println("--- ToInt(s) match ---")
    for (s <- Seq("2", "5", "x")) {
      toInt(s) match {
        case Success(r) => println(s"$s -> $r")
        case Failure(ex) => println(s"$s -> exception $ex")
      }
    }

    println("--- ToInt(s) flatmap ---")
    for (s <- Seq("2", "5", "x", "0")) {
      val r = toInt(s) flatMap (i => divide(10, i))
      println(s"$s -> $r")
    }

    println("--- ToInt(s) map ---")
    for (s <- Seq("2", "5", "x", "0")) {
      val r = toInt(s) map (i => divide(10, i))
      println(s"$s -> $r")
    }

    println("--- ToInt(s) flatmap match ---")
    for (s <- Seq("2", "5", "x", "0")) {
      toInt(s) flatMap (i => divide(10, i)) match {
        case Success(r) => println(s"$s -> $r")
        case Failure(ex) => println(s"$s -> exception $ex")
      }
    }

    println("--- for expression ---")
    for (s <- Seq("2", "5", "x", "0")) {
      val r: Try[Int] =
        for (i <- toInt(s);
             q <- divide(10, i)) yield q

      r match {
        case Success(r) => println(s"$s -> $r")
        case Failure(ex) => println(s"$s -> exception $ex")
      }
    }
  }

  println("=== traditionalErrorHandling ===")
  traditionalErrorHandling()

  println("=== tryMonad ===")
  tryMonad()
}
