package office.pdf.document.reader.viewer.editor.screen

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import office.pdf.document.reader.viewer.editor.R
import office.pdf.document.reader.viewer.editor.databinding.ActivityPolicyBinding
import com.ezteam.baseproject.activity.BaseActivity
import office.pdf.document.reader.viewer.editor.screen.main.ListFileTypeActivity
import com.brian.base_iap.utils.FirebaseRemoteConfigUtil
//import com.google.android.gms.ads.ez.EzAdControl
import java.io.IOException

class PolicyActivity : BaseActivity<ActivityPolicyBinding>() {
    companion object {
        fun start(activity: FragmentActivity) {
            val intent = Intent(activity, PolicyActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EzAdControl.getInstance(this).showAds()
    }

    override fun initView() {
        binding.toolbar.tvTitle.text = resources.getString(R.string.privacy_policy)

        try {
            val inputStream = assets.open("policy.html")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val str = String(buffer)
            binding.tvContent.text = Html.fromHtml(str)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initListener() {
        binding.toolbar.icBack.setOnClickListener {
            if (FirebaseRemoteConfigUtil.getInstance().isShowAdsMain()) {
                showAdsInterstitial(FirebaseRemoteConfigUtil.getInstance().getAdsConfigValue("inter_setting")){
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    override fun initData() {}
    override fun viewBinding(): ActivityPolicyBinding {
        return ActivityPolicyBinding.inflate(LayoutInflater.from(this))
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}
}