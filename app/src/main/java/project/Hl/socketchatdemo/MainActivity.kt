package project.Hl.socketchatdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {

    val Tag = "MainActivity"
    lateinit var mSocket: Socket
    lateinit var username: String
    var users: Array<String> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            mSocket = IO.socket("http://10.0.2.2:3001")
        } catch (e: URISyntaxException) {
            Log.e("MainActivity", e.reason)
        }

        var intent = intent
        username = intent.getStringExtra("username")

        mSocket.connect()

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("newUser", onNewUser)
        mSocket.on("myMsg", onMyMessage)
        mSocket.on("newMsg", onNewMessage)
        mSocket.on("logout", onLogOut)

        send.setOnClickListener {
            val chat = editText.text.toString()
            mSocket.emit("say", chat)
        }

        logout.setOnClickListener {
            mSocket.emit("logout")
        }

    }

    //onConnect는 Emmiter.Listener를 구현한 인터페이스
    //여기서 Server에서 주는 데이터를 어떤식으로 처리할지 정하는 것

    val onConnect: Emitter.Listener = Emitter.Listener {
        mSocket.emit("login", username)
        Log.d(Tag, "Socket is connected with ${username}")
    }

    val onMyMessage = Emitter.Listener {
        Log.d("on", "Mymessage has been triggered.")
        Log.d("mymsg : ", it[0].toString())
    }

    val onNewMessage = Emitter.Listener {
        Log.d("on", "New message has been triggered.")
        Log.d("new msg : ", it[0].toString())
    }

    val onLogout = Emitter.Listener {
        Log.d("on", "Logout has been triggered.")

        try {
//             {"diconnected" : nickname,
//              "whoIsOn" : whoIsOn
//         } 이런 식으로 넘겨진 데이터를 파싱하는 방법입니다.
            val jsonObj: JSONObject = it[0] as JSONObject //it[0] 은 사실 Any 타입으로 넘어오지만 캐스팅을 해줍니다.
            Log.d("logout ", jsonObj.getString("disconnected"))
            Log.d("WHO IS ON NOW", jsonObj.getString("whoIsOn"))

            //Disconnect socket!
            mSocket.disconnect()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    val onMessageRecieved: Emitter.Listener = Emitter.Listener {
        try {
            val receivedData: Any = it[0]
            Log.d(Tag, receivedData.toString())

        } catch (e: Exception) {
            Log.e(Tag, "error", e)
        }
    }

    val onNewUser: Emitter.Listener = Emitter.Listener {

        var data = it[0] //String으로 넘어옵니다. JSONArray로 넘어오지 않도록 서버에서 코딩한 것 기억나시죠?
        if (data is String) {
            users = data.split(",").toTypedArray() //파싱해줍니다.
            for (a: String in users) {
                Log.d("user", a) //누구누구 있는지 한 번 쫘악 뽑아줘봅시다.
            }
        } else {
            Log.d("error", "Something went wrong")
        }

    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
