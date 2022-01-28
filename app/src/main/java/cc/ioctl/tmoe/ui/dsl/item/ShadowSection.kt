package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.ShadowSectionCell

class ShadowSection : TMsgListItem {

    class ViewHolder(cell: ShadowSectionCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isVoidBackground = true

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return ViewHolder(ShadowSectionCell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        // nop
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        // not clickable
    }
}
