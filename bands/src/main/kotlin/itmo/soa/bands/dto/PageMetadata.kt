package itmo.soa.bands.dto

data class PageMetadata(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
