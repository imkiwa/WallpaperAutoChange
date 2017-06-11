package cc.kernel19.wallpaper.activity

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import cc.kernel19.wallpaper.Config
import cc.kernel19.wallpaper.adapter.OnItemClickListener
import cc.kernel19.wallpaper.adapter.OnItemLongClickListener
import cc.kernel19.wallpaper.adapter.WallpaperAdapter
import cc.kernel19.wallpaper.service.ChangeWallpaperService
import cc.kernel19.wallpaper.utils.MediaUtils


class MainActivity : android.support.v7.app.AppCompatActivity() {
    companion object {
        val CHOOSE_PICTURE = 100
        val REQUEST_PERMISSION = 101
    }

    private var emptyTextView: android.widget.TextView? = null
    private var wallpaperList: android.support.v7.widget.RecyclerView? = null

    private var adapter: cc.kernel19.wallpaper.adapter.WallpaperAdapter? = null
    private var preferences: android.content.SharedPreferences? = null

    private val wallpapers: MutableList<String> = mutableListOf()

    private var enableAutoChange: Boolean = false
    private var autoChangeFrequency: Int = 0

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(cc.kernel19.wallpaper.R.layout.activity_main)
        val toolbar = findViewById(cc.kernel19.wallpaper.R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)

        emptyTextView = findViewById(cc.kernel19.wallpaper.R.id.empty_text) as android.widget.TextView
        wallpaperList = findViewById(cc.kernel19.wallpaper.R.id.wallpaper_list) as android.support.v7.widget.RecyclerView

        val fab = findViewById(cc.kernel19.wallpaper.R.id.fab) as android.support.design.widget.FloatingActionButton
        fab.setOnClickListener {
            val intent = android.content.Intent()
            intent.action = android.content.Intent.ACTION_GET_CONTENT
            intent.type = "image/*"

            startActivityForResult(android.content.Intent.createChooser(intent, "选择壁纸"), cc.kernel19.wallpaper.activity.MainActivity.Companion.CHOOSE_PICTURE)
        }

        initPermissions()
        initWallpapers()

        adapter = WallpaperAdapter(LayoutInflater.from(this), wallpapers)

        adapter?.onClickListener = object : OnItemClickListener {
            override fun onClick(item: String, position: Int) {
                val intent = Intent(this@MainActivity, ViewWallpaperActivity::class.java)
                intent.putExtra(Config.EXTRA_WALLPAPER, item)
                startActivity(intent)
            }
        }
        adapter?.onLongClickListener = object : OnItemLongClickListener {
            override fun onLongClick(item: String, position: Int): Boolean {
                AlertDialog.Builder(this@MainActivity).setMessage("移除？")
                        .setPositiveButton(android.R.string.yes, { _: DialogInterface, _: Int ->
                            wallpapers.removeAt(position)
                            adapter?.notifyDataSetChanged()
                            updateView()
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show()
                return true
            }
        }

        wallpaperList?.adapter = adapter
        wallpaperList?.layoutManager = android.support.v7.widget.GridLayoutManager(this, 3)
        wallpaperList?.itemAnimator = android.support.v7.widget.DefaultItemAnimator()
        wallpaperList?.addItemDecoration(cc.kernel19.wallpaper.adapter.SpaceItemDecoration(Config.DECORATION_SPACE))
        updateView()
    }

    private fun initPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this).setMessage("本应用需要本地存储权限来读取本地的壁纸")
                        .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    REQUEST_PERMISSION)
                        })
                        .show()

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initWallpapers()
                    updateView()

                } else {
                    AlertDialog.Builder(this).setMessage("应用无法取得必须的权限，正在退出")
                            .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                                finish()
                            })
                            .show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        when (requestCode) {
            cc.kernel19.wallpaper.activity.MainActivity.Companion.CHOOSE_PICTURE ->
                if (resultCode == android.app.Activity.RESULT_OK && data != null) {
                    val path = MediaUtils.getPath(this@MainActivity, data.data)
                    addNewWallpaper(path)
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(cc.kernel19.wallpaper.R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        val id = item.itemId

        if (id == cc.kernel19.wallpaper.R.id.action_settings) {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val enabled = prefs.getBoolean(getString(cc.kernel19.wallpaper.R.string.key_enable_auto_change), false)
        val frequencyString = prefs.getString(getString(cc.kernel19.wallpaper.R.string.key_frequency), "0")
        val frequency = Integer.parseInt(frequencyString)

        // No changes
        if (autoChangeFrequency == frequency && enableAutoChange == enabled) {
            android.util.Log.e("Wallpaper", "No changes, no actions")
            return
        }

        // Already enabled
        if (enableAutoChange && enabled) {
            android.util.Log.e("Wallpaper", "Already enabled")
            return
        }

        autoChangeFrequency = frequency
        enableAutoChange = enabled

        // No effect
        if (!enabled || frequency == 0) {
            android.util.Log.e("Wallpaper", "No effect")
            return
        }

        val pendingIntent = android.app.PendingIntent.getService(this, 0, android.content.Intent(applicationContext, ChangeWallpaperService::class.java), 0)
        val am = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        am.cancel(pendingIntent)
        am.setExact(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + frequency, pendingIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = getSharedPreferences(cc.kernel19.wallpaper.Config.PREFERENCE_CURRENT, android.content.Context.MODE_PRIVATE)
        pref.edit().putInt(cc.kernel19.wallpaper.Config.KEY_COUNT, wallpapers.size).apply()

        val editor = preferences!!.edit()
        for (i in wallpapers.indices) {
            editor.putString(java.lang.String.valueOf(i), wallpapers[i])
        }
        editor.apply()
    }

    private fun addNewWallpaper(path: String?) {
        if (path == null || path.isEmpty() || wallpapers.contains(path)) {
            return
        }

        android.support.design.widget.Snackbar.make(findViewById(cc.kernel19.wallpaper.R.id.fab), path, 1000).show()
        wallpapers.add(path)
        updateView()
        adapter?.notifyItemInserted(wallpapers.size - 1)
    }

    private fun updateView() {
        when (wallpapers.size) {
            0 -> {
                emptyTextView?.visibility = android.view.View.VISIBLE
                wallpaperList?.visibility = android.view.View.GONE
            }
            else -> {
                emptyTextView?.visibility = android.view.View.GONE
                wallpaperList?.visibility = android.view.View.VISIBLE
            }
        }
    }

    private fun initWallpapers() {
        val pref = getSharedPreferences(cc.kernel19.wallpaper.Config.PREFERENCE_CURRENT, android.content.Context.MODE_PRIVATE)
        val count = pref.getInt(cc.kernel19.wallpaper.Config.KEY_COUNT, 0)

        preferences = getSharedPreferences(cc.kernel19.wallpaper.Config.PREFERENCE_NAME, android.content.Context.MODE_PRIVATE)

        wallpapers.clear()
        (0..count - 1).mapTo(wallpapers) { preferences!!.getString(java.lang.String.valueOf(it), null) }
    }

}
