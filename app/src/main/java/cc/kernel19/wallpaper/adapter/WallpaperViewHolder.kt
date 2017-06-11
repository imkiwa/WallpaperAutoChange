package cc.kernel19.wallpaper.adapter

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import cc.kernel19.wallpaper.R

/**
 * @author kiva
 */
class WallpaperViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    val image: ImageView = itemView!!.findViewById(R.id.wallpaper_image) as ImageView
    var bitmap: Bitmap? = null
}