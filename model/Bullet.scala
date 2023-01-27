package spaceInvaders.model

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

class Bullet(x: Int, y: Int, _color: Color) extends Rectangle(2, 4, _color) {
    val stepVal: Int = 18 // set step value for movement

    // set inital location of bullet on game layout
    setTranslateX(x)
    setTranslateY(y)

    def moveUp(): Unit = {
        // move bullet up
        setTranslateY(getTranslateY() - stepVal)
    }

    def moveDown(): Unit = {
        // move bullet down
        setTranslateY(getTranslateY() + stepVal)
    }

    def hitSprite(sprite: Sprite): Boolean = {
        // return true, if this bullet intersected with sprite argument
        return this.getBoundsInParent().intersects(sprite.getBounds()) 
    }

}
