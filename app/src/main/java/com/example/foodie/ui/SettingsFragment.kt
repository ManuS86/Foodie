package com.example.foodie.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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

        binding.cvLogOut.setOnClickListener {
            alertDialogLogout.show()
        }

        binding.cvDelAcc.setOnClickListener {
            alertDialogDel.show()
        }

        loginViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }
}