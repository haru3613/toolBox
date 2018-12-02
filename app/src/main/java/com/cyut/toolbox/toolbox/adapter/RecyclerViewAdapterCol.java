package com.cyut.toolbox.toolbox.adapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cyut.toolbox.toolbox.R;
import com.cyut.toolbox.toolbox.RecyclerViewHolders;
import com.cyut.toolbox.toolbox.RecyclerViewHoldersColl;
import com.cyut.toolbox.toolbox.connection.Backgorundwork;
import com.cyut.toolbox.toolbox.model.ItemObject;
import com.cyut.toolbox.toolbox.model.ItemQanda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class RecyclerViewAdapterCol extends RecyclerView.Adapter<RecyclerViewHoldersColl> {
    public ArrayList<ItemObject> itemList ;
    private String uid;
    private Context context;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerViewAdapterQanda adapter;

    int c_end_hours = 0;
    int c_end_mins = 0;
    int time_check_status = 0;
    int end_Minute = 0;
    int end_Hours = 0;
    int end_Day = 0;
    int end_Way = 0;
    private static MaterialDialog dialog;
    String message;
    private int mExpandedPosition=-1,previousExpandedPosition = -1;

    public RecyclerViewAdapterCol() {

    }
    public RecyclerViewAdapterCol(Context context, ArrayList<ItemObject> itemList,String uid) {
        this.itemList = itemList;
        this.context = context;
        this.uid=uid;
    }

    @Override
    public RecyclerViewHoldersColl onCreateViewHolder(ViewGroup parent, int viewType) {
        final View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_collection, null,false);
        layoutView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        RecyclerViewHoldersColl rcv = new RecyclerViewHoldersColl(layoutView);
        return rcv;
    }
    @Override
    public void onBindViewHolder(final RecyclerViewHoldersColl holder, final int position) {


        switch (itemList.get(position).getCategoryImage()) {
            case "日常":
                holder.CategoryImage.setImageResource(R.drawable.life);
                break;
            case "接送":
                holder.CategoryImage.setImageResource(R.drawable.pickup);
                break;
            case "外送":
                holder.CategoryImage.setImageResource(R.drawable.delivery);
                break;
            case "課業":
                holder.CategoryImage.setImageResource(R.drawable.homework);
                break;
            case "修繕":
                holder.CategoryImage.setImageResource(R.drawable.repair);
                break;
            case "除蟲":
                holder.CategoryImage.setImageResource(R.drawable.debug);
                break;
        }
        if (itemList.get(position).getTitle().length()>8){
            holder.Title.setText(itemList.get(position).getTitle().substring(0,8)+"\n"+itemList.get(position).getTitle().substring(8));
        }else{
            holder.Title.setText(itemList.get(position).getTitle());
        }

        String Address=itemList.get(position).getCity()+itemList.get(position).getTown()+itemList.get(position).getRoad();

        holder.Area.setText(Address);


        String lineSep = System.getProperty("line.separator");
        String m=itemList.get(position).getDetail().replaceAll("<br />", lineSep);

        holder.message.setText(m);

        holder.Money.setText("$"+itemList.get(position).getMoney());

        String status=itemList.get(position).getStatus();
        holder.Status.setText(status);


        String short_time=string_sub(itemList.get(position).getTime());
        String short_until=string_sub(itemList.get(position).getUntil());

        holder.time.setText(short_time+" 至  "+short_until);

        if (status.equals("待接案")){
            holder.Status.setTextColor(Color.parseColor("#ff3333"));
        }else{
            holder.Status.setTextColor(Color.parseColor("#908e8d"));
        }

        if (itemList.get(position).getPid().equals(uid)){
            holder.bg.setBackgroundColor(Color.parseColor("#72c6cc"));
        }else{
            holder.bg.setBackgroundColor(Color.parseColor("#dfffffff"));
        }

        final boolean isExpanded = position==mExpandedPosition;
        if (itemList.get(position).getStatus().equals("待接案")&&isExpanded){
            holder.send.setVisibility(View.VISIBLE);
            holder.ans.setVisibility(View.VISIBLE);
        }else{
            holder.send.setVisibility(View.GONE);
            holder.ans.setVisibility(View.GONE);
        }
        holder.time_title.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.time.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.content.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.message.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);
        if (isExpanded)
            previousExpandedPosition = position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
            }
        });


        holder.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uid.equals(itemList.get(position).getRid())){
                    Toast.makeText(context,"你不能接自己的案子",Toast.LENGTH_SHORT).show();

                }else if(status.equals("待接案")){
                    try {
                        line_notify(itemList.get(position).getPid(),"https://a238c12f.ngrok.io/send_lineNotify",itemList.get(position).getCid(),"case_someone_apply");

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    SendMessage(uid,itemList.get(position).getCid());
                }else{
                    Toast.makeText(context,"此案件已完成或在進行中",Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.ans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuestionAlert(itemList.get(position).getCid(),itemList.get(position).getPid());


            }
        });

        holder.ans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

    private void QuestionAlert(final String cid,final String pid){
        boolean wrapInScrollView = true;

        final View item = LayoutInflater.from(context).inflate(R.layout.dialog_question, null);

        final MaterialDialog dialog =new MaterialDialog.Builder(context)
                .title("問與答 Q&A")
                .customView(item,false )
                .backgroundColorRes(R.color.colorBackground)
                .build();

        recyclerView = (RecyclerView)item.findViewById(R.id.dialog_recyclerview);
        layoutManager = new LinearLayoutManager(item.getContext());
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setHasFixedSize(false);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        FloatingActionButton fab = (FloatingActionButton)item.findViewById(R.id.question_fab);
        NestedScrollView nsv= (NestedScrollView)item.findViewById(R.id.q_scroll);
        nsv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    fab.hide();
                }else{
                    fab.show();
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(context)
                        .title("我要問問題！")
                        .inputType(InputType.TYPE_CLASS_TEXT )
                        .input("要詢問雇主的事情", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog ddialog, CharSequence input) {
                                // Do something
                                Log.d(TAG, "onInput: "+input);
                                Backgorundwork backgorundwork = new Backgorundwork(context);
                                backgorundwork.execute("insert_qanda",input.toString(),cid,uid,pid);
                                dialog.dismiss();
                            }
                        }).show();

            }
        });
        LoadQuestion(cid);



        dialog.show();

    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }



    public static void dissmissDialog() {
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    public void LoadQuestion(final String cid){
        String url ="http://163.17.5.182/app/load_question.php";
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
                            Type listType = new TypeToken<ArrayList<ItemQanda>>() {}.getType();
                            ArrayList<ItemQanda> posts = new ArrayList<ItemQanda>();
                            if (!response.contains("Undefined")){
                                posts = mGson.fromJson(response, listType);
                            }

                            if (posts.isEmpty()){
                                Toast.makeText(context,"尚未有人詢問",Toast.LENGTH_SHORT).show();
                            }else{
                                adapter = new RecyclerViewAdapterQanda(context, posts,uid);
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
                params.put("q_cid",cid);
                return params;
            }

        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
    private String string_sub(String original){
        int start_index=original.indexOf("-");
        int last_index=original.lastIndexOf(":");

        return original.substring(start_index+1,last_index);
    }

    public void SendMessage(final String uid,final String cid) {
        boolean wrapInScrollView = true;

        dialog=new MaterialDialog.Builder(context)
                .customView(R.layout.wantcase, wrapInScrollView)
                .backgroundColorRes(R.color.colorBackground)
                .build();

        View item = dialog.getCustomView();


        final MaterialEditText sm_message=(MaterialEditText)item.findViewById(R.id.to_message);
        Button button=(Button)item.findViewById(R.id.setting_time);
        final TextView sm_time=(TextView)item.findViewById(R.id.ut_time);
        ImageView send=item.findViewById(R.id.send_job_message);
        message="";

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c_end_hours = 0;
                c_end_mins = 0;
                time_check_status = 0;
                end_Minute = 0;
                end_Hours = 0;
                end_Day = 0;
                end_Way = 0;

                message=sm_message.getText().toString();
                sm_time.setText("正在設定時間");

                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of TimePickerDialog and return it
                new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        sm_time.setText("您設定的案件有效時間:\n" + hourOfDay + "個小時" + minute + "分");
                        c_end_hours = hourOfDay;
                        c_end_mins = minute;
                        Log.d("Tag", "get_time_end_hour=" + c_end_hours + "min=" + c_end_mins);
                    }
                }, hour, minute, true).show();


                time_check_status = 0;

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sm_message.getText().toString().equals("")||sm_time.getText().toString().equals("申請時效")){
                    Toast.makeText(context,"請設定訊息及時間",Toast.LENGTH_SHORT).show();
                }else{
                    String sm =sm_message.getText().toString();
                    Log.d(TAG, "onClick: "+sm);
                    String type = "sendmessage";
                    Backgorundwork backgorundwork = new Backgorundwork(context);
                    backgorundwork.execute(type,cid,uid,sm,Integer.toString(c_end_hours),Integer.toString(c_end_mins));
                }
            }
        });

        dialog.show();

    }


    public void deleteItem(int index) {
        itemList.remove(index);
        notifyItemRemoved(index);
    }


    public String getItemCid(int position) {
        return itemList.get(position).getCid();
    }

    private void line_notify(final String account,final String url,final String cid,final String type ) throws JSONException {
        HttpURLConnection urlConnection;


        JSONObject datas = new JSONObject();
        datas.put("caseID",cid);
        datas.put("account",account);
        datas.put("type",type);


        Log.d(TAG, "line_notify: "+datas);


        String data = datas.toString();
        String result = null;
        try {
            //Connect

            urlConnection = (HttpURLConnection) ((new URL(url).openConnection()));
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.connect();

            //Write
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(data);
            writer.close();
            outputStream.close();

            //Read
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

            String line = null;
            StringBuilder sb = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            result = sb.toString();
            urlConnection.disconnect();
            Log.d(TAG, "send_message: "+result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}