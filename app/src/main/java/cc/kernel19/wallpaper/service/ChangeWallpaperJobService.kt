package cc.kernel19.wallpaper.service

import android.app.WallpaperManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
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
        Log.e("Wallpaper", "onStartJob: changing wallpaper")
        Thread {
            if (getEnabledStatus()) {
                changeWallpaper(getNextWallpaperPath())
            }
        }.start()
        return false
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