package itmo.soa.bands.dto.response

import itmo.soa.bands.model.Coordinates
import itmo.soa.bands.model.Label
import itmo.soa.bands.model.MusicGenre
import java.time.ZonedDateTime

data class MusicBandResponse(
    val id: Long,
    val creationDate: ZonedDateTime,
    val name: String,
    val coordinates: Coordinates,
    val numberOfParticipants: Int,
    val genre: MusicGenre,
    val label: Label? = null,
)
