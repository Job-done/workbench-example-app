package example

import java.awt.Desktop
import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import upickle.Js
import upickle.default.{Reader, Writer}

import scala.concurrent.ExecutionContext.Implicits.global

object Template {

  import scalatags.Text.all._
  import scalatags.Text.tags2.title

  def txt = "<!DOCTYPE html>" +
    html(
      head(
        title("Example Scala.js application"),
        meta(httpEquiv := "Content-Type", content := "text/html; charset=UTF-8"),
        script(`type` := "text/javascript", src := "/client-fastopt.js"),
        script(`type` := "text/javascript", src := "//localhost:12345/workbench.js"),
        link(
          rel := "stylesheet",
          `type` := "text/css",
          href := "META-INF/resources/webjars/bootstrap/4.3.1/css/bootstrap.min.css"
        )
      ),
      body(margin := 0)(
        script("ScalaJSExample.main()")
      )
    )
}

object AutowireServer extends autowire.Server[Js.Value, Reader, Writer] {
  def read[Result: Reader](p: Js.Value) = upickle.default.readJs[Result](p)

  def write[Result: Writer](r: Result) = upickle.default.writeJs(r)
}

object Server extends Api {
  val (httpHost, httpPort) = ("localhost", 8080)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    val route = {
      get {
        pathSingleSlash {
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              Template.txt
            )
          }
        } ~
          getFromResourceDirectory("")
      } ~
        post {
          path("api" / Segments) { s =>
            extract(_.request.entity match {
              case HttpEntity.Strict(nb: ContentType.NonBinary, data) => data.decodeString(nb.charset.value)
              case _ => ""
            }) { e =>
              complete {
                AutowireServer.route[Api](Server)(
                  autowire.Core.Request(
                    s,
                    upickle.json.read(e).asInstanceOf[Js.Obj].value.toMap
                  )
                ).map(upickle.json.write(_))
              }
            }
          }
        }
    }

    Http().bindAndHandle(route, httpHost, port = httpPort)
    val uri = new URI(s"http:/$httpHost:$httpPort/")
    println(s"Responding to $uri")

    try {
      Desktop.getDesktop.browse(uri)
    }
    catch {
      case _: Throwable =>
        println(s"Automatic start of Web browser not possible.\nWeb browser must be started manually.")
    }

  }

  def list(path: String) = {
    val chunks = path.split("/", -1)

    def prefix = "./" + chunks.dropRight(1).mkString("/")

    val files = Option(new java.io.File(prefix).list()).toSeq.flatten
    files.filter(_.startsWith(chunks.last))
  }
}