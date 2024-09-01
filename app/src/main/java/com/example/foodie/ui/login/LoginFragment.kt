package com.example.foodie.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LocationViewModel
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.databinding.FragmentLoginBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var callbackManager: CallbackManager
    private val locationViewModel: LocationViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        locationViewModel.handleLocationRequest(requireActivity())

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    userViewModel.handleFacebookAccessToken(result.accessToken, requireActivity())
                }

                override fun onCancel() {
                    // Handle cancel case
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoogleSignIn.setOnClickListener {
            userViewModel.handleGoogleSignIn()
        }

        binding.btnFacebookSignIn.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(
                requireActivity(), callbackManager, listOf("email", "public_profile")
            )
        }

        binding.btnPhoneNumberSignIn.setOnClickListener {
            findNavController().navigate(R.id.phoneNumberFragment)
        }

        addCurrentUserObserver()
    }

    private fun addCurrentUserObserver() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                if (locationViewModel.locationPermission.value == true) {
                    // Permissions granted, start location tracking
                    findNavController().navigate(R.id.restaurantsFragment)
                } else {
                    findNavController().navigate(R.id.locationPermissionFragment)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}