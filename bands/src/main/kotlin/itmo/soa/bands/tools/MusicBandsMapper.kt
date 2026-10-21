package itmo.soa.bands.tools

import itmo.soa.bands.dto.request.MusicBandRequest
import itmo.soa.bands.dto.response.MusicBandResponse
import itmo.soa.bands.model.MusicBand
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
@Component
class MusicBandsMapper {

    fun MusicBand.toResponse() = MusicBandResponse(
        id = id,
        creationDate = creationDate,
        name = name,
        coordinates = coordinates,
        numberOfParticipants = numberOfParticipants,
        genre = genre,
        label = label,
    )

    fun MusicBandRequest.toModel(id: Long, creationDate: ZonedDateTime) = MusicBand(
        id = id,
        creationDate = creationDate,
        name = name,
        coordinates = coordinates,
        numberOfParticipants = numberOfParticipants,
        genre = genre,
        label = label,
    )
}