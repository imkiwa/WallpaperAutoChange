package cc.kernel19.wallpaper.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.app.job.JobParameters
import android.app.job.JobService
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
class ChangeWallpaperJobService : JobService() {

    /**
     * 当收到取消请求时，该方法是系统用来取消挂起的任务的。
     * 如果onStartJob()返回false，则系统会假设没有当前运行的任务，故不会调用该方法。
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    /**
     * false: 该系统假设任何任务运行不需要很长时间并且到方法返回时已经完成。
     * true: 该系统假设任务是需要一些时间并且当任务完成时需要调用jobFinished()告知系统。
     */
    override fun onStartJob(params: JobParameters?): Boolean {
        if (getEnabledStatus()) {
            changeWallpaper(getNextWallpaperPath())
        }
        return false
    }


//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (intent != null && getEnabledStatus()) {
//            changeWallpaper(getNextWallpaperPath())
//            resetAlarm()
//        }
//        return START_NOT_STICKY
//    }

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