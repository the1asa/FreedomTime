package com.asabritten.barebonesfreedom;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by t410 on 12/25/2015.
 */
public class TimeContainer
{
    private TextView tvShiftDisplay;
    private Button delete;
    private LinearLayout container;

    public TimeContainer(TextView tvShiftDisplay, Button delete, LinearLayout container)
    {
        this.tvShiftDisplay = tvShiftDisplay;
        this.delete = delete;
        this.container = container;

    }


    public LinearLayout getLinearLayoutView()
    {
        return container;
    }
}
