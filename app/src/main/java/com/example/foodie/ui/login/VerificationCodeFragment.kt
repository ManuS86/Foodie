package com.example.foodie.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.foodie.R
import com.example.foodie.databinding.FragmentVerificationCodeBinding
import com.example.foodie.ui.viewmodels.LocationViewModel
import com.example.foodie.ui.viewmodels.UserViewModel

class VerificationCodeFragment : Fragment() {
    private val args: VerificationCodeFragmentArgs by navArgs()
    private val userViewModel: UserViewModel by activityViewModels()
    private val locationViewModel: LocationViewModel by activityViewModels()
    private lateinit var binding: FragmentVerificationCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerificationCodeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCurrentUserObserver()

        binding.tvPhoneNumber.text = args.phoneNumber
        binding.btnNext.setOnClickListener {
            val code =
                (binding.etCode1.text.toString() + binding.etCode2.text.toString() + binding.etCode3.text.toString() + binding.etCode4.text.toString() + binding.etCode5.text.toString() + binding.etCode6.text.toString())
            userViewModel.verifyCode(code)
        }

        binding.ivBackArrow.setOnClickListener {
            findNavController().navigateUp()
        }
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
}