package com.example.bykeandroid.view

import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.bykeandroid.R
import com.example.bykeandroid.data.User
import com.example.bykeandroid.databinding.FragmentSignUpBinding
import com.example.bykeandroid.viewmodel.LoginViewModel
import com.example.bykeandroid.viewmodel.SignUpViewModel
import java.text.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date


class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding

    private val viewModel: SignUpViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val args: SignUpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        binding.lifecycleOwner = this

        val activity = activity as MainActivity

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

        binding.etUsername.setText(args.username)

        binding.btnSignIn.setOnClickListener {
            SignUpFragmentDirections.signUpToLogin().also {
                findNavController().navigate(it)
            }
        }

        viewModel.datePicker.addOnPositiveButtonClickListener { date ->
            val formatter = DateFormat.getDateInstance()
            binding.etBirthday.text = formatter.format(Date(date))
        }

        with(binding.etBirthday) {
            setOnClickListener {
                viewModel.datePicker.show(childFragmentManager, "DATE_PICKER")
            }
        }

        binding.btnSignUp.setOnClickListener {
            var canLogIn = true
            val username = binding.etUsername.text.toString()
            val password = binding.etPwd.text.toString()
            val bdayText = binding.etBirthday.text.toString()

            var birthdate: String? = null

            if (username.isEmpty()) {
                binding.etUsername.error = getString(R.string.username_missing)
                canLogIn = false
            }
            if (password.isEmpty()) {
                binding.etPwd.error = getString(R.string.password_missing)
                canLogIn = false
            }

            if (bdayText.isNotEmpty()) {
                viewModel.datePicker.selection?.let {
                    if (it < System.currentTimeMillis()) {
                        birthdate = DateTimeFormatter.ISO_LOCAL_DATE.format(
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    } else {
                        Toast.makeText(activity, R.string.invalid_birthdate, Toast.LENGTH_SHORT).show()
                    }
                }
            }


            if (canLogIn) {
                val user = User(
                    username = username,
                    password = password,
                    firstname = binding.etFirstname.text.toString(),
                    lastname = binding.etLastname.text.toString(),
                    birthdate = birthdate,
                )


                viewModel.signIn(user) { res ->
                    if (res == null) {
                        Toast.makeText(context, R.string.connection_failed, Toast.LENGTH_LONG)
                            .show()
                        return@signIn
                    }
                    if (res.isSuccessful) {
                        loginViewModel.connect(username, password) { res2 ->
                            if (res2 == null) {
                                Toast.makeText(
                                    context,
                                    R.string.connection_failed,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@connect
                            }
                            val sharedPrefUser = activity.getPreferences(Context.MODE_PRIVATE)
                            with(sharedPrefUser.edit()) {
                                putString("username", username)
                                putString("password", password)
                                apply()
                            }
                            val directions = if (res2.isSuccessful)
                                SignUpFragmentDirections.signUpToHome()
                            else SignUpFragmentDirections.signUpToLogin(getString(R.string.login_fail_try_again))
                            directions.also {
                                findNavController().navigate(it)
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.sign_up_fail_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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