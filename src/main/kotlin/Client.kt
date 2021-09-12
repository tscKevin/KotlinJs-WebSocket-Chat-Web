import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.*

external fun alert(msg: String)

fun main() {
    val userNameInput = document.getElementById("userName") as HTMLInputElement
    val connectButton = document.getElementById("connectButton") as HTMLButtonElement
    val msgInput = document.getElementById("message") as HTMLInputElement
    val sendButton = document.getElementById("sendButton") as HTMLButtonElement
    val chatList = document.getElementById("chatTableBody") as HTMLTableSectionElement

    val wsocket = MyWebSocket(chatList)

    connectButton.addEventListener("click", {
        when (wsocket.getState()) {
            WebSocket.CLOSED -> {
                if (userNameInput.value != "") {
                    wsocket.connect("ws://127.0.0.1:8088/chat", userNameInput.value)
                    GlobalScope.launch {
                        while (wsocket.getState() == WebSocket.CONNECTING) delay(50)
                        if (wsocket.getState() == WebSocket.OPEN) {
                            document.getElementById("connectButton")!!.innerHTML = "Disconnect"
                            userNameInput.disabled = true
                        }
                    }
                } else {
                    alert("Please enter your name")
                }
            }
            WebSocket.OPEN -> {
                wsocket.disconnect()
                document.getElementById("connectButton")!!.innerHTML = "Connect"
                userNameInput.disabled = false
            }
        }
    })
    sendButton.autofocus = true
    sendButton.addEventListener("click", {
        if (msgInput.value != "") {
            when (wsocket.getState()) {
                WebSocket.OPEN -> {
                    wsocket.sendMsg(msgInput.value)
                    msgInput.value = ""
                    msgInput.focus()
                }
                WebSocket.CLOSED -> {
                    alert("Please connect websocket")
                    userNameInput.focus()
                }
            }
        }
    })
}

data class ChatMessage(val userName: String = "", val msg: String = "")
class MyWebSocket(private val chatView: Element) : CreateWebSocket() {
    override fun messageEventFunction(messageEvent: MessageEvent) {
        val msg = messageEvent.data
        println("receive $msg")
        val json = JSON.parse<ChatMessage>("$msg")
        val tr = document.createElement("tr")
        tr.innerHTML = "<td>${json.userName}</td><td colspan=4>${json.msg}</td>"
        chatView.appendChild(tr)
    }
}

open class CreateWebSocket() {
    lateinit var ws: WebSocket
    lateinit var userName: String
    fun connect(url: String, userName: String): Short {
        ws = WebSocket(url)
        ws.onopen = {
            println("web socket is connect")
            userName.also { this.userName = it }
        }
        ws.onmessage = {
            messageEventFunction(it)
        }
        ws.onerror = {
            println("Error, socket will close")
            disconnect()
        }
        ws.onclose = {
            println("Close")
        }
        return ws.readyState
    }

    open fun messageEventFunction(messageEvent: MessageEvent) {
        // TODO: processing after receiving websocket message
        println(messageEvent.data)
    }

    fun disconnect() {
        if (getState() == WebSocket.OPEN) ws.close()
    }

    fun sendMsg(msg: Any?) {
        println("Send {\"userName\":\"$userName\",\"msg\":\"${msg.toString()}\"}")
        ws.send("{\"userName\":\"$userName\",\"msg\":\"${msg as String}\"}")
    }

    fun getState() = try {
        ws.readyState
    } catch (e: UninitializedPropertyAccessException) {
        WebSocket.CLOSED
    }
}
