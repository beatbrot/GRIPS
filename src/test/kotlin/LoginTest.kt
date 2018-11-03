import de.beatbrot.grips.GripsClient
import de.beatbrot.grips.model.LoginData
import de.beatbrot.grips.model.Realm
import io.kotlintest.*
import io.kotlintest.extensions.SpecLevelExtension
import io.kotlintest.extensions.TagExtension
import io.kotlintest.extensions.TestListener
import io.kotlintest.specs.StringSpec
import java.lang.IllegalArgumentException

class LoginTest : StringSpec({

    val realm = System.getenv("GRIPS_REALM")
    val username = System.getenv("GRIPS_USER")
    val password = System.getenv("GRIPS_PW")

    val fooBarLogin = LoginData(Realm.HS, "foo", "bar")

    "Logging in with wrong credentials should fail" {
        val client = GripsClient(fooBarLogin)
        shouldThrow<IllegalArgumentException> {
            client.login()
        }
    }

    "Using GripsClient-object without logging in should fail" {
        val client = GripsClient(fooBarLogin)
        shouldThrowAny {
            client.createModel()
        }
    }

    "Logging in with the real username should succeed".config(enabled = hasLoginData()) {
        println(System.getenv())
        realm shouldNotBe null
        username shouldNotBe null
        password shouldNotBe null

        val realObj = Realm.valueOf(realm.toUpperCase())
        val loginData = LoginData(realObj, username, password)

        val client = GripsClient(loginData)
        client.login()
    }
})

fun hasLoginData(): Boolean {
    val env = System.getenv()
    val realm = env["GRIPS_REALM"]
    val user = env["GRIPS_USER"]
    val pw = env["GRIPS_PW"]

    return !(realm == null || user == null || pw == null)
}
