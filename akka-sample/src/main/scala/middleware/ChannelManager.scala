package middleware

import java.util.UUID
import scala.util.Random


/**
 * Created by wizni on 1/28/15.
 */
object ChannelManager {
    import scala.collection.mutable.{MutableList => MList}

    def getMinimumUsedPhoneNumber(PhoneNumbers: List[PhoneNumber], Channels: List[Channel]): Option[String] = {

        val phoneChannels: List[Channel] = Channels.filter(p => {
            !p.phoneNumber.isEmpty && PhoneNumbers.contains(PhoneNumber(p.phoneNumber.get))
        })
        var phone: Option[String] = None
        if (!phoneChannels.isEmpty){
            phone = Some(phoneChannels.groupBy(_.phoneNumber.get).reduceLeft((l,r) => { if (r._2.size < l._2.size) r else l})._1)
        }

        phone
    }

    def getLeastImpactingChannel(channels: List[Channel],broadCastCount: Map[UUID,Int]): Channel = {
        var phoneNumber = None: Option[String]
        //We have to change the phone number of the channel which has actually least number of broadcast to minimize disruption
        val channel = channels.reduceLeft((l,r) => { if (getChannelBroadcastCount(r,broadCastCount) < getChannelBroadcastCount(l,broadCastCount)) r else l })
        channel
    }

    def getChannelBroadcastCount(channel: Channel,broadCastCount: Map[UUID,Int]): Int = {
        var count = 0
        if (broadCastCount.contains(channel.id)){
            count = broadCastCount(channel.id)
        }
        count
    }

    @annotation.tailrec
    def assignPhoneToChannel(Channels: List[Channel],PhoneNumbers: List[PhoneNumber], Followings: List[Following],CurrentChannel: Channel): Option[String] = {

        //First attempt is to use a number which is already being used in some channel
        var phoneNumber : Option[String] = getMinimumUsedPhoneNumber(PhoneNumbers,Channels)
        var remainingNumbers: List[PhoneNumber] = PhoneNumbers
        phoneNumber match{
            case None =>
                //Do nothing
            case Some(number) =>
                remainingNumbers = PhoneNumbers.filterNot(_.number == number)
        }

        phoneNumber match {
            case None =>
                phoneNumber = Some(remainingNumbers(0).number)
            case Some(number) =>

                if (isConflicting(Followings,number ,Channels, CurrentChannel.id)){
                    phoneNumber = None
                }
        }
        //Assign the Phone number to channel
        CurrentChannel.phoneNumber = phoneNumber
        phoneNumber match {
            case None   =>
            case Some(number) =>
                phoneNumber
                //Phone number is assigned...Do Nothing
        }

        assignPhoneToChannel(Channels,remainingNumbers,Followings,CurrentChannel)
    }


    def isConflicting(Followings: List[Following],phoneNumber: String, channels: List[Channel], currentChannelId: UUID):Boolean = {
        //STEP1: Find all channels associated with this number
        val phoneChannels = channels.filterNot(c => c.phoneNumber.isEmpty || !c.phoneNumber.get.equals(phoneNumber))
        var isConflicting = false
        //STEP2: Find all the users who are following current channel
        //If any of the user who is following this channel and also any other channel associated with this number then we need to try again
        Followings.filter(_.channelId.equals(currentChannelId)).foreach(following => {
            phoneChannels.foreach( phoneChannel => {
                //Users following this channel
                val followings = Followings.filter(_.channelId.equals(phoneChannel.id))
                followings.find(_.UserId.equals(following.UserId)) match{
                    case None =>
                    case Some(following) =>
                        isConflicting = true //
                }
            })
        })
        isConflicting
    }

    def getUserChannelById(Channels: MList[Channel],ChannelId: UUID): Option[Channel] = {
        Channels.find(_.id.equals(ChannelId))
    }

    def followChannel(following: Following,Channels: List[Channel],PhoneNumbers: List[PhoneNumber], Followings: List[Following],broadCastMap: Map[UUID,Int]): Following = {
        //When one person is following two channels that both have the same phone number, this is called a "Collision".
        //Find the channels which are being followed by same user
        //If phone number being used by any of these channels are same then there is a collision
        var newFollowings: List[Following] = Followings
        Channels.find(_.id.equals(following.channelId)) match {
            case None =>
                //Do nothing
            case Some(channel) =>
                //Make sure user is not trying to follow the same channel again
                Followings.find(f => f.channelId == following.channelId && f.UserId == following.UserId ) match {
                    case None =>
                        newFollowings = Followings :+ following
                    case Some(following) =>

                }
                //If the channel user wants to follow does not have a phone number then
                if (!channel.phoneNumber.isEmpty){
                    if (isConflicting(newFollowings,channel.phoneNumber.get, Channels,channel.id)){
                        val remainingNumbers = PhoneNumbers//.filterNot(_.number == channel.phoneNumber.get)
                        //Cause there is a conflict...get the least impacting channel and change its number
                        assignPhoneToChannel(Channels,remainingNumbers,newFollowings,getLeastImpactingChannel(Channels,broadCastMap))
                    }
                }
        }
        following
    }

    def broadCast(channelId: UUID,Channels: List[Channel],PhoneNumbers: List[PhoneNumber], Followings: List[Following]) = {
        Channels.find(_.id.toString.equals(channelId.toString)) match{
            case None =>
            case Some(channel) =>
                assignPhoneToChannel(Channels,PhoneNumbers,Followings,channel)
                //Some dummy numbers...Raw Implementation
                DataStore.BroadcastCount.+(channel.id -> Random.nextInt)
        }
    }
}
