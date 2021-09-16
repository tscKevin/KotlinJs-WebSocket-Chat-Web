import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.label
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.Node
import kotlin.test.Test
import kotlin.test.assertEquals

class HTMLElementControlTest {
    @Test
    fun testAddNewHTMLElement() {
        val container = document.createElement("div") as HTMLDivElement
        container.sayHello()
        assertEquals("Hello, TSMC", container.textContent)

        val newLabel = document.create.label {
            id="labelId"
            +"Test"
        }
        container.appendChild(newLabel)
        assertEquals("Hello, TSMCTest", container.textContent)

        newLabel.innerHTML=""
        assertEquals("Hello, TSMC", container.textContent)
    }

    fun Node.sayHello() {
        append {
            div {
                +"Hello, TSMC"
            }
        }
    }
} 