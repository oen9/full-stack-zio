package example.modules.services

import zio._
import zio.random._

object randomService {
  type RandomService = Has[RandomService.Service]

  object RandomService {
    trait Service {
      def getRandom: Task[Int]
    }
    val live: ZLayer[Random, Nothing, RandomService] = ZLayer.fromFunction(random =>
      new Service {
        def getRandom: zio.Task[Int] = random.get.nextIntBetween(0, 9000)
      }
    )
  }

  def getRandom: ZIO[RandomService, Throwable, Int] =
    ZIO.accessM[RandomService](_.get.getRandom)
}
