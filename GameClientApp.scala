package spaceInvaders

import akka.cluster.typed._
import akka.{ actor => classic }
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.adapter._
import com.typesafe.config.ConfigFactory
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import scalafx.Includes._
import scala.concurrent.Future
import scala.concurrent.duration._  

import spaceInvaders.model._
import spaceInvaders.view.RootControllerLayout;
import spaceInvaders.util.Database
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.{Scene, Parent}
import scalafx.Includes._ //implicit function
import scalafxml.core.{NoDependencyResolver, FXMLView, FXMLLoader}
import javafx.{scene => jfxs}
import javafx.stage.Stage
import javafx.scene.layout.{Pane, AnchorPane}
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.animation.AnimationTimer
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.Alert
import scala.util.Random;
import javafx.application.Platform
import scalafx.scene.image.Image


object Client extends JFXApp {
    //implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    
    // initialise random number generator
    val randomNumberGenerator =  new Random()
    // initialise player sprite
    
    val player2 = new TeammateSprite(292, 345)
    val player = new PlayerSprite(292, 345)

    // create fxml file reference for menu
    val rootResource = getClass.getResourceAsStream("view/MenuLayout.fxml")
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(rootResource)
    val roots = loader.getRoot[jfxs.layout.AnchorPane]

    // create fxml file reference for game
    val gameRootResource = getClass.getResourceAsStream("view/GameLayout.fxml")
    val gameLoader = new FXMLLoader(null, NoDependencyResolver)
    gameLoader.load(gameRootResource)
    val control = gameLoader.getController[RootControllerLayout#Controller]
    val gameRoots = gameLoader.getRoot[jfxs.layout.Pane]
    
    val playerClient: ActorSystem[GameClient.Command] = ActorSystem(GameClient(), "HelloSystem")
    playerClient ! GameClient.start
    
    // initialise stage and set the scene to menu on load
    stage = new PrimaryStage {
        title = "Space Invaders"
        resizable = false;
        scene = new Scene {
            root = roots
            stylesheets += getClass.getResource("view/Style.css").toString()
        }  
        icons += new Image(getClass.getResourceAsStream("view/icons/space-invaders.png"))
    }

    // initialise global variables for game function
    var gameOver = false
    var win = false
    var frameCount: Long = 0
    var alienHorMoveCount = 0
    var alienToMoveLeft = true
    var alienVerMoveCount = 0
    var scoreTimer: Float = 0
    var powerUpTimer: Int = 0
    var power: SuperShoot = null
    // create AnimationTimer that updates game every 16 frames
    val timer = AnimationTimer(t => {
        frameCount += 1L
        // update timer display every frame
        scoreTimer = (frameCount.toFloat * 0.016f)
        control.showScore(Some(scoreTimer))

        if (frameCount % 16 == 0) {
            if (gameOver) {
                // if game ended, create alert and end game
                Platform.runLater(new Runnable() {
                    override def run() = {
                        // get final frame count for game
                        var finalFrameCount = frameCount
                        // reset game
                        gameReset()

                        if (win) {
                            // if player won, display win message
                            val finalScore = finalFrameCount * 0.016f
                            showGameOver("YOU WON", s"You took: ${finalScore}seconds")
                        } else {
                            // display game over message
                            showGameOver("YOU DIED", "GG")
                        }

                        // return back to menu page
                        stage.scene().root = roots                 
                    }
                })
            }
            // handle horizontal movement direction, switch direction every 3 count or every 48 frames
            if (alienHorMoveCount == 3){
                alienToMoveLeft = !alienToMoveLeft;
                alienHorMoveCount = 0
            } 
            // handle vertial movement direction, switch direction every 16 count or every 736 frames
            if (alienVerMoveCount == 16) {
                alienVerMove()
                alienVerMoveCount = 0
            }

            // update movement counter
            alienHorMoveCount += 1
            alienVerMoveCount += 1

            updateGame(alienToMoveLeft)
        }
    }) 

    def setGameScene() = {
        stage.scene().root = gameRoots
        control.showDifficulty(Some("Local"))
        gameRoots.getChildren().add(player)
        gameRoots.getChildren().add(player2)
    }

    def initialiseGame(): Parent = {        
        stage.scene().onKeyPressed = (k: KeyEvent) => {
            k.code match {
                case KeyCode.Left => {
                    player.moveLeft()
                    GameClient.teammates.get.ref ! GameClient.TeammateMoveLeft()
                }
                case KeyCode.Right => {
                    player.moveRight()
                    GameClient.teammates.get.ref ! GameClient.TeammateMoveRight()
                }
                case KeyCode.Space => player.shoot()
                case _ => println(k.code)
            }
        }   
        // start the game and AnimationTimer
        timer.start
        return roots
    }

    def updateGame(alienMoveLeft: Boolean): Unit = {
        // update game's visual, bullet movement, alien movement and shoot and check win conditions
        bulletUpdate()
        alienHorMove(alienMoveLeft)
        checkWin()
    }

    def bulletUpdate(): Unit = {
        // update bullet travel for player
        for (bullet <- PlayerSprite.playerBullets) {
            // player bullet travel up
            bullet.moveUp()
            for (alien <- GameClient.alienSpriteList){
                // check if bullet hit any alien
                if (bullet.hitSprite(alien)) {
                    // if bullet hit, remove bullet and alien from game
                    //GameClient.alienSpriteList -= alien
                    GameClient.teammates.get.ref ! GameClient.RemoveAlien(alien.uid)
                    GameClient.alienSpriteList -= alien
                    PlayerSprite.playerBullets -= bullet
                    gameRoots.getChildren.remove(alien)
                    gameRoots.getChildren.remove(bullet)
                }
            } 
        }
    }

    def alienHorMove(toMoveLeft: Boolean): Unit = {
        // update all alien horizontal movement based on argument
        // argument decide if alien move left or right 
        
        if (toMoveLeft) {
            for (alien <- AlienSprite.alienList) {
                alien.moveLeft()
            }
        } else {
            for (alien <- AlienSprite.alienList) {
                alien.moveRight()
            }
        }
    }
    
    def alienVerMove(): Unit = {
        // update all alien horizontal movement
        for (alien <- AlienSprite.alienList) {
            alien.moveDown()
            if (alien.translateY() >= 200) {
                // check if alien reached line, if so end game
                win = false
                gameOver = true
                return 
            }
        }
    }

    def checkWin(): Unit = {
        // check if player defeated all alien
        if (GameClient.alienSpriteList.isEmpty) {
            // if all alien defeated, end game, game is won
            win = true
            gameOver = true
        }
    }

    def showGameOver(headText:String, conText: String): Unit = {
        // display game over alert
        var alert = new Alert(Alert.AlertType.Warning) {
          title       = "Game Over"
          headerText  = headText
          contentText =  conText           
        }.showAndWait()
    } 

    def gameReset(): Unit = {  
        // reset all global variable, stop timer, and remove all sprites and bullet
        timer.stop()
     
        player.translateX = 292
        player.translateY = 345
        player2.translateX = 292
        player2.translateY = 345

        // remove all player bullets from display
        for (bullet <- PlayerSprite.playerBullets) {
            gameRoots.getChildren.remove(bullet)
        }
        // remove all alien bullets from display
        for (bullet <- AlienSprite.alienBullets) {
            gameRoots.getChildren.remove(bullet)
        }
        // remove all aliens from display
        for (alien <- AlienSprite.alienList) {
            gameRoots.getChildren.remove(alien)
        }
        // remove player powerup
        player.supershoot = false
        // remove all bullets and alien
        PlayerSprite.playerBullets.clear()
        AlienSprite.alienBullets.clear()
        AlienSprite.alienList.clear()
        gameOver = false
        powerUpTimer = 0
        if (power != null) {
            gameRoots.getChildren().remove(power)
        }
        power = null
        // reset frameCount
        frameCount = 0L
    }

    stage.onCloseRequest = handle( {
        playerClient.terminate
    })
}