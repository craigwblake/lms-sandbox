package scala.js

import scala.util.continuations._
import scala.virtualization.lms.common._

import java.io.PrintWriter

trait Ajax extends JS with CPS {

  type Request = JSLiteral {
    val url: String
    val `type`: String
    val dataType: String
    val data: JSLiteral
  }

  val ajax = new AjaxOps
  
  class AjaxOps {
    def get[Response:Manifest](request: Rep[Request]): Rep[Response] @suspendable =
      ajax_get[Response](request)
  }

  def ajax_get[Response:Manifest](request: Rep[Request]): Rep[Response] @suspendable

}

trait AjaxExp extends JSExp with Ajax {
  
  case class AjaxGet[Response:Manifest](request: Rep[Request], success: Rep[Response => Unit]) extends Def[Unit]
  
  def ajax_get[Response:Manifest](request: Rep[Request]): Rep[Response] @suspendable = shift { k =>
    reflectEffect(AjaxGet(request, fun(k)))
  }

}

trait GenAjax extends JSGenBase {
  val IR: AjaxExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any])(implicit stream: PrintWriter) = rhs match {
    case AjaxGet(req, succ) => 
      stream.println(quote(req) + ".success = " + quote(succ)) 
      emitValDef(sym, "$.ajax(" + quote(req) + ")")
    case _ => super.emitNode(sym, rhs)
  }
}
