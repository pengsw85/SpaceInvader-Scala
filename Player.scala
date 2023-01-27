package spaceInvaders
import akka.actor.typed.ActorRef
import spaceInvaders.model.AlienSprite

case class Player(ref: ActorRef[GameClient.Command]) {
  override def toString: String = ref.toString
}
