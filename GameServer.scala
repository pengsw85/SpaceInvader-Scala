package spaceInvaders;

import spaceInvaders.model.AlienSprite
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import spaceInvaders.Upnp
import spaceInvaders.Upnp.AddPortMapping
import scalafx.collections.ObservableHashSet
import javafx.embed.swing.JFXPanel



object GameServer extends Thread {
    var jfxPanel:JFXPanel = new JFXPanel();
    var alienId: Integer = 0
    sealed trait Command
    case class Message(value: String, from: ActorRef[GameClient.Command]) extends Command
    case class JoinGame(from: ActorRef[GameClient.Command]) extends Command
    val ServerKey: ServiceKey[GameServer.Command] = ServiceKey("Server")
    
    val players = new ObservableHashSet[Player]()

    def createAliens(): ObservableHashSet[Alien] = {
        // create battallion of alien
        // argument determine the number of line the battallion will have and whether they can shoot         
        // each line will have 8 aliens
        var alienSpriteList = new ObservableHashSet[Alien]()
        // create alien lines that is not able to shoot at first
        for (i <- 0 to 2) {
            for (a <- 0 to 5) {
                alienId += 1
                alienSpriteList += new Alien(alienId, 58 + a*31, 60 + i*20)
            }
        }
        return alienSpriteList
    }

    def apply(): Behavior[GameServer.Command] =
        Behaviors.setup { context =>
        val upnpRef = context.spawn(Upnp(), Upnp.name)
        upnpRef ! AddPortMapping(20000)

        context.system.receptionist ! Receptionist.Register(ServerKey, context.self)

        Behaviors.receiveMessage { message =>
            message match {
                case Message(value, from) =>
                    println(s"Server received message '${value}'")
                    from ! GameClient.Message("how are you", context.self)
                    Behaviors.same

                case JoinGame(from) =>

                    players += Player(from)
                    println("gameserver. JoinGAme")
                    println("length of players: " + players.toList.size)
                    println(players.toList)
                    println("joingame from server\n\n")
                    //if (players.toList.size % 2 == 0 && players.toList.size != 0) {
                    if (players.toList.size  == 2 ) {
                        var test = players.toList.takeRight(2)

                        var al = createAliens().toList 

                        test.head.ref ! GameClient.Joined(test.last)
                        test.last.ref ! GameClient.Joined(test.head)
                        println("testing1=========")
                        test.head.ref ! GameClient.GetAlienBattalion(al)
                        test.last.ref ! GameClient.GetAlienBattalion(al)
                        println("testing2=========")
                        test.head.ref ! GameClient.StartGame()
                        test.last.ref ! GameClient.StartGame()
                        players.clear()
                    }
                    Behaviors.same

                case _ => {
                    Behaviors.unhandled
                }
            }
        }
    }
}

object GameServerApp extends App {
    val gameServer: ActorSystem[GameServer.Command] = ActorSystem(GameServer(), "HelloSystem")
}
