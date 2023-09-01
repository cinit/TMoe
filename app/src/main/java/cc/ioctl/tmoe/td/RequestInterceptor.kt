package cc.ioctl.tmoe.td

import cc.ioctl.tmoe.hook.base.BaseDynamicHook
import cc.ioctl.tmoe.util.Initiator
import cc.ioctl.tmoe.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

object RequestInterceptor {

    @Volatile
    private var mInitialized = false
    private lateinit var kRequestDelegate: Class<*>
    private lateinit var kTLObject: Class<*>
    private lateinit var kTL_error: Class<*>

    private val mRuntimeHookLock = Any()
    private val mHookedClasses = HashSet<Class<*>>(4)
    private val mCallbackSetLock = Any()
    private val mRequestCallbacks = HashSet<Pair<WeakReference<*>, WeakReference<*>>>(4)

    private val mRegisterValueLock = Any()
    private val mRegisteredInterceptors = ConcurrentHashMap<Class<*>, HashSet<InterceptorInfo>>(4)

    private data class InterceptorInfo(val callback: TlrpcResultInterceptCallback, val owner: BaseDynamicHook)

    @Throws(ReflectiveOperationException::class)
    private fun ensureInitialized() {
        if (mInitialized) {
            return
        }
        kRequestDelegate = Initiator.loadClass("org.telegram.tgnet.RequestDelegate")
        kTLObject = Initiator.loadClass("org.telegram.tgnet.TLObject")
        kTL_error = Initiator.loadClass("org.telegram.tgnet.TLRPC\$TL_error")
        // ensure method name and signature is correct
        kRequestDelegate.getDeclaredMethod("run", kTLObject, kTL_error)
        val sendRequest9 = Initiator.loadClass("org.telegram.tgnet.ConnectionsManager").declaredMethods.single {
            it.name == "sendRequest" && it.parameterTypes.size == 9
        }
        val hookCallback = object : XC_MethodHook() {
            override fun beforeHookedMethod(params: MethodHookParam) {
                val request = kTLObject.cast(params.args[0] ?: return)
                val onComplete = params.args[1] ?: return
                kRequestDelegate.cast(onComplete)
                val klass = onComplete.javaClass
                if (klass.name.startsWith("\$Proxy")) {
                    // don't hook proxy
                    return
                }
                if (klass.classLoader == RequestInterceptor::class.java.classLoader) {
                    // don't hook request made by ourselves
                    return
                }
                if (isInterestedRequest(request.javaClass)) {
                    if (!mHookedClasses.contains(klass)) {
                        val run = klass.getDeclaredMethod("run", kTLObject, kTL_error)
                        synchronized(mRuntimeHookLock) {
                            if (!mHookedClasses.contains(klass)) {
                                XposedBridge.hookMethod(run, mHookedOnCompleteInterceptor)
                                mHookedClasses.add(klass)
                            }
                        }
                    }
                    val pair = Pair(WeakReference(request), WeakReference(onComplete))
                    synchronized(mCallbackSetLock) {
                        mRequestCallbacks.add(pair)
                    }
                }
            }
        }
        XposedBridge.hookMethod(sendRequest9, hookCallback)
        mInitialized = true
    }

    @JvmStatic
    fun registerTlrpcResultInterceptor(owner: BaseDynamicHook, requestClass: Class<*>, callback: TlrpcResultInterceptCallback) {
        ensureInitialized()
        // check requestClass
        check(kTLObject.isAssignableFrom(requestClass)) { "requestClass must be subclass of TLObject, but got ${requestClass.name}" }
        synchronized(mRegisterValueLock) {
            val list = mRegisteredInterceptors[requestClass]
            if (list == null) {
                val newList = HashSet<InterceptorInfo>(4)
                newList.add(InterceptorInfo(callback, owner))
                mRegisteredInterceptors[requestClass] = newList
            } else {
                list.add(InterceptorInfo(callback, owner))
            }
        }
    }

    context(BaseDynamicHook)
    fun registerTlrpcResultInterceptor(vararg requestClassArray: Class<*>, callback: TlrpcResultInterceptCallback) {
        for (requestClass in requestClassArray) {
            registerTlrpcResultInterceptor(this@BaseDynamicHook, requestClass, callback)
        }
    }

    context(BaseDynamicHook)
    fun registerTlrpcSuccessfulResultInterceptor(vararg requestClassArray: Class<*>, callback: TlrpcSuccessfulResultInterceptCallback) {
        for (requestClass in requestClassArray) {
            registerTlrpcResultInterceptor(this@BaseDynamicHook, requestClass) { requestObject, resultObject, errorObject ->
                if (errorObject != null) {
                    return@registerTlrpcResultInterceptor null
                }
                callback(requestObject, resultObject!!)
            }
        }
    }

    private fun isInterestedRequest(requestClass: Class<*>): Boolean {
        return mRegisteredInterceptors.containsKey(requestClass)
    }

    class RequestDelegateInvocationHandler(private val fp: TlrpcResultCallback) : InvocationHandler {
        override fun invoke(proxy: Any, method: Method, args: Array<out Any?>?): Any? {
            return if (method.name == "run") {
                fp(args!![0], args[1])
                null
            } else {
                // Object.class
                method.invoke(this, args)
            }
        }
    }

    fun interface TlrpcResultInterceptCallback {
        operator fun invoke(requestObject: Any, resultObject: Any?, errorObject: Any?): Any?
    }

    fun interface TlrpcSuccessfulResultInterceptCallback {
        operator fun invoke(requestObject: Any, resultObject: Any): Any?
    }

    fun interface TlrpcResultCallback {
        operator fun invoke(resultObject: Any?, errorObject: Any?)
    }

    @JvmStatic
    @Throws(ReflectiveOperationException::class)
    fun createRequestDelegate(fp: TlrpcResultCallback): Any {
        ensureInitialized()
        return Proxy.newProxyInstance(
            Initiator.getHostClassLoader(),
            arrayOf(kRequestDelegate),
            RequestDelegateInvocationHandler(fp)
        )
    }

    private val mHookedOnCompleteInterceptor = object : XC_MethodHook(50) {
        override fun beforeHookedMethod(params: MethodHookParam) {
            val respObject: Any? = params.args[0]
            val errorObject: Any? = params.args[1]
            if (respObject == null && errorObject == null) {
                return
            }
            var originalRequest: Any? = null
            synchronized(mCallbackSetLock) {
                val it = mRequestCallbacks.iterator()
                while (it.hasNext()) {
                    val pair = it.next()
                    val r = pair.first.get()
                    val c = pair.second.get()
                    if (r == null || c == null) {
                        it.remove()
                    } else {
                        if (c == params.thisObject) {
                            originalRequest = r
                            it.remove()
                            break
                        }
                    }
                }
            }
            if (originalRequest == null) {
                val hint = (respObject ?: errorObject ?: "null")
                Log.w("original request lost, resp = " + hint + ", this = " + params.thisObject.javaClass.name)
                return
            }
            dispatchRequestResult(params, originalRequest!!, respObject, errorObject)
        }
    }

    private fun dispatchRequestResult(params: XC_MethodHook.MethodHookParam, originalRequest: Any, responseObject: Any?, errorObject: Any?) {
        val reqType = originalRequest.javaClass
        val copy: List<InterceptorInfo>
        synchronized(mRegisterValueLock) {
            val interceptors = mRegisteredInterceptors[reqType] ?: return
            copy = interceptors.toList()
        }
        var overrideResult: Any? = null
        for (info in copy) {
            val callback = info.callback
            val ovr = callback(originalRequest, responseObject, errorObject)
            if (ovr != null && (kTLObject.isInstance(ovr) || kTL_error.isInstance(ovr))) {
                overrideResult = ovr
            }
        }
        if (overrideResult != null) {
            val isErr = kTL_error.isInstance(overrideResult)
            if (isErr) {
                params.args[1] = overrideResult
            } else {
                params.args[0] = overrideResult
            }
        }
    }

}
