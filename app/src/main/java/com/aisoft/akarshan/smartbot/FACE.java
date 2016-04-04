package com.aisoft.akarshan.smartbot;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.StrictMode.*;

public class FACE extends AppCompatActivity {
    TextView ip;
    WebView op;
    String botname = "ultron";
    Chat chatSession;
    Bot bot;
    String baseDir;
    TextToSpeech ts;
    protected static final int RESULT_SPEECH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
        setThreadPolicy(policy);
       final String googleTtsPackage = "com.google.android.tts";



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    op.loadData("","","");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }


                ip.setText("");

            }
        });

        ip = (TextView) findViewById(R.id.inputtext);
        op = (WebView) findViewById(R.id.responce);


        ts= new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                    if(status != TextToSpeech.ERROR) {

                         ts.setEngineByPackageName(googleTtsPackage);
                    }
                else
                         Toast.makeText(getApplicationContext(),"Please install Google text to speech available in different languages from Google play store or else i cannot talk",Toast.LENGTH_LONG).show();

            }
        });
        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            File baseDirFile = getExternalFilesDir(null);
            if(baseDirFile == null) {
                baseDir = getFilesDir().getAbsolutePath();
            } else {
                baseDir = baseDirFile.getAbsolutePath();
            }
        } else {
            baseDir = getFilesDir().getAbsolutePath();
        }
        File fileExt = new File(baseDir+ "/bots");
        if (!fileExt.exists()) {
            ZipFileExtraction extract = new ZipFileExtraction();
            try {
                extract.unZipIt(getAssets().open("bots.zip"),baseDir + "/");
                Log.i("InitSMARTBOT", "Extracted bots folder");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        //path = baseDir;
        bot = new Bot(botname, baseDir);
        chatSession = new Chat(bot);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_face, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Application developed by Akarshan Biswas. For upcoming features go to http://www.facebook.com/attract.akarshan. Please support this project on git.",
                    Toast.LENGTH_LONG);
            t.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    ip.setText(text.get(0));
                    String i = ip.getText().toString();

                    String r = chatSession.multisentenceRespond(i);

                    op.getSettings().setJavaScriptEnabled(true);

                    //Todo : find substring in ip in a string r

                    op.loadDataWithBaseURL(null, r, "text/html", "utf-8", null);
                    ts.speak(stripHtml(r), TextToSpeech.QUEUE_FLUSH, null, null);
                    if (!(pullLinks(stripHtml(r)).equals(""))) {

                        op.setWebViewClient(new WebViewClient());
                        op.setWebChromeClient(new WebChromeClient() {
                        });
                        op.loadUrl(pullLinks(r));
                        ip.setText("");
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ts.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ts.stop();
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }
    private String pullLinks(String text) {
        String links="";

        String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);
        while(m.find()) {
            String urlStr = m.group();
            if (urlStr.startsWith("(") && urlStr.endsWith(")"))
            {
                urlStr = urlStr.substring(1, urlStr.length() - 1);
            }
            links=urlStr;
        }
        return links;
    }
}
