package basics

object FunctionalProgramming extends App {
  println("Hello Scala")
  //  def main(args: Array[String]): Unit = {
  //    println("Hello Scala")
  //  }

  //  val numbers: Seq[Int] = Seq(1, 2, 3)
  val numbers: Seq[Int] = 1 to 4
  //  val numbers = new RichInt(1).to(4)

  //  numbers.foreach(i => println(i))
  //  numbers foreach(i => println(i))
  //  numbers foreach {i => println(i)}
  //  numbers foreach { println(_) }
  numbers foreach println

  println("---------")
  numbers map (i => i * i) foreach println

  println("---------")
  numbers map (i => i * i) filter (_ % 2 == 0) foreach println

  println("--- reduce ---")
  //  var sum1 = numbers map(i => i*i) reduce((s,i) => s+i)
  var sum1 = numbers reduce ((s, i) => s + i * i)
  println(s"sum1 = $sum1")

  var range = 1 to 3
  println("--- fold ---")
  val sum2 = range.foldLeft(0)((s, i) => s + i * i)
  println(s"sum2 = $sum2")

  val res1 = range.foldLeft("0")((s, i) => s"f($s,$i)")
  println(s"foldLeft res1 = $res1")
  val res2 = range.foldRight("0")((s, i) => s"f($s,$i)")
  println(s"foldRight res2 = $res2")

  val f: ((Int, Int) => Int) => Int = range.foldLeft(0)
  var sum3: Int = f((s, i) => s + i * i)
  println(s"currying: sum3 = $sum3")
}