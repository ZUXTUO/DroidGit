package com.olsc.droidgit.ui.settings;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.olsc.droidgit.R;
import com.olsc.droidgit.util.LocaleHelper;

/**
 * 关于和捐赠页面
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AboutActivity";
    
    private Button alipayButton;
    private Button wechatButton;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_zen);
        
        setupActionBar();
        initializeViews();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.about_support_title);
        }
    }

    private void initializeViews() {
        alipayButton = findViewById(R.id.donateAlipayButton);
        if (alipayButton != null) {
            alipayButton.setOnClickListener(this);
        }

        wechatButton = findViewById(R.id.donateWechatButton);
        if (wechatButton != null) {
            wechatButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.donateAlipayButton) {
            showQrDialog(R.drawable.pay_zfb);
        } else if (id == R.id.donateWechatButton) {
            showQrDialog(R.drawable.pay_wx);
        }
    }

    /**
     * 显示二维码对话框
     * @param resId 二维码资源ID
     */
    private void showQrDialog(int resId) {
        final Dialog dialog = new Dialog(this, R.style.ThemeOverlay_App_MaterialAlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_qr_code);

        ImageView imageView = dialog.findViewById(R.id.qrImageView);
        if (imageView != null) {
            imageView.setImageResource(resId);

            // 点击图片关闭对话框
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            // 长按打开对应应用
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String packageName = (resId == R.drawable.pay_zfb) ? 
                            "com.eg.android.AlipayGphone" : "com.tencent.mm";
                    String appName = (resId == R.drawable.pay_zfb) ? 
                            getString(R.string.donate_button_alipay) : getString(R.string.donate_button_wechat);

                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
                        if (intent != null) {
                            startActivity(intent);
                            Toast.makeText(AboutActivity.this, 
                                    getString(R.string.opening_app_toast, appName), 
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(AboutActivity.this, 
                                    getString(R.string.app_not_installed_toast, appName), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(AboutActivity.this, 
                                getString(R.string.error_opening_app_toast, appName), 
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }

        // 设置对话框大小和背景
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
                    WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
