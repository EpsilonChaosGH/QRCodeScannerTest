package com.example.qrcodescannertest

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.qrcodescannertest.databinding.FragmentMainBinding

class FragmentMain : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::class.java)

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(), ::onGotLocationPermissionResult
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonScanner.setOnClickListener {
            openScanner()
        }

        parentFragmentManager.setFragmentResultListener(FragmentScanner.SCANNER, viewLifecycleOwner) { _, data ->
            binding.textViewContent.text = data.getString(FragmentScanner.SCANNER_KEY)
        }
    }

    private fun openScanner() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        findNavController().navigate(R.id.action_fragmentMain_to_fragmentScanner)
    }

    private fun onGotLocationPermissionResult(granted: Boolean) {
        if (granted) {
            Toast.makeText(requireContext(), R.string.permission_grated, Toast.LENGTH_SHORT).show()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                askUserForOpeningAppSettings()
            } else {
                Toast.makeText(requireContext(), R.string.permissions_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun askUserForOpeningAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireActivity().packageName, null)
        )
        if (requireActivity().packageManager.resolveActivity(
                appSettingsIntent, PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            Toast.makeText(requireContext(), getString(R.string.permission_denied_forever), Toast.LENGTH_SHORT).show()
        } else {

            val listener = DialogInterface.OnClickListener { _, _ -> }
            val listenerSettings = DialogInterface.OnClickListener { _, _ ->
                startActivity(appSettingsIntent)
            }
            val builder = AlertDialog.Builder(requireContext())
                .setPositiveButton(R.string.button_open_settings, listenerSettings)
                .setNeutralButton(R.string.button_cancel, listener)
                .create()
            builder.setView(layoutInflater.inflate(R.layout.dialog_scanner_settings, null))
            builder.show()
        }
    }
}