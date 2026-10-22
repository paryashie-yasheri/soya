package itmo.soa.grammy.api

import itmo.soa.grammy.model.ErrorResponse
import itmo.soa.grammy.service.ApiException
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriInfo
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class ApiExceptionMapper : ExceptionMapper<ApiException> {
    @Context private lateinit var uriInfo: UriInfo

    override fun toResponse(exception: ApiException): Response = Response.status(exception.status)
        .type(MediaType.APPLICATION_JSON)
        .entity(ErrorResponse(exception.status, Response.Status.fromStatusCode(exception.status)?.reasonPhrase ?: "Error", exception.code, exception.message, uriInfo.path))
        .build()
}
