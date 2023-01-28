package cc.ioctl.tmoe.hook.func

import android.app.AndroidAppHelper
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import cc.ioctl.tmoe.hook.base.CommonDynamicHook
import com.github.kyuubiran.ezxhelper.utils.*

object HidePhoneNumber : CommonDynamicHook() {

    //ProfileActivity.java hidePhone = true;          updateListAnimated(false);
    override fun initOnce(): Boolean = tryOrLogFalse {
        findMethod(loadClass("org.telegram.ui.Cells.DrawerProfileCell"),false){
            name=="setUser"&& parameterTypes.size==2
        }.hookAfter {

            if (!isEnabled)return@hookAfter

//            Log.d("class name:  "+it.thisObject::class.java.name)
            val vvv=  findField(it.thisObject::class.java,true){
                name=="phoneTextView"
            }.get(it.thisObject)

            var username=  findField(it.args[0]::class.java,true){
                name=="username"
            }.get(it.args[0]) as String?

            username = if (TextUtils.isEmpty(username)){
                "@???"
            }else{
                "@$username"
            }

            (vvv as TextView).text = username
//                findMethod(vvv::class.java){
//                    name=="setText"&&parameterTypes.size==1&&parameterTypes[0]==CharSequence::class.java
//                }.invoke(vvv,vvv2)


        }
    }
}