package ali.abdullahmansour.egypt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.TimeUnit;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import ali.abdullahmansour.egypt.Models.ChatMessage;
import ali.abdullahmansour.egypt.Models.Review;
import ali.abdullahmansour.egypt.Models.UserData;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    String companykey;

    EditText message_field,record_txt;
    FloatingActionButton send_message_btn,send_voice_btn;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder> firebaseRecyclerAdapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    CircleImageView chat_picture;
    TextView chat_title;

    Toolbar toolbar;

    String company_title,company_imageurl;

    MediaRecorder mRecorder = null;
    static String mFileName = null;
    private static final String LOG_TAG = "AudioRecordTest";

    StorageReference audioref;

    RotateLoading progressBar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        companykey = getIntent().getStringExtra(CompaniesFragment.EXTRA_COMPANY_KEY);

        message_field = findViewById(R.id.message_field);
        send_message_btn = findViewById(R.id.send_message_btn);
        send_voice_btn = findViewById(R.id.send_voice_btn);
        record_txt = findViewById(R.id.record_txt);
        progressBar = findViewById(R.id.get_progress);
        recyclerView = findViewById(R.id.chat_recyclerview);

        toolbar = findViewById(R.id.chat_toolbar);

        audioref = FirebaseStorage.getInstance().getReference();

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.chat_appbar, null);
        actionBar.setCustomView(view);

        chat_picture = view.findViewById(R.id.chat_picture);
        chat_title = view.findViewById(R.id.chat_title);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("allusers").child(companykey).addListenerForSingleValueEvent(
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Get user value
                        UserData userData = dataSnapshot.getValue(UserData.class);

                        company_title = userData.getTitle();
                        company_imageurl = userData.getImage_url();

                        chat_title.setText(company_title);

                        Picasso.get()
                                .load(company_imageurl)
                                .placeholder(R.drawable.travel)
                                .error(R.drawable.travel)
                                .into(chat_picture);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Toast.makeText(getApplicationContext(), "can\'t fetch data", Toast.LENGTH_SHORT).show();
                    }
                });

        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        send_message_btn.setEnabled(false);

        message_field.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.toString().trim().length() == 0)
                {
                    send_message_btn.setVisibility(View.GONE);
                    send_voice_btn.setVisibility(View.VISIBLE);

                    send_message_btn.setEnabled(false);
                    send_voice_btn.setEnabled(true);
                } else {
                    send_message_btn.setVisibility(View.VISIBLE);
                    send_voice_btn.setVisibility(View.GONE);

                    send_message_btn.setEnabled(true);
                    send_voice_btn.setEnabled(false);
                }
            }
        });

        send_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                String message = message_field.getText().toString();

                newmesage(message,getUid(),"text",hour,minute);
                message_field.setText("");
            }
        });

        send_voice_btn.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    //Toast.makeText(getApplicationContext(), "recording ..", Toast.LENGTH_SHORT).show();
                    //send_voice_btn.animate().rotationBy(360).setDuration(2000);
                    message_field.setVisibility(View.GONE);
                    record_txt.setVisibility(View.VISIBLE);
                    record_txt.setText("Recording ..");
                    startRecording();
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    //Toast.makeText(getApplicationContext(), "Done ..", Toast.LENGTH_SHORT).show();
                    message_field.setVisibility(View.VISIBLE);
                    record_txt.setVisibility(View.GONE);
                    stopRecording();
                }
                return false;
            }
        });

        DisplayChat();
    }

    private void startRecording() {
        mFileName = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/"
                + UUID.randomUUID().toString() + "_audio_record.3gp";

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        uploadaudio();
    }

    @SuppressLint("RestrictedApi")
    private void uploadaudio()
    {
        /*Uri uri = Uri.fromFile(new File(mFileName));

        StorageReference storageReference = audioref.child("Audio/" + uri.getLastPathSegment());

        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {

            }
        });*/

        send_voice_btn.setAlpha(0f);
        progressBar.start();

        UploadTask uploadTask;

        Uri uri = Uri.fromFile(new File(mFileName));

        final StorageReference storageReference = audioref.child("Audio/" + uri.getLastPathSegment());

        uploadTask = storageReference.putFile(uri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();


                String audio_url = downloadUri.toString();

                send_voice_btn.setAlpha(1f);
                progressBar.stop();

                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                newmesage(audio_url,getUid(),"audio",hour,minute);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void returndata()
    {

    }

    private void bindLogo()
    {
        // Start animating the image
        final AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
        animation1.setDuration(700);
        final AlphaAnimation animation2 = new AlphaAnimation(1.0f, 0.2f);
        animation2.setDuration(700);
        //animation1 AnimationListener
        animation1.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                // start animation2 when animation1 ends (continue)
                record_txt.startAnimation(animation2);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationStart(Animation arg0) {}
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                // start animation1 when animation2 ends (repeat)
                record_txt.startAnimation(animation1);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationStart(Animation arg0) {}
        });

        record_txt.startAnimation(animation1);
    }

    private void bindLogo2()
    {
        // Start animating the image
        final AlphaAnimation animation1 = new AlphaAnimation(0.0f, 0.0f);
        animation1.setDuration(0);
        final AlphaAnimation animation2 = new AlphaAnimation(0.0f, 0.0f);
        animation2.setDuration(0);
        //animation1 AnimationListener
        animation1.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                // start animation2 when animation1 ends (continue)
                record_txt.startAnimation(animation2);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationStart(Animation arg0) {}
        });

        //animation2 AnimationListener
        animation2.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation arg0)
            {
                // start animation1 when animation2 ends (repeat)
                record_txt.startAnimation(animation1);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {}
            @Override
            public void onAnimationStart(Animation arg0) {}
        });

        record_txt.startAnimation(animation1);
    }

    public void newmesage(String message, String id ,String type,int hour,int minute)
    {
        ChatMessage chatMessage = new ChatMessage(message,id,type,hour,minute);

        String key = databaseReference.child("chats").push().getKey();
        databaseReference.child("chats").child(getUid()).child(companykey).child(key).setValue(chatMessage);
        databaseReference.child("chats").child(companykey).child(getUid()).child(key).setValue(chatMessage);
    }

    public static String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void DisplayChat ()
    {
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("chats")
                .child(getUid())
                .child(companykey);

        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull final ChatMessage model)
            {
                final String companykey = getRef(position).getKey();

                holder.BindMessages(model);
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.chat_item, parent, false);
                return new ChatViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1)))
                {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder implements MediaPlayer.OnCompletionListener,MediaPlayer.OnBufferingUpdateListener
    {
        TextView message,time;
        CardView cardView;

        SeekBar seekBar;
        CardView audio_card;
        TextView audio_time_txt,audio_duration_txt;
        ImageView play_audio;
        RotateLoading rotateLoading;

        MediaPlayer mediaPlayer;

        final Handler handler = new Handler();

        @SuppressLint("ClickableViewAccessibility")
        ChatViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message_txt);
            time = itemView.findViewById(R.id.time_txt);
            cardView = itemView.findViewById(R.id.chat_card);

            audio_card = itemView.findViewById(R.id.record_card);
            seekBar = itemView.findViewById(R.id.record_seekbar);
            audio_time_txt = itemView.findViewById(R.id.record_time_txt);
            audio_duration_txt = itemView.findViewById(R.id.record_duration_txt);
            play_audio = itemView.findViewById(R.id.play_record);
            rotateLoading = itemView.findViewById(R.id.get_progress);

            seekBar.setMax(99); // 100% (0~99)
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    if (fromUser)
                    {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        void BindMessages (final ChatMessage chatMessage)
        {
            if (chatMessage.getMessagetype().equals("audio"))
            {
                cardView.setVisibility(View.GONE);
                audio_card.setVisibility(View.VISIBLE);

                if (chatMessage.getId().equals(getUid()))
                {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)audio_card.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                    audio_card.setLayoutParams(params);
                    audio_card.setCardBackgroundColor(Color.parseColor("#e9b0e9ff"));
                }

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnBufferingUpdateListener(this);
                mediaPlayer.setOnCompletionListener(this);

                @SuppressLint("StaticFieldLeak") final AsyncTask<String,String,String> mp3player = new AsyncTask<String, String, String>()
                {
                    @Override
                    protected void onPreExecute()
                    {
                        rotateLoading.start();
                        play_audio.setVisibility(View.GONE);
                    }

                    @Override
                    protected String doInBackground(String... strings)
                    {
                        try
                        {
                            mediaPlayer.setDataSource(strings[0]);
                            mediaPlayer.prepare();
                        } catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        return "";
                    }

                    @SuppressLint("DefaultLocale")
                    @Override
                    protected void onPostExecute(String s)
                    {
                        seekBar.setMax(mediaPlayer.getDuration());
                        audio_duration_txt.setText(String.format("%02d:%02d ",
                                java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration()),
                                java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) -
                                        java.util.concurrent.TimeUnit.MINUTES.toSeconds(java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration()))
                        ));

                        rotateLoading.stop();
                        play_audio.setVisibility(View.VISIBLE);
                    }
                };

                mp3player.execute(chatMessage.getMessage()); // direct link mp3 file

                play_audio.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (mediaPlayer.isPlaying())
                        {
                            mediaPlayer.pause();
                            play_audio.setImageResource(R.drawable.playmusic);
                        } else
                        {
                            mediaPlayer.start();
                            play_audio.setImageResource(R.drawable.pause);
                            changeseekbar();
                        }
                    }
                });

                int hour = chatMessage.getHour();
                int minutes = chatMessage.getMinute();

                String timeSet = "";
                if (hour > 12)
                {
                    hour -= 12;
                    timeSet = "PM";
                } else if (hour == 0)
                {
                    hour += 12;
                    timeSet = "AM";
                } else if (hour == 12)
                {
                    timeSet = "PM";
                } else
                {
                    timeSet = "AM";
                }

                String min = "";
                if (minutes < 10)
                    min = "0" + minutes ;
                else
                    min = String.valueOf(minutes);

                // Append in a StringBuilder
                String aTime = new StringBuilder().append(hour).append(':')
                        .append(min ).append(" ").append(timeSet).toString();

                audio_time_txt.setText(aTime);
            } else if (chatMessage.getMessagetype().equals("text"))
            {
                cardView.setVisibility(View.VISIBLE);
                audio_card.setVisibility(View.GONE);

                message.setText(chatMessage.getMessage());

                int hour = chatMessage.getHour();
                int minutes = chatMessage.getMinute();

                String timeSet = "";
                if (hour > 12)
                {
                    hour -= 12;
                    timeSet = "PM";
                } else if (hour == 0)
                {
                    hour += 12;
                    timeSet = "AM";
                } else if (hour == 12)
                {
                    timeSet = "PM";
                } else
                {
                    timeSet = "AM";
                }

                String min = "";
                if (minutes < 10)
                    min = "0" + minutes ;
                else
                    min = String.valueOf(minutes);

                // Append in a StringBuilder
                String aTime = new StringBuilder().append(hour).append(':')
                        .append(min ).append(" ").append(timeSet).toString();

                time.setText(aTime);

                if (chatMessage.getId().equals(getUid()))
                {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)cardView.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                    cardView.setLayoutParams(params);
                    cardView.setCardBackgroundColor(Color.parseColor("#e9b0e9ff"));
                }
            }
        }

        private void changeseekbar()
        {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());

            if (mediaPlayer.isPlaying())
            {
                Runnable runnable = new Runnable()
                {
                    @Override
                    public void run() {
                        changeseekbar();
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {
            seekBar.setSecondaryProgress(percent);
        }


        @Override
        public void onCompletion(MediaPlayer mp)
        {
            play_audio.setImageResource(R.drawable.playmusic);
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (firebaseRecyclerAdapter != null)
        {
            firebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }
}
