package com.asabritten.barebonesfreedom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by t410 on 1/2/2016.
 */
public class ShiftGroup implements Serializable//implements Parcelable, Serializable
{
    private HashMap<String, Double> times;
    private double payRate, totalHours;

    public ShiftGroup()
    {
        times = new HashMap<>();
    }

    public void addShift(String date, Double hours)
    {
        totalHours += hours;

        if (times.get(date) != null)
        {
            hours += times.get(date);
        }

        times.put(date, hours);

        System.out.println(totalHours);
    }

    public ArrayList<String> getDates()
    {
        ArrayList<String> dates = new ArrayList<String>();

        for (Map.Entry<String, Double> e : times.entrySet())
        {
            dates.add(e.getKey());
        }

        return dates;
    }

    public Double getHours(String date)
    {
        return times.get(date);
    }

    public void removeHours(String date)
    {
        totalHours -= times.get(date);
        times.remove(date);
    }

    public void setPayRate(double payRate)
    {
        this.payRate = payRate;
    }

    public double getTotalPay()
    {
        return payRate * totalHours;
    }

    public double getTotalHours()
    {
        return totalHours;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Double> e : times.entrySet())
        {
            sb.append(e.getKey() + ": " + e.getValue() + "\n");
        }

        return sb.toString();
    }


}
