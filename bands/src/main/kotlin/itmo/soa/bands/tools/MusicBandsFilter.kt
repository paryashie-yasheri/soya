package itmo.soa.bands.tools

import itmo.soa.bands.model.MusicGenre
import java.time.ZonedDateTime

data class MusicBandsFilter(
    val id: Long? = null,
    val name: String? = null,
    val coordinatesX: Float? = null,
    val coordinatesY: Float? = null,
    val creationDate: ZonedDateTime? = null,
    val numberOfParticipants: Int? = null,
    val genre: MusicGenre? = null,
    val labelBands: Int? = null,
    val labelSales: Double? = null,
)
