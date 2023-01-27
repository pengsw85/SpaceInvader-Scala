package spaceInvaders.model

//import javafx.scene.image.ImageView;
import scalafx.scene.image.ImageView
import scala.collection.mutable.ListBuffer
import javafx.scene.paint.Color;
import javafx.geometry.Bounds
import spaceInvaders.Client
import java.io.Serializable

class AlienSprite(xValue: Int, yValue: Int, val uid: Int, var canShoot:Boolean = false, var downStepVal: Int = 12) extends ImageView("/spaceInvaders/view/icons/alien.png") with Sprite with Serializable {
    private val stepVal: Int = 5 // set step value for movement
    x = xValue.toDouble
    y = yValue.toDouble

    translateX = x()
    translateY = y()
    AlienSprite.alienList += this;

    def moveLeft(): Unit =  {
        // move left by 5 pixel
        translateX = translateX() - stepVal
    }

    def moveRight(): Unit =  {
        // move right by 5 pixel
        translateX = translateX() + stepVal
    }

    def moveDown(): Unit =  {
        // move down by downstepval pixel
        translateY = translateY().toInt + downStepVal
    }

    def enableShoot() = {
        // enable shooting by alien
        canShoot = true
    }

    def disableShoot() = {
        // disable shooting by alien
        canShoot = false
    }

    def shoot(): Unit = {
        if (canShoot) {
            // create bullet and display on screen if alien can shoot
            val bullet = new Bullet( x().toInt + 7, y().toInt + 16, Color.YELLOW)
            AlienSprite.alienBullets += bullet
            AlienSprite.roots.getChildren.add(bullet)
        }
    }

    def getBounds(): Bounds  = {
        // return BoundsInParent
        return this.asInstanceOf[ImageView].getBoundsInParent()
    }
}

object AlienSprite {
    private var roots = Client.gameRoots
    var alienBullets: ListBuffer[Bullet] = ListBuffer[Bullet]() 
    var alienList: ListBuffer[AlienSprite] = ListBuffer[AlienSprite]()

    def getAlienFromList(x: Int, y: Int): AlienSprite = {
        // return specific alien based on coordinate x and y
        for (alien <- alienList) {
            if (alien.x == x && alien.y == y) {
                return alien
            }
        }
        return null
    }
}