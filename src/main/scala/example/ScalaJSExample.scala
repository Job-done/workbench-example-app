package example

import org.scalajs.dom
import org.scalajs.dom.html

import scala.util.Random
import scala.scalajs.js
import js.annotation.{JSExport, JSExportTopLevel}

case class Point(x: Double, y: Double) {
  def +(p: Point) = Point(x + p.x, y + p.y)
  def -(p: Point) = Point(x - p.x, y - p.y)
  def *(d: Double) = Point(x * d, y * d)
  def /(d: Double) = Point(x / d, y / d)
  def length = Math.sqrt(x * x + y * y)
}

case class Enemy(pos: Point, vel: Point)

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {

  val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  var (startTime, player) = (js.Date.now(), Point(dom.window.innerWidth.toInt / 2, dom.window.innerHeight.toInt / 2))

  var enemies = Seq.empty[Enemy]

  var death: Option[(String, Int)] = None

  @JSExport
  def main(): Unit = {
    dom.console.log("main")

    dom.document.onmousemove = { e =>
      player = Point(e.clientX.toInt, e.clientY.toInt)
      (): js.Any
    }
    dom.window.setInterval(() => {
      def run() = {

        canvas.height = dom.window.innerHeight.toInt
        canvas.width = dom.window.innerWidth.toInt

        // doing

        def randSpeed = Random.nextInt(5) - 3

        def diedAndNewEnemies =
          enemies.filter(e => e.pos.x >= 0 && e.pos.x <= canvas.width && e.pos.y >= 0 && e.pos.y <= canvas.height) ++
            Seq.fill(20 - enemies.length)(Enemy(Point(Random.nextInt(canvas.width), 0), Point(randSpeed, randSpeed)))

        enemies = diedAndNewEnemies.map { en: Enemy =>
          val newEnimyPos = en.pos + en.vel
          val delta = player - newEnimyPos
          Enemy(newEnimyPos, en.vel + delta / delta.length / 100)
        }

        if (enemies.exists(e => (e.pos - player).length < 20)) {
          death = Some((s"You lasted $deltaT seconds", 100))
          enemies = enemies.filter(e => (e.pos - player).length > 20)
        }
      }

      def draw() = {
        // drawing
        ctx.fillStyle = "black"

        ctx.fillRect(0, 0, canvas.width, canvas.height)
        death match {
          case None =>

            ctx.fillStyle = "white"
            ctx.fillRect(player.x - 10, player.y - 10, 20, 20)
            ctx.fillText("player", player.x - 15, player.y - 30)

            ctx.fillStyle = "red"
            for (enemy <- enemies) {
              ctx.fillRect(enemy.pos.x - 10, enemy.pos.y - 10, 20, 20)
            }

            ctx.fillStyle = "white"

            ctx.fillText(s"$deltaT seconds", canvas.width / 2 - 100, canvas.height / 5)
          case Some((msg, time)) =>
            ctx.fillStyle = "white"
            ctx.fillText(msg, canvas.width / 2 - 100, canvas.height / 2)
            if (time - 1 == 0) {
              death = None
              startTime = js.Date.now()
            } else {
              death = Option((msg, time - 1))
            }
        }
      }

      run()
      draw()
    }, 20)
  }

  def deltaT = ((js.Date.now() - startTime) / 1000).toInt

}