package com.asabritten.barebonesfreedom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates a group of shifts, whether Lifeguard, Headguard, or WSI
 */
public final class ShiftGroup implements Serializable
{
    private HashMap<String, Double> times;
    private double payRate, totalHours;

    /**
     * Instantiates an empty ShiftGroup.
     */
    public ShiftGroup()
    {
        times = new HashMap<>();
    }

    /**
     * Adds a shift, where a shift is composed of a date and the hours for that date.
     * The dates entered are stored as is.
     *
     * @param date  date of a shift
     * @param hours hours worked for a shift
     */
    public void addShift(String date, Double hours)
    {
        totalHours += hours;

        if (times.get(date) != null)
        {
            hours += times.get(date);
        }

        times.put(date, hours);
    }

    /**
     * Gets a list of all the shift dates
     *
     * @return an ArrayList of date strings
     */
    public ArrayList<String> getDates()
    {
        ArrayList<String> dates = new ArrayList<String>();

        for (Map.Entry<String, Double> e : times.entrySet())
        {
            dates.add(e.getKey());
        }

        return dates;
    }

    /**
     * Gets the hours worked on a given date
     *
     * @param date date of a shift
     * @return hours worked
     */
    public Double getHours(String date)
    {
        return times.get(date);
    }

    /**
     * Removes the hours worked from all shifts on a given date
     *
     * @param date date of shifts
     */
    public void removeHours(String date)
    {
        totalHours -= times.get(date);
        times.remove(date);
    }

    /**
     * Changes the hourly pay rate for all shifts
     *
     * @param payRate hourly rate
     */
    public void setPayRate(double payRate)
    {
        this.payRate = payRate;
    }

    /**
     * Calculates the total pay for all shifts
     *
     * @return total pay
     */
    public double getTotalPay()
    {
        return payRate * totalHours;
    }

    /**
     * Gets the total hours for all shifts
     *
     * @return total hours
     */
    public double getTotalHours()
    {
        return totalHours;
    }

    /**
     * Returns a string representation of shifts worked in the format:
     * date: hours
     *
     * @return a string representation
     */
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
