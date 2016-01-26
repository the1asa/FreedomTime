package com.asabritten.barebonesfreedom;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by t410 on 12/29/2015.
 */
public class TimePickerFragment extends DialogFragment //implements TimePickerDialog.OnTimeSetListener
{

    Handler h;

    public static final TimePickerFragment newInstance(Handler h)
    {
        TimePickerFragment tpf = new TimePickerFragment();
        tpf.h = h;
        return tpf;
    }

    private TimePickerDialog.OnTimeSetListener callback = new TimePickerDialog.OnTimeSetListener()
    {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putInt("HOUR", hour);
            data.putInt("MINUTE", minute);
            msg.setData(data);
            h.sendMessage(msg);
        }
    };

    public Dialog onCreateDialog(Bundle bundle){
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), callback,
                                    hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState){
//        //Use the current time as the default values for the time picker
//        final Calendar c = Calendar.getInstance();
//        int hour = c.get(Calendar.HOUR_OF_DAY);
//        int minute = c.get(Calendar.MINUTE);
//
//        //Create and return a new instance of TimePickerDialog
//        return new TimePickerDialog(getActivity(),this, hour, minute,
//                DateFormat.is24HourFormat(getActivity()));
//    }

//    //onTimeSet() callback method
//    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
//        //Do something with the user chosen time
//        //Get reference of host activity (XML Layout File) TextView widget
//        TextView tv = (TextView) getActivity().findViewById(R.id.tv);
//        //Set a message for user
//        tv.setText("Your chosen time is...\n\n");
//        //Display the user changed time on TextView
//        tv.setText(tv.getText()+ "Hour : " + String.valueOf(hourOfDay)
//                + "\nMinute : " + String.valueOf(minute) + "\n");
//    }
}
