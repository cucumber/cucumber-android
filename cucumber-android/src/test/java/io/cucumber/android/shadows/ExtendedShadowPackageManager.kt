package io.cucumber.android.shadows

import android.content.ComponentName
import android.content.pm.InstrumentationInfo
import android.content.pm.PackageManager
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowApplicationPackageManager

@Implements(className = "android.app.ApplicationPackageManager", isInAndroidSdk = false, looseSignatures = true)
class ExtendedShadowPackageManager:ShadowApplicationPackageManager() {

    @Implementation
    @Throws(PackageManager.NameNotFoundException::class)
    override fun getInstrumentationInfo(className: ComponentName?, flags: Int): InstrumentationInfo {
        return InstrumentationInfo()
    }
}