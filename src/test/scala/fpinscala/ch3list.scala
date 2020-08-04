package fpinscala

/**
 * Functional Programming in Scala
 */

import org.scalatest.funsuite.AnyFunSuite

sealed trait List[+A] // abstract class
case object Nil extends List[Nothing] // singleton class
case class Cons[+B](head: B, tail: List[B]) extends List[B]

object List {

  def sum(ints: List[Int]): Int = ints match {
    case Nil => 0
    case Cons(x,xs) => x + sum(xs)
  }

  def product(ints: List[Double]): Double = ints match {
    case Nil => 1
    case Cons(0.0, _) => 0.0
    case Cons(x, xs) => x * product(xs)
  }

  // variadic function: it takes 0 or more arguments of type A (Seq)
  // https://www.scala-lang.org/api/current/scala/collection/immutable/Seq.html
  def apply[A](as: A*): List[A] =
    if (as.isEmpty) Nil
    else Cons(as.head, apply(as.tail: _*)) // _*: spread

  def tail[A](list: List[A]): List[A] = list match {
    case Nil => sys.error("no more element")
    case Cons(_, xs) => xs
  }

  @scala.annotation.tailrec
  def drop[A](list: List[A], n: Int): List[A] = list match {
    case list if n <= 0 => list  // pattern guard
    case Nil => Nil
    case Cons(_, xs) => drop(xs, n-1)
  }

  @scala.annotation.tailrec
  def dropFromBook[A](l: List[A], n: Int): List[A] =
    if (n <= 0) l
    else l match {
      case Nil => Nil
      case Cons(_,t) => dropFromBook(t, n-1)
    }

  @scala.annotation.tailrec
  def dropWhile[A](list: List[A], f: A => Boolean): List[A] = list match {
    case Cons(x, xs) if f(x) => dropWhile(xs, f)
    case _ => list
  }

  /**
   * 3.3.2 Type information flows from left to right => no need a type annotation for f.
   *
   * This cannot have the same name
   * -> ... have same type after erasure: (list: fpinscala.List, f: Function1)
   */
  @scala.annotation.tailrec
  def dropWhile2[A](as: List[A])(f: A => Boolean): List[A] = as match {
    case Cons(h, t) if f(h) => dropWhile2(t)(f)
    case _ => as
  }

  def init[A](list: List[A]): List[A] = list match {
    case Nil => Nil
    case Cons(_, Nil) => Nil
    case Cons(x, xs) => Cons(x, init(xs))
  }

  def setHead[A](list: List[A], element: A): List[A] = list match {
    case Nil => Cons(element, Nil)
    case Cons(_, xs) => Cons(element, xs)
  }

  def foldRight[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    as match {
      case Nil => z
      case Cons(h, t) => f(h, foldRight(t, z)(f))
    }

  def foldRight2[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(reverse(as), z)((x, y) => f(y, x))

  def foldRight3[A, B](as: List[A], z: B)(f: (A, B) => B): B =
    foldLeft(as, (b: B) => b)((g, a) => b => g(f(a, b)))(z)

  @scala.annotation.tailrec
  def foldLeft[A, B](as: List[A], z: B)(f: (B, A) => B): B =
    as match {
      case Nil => z
      case Cons(h, t) => foldLeft(t, f(z, h))(f)
    }

  def foldRandom[A, B](as: List[A], z: B)(f: (B, A) => B): B =
    as match {
      case Nil => z
      case Cons(h, t) => f(foldRandom(t, f(z, h))(f), h)
    }

  def foldLeft2[A, B](as: List[A], z: B)(f: (B, A) => B): B =
    foldRight(reverse(as), z)((x, y) => f(y, x))

  def sum2(as: List[Int]): Int =
    foldRight(as, 0)(_ + _)

  def product2(as: List[Double]): Double =
    foldRight(as, 1.0)(_ * _)

  def length[A](as: List[A]): Int =
    foldRight(as, 0)((_, x) => 1 + x)

  def sum3(as: List[Int]): Int =
    foldLeft(as, 0)(_ + _)

  def product3(as: List[Double]): Double =
    foldLeft(as, 1.0)(_ * _)

  def length2[A](as: List[A]): Int =
    foldLeft(as, 0)((acc, _) => 1 + acc)

  def reverse[A](as: List[A]): List[A] =
    foldLeft(as, Nil: List[A])((x, y) => Cons(y, x))

  def append[A](as: List[A], x: A): List[A] =
    foldRight(as, List(x))(Cons(_, _))

  def append2[A](l: List[A], r: List[A]): List[A] =
    foldRight(l, r)(Cons(_, _))

  def concat[A](as: List[List[A]]): List[A] =
    foldRight(as, Nil: List[A])(append2)

  def add1(as: List[Int]): List[Int] =
    foldRight(as, Nil: List[Int])((h, t) => Cons(h+1, t))

  def toString(as: List[Double]): List[String] =
    foldRight(as, Nil: List[String])((h, t) => Cons(h.toString, t))

  def map[A, B](as: List[A])(f: A => B): List[B] =
    foldRight(as, Nil: List[B])((h, t) => Cons(f(h), t))

  def filter[A](as: List[A])(f: A => Boolean): List[A] =
    foldRight(as, Nil:List[A])((h, t) => {
      if (f(h)) Cons(h, t)
      else t
    })

  def flatMap[A](as: List[A])(f: A => List[A]): List[A] =
    foldRight(as, Nil: List[A])((h, t) => append2(f(h), t))

  def flatMap2[A](as: List[A])(f: A => List[A]): List[A] =
    concat(map(as)(f))

  def flatMapFilter[A](as: List[A])(f: A => Boolean): List[A] =
    flatMap(as)(a => if (f(a)) List(a) else Nil)

  def add(l: List[Int], r: List[Int]): List[Int] = {
    // n^2 complexity - similar to foldLeft
    @scala.annotation.tailrec
    def go(l: List[Int], r: List[Int], acc: List[Int]): List[Int] = {
      (l, r) match {
        case (Nil, _) => acc
        case (_, Nil) => acc
        case (Cons(x, xs), Cons(y, ys)) =>
          go(xs, ys, append(acc, x+y))
      }
    }

    go(l, r, Nil)
  }

  def add2(l: List[Int], r: List[Int]): List[Int] = {
    // no tailrec - similar to foldRight
    def go(l: List[Int], r: List[Int], acc: List[Int]): List[Int] = {
      (l, r) match {
        case (Nil, _) => acc
        case (_, Nil) => acc
        case (Cons(x, xs), Cons(y, ys)) => Cons(x+y, go(xs, ys, acc))
      }
    }

    go(l, r, Nil)
  }

  def add3(l: List[Int], r: List[Int]): List[Int] = {
      (l, r) match {
        case (Nil, _) => Nil
        case (_, Nil) => Nil
        case (Cons(x, xs), Cons(y, ys)) => Cons(x+y, add3(xs, ys))
      }
    }

  def zipWith[A, B](l: List[A], r: List[A])(f: (A, A) => B): List[B] =
    (l, r) match {
      case (Nil, _) => Nil
      case (_, Nil) => Nil
      case (Cons(x, xs), Cons(y, ys)) => Cons(f(x, y), zipWith(xs, ys)(f))
    }

  def hasSubsequence[A](sup: List[A], sub: List[A]): Boolean = {
    @scala.annotation.tailrec
    def go[B](l: List[B], r: List[B]): Boolean = (l, r) match {
      case (_, Nil) => true
      case (Nil, _) => false
      case (Cons(x, xs), Cons(y, ys)) =>
        if (x == y) go(xs, ys)
        else go(xs, sub)
    }

    go(sup, sub)
  }

}

class ch3 extends AnyFunSuite {

  test("Cons") {
    val c = Cons(1, List(2,3))

    assert(c.head == 1)
    assert(c.tail == List(2,3))

    assert(Cons(1, Nil) == List(1))
    assert(Cons(1, Cons(2, Nil)) == List(1, 2))
  }

  test("3.1") {
    def matchTest(list: List[Int]): Int =
      list match {
        case Cons(x, Cons(2, Cons(4, _))) => x // 1
        case Nil => 42 // 2
        case Cons(x, Cons(y, Cons(3, Cons(4, _)))) => x + y // 3
        case Cons(h, t) => h + List.sum(t) // 4
        case _ => 101 // 5
      }

    assert(matchTest(List(1,2,4,4,5)) == 1) // 1
    assert(matchTest(Nil) == 42) // 2
    assert(matchTest(List()) == 42) // 2
    assert(matchTest(List(1,2,3,4,5)) == 3) // 3
    assert(matchTest(List(2,3,4,4,5)) == 18) // 4
    assert(matchTest(List(2,3,4,4,5,6)) == 24) // 4
  }

  test("3.2") {
    assert(List.tail(List(1,2,3)) == List(2,3))
  }

  test("3.3") {
    assert(List.setHead(List(1,2), 3) == List(3,2))
    assert(List.setHead(Nil, 3) == List(3))
  }

  test("3.4") {
    assert(List.drop(List(1,2,3), 2) == List(3))
    assert(List.drop(List(1), -1) == List(1))
  }

  test("3.5") {
    assert(List.dropWhile(List(1,2,3), (x: Int) => x < 3) == List(3))
    assert(List.dropWhile2(List(1,2,3))(_ < 3) == List(3))
  }

  test("3.6") {
    assert(List.init(List(1,2,3)) == List(1,2))
  }

  test("foldRight") {
    assert(List.sum2(List(1, 2)) == 3)
    assert(List.product2(List(2, 3)) == 6)
  }

  test("3.8") {
    // "Nil: List[Int]" is needed to prevent Scala from inferring it as List[Nothing]
    // The last element is Consed first.
    assert(List.foldRight(List(1, 2, 3), Nil: List[Int])(Cons(_, _)) == List(1, 2, 3))

    val a = Nil
    val b = Nil: List[Nothing]
    val c = Nil: List[Int]

    assert(a == b)
    assert(a == c)
  }

  test("3.9") {
    assert(List.length(Nil) == 0)
    assert(List.length(List()) == 0)
    assert(List.length(List(1,2,3)) == 3)
  }

  test("3.10") {
    // The head is added first
    assert(List.foldLeft(List(1,2,3), 0)(_ + _) == 6)
    assert(List.foldLeft(List(1,2,3), Nil: List[Int])((x, y) => Cons(y, x)) == List(3, 2, 1))
  }

  test("3.11") {
    assert(List.sum3(List(1,2,3)) == 6)
    assert(List.product3(List(1,2,3,4)) == 24)
    assert(List.length2(List(1,2,3)) ==3)
  }

  test("3.12") {
    assert(List.reverse(List(1,2,3)) == List(3,2,1))
  }

  test("3.13") {
    assert(List.foldRight2(List(1,2,3), Nil: List[Int])(Cons(_, _)) == List(1,2,3))
    assert(List.foldLeft2(List(1,2,3), Nil: List[Int])((x, y) => Cons(y, x)) == List(3,2,1))
    assert(List.foldRight3(List(1,2,3), Nil: List[Int])(Cons(_, _)) == List(1,2,3))
  }

  test("3.14") {
    assert(List.append(List(1), 2) == List(1,2))
    assert(List.append2(List(1), List(2)) == List(1,2))
  }

  test("3.15") {
    assert(List.concat(List(List(1),List(2))) == List(1,2))
  }

  test("3.16") {
    assert(List.add1(List(1,2,3)) == List(2,3,4))
  }

  test("3.17") {
    assert(List.toString(List(1.0,2.0)) == List("1.0", "2.0"))
  }

  test("3.18") {
    assert(List.map(List(1, 2))(_ * 3) == List(3, 6))
  }

  test("3.19") {
    assert(List.filter(List(1,2,3,4))(_ % 2 == 0) == List(2,4))
  }

  test("3.20") {
    assert(List.flatMap(List(1,2))(x => List(x,x)) == List(1,1,2,2))
    assert(List.flatMap2(List(1,2))(x => List(x,x)) == List(1,1,2,2))
  }

  test("3.21") {
    assert(List.flatMapFilter(List(1,2,3,4))(_ % 2 == 0) == List(2,4))
  }

  test("3.22") {
    assert(List.add(List(1,2,3), List(1,2,3)) == List(2,4,6))
    assert(List.add(List(1,2), List(1,2,3)) == List(2,4))

    assert(List.add2(List(1,2,3), List(1,2,3)) == List(2,4,6))
    assert(List.add2(List(1,2), List(1,2,3)) == List(2,4))

    assert(List.add3(List(1,2,3), List(1,2,3)) == List(2,4,6))
    assert(List.add3(List(1,2), List(1,2,3)) == List(2,4))
  }

  test("3.23") {
    assert(List.zipWith(List(1,2), List(1,2))(_ + _) == List(2,4))
    assert(List.zipWith(List(1,2), List(1,2))(_ * _) == List(1,4))
  }

  test("3.24") {
    assert(List.hasSubsequence(List(1,2,3), List(1)))
    assert(List.hasSubsequence(List(1,2,3), List(2,3)))
    assert(List.hasSubsequence(List(1,2,4,3,2,3), List(2,3)))

    assert(!List.hasSubsequence(List(1,2,4,2), List(2,3)))
    assert(!List.hasSubsequence(List(1,2,4,3,2), List(2,3)))
  }

  test("foldRandom") {
    assert(List.foldRandom(List(1,2,3), Nil: List[Int])((a, b) => Cons(b, a))
      == List(1,2,3,3,2,1))
  }
}