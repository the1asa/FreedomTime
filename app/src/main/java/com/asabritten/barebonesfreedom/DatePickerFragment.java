package com.asabritten.barebonesfreedom;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by t410 on 12/29/2015.
 */
public class DatePickerFragment extends DialogFragment //implements TimePickerDialog.OnTimeSetListener
{

    Handler h;

    public static final DatePickerFragment newInstance(Handler h)
    {
        DatePickerFragment dpf = new DatePickerFragment();
        dpf.h = h;
        return dpf;
    }

    private DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener()
    {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {

            Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("M/dd/yy", Locale.US);
            //SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMM-dd-yyyy");

            String fullDate = fullDateFormat.format(cal.getTime());
            String dayOfWeek = dayFormat.format(cal.getTime());

//            // hacky, SimpleDateFormat.format(Date date) gives one day ahead with custom Date's
//            Date date = new Date(year, monthOfYear, dayOfMonth - 1);
//            String dayOfWeek = dayFormat.format(date);
//
//            // normal use
//            date = new Date(year, monthOfYear, dayOfMonth);
//            String fullDate = fullDateFormat.format(date);

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("FULL_DATE", fullDate);
            data.putString("DAY_OF_WEEK", dayOfWeek);
            msg.setData(data);
            h.sendMessage(msg);
        }
    };

    public Dialog onCreateDialog(Bundle bundle){
        final Calendar c = Calendar.getInstance();
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        return new DatePickerDialog(getActivity(), callback,
                year, monthOfYear, dayOfMonth);
    }
}