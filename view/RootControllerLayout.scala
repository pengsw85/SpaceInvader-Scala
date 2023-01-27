package spaceInvaders.view
import scalafx.scene.input.MouseEvent
import scalafx.scene.input.KeyEvent
import scalafxml.core.macros.sfxml
import scalafx.scene.control.Label
import scalafx.scene.layout.Pane
import spaceInvaders.model.AlienSprite

@sfxml
class RootControllerLayout(private val scoreLabel: Label, private val difficultyLabel: Label, private val highscoreLabel: Label, private val myPane: Pane) {
    def addAlienSprite(alien: AlienSprite) = {
        myPane.getChildren().add(alien)
    }
    
    def showScore(score: Option[Float]) = {
        score match {
            case Some(x) => 
            scoreLabel.text = x.toString()
            case None =>
            scoreLabel.text = "0.0"
        }
    }

    def showDifficulty(difficulty: Option[String]) = {
        difficulty match {
            case Some(x) => 
            difficultyLabel.text = x.toString()
            case None =>
            difficultyLabel.text = ""
        }
    }

    def showHighscore(highscore: Option[Float]) = {
        highscore match {
            case Some(x) => 
            highscoreLabel.text = x.toString()
            case None =>
            highscoreLabel.text = "----"
        }
    }
    showHighscore(None)
    showScore(None)
    showDifficulty(None)
}

