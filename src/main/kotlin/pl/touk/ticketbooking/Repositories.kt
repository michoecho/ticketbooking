package pl.touk.ticketbooking

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface ScreeningRepo : CrudRepository<Screening, Long> {
    fun findByStartTimeBetween(beg: LocalDateTime, end: LocalDateTime, page: Pageable) : Slice<Screening>
}

interface SeatRepo : CrudRepository<Seat, Long> {
    @Query("""
        SELECT t.seat
        FROM Ticket t JOIN t.reservation b
        WHERE b.screening = ?1 AND (b.paid = true OR b.expirationTime > CURRENT_TIMESTAMP)
        ORDER BY t.seat.seatRow, t.seat.seatNumber
        """)
    fun findReservedSeats(screening: Screening) : List<Seat>

    @Query("""
        SELECT s
        FROM Seat s, Screening scr
        WHERE scr = ?1 AND scr.room.id = s.room.id
        ORDER BY s.seatRow, s.seatNumber
        """)
    fun findAllSeats(screening: Screening) : List<Seat>
}

fun SeatRepo.findFreeSeats(screening: Screening) : List<Seat> =
    findAllSeats(screening).minus(findReservedSeats(screening))

interface ReservationRepo : CrudRepository<Reservation, Long>
interface RoomRepo : CrudRepository<Room, Long>
interface MovieRepo : CrudRepository<Movie, Long>
interface TicketRepo : CrudRepository<Ticket, Long>
interface TicketTypeRepo : CrudRepository<TicketType, Long>
