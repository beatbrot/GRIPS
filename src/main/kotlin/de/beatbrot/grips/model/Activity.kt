package de.beatbrot.grips.model

data class Activity(val id: Int, val name: String) {
    val link: String = "https://elearning.uni-regensburg.de/mod/resource/view.php?id=$id"
}