package itmo.soa.bands.repository

import itmo.soa.bands.model.Coordinates
import itmo.soa.bands.model.Label
import itmo.soa.bands.model.MusicBand
import itmo.soa.bands.model.MusicBandPage
import itmo.soa.bands.model.MusicGenre
import itmo.soa.bands.tools.MusicBandsFilter
import java.time.ZonedDateTime

interface MusicBandsRepository {

    fun insert(
        name: String,
        coordinates: Coordinates,
        creationDate: ZonedDateTime,
        numberOfParticipants: Int,
        genre: MusicGenre,
        label: Label?,
    ): MusicBand
    fun update(id: Long, band: MusicBand): MusicBand
    fun findById(id: Long): MusicBand?

    fun findAll(
        page: Int, size: Int, sort: String?, filter: MusicBandsFilter
    ): MusicBandPage

    fun deleteById(id: Long): Boolean

    fun getAverageNumberOfParticipants(): Double

    fun countByNumberOfParticipants(value: Int): Long

    fun findByNameContaining(substring: String): List<MusicBand>
}
