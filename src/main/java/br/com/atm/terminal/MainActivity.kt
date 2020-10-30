package br.com.atm.terminal

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import br.com.atm.terminal.databinding.ActivityMainBinding
import br.com.atm.terminal.frag.FirstFragment
import br.com.atm.terminal.service.MessageEvent
import br.com.atm.terminal.service.Mqtt
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {

    private  lateinit var  build : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        build = ActivityMainBinding.inflate(layoutInflater)

        setContentView(build.root)
        setSupportActionBar(build.toolbar)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
         when (item.itemId) {
            R.id.action_settings -> {

                startService(Intent(this,Mqtt::class.java))

            }

             R.id.subs -> {

                 val messageEvent = MessageEvent()
                 messageEvent.text = "t.payload.toString()"
                 messageEvent.topic = "memory.DM.nomeDaLoja"
                 messageEvent.cls = Mqtt::class.java.name
                 messageEvent.type = 1

                 EventBus.getDefault().post(messageEvent)


             }


        }


            return true
        }
    }
