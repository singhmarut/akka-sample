import middleware.DataStore
import org.scalatest.{Matchers, FunSuite}

/**
 * Created by wizni on 1/28/15.
 */
class DataStoreSpec  extends FunSuite   with Matchers{

    test("Channel is created") {
        val channel = DataStore.createChannel("SMS Channel")
        val isEmpty = DataStore.Channels.find(_.id.equals(channel.get.id)).isEmpty
        assert (!isEmpty)
    }
}
