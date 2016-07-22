package in.infinitecanvas.android;

import android.content.Context;
import android.net.http.SslError;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.Toast;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class WebviewActivity extends AppCompatActivity {
    private TelephonyManager telephonyManager;


    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendLocation(String json) {
            try {
                Log.i("Location from webview", json);
                JSONObject location = new JSONObject(json);
                Double latitude = location.getDouble("latitude");
                Double longitude = location.getDouble("longitude");
                Toast.makeText(mContext, "Location received", Toast.LENGTH_SHORT).show();

                Integer strength = null;
                String carrierName = telephonyManager.getNetworkOperatorName().toLowerCase();
                String networkType = getNetworkClass(telephonyManager.getNetworkType()).toLowerCase();
                List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
                if(cellInfos != null && cellInfos.size() > 0){
                    strength = getStrength(cellInfos.get(0));
                }
            } catch (Exception ex) {}
        }

        private String getNetworkClass(int networkType) {
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return "4G";
                default:
                    return null;
            }
        }

        private Integer getStrength(CellInfo cellInfo) {
            Integer strength = null;
            if (cellInfo.isRegistered()) {
                if(cellInfo instanceof CellInfoWcdma){
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                    CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                    strength = cellSignalStrengthWcdma.getLevel();
                } else if(cellInfo instanceof CellInfoGsm){
                    CellInfoGsm cellInfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                    CellSignalStrengthGsm cellSignalStrengthGsm = cellInfogsm.getCellSignalStrength();
                    strength = cellSignalStrengthGsm.getLevel();
                } else if(cellInfo instanceof CellInfoLte){
                    CellInfoLte cellInfoLte = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                    CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                    strength = cellSignalStrengthLte.getLevel();
                } else if(cellInfo instanceof CellInfoCdma){
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) telephonyManager.getAllCellInfo().get(0);
                    CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                    strength = cellSignalStrengthCdma.getLevel();
                }
            }

            return strength;
        }
    }

    public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // Ignore SSL certificate errors
        }

    }

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview2);
        final WebView webview = (WebView) findViewById(R.id.webview);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                webview.reload();
            }
        });

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webview.setWebViewClient(new GeoWebViewClient());
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webview.setWebChromeClient(new GeoWebChromeClient());
        webview.addJavascriptInterface(new WebAppInterface(this), "Android");
        webview.loadUrl("https://aceankur.github.io/infinitecanvasweb");
    }
}
