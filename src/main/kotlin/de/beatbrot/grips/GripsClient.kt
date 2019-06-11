package de.beatbrot.grips

import de.beatbrot.grips.model.*
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*
import java.util.stream.Collectors

private const val baseUrl = "https://elearning.uni-regensburg.de"

class GripsClient private constructor(override val sessionId: String) : IGripsClient {
    private val cookies: Map<String, String> = mapOf("MoodleSession" to sessionId)

    override fun loadCourses(): Set<Course> {
        val request = request("$baseUrl/my/")

        val elements = request.body().getElementsByClass("qa-course")

        return elements.stream()
                .map { createCourseByElement(it) }
                .collect(Collectors.toSet())
    }

    override fun loadCourseContent(course: Course) = loadCourseContent(course.id)

    override fun loadCourseContent(courseId: Int): List<Topic> {
        val request = request("$baseUrl/course/view.php?id=$courseId")

        val topics: MutableList<Topic> = ArrayList()
        for (element in request.select("li.section.main")) {
            val id = element.id().substringAfter("section-")
            val name = element.attr("aria-label")
            val desc = element.select("div.summary")[0].text()
            val activities = extractActivities(element)

            topics.add(Topic(id.toInt(), name, desc, activities))
        }

        return topics
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

        return Course(id, link.text())
    }

    private fun request(url: String): Document {
        return Jsoup.connect(url)
                .cookies(cookies)
                .get()
    }


    private fun extractActivities(element: Element): List<Activity> {
        val rawActivities = element.select("li.activity.resource, li.activity.url")

        val result: MutableList<Activity> = ArrayList()
        for (rawActivity in rawActivities) {
            val id: Int = rawActivity.id().substringAfter("module-").toInt()
            val a = rawActivity.select("a").first()
            val name = a.getElementsByClass("instancename").first().ownText()

            try {
                val newActivity = createActivity(id, name, rawActivity.classNames())
                result.add(newActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }

    private fun createActivity(id: Int, name: String, classes: Collection<String>): Activity {
        return when {
            "resource" in classes -> DownloadActivity(id, name, resolveDownloadActivityLink(id))
            "url" in classes -> UrlActivity(id, name, resolveUrlActivityLink(id))
            else -> BaseActivity(id, name)
        }
    }

    private fun resolveUrlActivityLink(id: Int): String {
        val link = "$baseUrl/mod/url/view.php?id=$id"
        val request = request(link)

        return if (request.location() == link) {
            val linkObj = request.select(".urlworkaround > a")
            linkObj[0].attr("href")
        } else {
            request.location()
        }
    }

    private fun resolveDownloadActivityLink(id: Int): String {
        val link = "$baseUrl/mod/resource/view.php?id=$id"
        val response = Jsoup.connect(link)
                .followRedirects(false)
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute()

        return response.header("Location")
    }

    companion object {
        fun create(loginData: LoginData): IGripsClient {
            val sessionId = requestSessionId(loginData)
            return GripsClient(sessionId)
        }

        private fun requestSessionId(loginData: LoginData): String {
            val loginRequest = Jsoup.connect("$baseUrl/login/index.php")
                    .data(
                            mapOf(
                                    "username" to loginData.username,
                                    "password" to loginData.password,
                                    "realm" to loginData.realm.token
                            )
                    )
                    .method(Connection.Method.POST)
                    .execute()

            val cookies = loginRequest.cookies()

            val title = loginRequest.parse().title()

            if (title.endsWith("anmelden")) {
                throw IllegalArgumentException("Login not possible: Invalid credentials")
            } else {
                return cookies["MoodleSession"] ?: throw RuntimeException("Login failed for unknown reasons")
            }
        }
    }
}