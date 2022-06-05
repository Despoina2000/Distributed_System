package Distributed_System_part2.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import Distributed_System_part2.app.Node.UserNode;

public class MainActivity extends AppCompatActivity {

    private final String URL_BROKER1 = "10.0.2.2";
    private final String URL_BROKER2 = "10.0.2.2";
    private final String URL_BROKER3 = "10.0.2.2";
    private boolean connected = false;

    private Button connectButton;
    private Button newTopicButton;
    private Button refreshTopicsButton;
    private ListView topicsListView;
    private ArrayAdapter topicsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button) findViewById(R.id.connectButton);
        newTopicButton = (Button) findViewById(R.id.newTopicButton);
        refreshTopicsButton = (Button) findViewById(R.id.refreshTopicsButton);
        topicsListView = (ListView) findViewById(R.id.topicsListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //change thread policy to run from main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!connected) {
            newTopicButton.setEnabled(false);
            refreshTopicsButton.setEnabled(false);
        }

        //request topics if going back to topics list
        if (UserNode.isUserNodeInitialized()) {
            UserNode.getUserNodeInstance().requestTopics();
        }

        //connect button on click listener
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //button = connect
                if (!connected) {
                    //TODO: request username and broker IPs, create new usernode
                    AlertDialog.Builder connectDialog = new AlertDialog.Builder(MainActivity.this);
                    final EditText usernameDialogEditText = new EditText(MainActivity.this);
                    connectDialog.setTitle("Set username:");
                    connectDialog.setView(usernameDialogEditText);
                    connectDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            //create usernode
                            UserNode.createUserNodeInstance(usernameDialogEditText.getText().toString(), URL_BROKER1, URL_BROKER2, URL_BROKER3);

                            //create topicsadapter, add callback on observable list topics
                            topicsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, UserNode.getUserNodeInstance().topics);
                            UserNode.getUserNodeInstance().topics.addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
                                @Override
                                public void onChanged(ObservableList sender) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topicsAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topicsAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topicsAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topicsAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }

                                @Override
                                public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            topicsAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                            //set adapter for topics list view
                            topicsListView.setAdapter(topicsAdapter);

                            //set onclick listener for every item in list
                            topicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(MainActivity.this, TopicActivity.class);
                                    intent.putExtra("topic", adapterView.getItemAtPosition(i).toString()); // put the topic string in the intent extras
                                    startActivity(intent);
                                }
                            });

                            refreshTopicsButton.setEnabled(true);
                            newTopicButton.setEnabled(true);
                            connectButton.setText(R.string.disconnect_button_text);
                            connected = true;
                        }
                    });
                    connectDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // do nothing if cancel
                        }
                    });

                    connectDialog.show();

                } else { // button = disconnect
                    //TODO: disconnect, destroy usernode
                    UserNode.getUserNodeInstance().quit();
                    topicsAdapter.clear();
                    refreshTopicsButton.setEnabled(false);
                    newTopicButton.setEnabled(false);
                    connectButton.setText(R.string.connect_button_text);
                    connected = false;
                }
            }
        });

        //new topic button on click listener
        newTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: request new topic String, usernode.setTopic, go to TopicActivity with this topic string
                AlertDialog.Builder setTopicDialog = new AlertDialog.Builder(MainActivity.this);
                final EditText setTopicDialogEditText = new EditText(MainActivity.this);
                setTopicDialog.setTitle("New Topic");
                setTopicDialog.setView(setTopicDialogEditText);
                setTopicDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(MainActivity.this, TopicActivity.class);
                        intent.putExtra("topic", setTopicDialogEditText.getText().toString()); // put the topic string in the intent extras
                        startActivity(intent);
                    }
                });
                setTopicDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing if cancel
                    }
                });

                setTopicDialog.show();
            }
        });

        //refresh topics button on click listener
        refreshTopicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserNode.getUserNodeInstance().requestTopics();
            }
        });
    }
}