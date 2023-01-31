package cc.ioctl.tmoe.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat.startActivity
import cc.ioctl.tmoe.BuildConfig
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.ui.LocaleController
import cc.ioctl.tmoe.ui.dsl.BaseHierarchyFragment
import cc.ioctl.tmoe.ui.dsl.HierarchyDescription
import cc.ioctl.tmoe.ui.dsl.item.TextDetailItem
import cc.ioctl.tmoe.util.HostInfo

class AboutFragment : BaseHierarchyFragment() {
    override val hierarchy = HierarchyDescription(
        titleKey = "About",
        titleResId = R.string.About,
    ) {
        category("VersionInfo", R.string.VersionInfo) {
            val hostVersionStr = HostInfo.getVersionName() + "(" + HostInfo.getVersionCode() + ")"
            val moduleVersionStr = BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")"
            val hostName = HostInfo.getAppName()
            textValueStatic("TMoe", moduleVersionStr) {
                copyTextToClipboardWithToast(it.context, moduleVersionStr)
            }
            textValueStatic(hostName, hostVersionStr) {
                copyTextToClipboardWithToast(it.context, hostVersionStr)
            }
        }
        category("SourceCode", R.string.SourceCode) {
            textValue("GitHub", R.string.GitHub, valueConstant = "cinit/TMoe") {
                openUrl("https://github.com/cinit/TMoe")
            }
        }
        category("About", R.string.About) {
            textValue("DiscussionGroup", R.string.DiscussionGroup, valueConstant = "@TMoe0") {
                openDiscussionGroup()
            }
        }
        category("LicenseNotices", R.string.LicenseNotices) {
            notices.forEach { this@category.add(noticeToUiItem(it)) }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(parentActivity, intent, null)
    }

    private fun openDiscussionGroup() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            component = ComponentName(context, "org.telegram.ui.LaunchActivity")
            action = Intent.ACTION_VIEW
            // use t.me/+ instead of t.me/@username
            // to avoid the unavailability when the username got revoked by Telegram
            // which happens much frequently when the username is too short
            // I'm not sure whether this will happen, but it's better to be safe
            // encode with base64 to avoid the web scraper
            data = Uri.parse(
                Base64.decode("aHR0cHM6Ly90Lm1lLytBZlhyZEtMMDVHOHdaVGsx", Base64.DEFAULT)
                    .toString(Charsets.UTF_8).replace("\n", "")
            )
        }
        startActivity(context, intent, null)
    }

    private val notices: Array<LicenseNotice> by lazy {
        arrayOf(
            LicenseNotice(
                "Telegram",
                "https://github.com/DrKLO/Telegram",
                "Copyright Nikolai Kudashov",
                "GPL-2.0 License",
            ),
            LicenseNotice(
                "Xposed",
                "https://github.com/rovo89/XposedBridge",
                "Copyright 2013 rovo89, Tungstwenty",
                "Apache License 2.0"
            ),
            LicenseNotice(
                "dexlib2",
                "https://github.com/JesusFreke/smali",
                "Copyright (c) 2010 Ben Gruver (JesusFreke)",
                "https://github.com/JesusFreke/smali/blob/master/NOTICE"
            ),
            LicenseNotice(
                "MMKV",
                "https://github.com/Tencent/MMKV",
                "Copyright (C) 2018 THL A29 Limited, a Tencent company.",
                "BSD 3-Clause License"
            ),
            LicenseNotice(
                "EzXHelper",
                "https://github.com/KyuubiRan/EzXHelper",
                "KyuubiRan",
                "Apache-2.0 License"
            ),
            LicenseNotice(
                "AndroidHiddenApiBypass",
                "https://github.com/LSPosed/AndroidHiddenApiBypass",
                "Copyright (C) 2021 LSPosed",
                "Apache License 2.0"
            ),
            LicenseNotice(
                "coolicons",
                "https://github.com/krystonschwarze/coolicons",
                "Kryston Schwarze",
                "CC 4.0"
            )
        )
    }

    @UiThread
    private fun copyTextToClipboardWithToast(context: Context, text: String) {
        if (text.isEmpty()) return
        val item = ClipData.Item(text)
        val clipData = ClipData("", arrayOf("text/plain"), item)
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager!!.setPrimaryClip(clipData)
        val msg = LocaleController.getString("TextCopied", R.string.TextCopied)
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun noticeToUiItem(notice: LicenseNotice) = TextDetailItem(
        title = notice.name,
        description = { notice.license + ", " + notice.copyright },
        multiLine = true,
        onClick = {
            openUrl(notice.url)
        }
    )

    private data class LicenseNotice(
        val name: String,
        val url: String,
        val copyright: String,
        val license: String
    )
}
