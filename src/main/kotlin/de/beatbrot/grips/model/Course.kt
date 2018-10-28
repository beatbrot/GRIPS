package de.beatbrot.grips.model

import de.beatbrot.grips.GripsClient

interface ICourse {
    val id: Int
    val name: String
    val checked: Boolean
    val topics: List<ITopic>
}

data class Course(override val id: Int, override val name: String, override val checked: Boolean, override val topics: List<ITopic>) : ICourse

class GripsBackedCourse(override val id: Int, override val name: String, override val checked: Boolean, private val gripsClient: GripsClient) : ICourse {
    override val topics: List<ITopic> by lazy { gripsClient.getTopics(id) }
}
