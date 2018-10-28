package de.beatbrot.grips.model.activity

import de.beatbrot.grips.GripsClient

interface IActivity {
    val id: Int
    val name: String
    val link: String
}

open class Activity(override val id: Int, override val name: String) : IActivity {
    override val link: String
        get() = "https://elearning.uni-regensburg.de/mod/resource/view.php?id=$id"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Activity) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(id=$id, name='$name', link='$link')"
    }


    companion object {
        fun of(id: Int, name: String, classes: Set<String>, client: GripsClient): Activity {
            return when {
                classes.contains("resource") -> DownloadActivity(id, name)
                classes.contains("url") -> UrlActivity(
                    id,
                    name,
                    client.resolveUrlActivityLink("https://elearning.uni-regensburg.de/mod/url/view.php?id=$id")
                )
                else -> Activity(id, name)
            }
        }


    }
}

