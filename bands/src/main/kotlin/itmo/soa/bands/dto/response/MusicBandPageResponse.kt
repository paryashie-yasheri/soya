package itmo.soa.bands.dto.response

data class MusicBandPageResponse(
    val content: List<MusicBandResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)