package com.example.bykeandroid.data

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.bykeandroid.R
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@kotlinx.serialization.Serializable
class ExcursionId(
    val userId: Int,
    val departure: String,
)

@kotlinx.serialization.Serializable
class Excursion(
    val id: ExcursionId?,
    val arrival: String?,
    val path: Path?,
    val user: User?,
)

class ExcursionListAdapter(
    private val context: Activity,
    private val list: List<Excursion>,
) : CustomListAdapter<Excursion>(list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.layoutInflater
        val excursion = list[position]
        var view = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.excursions, null, true)
            view.findViewById<TextView>(R.id.excursion_name).text = excursion.path?.name

            val tvDeparture = view.findViewById<TextView>(R.id.excursion_date)
            val departure = excursion.id?.departure?.toInstant()
            val arrival = excursion.arrival?.toInstant()
            val departureDate = departure?.let { d ->
                val date = d.toLocalDateTime(TimeZone.currentSystemDefault())
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                formatter.format(date.toJavaLocalDateTime())
            }
            tvDeparture.text = departureDate

            val time = arrival?.let { a ->
                departure?.let { d ->
                    val duration = a - d
                    val hours = duration.inWholeHours
                    val minutes = duration.inWholeMinutes - hours * 60
                    "$hours h $minutes min"
                }
            }
            val tvTime = view.findViewById<TextView>(R.id.excursion_duration)
            tvTime.text = time

            view.findViewById<TextView>(R.id.excursion_creator).text = excursion.user?.username
        }

        return view!!
    }
}