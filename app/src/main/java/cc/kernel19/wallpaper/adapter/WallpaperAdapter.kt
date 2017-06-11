package cc.kernel19.wallpaper.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cc.kernel19.wallpaper.Config
import cc.kernel19.wallpaper.R
import cc.kernel19.wallpaper.utils.BitmapUtils

/**
 * @author kiva
 */
class WallpaperAdapter(val layoutInflater: LayoutInflater, val wallpapers: List<String>) : RecyclerView.Adapter<WallpaperViewHolder>() {
    var onClickListener: OnItemClickListener? = null
    var onLongClickListener: OnItemLongClickListener? = null

    override fun onBindViewHolder(viewHolder: WallpaperViewHolder?, position: Int) {
        if (viewHolder == null) {
            return
        }

        val path = wallpapers[position]

        val bitmap = BitmapUtils.decodeSampledBitmapFromFileSystem(path, Config.BITMAP_SAMPLE_LEVEL)
        viewHolder.bitmap = bitmap
        viewHolder.image.setImageBitmap(bitmap)

        viewHolder.itemView.setOnClickListener({ onClickListener?.onClick(path, position) })
        viewHolder.itemView.setOnLongClickListener({
            return@setOnLongClickListener onLongClickListener?.onLongClick(path, position)!!
        })
    }

    override fun getItemCount(): Int {
        return wallpapers.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, p1: Int): WallpaperViewHolder {
        return WallpaperViewHolder(layoutInflater.inflate(R.layout.layout_wallpaper_item, parent, false))
    }

    override fun onViewRecycled(holder: WallpaperViewHolder?) {
        holder?.bitmap?.recycle()
        super.onViewRecycled(holder)
    }
}