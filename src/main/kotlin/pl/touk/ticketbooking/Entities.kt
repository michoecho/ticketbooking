package pl.touk.ticketbooking

import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Movie(
    var title: String,
    var director: String,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class Room(
    var name: String,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class Seat(
    @ManyToOne var room: Room,
    var seatRow: Int,
    var seatNumber: Int,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class Screening(
    @ManyToOne var movie: Movie,
    @ManyToOne var room: Room,
    var startTime: LocalDateTime,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class Reservation(
    @ManyToOne var screening: Screening,
    var firstName: String,
    var surname: String,
    var paid: Boolean,
    var expirationTime: LocalDateTime,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class Ticket(
    @ManyToOne var reservation: Reservation,
    @ManyToOne var seat: Seat,
    @ManyToOne var type: TicketType,
    @Id @GeneratedValue var id: Long? = null)

@Entity
class TicketType(
    var name: String,
    var price: BigDecimal,
    @Id @GeneratedValue var id: Long? = null)
