package itmo.soa.bands.repository

import itmo.soa.bands.model.Coordinates
import itmo.soa.bands.model.Label
import itmo.soa.bands.model.MusicBand
import itmo.soa.bands.model.MusicBandPage
import itmo.soa.bands.model.MusicGenre
import itmo.soa.bands.tools.MusicBandsFilter
import itmo.soa.bands.tools.PageMetadata
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Repository
class JdbcMusicBandsRepository(
    private val jdbc: NamedParameterJdbcTemplate,
) : MusicBandsRepository {

    override fun insert(
        name: String,
        coordinates: Coordinates,
        creationDate: ZonedDateTime,
        numberOfParticipants: Int,
        genre: MusicGenre,
        label: Label?,
    ): MusicBand {
        val parameters = MapSqlParameterSource()
            .addValue("name", name)
            .addValue("coordinatesX", coordinates.x)
            .addValue("coordinatesY", coordinates.y)
            .addValue("creationDate", creationDate.toOffsetDateTime())
            .addValue("creationZoneId", creationDate.zone.id)
            .addValue("numberOfParticipants", numberOfParticipants)
            .addValue("genre", genre.name)
            .addValue("labelBands", label?.bands)
            .addValue("labelSales", label?.sales)

        val id = jdbc.queryForObject(
            """
                insert into music_bands (
                    name, coordinates_x, coordinates_y, creation_date, creation_zone_id,
                    number_of_participants, genre, label_bands, label_sales
                ) values (
                    :name, :coordinatesX, :coordinatesY, :creationDate, :creationZoneId,
                    :numberOfParticipants, :genre, :labelBands, :labelSales
                ) returning id
            """.trimIndent(),
            parameters,
            Long::class.javaObjectType,
        ) ?: error("PostgreSQL did not return the generated MusicBand id")

        return MusicBand(
            id = id,
            name = name,
            coordinates = coordinates,
            creationDate = creationDate,
            numberOfParticipants = numberOfParticipants,
            genre = genre,
            label = label,
        )
    }

    override fun update(id: Long, band: MusicBand): MusicBand {
        val updatedRows = jdbc.update(
            """
                update music_bands set
                    name = :name,
                    coordinates_x = :coordinatesX,
                    coordinates_y = :coordinatesY,
                    creation_date = :creationDate,
                    creation_zone_id = :creationZoneId,
                    number_of_participants = :numberOfParticipants,
                    genre = :genre,
                    label_bands = :labelBands,
                    label_sales = :labelSales
                where id = :id
            """.trimIndent(),
            band.toParameters().addValue("id", id),
        )

        if (updatedRows == 0) {
            throw NoSuchElementException("MusicBand with id $id was not found")
        }
        return band.copy(id = id)
    }

    override fun findById(id: Long): MusicBand? = jdbc.query(
        "select * from music_bands where id = :id",
        mapOf("id" to id),
        musicBandRowMapper,
    ).firstOrNull()

    override fun findAll(
        page: Int,
        size: Int,
        sort: String?,
        filter: MusicBandsFilter,
    ): MusicBandPage {
        require(page >= 0) { "page must not be negative" }
        require(size > 0) { "size must be positive" }

        val (whereClause, filterParameters) = filter.toSql()
        val totalElements = jdbc.queryForObject(
            "select count(*) from music_bands$whereClause",
            filterParameters,
            Long::class.javaObjectType,
        ) ?: 0L

        val parameters = MapSqlParameterSource()
            .addValues(filterParameters.values)
            .addValue("limit", size)
            .addValue("offset", Math.multiplyExact(page.toLong(), size.toLong()))
        val bands = jdbc.query(
            """
                select * from music_bands$whereClause
                order by ${sort.toOrderBy()}
                limit :limit offset :offset
            """.trimIndent(),
            parameters,
            musicBandRowMapper,
        )

        val totalPages = if (totalElements == 0L) 0 else ((totalElements - 1) / size + 1).toInt()
        return MusicBandPage(
            bands = bands,
            metadata = PageMetadata(page, size, totalElements, totalPages),
        )
    }

    override fun deleteById(id: Long): Boolean = jdbc.update(
        "delete from music_bands where id = :id",
        mapOf("id" to id),
    ) > 0

    override fun getAverageNumberOfParticipants(): Double =
        jdbc.queryForObject(
            "select avg(number_of_participants) from music_bands",
            emptyMap<String, Any>(),
            Double::class.javaObjectType,
        ) ?: throw NoSuchElementException("Cannot calculate average for an empty MusicBand collection")

    override fun countByNumberOfParticipants(value: Int): Long = jdbc.queryForObject(
        "select count(*) from music_bands where number_of_participants = :value",
        mapOf("value" to value),
        Long::class.javaObjectType,
    ) ?: 0L

    override fun findByNameContaining(substring: String): List<MusicBand> = jdbc.query(
        "select * from music_bands where position(:substring in name) > 0 order by id",
        mapOf("substring" to substring),
        musicBandRowMapper,
    )

    private fun MusicBand.toParameters() = MapSqlParameterSource()
        .addValue("name", name)
        .addValue("coordinatesX", coordinates.x)
        .addValue("coordinatesY", coordinates.y)
        .addValue("creationDate", creationDate.toOffsetDateTime())
        .addValue("creationZoneId", creationDate.zone.id)
        .addValue("numberOfParticipants", numberOfParticipants)
        .addValue("genre", genre.name)
        .addValue("labelBands", label?.bands)
        .addValue("labelSales", label?.sales)

    private fun MusicBandsFilter.toSql(): Pair<String, MapSqlParameterSource> {
        val conditions = mutableListOf<String>()
        val parameters = MapSqlParameterSource()

        fun addCondition(column: String, parameter: String, value: Any?) {
            if (value != null) {
                conditions += "$column = :$parameter"
                parameters.addValue(parameter, value)
            }
        }

        addCondition("id", "id", id)
        addCondition("name", "name", name)
        addCondition("coordinates_x", "coordinatesX", coordinatesX)
        addCondition("coordinates_y", "coordinatesY", coordinatesY)
        addCondition("creation_date", "creationDate", creationDate?.toOffsetDateTime())
        addCondition("number_of_participants", "numberOfParticipants", numberOfParticipants)
        addCondition("genre", "genre", genre?.name)
        addCondition("label_bands", "labelBands", labelBands)
        addCondition("label_sales", "labelSales", labelSales)

        val whereClause = if (conditions.isEmpty()) "" else " where ${conditions.joinToString(" and ")}" 
        return whereClause to parameters
    }

    private fun String?.toOrderBy(): String {
        if (this == null) return "id asc"
        val match = sortPattern.matchEntire(this)
            ?: throw IllegalArgumentException("sort must have the form field,asc or field,desc")
        val column = sortColumns[match.groupValues[1]]
            ?: throw IllegalArgumentException("Unsupported sort field: ${match.groupValues[1]}")
        return "$column ${match.groupValues[2]}, id asc"
    }

    private companion object {
        val sortPattern = Regex("([A-Za-z][A-Za-z0-9]*),(asc|desc)")
        val sortColumns = mapOf(
            "id" to "id",
            "name" to "name",
            "coordinatesX" to "coordinates_x",
            "coordinatesY" to "coordinates_y",
            "creationDate" to "creation_date",
            "numberOfParticipants" to "number_of_participants",
            "genre" to "genre",
            "labelBands" to "label_bands",
            "labelSales" to "label_sales",
        )

        val musicBandRowMapper = RowMapper { rs, _ ->
            val creationDate = rs.getObject("creation_date", OffsetDateTime::class.java)
            val creationZone = ZoneId.of(rs.getString("creation_zone_id"))
            val labelBands = rs.getObject("label_bands", Int::class.javaObjectType)
            val labelSales = rs.getObject("label_sales", Double::class.javaObjectType)

            MusicBand(
                id = rs.getLong("id"),
                name = rs.getString("name"),
                coordinates = Coordinates(
                    x = rs.getFloat("coordinates_x"),
                    y = rs.getFloat("coordinates_y"),
                ),
                creationDate = creationDate.atZoneSameInstant(creationZone),
                numberOfParticipants = rs.getInt("number_of_participants"),
                genre = MusicGenre.valueOf(rs.getString("genre")),
                label = labelBands?.let { Label(it, labelSales) },
            )
        }
    }
}
