package office.file.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.ezteam.baseproject.utils.PreferencesUtils;
import com.ezteam.baseproject.utils.PresKey;

public class OpenFileActivity extends BaseOpenFileActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        EdgeToEdge.enable(this);
        super.onCreate(bundle);
        boolean keepScreenOn = PreferencesUtils.getBoolean(PresKey.SETTING_STAY_AWAKE, false);
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
//                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.updatePadding(
//                    //   left = insets.left,
//                    top = insets.top,
//                    // right = insets.right,
//                    bottom = insets.bottom
//            )
//            WindowInsetsCompat.CONSUMED
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        Log.d("openDocuments", "OpenFileActivity onDestroy in");
        super.onDestroy();
        int readCount = PreferencesUtils.getInteger(PresKey.READ_DOCUMENT_COUNT, 0);
        if (readCount < 2) {
            PreferencesUtils.putInteger(PresKey.READ_DOCUMENT_COUNT, readCount + 1);
        } else {
            PreferencesUtils.putInteger(PresKey.READ_DOCUMENT_COUNT, 0);
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.d("openDocuments", "OpenFileActivity onDestroy out");

    }
}
