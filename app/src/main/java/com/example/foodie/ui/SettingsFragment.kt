package com.example.foodie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.foodie.LoginViewModel
import com.example.foodie.R
import com.example.foodie.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private val loginViewModel: LoginViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nightMode = AppCompatDelegate.getDefaultNightMode()

        when (nightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> {
                binding.btnDarkModeCheckmark.alpha = 1f
            }

            AppCompatDelegate.MODE_NIGHT_NO -> {
                binding.btnLightModeCheckmark.alpha = 1f
            }

            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED, MODE_NIGHT_FOLLOW_SYSTEM -> {
                binding.btnSystemDefaultCheckmark.alpha = 1f
            }

            else -> {
            }
        }

        loginViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        val alertDialogLogout = AlertDialog.Builder(view.context)
            .setMessage("Are you sure you want to logout?")
            .setTitle("Logout")
            .setPositiveButton("Logout") { _, _ ->
                loginViewModel.logout()
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()

        val alertDialogDel = AlertDialog.Builder(view.context)
            .setMessage("Are you sure you want to delete your account?")
            .setTitle("Delete Account")
            .setPositiveButton("Delete") { _, _ ->
                loginViewModel.deleteUser()
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()

        binding.btnDarkModeCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.btnDarkModeCheckmark.alpha = 1f
            binding.btnLightModeCheckmark.alpha = 0f
            binding.btnSystemDefaultCheckmark.alpha = 0f
        }

        binding.btnLightModeCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.btnDarkModeCheckmark.alpha = 0f
            binding.btnLightModeCheckmark.alpha = 1f
            binding.btnSystemDefaultCheckmark.alpha = 0f
        }

        binding.btnSystemDefaultCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            binding.btnDarkModeCheckmark.alpha = 0f
            binding.btnLightModeCheckmark.alpha = 0f
            binding.btnSystemDefaultCheckmark.alpha = 1f
        }

        binding.cvLogOut.setOnClickListener {
            alertDialogLogout.show()
        }

        binding.cvDelAcc.setOnClickListener {
            alertDialogDel.show()
        }
    }
}