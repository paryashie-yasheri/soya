package itmo.soa.bands.dto.response

import itmo.soa.bands.dto.PageMetadata

data class PageResponse<T>(
    val content: List<T>,
    val metadata: PageMetadata
)
