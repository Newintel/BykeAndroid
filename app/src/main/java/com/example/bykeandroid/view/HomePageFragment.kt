package com.example.bykeandroid.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.bykeandroid.R
import com.example.bykeandroid.databinding.FragmentHomePageBinding
import com.example.bykeandroid.databinding.FragmentLoginBinding
import com.example.bykeandroid.utils.MyDialog

class HomePageFragment : Fragment() {
    private lateinit var binding: FragmentHomePageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_home_page, container, false)
        val activity = activity as MainActivity

        binding.scan.text = "Start scanning"
        binding.scan.setOnClickListener {
            activity.startBleScan()
        }

        return binding.root
    }
}