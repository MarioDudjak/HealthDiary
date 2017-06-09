package hr.ferit.mdudjak.healthdiary;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    private static final String finalUrl = "http://www.healthline.com/rss/health-news";
    private static final String tipsUrl = "http://feeds.feedburner.com/quotationspage/qotd";
    ListView lvNews;
    List<String> links,descriptions,titles,pubDates,images;
    private HandleXML obj;
    private HandleTipsXML tipsObj;
    NewsAdapter newsAdapter;
    TextView txConnectivity,txConnectionTimeout,txNumberOfSymptomLogs, txInfoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setUpUI();
        this.setUpListView();
        this.setUpFloatingButton();
    }

    private void setUpFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(Utils.connectivity(getApplicationContext()))
        {
            tipsObj = new HandleTipsXML(tipsUrl);
            tipsObj.fetchXML();
            while(tipsObj.parsingComplete);
            if(tipsObj.getsFailedMessage().equals("OK")) {

                final int mNumberOfItems = tipsObj.getmNumberOfItems();
                final List<String> tips = tipsObj.getTips();
                final List<String> authors = tipsObj.getAuthors();
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Random r = new Random();
                        int mRandomNumber = r.nextInt(mNumberOfItems - 1);
                        Snackbar snackbar = Snackbar.make(view, tips.get(mRandomNumber) + "\n" + authors.get(mRandomNumber), Snackbar.LENGTH_LONG)
                                .setAction("Action", null);
                        final View snackbarView = snackbar.getView();
                        final TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setText(tips.get(mRandomNumber) + "\n" + authors.get(mRandomNumber));
                        tv.setHeight(250);
                        snackbar.show();
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(),R.string.connectionTimeoutMessageForTips, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            txConnectivity.setVisibility(View.VISIBLE);
            txConnectivity.setText(R.string.noConnectivityWarning);
            Toast.makeText(getApplicationContext(), "Unable to read status feed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpUI() {
        this.txNumberOfSymptomLogs= (TextView) this.findViewById(R.id.txNumberOfSymptomLogs);
        this.txInfoData= (TextView) this.findViewById(R.id.txInfoPodaci);
        this.txConnectivity = (TextView) this.findViewById(R.id.txConnectivity);
        this.txConnectionTimeout= (TextView) this.findViewById(R.id.txConnectionTimeout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        StringBuilder stringBuilder = new StringBuilder();
        Calendar calendar =Calendar.getInstance();
        String day= String.valueOf(calendar.get(Calendar.DATE));
        String month =String.valueOf(calendar.get(Calendar.MONTH));
        String year =String.valueOf(calendar.get(Calendar.YEAR));
        stringBuilder.append(day+".").append(month+".").append(year+".").append("\n");
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case 1:  stringBuilder.append("Sunday");
                break;
            case 2:  stringBuilder.append("Monday");
                break;
            case 3:  stringBuilder.append("Tuesday");
                break;
            case 4:  stringBuilder.append("Wednesday");
                break;
            case 5:  stringBuilder.append("Thursday");
                break;
            case 6:  stringBuilder.append("Friday");
                break;
            case 7:  stringBuilder.append("Saturday");
                break;
        }
        this.txInfoData.setText(String.valueOf(stringBuilder));
        List<Symptom> symptoms = DBHelper.getInstance(this).getAllSymptoms();
        int i=0,br=0;
        for(i=0;i<symptoms.size();i++){
            Symptom symptom =symptoms.get(i);
            if(symptom.getDate().equals(day)&&symptom.getMonth().equals(month)&&symptom.getYear().equals(year)){
                br++;
            }
        }
        this.txNumberOfSymptomLogs.setText(String.valueOf(br));
    }


    public void setUpListView(){
        this.lvNews = (ListView) this.findViewById(R.id.lvHealthNews);
        if(Utils.connectivity(getApplicationContext()))
        {
        obj = new HandleXML(finalUrl);
        obj.fetchXML();
        while(obj.parsingComplete);
        if(obj.getsFailedMessage().equals("OK")) {
        links=obj.getLinks();
        descriptions=obj.getDescriptions();
        titles=obj.getTitles();
        pubDates=obj.getPubDates();
        images = obj.getImages();
        int i=0;
        List<News> news = new ArrayList<>();
        for(i=2;i<titles.size();i++){
            try {
                if (titles.get(i) != null && descriptions.get(i) != null && links.get(i) != null && pubDates.get(i - 2) != null && images.get(i) != null)
                    news.add(new News(titles.get(i), descriptions.get(i - 2), links.get(i), pubDates.get(i - 2), images.get(i - 2)));
            }
            catch(Exception e){
                    Log.e("Error", String.valueOf(i));
                }
        }
        this.newsAdapter = new NewsAdapter(news);
        this.lvNews.setAdapter(this.newsAdapter);
        this.lvNews.setOnItemClickListener(this);
        }
        else{
            txConnectionTimeout.setVisibility(View.VISIBLE);
            txConnectionTimeout.setText(R.string.connectionTimeoutMessage);
        }
        }
        else
        {
            txConnectivity.setVisibility(View.VISIBLE);
            txConnectivity.setText(R.string.noConnectivityWarning);
            Toast.makeText(getApplicationContext(), "Unable to read status feed.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsAdapter adapter = (NewsAdapter) parent.getAdapter();
        News element = (News) adapter.getItem(position);
        Uri uri = Uri.parse(element.getLink());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_symptomLog) {
            intent = new Intent(this, SymptomLogActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bodyLog) {
            intent = new Intent(this, BodyLogActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_statistics) {

        } else if (id == R.id.nav_symptomHistory) {
            intent = new Intent(this, SymptomsHistory.class);
            startActivity(intent);
        } else if (id == R.id.nav_bodyHistory) {
            intent = new Intent(this, BodyLogsHistory.class);
            startActivity(intent);
        } else if (id == R.id.nav_HealthInstitutions) {
            intent = new Intent(this, HealthInstitutions.class);
            startActivity(intent);

        } else if (id == R.id.nav_TherapyReminder) {
            intent = new Intent(this,TherapyReminder.class);
            startActivity(intent);

        } else if (id == R.id.nav_CameraLog) {
            intent = new Intent(this, CameraLogsActivity.class);
            startActivity(intent);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
