package example.model

import example.shared.Dto
import fs2.concurrent.Queue
import zio._

object ChatData {
  case class User(id: Int, name: String, out: Queue[Task[*], Dto.ChatDto])
}
