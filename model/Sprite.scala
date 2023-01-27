package spaceInvaders.model

import javafx.geometry.Bounds

trait Sprite {
    def moveLeft(): Unit
    def moveRight(): Unit
    def shoot(): Unit
    def getBounds(): Bounds
}