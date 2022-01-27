package cc.ioctl.tmoe.ui.dsl

interface DslTMsgListItemInflatable {
    fun inflateTMsgListItems(context: android.content.Context): List<TMsgListItem>
}
