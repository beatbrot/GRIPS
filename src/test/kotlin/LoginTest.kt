import de.beatbrot.grips.model.LoginData
import de.beatbrot.grips.model.Realm
import io.kotlintest.shouldThrow
import io.kotlintest.shouldThrowAny
import io.kotlintest.specs.StringSpec

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

    return !(realm.isNullOrEmpty() || user.isNullOrEmpty() || pw.isNullOrBlank())
}
