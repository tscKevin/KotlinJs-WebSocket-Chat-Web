import org.w3c.dom.WebSocket
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketTest {
//    in this test, must create websocket server first.
    val post = 8088
    val path = "chat"

    @Test
    fun WebSocketTest() {
        Promise<Unit> { resolve, reject ->
            val webSocket = WebSocket("ws://127.0.0.1:$post/$path")
            assertEquals(WebSocket.OPEN, webSocket.readyState)
        }
    }
}