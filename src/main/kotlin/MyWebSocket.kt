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
        //data class JsonPacket(val serverCmd: ServerCmd? = null, val chatMessage: ChatMessage? = null)
        data class ServerCmd(val cmdCode: CmdCode, val chatMessage: ChatMessage?, val userList: ArrayList<String>?)
        data class ChatMessage(val userName: String? = null, val msg: String? = null)

        //cmdFun keyCode
        enum class CmdCode {
            CMD_UPDATE_USERS, CMD_CHAT, CND_CLOSE
        }
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
        val receive = JSON.parse<ServerCmd>("${it.data}")
        when (receive.cmdCode.toString()){
            CmdCode.CMD_UPDATE_USERS.toString()->{
                var nameItem = (receive.userList as Array<String>).joinToString()
                println(nameItem)
                onlineUserList.innerHTML = nameItem
            }
            CmdCode.CMD_CHAT.toString() -> {
                println(receive.chatMessage!!.userName)
                println(receive.chatMessage.msg)
                chatView.append {
                    tr {
                        td {
                            +"${receive.chatMessage.userName}"
                        }
                        td {
                            colSpan = "4"
                            +"${receive.chatMessage.msg}"
                        }
                    }
                }
            }
            CmdCode.CND_CLOSE.toString() -> {
                println("Close")
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
        println("Send ${JSON.stringify(ServerCmd(CmdCode.CMD_CHAT,ChatMessage(userName, msg),null))}")
        webSocket!!.send(JSON.stringify(ServerCmd(CmdCode.CMD_CHAT,ChatMessage(userName, msg),null)))
    }

    fun close() {
        webSocket!!.close()
        userName = null
    }
}