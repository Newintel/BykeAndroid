package com.example.bykeandroid.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bykeandroid.R
import com.example.bykeandroid.data.PathListAdapter
import com.example.bykeandroid.databinding.FragmentHomePageBinding
import com.example.bykeandroid.utils.round
import com.example.bykeandroid.viewmodel.HomeViewModel

class HomePageFragment : Fragment() {
    private lateinit var binding: FragmentHomePageBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = requireActivity() as MainActivity

        // Inflate the layout for this fragment
        if (activity.homePageView != null) {
            return activity.homePageView!!
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page, container, false)

        viewModel.getPaths { res ->
            if (res != null) {
                if (res.isSuccessful) {
                    res.body()?.let { paths ->
                        var i = 0
                        for (path in paths) {
                            viewModel.getLength(path) { len, lines ->
                                path.distance = ((len ?: 0.0) / 1000).round(1)
                                path.polylines = lines
                                binding.pathsList.adapter = PathListAdapter(activity, paths) {
                                    position ->
                                    View.OnClickListener {
                                        findNavController().navigate(
                                            HomePageFragmentDirections.homeToMap(
                                                paths[position].pathsteps?.toTypedArray(),
                                                paths[position].polylines?.toTypedArray()
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        activity.homePageView = binding.root
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        with(activity.bottomNavigationView) {
            isVisible = true
        }
    }
}