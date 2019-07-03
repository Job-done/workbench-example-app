package example

import org.scalajs.dom
import org.scalajs.dom.{ext, html}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

case class Point(x: Int, y: Int) {
  def +(p: Point) = Point(x + p.x, y + p.y)

  def /(d: Int) = Point(x / d, y / d)
}

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {
  // OpenWeatherMap endpoint details
  def weatherBaseURL = {
    def openWeatherMapHost = "openweathermap.org"

    def openWeatherMapAPI = "https://api.".concat(openWeatherMapHost)

    openWeatherMapAPI.concat("/data/2.5/find")
  }


  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = dom.window.innerWidth.toInt
    canvas.height = dom.window.innerHeight.toInt

    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, 10000, 20000)

    val prefixes = 'A' to 'Z'
    val allPrefixes = for {
      a <- prefixes
      b <- prefixes
      c <- prefixes
    } yield s"$b$a$c"
    var i = 0

    def run(prefix: String) = {
      val owmQueryParams = scala.collection.mutable.Map[String, String](
        "type" -> "like"
        , "mode" -> "json"
        //,"apikey" -> "<Paste your own API Key value here>"
        , "apikey" -> "9ff16c79edd6ad12396c22ed8a7996ec"
      )

      owmQueryParams += ("q" -> prefix)

      val queryStr = owmQueryParams.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")

      i += 1

      val fut = ext.Ajax.get(weatherBaseURL.concat(queryStr))
      fut.foreach { xhr =>
        // println(prefix + "\t" + xhr.responseText.length)
        val parsed = js.JSON.parse(xhr.responseText)
        //        dom.console.log(parsed)
        parsed.list.map { el: js.Dynamic =>
          val x = el.coord.lon.asInstanceOf[Double]
          val y = el.coord.lat.asInstanceOf[Double]
          val t = el.main.temp.asInstanceOf[Double] // 250 -> 350

          println(s"$queryStr, Reponse $x $y $t")


          val screenX = (x / 180 + 1) / 2 * canvas.width
          val screenY = canvas.height - (y / 90 + 1) / 2 * canvas.height

          val tScaled = ((t - 260) / 50 * 255).toInt // 0 - 255
        val (r, g, b) = color(tScaled)
          ctx.fillStyle = s"rgb($r, $g, $b)"
          ctx.fillRect(screenX - 2, screenY - 2, 4, 4)
        }

      }
    }

    def color(tScaled: Int) = { // 0 -> 255
      val r = math.max(tScaled - 128, 0) * 2
      val g = (128 - math.abs(tScaled - 128)) * 2
      val b = math.max(128 - tScaled, 0) * 2
      (r, g, b)
    }
    dom.window.setInterval(() => run(allPrefixes(i)), 2000)
  }
}
