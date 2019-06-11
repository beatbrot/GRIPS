package de.beatbrot.grips.model

sealed class Activity {
    abstract val id: Int
    abstract val name: String

    open val link: String
        get() = "https://elearning.uni-regensburg.de/mod/resource/view.php?id=$id"
}

data class BaseActivity(override val id: Int, override val name: String) : Activity()

data class DownloadActivity(override val id: Int, override val name: String, override val link: String) : Activity()

data class UrlActivity(override val id: Int, override val name: String, override val link: String) : Activity()