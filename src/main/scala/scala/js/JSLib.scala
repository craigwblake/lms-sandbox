package scala.js

import scala.virtualization.lms.common._

trait JSLib extends JSProxyBase with JSLiteral {
  val window: Rep[Window]
  trait Window {
    def setTimeout(func: Rep[Unit => Unit], delay: Rep[Int]): Rep[Int]
  }
  implicit def repToWindow(x: Rep[Window]): Window = repProxy[Window](x)

  val json: Rep[JSON]
  trait JSON {
    def stringify(literal: Rep[JSLiteral]): Rep[String]
    def parse[T <: JSLiteral](data: Rep[String]): Rep[T]
  }
  implicit def repToJSON(x: Rep[JSON]): JSON = repProxy[JSON](x)

}

trait JSLibExp extends JSLib with JSProxyExp with JSLiteralExp {
  case object WindowVar extends Exp[Window]
  val window = WindowVar

  case object JSONVar extends Exp[JSON]
  val json = JSONVar
}

trait JSGenLib extends JSGenProxy with JSGenLiteral {
  val IR: JSLibExp
  import IR._

  override def quote(x: Exp[Any]) : String = x match {
    case WindowVar => "window"
    case JSONVar => "JSON"
    case _ => super.quote(x)
  }
}
