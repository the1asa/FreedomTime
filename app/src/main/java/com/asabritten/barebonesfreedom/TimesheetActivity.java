package com.asabritten.barebonesfreedom;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Automatically logs user's shifts into PatriotWeb. User logs in, answers a security question,
 * and selects a time sheet to enter his shift into. The activity enters all shift data into the
 * PatriotWeb time sheet form using a JavaScript interface. Once completed, user will see a preview
 * of all hours entered and may choose to adjust hours and submit time sheet as usual.
 */
public class TimesheetActivity extends AppCompatActivity
{
    // Main web page (directed to PatriotWeb log in)
    private WebView page;
    // Shift information entered by user
    private static ShiftGroup sg;
    // Used to hide automated page activity
    private ProgressDialog pd;
    // Used to store dates for HTML lookup and button links for internal WebView navigation
    private ArrayList<String> dates, finalLinks;
    // When true, returns WebView to user control
    private static boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timesheet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle data = getIntent().getExtras();
        sg = (ShiftGroup) data.getSerializable("SG");

        try
        {
            dates = sg.getDates();
        } catch (NullPointerException e)
        {
            Log.e("ERROR", "ShiftGroup is NULL!");
        }

        finalLinks = new ArrayList<String>();

        if (!hasNetworkConnection())
        {
            Toast.makeText(this, "There is no network connection", Toast.LENGTH_LONG).show();
        }

        page = (WebView) findViewById(R.id.wv_page);
        page.getSettings().setJavaScriptEnabled(true);

        // Displays console.log() messages from interface
        page.setWebChromeClient(new WebChromeClient()
        {
            public boolean onConsoleMessage(ConsoleMessage cm)
            {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        pd = new ProgressDialog(TimesheetActivity.this);
        pd.setMessage("Loading...");
        pd.show();

        // Most of the program logic is in custom WebViewClient
        page.setWebViewClient(new PatriotWebViewClient());
        page.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        page.loadUrl("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_WWWLogin");
    }

    /**
     * Checks for WiFi and data connection.
     *
     * @return true if there is WiFi or data connection
     */
    private boolean hasNetworkConnection()
    {
        boolean hasWifi = false;
        boolean hasMobile = false;

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
            {
                hasWifi = ni.isConnected();
            }

            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
            {
                hasMobile = ni.isConnected();
            }
        }

        return hasWifi || hasMobile;
    }

    /**
     * Simplifies most PatriotWeb pages by removing irrelevant menus and text
     *
     * @param v the WebView with page to simplify
     */
    private void simplifyPage(WebView v)
    {
        v.loadUrl("javascript: { " +
                "document.getElementById('headerImage').style.display = 'none';" +
                "document.getElementsByClassName('headerwrapperdiv')[0].style.display = 'none';" +
                "document.getElementsByClassName('pagetitlediv')[0].style.display = 'none';" +
                "document.getElementsByClassName('infotextdiv')[0].style.display = 'none';" +
                "document.getElementsByClassName('infotextdiv')[1].style.display = 'none';" +
                "document.getElementsByClassName('pagefooterdiv')[0].style.display = 'none';" +
                "document.getElementsByClassName('banner_copyright')[0].style.display = 'none';" +
                "void(0);" +
                "};");
    }

    /**
     * Loads next page if there are more links to follow. Loading a link acts as a button press that
     * opens a page with a dialog for entering hours for a given date. If there are more links, this
     * means that there are unentered hours.
     */
    private void navigateToLinks()
    {
        Log.d("DEBUG", "NAVIGATING NOW");
        if (finalLinks.size() > 0)
        {
            String url = finalLinks.get(0);
            finalLinks.remove(0);
            page.loadUrl("https://patriotweb.gmu.edu" + url);
        } else
        {
            page.loadUrl("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_TimeSheetButtonsDriver");
        }
    }

    protected void onPause()
    {
        super.onPause();
        pd.dismiss();
    }

    protected void onStop()
    {
        super.onStop();

        finish();
    }

    /**
     * Contains the bulk of this Activity's logic. After the user selects a time sheet his hours are
     * entered automatically.
     */
    private class PatriotWebViewClient extends WebViewClient
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            if (!finished)
            {
                page.setVisibility(View.INVISIBLE);
                pd.show();
            }

            // Skip to timesheet selection
            if (url.contains("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_GenMenu"))
            {
                view.loadUrl("https://patriotweb.gmu.edu/pls/prod/bwpktais.P_SelectTimeSheetRoll");
            }
        }

        public void onPageFinished(WebView view, String url)
        {
            simplifyPage(view);

            // Log in page
            if (url.equals("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_WWWLogin") ||
                    url.equals("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_ValLogin"))
            {
                pd.hide();
                view.setVisibility(View.VISIBLE);

                String user = "";
                String pass = "";

                if (!PrefUtils.getFromPrefs(TimesheetActivity.this, "USER", "FAILED").equals("FALSE"))
                {
                    user = PrefUtils.getFromPrefs(TimesheetActivity.this, "USER", "FAILED");
                    pass = PrefUtils.getFromPrefs(TimesheetActivity.this, "PASS", "FAILED");
                }

                // Automatically fills in fields if user and pass are saved
                view.loadUrl("javascript: { " +
                        "document.getElementById('UserID').value = '" + user + "';" +
                        "document.getElementsByName('PIN')[0].value='" + pass + "';" +
                        "void(0);" +
                        "};");

            }
            // Security question page
            else if (url.equals("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_SecurityAnswer?ret_code=") ||
                    url.equals("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_ProcSecurityAnswer"))
            {
                pd.hide();
                view.setVisibility(View.VISIBLE);
            }
            // Select time sheet page
            else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpktais.P_SelectTimeSheetRoll"))
            {
                pd.hide();
                view.setVisibility(View.VISIBLE);
            }
            // First page of time sheet
            else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpkteis.P_SelectTimeSheetDriver"))
            {
                if (!finished)
                {
                    view.loadUrl("javascript: { " +
                            "HTMLOUT.processHTML(document.documentElement.outerHTML);" +
                            "var button = document.querySelectorAll(\"input[value=Next]\")[0];" +
                            "button.click();" +
                            "void(0);" +
                            "};");
                }
            }
            // Page after any button has been pressed
            else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_TimeSheetButtonsDriver"))
            {
                // final PREVIEW screen
                if (finished)
                {
                    pd.dismiss();
                    view.setVisibility(View.VISIBLE);
                } else
                {
                    view.loadUrl("javascript: { " +
                            "console.log('Button pressed');" +
                            "HTMLOUT.processHTML(document.documentElement.outerHTML);" +
                            "void(0);" +
                            "};");

                    // Hacky, but gives the HTMLOUT function time to run through Javascript
                    // interface before Java code continues execution
                    try
                    {
                        Thread.sleep(1000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    if (finalLinks.size() > 0)
                    {
                        navigateToLinks();
                    } else
                    {
                        finished = true;

                        Log.d("DEBUG", "NO LINKS FOUND!!!");

                        view.loadUrl("javascript: { " +
                                "var preview = document.querySelectorAll(\"input[value=Preview]\")[0];" +
                                "preview.click();" +
                                "void(0);" +
                                "};");
                    }
                }
            }
            // Input form for some date
            else if (url.contains("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_EnterTimeSheet?"))
            {
                if (!finished)
                {
                    Log.d("DEBUG", "INPUT LINK!");

                    String hours = "";
                    for (String d : dates)
                    {
                        if (url.contains(d))
                        {
                            hours = String.valueOf(sg.getHours(d));
                        }
                    }

                    view.loadUrl("javascript: { " +
                            "document.querySelectorAll(\"input[name=Hours]\")[0].value='" + hours + "';" +
                            "var save = document.querySelectorAll(\"input[value=Save]\")[0];" +
                            "save.click();" +
                            "void(0);" +
                            "};");
                }
            } else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_UpdateTimeSheet"))
            {
                Log.d("DEBUG", "UPDATE TIME SHEET");

                if (!finished)
                {
                    if (finalLinks.size() > 0)
                    {
                        navigateToLinks();
                    } else
                    {
                        finished = true;

                        view.loadUrl("javascript: { " +
                                "var preview = document.querySelectorAll(\"input[value=Preview]\")[0];" +
                                "preview.click();" +
                                "void(0);" +
                                "};");
                    }
                }
            }
        }
    }

    /**
     * A link is pressed to enter hours into a date. The date is contained in the link as
     * "dd-MMM-yyyy". The formatted date strings in sg are compared to the date strings contained in
     * the HTML links, if they match, this link is added to the finalLinks ArrayList. All the links
     * in finalLinks must eventually be followed to a page where the hours for this date can be
     * filled out.
     */
    private class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            Log.d("DEBUG", "PROCESSING HTML");

            int start = 0;
            int end = 0;
            String link = "";

            // Adds all the links with dates saved in SG
            while (true)
            {
                start = html.indexOf("/pls/prod/bwpktetm.P_EnterTimeSheet?", end);
                end = html.indexOf("\"", start);

                if (start > -1 && end > -1)
                {
                    link = html.substring(start, end);
                    link = link.replaceAll("amp;", "");

                    for (String d : dates)
                    {
                        if (link.contains(d))
                        {
                            finalLinks.add(link);
                        }
                    }
                } else
                {
                    break;
                }
            }
        }
    }
}
