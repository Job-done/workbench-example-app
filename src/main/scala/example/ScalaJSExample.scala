package example

import org.scalajs.dom
import org.scalajs.dom.{ext, html}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

case class Point(x: Int, y: Int) {
  def +(p: Point) = Point(x + p.x, y + p.y); def /(d: Int) = Point(x / d, y / d)
}

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {
  private val execStart: Long = System.currentTimeMillis()

  def logInfo(info: String): Unit = {
    dom.console.log(f"[Info][${System.currentTimeMillis() - execStart}%5d ms]" + info)
  }

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    var cursorText = ""

    canvas.width = dom.window.innerWidth.toInt
    canvas.height = dom.window.innerHeight.toInt

    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.fillStyle = "black"
    ctx.fillRect(0, 0, 10000, 20000)

    val allPrefixes: Iterator[Seq[Char]] = {
      def prefixes = ('A' to 'Z').mkString
      def comb(s: String) = (s * s.length).toSeq.combinations(3)

      comb(prefixes).flatMap(_.toSeq.permutations.toList)
    }

    lazy val interval: Int = dom.window.setInterval(() => {
      def run(prefix: Seq[Char]): Unit = {
        def queryStr(query: Seq[Char]): String = {
          def owmQueryParams = scala.collection.Map[String, String](
            "type" -> "like"
            , "mode" -> "json"
            //,"apikey" -> "<Paste your own API Key value here>"
            , "apikey" -> "9ff16c79edd6ad12396c22ed8a7996ec"
          )

          def queryMap = Map("q" -> query) ++ owmQueryParams

          queryMap.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")
        }

        // OpenWeatherMap endpoint details
        def weatherBaseURL = {
          def openWeatherMapHost = "openweathermap.org"
          def openWeatherMapAPI = "https://api.".concat(openWeatherMapHost)

          openWeatherMapAPI.concat("/data/2.5/find")
        }

        ext.Ajax.get(weatherBaseURL.concat(queryStr(prefix))).foreach { xhr =>
          // println(prefix + "\t" + xhr.responseText.length)
          val parsed = js.JSON.parse(xhr.responseText)
          //        dom.console.log(parsed)
          //        println(s"${queryStr(prefix)}, ${parsed.list.length}")
          parsed.list.map { el: js.Dynamic =>
            val (x, y) = (el.coord.lon.asInstanceOf[Double], el.coord.lat.asInstanceOf[Double])

            def t = el.main.temp.asInstanceOf[Double] // 250 ... 350 °K

            val color = {
              def color(tScaled: Int) = {
                (math.max(tScaled - 128, 0) * 2,
                  (128 - math.abs(tScaled - 128)) * 2,
                  math.max(128 - tScaled, 0) * 2)
              }

              val (r, g, b) = color(((t - 260) / 50 * 255).toInt) // 0 -> 255

              s"rgb($r, $g, $b)"
            }

            def country = el.sys.country.asInstanceOf[String]
            def screenX = (x / 180 + 1) / 2 * canvas.width

            def screenY = canvas.height - (y / 90 + 1) / 2 * canvas.height

            ctx.fillStyle = color
            ctx.fillRect(screenX - 2, screenY - 2, 4, 4)

            if (cursorText.nonEmpty) {
              ctx.fillStyle = "black"
              ctx.font = "24px Helvetica"
              ctx.textAlign = "left"
              ctx.textBaseline = "top"
              ctx.fillText(cursorText, 32, 32)
              cursorText = ""
              ctx.stroke()
            }

            if (cursorText.isEmpty) {
              cursorText =
                f"""Country $country, ${BigDecimal((t * 100 - 27315).toInt) / 100}%6s °C"""
              ctx.fillStyle = color
              ctx.font = "24px Helvetica"
              ctx.textAlign = "left"
              ctx.textBaseline = "top"
              ctx.fillText(cursorText, 32, 32)
            }
          }

        }
      }

      if (allPrefixes.hasNext) run(allPrefixes.next())
      else {
        dom.window.clearInterval(interval)
        logInfo(""""That's All Folks!"""")
      }

    }, 10)
    interval
  }

}