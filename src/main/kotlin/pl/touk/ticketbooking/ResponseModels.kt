package pl.touk.ticketbooking

import java.math.BigDecimal
import java.time.LocalDateTime

data class ScreeningSummary (
    val title: String,
    val startTime: LocalDateTime,
    val id: Long?
)

data class ScreeningDetails (
    val id: Long?,
    val movie: Movie,
    val room: Room,
    val startTime: LocalDateTime,
    val availableSeats: List<SeatSummary>
)

data class SeatSummary (
    val row: Int,
    val number: Int,
    val id: Long?
)

data class ReservationResponse (
    val reservationId: Long,
    val totalPrice: BigDecimal,
    val expirationTime: LocalDateTime
)
