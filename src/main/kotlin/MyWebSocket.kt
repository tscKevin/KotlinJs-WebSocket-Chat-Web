import kotlinx.browser.document
import org.w3c.dom.*
import org.w3c.dom.WebSocket.Companion.CLOSED
import org.w3c.dom.WebSocket.Companion.OPEN

data class JsonPacket(val serverCmd: ServerCmd?, val chatMessage: ChatMessage?)
data class ChatMessage(val userName: String?, val msg: String?)
data class ServerCmd(val cmdFun: String?, val cmd: Array<String>?, val userList: Array<String>?)
data class userList(val userLister: ArrayList<String>?)

// basic websocket build
open class MyWebSocket() {
    lateinit var ws: WebSocket
    lateinit var userName: String

    // build and connect url server websocket
    constructor(url: String, userName: String) : this() {
        connect(url, userName)
    }

    fun connect(url: String, userName: String) {
        this.userName = userName
        ws = WebSocket(url)
        setSocketEvent()
    }

    fun disconnect() {
        if (getState() == OPEN) ws.close()
    }
    // websocket state even
    open fun setSocketEvent() {
        ws.onopen = {
            println("web socket is connect")
            userName.also { this.userName = it }
        }
        ws.onmessage = {
            messageEventFunction(it)
        }
        ws.onerror = {
            println("Error, socket connect failed")
            disconnect()
        }
        ws.onclose = {
            println("Close")
        }
    }

    open fun messageEventFunction(messageEvent: MessageEvent) {
        println("receive: ${messageEvent.data}")
    }

    open fun sendMsg(msg: String) {
        ws.send(msg)
    }

    //get connection state
    fun getState() = try {
        ws.readyState
    } catch (e: UninitializedPropertyAccessException) {
        CLOSED
    }
}

class CreateWebSocket(
    var chatView: Element,
    var userNameInput: HTMLInputElement,
    var connectButton: HTMLButtonElement,
    var onlineUserList: HTMLLabelElement,
) : MyWebSocket() {

    override fun sendMsg(msg: String) {
        super.sendMsg("{\"chatMessage\":{\"userName\":\"$userName\",\"msg\":\"$msg\"}}")
        println("Send {\"userName\":\"$userName\",\"msg\":\"$msg\"}")
    }

    override fun setSocketEvent() {
        ws.onopen = {
            ws.send(userName)
            println("web socket is connect")
            setLoginGUIState()
        }
        ws.onmessage = {
            messageEventFunction(it)
        }
        ws.onerror = {
            println("Error, socket connect failed")
            alert("Error, socket connect failed")
            disconnect()
        }
        ws.onclose = {
            println("Close")
            setLoginGUIState()
        }
    }

    override fun messageEventFunction(messageEvent: MessageEvent) {
        super.messageEventFunction(messageEvent)
        val msg = messageEvent.data
        val json = JSON.parse<JsonPacket>("$msg")
        if (json.serverCmd == null) {
            val tr = document.createElement("tr")
            tr.innerHTML = "<td>${json.chatMessage!!.userName}</td><td colspan=4>${json.chatMessage.msg}</td>"
            chatView.appendChild(tr)
        } else {
            when (json.serverCmd.cmdFun) {
                "UPDATE_USER" -> {
                    var item = ""
                    json.serverCmd.userList!!.toList().forEach { item = "$item$it, " }
                    onlineUserList.innerText = item.dropLast(2)
                }
            }
        }
    }

    fun setLoginGUIState() {
        when (getState()) {
            OPEN -> {
                connectButton.innerHTML = "Disconnect"
                userNameInput.disabled = true
            }
            CLOSED -> {
                connectButton.innerHTML = "Connect"
                userNameInput.disabled = false
            }
        }
    }
}