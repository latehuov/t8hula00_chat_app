package chat.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class myadapter(private val mydata :ArrayList<Message>) :RecyclerView.Adapter<myadapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val myview = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_row, parent, false)
        return MyViewHolder(myview)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.message.text = mydata[position].message
        holder.author.text = "by ${mydata[position].author} on ${mydata[position].time}"
    }

    override fun getItemCount()= mydata.size

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val message : TextView = itemView.findViewById(R.id.Message)
        val author : TextView = itemView.findViewById(R.id.Author)
    }

}