package itmo.soa.bands.exception

import itmo.soa.bands.dto.response.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.ZonedDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.NOT_FOUND
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "MUSIC_BAND_NOT_FOUND",
            message = ex.message ?: "The requested MusicBand was not found.",
            path = request.requestURI,
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.UNPROCESSABLE_ENTITY
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "MUSIC_BAND_VALIDATION_FAILED",
            message = "One or more MusicBand fields violate the validation constraints.",
            path = request.requestURI,
        )
        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "INVALID_REQUEST_BODY",
            message = "The request body is malformed or contains an invalid value.",
            path = request.requestURI,
        )

        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(
        ex: MethodArgumentTypeMismatchException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "INVALID_REQUEST_PARAMETER",
            message = "Request parameter '${ex.name}' has an invalid value.",
            path = request.requestURI,
        )

        return ResponseEntity.status(status).body(errorResponse)
    }


    @ExceptionHandler(InvalidPaginationException::class)
    fun handleInvalidPagination(
        ex: InvalidPaginationException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "INVALID_REQUEST_PARAMETER",
            message = ex.message ?: "Pagination parameters have invalid values.",
            path = request.requestURI,
        )

        return ResponseEntity.status(status).body(errorResponse)
    }

    @ExceptionHandler(InvalidSortException::class)
    fun handleInvalidSort(
        ex: InvalidSortException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.BAD_REQUEST
        val errorResponse = ErrorResponse(
            timestamp = ZonedDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            code = "INVALID_REQUEST_PARAMETER",
            message = ex.message ?: "The sort parameter has an invalid value.",
            path = request.requestURI,
        )

        return ResponseEntity.status(status).body(errorResponse)
    }
}
