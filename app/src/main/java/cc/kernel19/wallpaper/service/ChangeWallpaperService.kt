package cc.kernel19.wallpaper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import cc.kernel19.wallpaper.Config
import cc.kernel19.wallpaper.R
import cc.kernel19.wallpaper.utils.FileUtils

/**
 * @author kiva
 */
class ChangeWallpaperService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && getEnabledStatus()) {
            changeWallpaper(getNextWallpaperPath())
            resetAlarm()
        }
        return START_NOT_STICKY
    }

    private fun getNextWallpaperPath(): String? {
        val preference = getSharedPreferences(Config.PREFERENCE_CURRENT, Context.MODE_PRIVATE)
        val count = preference.getInt(Config.KEY_COUNT, 0)

        var current = preference.getInt(Config.KEY_CURRENT, 0) + 1
        if (current >= count) {
            current = 0
        }

        preference.edit().putInt(Config.KEY_CURRENT, current).apply()

        val pref = getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE)
        return pref.getString(java.lang.String.valueOf(current), null)
    }

    private fun resetAlarm() {
        val freq = getFrequency()
        if (freq == 0) {
            return
        }

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + freq, getChangeAction())
    }

    private fun getChangeAction(): PendingIntent? {
        val intent = Intent(applicationContext, ChangeWallpaperService::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        val action = PendingIntent.getService(this, 0, intent, 0)
        return action
    }

    private fun getFrequency(): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val freqString = prefs.getString(getString(R.string.key_frequency), "0")
        return Integer.parseInt(freqString)
    }

    private fun getEnabledStatus(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getBoolean(getString(R.string.key_enable_auto_change), false)
    }

    private fun changeWallpaper(path: String?) {
        Log.e("WallpaperAutoChange", "next: " + path)
        if (path == null) {
            return
        }

        val wm = WallpaperManager.getInstance(applicationContext)
        val inputStream = FileUtils.openInput(path)

        if (inputStream != null) {
            wm.setStream(inputStream)
            inputStream.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}