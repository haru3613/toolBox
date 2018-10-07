package com.cyut.toolbox.toolbox.Fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cyut.toolbox.toolbox.HideShowScrollListener;
import com.cyut.toolbox.toolbox.MainActivity;
import com.cyut.toolbox.toolbox.R;
import com.cyut.toolbox.toolbox.adapter.RecyclerViewAdapter;
import com.cyut.toolbox.toolbox.adapter.RecyclerViewAdapterCategory;
import com.cyut.toolbox.toolbox.RecylerItemClickListener;
import com.cyut.toolbox.toolbox.SimpleDividerItemDecoration;
import com.cyut.toolbox.toolbox.adddata;
import com.cyut.toolbox.toolbox.model.ItemObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment  implements SearchView.OnQueryTextListener{

    private RecyclerView recyclerView,categoryView;
    private LinearLayoutManager layoutManager;
    protected static RecyclerViewAdapter adapter;
    private RecyclerViewAdapterCategory categoryadapter;
    public static final String KEY = "STATUS";
    final String[] items = {"我同意契約書","我會當一個稱職的工具人"};
    final boolean[] checked= new boolean[]{
            false, // 同意契約書
            false, // 同意稱職
    };
    final List<String> seletedItems = Arrays.asList(items);
    private View view;
    String SearchString,uid;
    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // What i have added is this
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v =inflater.inflate(R.layout.fragment_main, container, false);
        uid="";
        view=v;
        recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(v.getContext()));
        layoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(KEY, MODE_PRIVATE);
        uid=sharedPreferences.getString("uid",null);
        Log.d(TAG, "onCreateView: uid "+uid);
        requestJsonObject(v);

        ArrayList<String> categorylist=new ArrayList<String>();

        categorylist.add("日常");
        categorylist.add("接送");
        categorylist.add("外送");
        categorylist.add("課業");
        categorylist.add("修繕");
        categorylist.add("除蟲");


        LinearLayoutManager categoryManager
                = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);

        categoryView = (RecyclerView) v.findViewById(R.id.category);
        categoryView.setBackgroundColor(Color.parseColor("#72c6cc"));
        categoryView.setLayoutManager(categoryManager);
        Log.d(TAG, "list: "+categorylist);
        categoryadapter=new RecyclerViewAdapterCategory(v.getContext(),categorylist);
        categoryView.setAdapter(categoryadapter);
        categoryView.setNestedScrollingEnabled(false);
        categoryView.addOnItemTouchListener(new RecylerItemClickListener(v.getContext(), categoryView, new RecylerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SearchV(categoryadapter.itemList.get(position).substring(0,2));
                categoryadapter.setAlpha(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // ...
            }
        }));



        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                addcase_notic(v);

            }
        });




        recyclerView.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
            }

            @Override
            public void onShow() {
            }
            @Override
            public void onScrolled() {
                // To load more data
            }
        });


        return v;
    }
    private void requestJsonObject(final View v){
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        String url ="http://163.17.5.182/SelectCase.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    byte[] u = fromHtml(response).toString().getBytes(
                            "UTF-8");
                    response = new String(u, "UTF-8");
                    Log.d(TAG, "Response " + response);
                    GsonBuilder builder = new GsonBuilder();
                    Gson mGson = builder.create();
                    Type listType = new TypeToken<ArrayList<ItemObject>>() {}.getType();
                    ArrayList<ItemObject> posts = new ArrayList<ItemObject>();
                    if (!response.contains("Undefined")) {
                        posts = mGson.fromJson(response, listType);
                    }
                    if (posts.isEmpty()){
                        Toast.makeText(view.getContext(),"尚未有人發案",Toast.LENGTH_SHORT).show();
                    }else{
                        adapter = new RecyclerViewAdapter(v.getContext(), posts, uid);
                        recyclerView.setAdapter(adapter);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }

    public static Spanned fromHtml(String html) {
        html = (html.replace("&lt;", "<").replace("&gt;", ">"));
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html.trim(), Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html.trim());
        }
        return result;
    }

    //Reload Fragment
    public void Reload(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: !");
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView sv = new SearchView(((MainActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        sv.setOnQueryTextListener(this);
        sv.setIconifiedByDefault(false);
        sv.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Reload();
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {

                return true;  // Return true to expand action view
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SearchString=item.toString();
        switch (item.getItemId()) {
            case R.id.refresh:
                SearchV(SearchString);
                return true;
            case R.id.nav_1:
                SearchV(SearchString);
                Log.d(TAG, item.toString());
                return true;
            case R.id.nav_2:
                SearchV(SearchString);
                return true;
            case R.id.nav_3:
                SearchV(SearchString);
                return true;
            case R.id.nav_4:
                SearchV(SearchString);
                return true;
            case R.id.nav_5:
                SearchV(SearchString);
                return true;
            case R.id.nav_6:
                SearchV(SearchString);
                return true;
            case R.id.nav_7:
                requestJsonObject(view);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);

    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        SearchQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(!newText.equals("")){
            //TODO 字串篩選
        }
        return false;
    }

    public void SearchV(final String SearchString){
        String url ="http://163.17.5.182/search.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response:",response);
                        try {
                            byte[] u = response.getBytes(
                                    "UTF-8");
                            response = new String(u, "UTF-8");
                            Log.d(TAG, "Response " + response);
                            GsonBuilder builder = new GsonBuilder();
                            Gson mGson = builder.create();
                            Type listType = new TypeToken<ArrayList<ItemObject>>() {}.getType();
                            ArrayList<ItemObject> posts = new ArrayList<ItemObject>();
                            if (!response.contains("Undefined")) {
                                posts = mGson.fromJson(response, listType);
                            }
                            if (posts.isEmpty()){
                                Toast.makeText(view.getContext(),"尚未有此類型的案件",Toast.LENGTH_SHORT).show();
                            }else{
                                adapter = new RecyclerViewAdapter(view.getContext(), posts,uid);
                                recyclerView.setAdapter(adapter);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do stuffs with response erroe
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("search",SearchString);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(view.getContext());
        requestQueue.add(stringRequest);
    }

    public void addcase_notic(final View view) {
        MaterialDialog.Builder dialog_notice = new MaterialDialog.Builder(view.getContext());
        dialog_notice.title("與工具人的契約");
        dialog_notice.content("一、禁止欺騙以及傷害雇主，以效忠雇主為旨。\n\n"+"       "+"例:\n"+"       "+"雇主:");
        dialog_notice.positiveText("我肯定");
        dialog_notice.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(MaterialDialog dialog, DialogAction which) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(),adddata.class);
                startActivity(intent);
            }
        });

        dialog_notice.show();
    }

    public void SearchQuery(final String SearchString){
        String url ="http://163.17.5.182/querysearch.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response:",response);
                        try {
                            byte[] u = response.getBytes(
                                    "UTF-8");
                            response = new String(u, "UTF-8");
                            Log.d(TAG, "Response " + response);
                            GsonBuilder builder = new GsonBuilder();
                            Gson mGson = builder.create();
                            Type listType = new TypeToken<ArrayList<ItemObject>>() {}.getType();
                            ArrayList<ItemObject> posts = new ArrayList<ItemObject>();
                            if (!response.contains("Undefined")) {
                                posts = mGson.fromJson(response, listType);
                                adapter = new RecyclerViewAdapter(view.getContext(), posts,uid);
                                recyclerView.setAdapter(adapter);
                            }else{
                                Toast.makeText(getContext(),"沒有搜尋到相關案件",Toast.LENGTH_SHORT).show();
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do stuffs with response erroe
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("search",SearchString);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(view.getContext());
        requestQueue.add(stringRequest);
    }


}