package example

import org.scalajs.dom
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.Random

case class Point(x: Int, y: Int) {
  def +(p: Point) = Point(x + p.x, y + p.y)

  def /(d: Int) = Point(x / d, y / d)
}

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {
  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val corners = Seq(Point(255, 255), Point(0, 255), Point(128, 0))
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    var (count, p) = (0, Point(0, 0))

    def run() = (0 until 10).foreach { _ =>

      def clear() = {
        ctx.fillStyle = "black"
        ctx.fillRect(0, 0, 255, 255)
      }

      if (count % 3000 == 0) clear()
      count += 1
      p = (p + corners(Random.nextInt(3))) / 2

      val height = 512.0 / (255 + p.y)
      ctx.fillStyle = s"rgb(${((255 - p.x) * height).toInt}, ${(p.x * height).toInt}, ${p.y})"

      ctx.fillRect(p.x, p.y, 1, 1)
    }

    dom.window.setInterval(() => run(), 50)
  }
}