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

public class TimesheetActivity extends AppCompatActivity
{

    private WebView page;
    private static ShiftGroup sg;
    private ProgressDialog pd;
    private ArrayList<String> links = new ArrayList<String>();
    private ArrayList<String> dates;
    private ArrayList<String> finalLinks;
    private boolean finished;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timesheet);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle data = getIntent().getExtras();
        sg = (ShiftGroup) data.getSerializable("SG");
        dates = sg.getDates();
        finalLinks = new ArrayList<String>();

        if (!hasNetworkConnection())
        {
            Toast.makeText(this, "There is no network connection", Toast.LENGTH_LONG).show();
        }

        page = (WebView) findViewById(R.id.wv_page);
        page.getSettings().setJavaScriptEnabled(true);

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

        page.setWebViewClient(new WebViewClient()
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

                    simplifyPage(view);

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
                    simplifyPage(view);

                    pd.hide();
                    view.setVisibility(View.VISIBLE);
                }
                // Select time sheet page
                else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpktais.P_SelectTimeSheetRoll"))
                {
                    simplifyPage(view);

                    pd.hide();
                    view.setVisibility(View.VISIBLE);
                }
                // First page of time sheet
                else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpkteis.P_SelectTimeSheetDriver"))
                {
                    if (!finished)
                    {
                        view.loadUrl("javascript: { " +
                                // Finds and adds links
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
                        simplifyPage(view);

                        pd.dismiss();
                        view.setVisibility(View.VISIBLE);

                    } else
                    {
                        view.loadUrl("javascript: { " +
                                //Finds and adds links
                                "console.log('Button pressed');" +
                                "HTMLOUT.processHTML(document.documentElement.outerHTML);" +
                                "void(0);" +
                                "};");

                        // Hacky, but gives the HTMLOUT function time to run through javascript interface
                        // before Java code beats it
                        try
                        {
                            Thread.sleep(1000);
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }

                        // All links collected so start navigation 'loop'
                        System.out.println("After second page finalLinks.size(): " + finalLinks.size());

                        if (finalLinks.size() > 0)
                        {
                            navigateToLinks();
                        } else
                        {
                            finished = true;

                            System.out.println("NO LINKS FOUND!!!");

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
                        System.out.println("INPUT LINK!");

                        String hours = "";
                        for (String d : dates)
                        {
                            if (url.contains(d))
                            {
                                hours = String.valueOf(sg.getHours(d));
                                System.out.println("Hours: " + hours);
                            }
                        }

                        view.loadUrl("javascript: { " +
                                "document.querySelectorAll(\"input[name=Hours]\")[0].value='" + hours + "';" +
                                "var save = document.querySelectorAll(\"input[value=Save]\")[0];" +
                                "save.click();" +
                                "void(0);" +
                                "};");
                    } else
                    {
                        simplifyPage(view);
                    }
                } else if (url.equals("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_UpdateTimeSheet"))
                {
                    System.out.println("UPDATE TIME SHEET");

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
                    } else
                    {
                        simplifyPage(view);
                    }
                }
            }
        });

        page.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        page.loadUrl("https://patriotweb.gmu.edu/pls/prod/twbkwbis.P_WWWLogin");
    }

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

    private void navigateToLinks()
    {
        System.out.println("NAVIGATING NOW");
        if (finalLinks.size() > 0)
        {
            String url = finalLinks.get(0);
            System.out.println("NAVIGATING TO " + "https://patriotweb.gmu.edu" + url);
            finalLinks.remove(0);
            page.loadUrl("https://patriotweb.gmu.edu" + url);
        } else
        {
            System.out.println("NAVIGATING TO https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_TimeSheetButtonsDriver\n" + "This won't work");
            page.loadUrl("https://patriotweb.gmu.edu/pls/prod/bwpktetm.P_TimeSheetButtonsDriver");
        }
    }

    protected void onPause()
    {
        super.onPause();

        System.out.println("Paused");
        pd.dismiss();
    }

    protected void onStop()
    {
        super.onStop();

        System.out.println("STOPPED");
        finish();
    }

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html)
        {
            System.out.println("PARSING HTML");

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

            System.out.println("dates: " + dates);
            System.out.println("finalLinks: " + finalLinks);
            System.out.println("SG: " + sg.toString());
        }
    }

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

    public void onBack(View v)
    {
        onBackPressed();
    }
}
