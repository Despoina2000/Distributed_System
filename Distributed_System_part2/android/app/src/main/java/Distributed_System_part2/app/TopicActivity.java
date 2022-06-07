package Distributed_System_part2.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import Distributed_System_part2.app.Node.UserNode;

public class TopicActivity extends AppCompatActivity {

    private TextView currentTopicTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private Button sendImageButton;
    private Button sendVideoButton;
    private Button cameraButton;
    private ListView messagesListView;
    private ArrayAdapter messagesAdapter;

    private String currentTopic;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Intent intent = getIntent();
        currentTopic = intent.getStringExtra("topic");

        currentTopicTextView = (TextView) findViewById(R.id.currentTopicTextView);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        sendImageButton = (Button) findViewById(R.id.sendImageButton);
        sendVideoButton = (Button) findViewById(R.id.sendVideoButton);
        cameraButton = (Button) findViewById(R.id.cameraButton);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
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

        //send image button on click listener
        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        //send video button on click listener
        sendVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("video/*");
                startActivityForResult(intent, PICK_VIDEO);
            }
        });

        //camera button on click listener
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: take photo or image and send it
            }
        });

        //messages adapter initialization
        messagesAdapter = new MessageAdapter(TopicActivity.this, R.layout.message_layout, UserNode.getUserNodeInstance().topicsMessages.get(currentTopic));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                if (requestCode == PICK_IMAGE) {
                    UserNode.getUserNodeInstance().sendImageMessage(new File(uri.getPath().replaceAll("/document/raw:","")));
                } else if (requestCode == PICK_VIDEO) {
                    UserNode.getUserNodeInstance().sendVideoMessage(new File(uri.getPath().replaceAll("/document/raw:","")));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}