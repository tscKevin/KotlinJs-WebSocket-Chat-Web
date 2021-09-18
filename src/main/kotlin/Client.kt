import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.HTMLTableSectionElement
import org.w3c.dom.WebSocket.Companion.CLOSED
import org.w3c.dom.WebSocket.Companion.OPEN


external fun alert(msg: String)

fun main() {
    val userNameInput = document.getElementById("userName") as HTMLInputElement
    val connectButton = document.getElementById("connectButton") as HTMLButtonElement
    val msgInput = document.getElementById("message") as HTMLInputElement
    val sendButton = document.getElementById("sendButton") as HTMLButtonElement
    val chatView = document.getElementById("chatTableBody") as HTMLTableSectionElement
    val onlineUserList = document.getElementById("onlineList") as HTMLLabelElement

    val webSocket = MyWebSocket(chatView, userNameInput, connectButton, onlineUserList)
    connectButton.addEventListener("click", {
        when (webSocket.connectState) {
            OPEN -> webSocket.close()
            CLOSED -> {
                if (userNameInput.value != "") {
                    webSocket.connect("ws://127.0.0.1:8088/chats", userNameInput.value)
                } else {
                    alert("Please enter user name")
                    userNameInput.focus()
                }
            }
        }
    })

    sendButton.addEventListener("click", {
        when (webSocket.connectState) {
            OPEN -> {
                if (msgInput.value != "") {
                    webSocket.send(msgInput.value)
                    msgInput.value = ""
                }
                msgInput.focus()
            }
            CLOSED -> {
                alert("Please enter user name")
                userNameInput.focus()
            }
        }
    })

    document.onkeydown = {
        if (it.keyCode == 13) {
            sendButton.click()
        }
    }
}
