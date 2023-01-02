package com.example.bykeandroid.view

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bykeandroid.R
import com.example.bykeandroid.databinding.FragmentLoginBinding
import com.example.bykeandroid.viewmodel.LoginViewModel

class LoginFragment: Fragment() {
    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by viewModels()
    private val args : LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        val message = args.message
        if (message.isNullOrEmpty() == false) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        // Required fields
        binding.etUsername.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                binding.etUsername.error = getString(R.string.username_missing)
            }
        }
        binding.etPwd.addTextChangedListener {
            if (it.isNullOrEmpty()) {
                binding.etPwd.error = getString(R.string.password_missing)
            }
        }

        binding.btnLogin.setOnClickListener {
            var canLogIn = true
            if (binding.etUsername.text.isNullOrEmpty()) {
                binding.etUsername.error = getString(R.string.username_missing)
                canLogIn = false
            }
            if (binding.etPwd.text.isNullOrEmpty()) {
                binding.etPwd.error = getString(R.string.password_missing)
                canLogIn = false
            }
            if (canLogIn) {
                viewModel.connect(binding.etUsername.text.toString(), binding.etPwd.text.toString()) { res ->
                    if (res == null) {
                        Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG).show()
                        return@connect
                    }
                    if (res.isSuccessful) {
                        LoginFragmentDirections.loginToScan().also {
                            findNavController().navigate(it)
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnSignup.setOnClickListener {
            LoginFragmentDirections.loginToSignUp(binding.etUsername.text.toString()).also {
                findNavController().navigate(it)
            }
        }

        binding.btViewPassword.setOnClickListener {
            binding.etPwd.transformationMethod = if (binding.etPwd.transformationMethod == null) {
                PasswordTransformationMethod()
            } else {
                null
            }
        }

        return binding.root
    }
}