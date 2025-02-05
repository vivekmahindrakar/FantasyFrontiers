package de.coasterfreak.fantasyfrontiers.data.db.discord

import de.coasterfreak.fantasyfrontiers.data.model.discord.ServerSettings
import de.coasterfreak.fantasyfrontiers.data.model.discord.SystemAnnouncement
import de.coasterfreak.fantasyfrontiers.data.model.discord.SystemAnnouncementType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Represents a table for storing server settings in a database.
 *
 * @property guildID The column representing the guild ID.
 * @property language The column representing the language code.
 */
object ServerSettingsTable : Table("server_settings") {

    val guildID = varchar("guild_id", 24)
    val language = varchar("language", 5).default("en-US")

    val systemAnnouncementType = enumeration("system_announcement_type", SystemAnnouncementType::class).default(SystemAnnouncementType.NONE)
    val announcementRoomChannelId = varchar("announcement_room_channel_id", 24).nullable()

    override val primaryKey = PrimaryKey(guildID)
}

/**
 * Loads all server settings from the database.
 *
 * @return A list of [ServerSettings] representing the settings for all servers.
 */
fun loadAllServerSettings(): List<ServerSettings> = transaction {
    return@transaction ServerSettingsTable.selectAll().map { row ->
        ServerSettings(
            guildID = row[ServerSettingsTable.guildID],
            language = row[ServerSettingsTable.language],
            systemAnnouncement = SystemAnnouncement(
                systemAnnouncementType = row[ServerSettingsTable.systemAnnouncementType],
                announcementRoomChannelId = row[ServerSettingsTable.announcementRoomChannelId]
            ),
            guildRoles = getGuildRoles(row[ServerSettingsTable.guildID])
        )
    }
}

/**
 * Loads the server settings for a given guild ID.
 *
 * @param guildID The ID of the guild for which to load the settings.
 * @return The server settings for the specified guild ID. If no settings are found, a default ServerSettings object
 *         with the specified guild ID is returned.
 */
fun loadServerSettings(guildID: String): ServerSettings = transaction {
    return@transaction ServerSettingsTable.select { ServerSettingsTable.guildID eq guildID }.map { row ->
        ServerSettings(
            guildID = row[ServerSettingsTable.guildID],
            language = row[ServerSettingsTable.language],
            systemAnnouncement = SystemAnnouncement(
                systemAnnouncementType = row[ServerSettingsTable.systemAnnouncementType],
                announcementRoomChannelId = row[ServerSettingsTable.announcementRoomChannelId]
            ),
            guildRoles = getGuildRoles(guildID)
        )
    }.firstOrNull() ?: ServerSettings(guildID = guildID)
}

/**
 * Updates the server settings with the provided values.
 *
 * @param settings The ServerSettings object containing the updated settings.
 */
fun updateServerSettings(settings: ServerSettings) = transaction {
    ServerSettingsTable.replace {
        it[guildID] = settings.guildID
        it[language] = settings.language
        it[systemAnnouncementType] = settings.systemAnnouncement.systemAnnouncementType
        it[announcementRoomChannelId] = settings.systemAnnouncement.announcementRoomChannelId
    }
    updateGuildRoles(settings.guildID, settings.guildRoles)
}