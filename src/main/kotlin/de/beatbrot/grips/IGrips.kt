package de.beatbrot.grips

import de.beatbrot.grips.model.Course
import de.beatbrot.grips.model.Topic

interface IGrips {
    fun loadCourses(): Set<Course>
    fun loadCourseContent(course: Course): List<Topic>
    fun loadCourseContent(courseId: Int): List<Topic>
}