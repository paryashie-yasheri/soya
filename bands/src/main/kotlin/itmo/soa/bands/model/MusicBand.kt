package itmo.soa.bands.model
import java.time.ZonedDateTime

data class MusicBand(
    val id: Long,
    val name: String,
    val coordinates: Coordinates,
    val creationDate: ZonedDateTime,
    val numberOfParticipants: Int,
    val genre: MusicGenre,
    val label: Label?,
)
