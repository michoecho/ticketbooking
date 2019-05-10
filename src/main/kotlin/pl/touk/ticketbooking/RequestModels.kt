package pl.touk.ticketbooking

data class TicketRequest (
    val seatId: Long,
    val ticketType: Long
)

data class ReservationRequest (
    val firstName: String,
    val surname: String,
    val tickets: List<TicketRequest>
)
