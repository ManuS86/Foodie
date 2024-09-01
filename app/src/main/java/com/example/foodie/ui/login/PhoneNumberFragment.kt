package com.example.foodie.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.UserViewModel
import com.example.foodie.databinding.FragmentPhoneNumberBinding

class PhoneNumberFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentPhoneNumberBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneNumberBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString()
            userViewModel.sendVerificationCode(requireActivity(), phoneNumber)
            findNavController().navigate(
                PhoneNumberFragmentDirections.actionPhoneNumberFragmentToVerificationCodeFragment(
                    phoneNumber
                )
            )
        }
    }
}