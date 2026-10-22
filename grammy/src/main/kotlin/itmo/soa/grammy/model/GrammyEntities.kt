package itmo.soa.grammy.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "grammy_singles")
open class SingleEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,
    @Column(name = "band_id", nullable = false)
    open var bandId: Long = 0,
    @Column(name = "single_name", nullable = false, length = 255)
    open var singleName: String = "",
    @Column(name = "created_at", nullable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now(),
)

@Entity
@Table(name = "grammy_nominations")
open class NominationEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null,
    @Column(name = "band_id", nullable = false)
    open var bandId: Long = 0,
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false, length = 32)
    open var genre: MusicGenre = MusicGenre.PSYCHEDELIC_ROCK,
    @Column(name = "created_at", nullable = false)
    open var createdAt: OffsetDateTime = OffsetDateTime.now(),
)
