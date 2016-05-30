package com.asabritten.barebonesfreedom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Dialog that communicates selected hour and minute via handler.
 * Instantiated by passing a Handler object to newInstance(Handler h)
 */
public class TimePickerFragment extends DialogFragment
{
    Handler h;

    /**
     * Instantiates a TimePickerFragment with Handler object
     *
     * @param h the Handler object to associate with a TimePickerFragment
     * @return an instantiated TimePickerFragment
     */
    public static final TimePickerFragment newInstance(Handler h)
    {
        TimePickerFragment tpf = new TimePickerFragment();
        tpf.h = h;
        return tpf;
    }

    /**
     * Dialog listener, creates and sends a message via Handler object once a time is selected
     */
    private TimePickerDialog.OnTimeSetListener callback = new TimePickerDialog.OnTimeSetListener()
    {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute)
        {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt("HOUR", hour);
            data.putInt("MINUTE", minute);
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
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), callback,
                hour, minute, DateFormat.is24HourFormat(getActivity()));
    }
}
