package com.asabritten.barebonesfreedom;

import android.widget.TextView;

/**
 * Created by t410 on 1/1/2016.
 */
public class Shift
{
    private double hoursWorked;
    private String dayOfWeek, fullDate, startTime, endTime;
    private TextView display;

    public Shift()
    {

    }

    public TextView getDisplay()
    {
        return display;
    }

    public Shift(double hoursWorked, TextView display)
    {
        this.hoursWorked = hoursWorked;
        this.display = display;

    }

    public Shift(double hoursWorked, String startTime, String endTime, String dayOfWeek, String fullDate)
    {
        this.hoursWorked = hoursWorked;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.fullDate = fullDate;
    }

    public double getHoursWorked()
    {
        return hoursWorked;
    }

    public void setHoursWorked(double mHoursWorked)
    {
        hoursWorked = mHoursWorked;
    }

    public String getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek(String mDayOfWeek)
    {
        dayOfWeek = mDayOfWeek;
    }

    public String getFullDate()
    {
        return fullDate;
    }

    public void setFullDate(String mFullDate)
    {
        fullDate = mFullDate;
    }


}
