package itmo.soa.bands.dto.request

import itmo.soa.bands.model.Coordinates
import itmo.soa.bands.model.Label
import itmo.soa.bands.model.MusicGenre

data class MusicBandRequest(
    val name: String,
    val coordinates: Coordinates,
    val numberOfParticipants: Int,
    val genre: MusicGenre,
    val label: Label? = null,
)
