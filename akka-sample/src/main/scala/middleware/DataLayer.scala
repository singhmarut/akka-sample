    package middleware

import java.util.UUID

/**
 * Created by wizni on 1/27/15.
 */
//class BusinessLayer {
//
//}

case class Channel(id: UUID, name: String, var phoneNumber: Option[String])
case class User(id: UUID, name: String)
case class Following(channelId: UUID, UserId: UUID)
case class PhoneNumber(number: String)
