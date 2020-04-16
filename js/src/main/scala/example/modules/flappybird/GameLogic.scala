package example.modules.flappybird

import com.github.oen9.slinky.bridge.konva.Konva.IFrame
import com.softwaremill.quicklens._
import scala.util.Random

object GameLogic {
  val width = 480
  val height = 640
  val groundY = 576
  val pipeWidth = 69

  val defOpt = GameOptions()
  case class GameOptions(
    holeSize: Int = 200,
    distanceBetweenPipes: Int = 250,
    pipeStartAtX: Int = 500,

    gravitationStep: Int = 5,
    upStep: Int = 20,
    rotationAngle: Int = 30,
    groundSpeed: Int = 2,
    upSlowdown: Int = 2,

    debug: Boolean = false,
  )
  case class Pipe(x: Int , y: Int)
  case class Bird(
    y: Int = height / 2,
    angle: Int = 0,
    upAcceleration: Int = 0,
  )
  case class GameState(
    gameOver: Boolean = false,
    fps: Double = 0,
    score: Int = 0,
    groundShift: Int = 0,
    bird: Bird = Bird(),
    pipe1: Pipe = generateNewPipe(defOpt.pipeStartAtX, defOpt.holeSize),
    pipe2: Pipe = generateNewPipe(defOpt.pipeStartAtX + defOpt.distanceBetweenPipes, defOpt.holeSize),
    opt: GameOptions = defOpt,
  )

  def generateNewPipe(startX: Int, holeSize: Int): Pipe = {
    val holePosition = Random.nextInt(groundY - holeSize)
    Pipe(x = startX, y = holePosition)
  }


  def movePipe(pipe: Pipe, opts: GameOptions): (Pipe, Int) = {
    if (pipe.x <= -pipeWidth)
      (generateNewPipe(opts.pipeStartAtX, opts.holeSize), 1)
    else
      (pipe.copy(x = pipe.x - opts.groundSpeed), 0)
  }

  def loop(frame: IFrame, gs: GameState): GameState = {
    val newUpAcc = Some(gs.bird.upAcceleration - gs.opt.upSlowdown).filter(_ > 0).getOrElse(0)

    val currentAcc = gs.opt.gravitationStep - newUpAcc
    val newBirdY = gs.bird.y + currentAcc

    val newAngle =
      if (currentAcc > 0) gs.opt.rotationAngle
      else if (currentAcc < 0) -gs.opt.rotationAngle
      else 0

    val newGroundShift = if (gs.groundShift < (- 15)) 0 else gs.groundShift - gs.opt.groundSpeed

    val (newPipe1, pipe1Score) = movePipe(gs.pipe1, gs.opt)
    val (newPipe2, pipe2Score) = movePipe(gs.pipe2, gs.opt)

    val newScore = gs.score + pipe1Score + pipe2Score

    gs.modify(_.bird.angle).setTo(newAngle)
      .modify(_.bird.upAcceleration).setTo(newUpAcc)
      .modify(_.bird.y).setTo(newBirdY)
      .modify(_.fps).setTo((1000 / frame.timeDiff).toInt)
      .modify(_.groundShift).setTo(newGroundShift)
      .modify(_.score).setTo(newScore)
      .modify(_.pipe1).setTo(newPipe1)
      .modify(_.pipe2).setTo(newPipe2)
  }
}
