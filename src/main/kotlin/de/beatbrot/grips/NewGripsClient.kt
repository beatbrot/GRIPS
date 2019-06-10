package de.beatbrot.grips

import de.beatbrot.grips.model.Course
import de.beatbrot.grips.model.LoginData
import de.beatbrot.grips.model.Topic
import de.beatbrot.grips.model.activity.Activity
import de.beatbrot.grips.model.activity.BaseActivity
import de.beatbrot.grips.model.activity.DownloadActivity
import de.beatbrot.grips.model.activity.UrlActivity
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.stream.Collectors

private const val baseUrl = "https://elearning.uni-regensburg.de"

class NewGripsClient(private val loginData: LoginData) : IGrips {
    private lateinit var cookies: Map<String, String>

    /**
     * @throws IllegalArgumentException Exception is thrown if loginData is not valid
     */
    private fun init() {
        cookies = requestInitialCookies()

        val loginRequest = Jsoup.connect("$baseUrl/login/index.php")
                .cookies(cookies)
                .data(
                        mapOf(
                                "username" to loginData.username,
                                "password" to loginData.password,
                                "realm" to loginData.realm.token
                        )
                )
                .method(Connection.Method.POST)
                .execute()

        cookies = loginRequest.cookies()

        val title = loginRequest.parse().title()
        if (title.endsWith("anmelden")) {
            throw IllegalArgumentException("Login not possible: Invalid credentials")
        }
    }

    /**
     * GRIPS needs to have ANY cookies before an attempt of a login, for some reason. To do that, we just get the frontpage and fetch the cookies.
     */
    private fun requestInitialCookies(): Map<String, String> {
        val request = Jsoup.connect(baseUrl)
                .method(Connection.Method.GET)
                .execute()
        return request.cookies()
    }

    override fun loadCourses(): Set<Course> {
        val request = request("$baseUrl/my/")

        val elements = request.body().getElementsByClass("qa-course")

        return elements.stream()
                .map { createCourseByElement(it) }
                .collect(Collectors.toSet())
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
            "resource" in classes -> DownloadActivity(id, name)
            "url" in classes -> UrlActivity(id, name, resolveUrlActivityLink("https://elearning.uni-regensburg.de/mod/url/view.php?id=$id"))
            else -> BaseActivity(id, name)
        }
    }

    private fun resolveUrlActivityLink(link: String): String {
        val request = Jsoup.connect(link)
                .method(Connection.Method.GET)
                .cookies(cookies)
                .get()

        return if (request.location().equals(link)) {
            val linkObj = request.select(".urlworkaround > a")
            linkObj[0].attr("href")
        } else {
            request.location()
        }
    }

    private fun resolveDownloadActivityLink(link: String): String {
        val response = Jsoup.connect(link)
                .method(Connection.Method.GET)
                .cookies(cookies)
                .execute()

        val url = response.header("Location")
        return url
    }

    companion object {
        fun create(loginData: LoginData): IGrips {
            val result = NewGripsClient(loginData)
            result.init()
            return result
        }
    }
}