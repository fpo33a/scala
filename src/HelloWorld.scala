import javax.xml.bind.DatatypeConverter

import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

// object is singleton in scala
object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, Frank!")

    val th = new TestHexaConvertion
    th.test

    val tu = new TestUnderscore
    tu.test

    val to = new TestOption
    to.test
    // test closure
    val tc = new TestClosure
    tc.test

    // construtor without parameter
    val frank = new Employee
    frank.personType
    frank.role ("developer")

    // construtor with parameters
    val marc = new OtherEmployee(1,"Marc")
    marc.personType
    marc.role ("manager")

    // test "trait"
    val paul = new TraitEmployee(2,"Paul")
    paul.personType
    paul.info ("general")     // only available if object inherits from trait
    paul.role ("director")

    // uses trait as from variable is also possible !
    val pierre = new OtherEmployee(3,"Pierre") with PersonTrait
    pierre.personType
    pierre.info ("local")     // only available if object inherits from trait

    // function call can also be written differently
    pierre.role ("contractor")
    pierre role {"admin"}
    pierre role "tester"

    // with case class id and name attributes are exposed
    var louis = new TraitCaseEmployee(4)
    louis.personType
    println ("- Louis id = "+louis.id+" , name = "+louis.name)
    louis = louis.copy (louis.id,"louis")   // name is implicit val, use copy set the name (as we use an overwritten constructor without name) - this recreate another object !
    louis.info ("remote")     // only available if object inherits from trait
    louis.role ("operator")
    println ("+ Louis id = "+louis.id+" , name = "+louis.name)

    val marie = new TraitCaseComplexEmployee(5,"Marie")
    marie.personType
    marie.info ("hospital")      // only available if object inherits from trait
    marie.complexInfo ("specialist", "london")     // only available if object inherits from trait
    marie.role ("doctor")
    println ("marie id = "+marie.id+" , marie = "+marie.name)
    val x : Tuple2 [Int,String] = marie.getComplexData("data")
    println ("getComplexData result : " + x)

    // use some list
    println ("--- List ---")
    val emps =  List (frank, marc, paul, pierre, louis, marie)
    emps.foreach ( emp => emp.personType )

    // do some tests on future (unrelated with previous tests)
    val testFuture = new TestFuture
    testFuture.doTests
  }
}

//---------------------------------------------------------

// more or less equivalent to java interface ... here we also have default implementation
trait PersonTrait {
  def info (data : String) ={ println ("info from trait "+data)}
}

//---------------------------------------------------------

trait PersonComplexTrait {
  def complexInfo (data : String, location : String) = { println ("complex info "+data+ ", "+location)}
}

//---------------------------------------------------------

// abstract class
abstract class Person {

  def personType = { println ("person type ")}
}

/* notes
trait vs abstract:
- Abstract classes can have constructor parameters as well as type parameters. Traits can have only type parameters.
- A class can inherit from multiple traits but only one abstract class
- Abstract classes are fully interoperable with Java. Traits are fully interoperable only if they do not contain any implementation code
- Per Scala 2.12, a trait compiles to a Java 8 interface ( implementation code in interface )

 */

// as you can see multiple classes can be defined in same source file

//---------------------------------------------------------

class Employee extends Person {

  println ("--- Employee ---")  // call when object is created

  def role ( role : String ) = { println ("role "+role)}
}

//---------------------------------------------------------

class OtherEmployee (id : Int, name : String) extends Person {

  println ("--- OtherEmployee ---")  // call when object is created

  def role ( role : String ) = { println ("name "+name+", id "+ id +", role "+role)}

}

//---------------------------------------------------------

class TraitEmployee (id : Int, name : String) extends Person with PersonTrait {

  println ("--- TraitEmployee ---") // call when object is created

  override def info (data : String) ={ println ("local info "+data)}       // need 'override' if we want to replace original trait function

  def role ( role : String ) = { println ("name "+name+", id "+ id +", role "+role)}
}

//---------------------------------------------------------

case class TraitCaseEmployee (id : Int, name : String) extends Person with PersonTrait {

  println ("--- TraitCaseEmployee ---")   // call when object is created

  // overwrite constructor
  def this (id : Int) = {
    this (id, "unknown")
  }
  
  def role ( role : String ) = { println ("name "+name+", id "+ id +", role "+role)}
}

//---------------------------------------------------------

case class TraitCaseComplexEmployee (id : Int, name : String) extends Person with PersonTrait with PersonComplexTrait {

  println ("--- TraitCaseComplexEmployee ---")   // call when object is created

  def role ( role : String ) = { println ("name "+name+", id "+ id +", role "+role)}

  // this function returns a tuple (Int, String)
  def getComplexData ( data : String ) : (Int,String) = {
    println (" in getComplex Data "+data)
    (id,name +" "+data)
  }
}

//---------------------------------------------------------

class TestFuture
{
  println ("--- TestFuture ---")        // call when object is created

  def doTests = {

    println ("this test should be ok")
    val f: Future[Int] = Future[Int] { Thread.sleep(500);   5 }
    val result: Try[Int] = Try(Await.result(f, 1 second))

    val resultValue = result match {
      case Success(t) => println("result = "+result.get+" t = "+t)
      case Failure(e) => println("timeout occured !")
    }

    println ("this test should NOT be ok")
    val f2: Future[String] = Future[String] { Thread.sleep(5000);   "failing" }
    val result2: Try[String] = Try(Await.result(f2, 1 second))

    result2 match {
      case Success(t) => println("result2 = "+result2.get)
      case Failure(e) => println("timeout occured !"+e.getMessage)
    }

    println ("this test should be ok")
    val f3: Future[String] = Future[String] { Thread.sleep(500);   "success" }
    (Try(Await.result(f3, 1 second))) match {
      case Success(value) => println("result3 = "+value)
      case Failure(e) => println("timeout occured !"+e.getMessage)
    }

  }
}

//----------------------------------------------------------

class TestHexaConvertion
{

  println ("--- TestHexaConvertion ---")  // call when object is created

  def test = {
    val hex = "2020202020202020202020202020202020202020202020203c3f786d6c2076657273696f6e3d22312e302220656e636f2f5361613a53656e6465723e3c536161583c2f5361613a42494331323e3c2f53613a5072696f726974793e557267656e"
    val s = DatatypeConverter.parseHexBinary(hex)
    System.out.println(new String(s))

  }

}

//--------------------------------------------------

class TestUnderscore
{

  println ("--- TestUnderscore ---")  // call when object is created

  def test = {
    val data = Map [Int , String](
      1 -> "aa",
      2 -> "bb",
      3 -> "cc",
      4 -> "dd",
      5 -> "ee"
    ).foreach(entry => System.out.println("entry._1 " + entry._1 + ", entry._2 " + entry._2))
  }
}

//--------------------------------------------------

class TestOption {

  println ("--- TestOption ---")  // call when object is created

  def test = {
    // option : An Option[T] can be either Some[T] or None object, which represents a missing value. For instance, the get method of Scala's Map produces Some(value) if a value corresponding to a given key has been found, or None if the given key is not defined in the Map.
    val capitals = Map("France" -> "Paris", "Japan" -> "Tokyo")
    println("show(capitals.get( \"Japan\")) : " + show(capitals.get("Japan")))
    println("show(capitals.get( \"India\")) : " + show(capitals.get("India")))
  }

  def show(x: Option[String]) = x match {
    case Some(s) => s
    case None => "?"
  }
}


//--------------------------------------------------
// A closure is a function, whose return value depends on the value of one or more variables declared outside this function.

class TestClosure
{
  println ("--- TestClosure ---")  // call when object is created

  def loopThrough(number: Int)(closureFunc: Int => Unit) {
    for (i <- 1 to number) {
      closureFunc(i)
    }
  }
  def test = {
    var result = 0
    val addIt = { value:Int => result += 2*value }

    loopThrough(3) { addIt }
    println("Total of 2*values from 1 to 3 is " + result)
  }
}

//--------------------------------------------------