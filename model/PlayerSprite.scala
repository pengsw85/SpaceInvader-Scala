package spaceInvaders.model

import spaceInvaders.Client
import scalafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import scala.collection.mutable.ListBuffer;
import javafx.geometry.Bounds

class PlayerSprite(xVal: Int, yVal: Int) extends ImageView("/spaceInvaders/view/icons/space-invaders.png") with Sprite {
    private val stepVal: Int = 16 // set step value for movement
    var supershoot = false;
    
    
    // set initial location of player sprite
    //x = xVal.toDouble
    //y = yVal.toDouble

    translateX = xVal
    translateY = yVal
    this.toFront()
    def moveLeft(): Unit =  {
        if (translateX() > 5) {
            // ensure player cannot move pass the game layout 
            var newXValue: Int = translateX().toInt - stepVal
            if (newXValue <= 5) {
                translateX = 6
            } else {
                translateX = newXValue
            }
            
        }
    }

    def moveRight(): Unit =  {
        // move playerSprite to right by 16 pixel
        if (translateX() <= 552) {
            // ensure player cannot move pass the game layout 
            var newXValue: Int = translateX().toInt + stepVal
            if (newXValue > 552) {
                translateX = 540
            } else {
                translateX = newXValue
            }
        }
    }

    def shoot(): Unit = {
        // create bullet and add to playerBullets list and display to game layout
        if (this.supershoot) {
            // if powerup supershoot is enabled, shoot 3 bullets instead of one
            val bulletL = new Bullet( translateX().toInt + 2, translateY().toInt - 6, Color.LIME)
            val bulletM = new Bullet( translateX().toInt + 11, translateY().toInt - 6, Color.LIME)
            val bulletR= new Bullet( translateX().toInt + 20, translateY().toInt - 6, Color.LIME)        
            PlayerSprite.playerBullets += bulletL
            PlayerSprite.playerBullets += bulletM
            PlayerSprite.playerBullets += bulletR
            PlayerSprite.roots.getChildren.add(bulletL)
            PlayerSprite.roots.getChildren.add(bulletM)
            PlayerSprite.roots.getChildren.add(bulletR)
        } else {
            val bullet = new Bullet( translateX().toInt + 11, translateY().toInt - 6, Color.LIME)
            PlayerSprite.playerBullets += bullet
            PlayerSprite.roots.getChildren.add(bullet)
        }
    }

    def getBounds(): Bounds  = {
        // get the bounds of playerSprite
        return this.asInstanceOf[ImageView].getBoundsInParent()
    }
}

object PlayerSprite {
    private var roots = Client.gameRoots
    var playerBullets: ListBuffer[Bullet] = ListBuffer[Bullet]()
    
}