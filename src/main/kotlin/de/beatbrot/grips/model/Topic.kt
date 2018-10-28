package de.beatbrot.grips.model

import de.beatbrot.grips.GripsClient
import de.beatbrot.grips.model.activity.Activity

interface ITopic{
    val id: Int
    val name: String
    val description: String
    val activities: List<Activity>
}

data class Topic(override val id: Int, override val name: String, override val description: String, override val activities: List<Activity>): ITopic
