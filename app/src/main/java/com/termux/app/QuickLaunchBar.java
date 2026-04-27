package com.termux.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

public class QuickLaunchBar extends HorizontalScrollView {

    private LinearLayout mContainer;

    public QuickLaunchBar(Context context) {
        super(context);
        init();
    }

    public QuickLaunchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setHorizontalScrollBarEnabled(false);
        mContainer = new LinearLayout(getContext());
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mContainer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }

    public void setPackages(List<String> entries) {
        mContainer.removeAllViews();
        PackageManager pm = getContext().getPackageManager();
        float density = getContext().getResources().getDisplayMetrics().density;
        int iconSize = Math.round(36 * density);
        int padding = Math.round(6 * density);

        for (String entry : entries) {
            String pkg = resolvePackage(pm, entry);
            if (pkg == null) continue;

            try {
                Drawable icon = pm.getApplicationIcon(pkg);
                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent == null) continue;

                ImageButton btn = new ImageButton(getContext());
                btn.setImageDrawable(roundedIcon(icon, iconSize));
                btn.setBackground(null);
                btn.setPadding(padding, padding, padding, padding);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    iconSize + padding * 2, iconSize + padding * 2);
                lp.gravity = Gravity.CENTER_VERTICAL;
                btn.setLayoutParams(lp);

                final Intent intent = launchIntent;
                btn.setOnClickListener(v -> getContext().startActivity(intent));
                mContainer.addView(btn);
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
    }

    private Drawable roundedIcon(Drawable icon, int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        float radius = size * 0.25f;
        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, size, size), radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        icon.setBounds(0, 0, size, size);
        icon.draw(canvas);
        return new BitmapDrawable(getContext().getResources(), bmp);
    }

    private String resolvePackage(PackageManager pm, String entry) {
        // Try as package name first
        try {
            pm.getApplicationInfo(entry, 0);
            return entry;
        } catch (PackageManager.NameNotFoundException ignored) {}

        // Fall back to matching by app label (case-insensitive)
        String lower = entry.toLowerCase(java.util.Locale.ROOT);
        for (ApplicationInfo app : pm.getInstalledApplications(0)) {
            CharSequence label = pm.getApplicationLabel(app);
            if (label != null && label.toString().toLowerCase(java.util.Locale.ROOT).equals(lower)) {
                return app.packageName;
            }
        }
        return null;
    }
}
