package spaceInvaders.model
import javafx.scene.image.ImageView;

abstract class Powerup(val _url: String) extends ImageView(_url) {
    def enablePowerUp(player: PlayerSprite): Unit
    
    def spawnLocation(x: Int, y: Int) {
        setTranslateX(x)
        setTranslateY(y)
    }
}

