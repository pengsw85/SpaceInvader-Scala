package spaceInvaders
import akka.actor.typed.ActorRef
import spaceInvaders.model.AlienSprite
import java.io.Serializable

case class Alien(uid: Int, x: Int, y: Int) extends Serializable{
    override def toString: String = s"alien with id ${uid}"
}
