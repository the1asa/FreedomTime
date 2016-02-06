package com.asabritten.barebonesfreedom;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Created by t410 on 1/11/2016.
 */
public class PopupInfo
{
    private ViewGroup parent;
    private View popupView;
    private final PopupWindow popupWindow;
    private WindowManager wm;

    public PopupInfo(Activity layoutActivity, int layout_id, int layout_parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) layoutActivity.getBaseContext()
                .getSystemService(layoutActivity.LAYOUT_INFLATER_SERVICE);

        wm = (WindowManager) layoutActivity.getSystemService(Context.WINDOW_SERVICE);
        parent = (ViewGroup) layoutActivity.findViewById(layout_parent);
        popupView = layoutInflater.inflate(layout_id, parent, false);

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Necessary to close popupWindow when background is touched
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
    }

    private PopupWindow getPopupWindow()
    {
        return popupWindow;
    }

    public void display()
    {
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        dimBackground();
    }

    private void dimBackground()
    {
        View container = (View) getPopupWindow().getContentView().getParent();
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.5f;
        wm.updateViewLayout(container, p);
    }

}
