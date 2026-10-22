package itmo.soa.grammy.api

import itmo.soa.grammy.model.MusicGenre
import itmo.soa.grammy.service.ApiException
import itmo.soa.grammy.service.GrammyService
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject

@RequestScoped
@Path("/grammy/bands")
@Produces(MediaType.APPLICATION_JSON)
class GrammyResource @Inject constructor(private val service: GrammyService) {
    @POST
    @Path("/{band-id}/singles/add")
    fun addSingle(@PathParam("band-id") bandIdValue: String, @QueryParam("single-name") singleName: String?): Response {
        val bandId = parseBandId(bandIdValue)
        if (singleName.isNullOrBlank()) invalid("Query parameter single-name must not be blank")
        return Response.status(Response.Status.CREATED).entity(service.addSingle(bandId, singleName.trim())).build()
    }

    @POST
    @Path("/{band-id}/nominate/{genre}")
    fun nominate(@PathParam("band-id") bandIdValue: String, @PathParam("genre") genreValue: String): Response {
        val bandId = parseBandId(bandIdValue)
        val genre = try { MusicGenre.valueOf(genreValue) } catch (_: IllegalArgumentException) {
            invalid("Path parameter genre is invalid")
        }
        return Response.status(Response.Status.CREATED).entity(service.nominate(bandId, genre)).build()
    }

    private fun parseBandId(value: String): Long = value.toLongOrNull()?.takeIf { it > 0 }
        ?: invalid("Path parameter band-id must be an integer greater than 0")

    private fun invalid(message: String): Nothing = throw ApiException(400, "INVALID_REQUEST_PARAMETER", message)
}
