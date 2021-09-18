import kotlinx.html.dom.append
import kotlinx.html.js.tr
import kotlinx.html.td
import org.w3c.dom.*
import org.w3c.dom.WebSocket.Companion.CLOSED
import org.w3c.dom.WebSocket.Companion.OPEN


class MyWebSocket(
    val chatView: HTMLTableSectionElement,
    val userNameInput: HTMLInputElement,
    val connectButton: HTMLButtonElement,
    val onlineUserList: HTMLLabelElement,
) {

    var webSocket: WebSocket? = null
    var userName: String? = null
    var connectState = CLOSED

    companion object {
        data class JsonPacket(val serverCmd: ServerCmd? = null, val chatMessage: ChatMessage? = null)
        data class ServerCmd(val cmdFun: Int? = null, val cmd: Array<String>? = null, val userList: ArrayList<String>? = null)
        data class ChatMessage(val userName: String? = null, val msg: String? = null)

        //cmdFun keyCode
        val UPDATE_ONLINE_USER_LIST: Int = 1
    }

    fun connect(url: String, userName: String) {
        this.userName = userName
        webSocket = WebSocket(url)
        webSocket!!.onopen = {
            println("Already Connected to ${webSocket!!.url}")
            connectState = OPEN
            updateGUIState()
            webSocket!!.send(userName)
        }
        webSocket!!.onmessage = {
            println("Receive data: ${it.data}")
            messageEventFun(it)
        }
        webSocket!!.onerror = {
            println("Error, ${it.type}")
            webSocket!!.close()
        }
        webSocket!!.onclose = {
            println("Connect close")
            connectState = CLOSED
            updateGUIState()
        }
    }

    private fun messageEventFun(it: MessageEvent) {
        val receive = JSON.parse<JsonPacket>("${it.data}")
        if (receive.serverCmd != null) {
            when(receive.serverCmd!!.cmdFun){
                UPDATE_ONLINE_USER_LIST->{
                    var nameItem = ""
                    (receive.serverCmd!!.userList as Array<String>).forEach { nameItem += "$it, " }
                    onlineUserList.innerHTML = nameItem.dropLast(2)
                }
            }
        }
        if (receive.chatMessage != null) {
            println(receive.chatMessage!!.userName)
            println(receive.chatMessage!!.msg)
            chatView.append {
                tr {
                    td {
                        +"${receive.chatMessage!!.userName}"
                    }
                    td {
                        colSpan = "4"
                        +"${receive.chatMessage!!.msg}"
                    }
                }
            }
        }
    }

    fun updateGUIState() {
        when (connectState) {
            OPEN -> {
                userNameInput.disabled = true
                connectButton.innerHTML = "Disconnect"
            }
            CLOSED -> {
                userNameInput.disabled = false
                connectButton.innerHTML = "Connect"
            }
        }
    }

    fun send(msg: String) {
        println("Send ${JSON.stringify(JsonPacket(chatMessage = ChatMessage(userName, msg)))}")
        webSocket!!.send(JSON.stringify(JsonPacket(chatMessage = ChatMessage(userName, msg))))
    }

    fun close() {
        webSocket!!.close()
        userName = null
    }
}