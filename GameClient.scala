package spaceInvaders

import akka.actor.typed.{ActorRef, PostStop, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist,ServiceKey}
import akka.cluster.typed._
import akka.{ actor => classic }
import akka.actor.typed.scaladsl.adapter._
import scalafx.collections.ObservableHashSet
import scalafx.application.Platform
import akka.cluster.ClusterEvent.ReachabilityEvent
import akka.cluster.ClusterEvent.ReachableMember
import akka.cluster.ClusterEvent.UnreachableMember
import akka.cluster.ClusterEvent.MemberEvent
import akka.actor.Address
import spaceInvaders.model.AlienSprite
import spaceInvaders.Upnp._
import scala.collection.mutable.ListBuffer

object GameClient {
    sealed trait Command

    case object start extends Command
    case class StartJoin() extends Command
    final case class Joined(x: Player) extends Command
    case class StartGame() extends Command
    case class TeammateMoveLeft() extends Command
    case class TeammateMoveRight() extends Command
    case class Message(value: String, from: ActorRef[GameServer.Command]) extends Command
    case class SystemMessage(value: String) extends Command

    //case class GetAlienBattalion(alienlist: Iterable[AlienSprite]) extends Command
    case class RemoveAlien(alienUid: Int) extends Command
    case class GetAlienBattalion(alienlist: Iterable[Alien]) extends Command
    
    final case object FindTheServer extends Command
    private case class ListingResponse(listing: Receptionist.Listing) extends Command
    private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Command
    var teammates: Option[Player] = None
    var alienSpriteList: ListBuffer[AlienSprite] = ListBuffer()

    var remoteOpt: Option[ActorRef[GameServer.Command]] = None 

    def apply(): Behavior[GameClient.Command] =
        Behaviors.setup { context =>
        var counter = 0
        val upnpRef = context.spawn(Upnp(), Upnp.name)
        upnpRef ! AddPortMapping(20000)

        val reachabilityAdapter = context.messageAdapter(ReachabilityChange)
        Cluster(context.system).subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

        var remoteOpt:Option[ActorRef[GameServer.Command]] = None 

        val listingAdapter: ActorRef[Receptionist.Listing] =
            context.messageAdapter { listing =>
                println(s"listingAdapter:listing: ${listing.toString}")
                GameClient.ListingResponse(listing)
            }

        context.system.receptionist ! Receptionist.Subscribe(GameServer.ServerKey, listingAdapter)

        Behaviors.receiveMessage { message =>
            message match {
                case GameClient.start =>
                    context.self ! FindTheServer 
                    for (remote <- remoteOpt){
                        remote ! GameServer.Message("i am here", context.self)
                    }
                    Behaviors.same
             
                case FindTheServer =>
                    context.system.receptionist !
                        Receptionist.Find(GameServer.ServerKey, listingAdapter)
                    Behaviors.same

                case ListingResponse(GameServer.ServerKey.Listing(listings)) =>
                    val xs: Set[ActorRef[GameServer.Command]] = listings
                    println("-- listing response --")
                    for (x <- xs) {
                        remoteOpt = Some(x)
                        println("x:" + x)
                    }

                    Behaviors.same

                case GameClient.SystemMessage(value) =>
                    for (remote <- remoteOpt){
                        remote ! GameServer.Message(value, context.self)
                    }
                    Behaviors.same

                case StartJoin() => 
                    println(s"player Joined\n")
                    for (remote <- remoteOpt){
                        remote ! GameServer.JoinGame(context.self)
                        remote ! GameServer.Message("player joined", context.self)
                    }

                    Behaviors.same

                case GameClient.Joined(x) =>
                    println("--gameClient.joined--")
                    teammates = Some(x)
                    println("x: " + x.ref)
                    println("--gameClient.joined end--")
                    Behaviors.same


                case GameClient.TeammateMoveLeft() =>
                    Platform.runLater( () =>{
                        Client.player2.moveLeft()
                    })
                    
                    Behaviors.same
                
                case GameClient.TeammateMoveRight() =>
                    Platform.runLater( () =>{
                        Client.player2.moveRight()
                        println(context.self.toString + " " + "moving Right===============")
                    })
                    
                    Behaviors.same
                
                case GameClient.RemoveAlien(alienId) => 

                    Platform.runLater( () => {
                        println("=========== game client remove alien ==========")
                        var alien = alienSpriteList.find(x => x.uid == alienId) 
                        alienSpriteList -= alien.get
                        println(alienId)
                        println(alien.get)
                        Client.gameRoots.getChildren.remove(alien.get)
                        AlienSprite.alienList -= alien.get;
                        println("=========== game client remove alien ==========")
                    })
                    Behaviors.same
                

                case GameClient.GetAlienBattalion(x: Iterable[Alien]) => {
                    println("========GameClient.GetAlienBattalion=======")
                    Platform.runLater(
                        () -> {
                            
                            for (alien <- x) {
                                var aliensp = new AlienSprite(alien.x, alien.y, alien.uid)
                                alienSpriteList += aliensp
                                Client.control.addAlienSprite(aliensp)
                            }
                        }
                    )
                    Behaviors.same
                }

                case StartGame() => {
                    Client.initialiseGame()
                    println(Client)
                    Behaviors.same
                }

                case _ => {
                    Behaviors.unhandled
                }
            }
        }
    }
}

