package com.asabritten.barebonesfreedom;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    // Contains all the shifts in a HashMap
    private ShiftGroup sg = new ShiftGroup();
    // Displays delete button for a shift
    private ImageButton ibDelete;
    // Displays date information for a shift
    private TextView tvShiftDisplay;
    // Holds LinearLayouts containing shift information
    private LinearLayout llShiftHolder;
    // From TimePickerDialog, used to calculate shift time
    private int inHour, outHour, inMinute, outMinute;
    // From DatePickerDialog, used to display shift date
    private String dayOfWeek, fullDate;

    private TextSwitcher totalHoursDisplay, payDisplay;

    private Animation fadeIn, fadeOut;


    // Handler for DatePickerFragment
    private Handler cal = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            dayOfWeek = msg.getData().getString("DAY_OF_WEEK");
            fullDate = msg.getData().getString("FULL_DATE");

            printDate();
        }
    };

    // Handler for TimePickerFragment
    private Handler in = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            inHour = msg.getData().getInt("HOUR");
            inMinute = msg.getData().getInt("MINUTE");
            printTime();
        }
    };

    // Handler for TimePickerFragment
    private Handler out = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            outHour = msg.getData().getInt("HOUR");
            outMinute = msg.getData().getInt("MINUTE");
            printTime();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        totalHoursDisplay = (TextSwitcher) findViewById(R.id.ts_total_hours);
        totalHoursDisplay.setFactory(new ViewSwitcher.ViewFactory()
        {
            public View makeView()
            {
                TextView tv = new TextView(MainActivity.this);
                tv.setTextSize(20);
                tv.setTextColor(Color.BLACK);
                return tv;
            }
        });

        payDisplay = (TextSwitcher) findViewById(R.id.ts_pay);
        payDisplay.setFactory(new ViewSwitcher.ViewFactory()
        {
            public View makeView()
            {
                TextView tv = new TextView(MainActivity.this);
                tv.setTextSize(20);
                tv.setTextColor(Color.WHITE);
                return tv;
            }
        });

        payDisplay.setInAnimation(this, android.R.anim.fade_in);
        totalHoursDisplay.setInAnimation(this, android.R.anim.fade_in);
        payDisplay.setOutAnimation(this, android.R.anim.fade_out);
        totalHoursDisplay.setOutAnimation(this, android.R.anim.fade_out);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_pay, popup.getMenu());
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        String job = "";
                        if (item.getTitle().equals("Lifeguard"))
                        {
                            sg.setPayRate(8.50);
                            job = "LG";
                        } else if (item.getTitle().equals("Headguard"))
                        {
                            sg.setPayRate(10.00);
                            job = "HG";
                        } else if (item.getTitle().equals("WSI/LGI"))
                        {
                            sg.setPayRate(15.00);
                            job = "WSI/LGI";
                        }

                        totalHoursDisplay.setText(job + ": " + sg.getTotalHours() + "  ");
                        payDisplay.setText("$" + String.format("%.2f", sg.getTotalPay()));

                        return true;
                    }
                });
            }

        });

        loadTime();
        loadDate();

        // Animations
        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        llShiftHolder = (LinearLayout) findViewById(R.id.ll_shift_holder);
        ImageButton ibAddShift = (ImageButton) findViewById(R.id.ibAddShift);

        // Adds shift to display and memory (ShiftGroup)
        ibAddShift.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                double hours;

                try
                {
                    hours = calculateTime();
                } catch (IllegalStateException e)
                {
                    Toast.makeText(MainActivity.this,
                            "Clock out time must be later than clock in time",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                loadShiftDisplay(hours);

            }
        });
    }


    // Sets up and displays date and hours worked for a shift
    private void loadShiftDisplay(double hours)
    {
        LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, .95f);

        tvShiftDisplay = new TextView(MainActivity.this);
        tvShiftDisplay.setLayoutParams(tvParam);
        if (dayOfWeek == null || fullDate == null)
            tvShiftDisplay.setText(String.valueOf(hours));
        else
            tvShiftDisplay.setText(dayOfWeek + ", " + fullDate + "\n" + hours);
        tvShiftDisplay.setTextSize(20);
        tvShiftDisplay.setGravity(Gravity.CENTER);
        tvShiftDisplay.setTextColor(Color.BLACK);

        LinearLayout.LayoutParams bParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, .05f);

        ibDelete = new ImageButton(MainActivity.this);
        ibDelete.setLayoutParams(bParam);
        ibDelete.setImageResource(R.drawable.delete_no_border);
        ibDelete.setBackgroundColor(Color.parseColor("#cee9f8"));

        // Removes shift layout
        ibDelete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                //ViewGroup vg = (ViewGroup)sg.getShift(view).getDisplay().getParent();
                final ViewGroup vg = (ViewGroup) view.getParent();
                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        MainActivity.this.llShiftHolder.removeView(vg);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                    }
                });
                vg.startAnimation(fadeOut);


                //llShiftHolder.removeView(vg);
                //llShiftHolder.removeView((ViewGroup) sg.getShift(view).getDisplay().getParent());
                sg.removeShift(view);
            }
        });

        final LinearLayout llShift = new LinearLayout(MainActivity.this);

        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llParam.setMargins(10, 5, 10, 5);

        llShift.setLayoutParams(llParam);
        llShift.setBackgroundResource(R.drawable.light_blue_rounded_corner_rectangle);
        llShift.setOrientation(LinearLayout.HORIZONTAL);
        llShift.setPadding(5, 5, 5, 5);
        llShift.addView(tvShiftDisplay);
        llShift.addView(ibDelete);

        llShiftHolder.addView(llShift, 0);

        llShift.startAnimation(fadeIn);

        sg.addShift(ibDelete, new Shift(hours, tvShiftDisplay));
    }

    // Prints date given by DatePickerFragment
    private void printDate()
    {
        TextView tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate.setText(fullDate);
    }

    // Starts DatePickerFragment
    private void loadDate()
    {
        ImageButton calendar = (ImageButton) findViewById(R.id.ib_calendar);
        calendar.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = DatePickerFragment.newInstance(cal);
                newFragment.show(getFragmentManager(), "DatePicker");
            }
        });
    }

    // Calculates time based on information from TimePickerFragment
    private double calculateTime()
    {
        int totalInMinutes = (inHour * 60) + inMinute;
        int totalOutMinutes = (outHour * 60) + outMinute;
        int totalMinutes = (totalOutMinutes - totalInMinutes) % 60;
        int totalHours = (totalOutMinutes - totalInMinutes) / 60;

        double time = (double) totalHours;
        if (time < 0)
            throw new IllegalStateException();

        if (totalMinutes <= 7)
            time += 0;
        else if (totalMinutes > 7 && totalMinutes <= 22)
            time += 0.25;
        else if (totalMinutes > 22 && totalMinutes <= 37)
            time += 0.5;
        else if (totalMinutes > 37 && totalMinutes <= 52)
            time += 0.75;
        else
            time += 1;

        return time;
    }

    // Starts TimePickerFragment
    private void loadTime()
    {
        ImageButton inTime = (ImageButton) findViewById(R.id.in_time);
        ImageButton outTime = (ImageButton) findViewById(R.id.out_time);

        inTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = TimePickerFragment.newInstance(in);
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });

        outTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = TimePickerFragment.newInstance(out);
                newFragment.show(getFragmentManager(), "TimePicker");
            }
        });
    }

    // Displays information from TimePickerFragment
    private void printTime()
    {
        TextView tvInTime = (TextView) findViewById(R.id.tv_clock_in);
        TextView tvOutTime = (TextView) findViewById(R.id.tv_clock_out);

        tvInTime.setText(convert24HourTime(inHour, inMinute));
        tvOutTime.setText(convert24HourTime(outHour, outMinute));
    }

    // Converts 24 hour time from TimePickerFragment to displayed 12 hour time
    private String convert24HourTime(int h, int m)
    {
        String amPM = "am";
        String minute = String.valueOf(m);
        int hour = h;

        if (hour > 11)
            amPM = "pm";
        if (hour == 0)
            hour = 12;
        if (hour > 12)
            hour -= 12;
        if (m < 10)
            minute = "0" + minute;

        return new String(hour + " : " + minute + amPM);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }

        if (id == R.id.action_help)
        {
            displayPopupInfo(R.layout.help_popup);
        }

        if (id == R.id.action_about)
        {
            displayPopupInfo(R.layout.about_popup);
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayPopupInfo(int id)
    {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        ViewGroup parent = (ViewGroup) findViewById(R.id.ll_shift_holder);
        View popupView = layoutInflater.inflate(id, parent, false);

        PopupInfo info = new PopupInfo(parent, popupView,
                (WindowManager) getSystemService(Context.WINDOW_SERVICE));

        info.display();
    }

}
