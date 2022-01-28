package cc.ioctl.tmoe.fragment

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.ui.dsl.BaseHierarchyFragment
import cc.ioctl.tmoe.ui.dsl.HierarchyDescription
import cc.ioctl.tmoe.ui.dsl.item.TextDetailItem

class AboutFragment : BaseHierarchyFragment() {
    override val hierarchy = HierarchyDescription(
        titleKey = "About",
        titleResId = R.string.About,
    ) {
        category("SourceCode", R.string.SourceCode) {
            textValue("GitHub", R.string.GitHub, valueConstant = "cinit/TMoe") {
                openUrl("https://github.com/cinit/TMoe")
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
