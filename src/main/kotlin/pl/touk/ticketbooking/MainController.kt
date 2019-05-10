package pl.touk.ticketbooking

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@RestController
class MainController(
    val screeningRepo: ScreeningRepo,
    val seatRepo: SeatRepo,
    val reservationRepo: ReservationRepo,
    val ticketTypeRepo: TicketTypeRepo,
    val ticketRepo: TicketRepo
) {
    @GetMapping("/ticketTypes")
    fun getTicketTypes() = ticketTypeRepo.findAll()

    @GetMapping("/screenings")
    fun getScreenings(
        @RequestParam(name = "startsAfter", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startsAfter: LocalDateTime?,
        @RequestParam(name = "startsBefore", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        startsBefore: LocalDateTime?,
        @RequestParam(name = "page", defaultValue = "0")
        page: Int,
        @RequestParam(name = "size", defaultValue = "25")
        size: Int
    ) : List<ScreeningSummary> {
        val intervalStart = startsAfter ?: LocalDateTime.now()
        val intervalEnd = startsBefore ?: intervalStart.plusDays(7)
        val pageRequest = PageRequest.of(page, size, Sort.Direction.ASC, "movie.title", "startTime")
        return screeningRepo
            .findByStartTimeBetween(intervalStart, intervalEnd, pageRequest)
            .map { ScreeningSummary(it.movie.title, it.startTime, it.id) }
            .content
    }

    @GetMapping("/screenings/{id}")
    fun getScreening(
        @PathVariable id: Long
    ) : ScreeningDetails {
        val screening = screeningRepo.findByIdOrNull(id) ?: throw NotFound("screening $id")
        return ScreeningDetails(
            screening.id,
            screening.movie,
            screening.room,
            screening.startTime,
            seatRepo.findFreeSeats(screening).map { SeatSummary(it.seatRow, it.seatNumber, it.id) }
        )
    }

    @Transactional
    @PostMapping("/screenings/{id}/reservations")
    fun postReservation(@PathVariable id: Long, @RequestBody request: ReservationRequest) : ReservationResponse {
        val screening = screeningRepo.findByIdOrNull(id) ?: throw NotFound("screening $id")

        // Assumption #2:
        // Seats can be booked at latest 15 minutes before the screening begins.
        val expirationTime = screening.startTime.minusMinutes(15)

        // Convert the reservation request to a domain object. Tickets will be converted separately.
        var reservation = Reservation(screening, request.firstName, request.surname, false, expirationTime)

        // Assumption #2:
        // Seats can be booked at latest 15 minutes before the screening begins.
        if (reservation.expirationTime < LocalDateTime.now())
            throw BusinessError("It is too late to make reservations for this screening")

        // Business requirement #1a:
        // name and surname should each be at least three characters long, starting with a capital letter.
        // The surname could consist of two parts separated with a single dash,
        // in this case the second part should also start with a capital letter.
        //
        // Business requirement #3:
        // The system should properly handle Polish characters.
        val validName = """\p{Lu}\p{Ll}{2,}"""
        if (!reservation.firstName.matches(Regex(validName)))
            throw BusinessError("Invalid first name '${reservation.firstName}'")
        if (!reservation.surname.matches(Regex("$validName(-$validName)?")))
            throw BusinessError("Invalid surname '${reservation.surname}'")

        // Convert the ticket requests to domain objects.
        val tickets = request.tickets.map { Ticket(
            reservation,
            seatRepo.findByIdOrNull(it.seatId) ?: throw BadRequest("Seat ${it.seatId} not found"),
            ticketTypeRepo.findByIdOrNull(it.ticketType) ?: throw BadRequest("Ticket type ${it.ticketType} not found")
        ) }

        // Business requirement #1b:
        // reservation applies to at least one seat.
        if (tickets.isEmpty())
            throw BusinessError("Zero seat reservations are forbidden")

        // A reservation can only consist of seats in the same room as its screening.
        tickets.find { it.seat.room !== screening.room }?.let {
            throw BusinessError("Seat ${it.seat.id} belongs to a different room")
        }

        // There can only be one active reservation for a given seat and a given screening.
        findDuplicate(tickets.map { it.seat })?.let {
            throw BusinessError("Duplicate seat ${it.id} found in the request")
        }

        // There can only be one active reservation for a given seat and a given screening.
        val reservedSeats = seatRepo.findReservedSeats(screening)
        tickets.find { it.seat in reservedSeats }?.let {
            throw BusinessError("Seat ${it.seat.id} has already been reserved")
        }

        // Business requirement #2
        // There cannot be a single place left over in a row between two already reserved places
        findIsolatedSeat(reservedSeats + tickets.map { it.seat } )?.let {
            throw BusinessError("A single place would be left over between seats ${it.first.id} and ${it.second.id}")
        }

        reservation = reservationRepo.save(reservation)
        ticketRepo.saveAll(tickets)

        val total = tickets.fold(BigDecimal.ZERO) { acc, ticket -> acc + ticket.type.price }

        return ReservationResponse(reservation.id!!, total, reservation.expirationTime)
    }

    // Optionally returns a pair of reserved seats that violate business requirement #2
    fun findIsolatedSeat(seats: List<Seat>) : Pair<Seat, Seat>? {
        val sortedSeats = seats.sortedWith(compareBy(Seat::seatRow, Seat::seatNumber))
        var prevSeat = sortedSeats[0]
        for (seat in sortedSeats) {
            // There cannot be a single place left over in a row between two already reserved places
            if (seat.seatRow == prevSeat.seatRow && seat.seatNumber == prevSeat.seatNumber + 2)
                return Pair(prevSeat, seat)
            prevSeat = seat
        }
        return null
    }

    fun <T>findDuplicate(collection: Collection<T>) : T? {
        val set = mutableSetOf<T>()
        for (e in collection) {
            if (e in set)
                return e
            set.add(e)
        }
        return null
    }
}
