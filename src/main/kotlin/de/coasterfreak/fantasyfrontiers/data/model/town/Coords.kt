package de.coasterfreak.fantasyfrontiers.data.model.town

import kotlinx.serialization.Serializable

/**
 * Represents a set of coordinates (x, y) in a 2D space.
 *
 * @property x The x-coordinate.
 * @property y The y-coordinate.
 */
@Serializable
data class Coords(
    val x: Double = 0.0,
    val y: Double = 0.0
)