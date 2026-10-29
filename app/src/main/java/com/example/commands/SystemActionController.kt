package com.example.commands

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import android.util.Log

class SystemActionController(private val context: Context) {

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    }

    private var isTorchOn = false

    fun toggleFlashlight(enable: Boolean? = null): Result<Boolean> {
        val cm = cameraManager ?: return Result.failure(Exception("Camera manager unavailable"))
        return try {
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val characteristics = cm.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cm.cameraIdList.firstOrNull() ?: return Result.failure(Exception("No camera with flash detected"))

            val targetState = enable ?: !isTorchOn
            cm.setTorchMode(cameraId, targetState)
            isTorchOn = targetState
            Result.success(isTorchOn)
        } catch (e: CameraAccessException) {
            Log.e("SystemActionController", "Flashlight error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("SystemActionController", "Torch failed", e)
            Result.failure(e)
        }
    }

    fun isFlashlightOn(): Boolean = isTorchOn

    fun openYouTube(): Result<String> {
        return try {
            val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appIntent)
            Result.success("Opening YouTube application")
        } catch (e: Exception) {
            // Fallback to web browser
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                Result.success("Launching YouTube in browser")
            } catch (err: Exception) {
                Result.failure(err)
            }
        }
    }

    fun openBrowser(url: String = "https://www.google.com"): Result<String> {
        return try {
            val validUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Opening web browser")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openSettings(panel: String? = null): Result<String> {
        return try {
            val action = when (panel?.lowercase()) {
                "wifi" -> Settings.ACTION_WIFI_SETTINGS
                "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
                "display" -> Settings.ACTION_DISPLAY_SETTINGS
                "sound" -> Settings.ACTION_SOUND_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Opening system settings")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun openAlarm(): Result<String> {
        return try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Displaying alarms")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun dialPhone(phoneNumber: String = ""): Result<String> {
        return try {
            val uri = if (phoneNumber.isNotBlank()) Uri.parse("tel:${phoneNumber.trim()}") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Opening phone dialer")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun draftSms(recipient: String = "", message: String = ""): Result<String> {
        return try {
            val uri = if (recipient.isNotBlank()) Uri.parse("smsto:${recipient.trim()}") else Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Result.success("Drafting SMS message with user confirmation")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            100
        }
    }

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
