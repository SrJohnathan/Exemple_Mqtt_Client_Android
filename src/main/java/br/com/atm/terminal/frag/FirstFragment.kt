package br.com.atm.terminal.frag

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.atm.terminal.adapter.MyAdapter
import br.com.atm.terminal.databinding.FragmentFirstBinding
import br.com.atm.terminal.impl.ArrayAdapterBinding
import br.com.atm.terminal.impl.DataChart
import br.com.atm.terminal.impl.Fn
import br.com.atm.terminal.service.MessageEvent
import br.com.atm.terminal.service.Mqtt
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import lecho.lib.hellocharts.util.ChartUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private lateinit var adapter: MyAdapter
    private var arraySpinner: ArrayAdapterBinding<String>? = null

    val canais = arrayOf("memory", "log", "process", "function")
    val servico = arrayOf("WP,DM")


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        adapter = MyAdapter()


        _binding = FragmentFirstBinding.inflate(inflater, container, false)


        // icones das plataformas
        binding.imagewp.setImageDrawable(IconicsDrawable(requireActivity()).apply {
            icon = CommunityMaterial.Icon3.cmd_whatsapp
            colorInt = Color.GREEN
            sizeDp = 50
        })
        binding.imageit.setImageDrawable(IconicsDrawable(requireActivity()).apply {
            icon = CommunityMaterial.Icon2.cmd_instagram
            colorInt = Color.parseColor("#FFBB86FC")
            sizeDp = 50
        })
        binding.imagefb.setImageDrawable(IconicsDrawable(requireActivity()).apply {
            icon = CommunityMaterial.Icon2.cmd_facebook_messenger
            colorInt = Color.BLUE
            sizeDp = 50
        })
        binding.imagetl.setImageDrawable(IconicsDrawable(requireActivity()).apply {
            icon = CommunityMaterial.Icon3.cmd_telegram
            colorInt = Color.LTGRAY
            sizeDp = 50
        })


        // array do spinner
        arraySpinner = ArrayAdapterBinding<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item
        )



        arraySpinner?.setItems(
            requireActivity(), *arrayOf(
                "nomeDaLoja",
                "DeliveryBR",
                "ClinicaSobral"
            )
        )


        //RECYCLEYVIEW
        binding.spinner.adapter = arraySpinner
        binding.console.adapter = this.adapter
        binding.console.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.VERTICAL,
            false
        )





        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*   view.findViewById<Button>(R.id.button_first).setOnClickListener {
               findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
           } */
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);


        val messageEvent = MessageEvent()
        messageEvent.text = ""
        messageEvent.topic = ""
        messageEvent.cls = Mqtt::class.java.name
        messageEvent.type = Mqtt.CONNECT
        EventBus.getDefault().post(messageEvent)


        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {


                servico.forEach { s: String ->

                    canais.forEach { ss: String ->
                        val messageEvent = MessageEvent()
                        messageEvent.text = ""
                        messageEvent.topic = "${ss}/${s}/${arraySpinner?.getItem(position)}"
                        messageEvent.cls = Mqtt::class.java.name
                        messageEvent.type = Mqtt.SUSBCRIBE
                        EventBus.getDefault().post(messageEvent)


                        println("${ss}/${s}/${arraySpinner?.getItem(position)}")

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    //  EventBus - Callback dispara quando se envia algo para
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent?) {

        if (event?.cls == FirstFragment::class.java.name) {

            if (event?.type == Mqtt.MESSAGE) {


                this.adapter.add(event)
                binding.console.smoothScrollToPosition(adapter.itemCount);



                if (event.topic.contains("function")) {

                    val value = Gson().fromJson(event.text, Fn::class.java)

                    if (value.value) {
                        Snackbar.make(
                            binding.root,
                            "Serviço iniciado com sucesso",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            binding.root,
                            "Serviço parado com sucesso",
                            Snackbar.LENGTH_LONG
                        ).show()

                    }


                } else {

                    binding.chartit.pieChartData = generateData(event.text)
                    binding.chartwp.pieChartData = generateData(event.text)

                }


            }

            if (event?.type == Mqtt.OK_SUSBCRIBE) {
                Toast.makeText(
                    requireActivity(),
                    "Canal ${event?.topic} inscrito",
                    Toast.LENGTH_SHORT
                ).show()

                clickStopServer()
                clickRestartServer()

            }

            if (event?.type == Mqtt.ERRO_SUSBCRIBE) {
                Toast.makeText(
                    requireActivity(),
                    "Canal ${event?.topic} : ${event.text}",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }

    }

    private fun generateData(json: String): PieChartData? {

        val value = Gson().fromJson(json, DataChart::class.java)

        println(value)

        value.used = (Math.round(value.used.toDouble() * 100) / 100).toString()
        value.total = (Math.round(value.total.toDouble() * 100) / 100).toString()
        value.external = (Math.round(value.external.toDouble() * 100) / 100).toString()

        val values: MutableList<SliceValue> = ArrayList()
        val sliceValue1 = SliceValue(value.used.toFloat(), ChartUtils.pickColor())
        val sliceValue2 = SliceValue(value.total.toFloat(), ChartUtils.pickColor())
        val sliceValue3 = SliceValue(value.external.toFloat(), ChartUtils.pickColor())

        println(value)

        values.add(sliceValue1)
        values.add(sliceValue2)
        values.add(sliceValue3)

        var data = PieChartData(values)
        data?.setHasLabels(true)
        data?.setHasLabelsOnlyForSelected(false)
        data?.setHasLabelsOutside(false)
        data?.setHasCenterCircle(true)
        if (true) {
            data?.slicesSpacing = 5
        }
        if (true) {
            // data?.centerText1 = "Re"

        }

        return data
    }

    private fun clickStopServer() {

        binding.buttonwpstop.setOnClickListener {

            val messageEvent = MessageEvent()
            messageEvent.text = "1"
            messageEvent.topic = "function/DM/${binding.spinner.selectedItem}"
            messageEvent.cls = Mqtt::class.java.name
            messageEvent.type = Mqtt.PUBLISH
            EventBus.getDefault().post(messageEvent)

        }

        binding.buttonitstop.setOnClickListener {

            val messageEvent = MessageEvent()
            messageEvent.text = "1"
            messageEvent.topic = "function/DM/${binding.spinner.selectedItem}"
            messageEvent.cls = Mqtt::class.java.name
            messageEvent.type = Mqtt.PUBLISH
            EventBus.getDefault().post(messageEvent)

        }

    }

    private fun clickRestartServer() {

        binding.buttonwprelold.setOnClickListener {

            val messageEvent = MessageEvent()
            messageEvent.text = "2"
            messageEvent.topic = "function/DM/${binding.spinner.selectedItem}"
            messageEvent.cls = Mqtt::class.java.name
            messageEvent.type = Mqtt.PUBLISH
            EventBus.getDefault().post(messageEvent)

        }

        binding.buttonitrelold.setOnClickListener {

            val messageEvent = MessageEvent()
            messageEvent.text = "2"
            messageEvent.topic = "function/DM/${binding.spinner.selectedItem}"
            messageEvent.cls = Mqtt::class.java.name
            messageEvent.type = Mqtt.PUBLISH
            EventBus.getDefault().post(messageEvent)

        }

    }
}