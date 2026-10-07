package com.rehelp.app.ui

import android.graphics.BitmapFactory
import android.widget.ImageView
import com.rehelp.app.R

// imageName is either a drawable name ("img_buns") or "file:<path>" for a donor's own photo
object FoodImages {
    fun load(view: ImageView, imageName: String) {
        if (imageName.startsWith("file:")) {
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            val bitmap = BitmapFactory.decodeFile(imageName.removePrefix("file:"), options)
            if (bitmap != null) {
                view.setImageBitmap(bitmap)
                return
            }
        } else if (imageName.isNotEmpty()) {
            val id = view.resources.getIdentifier(imageName, "drawable", view.context.packageName)
            if (id != 0) {
                view.setImageResource(id)
                return
            }
        }
        view.setImageResource(R.drawable.ic_food_placeholder)
    }
}
