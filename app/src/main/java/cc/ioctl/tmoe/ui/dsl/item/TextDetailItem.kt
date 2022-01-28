package cc.ioctl.tmoe.ui.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cc.ioctl.tmoe.ui.dsl.DslTMsgListItemInflatable
import cc.ioctl.tmoe.ui.dsl.TMsgListItem
import cc.ioctl.tmoe.ui.wrapper.cell.TextDetailSettingsCell

class TextDetailItem(
    private val title: String,
    private val description: ((Context) -> String?)?,
    private val onClick: View.OnClickListener?,
    private val multiLine: Boolean
) : DslTMsgListItemInflatable, TMsgListItem {

    class ViewHolder(cell: TextDetailSettingsCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = true

    override fun createViewHolder(context: Context, parent: ViewGroup) =
        ViewHolder(TextDetailSettingsCell(context))

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TextDetailSettingsCell
        val titleStr = title
        val descStr = description?.invoke(context)
        if (descStr != null) {
            cell.setTextAndValue(titleStr, descStr, true)
            cell.setMultilineDetail(multiLine)
        } else {
            cell.setTextAndValue(titleStr, null, true)
        }
    }

    override fun onItemClick(v: View, position: Int, x: Float, y: Float) {
        onClick?.onClick(v)
    }
}
