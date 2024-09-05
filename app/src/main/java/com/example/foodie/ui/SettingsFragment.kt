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
import com.example.foodie.R
import com.example.foodie.UserViewModel
import com.example.foodie.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        setSettingsUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appSettings = userViewModel.currentAppSettings.value

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        val alertDialogLogout =
            AlertDialog.Builder(view.context).setMessage("Are you sure you want to logout?")
                .setTitle("Logout").setPositiveButton("Logout") { _, _ ->
                    userViewModel.logout()
                }.setNegativeButton("Cancel") { _, _ ->
                }.create()

        val alertDialogDel = AlertDialog.Builder(view.context)
            .setMessage("Are you sure you want to delete your account?").setTitle("Delete Account")
            .setPositiveButton("Delete") { _, _ ->
                userViewModel.deleteUser()
            }.setNegativeButton("Cancel") { _, _ ->
            }.create()

        binding.tglBtnKmMi.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_Km -> {
                        appSettings?.distanceUnit = "Km"
                        userViewModel.saveAppSettings()
                        binding.tvDistUnit.text = appSettings?.distanceUnit
                    }

                    R.id.btn_Mi -> {
                        appSettings?.distanceUnit = "Mi"
                        userViewModel.saveAppSettings()
                        binding.tvDistUnit.text = appSettings?.distanceUnit
                    }
                }
            }
        }

        binding.btnDarkModeCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            appSettings?.nightmode = AppCompatDelegate.MODE_NIGHT_YES
            userViewModel.saveAppSettings()
            binding.btnDarkModeCheckmark.alpha = 1f
            binding.btnLightModeCheckmark.alpha = 0f
            binding.btnSystemDefaultCheckmark.alpha = 0f
        }

        binding.btnLightModeCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            appSettings?.nightmode = AppCompatDelegate.MODE_NIGHT_NO
            userViewModel.saveAppSettings()
            binding.btnDarkModeCheckmark.alpha = 0f
            binding.btnLightModeCheckmark.alpha = 1f
            binding.btnSystemDefaultCheckmark.alpha = 0f
        }

        binding.btnSystemDefaultCheckmark.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            appSettings?.nightmode = MODE_NIGHT_FOLLOW_SYSTEM
            userViewModel.saveAppSettings()
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

    private fun setSettingsUI() {
        val distanceUnit = userViewModel.currentAppSettings.value?.distanceUnit
        if (distanceUnit == "Km") {
            binding.btnKm.isChecked = true
            binding.tvDistUnit.text = distanceUnit
        } else {
            binding.btnMi.isChecked = true
            binding.tvDistUnit.text = distanceUnit
        }

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
    }
}