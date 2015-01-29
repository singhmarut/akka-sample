package middleware

import akka.actor.Actor
import java.util.UUID
import scala.util.Random

/**
 * Created by wizni on 1/27/15.
 */
class ChannelCommunicator extends Actor{

  def receive: Actor.Receive = {
    case msg: Channel =>
        sender ! ChannelReply(DataStore.createChannel(msg))
    case msg: Following =>
        val following =(ChannelManager.followChannel(msg,DataStore.Channels.toList,DataStore.PhoneNumbers.toList,DataStore.Followings.toList,DataStore.BroadcastCount))
        sender ! following
    case msg: BroadcastMessage =>
       ChannelManager.broadCast(msg.channelId,DataStore.Channels.toList,DataStore.PhoneNumbers.toList,DataStore.Followings.toList)
  }
}

case class BroadcastMessage(channelId: UUID)
case class ChannelReply(channel: Option[Channel])
case class FollowingReply(following: Following)


