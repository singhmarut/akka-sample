import akka.actor.{ActorRef, Inbox, Props, ActorSystem}
import java.util.concurrent.TimeUnit
import java.util.UUID
import middleware._
import middleware.PhoneNumber
import scala.concurrent.duration.FiniteDuration
import scala.util.Random

/**
 * Created by wizni on 1/27/15.
 */
object Evernym extends App{

    //Populate some dummy phones in datastore
    DataStore.PhoneNumbers.+=(PhoneNumber("123"),PhoneNumber("234"),PhoneNumber("345"))

    // Create the 'evernym' actor system
    val system = ActorSystem("evernym")

    // Create the 'greeter' actor
    val monitor = system.actorOf(Props[ChannelMonitor], "ChannelMonitor")

    // Create an "actor-in-a-box"
    val inbox = Inbox.create(system)

    val newChannel = Channel(UUID.randomUUID(),"Channel1",None)
    monitor.tell(newChannel, ActorRef.noSender)
    //Try to follow this channel...
    monitor.tell(Following(newChannel.id,UUID.randomUUID()), ActorRef.noSender)

}
