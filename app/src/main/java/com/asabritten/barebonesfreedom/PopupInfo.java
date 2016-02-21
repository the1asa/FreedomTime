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
 * Applies custom background to PopupWindow and gives screen dimming functionality
 */
public class PopupInfo
{
    // Parent of PopupWindow
    private ViewGroup parent;
    private final PopupWindow popupWindow;
    private WindowManager wm;

    /**
     * Instantiates PopupWindow in a given Activity with a custom layout and layout parent
     *
     * @param layoutActivity the Activity to display the PopupWindow in
     * @param layout_id      the custom layout to display in the PopupWindow
     * @param layout_parent  the parent of the custom layout
     */
    public PopupInfo(Activity layoutActivity, int layout_id, int layout_parent)
    {
        LayoutInflater layoutInflater = (LayoutInflater) layoutActivity.getBaseContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        wm = (WindowManager) layoutActivity.getSystemService(Context.WINDOW_SERVICE);
        parent = (ViewGroup) layoutActivity.findViewById(layout_parent);
        View popupView = layoutInflater.inflate(layout_id, parent, false);

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Necessary to close popupWindow when background is touched
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
    }

    /**
     * Returns PopupWindow object
     *
     * @return a PopupWindow object
     */
    private PopupWindow getPopupWindow()
    {
        return popupWindow;
    }

    /**
     * Displays the PopupWindow in the center of the layout
     */
    public void display()
    {
        popupWindow.showAtLocation(parent, Gravity.CENTER, 0, 0);
        dimBackground();
    }

    /**
     * Dims the parent layout
     */
    private void dimBackground()
    {
        View container = (View) getPopupWindow().getContentView().getParent();
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.5f;
        wm.updateViewLayout(container, p);
    }

}
