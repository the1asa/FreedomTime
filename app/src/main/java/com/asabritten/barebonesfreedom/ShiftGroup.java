package com.asabritten.barebonesfreedom;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by t410 on 1/2/2016.
 */
public class ShiftGroup
{
    private HashMap<ImageButton, Shift> times;
    private double payRate, totalHours;

    public ShiftGroup()
    {
        times = new HashMap<>();
    }

    public void addShift(ImageButton ibDelete, Shift s)
    {
        times.put(ibDelete, s);
        totalHours += s.getHoursWorked();
    }

    private Shift getShift(View v)
    {
        return times.get(v);
    }

    public void removeShift(View v)
    {
        totalHours -= getShift(v).getHoursWorked();
        times.remove(v);
    }

    public void setPayRate(double payRate)
    {
        this.payRate = payRate;
    }

//    private double calculateTotalHours()
//    {
//        double totalHours = 0;
//        for (Map.Entry e : times.entrySet())
//        {
//            System.out.println(e.getKey().toString());
//            System.out.println(e.getValue().toString());
//
//            Shift s = (Shift)e.getValue();
//            totalHours += s.getHoursWorked();
//        }
//
//        return totalHours;
//    }

    public double getTotalPay()
    {
        return payRate * totalHours;
    }

    public double getTotalHours()
    {
        return totalHours;
    }


}
