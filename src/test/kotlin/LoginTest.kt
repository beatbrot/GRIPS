import de.beatbrot.grips.GripsClient
import de.beatbrot.grips.model.LoginData
import de.beatbrot.grips.model.Realm
import io.kotlintest.shouldThrow
import io.kotlintest.shouldThrowAny
import io.kotlintest.specs.StringSpec
import java.lang.IllegalArgumentException

class LoginTest : StringSpec({
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
})
