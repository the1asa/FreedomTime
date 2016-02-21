package com.asabritten.barebonesfreedom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Dialog that communicates selected date formatted as MM/dd/yy and dd-MMM-yyyy via handler.
 * Instantiated by passing a Handler object to newInstance(Handler h)
 */
public class DatePickerFragment extends DialogFragment
{
    Handler h;

    /**
     * Instantiates a DatePickerFragment with Handler object
     *
     * @param h the Handler object to associate with a DatePickerFragment
     * @return an instantiated DatePickerFragment
     */
    public static final DatePickerFragment newInstance(Handler h)
    {
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.h = h;
        return dpf;
    }

    /**
     * Dialog listener, creates and sends a message via Handler object once a date is selected
     */
    private DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {

            Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);

            SimpleDateFormat imageDateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
            //SimpleDateFormat fullDateFormat = new SimpleDateFormat("M/dd/yy", Locale.US);
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

            String fullDate = fullDateFormat.format(cal.getTime());
            String imageDate = imageDateFormat.format(cal.getTime());

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("FULL_DATE", fullDate.toUpperCase());
            data.putString("IMAGE_DATE", imageDate);
            msg.setData(data);
            h.sendMessage(msg);
        }
    };

    /**
     * Creates a TimePickerDialog that displays the current time.
     *
     * @param bundle
     * @return a TimePickerDialog
     */
    public Dialog onCreateDialog(Bundle bundle)
    {
        final Calendar c = Calendar.getInstance();
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        return new DatePickerDialog(getActivity(), callback,
                year, monthOfYear, dayOfMonth);
    }
}