package com.cyut.toolbox.toolbox.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cyut.toolbox.toolbox.ChatroomActivity;
import com.cyut.toolbox.toolbox.R;
import com.cyut.toolbox.toolbox.RecyclerViewMsgDetailHolders;
import com.cyut.toolbox.toolbox.connection.Backgorundwork;
import com.cyut.toolbox.toolbox.model.Item;
import com.cyut.toolbox.toolbox.model.ItemMsg;
import com.cyut.toolbox.toolbox.model.ItemObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;



import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class RecyclerViewAdapterMsgDetail extends RecyclerView.Adapter<RecyclerViewMsgDetailHolders> {
    public List<ItemMsg> itemList;
    private Context context;
    String name , email,uid;
    public static final String KEY = "STATUS";
    public RecyclerViewAdapterMsgDetail(Context context, List<ItemMsg> itemList,String uid) {
        this.itemList = itemList;
        this.context = context;
        this.uid=uid;
    }


    @Override
    public RecyclerViewMsgDetailHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_msg, null);
        RecyclerViewMsgDetailHolders rcv = new RecyclerViewMsgDetailHolders(layoutView);

        return rcv;
    }
    @Override
    public void onBindViewHolder(final RecyclerViewMsgDetailHolders holder, final int position) {

        name="";
        email="";

        if(itemList.get(position).getUid()!=""){
            LoadUser(itemList.get(position).getUid(),holder);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LoadUserName(itemList.get(position).getUid(),itemList.get(position).getTime(),itemList.get(position).getMessage(),itemList.get(position).getCid(),position);

            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {

                return true;
            }
        });
    }
    @Override
    public int getItemCount() {
        return this.itemList.size();
    }



    private String string_sub(String original){
        int start_index=original.indexOf("-");
        int last_index=original.lastIndexOf(":");

        return original.substring(start_index+1,last_index);
    }


    public void dialog(String time,final String name,final String mail,final String message,final String cid,final String r_uid,final String introduction,final int position){
        boolean wrapInScrollView = true;

        final View item = LayoutInflater.from(context).inflate(R.layout.choice_tool, null);

        MaterialDialog dialog =new MaterialDialog.Builder(context)
                .title("工具人申請表")
                .customView(item,wrapInScrollView)
                .backgroundColorRes(R.color.colorBackground)
                .build();
        TextView tv_name=(TextView)item.findViewById(R.id.tool_name);
        TextView tv_date=(TextView)item.findViewById(R.id.tool_date);
        TextView tv_message=(TextView)item.findViewById(R.id.tool_message);
        TextView tv_introduction=(TextView)item.findViewById(R.id.tool_introduction);
        ImageView im_choice=item.findViewById(R.id.choice_this);
        TextView cancel=item.findViewById(R.id.tool_cancel);
        tv_name.setText(name);
        tv_date.setText(string_sub(time));
        tv_message.setText(message);
        tv_introduction.setText(introduction);


        im_choice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadCase(cid,r_uid);

                Intent intent = new Intent();
                intent.setClass(context,ChatroomActivity.class);
                intent.putExtra("cid", itemList.get(position).getCid());//此方式可以放所有基本型別
                context.startActivity(intent);
               /* if (Qiscus.hasSetupUser()) {

                    Qiscus.buildChatWith(mail)
                            .build(context)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(intent -> {
                                context.startActivity(intent);
                            }, throwable -> {
                                //do anything if error occurs
                                Log.d(TAG, "onError: " + throwable);
                            });
                }
*/
                Toast.makeText(context, "成功，即將開啟聊天室", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }



    public void LoadUser(final String uid,final RecyclerViewMsgDetailHolders holder){
        String url ="http://163.17.5.182/loadusername.php";
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
                            List<Item> posts = new ArrayList<Item>();
                            if (!response.contains("Undefined")){
                                posts = Arrays.asList(mGson.fromJson(response, Item[].class));
                                List<Item> itemList=posts;

                                holder.ap_name.setText(itemList.get(0).getName());

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
                params.put("u_uid",uid);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    public void LoadUserName(final String uid,final String time,final String message,final String cid,final int position){
        String url ="http://163.17.5.182/loadusername.php";
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
                            if (!response.contains("Undefined")){
                                List<Item> posts = new ArrayList<Item>();
                                posts = Arrays.asList(mGson.fromJson(response, Item[].class));
                                List<Item> itemList=posts;
                                name= itemList.get(0).getName();
                                email=itemList.get(0).getMail();
                                String indroduction=itemList.get(0).getIntroduce();
                                dialog(time,name,email,message,cid,uid,indroduction,position);
                                Log.d(TAG, "onResponse:"+name+"/"+email);
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
                params.put("u_uid",uid);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    //讀取案件rid及money，並且更新案件進入進行中，並將錢存入第三方
    public void LoadCase(final String cid,final String r_uid){
        String url ="http://163.17.5.182/app/getcase.php";
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
                            List<ItemObject> posts = new ArrayList<ItemObject>();
                            if (!response.contains("Undefined")){
                                posts = Arrays.asList(mGson.fromJson(response, ItemObject[].class));
                                Backgorundwork backgorundwork = new Backgorundwork(context);
                                Log.d(TAG, "Load Case Success!");
                                backgorundwork.execute("deciderid",r_uid,posts.get(0).getMoney(),uid,cid);
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
                params.put("c_cid",cid);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }



}