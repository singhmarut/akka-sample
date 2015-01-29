import akka.actor.Props
import akka.testkit.TestActorRef
import java.util.UUID
import middleware._
import org.scalatest.{FlatSpecLike, Matchers, BeforeAndAfterAll, FunSuite}
import scala.util.Random

/**
 * Created by wizni on 1/27/15.
 */
class ChannelManagerSpec  extends FunSuite   with Matchers with BeforeAndAfterAll{

    import scala.collection.mutable.{MutableList => MList}

    override def beforeAll: Unit = {

    }

    def initDummyPhoneList() = {
        var count = 0
        val numbers: MList[PhoneNumber] = MList()
        PhoneNumber(Random.nextString(10))
        while (count < 5){
            numbers += PhoneNumber(Random.nextString(10))
        }
        numbers
    }
//
//    test("Should change phone number") {
//        val channels = MList[Channel](Channel(UUID.randomUUID(),"Dummy", Some("12345")),Channel(UUID.randomUUID(),"Dummy", Some("12345")),Channel(UUID.randomUUID(),"Dummy", Some("23456")),
//            Channel(UUID.randomUUID(),"Dummy", None))
////        val channels = MList[Channel](
////            Channel(UUID.randomUUID(),"Dummy", None))
//        ChannelManager.getMinimumUsedPhoneNumber()
//        assert(!phone.isEmpty && phone.get == "23456")
//
//    }

    test("Should assign number to channel") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"))
        val NewChannel = Channel(UUID.randomUUID(),"Channel1", None)
        val Channels = List[Channel](NewChannel)
        val Followings = List[Following]()
        ChannelManager.broadCast(NewChannel.id,Channels,PhoneNumbers,Followings)
        assert (!NewChannel.phoneNumber.isEmpty && NewChannel.phoneNumber.get.equals("123"))
    }

    test("Should assign already used number to channel") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"))
        val AssignedChannel = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel1", None)

        val Channels = List[Channel](AssignedChannel,UnassignedChannel)
        val Followings = List[Following]()

        ChannelManager.broadCast(UnassignedChannel.id,Channels,PhoneNumbers,Followings)
        assert (!UnassignedChannel.phoneNumber.isEmpty && UnassignedChannel.phoneNumber.get.equals("123"))
    }

    test("Should assign already used number to channel when no conflicting") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"))
        val AssignedChannel = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel2", None)

        val Channels = List[Channel](AssignedChannel,UnassignedChannel)

        val Followings = List[Following](Following(AssignedChannel.id,UUID.randomUUID()))

        ChannelManager.broadCast(UnassignedChannel.id,Channels,PhoneNumbers,Followings)
        assert (!UnassignedChannel.phoneNumber.isEmpty && UnassignedChannel.phoneNumber.get.equals("123"))
    }

    test("Should assign already number to channel when different users are following channel with same number") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"),PhoneNumber("345"))
        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some("234"))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel3", None)

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,UnassignedChannel)

        val Followings = List[Following](Following(AssignedChannel1.id,UUID.randomUUID()),Following(AssignedChannel2.id,UUID.randomUUID()),Following(UnassignedChannel.id,UUID.randomUUID()))

        ChannelManager.broadCast(UnassignedChannel.id,Channels,PhoneNumbers,Followings)
        assert (!UnassignedChannel.phoneNumber.isEmpty && !UnassignedChannel.phoneNumber.get.equals("345"))
    }

    test("Should assign Used number to channel when there is channel available not being followed by this user") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"),PhoneNumber("345"))
        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some("234"))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel3", None)

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,UnassignedChannel)

        val userId = UUID.randomUUID()
        val otherUserId =  UUID.randomUUID()
        val Followings = List[Following](Following(AssignedChannel1.id,userId),Following(AssignedChannel2.id,otherUserId),Following(UnassignedChannel.id,userId))

        ChannelManager.broadCast(UnassignedChannel.id,Channels,PhoneNumbers,Followings)
        assert (!UnassignedChannel.phoneNumber.isEmpty && UnassignedChannel.phoneNumber.get.equals("234"))
    }

    test("Should assign unused number to channel when user is following other channels with same number") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"),PhoneNumber("345"))
        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some("234"))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel3", None)

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,UnassignedChannel)

        val userId = UUID.randomUUID()
        val Followings = List[Following](Following(AssignedChannel1.id,userId),Following(AssignedChannel2.id,userId),Following(UnassignedChannel.id,userId))

        ChannelManager.broadCast(UnassignedChannel.id,Channels,PhoneNumbers,Followings)
        assert (!UnassignedChannel.phoneNumber.isEmpty && UnassignedChannel.phoneNumber.get.equals("345"))
    }

    test("Should not change Phone number when following unassigned Channel") {

        val number1 = "123"
        val number2 = "234"
        val PhoneNumbers = List[PhoneNumber](PhoneNumber(number1),PhoneNumber(number2),PhoneNumber("345"))

        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some(PhoneNumbers(0).number))

        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some(PhoneNumbers(1).number))
        val UnassignedChannel = Channel(UUID.randomUUID(),"Channel3", None)

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,UnassignedChannel)

        val Followings = List[Following](Following(AssignedChannel1.id,UUID.randomUUID()),Following(AssignedChannel2.id,UUID.randomUUID()),Following(UnassignedChannel.id,UUID.randomUUID()))

        val broadCastMap: Map[UUID,Int] = Map(AssignedChannel1.id -> 1,(AssignedChannel2.id -> 5))

        ChannelManager.followChannel(Following(UnassignedChannel.id,UUID.randomUUID()),Channels,PhoneNumbers,Followings,broadCastMap)
        assert (AssignedChannel1.phoneNumber.get.equals(number1) && AssignedChannel2.phoneNumber.get.equals(number2))
    }

    test("Should be able to change least impacting channel when following conflicting channel") {

        val PhoneNumbers = List[PhoneNumber](PhoneNumber("123"),PhoneNumber("234"),PhoneNumber("345"))
        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some("123"))
        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some("234"))
        val AssignedChannel3 = Channel(UUID.randomUUID(),"Channel3", Some("234"))

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,AssignedChannel3)
        val userId = UUID.randomUUID()

        val Followings = List[Following](Following(AssignedChannel1.id,userId),Following(AssignedChannel2.id,userId))

        val broadCastMap: Map[UUID,Int] = Map(AssignedChannel1.id -> 1,(AssignedChannel2.id -> 5),AssignedChannel3.id -> 6)

        ChannelManager.followChannel(Following(AssignedChannel3.id,userId),Channels,PhoneNumbers,Followings,broadCastMap)
        assert (!AssignedChannel1.phoneNumber.isEmpty && AssignedChannel1.phoneNumber.get.equals("345"))
    }

    test("Should Not change phone number when following non-conflicting channel") {

        val number1 = "123"
        val number2 = "234"
        val number3 = "345"

        val PhoneNumbers = List[PhoneNumber](PhoneNumber(number1),PhoneNumber(number2),PhoneNumber(number3))
        val AssignedChannel1 = Channel(UUID.randomUUID(),"Channel1", Some(number1))
        val AssignedChannel2 = Channel(UUID.randomUUID(),"Channel2", Some(number2))
        val AssignedChannel3 = Channel(UUID.randomUUID(),"Channel3", Some(number3))

        val Channels = List[Channel](AssignedChannel1,AssignedChannel2,AssignedChannel3)
        val userId = UUID.randomUUID()

        val Followings = List[Following](Following(AssignedChannel1.id,userId),Following(AssignedChannel2.id,userId))

        val broadCastMap: Map[UUID,Int] = Map(AssignedChannel1.id -> 1,(AssignedChannel2.id -> 5),AssignedChannel3.id -> 6)

        ChannelManager.followChannel(Following(AssignedChannel3.id,userId),Channels,PhoneNumbers,Followings,broadCastMap)
        assert (AssignedChannel1.phoneNumber.get.equals(number1) && AssignedChannel2.phoneNumber.get.equals(number2))
    }
}
