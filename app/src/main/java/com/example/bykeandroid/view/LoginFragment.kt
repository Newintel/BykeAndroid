package com.example.bykeandroid.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bykeandroid.R
import com.example.bykeandroid.databinding.FragmentLoginBinding
import com.example.bykeandroid.viewmodel.LoginViewModel

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by viewModels()
    private val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        val activity = activity as MainActivity
        val sharedPrefUser = activity.getPreferences(Context.MODE_PRIVATE)


        val message = args.message
        if (message.isNullOrEmpty() == false) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        val sUsername = sharedPrefUser.getString("username", "").orEmpty()
        val sPassword = sharedPrefUser.getString("password", "").orEmpty()

        // Required fields
        with(binding.etUsername) {
            text = Editable.Factory().newEditable(sUsername)
            addTextChangedListener {
                if (it.isNullOrEmpty()) {
                    binding.etUsername.error = getString(R.string.username_missing)
                }
            }
        }

        with(binding.etPwd) {
            text = Editable.Factory().newEditable(sPassword)
            addTextChangedListener {
                if (it.isNullOrEmpty()) {
                    binding.etPwd.error = getString(R.string.password_missing)
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            var canLogIn = true
            val username = binding.etUsername.text.toString()
            val password = binding.etPwd.text.toString()
            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.username_missing)
                canLogIn = false
            }
            if (password.isEmpty()) {
                binding.etPwd.error = getString(R.string.password_missing)
                canLogIn = false
            }
            if (canLogIn) {
                viewModel.connect(
                    username,
                    password
                ) { res ->
                    if (res == null) {
                        Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG)
                            .show()
                        return@connect
                    }
                    if (res.isSuccessful) {
                        res.body()?.let {
                            with(sharedPrefUser.edit()) {
                                putString("username", username)
                                putString("password", password)
                                apply()
                            }
                        }
                        LoginFragmentDirections.loginToHome().also {
                            findNavController().navigate(it)
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.login_fail), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        if (sUsername.isNotEmpty() && sPassword.isNotEmpty()) {
            binding.btnLogin.performClick()
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