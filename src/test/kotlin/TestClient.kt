import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import org.w3c.dom.Node
import kotlin.test.Test
import kotlin.test.assertEquals

class TestClient {
    @Test
    fun testSayHello() {
        val container = document.createElement("div")
        container.sayHello()
        assertEquals("Hello from JS", container.textContent)
    }

    fun Node.sayHello() {
        append {
            div {
                +"Hello from JS"
            }
        }
    }
} 