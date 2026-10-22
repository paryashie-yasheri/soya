package itmo.soa.bands.dto.response

import java.time.ZonedDateTime

data class ErrorResponse(
    val timestamp: ZonedDateTime,
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String,
)
