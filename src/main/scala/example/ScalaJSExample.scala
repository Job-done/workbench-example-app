package example

import org.scalajs.dom
import org.scalajs.dom.raw.ImageData

import scala.language.{existentials, implicitConversions, postfixOps}
import scala.math.{Pi, abs, cos, sin, sqrt, tan}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

/**
  * A simple ray tracer, taken from the PyPy benchmarks
  *
  * https://bitbucket.org/pypy/benchmarks/src/846fa56a282b/own/raytrace-simple.py?at=default
  *
  * Half the lines of code
  *
  * Author: https://github.com/lihaoyi
  */

@JSExportTopLevel("ScalaJSExample")
object ScalaJSExample {
  private type Color = Vec
  private val execStart: Long = System.currentTimeMillis()
  private val Epsilon = 0.00001
  private val Color: Vec.type = Vec

  private val canvas = dom.document.getElementById("canvas").asInstanceOf[dom.html.Canvas]
  canvas.width = 1024
  canvas.height = 1024

  private val ctx: dom.CanvasRenderingContext2D =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def logInfo(info: String) = {
    dom.console.log(f"[Info][${System.currentTimeMillis() - execStart}%5d ms]" + info)
  }


  @JSExport
  def main(): Unit = {
    def s: Scene = {
      def spiral = for (i <- 0 until 11) yield {
        val theta = i * (i + 5) * Pi / 100 + 0.3
        def center = (0 - 4 * sin(theta), 1.5 - i / 2.0, 0 - 4 * cos(theta))
        def form = Sphere(center, 0.3 + i * 0.1)
        def surface = Flat((i / 6.0, 1 - i / 6.0, 0.5))

        (form, surface)
      }

      def drops = {
        case class Refractor(refractiveIndex: Double = 0.5) extends Surface {
          def colorAt(scene: Scene, ray: Ray, p: Vec, normal: Vec.Unit, depth: Int): Color = {
            val r = if ((normal dot ray.vector) < 0) refractiveIndex else 1.0 / refractiveIndex
            val c = (normal * -1) dot ray.vector
            val sqrtValue = 1 - r * r * (1 - c * c)

            def refractedOrreflected = if (sqrtValue > 0) {
              ray.vector * r + normal * (r * c - sqrt(sqrtValue))
            } else {
              def perp = ray.vector dot normal

              Vec.denormalizer(ray.vector) + normal * 2 * perp
            }

            scene.rayColor(Ray(p, refractedOrreflected), depth)
          }
        }

        Seq(
          Sphere((2.5, 2.5, -8), 0.3),
          Sphere((1.5, 2.2, -7), 0.25),
          Sphere((-1.3, 0.8, -8.5), 0.15),
          Sphere((0.5, -2.5, -7.5), 0.2),
          Sphere((-1.8, 2.3, -7.5), 0.3),
          Sphere((-1.8, -2.3, -7.5), 0.3),
          Sphere((1.3, 0.0, -8), 0.25)
        ).map(_ -> Refractor())
      }

      case class Checked(baseColor: Color = Color(1, 1, 1),
                         specularC: Double = 0.3,
                         lambertC: Double = 0.6,
                         otherColor: Color = (0, 0, 0),
                         checkSize: Double = 1) extends SolidSurface {
        override def baseColorAt(p: Vec): Color = {
          val v = p * (1.0 / checkSize)

          def f(x: Double) = (abs(x) + 0.5).toInt

          if ((f(v.x) + f(v.y) + f(v.z)) % 2 == 1) otherColor else baseColor
        }
      }

      new Scene(objects = Seq(
        Sphere((0, 0, 0), 2) -> Flat((1, 1, 1), specularC = 0.6, lambertC = 0.4),
        Plane((0, 4, 0), (0, 1, 0)) -> Checked(),
        Plane((0, -4, 0), (0, 1, 0)) -> Flat((0.9, 1, 1)),
        Plane((6, 0, 0), (1, 0, 0)) -> Flat((1, 0.9, 1)),
        Plane((-6, 0, 0), (1, 0, 0)) -> Flat((1, 1, 0.9)),
        Plane((0, 0, 6), (0, 0, 1)) -> Flat((0.9, 0.9, 1))
      ) ++ spiral ++ drops,
        lightPoints = Seq(
          Light((0, -3, 0), (3, 3, 0)),
          Light((3, 3, 0), (0, 3, 3)),
          Light((-3, 3, 0), (3, 0, 3))
        ),
        position = (0, 0, -15),
        lookingAt = (0, 0, 0),
        fieldOfView = 45.0
      )
    }

    def c: Canvas = new Canvas {
      val width = math.min(canvas.width, canvas.height)
      val height = math.min(canvas.width, canvas.height)
      val data: ImageData = ctx.getImageData(0, 0, canvas.width, canvas.height)

      def save(y: Int): Unit = {
        // println("Saving...")
        ctx.putImageData(data, 0, 0, 0, y - 1, width, 1)
      }

      def plot(x: Int, y: Int, rgb: Color): Unit = {
        val index = (y * data.width + x) * 4
        data.data(index + 0) = (rgb.x * 255).toInt
        data.data(index + 1) = (rgb.y * 255).toInt
        data.data(index + 2) = (rgb.z * 255).toInt
        data.data(index + 3) = 255
      }
    }

    s.render(c)
  }

  abstract class Canvas {
    def width: Int
    def height: Int
    def save(y: Int): Unit
    def plot(x: Int, y: Int, rgb: Color): Unit
  }

  abstract class Form {
    def intersectionTime(ray: Ray): Double

    def normalAt(p: Vec): Vec
  }

  abstract class SolidSurface extends Surface {
    val specularC: Double

    def baseColorAt(p: Vec): Color

    def lambertC: Double

    def colorAt(scene: Scene, ray: Ray, p: Vec, normal: Vec.Unit, depth: Int): Color = {
      val b = baseColorAt(p)
      val specular = {
        val reflectedRay = Ray(p, ray.vector.reflectThrough(normal))
        val reflectedColor = scene.rayColor(reflectedRay, depth)
        reflectedColor * specularC
      }
      val lambert = {
        var lambertAmount = Vec(0, 0, 0)
        for (light <- scene.lightPoints) {
          if (scene.lightIsVisible(light.center, p)) {
            val d = p - light.center

            def dLengthSqr = d.magnitude * d.magnitude

            def contribution = light.color * abs(d dot normal / dLengthSqr)

            lambertAmount += contribution
          }
        }
        b * lambertAmount * lambertC
      }

      def ambientC: Double = 1.0 - specularC - lambertC
      def ambient = b * ambientC

      specular + lambert + ambient
    }

  }

  abstract class Surface {
    def colorAt(scene: Scene, ray: Ray, p: Vec, normal: Vec.Unit, depth: Int): Color
  }

  case class Flat(baseColor: Color = Color(1, 1, 1),
                  specularC: Double = 0.3,
                  lambertC: Double = 0.6) extends SolidSurface {
    def baseColorAt(p: Vec): Color = baseColor
  }

  case class Light(center: Vec, color: Color)

  case class Plane(point: Vec, normal: Vec.Unit) extends Form {
    def intersectionTime(ray: Ray): Double = {
      val v = ray.vector dot normal
      if (v != 0) ((point - ray.point) dot normal) / v else -1
    }

    def normalAt(p: Vec): Color = normal
  }

  case class Ray(point: Vec, vector: Vec.Unit) {
    def pointAtTime(t: Double): Color = point + vector * t
  }

  class Scene(objects: Seq[(Form, Surface)],
              val lightPoints: Seq[Light],
              position: Vec,
              lookingAt: Vec,
              fieldOfView: Double) {

    def lightIsVisible(l: Vec, p: Vec): Boolean = {
      val (ray, length) = (Ray(p, l - p), (l - p).magnitude)

      objects.forall { case (o: Form, _: Surface) =>
        val t: Double = o.intersectionTime(ray)
        t <= ScalaJSExample.Epsilon || t >= length - ScalaJSExample.Epsilon
      }
    }

    def render(canvas: Canvas) = {
      def fovRadians = Pi * (fieldOfView / 2.0) / 180.0

      val halfWidth = tan(fovRadians)
      val eye = Ray(position, lookingAt - position)
      val vpRight = eye.vector.cross((0, 1, 0)).normalized


      def halfHeight = halfWidth
      def width = halfWidth * 2
      def height = halfHeight * 2
      def pixelWidth = width / (canvas.width - 1)
      def pixelHeight = height / (canvas.height - 1)
      def vpUp = vpRight.cross(eye.vector).normalized

      var y = 0
      lazy val interval: Int = dom.window.setInterval({ () =>
        for (x <- 0 until canvas.width) {
          def xcomp = vpRight * (x * pixelWidth - halfWidth)
          def ycomp = vpUp * (y * pixelHeight - halfHeight)
          def ray = Ray(eye.point, xcomp + ycomp + eye.vector)
          def color = rayColor(ray, 0)

          canvas.plot(x, y, color)
        }
        canvas.save(y)
        if (y > canvas.height) {
          dom.window.clearInterval(interval)
          logInfo(""""That's All Folks!"""")
        }
        y += 1
      }, 0)
      interval
    }

    def rayColor(ray: Ray, depth: Int): Color = {
      if (depth > 3) (0, 0, 0)
      else {
        val (minT, minO, minS) = objects.foldLeft(-1.0.toDouble, null: Form, null: Surface) {
          case (maxColor, (o, s)) => {
            val t = o.intersectionTime(ray)
            if (t > ScalaJSExample.Epsilon && (t < maxColor._1 || maxColor._1 < 0)) (t, o, s)
            else maxColor
          }
        }
        minT match {
          case -1 => (0, 0, 0)
          case _ =>
            val p = ray.pointAtTime(minT)
            minS.colorAt(this, ray, p, minO.normalAt(p), depth + 1)
        }
      }
    }
  }

  case class Sphere(center: Vec, radius: Double) extends Form {
    def intersectionTime(ray: Ray): Double = {
      val cp = center - ray.point
      val v = cp dot ray.vector
      val d = radius * radius - ((cp dot cp) - v * v)
      if (d < 0) -1 else v - sqrt(d)
    }

    def normalAt(p: Vec): Color = (p - center).normalized
  }

  final case class Vec(x: Double, y: Double, z: Double) {
    def +(o: Vec) = Vec(x + o.x, y + o.y, z + o.z)

    def *(o: Vec) = Vec(x * o.x, y * o.y, z * o.z)

    def cross(o: Vec) = Vec(
      y * o.z - z * o.y,
      z * o.x - x * o.z,
      x * o.y - y * o.x
    )

    def normalized: Color = this / magnitude
    def magnitude: Double = sqrt(this dot this)
    def /(f: Double) = Vec(x / f, y / f, z / f)
    def reflectThrough(normal: Vec): Color = this - normal * (this dot normal) * 2
    def -(o: Vec) = Vec(x - o.x, y - o.y, z - o.z)
    def *(f: Double) = Vec(x * f, y * f, z * f)
    def dot(o: Vec): Double = x * o.x + y * o.y + z * o.z
  }

  object Vec {

    case class Unit(x: Double, y: Double, z: Double)

    implicit def normalizer(v: Vec): Unit = {
      val l = v.magnitude
      Unit(v.x / l, v.y / l, v.z / l)
    }

    implicit def denormalizer(v: Vec.Unit): Color = new Vec(v.x, v.y, v.z)

    implicit def pointify[X: Numeric, Y: Numeric, Z: Numeric](x: (X, Y, Z)): Vec = Vec(
      implicitly[Numeric[X]].toDouble(x._1),
      implicitly[Numeric[Y]].toDouble(x._2),
      implicitly[Numeric[Z]].toDouble(x._3)
    )

    implicit def pointify2[X: Numeric, Y: Numeric, Z: Numeric](x: (X, Y, Z)): Vec.Unit = Vec.normalizer(x)
  }

}