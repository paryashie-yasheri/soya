package itmo.soa.bands.service

import itmo.soa.bands.dto.request.MusicBandRequest
import itmo.soa.bands.model.MusicBand
import itmo.soa.bands.model.MusicBandPage
import itmo.soa.bands.repository.MusicBandsRepository
import itmo.soa.bands.tools.MusicBandsFilter
import itmo.soa.bands.tools.MusicBandsMapper
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class MusicBandsServiceImpl(
    private val repo: MusicBandsRepository,
    private val mapper: MusicBandsMapper
) : MusicBandsService {
    override fun createMusicBand(band: MusicBandRequest): MusicBand {
        return repo.insert(
            band.name,
            band.coordinates,
            ZonedDateTime.now(),
            band.numberOfParticipants,
            band.genre,
            band.label,
        )
    }

    override fun getMusicBands(
        page: Int,
        size: Int,
        sort: String?,
        filter: MusicBandsFilter
    ): MusicBandPage {
        return repo.findAll(
            page,
            size,
            sort,
            filter
        )

    }

    override fun getMusicBandById(id: Long): MusicBand {
        val found = repo.findById(id)
        if (found != null) {
            return found
        }
        throw NoSuchElementException("getMusicBandById: there is no music bands with id: $id")
    }

    override fun updateMusicBandById(
        id: Long,
        musicBand: MusicBandRequest
    ): MusicBand {
        val existingBand = repo.findById(id)
            ?: throw NoSuchElementException("updateMusicBandById: there is no music bands with id: $id")

        with(mapper) {
            return repo.update(id, musicBand.toModel(id, existingBand.creationDate))
        }

    }

    override fun deleteMusicBandById(id: Long) {
        if (!repo.deleteById(id)){
            throw NoSuchElementException("deleteMusicBandById: MusicBand with id $id was not found")
        }
    }

    override fun getAverageNumberOfParticipants(): Double {
        return repo.getAverageNumberOfParticipants()
    }

    override fun getCountByNumberOfParticipants(value: Int): Long {
        return repo.countByNumberOfParticipants(value)
    }

    override fun searchByName(substring: String): List<MusicBand> {
        return repo.findByNameContaining(substring)
    }
}