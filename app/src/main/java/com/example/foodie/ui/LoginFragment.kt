package com.example.foodie.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LoginViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var callbackManager: CallbackManager
    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.handleFacebookAccessToken(result.accessToken, requireContext())
                }

                override fun onCancel() {
                    // Handle cancel case
                }

                override fun onError(error: FacebookException) {
                    // Handle error case
                }
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoogleSignIn.setOnClickListener {
            viewModel.handleGoogleSignIn(requireActivity())
        }

        binding.btnFacebookSignIn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                requireActivity(),
                callbackManager,
                listOf("email", "public_profile")
            )
        }

        binding.btnPhoneNumberSignIn.setOnClickListener {
            TODO("Not yet implemented")
        }

        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it != null) {
                findNavController().navigate(R.id.homeFragment)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}