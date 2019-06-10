package de.beatbrot.grips.model

import de.beatbrot.grips.model.activity.Activity

data class Topic(val id: Int, val name: String, val description: String, val activities: List<Activity>)
