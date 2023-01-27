package spaceInvaders.model


class SuperShoot() extends Powerup("spaceInvaders/view/icons/ray.png") {
    def enablePowerUp(player: PlayerSprite): Unit = {
        player.supershoot = true;
    }

    def pickUp(player: PlayerSprite): Boolean = {
        return this.getBoundsInParent().intersects(player.getBounds()) 
    }
}