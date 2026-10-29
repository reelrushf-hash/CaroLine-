package com.example.commands

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ParsedAction {
    data class Flashlight(val enable: Boolean) : ParsedAction()
    object OpenYouTube : ParsedAction()
    data class OpenBrowser(val url: String = "https://www.google.com") : ParsedAction()
    object OpenSettings : ParsedAction()
    object OpenAlarm : ParsedAction()
    data class MakeCall(val number: String = "") : ParsedAction()
    data class DraftReply(val context: String = "") : ParsedAction()
    data class BatteryQuery(val level: Int) : ParsedAction()
    data class TimeQuery(val formattedTime: String) : ParsedAction()
    data class DateQuery(val formattedDate: String) : ParsedAction()
    data class Greeting(val greetingText: String) : ParsedAction()
    data class Unknown(val rawQuery: String) : ParsedAction()
}

data class CommandExecutionResult(
    val action: ParsedAction,
    val spokenResponse: String,
    val isHandledOffline: Boolean = true,
    val actionCode: String? = null
)

class LocalCommandParser(
    private val actionController: SystemActionController
) {

    fun parseAndExecute(rawInput: String, languagePreference: String = "en"): CommandExecutionResult {
        val input = rawInput.trim().lowercase(Locale.ROOT)

        // 1. Flashlight ON
        if (matchesAny(input,
                "torch on", "torch chalao", "torch jalao", "flashlight on",
                "turn on torch", "turn on flashlight", "light on",
                "টর্চ অন করো", "আলো জ্বালাও", "টর্চ জ্বালাও")) {
            val res = actionController.toggleFlashlight(true)
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("karo") || input.contains("jalao") ->
                    "Ji sir, flashlight on kar di gayi hai."
                languagePreference.startsWith("bn") || input.contains("করো") ->
                    "হ্যাঁ স্যার, টর্চ অন করা হয়েছে।"
                else ->
                    "Torch activated, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.Flashlight(true),
                spokenResponse = reply,
                actionCode = "FLASHLIGHT_ON"
            )
        }

        // 2. Flashlight OFF
        if (matchesAny(input,
                "torch off", "torch band", "flashlight off",
                "turn off torch", "turn off flashlight", "light off",
                "টর্চ বন্ধ করো", "আলো নেভাও", "টর্চ নেভাও")) {
            val res = actionController.toggleFlashlight(false)
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("band") ->
                    "Flashlight band kar di gayi hai, sir."
                languagePreference.startsWith("bn") || input.contains("বন্ধ") ->
                    "টর্চ বন্ধ করা হয়েছে, স্যার।"
                else ->
                    "Torch deactivated, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.Flashlight(false),
                spokenResponse = reply,
                actionCode = "FLASHLIGHT_OFF"
            )
        }

        // 3. YouTube
        if (matchesAny(input, "youtube", "open youtube", "youtube chalao", "ইউটিউব খোলো")) {
            actionController.openYouTube()
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("chalao") -> "YouTube khol raha hoon, sir."
                languagePreference.startsWith("bn") -> "ইউটিউব খোলা হচ্ছে, স্যার।"
                else -> "Opening YouTube protocols, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.OpenYouTube,
                spokenResponse = reply,
                actionCode = "YOUTUBE"
            )
        }

        // 4. Browser ("আমার ব্রাউজার খোলো" / "Open browser")
        if (matchesAny(input, "open browser", "browser kholo", "chrome kholo", "আমার ব্রাউজার খোলো", "ব্রাউজার খোলো", "google kholo")) {
            actionController.openBrowser()
            val reply = when {
                languagePreference.startsWith("bn") || input.contains("ব্রাউজার") -> "আপনার ব্রাউজার খোলা হচ্ছে, স্যার।"
                languagePreference.startsWith("hi") || input.contains("kholo") -> "Browser open kiya ja raha hai, sir."
                else -> "Initializing Web Browser interface, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.OpenBrowser(),
                spokenResponse = reply,
                actionCode = "BROWSER"
            )
        }

        // 5. Time ("Abhi kitne baje hain?" / "What time is it?")
        if (matchesAny(input, "kitne baje", "samay kya", "time kya", "what time", "current time", "কয়টা বাজে", "কটা বাজে", "সময় কত")) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("baje") || input.contains("samay") ->
                    "Sir, abhi samay hai $currentTime."
                languagePreference.startsWith("bn") || input.contains("বাজে") ->
                    "স্যার, এখন সময় $currentTime."
                else ->
                    "The current time is $currentTime, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.TimeQuery(currentTime),
                spokenResponse = reply,
                actionCode = "TIME"
            )
        }

        // 6. Date
        if (matchesAny(input, "tarikh kya", "date kya", "what is today's date", "today date", "আজকের তারিখ", "tarikh batao")) {
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("tarikh") ->
                    "Sir, aaj ki tarikh hai $currentDate."
                languagePreference.startsWith("bn") || input.contains("তারিখ") ->
                    "স্যার, আজকের তারিখ $currentDate."
                else ->
                    "Today is $currentDate, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.DateQuery(currentDate),
                spokenResponse = reply,
                actionCode = "DATE"
            )
        }

        // 7. Battery Status
        if (matchesAny(input, "battery", "charge kitna", "battery percent", "power level", "ব্যাটারি কত")) {
            val battery = actionController.getBatteryLevel()
            val reply = when {
                languagePreference.startsWith("hi") ->
                    "System power level $battery percent par hai, sir."
                languagePreference.startsWith("bn") ->
                    "ব্যাটারি লেভেল $battery শতাংশে আছে, স্যার।"
                else ->
                    "Power reserves are at $battery percent, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.BatteryQuery(battery),
                spokenResponse = reply,
                actionCode = "BATTERY"
            )
        }

        // 8. Open Settings
        if (matchesAny(input, "settings", "open settings", "wifi settings", "bluetooth settings", "সেটিংস খোলো")) {
            val panel = if (input.contains("wifi")) "wifi" else if (input.contains("bluetooth")) "bluetooth" else null
            actionController.openSettings(panel)
            val reply = when {
                languagePreference.startsWith("hi") -> "System settings panel khol raha hoon, sir."
                languagePreference.startsWith("bn") -> "সিস্টেম সেটিংস খোলা হচ্ছে, স্যার।"
                else -> "Opening system configuration panels, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.OpenSettings,
                spokenResponse = reply,
                actionCode = "SETTINGS"
            )
        }

        // 9. Alarm
        if (matchesAny(input, "alarm", "open alarm", "alarm dikhao", "alarm lagao", "অ্যালার্ম")) {
            actionController.openAlarm()
            val reply = when {
                languagePreference.startsWith("hi") -> "Alarms aur timers khol raha hoon, sir."
                languagePreference.startsWith("bn") -> "অ্যালার্ম কনসোল খোলা হচ্ছে, স্যার।"
                else -> "Accessing chrono alarms console, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.OpenAlarm,
                spokenResponse = reply,
                actionCode = "ALARM"
            )
        }

        // 10. Phone Dialer
        if (matchesAny(input, "call", "dial", "phone lagao", "call karo", "ফোন করো", "ডায়াল করো")) {
            actionController.dialPhone()
            val reply = when {
                languagePreference.startsWith("hi") -> "Comms dialer khol raha hoon, sir."
                languagePreference.startsWith("bn") -> "ফোন ডায়ালার খোলা হচ্ছে, স্যার।"
                else -> "Telecommunication dialer online, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.MakeCall(),
                spokenResponse = reply,
                actionCode = "CALL"
            )
        }

        // 11. Reply Draft ("Reply ka draft banao" / "draft a message")
        if (matchesAny(input, "reply ka draft", "draft", "message banao", "draft banao", "ড্রাফট বানাও", "মেসেজ ড্রাফট")) {
            val sampleDraft = "Hello, thank you for reaching out. I will get back to you shortly."
            actionController.draftSms("", sampleDraft)
            val reply = when {
                languagePreference.startsWith("hi") -> "Reply draft ready kar diya gaya hai, sir. Confirmation ke liye SMS app khol di hai."
                languagePreference.startsWith("bn") -> "রিপ্লাই ড্রাফট প্রস্তুত করা হয়েছে, স্যার। আপনার কনফার্মেশনের জন্য অপেক্ষা করা হচ্ছে।"
                else -> "Message draft constructed and prepared in communications module, Sir."
            }
            return CommandExecutionResult(
                action = ParsedAction.DraftReply(sampleDraft),
                spokenResponse = reply,
                actionCode = "DRAFT_REPLY"
            )
        }

        // 12. Greetings / Who are you
        if (matchesAny(input, "hello jarvis", "hi jarvis", "who are you", "tum kaun ho", "kaun ho tum", "apna parichay do", "তুমি কে", "তোমার নাম কি")) {
            val reply = when {
                languagePreference.startsWith("hi") || input.contains("tum") || input.contains("kaun") ->
                    "Namaste sir! Main CARO LINE AI hoon, aapka futuristic JARVIS personal assistant. Main offline commands, device control, aur online neural intelligence support karta hoon."
                languagePreference.startsWith("bn") || input.contains("কে") ->
                    "নমস্কার স্যার! আমি ক্যারো লাইন এআই (CARO LINE AI), আপনার ব্যক্তিগত জার্ভিস সহকারী। আমি ডিভাইস নিয়ন্ত্রণ এবং বহুভাষিক সহায়তা প্রদান করি।"
                else ->
                    "Greetings, Sir. I am CARO LINE AI, your advanced personal JARVIS system. All systems are fully operational and ready for your command."
            }
            return CommandExecutionResult(
                action = ParsedAction.Greeting(reply),
                spokenResponse = reply,
                actionCode = "GREETING"
            )
        }

        // Unknown local command
        return CommandExecutionResult(
            action = ParsedAction.Unknown(rawInput),
            spokenResponse = "",
            isHandledOffline = false
        )
    }

    private fun matchesAny(input: String, vararg phrases: String): Boolean {
        return phrases.any { phrase ->
            input.contains(phrase.lowercase(Locale.ROOT))
        }
    }
}
