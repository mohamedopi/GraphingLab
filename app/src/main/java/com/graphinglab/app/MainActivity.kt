package com.graphinglab.app

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout

class MainActivity : Activity() {

    private lateinit var web: WebView

    @Suppress("SetJavaScriptEnabled", "DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val topColor = Color.rgb(23, 61, 154)
        val bottomColor = Color.rgb(19, 35, 61)
        window.statusBarColor = topColor
        window.navigationBarColor = bottomColor
        clearLightStatusBar()

        web = WebView(this).apply {
            setBackgroundColor(Color.rgb(245, 248, 252))
            overScrollMode = View.OVER_SCROLL_NEVER
            webChromeClient = WebChromeClient()
            // يمنع فتح أي رابط خارجي داخل التطبيق: الواجهة محلية بالكامل
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
            }
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false      // لا يؤثر على android_asset
                allowContentAccess = false
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
            }
        }

        // مساحتان ملونتان أعلى وأسفل الشاشة: في Android 15+ يرسم النظام التطبيق
        // خلف شريط الحالة وأزرار التنقل، فنحجز لهما مكانًا بدل أن يغطيا الواجهة.
        val topSpacer = View(this).apply { setBackgroundColor(topColor) }
        val bottomSpacer = View(this).apply { setBackgroundColor(bottomColor) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 248, 252))
            addView(topSpacer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
            addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(bottomSpacer, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0))
        }

        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
                val ime = insets.getInsets(WindowInsets.Type.ime())
                v.setPadding(bars.left, 0, bars.right, 0)
                topSpacer.layoutParams.height = bars.top
                bottomSpacer.layoutParams.height = maxOf(bars.bottom, ime.bottom)
                topSpacer.requestLayout()
                bottomSpacer.requestLayout()
                insets
            }
        }

        setContentView(root)
        web.loadUrl("file:///android_asset/index.html")
    }

    @Suppress("DEPRECATION")
    private fun clearLightStatusBar() {
        // خلفية الشريط زرقاء داكنة، فنريد أيقونات بيضاء
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= 23) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onPause() {
        web.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        (web.parent as? ViewGroup)?.removeView(web)
        web.destroy()
        super.onDestroy()
    }
}
