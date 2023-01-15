package com.example.bykeandroid.data

import android.app.Activity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import com.example.bykeandroid.R

@kotlinx.serialization.Serializable
class Path(
    val id: Int?,
    val name: String?,
    val creator: User?,
    val pathsteps: List<PathStep>?,
    var distance: Double = 0.0,
    var polylines : List<String>? = null,
)

class PathListAdapter(
    private val context: Activity,
    private val list: List<Path>,
    private val onClickListener: ((Int) -> OnClickListener?)? = null,
) : CustomListAdapter<Path>(list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.layoutInflater
        val path = list[position]
        var view = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.paths, null, true)
            view.findViewById<TextView>(R.id.path_name).text = path.name

            view.findViewById<TextView>(R.id.path_creator).text = path.creator?.username
            view.findViewById<TextView>(R.id.path_length).text = "${path.distance} km"
            view.setOnClickListener(onClickListener?.invoke(position))
        }

        return view!!
    }
}