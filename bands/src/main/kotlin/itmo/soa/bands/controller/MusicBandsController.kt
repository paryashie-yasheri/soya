package itmo.soa.bands.controller

import itmo.soa.bands.dto.request.MusicBandRequest
import itmo.soa.bands.dto.response.AverageNumberOfParticipantsResponse
import itmo.soa.bands.dto.response.CountResponse
import itmo.soa.bands.dto.response.MusicBandResponse
import itmo.soa.bands.dto.response.PageResponse
import itmo.soa.bands.service.MusicBandsService
import itmo.soa.bands.tools.MusicBandsFilter
import itmo.soa.bands.tools.MusicBandsMapper
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/music-bands")
class MusicBandsController(private val musicBandsService: MusicBandsService, private val mapper: MusicBandsMapper) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMusicBand(@RequestBody musicBand: MusicBandRequest): MusicBandResponse =
        with(mapper) {
            musicBandsService.createMusicBand(musicBand).toResponse()
        }


    @GetMapping
    fun getMusicBands(
        @RequestParam(name = "page", defaultValue = "0") page: Int,
        @RequestParam(name = "size", defaultValue = "10") size: Int,
        @RequestParam(name = "sort", required = false) sort: String?,
        @ModelAttribute filter: MusicBandsFilter,
    ): PageResponse<MusicBandResponse> {
        val musicBandPage = musicBandsService.getMusicBands(page, size, sort, filter)
        val content = musicBandPage.bands.map{
            mapper.run {
                it.toResponse()
            }
        }
        return PageResponse(content, musicBandPage.metadata)
    }

    @GetMapping("/{id}")
    fun getMusicBandById(@PathVariable("id") id: Long): MusicBandResponse =
        with(mapper) {
            musicBandsService.getMusicBandById(id).toResponse()
        }


    @PutMapping("/{id}")
    fun updateMusicBandById(@PathVariable("id") id: Long, @RequestBody musicBand: MusicBandRequest): MusicBandResponse =
        with(mapper) {
            musicBandsService.updateMusicBandById(id, musicBand).toResponse()
        }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMusicBandById(@PathVariable("id") id: Long) {
        return musicBandsService.deleteMusicBandById(id)
    }

    @GetMapping("/average-number-of-participants")
    fun getAverageNumberOfParticipants(): AverageNumberOfParticipantsResponse =
        AverageNumberOfParticipantsResponse(musicBandsService.getAverageNumberOfParticipants())


    @GetMapping("/count-by-number-of-participants")
    fun getCountByNumberOfParticipants(@RequestParam(name = "value") value: Int): CountResponse =
        CountResponse(musicBandsService.getCountByNumberOfParticipants(value))


    @GetMapping("/search-by-name")
    fun searchByName(@RequestParam(name = "substring") substring: String): List<MusicBandResponse> {

        return musicBandsService.searchByName(substring).map {
            mapper.run {
                it.toResponse()
            }
        }
    }
}
