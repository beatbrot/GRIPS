package de.beatbrot.grips

import de.beatbrot.grips.model.Activity
import de.beatbrot.grips.model.Course
import de.beatbrot.grips.model.LoginData
import de.beatbrot.grips.model.Topic
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class GripsClient(private val loginData: LoginData) {
    private val timeout: Int = 10_000
    private var cookies: MutableMap<String, String> = Collections.emptyMap()

    /**
     * @throws IllegalArgumentException Exception is thrown if loginData is not valid
     */
     fun login() {
        cookies = requestInitialCookies()

        val loginRequest = Jsoup.connect("$baseUrl/login/index.php")
                .timeout(timeout)
                .cookies(cookies)
                .data(mapOf("username" to loginData.username, "password" to loginData.password, "realm" to loginData.realm.token))
                .method(Connection.Method.POST)
                .execute()

        cookies = loginRequest.cookies()

         val title = loginRequest.parse().title()
         if (title.endsWith("anmelden")) {
             throw IllegalArgumentException("Login not possible: Invalid credentials")
         }
    }


     fun getCourses(): Set<Course> {
        val request = request("$baseUrl/my/")

        val elements = request.body().getElementsByClass("qa-course")

        var courses: Set<Course> = HashSet()
        for (el in elements) {
            courses += createCourseByElement(el)
        }

        return courses
    }

     fun getTopics(course: Course): List<Topic> {
        val request = request("$baseUrl/course/view.php?id=${course.id}")

        var topics: List<Topic> = ArrayList()
        for (element in request.select("li.section.main")) {
            val id = element.id().substringAfter("section-")
            val name = element.attr("aria-label")
            val desc = element.select("div.summary")[0].text()
            val activities = extractActivities(element)

            topics += Topic(id.toInt(), name, desc, activities)
        }

        return topics
    }

    private fun extractActivities(element: Element): List<Activity> {
        val rawActivities = element.select("li.activity.resource, li.activity.url")

        var result: List<Activity> = ArrayList()
        for (rawActivity in rawActivities) {
            val id: Int = rawActivity.id().substringAfter("module-").toInt()
            val a = rawActivity.select("a").first()
            val name = a.getElementsByClass("instancename").first().ownText()

            result += Activity(id, name)
        }
        return result
    }

    /**
     * GRIPS needs to have ANY cookies before an attempt of a login, for some reason. To do that, we just get the frontpage and fetch the cookies.
     */
    private fun requestInitialCookies(): MutableMap<String, String> {
        val request = Jsoup.connect(baseUrl)
                .method(Connection.Method.GET)
                .execute()
        return request.cookies()
    }

    /**
     * Extracts a course from the html element.
     * We expect the HTML element to look similar to be a li-Element with a link (a) in it. The tail of the href-attribute gives us the id, while the content of the a-tag gives us the name.
     *
     * @param element A HTML element of type li, containing a link of type a
     */
    private fun createCourseByElement(element: Element): Course {
        val link = element.getElementsByTag("a")[0]
        val linkText: String = link.attr("href")
        val id = linkText.substringAfter("id=").toInt()
        return Course(id, link.text(), false)
    }

    private fun request(url: String): Document {
        return Jsoup.connect(url)
                .timeout(timeout)
                .cookies(cookies)
                .get()
    }

    companion object {
        /**
         * Beware: This URL shall not end with a slash.
         */
        private const val baseUrl= "https://elearning.uni-regensburg.de"
    }
}