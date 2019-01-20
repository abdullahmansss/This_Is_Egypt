package ali.abdullahmansour.egypt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import ali.abdullahmansour.egypt.Models.ChatMessage;
import ali.abdullahmansour.egypt.Models.Review;
import ali.abdullahmansour.egypt.Models.UserData;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    String companykey;

    EditText message_field;
    Button send_message_btn;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder> firebaseRecyclerAdapter;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    CircleImageView chat_picture;
    TextView chat_title;

    Toolbar toolbar;

    String company_title,company_imageurl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        companykey = getIntent().getStringExtra(CompaniesFragment.EXTRA_COMPANY_KEY);

        message_field = findViewById(R.id.message_field);
        send_message_btn = findViewById(R.id.send_message_btn);
        recyclerView = findViewById(R.id.chat_recyclerview);

        toolbar = findViewById(R.id.chat_toolbar);


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

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.toString().trim().length() == 0) {
                    send_message_btn.setEnabled(false);
                } else {
                    send_message_btn.setEnabled(true);
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

                newmesage(message,getUid(),hour,minute);
                message_field.setText("");
            }
        });

        DisplayChat();
    }

    public void returndata()
    {

    }

    public void newmesage(String message, String id,int hour,int minute)
    {
        ChatMessage chatMessage = new ChatMessage(message,id,hour,minute);

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

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView message,time;
        CardView cardView;

        ChatViewHolder(View itemView)
        {
            super(itemView);

            message = itemView.findViewById(R.id.message_txt);
            time = itemView.findViewById(R.id.time_txt);
            cardView = itemView.findViewById(R.id.chat_card);
        }

        void BindMessages (final ChatMessage chatMessage)
        {
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
