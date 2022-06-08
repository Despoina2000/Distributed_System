package Distributed_System_part2.app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.ObservableList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import Distributed_System_part2.app.Node.UserNode;

public class TopicActivity extends AppCompatActivity {

    private TextView currentTopicTextView;
    private EditText messageEditText;
    private Button sendMessageButton;
    private Button sendImageButton;
    private Button sendVideoButton;
    private Button cameraImageButton;
    private Button cameraVideoButton;
    private ListView messagesListView;
    private ArrayAdapter messagesAdapter;

    private String currentTopic;

    private File file;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int CAMERA_IMAGE = 3;
    private static final int CAMERA_VIDEO = 4;

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
        cameraImageButton = (Button) findViewById(R.id.cameraImageButton);
        cameraVideoButton = (Button) findViewById(R.id.cameraVideoButton);
        messagesListView = (ListView) findViewById(R.id.messagesListView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

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

        //camera image button on click listener
        cameraImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                String f = System.currentTimeMillis()+".jpg"; // Designated name
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), f);
                Uri fileUri = FileProvider.getUriForFile(TopicActivity.this,"Distributed_System_part2.app.fileprovider", file); // Specify the uri of the image to be saved, where the image is saved in the system album
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_IMAGE);
            }
        });

        //camera video button on click listener
        cameraVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                String f = System.currentTimeMillis()+".mp4"; // Designated name
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), f);
                Uri fileUri = FileProvider.getUriForFile(TopicActivity.this,"Distributed_System_part2.app.fileprovider", file); // Specify the uri of the image to be saved, where the image is saved in the system album
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_VIDEO);
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
                        messagesListView.smoothScrollToPosition(messagesAdapter.getCount() -1);
                    }
                });
            }

            @Override
            public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                        messagesListView.smoothScrollToPosition(messagesAdapter.getCount() -1);
                    }
                });
            }

            @Override
            public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                        messagesListView.smoothScrollToPosition(messagesAdapter.getCount() -1);
                    }
                });
            }

            @Override
            public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                        messagesListView.smoothScrollToPosition(messagesAdapter.getCount() -1);
                    }
                });
            }

            @Override
            public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesAdapter.notifyDataSetChanged();
                        messagesListView.smoothScrollToPosition(messagesAdapter.getCount() -1);
                    }
                });
            }
        });

        //set adapter for messages list view
        messagesListView.setAdapter(messagesAdapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                if (requestCode == PICK_IMAGE) {
                    try {
                        String f = System.currentTimeMillis()+".jpg"; // Designated name
                        file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), f);
                        InputStream in = getContentResolver().openInputStream(uri);
                        OutputStream out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while((len=in.read(buf))>0){
                            out.write(buf,0,len);
                        }
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    UserNode.getUserNodeInstance().sendImageMessage(file);
                } else if (requestCode == PICK_VIDEO) {
                    try {
                        String f = System.currentTimeMillis()+".mp4"; // Designated name
                        file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), f);
                        InputStream in = getContentResolver().openInputStream(uri);
                        OutputStream out = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int len;
                        while((len=in.read(buf))>0){
                            out.write(buf,0,len);
                        }
                        out.close();
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    UserNode.getUserNodeInstance().sendVideoMessage(file);
                } else if (requestCode == CAMERA_IMAGE) {
                    UserNode.getUserNodeInstance().sendImageMessage(file);
                } else if (requestCode == CAMERA_VIDEO) {
                    UserNode.getUserNodeInstance().sendVideoMessage(file);
                }
            }
        }

    }
}