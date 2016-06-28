package middleware

import akka.actor.{Props, Actor}
import java.util.UUID
import scala.util.Random

/**
 * Created by wizni on 1/29/15.
 */
class ChannelMonitor extends Actor{
    val communicator = context.system.actorOf(Props[ChannelCommunicator])

    def receive: Actor.Receive = {
        case msg: Channel =>
            communicator ! msg
        case msg: Following =>
            communicator ! msg
        case msg: BroadcastMessage =>
           communicator ! msg
        case msg: ChannelReply =>
            msg.channel match{
                case None =>
                case Some(channel) =>
                    DataStore.Channels.+=(channel)
            }
        case msg: FollowingReply =>
            DataStore.Followings.+=(msg.following)
    }
}
