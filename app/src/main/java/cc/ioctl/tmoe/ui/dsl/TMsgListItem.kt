package cc.ioctl.tmoe.ui.dsl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface TMsgListItem : DslTMsgListItemInflatable {

    val isEnabled: Boolean

    val isVoidBackground: Boolean

    fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder

    fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context)

    fun onItemClick(v: View, position: Int, x: Float, y: Float)

    fun hasDoubleTap(v: View, position: Int): Boolean {
        // default do nothing
        return false
    }

    fun onDoubleTap(v: View, position: Int, x: Float, y: Float) {
        // do nothing
    }

    override fun inflateTMsgListItems(context: Context): List<TMsgListItem> {
        return listOf(this)
    }
}
