package pl.touk.ticketbooking

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class NotFound(message: String) : RuntimeException("Not found: $message")

@ResponseStatus(code = HttpStatus.CONFLICT)
class BusinessError(message: String): RuntimeException("Business rule violation: $message")

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class BadRequest(message: String): RuntimeException("Bad request: $message")
