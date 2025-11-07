package com.ezteam.ezpdflib.activity.thubnail

//import com.google.android.gms.ads.ez.EzAdControl;
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.ezteam.ezpdflib.activity.BasePdfViewerActivity
import com.ezteam.ezpdflib.databinding.InsertBlankPageBinding
import com.ezteam.ezpdflib.util.Config
import io.reactivex.rxjava3.disposables.CompositeDisposable

class InserActivity : BasePdfViewerActivity() {
    protected var disposable: CompositeDisposable = CompositeDisposable()
    private var binding: InsertBlankPageBinding? = null
    private var mapUriPage: HashMap<Int, Uri>? = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InsertBlankPageBinding.inflate(LayoutInflater.from(this))
        setContentView(binding!!.root)
        //        EzAdControl.getInstance(this).showAds();
        initView2() // change name to 2 to avoid conflict with init view of BasePdfViewerActivity
        initListener2()
    }

    private fun initView2() {
        if (intent != null) {
            mapUriPage =
                intent.getSerializableExtra(Config.Constant.DATA_MU_PDF_CORE) as HashMap<Int, Uri>?
        }
        if (mapUriPage == null) return
        val datas = ArrayList(
            mapUriPage!!.values
        )

    }

    private fun initListener2() {
        binding!!.ivBack.setOnClickListener { v ->
            finish()
        }
    }
}