package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.DslTMsgListItemInflatable
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.TextInfo2Cell

class DescriptionItem(
    val textKey: String,
    val textResId: Int
) : DslTMsgListItemInflatable, TMsgListItem {

    class HeaderViewHolder(cell: TextInfo2Cell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isVoidBackground = false

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(TextInfo2Cell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TextInfo2Cell
        val text = LocaleController.getString(textKey, textResId)
        cell.setText(text)
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        // not clickable
    }
}
