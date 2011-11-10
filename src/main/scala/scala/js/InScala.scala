package scala.js

import scala.virtualization.lms.common._

trait InScala extends Base {
  type Rep[+T] = T

  protected def unit[T:Manifest](x: T) = x
}

trait JSKInScala extends JSK with NumericOpsInScala with EqualInScala with IfThenElseInScala with JSFunctionsInScala with TupleOpsInScala

trait VariablesInScala extends Variables with ReadVarImplicit with InScala with ImplicitOpsInScala {
  implicit def readVar[T:Manifest](v: Var[T]) : T = ???
  def var_new[T:Manifest](init: T): Var[T] = ???
  def var_assign[T:Manifest](lhs: Var[T], rhs: T): Unit = ???
  def var_plusequals[T:Manifest](lhs: Var[T], rhs: T): Unit = ???
  def var_minusequals[T:Manifest](lhs: Var[T], rhs: T): Unit = ???
}

trait ImplicitOpsInScala extends ImplicitOps with InScala {
  def implicit_convert[X,Y](x: X)(implicit c: X => Y, mX: Manifest[X], mY: Manifest[Y]) : Y = c(x)
}

trait NumericOpsInScala extends NumericOps with VariablesInScala {
  def numeric_plus[T:Numeric:Manifest](lhs: T, rhs: T) : T = {
    val t = implicitly[Numeric[T]]
    t.plus(lhs, rhs)
  }
  def numeric_minus[T:Numeric:Manifest](lhs: T, rhs: T) : T = {
    val t = implicitly[Numeric[T]]
    t.minus(lhs, rhs)
  }
  def numeric_times[T:Numeric:Manifest](lhs: T, rhs: T) : T = {
    val t = implicitly[Numeric[T]]
    t.times(lhs, rhs)
  }
  def numeric_divide[T:Numeric:Manifest](lhs: T, rhs: T) : T = {
    val t = implicitly[Numeric[T]]
    (t.toDouble(lhs) / t.toDouble(rhs)).asInstanceOf[T]
  }

}

trait EqualInScala extends Equal with InScala {
  def equals[A:Manifest,B:Manifest](a: A, b: B) : Boolean = a.equals(b)
  def notequals[A:Manifest,B:Manifest](a: A, b: B) : Boolean = !a.equals(b)

}

trait IfThenElseInScala extends IfThenElse with InScala {
  def __ifThenElse[T:Manifest](cond: Boolean, thenp: => T, elsep: => T): T = cond match {
    case true => thenp
    case false => elsep
  }
}

trait JSFunctionsInScala extends JSFunctions with InScala {
  implicit def doLambda[A:Manifest,B:Manifest](fun: A => B): A => B = fun
  def doApply[A:Manifest,B:Manifest](fun: A => B, arg: A): B = fun(arg)
}

trait TupleOpsInScala extends TupleOps with InScala {
  implicit def make_tuple2[A:Manifest,B:Manifest](t: (A, B)) : (A,B) = t
  implicit def make_tuple3[A:Manifest,B:Manifest,C:Manifest](t: (A, B, C)) : (A,B,C) = t
  implicit def make_tuple4[A:Manifest,B:Manifest,C:Manifest,D:Manifest](t: (A, B, C, D)) : (A,B,C,D) = t
  implicit def make_tuple5[A:Manifest,B:Manifest,C:Manifest,D:Manifest,E:Manifest](t: (A, B, C, D, E)) : (A,B,C,D,E) = t

  def tuple2_get1[A:Manifest](t: (A,_)) : A = t._1
  def tuple2_get2[B:Manifest](t: (_,B)) : B = t._2

  def tuple3_get1[A:Manifest](t: (A,_,_)) : A = t._1
  def tuple3_get2[B:Manifest](t: (_,B,_)) : B = t._2
  def tuple3_get3[C:Manifest](t: (_,_,C)) : C = t._3

  def tuple4_get1[A:Manifest](t: (A,_,_,_)) : A = t._1
  def tuple4_get2[B:Manifest](t: (_,B,_,_)) : B = t._2
  def tuple4_get3[C:Manifest](t: (_,_,C,_)) : C = t._3
  def tuple4_get4[D:Manifest](t: (_,_,_,D)) : D = t._4

  def tuple5_get1[A:Manifest](t: (A,_,_,_,_)) : A = t._1
  def tuple5_get2[B:Manifest](t: (_,B,_,_,_)) : B = t._2
  def tuple5_get3[C:Manifest](t: (_,_,C,_,_)) : C = t._3
  def tuple5_get4[D:Manifest](t: (_,_,_,D,_)) : D = t._4
  def tuple5_get5[E:Manifest](t: (_,_,_,_,E)) : E = t._5
}

trait DomsInScala extends Doms with InScala {
  import java.awt._
  import java.awt.geom._
  import javax.swing.JFrame
  import scala.collection.mutable.Stack

  val transforms : Stack[AffineTransform] = new Stack()
  var point : Point2D = new Point2D.Double(0, 0)
  var pointTransform = new AffineTransform()

  var graphics : Graphics2D = null
  def init(draw: () => Unit) {
    val frame = new JFrame() {
      override def paint(g : Graphics) {
	graphics = g.asInstanceOf[Graphics2D]
	graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
	graphics.setPaint(Color.black)
	graphics.translate(0, 30)
	draw()
      }
    }
    frame.setBackground(Color.white)
    frame.setForeground(Color.white)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.setSize(800, 300)
    frame.setVisible(true)
  }

  class ElementInScala extends Element with ElementOps {
    def getElementById(id: String) : ElementInScala = new CanvasInScala
  }
  class CanvasInScala extends ElementInScala with Canvas with CanvasOps {
    def getContext(context: String) : ContextInScala = new ContextInScala
  }
  class ContextInScala extends ElementInScala with Context with ContextOps {
    def save(): Unit =
      transforms.push(graphics.getTransform)
    def restore(): Unit =
      graphics.setTransform(transforms.pop())
    def moveTo(x: Int, y: Int) {
      point = new Point2D.Double(x, y)
      pointTransform = graphics.getTransform
    }
    def lineTo(x: Int, y: Int): Unit = {
      val start = graphics.getTransform.inverseTransform(pointTransform.transform(point, null), null)
      val end = new Point2D.Double(x, y)
      graphics.draw(new Line2D.Double(start, end))

      point = end
      pointTransform = graphics.getTransform
    }
    def scale(sx: Double, sy: Double): Unit =
      graphics.scale(sx, sy)
    def rotate(theta: Double): Unit =
      graphics.rotate(theta)
    def translate(tx: Int, ty: Int): Unit =
      graphics.translate(tx, ty)
    def closePath(): Unit = {}
    def stroke(): Unit = {}
  }
  val document = new ElementInScala
  implicit def elementOps(x: Element): ElementOps = new ElementInScala
  implicit def canvasOps(x: Canvas): CanvasOps = new CanvasInScala
  implicit def contextOps(x: Context): ContextOps = new ContextInScala
  implicit def asOps(x: X forSome {type X}): AsOps = new AsOps {
    def as[T]: T = x.asInstanceOf[T]
  }
}