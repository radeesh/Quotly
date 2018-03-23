package com.radibax.anibax.quotly;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    //String quoteURL="http://api.forismatic.com/api/1.0/?method=getQuote&format=json&key=&lang=en";

    String quoteText = "Default Text!", quoteAuthor, changeQuoteText=null, changeAuthorText =null;
    String c1 = "#FFFFFF",c2 = "#000000";
    int h,w,quoteSize,authorSize;
    Quote q;
    Bitmap bmp;
    float textX,textY;
    Canvas canvas;
    DynamicLayout mTextLayout,aTextLayout;
    WallpaperManager wpm;
    NetworkInfo activeNetwork;
    boolean isConnected,doInvert = false,cbBold = false, cbItalic = false;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    View mDecorView;
    int yPos,fontAlignment=1;
    DisplayMetrics metrics;
    float density,shadowRadius;
    int shadowX,shadowY;
    SeekBar fontSizeSeekBar,shadowRadiusSeekBar,shadowXSeekbar,shadowYSeekaBar;
    Switch switchAuthor;
    HorizontalScrollView horizontalSettingsView;
    String STORAGE_NAME = "com.radibax.quotly";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int quoteTypeface,aTextLayoutHeight=0;
    Boolean showAuthor;
    String quoteFont, authorFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(getWindow().FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Get density
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        fontSizeSeekBar = (SeekBar) findViewById(R.id.font_seekBar);
        shadowRadiusSeekBar = (SeekBar) findViewById(R.id.seekbar_shadow_radius);
        shadowXSeekbar= (SeekBar) findViewById(R.id.seekbar_shadow_x);
        shadowYSeekaBar= (SeekBar) findViewById(R.id.seekbar_shadow_y);

        fontSizeSeekBar.setProgress(22);
        horizontalSettingsView = (HorizontalScrollView) findViewById(R.id.settings_view);
        horizontalSettingsView.setVisibility(LinearLayout.GONE);
        //Needed if we plan to hide the systemUI that is status bar and navigation bar
        mDecorView = getWindow().getDecorView();
        hideSystemUI();
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(MainActivity.this);

        //Set Initial Shared Preferences
        quoteTypeface = sharedPreferences.getInt("com.radibax.quotly.fontface",Typeface.NORMAL);
        showAuthor = sharedPreferences.getBoolean("com.radibax.quotly.author",true);
        cbBold = sharedPreferences.getBoolean("com.radibax.quotly.bold",false);
        cbItalic = sharedPreferences.getBoolean("com.radibax.quotly.italic",false);
        fontAlignment = sharedPreferences.getInt("com.radibax.quotly.fontalignment",1);
        quoteFont = sharedPreferences.getString("com.radibax.quotly.quotefont","Fluent Sans");
        authorFont = sharedPreferences.getString("com.radibax.quotly.authorfont","Yasnas Hand");
        //Set state based on shared preferences
        //@TODO

//        //FAB Details.
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        //Delete this
//        fab.setVisibility(View.INVISIBLE);
//        fab.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                //@TODO: Write fab method here
//            }
//        });

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

        activeNetwork = cm.getActiveNetworkInfo();
        isConnected = checkConnection();

        fontSizeSeekBar.setOnSeekBarChangeListener(fontSizeSeekBarListener);
        shadowRadiusSeekBar.setOnSeekBarChangeListener(shadowRadiusSeekBarListener);
        shadowXSeekbar.setOnSeekBarChangeListener(shadowXSeekBarListener);
        shadowYSeekaBar.setOnSeekBarChangeListener(shadowYSeekBarListener);

        //get original screen size of the device
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        h = size.y;
        w = size.x;
        yPos = h/3;
        bmp = Bitmap.createBitmap(w, h, conf);
        q = new Quote(getApplicationContext());
        q.changeQuote();

        //Font Selection Code
        Spinner fontQuoteSpinner = (Spinner) findViewById(R.id.fontQuoteSpinner);
        Spinner fontAuthorSpinner = (Spinner) findViewById(R.id.fontAuthorSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.font_arrays, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontQuoteSpinner.setAdapter(adapter);
        //fontQuoteSpinner.setSelection(11);
        fontQuoteSpinner.setSelection (adapter.getPosition(quoteFont));
        fontQuoteSpinner.setOnItemSelectedListener(fontChangeListener);
        fontAuthorSpinner.setAdapter(adapter);
        //fontAuthorSpinner.setSelection(31);
        fontAuthorSpinner.setSelection (adapter.getPosition(authorFont));
        fontAuthorSpinner.setOnItemSelectedListener(fontChangeListener);

        hideExtendedControls();
    }
    @Override
    public void onResume() {

        super.onResume();  // Always call the superclass method first
        hideSystemUI();
    }

    public void hideExtendedControls()
    {
        //Turn off extended controls
        LinearLayout layoutFontSize = (LinearLayout) findViewById(R.id.layout_fontsize);
        layoutFontSize.setVisibility(View.GONE);
        LinearLayout layoutShadowControls = (LinearLayout) findViewById(R.id.layout_shadow);
        layoutShadowControls.setVisibility(View.GONE);
        LinearLayout layoutFontControls = (LinearLayout) findViewById(R.id.layout_fontselect);
        layoutFontControls.setVisibility(View.GONE);
    }
    public void renderQuote (Boolean flag) {
        showAuthor = sharedPreferences.getBoolean("com.radibax.quotly.author",true);
        mSurfaceHolder = mSurfaceView.getHolder();
        canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) {
            Log.e("Error", "Cannot draw onto the canvas as it's null");
        } else {
            Canvas bmpCanvas = new Canvas(bmp);
            if(doInvert == false)
            {
                c1 = q.getTextColor();
                c2 = q.getBackgroundColor();
            }
            else
            {
                c2 = q.getTextColor();
                c1 = q.getBackgroundColor();
            }
            getWindow().setStatusBarColor(Color.parseColor(c2));
            getWindow().setNavigationBarColor(Color.parseColor(c2));
            bmpCanvas.drawColor(Color.parseColor(c2));
            //Get minimum quote size from resources
            quoteSize = fontSizeSeekBar.getProgress() + getResources().getDimensionPixelSize(R.dimen.quote_size);
            //quoteSize = (int) ((fontSizeSeekBar.getProgress()+quoteSize)*density + 0.5f);
            authorSize = getResources().getDimensionPixelSize(R.dimen.author_size);
            if (changeQuoteText == null) {
                do {
                    quoteText = q.getQuoteText();
                } while (quoteText == null);
                quoteAuthor = q.getQuoteAuthor();
            }
            else {
                quoteText = changeQuoteText;
                quoteAuthor = changeAuthorText;
            }
            TextPaint mTextPaint = new TextPaint();
            Spinner quoteFontSpinner = (Spinner) findViewById(R.id.fontQuoteSpinner);
            String quoteFont = getFont(quoteFontSpinner);
            Spinner authorFontSpinner = (Spinner) findViewById(R.id.fontAuthorSpinner);
            String authorFont = getFont(authorFontSpinner);
            editor.putString("com.radibax.quotly.quotefont",quoteFontSpinner.getSelectedItem().toString());
            editor.putString("com.radibax.quotly.authorfont",authorFontSpinner.getSelectedItem().toString());
            editor.commit();
            Typeface afont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/"+authorFont);
            Typeface qfont = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/"+quoteFont);
            mTextPaint.setColor(Color.parseColor(c1));
            mTextPaint.setTextSize(quoteSize);
            ImageButton btnTypeFace = (ImageButton) findViewById(R.id.btnTypeface);
            switch(quoteTypeface){
                case Typeface.NORMAL:
                    btnTypeFace.setBackgroundResource(R.drawable.ic_action_regular);
                    break;
                case Typeface.BOLD:
                    btnTypeFace.setBackgroundResource(R.drawable.ic_action_bold);
                    break;
                case Typeface.ITALIC:
                    btnTypeFace.setBackgroundResource(R.drawable.ic_action_italic);
                    break;
                case Typeface.BOLD_ITALIC:
                    btnTypeFace.setBackgroundResource(R.drawable.ic_action_bolditalic);
                    break;
            }
            mTextPaint.setTypeface(Typeface.create(qfont, quoteTypeface));
            textX = 0;
            textY = h * 0.20f;
            int xPos = (bmpCanvas.getWidth() / 2);
            ImageButton btnAlignment = (ImageButton) findViewById(R.id.btnAlignment);
            switch(fontAlignment){
                case 1:
                    mTextPaint.setTextAlign(Paint.Align.CENTER);
                    btnAlignment.setBackgroundResource(R.drawable.ic_action_quote_alignment);
                    mTextLayout = new DynamicLayout(quoteText, mTextPaint, (int) (bmpCanvas.getWidth()-w*0.10f), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    break;
                case 2:
                    xPos = (int) (w*0.02f);
                    mTextPaint.setTextAlign(Paint.Align.LEFT);
                    btnAlignment.setBackgroundResource(R.drawable.ic_action_left_align);
                    mTextLayout = new DynamicLayout(quoteText, mTextPaint, (int) (bmpCanvas.getWidth()-w*0.10f), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    break;
                case 3:
                    xPos = (int) (bmpCanvas.getWidth()-w*0.02f);
                    mTextPaint.setTextAlign(Paint.Align.RIGHT);
                    btnAlignment.setBackgroundResource(R.drawable.ic_action_right_align);
                    mTextLayout = new DynamicLayout(quoteText, mTextPaint, (int) (bmpCanvas.getWidth()-w*0.10f), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    break;
            }
            ImageButton btnShowAuthor = (ImageButton) findViewById(R.id.btnAuthorToggle);
            if (showAuthor) {
                TextPaint authorTextPaint = new TextPaint();
                authorTextPaint.setColor(Color.parseColor(c1));
                authorTextPaint.setTextSize(authorSize);
                authorTextPaint.setTypeface(Typeface.create(afont, Typeface.NORMAL));
                aTextLayout = new DynamicLayout(quoteAuthor, authorTextPaint, bmpCanvas.getWidth(), Layout.Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, true);
                aTextLayoutHeight = aTextLayout.getHeight();
                btnShowAuthor.setBackgroundResource(R.drawable.ic_action_author);
            }
            else {
                btnShowAuthor.setBackgroundResource(R.drawable.ic_action_author_off);
            }

            bmpCanvas.save();
            // calculate x and y position where your text will be placed
            int authorPosX = (int) (-w*0.02f);
            if(flag==true) {
                yPos = yPos - (mTextLayout.getHeight()/2 + aTextLayoutHeight);
            }
            bmpCanvas.translate(xPos, yPos);
            mTextPaint.setShadowLayer(shadowRadius, shadowX, shadowY, R.color.colorTranslucentBlack );
            mTextLayout.draw(bmpCanvas);
            bmpCanvas.restore();
            // calculate x and y position where your text will be placed
            if (showAuthor) {
                bmpCanvas.translate(authorPosX, yPos+mTextLayout.getHeight());
                aTextLayout.draw(bmpCanvas);
            }
            canvas.drawBitmap(bmp, 0, 0,null);


        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        mSurfaceView.getDrawingCache();
    }

    public String getFont(Spinner spinner){
        String quoteFont;
        switch (spinner.getSelectedItem().toString()){
            case "Aclonica":
                quoteFont = "Aclonica.ttf";
                break;
            case "Bahiana":
                quoteFont = "Bahiana-Regular.ttf";
                break;
            case "Barrio":
                quoteFont = "Barrio-Regular.ttf";
                break;
            case "Bowlby One":
                quoteFont = "BowlbyOne-Regular.ttf";
                break;
            case "Bungee Hairline":
                quoteFont = "BungeeHairline-Regular.ttf";
                break;
            case "Cabin Sketch":
                quoteFont = "CabinSketch-Regular.ttf";
                break;
            case "Cinzel Decorative":
                quoteFont = "CinzelDecorative-Regular.ttf";
                break;
            case "Codystar":
                quoteFont = "Codystar-Regular.ttf";
                break;
            case "Comili":
                quoteFont = "Comili.otf";
                break;
            case "Doodle":
                quoteFont = "Doodle.ttf";
                break;
            case "Finger Paint":
                quoteFont = "FingerPaint.ttf";
                break;
            case "Fontdiner Swanky":
                quoteFont = "FontdinerSwanky.ttf";
                break;
            case "Frijole":
                quoteFont = "Frijole-Regular.ttf";
                break;
            case "Inconsolata":
                quoteFont = "Inconsolata-Regular.ttf";
                break;
            case "Josefin Slab":
                quoteFont = "JosefinSlab-Regular.ttf";
                break;
            case "Kaushan Script":
                quoteFont = "KaushanScript-Regular.ttf";
                break;
            case "Lora":
                quoteFont = "Lora-Regular.ttf";
                break;
            case "Monoton":
                quoteFont = "Monoton-Regular.ttf";
                break;
            case "Montserrat Subrayada":
                quoteFont = "MontserratSubrayada-Regular.ttf";
                break;
            case "Orbitron":
                quoteFont = "Orbitron-Regular.ttf";
                break;
            case "Pacifico":
                quoteFont = "Pacifico-Regular.ttf";
                break;
            case "Permanent Marker":
                quoteFont = "PermanentMarker.ttf";
                break;
            case "PoiretOne":
                quoteFont = "PoiretOne-Regular.ttf";
                break;
            case "Raleway":
                quoteFont = "Raleway-Regular.ttf";
                break;
            case "Sacramento":
                quoteFont = "Sacramento-Regular.ttf";
                break;
            case "Sansita":
                quoteFont = "Sansita-Regular.ttf";
                break;
            case "Ley":
                quoteFont = "Ley.ttf";
                break;
            case "Satisfy":
                quoteFont = "Satisfy-Regular.ttf";
                break;
            case "Special Elite":
                quoteFont = "SpecialElite.ttf";
                break;
            case "VT323":
                quoteFont = "VT323-Regular.ttf";
                break;
            case "Waiting for the Sunrise":
                quoteFont = "WaitingfortheSunrise.ttf";
                break;
            case "Yasnas Hand":
                quoteFont = "YasnasHand.ttf";
                break;
            case "Fluent Sans":
            default:
                quoteFont = "FluentSans.otf";

        }
        return quoteFont;
    }
    public boolean checkConnection(){
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    public void onTypeFaceClicked(View view){
        ImageButton btnTypeFace = (ImageButton) findViewById(R.id.btnTypeface);
        switch(quoteTypeface) {
            case Typeface.NORMAL:
                quoteTypeface = Typeface.BOLD;
                btnTypeFace.setBackgroundResource(R.drawable.ic_action_bold);
                break;
            case Typeface.BOLD:
                quoteTypeface = Typeface.ITALIC;
                btnTypeFace.setBackgroundResource(R.drawable.ic_action_italic);
                break;
            case Typeface.ITALIC:
                quoteTypeface = Typeface.BOLD_ITALIC;
                btnTypeFace.setBackgroundResource(R.drawable.ic_action_bolditalic);
                break;
            case Typeface.BOLD_ITALIC:
                quoteTypeface = Typeface.NORMAL;
                btnTypeFace.setBackgroundResource(R.drawable.ic_action_regular);
                break;
        }
        editor.putInt("com.radibax.quotly.fontface",quoteTypeface);
        editor.commit();
        renderQuote(false);
    }

    public void onFontSizeClicked(View view){
        hideExtendedControls();
        LinearLayout layoutFontSize = (LinearLayout) findViewById(R.id.layout_fontsize);
        if(layoutFontSize.getVisibility()==View.GONE) {
            layoutFontSize.setVisibility(View.VISIBLE);
        }
        else
            layoutFontSize.setVisibility(View.GONE);

    }

    public void onShadowClicked(View view){
        hideExtendedControls();
        LinearLayout layoutShadowControls = (LinearLayout) findViewById(R.id.layout_shadow);
        if(layoutShadowControls.getVisibility()==View.GONE) {
            view.animate().alpha(1.0f).setDuration(500);
            layoutShadowControls.setVisibility(View.VISIBLE);
        }
        else if(layoutShadowControls.getVisibility()==View.VISIBLE) {
            view.animate().alpha(1.0f).setDuration(500);
            layoutShadowControls.setVisibility(View.GONE);
        }
    }

    public void onFontSelectClicked(View view){
        hideExtendedControls();
        LinearLayout layoutFontControls = (LinearLayout) findViewById(R.id.layout_fontselect);
        if(layoutFontControls.getVisibility()==View.GONE) {
            view.animate().alpha(1.0f).setDuration(500);
            layoutFontControls.setVisibility(View.VISIBLE);
        }
        else if(layoutFontControls.getVisibility()==View.VISIBLE) {
            view.animate().alpha(1.0f).setDuration(500);
            layoutFontControls.setVisibility(View.GONE);
        }
    }

    public void onFontAlignmentClicked(View view){
        if(fontAlignment!=3)
        {
            fontAlignment+=1;
        }
        else
            fontAlignment=1;
        editor.putInt("com.radibax.quotly.fontalignment",fontAlignment);
        editor.commit();
        renderQuote(false);
    }

    public void onEditTextClicked(View view){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.edit_text, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.EditTextDialog));
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditQuoteText = (EditText) mView.findViewById(R.id.editQuoteText);
        final EditText userInputDialogEditAuthorText = (EditText) mView.findViewById(R.id.editAuthorText);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        hideSystemUI();
                        changeQuoteText = userInputDialogEditQuoteText.getText().toString();
                        changeAuthorText = userInputDialogEditAuthorText.getText().toString();
                        renderQuote(false);
                    }
                })

                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                hideSystemUI();
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

    public void onAuthorClicked(View view){
        Boolean authorFlag = sharedPreferences.getBoolean("com.radibax.quotly.author", true);
        if(authorFlag)
            editor.putBoolean("com.radibax.quotly.author",false);
        else
            editor.putBoolean("com.radibax.quotly.author",true);
        editor.commit();
        renderQuote(false);
    }
    public void onClick(View v) {
        Log.i("id", String.valueOf(v.getId()));
        switch (v.getId()){
            case R.id.action_set_wallpaper:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Are you sure you want to set this as your wallpaper?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                wpm = WallpaperManager.getInstance(MainActivity.this);
                                try {
                                    wpm.setBitmap(bmp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(MainActivity.this,
                                        "Wallpaper successfully changed", Toast.LENGTH_SHORT)
                                        .show();
                                hideSystemUI();
                            }
                        });
                alertDialogBuilder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                hideSystemUI();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;
            case R.id.action_invert_color:
                if(doInvert==false)
                {
                    doInvert=true;
                }
                else doInvert=false;
                renderQuote(false);
                break;
            case R.id.action_change_color:
                q.changeColors();
                renderQuote(false);
                break;
            case R.id.action_change_quote:
                changeQuoteText = null;
                changeAuthorText = null;
                q.changeQuote();
                renderQuote(false);
                break;
            case R.id.action_settings:
                if(horizontalSettingsView.getVisibility()==View.GONE)
                    horizontalSettingsView.setVisibility(View.VISIBLE);
                else if(horizontalSettingsView.getVisibility()==View.VISIBLE) {
                    horizontalSettingsView.setVisibility(View.GONE);
                    hideExtendedControls();
                }
                break;

        }
    }

    private Spinner.OnItemSelectedListener fontChangeListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    renderQuote(false);
                    hideSystemUI();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    hideSystemUI();
                }
            };
    private SeekBar.OnSeekBarChangeListener fontSizeSeekBarListener=
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    quoteSize=progress;
                    renderQuote(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

    private SeekBar.OnSeekBarChangeListener shadowRadiusSeekBarListener=
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    shadowRadius=progress;
                    renderQuote(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

    private SeekBar.OnSeekBarChangeListener shadowXSeekBarListener=
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    shadowX=progress-30;
                    renderQuote(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

    private SeekBar.OnSeekBarChangeListener shadowYSeekBarListener=
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    shadowY=progress-30;
                    renderQuote(false);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                yPos = (int) motionEvent.getY();
                renderQuote(true);
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        renderQuote(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
        renderQuote(false);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}