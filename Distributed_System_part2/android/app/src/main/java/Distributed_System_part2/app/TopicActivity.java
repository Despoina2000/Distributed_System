package Distributed_System_part2.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableList;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import Distributed_System_part2.app.Node.UserNode;

public class TopicActivity extends AppCompatActivity {

    private TextView currentTopicTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private ListView messagesListView;
    private ArrayAdapter messagesAdapter;

    private String currentTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Intent intent = getIntent();
        currentTopic = intent.getStringExtra("topic");

        currentTopicTextView = (TextView)findViewById(R.id.currentTopicTextView);
        messageEditText = (EditText)findViewById(R.id.messageEditText);
        sendMessageButton = (Button)findViewById(R.id.sendMessageButton);
        messagesListView = (ListView)findViewById(R.id.messagesListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //set current topic
        currentTopicTextView.setText(currentTopic);
        UserNode.getUserNodeInstance().setTopic(currentTopic);

        //send button on click listener
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserNode.getUserNodeInstance().sendTextMessage(messageEditText.getText().toString());
                messageEditText.getText().clear();
            }
        });

        //messages adapter initialization
        messagesAdapter = new ArrayAdapter(TopicActivity.this,android.R.layout.simple_list_item_1, UserNode.getUserNodeInstance().topicsMessages.get(currentTopic));
        UserNode.getUserNodeInstance().topicsMessages.get(currentTopic).addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
            @Override
            public void onChanged(ObservableList sender) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        //set adapter for messages list view
        messagesListView.setAdapter(messagesAdapter);
    }
}