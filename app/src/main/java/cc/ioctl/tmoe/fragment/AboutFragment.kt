package cc.ioctl.tmoe.fragment

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import cc.ioctl.tmoe.R
import cc.ioctl.tmoe.ui.dsl.BaseHierarchyFragment
import cc.ioctl.tmoe.ui.dsl.HierarchyDescription

class AboutFragment : BaseHierarchyFragment() {
    override val hierarchy = HierarchyDescription(
        titleKey = "About",
        titleResId = R.string.About,
    ) {
        textValue("GitHub", R.string.GitHub, valueConstant = "cinit/TMoe") {
            openUrl("https://github.com/cinit/TMoe")
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(parentActivity, intent, null)
    }
}
