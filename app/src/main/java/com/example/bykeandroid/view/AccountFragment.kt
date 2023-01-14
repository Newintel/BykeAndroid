package com.example.bykeandroid.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bykeandroid.R
import com.example.bykeandroid.data.CustomListAdapter
import com.example.bykeandroid.data.ExcursionListAdapter
import com.example.bykeandroid.databinding.FragmentAccountBinding
import com.example.bykeandroid.viewmodel.UserViewModel
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        binding.lifecycleOwner = this

        val activity = activity as MainActivity
        val listView = binding.ridesList

        with(activity.bottomNavigationView) {
            isVisible = true
        }

        userViewModel.loadUser {
            binding.infoUsername.text = it.username
            binding.infoFirstname.text = it.firstname
            binding.infoLastname.text = it.lastname
            binding.infoBirthdate.text = it.birthdate?.let { bd ->
                val date = bd.toLocalDate()
                val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                formatter.format(date.toJavaLocalDate())
            }
            userViewModel.loadUserExcursions { excursions ->
                var i = 1
                val excursionText =
                    excursions.joinToString("\n", limit = 5) { e ->
                        val arrival = e.arrival?.toInstant()
                        val departure = e.id?.departure?.toInstant()
                        val departureDate = departure?.let { d ->
                            val date = d.toLocalDateTime(TimeZone.currentSystemDefault())
                            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                            formatter.format(date.toJavaLocalDateTime())
                        }
                        val time = arrival?.let { a ->
                            departure?.let { d ->
                                val duration = a - d
                                val hours = duration.inWholeHours
                                val minutes = duration.inWholeMinutes - hours * 60
                                "$hours h $minutes min"
                            }
                        }
                        "${i++}- ${e.path?.name} (${departureDate.toString()} - $time)\n"
                    }
//                binding.ridesTable.visibility = View.GONE
                val customAdapter = ExcursionListAdapter(activity, excursions)
                listView.adapter = customAdapter
                listView.visibility = View.VISIBLE
            }
        }

//        binding.ridesTable.text = getString(R.string.no_rides)

        binding.logoutBtn.setOnClickListener {
            userViewModel.logout()
            val sPassword = activity.getPreferences(Context.MODE_PRIVATE)
            with(sPassword.edit()) {
                putString("password", "")
                apply()
            }
            AccountFragmentDirections.actionToLogin().also {
                findNavController().navigate(it)
            }
            val intent = activity.intent
            activity.finish()
            startActivity(intent)

        }

        return binding.root
    }
}