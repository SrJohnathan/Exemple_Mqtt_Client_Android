package br.com.atm.terminal.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import br.com.atm.terminal.frag.FirstFragment
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.UnsupportedEncodingException


/*
@SuppressLint("NewApi")
class Mqtt : Service() {

    private lateinit var client: Mqtt3AsyncClient

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()


        client = Mqtt3Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("3.87.232.72")
            .serverPort(1883)
            .build().toAsync()


        Log.i("LOG","onCreateMqtt")



    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        client.connect().thenAccept { it  -> Log.i("LOG",it.toString()) }

        client.publishes(MqttGlobalPublishFilter.SUBSCRIBED, Consumer { t: Mqtt3Publish ->





        })

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {


        if(event?.cls ==  Mqtt::class.java.name){

            if(event?.type == 1  ){
                client.subscribeWith().topicFilter(event.topic).qos(MqttQos.EXACTLY_ONCE).send();

            }else if(event?.type == 2  ){
                client.unsubscribeWith().topicFilter(event.topic).send();

            }else if(event?.type == 3  ){

                client.publishWith()
                    .topic(event.topic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .payload(event.text.toByteArray())
                    .send();

            }


        }




    }
}  */

class Mqtt : Service(), MqttCallback {


    companion object {

        val SUSBCRIBE = 1
        val UNSUBSCRIBE = 2
        val PUBLISH = 3
        val MESSAGE = 4
        val OK_SUSBCRIBE = 5
        val OK_UNSUBSCRIBE = 6
        val ERRO_SUSBCRIBE = 7
        val ERRO_UNSUBSCRIBE = 8
        val CONNECT = 9
    }

    private lateinit var client: MqttAndroidClient


    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this);
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val clientId = MqttClient.generateClientId()
        client = MqttAndroidClient(
            this.applicationContext, "tcp://3.87.232.72:1883",
            clientId
        )

        client.setCallback(this)


        try {
            val token = client.connect()

            token.actionCallback = object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // We are connected
                    Log.d("LOG", "onSuccess")

                    Toast.makeText(
                        this@Mqtt.applicationContext,
                        "onSuccess",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("LOG", exception.cause?.message.toString())


                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun connectionLost(cause: Throwable?) {


    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {

        val messageEvent = MessageEvent()
        messageEvent.text = message?.toString().toString()
        messageEvent.topic = topic.toString()
        messageEvent.cls = FirstFragment::class.java.name
        messageEvent.type = MESSAGE
        EventBus.getDefault().post(messageEvent)

    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {
        if (event?.cls == Mqtt::class.java.name) {

            if (event?.type == 1) {

                val qos = 0
                try {
                    val subToken = client.subscribe(event.topic, qos)
                    subToken.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            // The message was published

                            val messageEvent = MessageEvent()
                            messageEvent.text = ""
                            messageEvent.topic = event.topic
                            messageEvent.cls = FirstFragment::class.java.name
                            messageEvent.type = OK_SUSBCRIBE
                            EventBus.getDefault().post(messageEvent)


                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {

                            val messageEvent = MessageEvent()
                            messageEvent.text = exception.message.toString()
                            messageEvent.topic = event.topic
                            messageEvent.cls = FirstFragment::class.java.name
                            messageEvent.type = ERRO_SUSBCRIBE
                            EventBus.getDefault().post(messageEvent)

                        }
                    }
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
            } else if (event?.type == 2) {
                try {
                    val unsubToken: IMqttToken = client.unsubscribe(event.topic)
                    unsubToken.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {

                        }
                    }
                } catch (e: MqttException) {
                    e.printStackTrace()
                }

            } else if (event?.type == PUBLISH) {

                var encodedPayload = ByteArray(0)
                try {
                    encodedPayload = event.text.toByteArray(charset("UTF-8"))
                    val message = MqttMessage(encodedPayload)
                    client.publish(event.topic, message)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                } catch (e: MqttException) {
                    e.printStackTrace()
                }

            } else if (event?.type == CONNECT) {

                if (!client.isConnected) {

                    try {
                        val token = client.connect()

                        token.actionCallback = object : IMqttActionListener {
                            override fun onSuccess(asyncActionToken: IMqttToken) {
                                // We are connected
                                Log.d("LOG", "onSuccess")

                                Toast.makeText(
                                    this@Mqtt.applicationContext,
                                    "onSuccess",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFailure(
                                asyncActionToken: IMqttToken,
                                exception: Throwable
                            ) {
                                // Something went wrong e.g. connection timeout or firewall problems
                                Log.d("LOG", exception.cause?.message.toString())


                            }
                        }
                    } catch (e: MqttException) {
                        e.printStackTrace()
                    }
                }

            }


        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this);


    }
}