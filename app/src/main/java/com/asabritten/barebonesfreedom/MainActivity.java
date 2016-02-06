package com.asabritten.barebonesfreedom;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends AppCompatActivity
{
    // Contains all the shifts in a HashMap
    private ShiftGroup sg = new ShiftGroup();
    // Displays delete button for a shift
    private ImageButton ibDelete;
    // Displays date information for a shift
    private TextView tvShiftDisplay, tvShiftList;
    // Holds LinearLayouts containing shift information
    private LinearLayout llShiftHolder;
    // From TimePickerDialog, used to calculate shift time
    private int inHour, outHour, inMinute, outMinute;
    // From DatePickerDialog, used to display shift date
    private String imageDate, fullDate;

    private TextSwitcher totalHoursDisplay, payDisplay;

    private Animation fadeIn, fadeOut;



    // Handler for DatePickerFragment
    private Handler cal = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            imageDate = msg.getData().getString("IMAGE_DATE");
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

                if (fullDate == null)
                {
                    Toast.makeText(MainActivity.this,
                            "You must enter a date first",
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

        final LinearLayout llShift = new LinearLayout(MainActivity.this);

        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llParam.setMargins(10, 5, 10, 5);

        llShift.setLayoutParams(llParam);
        llShift.setBackgroundResource(R.drawable.light_blue_rounded_corner_rectangle);
        llShift.setOrientation(LinearLayout.HORIZONTAL);
        llShift.setPadding(5, 5, 5, 5);


        LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, .95f);

        if (sg.getHours(fullDate) != null)
        {
            for (int i = 0; i < llShiftHolder.getChildCount(); i++)
            {
                LinearLayout shift = (LinearLayout) llShiftHolder.getChildAt(i);
                tvShiftDisplay = (TextView) shift.getChildAt(0);
                String d = tvShiftDisplay.getText().toString();
                if (d.contains(fullDate))
                {
                    Double newHours = hours + Double.valueOf(d.substring(d.indexOf("\n"), d.length()));
                    tvShiftDisplay.setText(fullDate + "\n" + newHours);
                    break;
                }
            }
        }
        else
        {
            tvShiftDisplay = new TextView(MainActivity.this);
            tvShiftDisplay.setLayoutParams(tvParam);
            tvShiftDisplay.setText(fullDate + "\n" + hours);
            tvShiftDisplay.setTextSize(20);
            tvShiftDisplay.setGravity(Gravity.CENTER);
            tvShiftDisplay.setTextColor(Color.BLACK);
            llShift.addView(tvShiftDisplay);

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
                    final ViewGroup vg = (ViewGroup) view.getParent();

                    TextView display = (TextView) vg.getChildAt(0);
                    String displayText = display.getText().toString();
                    // Gets only the date text from display
                    String displayDate = displayText.substring(0, displayText.indexOf("\n"));

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

                    sg.removeHours(displayDate);
                }
            });

            llShift.addView(ibDelete);
            llShiftHolder.addView(llShift, 0);
            llShift.startAnimation(fadeIn);
        }

        sg.addShift(fullDate, hours);
    }

    // Prints date given by DatePickerFragment
    private void printDate()
    {
        TextView tvDate = (TextView) findViewById(R.id.tv_date);
        tvDate.setText(imageDate);
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
    private double calculateTime() throws IllegalStateException
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

        return hour + " : " + minute + amPM;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    protected void onPause()
    {
        super.onPause();

        System.out.println("MAIN Paused");
    }

    protected void onStop()
    {
        super.onStop();

        System.out.println("MAIN Stopped");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putSerializable("SG", sg);

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        sg = (ShiftGroup) savedInstanceState.getSerializable("SG");
    }

    public void onContinueButtonPressed(View v)
    {
        startActivity(new Intent(this, TimesheetActivity.class).putExtra("SG", sg));
    }

    private void setAlertDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = "";
        if (sg.toString().isEmpty())
        {
            message = "No shifts entered!";
        } else
        {
            message = "The following shifts will be entered into a time sheet of your choosing:\n\n"
                    + sg.toString();
        }

        builder.setTitle("Verify Shifts");
        builder.setMessage(message);

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                startActivity(new Intent(MainActivity.this, TimesheetActivity.class).putExtra("SG", sg));
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setPasswordDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.password_dialog, null);
        builder.setView(v);

        builder.setMessage("Enter your PatriotWeb username and password:")
                .setTitle("Save Credentials");

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                EditText user = (EditText) v.findViewById(R.id.et_user);
                EditText pass = (EditText) v.findViewById(R.id.et_pass);

                PrefUtils.saveToPrefs(MainActivity.this, "USER", user.getText().toString());
                PrefUtils.saveToPrefs(MainActivity.this, "PASS", pass.getText().toString());

                Toast.makeText(MainActivity.this, "Your login credentials have been changed", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_timesheet)
        {
            setAlertDialog();
        }

        if (id == R.id.action_password)
        {
            setPasswordDialog();
        }

        if (id == R.id.action_help)
        {
            new PopupInfo(this, R.layout.help_popup, R.id.ll_shift_holder).display();
        }

        if (id == R.id.action_about)
        {
            new PopupInfo(this, R.layout.about_popup, R.id.ll_shift_holder).display();
        }

        return super.onOptionsItemSelected(item);
    }

}
