package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.HeaderCell

class Header(
    val headerText: String?
) : TMsgListItem {

    class HeaderViewHolder(cell: HeaderCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isVoidBackground = false

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(HeaderCell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as HeaderCell
        cell.setText(headerText.orEmpty())
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        // not clickable
    }
}
