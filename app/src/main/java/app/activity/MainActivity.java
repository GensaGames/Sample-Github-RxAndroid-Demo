package app.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import app.Data;
import app.adapter.CardAdapter;
import app.model.Github;
import app.service.GithubService;
import app.service.ServiceFactory;
import com.example.githubdemo.app.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String LOG = "Genka";
    public final static long TIME_DEBOUNCE = 500;
    CardAdapter mCardAdapter;
    RecyclerView mRecyclerView;
    GithubService service;

    Button bClear, bFind;
    EditText searchEdit;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG, " OnPause .... ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG, " onResume .... ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * INIT: Initialize GitHub Service
         */

        Log.d(LOG, " OnCreate .... ");

        service = ServiceFactory.createRetrofitService(
                GithubService.class, GithubService.SERVICE_ENDPOINT);

        initView();

        /**
         *  INIT: Set up Android CardView/RecycleView
         */

        initCardView();


        /**
         * START: Set Default Users Information
         */

        setDefaultUsers();


        /**
         * WORK: Set RxEvent to EditText
         */

        searchObservable();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_clear:
                mCardAdapter.clear();
                break;

            case R.id.button_fetch:
                setDefaultUsers();
                break;
        }
    }

    private void searchObservable () {

        WidgetObservable.text(searchEdit)
                .filter(new Func1<OnTextChangeEvent, Boolean>() {
                    @Override
                    public Boolean call(OnTextChangeEvent onTextChangeEvent) {
                        Log.d(LOG, " Text is: " + onTextChangeEvent.text().toString());
                        return searchEdit.getText().length() > 3;
                    }
                })
                .flatMap(new Func1<OnTextChangeEvent, Observable<Github>>() {
                    @Override
                    public Observable<Github> call(OnTextChangeEvent onTextChangeEvent) {
                        return service.getUserRx(onTextChangeEvent.text().toString());
                    }
                })
                .retry()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Github>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(LOG, " Error is: " + e.toString());
                    }

                    @Override
                    public void onNext(Github github) {
                        mCardAdapter.clear();
                        mCardAdapter.notifyDataSetChanged();
                        Log.d(LOG, "   Response is: " + github.getLogin());
                        mCardAdapter.addData(github);
                    }
                });

    }

    private void createPopUp () {
        Log.d(LOG, " --- Creating Popup Window ---- ");
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_window, null);

        String[] names = { "..." };

        ListView popupListView = (ListView)popupView.findViewById(R.id.popup_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, names);

        popupListView.setAdapter(adapter);

        final PopupWindow popupWindow = new PopupWindow(
                popupView, searchEdit.getWidth(), LayoutParams.WRAP_CONTENT);

        popupWindow.showAsDropDown(searchEdit);
    }

    private void initCardView () {
        Log.d(LOG, " --- Setting Default CardView ---- ");
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCardAdapter = new CardAdapter();
        mRecyclerView.setAdapter(mCardAdapter);

        mCardAdapter.setOnItemClickListener(new CardAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Toast.makeText(getBaseContext(), "Position View: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView () {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        bClear = (Button) findViewById(R.id.button_clear);
        bFind = (Button) findViewById(R.id.button_fetch);
        searchEdit = (EditText) findViewById(R.id.find_edit_text);
    }

    private void setDefaultUsers () {
        Log.d(LOG, " --- Setting default Github users ---- ");
        for(String login : Data.githubList) {
            service.getUserRx(login)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .cache()
                    .subscribe(new Subscriber<Github>() {
                        @Override
                        public final void onCompleted() {
                        }

                        @Override
                        public final void onError(Throwable e) {
                            Log.e(LOG, " ErrorRx Default Github" + e.getMessage());
                        }

                        @Override
                        public final void onNext(Github response) {
                            mCardAdapter.addData(response);
                        }
                    });
        }
    }


}
