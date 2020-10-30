package br.com.atm.terminal.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.atm.terminal.databinding.ComentsBinding
import br.com.atm.terminal.service.MessageEvent

class MyAdapter() : RecyclerView.Adapter<MyAdapter.MyView>() {

    val sts = ArrayList<MessageEvent>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyView {

        var lay = LayoutInflater.from(parent.context)

        var c = ComentsBinding.inflate(lay)

        return MyView(c)
    }

    fun add(st: MessageEvent) {

        sts.add(st)
        notifyDataSetChanged()
    }



    fun clear() {

        sts.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyView, position: Int) {


        val name = sts[position].topic
        val text = sts[position].text

        if(name.contains("function")){

            val span = SpannableString("$name $text")

            span.setSpan(
                ForegroundColorSpan(Color.parseColor("#FFBB86FC")),
                0,
                name.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            span.setSpan(
                ForegroundColorSpan(Color.GREEN),
                name.length,
                (name.length ) + text.length ,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            holder.vi.text.text = span

        }else{

            val span = SpannableString("$name $text")

            span.setSpan(
                ForegroundColorSpan(Color.parseColor("#FFBB86FC")),
                0,
                name.length,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            holder.vi.text.text = span
        }





    }

    override fun getItemCount(): Int {

        return sts.size
    }

    class MyView(var vi: ComentsBinding) : RecyclerView.ViewHolder(vi.root)
}