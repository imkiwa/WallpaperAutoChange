package cc.kernel19.wallpaper.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ImageView
import cc.kernel19.wallpaper.Config
import cc.kernel19.wallpaper.R

class ViewWallpaperActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_view_wallpaper)

        val wallpaper = intent.getStringExtra(Config.EXTRA_WALLPAPER)
        if (wallpaper != null) {
            bitmap = BitmapFactory.decodeFile(wallpaper)
            val imageView = findViewById(R.id.view_wallpaper_image) as ImageView
            imageView.setImageBitmap(bitmap)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bitmap != null) {
            bitmap?.recycle()
        }
    }
}
