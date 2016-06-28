package middleware

import java.util.UUID
import middleware._
import scala.collection.mutable
import scala.collection.mutable.MutableList
import scala.util.Random

/**
 * Created by wizni on 1/27/15.
 */
object DataStore {

    import scala.collection.mutable.{MutableList => MList}
    val Channels: MList[Channel] = MList()
    val Users: MList[User] = MList()
    val Followings: MList[Following] = MList()
    val PhoneNumbers: MList[PhoneNumber] = MList()
    val BroadcastCount: Map[UUID,Int] = Map()
    //Map of Channel ID with list of followers
    //val ChannelFollowers: mutable.HashMap[UUID,MList[UUID]] = _
    //Map between Phone number and Channel Id
    //val PhoneChannels: mutable.HashMap[PhoneNumber,UUID] = _

    /**
     * At the time of channel creation phone number is not assigned
     * @param name
     */
    def createChannel(name: String): Option[Channel] = {
        //For new Channel we generate a UUID
        val uuid = UUID.randomUUID()
        createChannel(Channel(uuid,name,None))
    }

    def createChannel(channel: Channel): Option[Channel] = {
        val findChannel: Option[Channel]  = Channels.find((c: Channel) =>  channel.id.equals(c.id))
        findChannel match{
            case Some(ch) =>
                None: Option[Channel]
                //Do nothing here
            case None =>
               //If its a new channel Add the Channel to the list
               Channels.+=(channel)
               Some(channel)
        }
    }

    def broadCast(channel: Channel) = {

    }
}
