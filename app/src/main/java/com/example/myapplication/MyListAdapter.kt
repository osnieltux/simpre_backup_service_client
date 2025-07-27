package com.example.myapplication


import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// class MyListAdapter(private val context: Activity, private val title: Array<String>, private val description: Array<String>, private val imgid: Array<Int>)
class MyListAdapter(
    private val context: Activity,
    private val backupsBDArray: ArrayList<String>,
    private val title: ArrayList<String>,
    private val backupsDateArray: ArrayList<String>,
    private val backupsSizeArray: ArrayList<String>,

    ) : ArrayAdapter<String>(context, R.layout.custom_list, title) {

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.custom_list, null, true)
        //val imageView = rowView.findViewById(R.id.icon) as ImageView

        val bdText: TextView = rowView.findViewById(R.id.bd)
        val dateText: TextView = rowView.findViewById(R.id.date)
        val nameText = rowView.findViewById<TextView>(R.id.name)
        val sizeText = rowView.findViewById<TextView>(R.id.size)

        // imageView.setImageResource(imgid[position])
        bdText.text = backupsBDArray[position]
        dateText.text = backupsDateArray[position]
        nameText.text = title[position]
        sizeText.text = buildString {
            append(backupsSizeArray[position])
            append(" M")
        }

        return rowView
    }
}  