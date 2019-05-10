package pl.touk.ticketbooking

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Configuration
class DatabaseInitializer {
    @Bean
    fun initializeDatabase(
        screeningRepo: ScreeningRepo,
        movieRepo: MovieRepo,
        roomRepo: RoomRepo,
        seatRepo: SeatRepo,
        reservationRepo: ReservationRepo,
        ticketRepo: TicketRepo,
        ticketTypeRepo: TicketTypeRepo
    ) = ApplicationRunner {
        val rooms = listOf(roomRepo.save(Room("Room 1")), roomRepo.save(Room("Room 2")), roomRepo.save(Room("Room 3")))

        for (room in rooms) for (row in 1..10) for (number in 1..20)
            seatRepo.save(Seat(room, row, number))

        val movies = listOf(
            movieRepo.save(Movie("Titanic", "James Cameron")),
            movieRepo.save(Movie("The Dark Knight", "Christopher Nolan")),
            movieRepo.save(Movie("The Return of the King", "Peter Jackson"))
        )

        screeningRepo.save(Screening(movies[0], rooms[0], LocalDateTime.now().plusDays(1)))
        screeningRepo.save(Screening(movies[1], rooms[0], LocalDateTime.now().plusMinutes(30)))
        screeningRepo.save(Screening(movies[1], rooms[1], LocalDateTime.now().plusDays(1)))
        screeningRepo.save(Screening(movies[2], rooms[1], LocalDateTime.now().plusMinutes(15)))
        screeningRepo.save(Screening(movies[2], rooms[2], LocalDateTime.now().plusDays(1)))
        screeningRepo.save(Screening(movies[0], rooms[2], LocalDateTime.now().plusMinutes(45)))

        ticketTypeRepo.save(TicketType("ADULT", BigDecimal("25.00")))
        ticketTypeRepo.save(TicketType("STUDENT", BigDecimal("18.00")))
        ticketTypeRepo.save(TicketType("CHILD", BigDecimal("12.50")))
    }
}
