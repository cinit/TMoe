package cc.ioctl.tmoe.hook.func

import android.widget.FrameLayout
import android.widget.TextView
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.init.InitFields
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

object HistoricalNewsOption : CommonDynamicHook() {
    override fun initOnce(): Boolean = tryOrFalse {

        try {

        var chatActivity: Any? =null
        var isCreateMenu=false
            findMethod("org.telegram.ui.ChatActivity"){ name=="createMenu" }.hookMethod {
            before {
                chatActivity=it.thisObject
                isCreateMenu=true
            }
            after { isCreateMenu=false }
            }

        //LocaleController.getString("ReportChat", R.string.ReportChat);
        val reportChat = findField("org.telegram.messenger.R\$string"){ name=="ReportChat" }.get(null) as Int
        val reportChatText= findMethod("org.telegram.messenger.LocaleController"){
            name=="getString"&&parameterTypes.size==2
        }.invoke(null,"ReportChat", reportChat) as String

        val callC =  findConstructor("org.telegram.ui.ActionBar.ActionBarMenuSubItem"){ parameterTypes.size==4 }

        findMethod("org.telegram.ui.ActionBar.ActionBarPopupWindow\$ActionBarPopupWindowLayout"){
            name=="addView"&&parameterTypes.size==1
        }.hookBefore {

            if (it.args[0]::class.java.canonicalName=="org.telegram.ui.ActionBar.ActionBarMenuSubItem"){

                if (!isCreateMenu) return@hookBefore

                val texts= getField("textView",it.args[0]) as TextView

                if (texts.text==reportChatText){
                    // ActionBarMenuSubItem cell = new ActionBarMenuSubItem(getParentActivity(), true, true, themeDelegate);
                    val cnt= getField("mContext",it.thisObject,true)
                    val themeDelegate=getField("resourcesProvider",it.thisObject)
                   val call = callC.newInstance(cnt,true,true,themeDelegate)

                      findMethod(call::class.java){
                        name=="setTextAndIcon"&&parameterTypes.size==2
                    }.invoke(call,"历史消息", R.drawable.ic_setting_hex_outline_24)

                    XposedBridge.invokeOriginalMethod(it.method,it.thisObject,arrayOf(call))


                    (call as FrameLayout).setOnClickListener {
                        try {
                                // TLRPC.Peer peer = selectedObject.messageOwner.from_id;
                                val selectedObject=getField("selectedObject",chatActivity)
                                val messageOwner=getField("messageOwner",selectedObject)
                                val from_id=getField("from_id",messageOwner,true)


                                getMethod("openSearchWithText",chatActivity, args = arrayOf(""))

                                val getMessagesController= getMethod("getMessagesController",chatActivity,true)!!
                                val user_id=getField("user_id",from_id,true) as Long
                                val chat_id=getField("chat_id",from_id,true) as Long
                                val channel_id=getField("channel_id",from_id,true) as Long
                                when{
                                    user_id.toInt() != 0->{
                //                    TLRPC.User user = getMessagesController().getUser(peer.user_id);
                //                    searchUserMessages(user, null);
                                        val user= getMethod("getUser",getMessagesController,true,1,user_id)!!
                                        getMethod("searchUserMessages",chatActivity, args= arrayOf(user, null))

                                    }
                                    chat_id.toInt() != 0->{
                                        val chat= getMethod("getChat",getMessagesController,true,1,chat_id)!!
                                        getMethod("searchUserMessages",chatActivity, args= arrayOf(null, chat))
                                    }
                                    channel_id.toInt() != 0->{
                                        val chat= getMethod("getChat",getMessagesController,true,1,channel_id)!!
                                        getMethod("searchUserMessages",chatActivity, args= arrayOf(null, chat))
                                    }
                                }

                                getMethod("showMessagesSearchListView",chatActivity, args= arrayOf(true))
                                getMethod("processSelectedOption",chatActivity, args= arrayOf(999))

                                chatActivity=null
                            }catch (e:Throwable){
                               XposedBridge.log(e)
                            }
                        }





                }

            }

        }

        }catch (e:Throwable){
            XposedBridge.log(e)
        }
    }

    private val mField: MutableMap<String, Field> = ConcurrentHashMap()//HashMap()
    private fun getField(n:String,
                         o:Any?,
                         findSuper: Boolean = false,
                         clzName: String=""
    ):Any?{
        if (mField.containsKey(n)){
            return mField[n]!!.get(o)
        }

        if (o!=null){
            val mF= findField(o::class.java,findSuper){ name==n }
            mField[n] = mF
            return mF.get(o)
        }

        if (clzName!=""){
            val mF= findField(clzName, InitFields.ezXClassLoader,findSuper){ name==n }
            mField[n] = mF
            return mF.get(o)
        }

        return null
    }

    private val mMethod: MutableMap<Any, Method> = ConcurrentHashMap()
    private fun getMethod(
        n:String,
        obj: Any?,
        findSuper: Boolean = false,
        parameterSize:Int=-1,
        vararg args: Any?
    ):Any?{

        if (mMethod.containsKey(n)){
            return mMethod[n]!!.invoke(obj,*args)
        }


        if (obj!=null){
          val mM =  findMethod(obj::class.java,findSuper){
                if (name==n){
                    if (parameterSize!=-1) return@findMethod parameterTypes.size==parameterSize

                    return@findMethod true
                }
               false
            }
            mMethod[n] = mM
            return mM.invoke(obj,*args)
        }


        return null
    }


}