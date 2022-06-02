package Distributed_System_part2.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.ObservableList;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import Distributed_System_part2.app.Node.UserNode;

public class MainActivity extends AppCompatActivity {

    private boolean connected = false;
    private Button connectButton;
    private Button newTopicButton;
    private Button refreshTopicsButton;
    private ListView topicsListView;
    private ArrayAdapter topicsAdapter;
    public UserNode userNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button)findViewById(R.id.connectButton);
        newTopicButton = (Button)findViewById(R.id.newTopicButton);
        refreshTopicsButton = (Button)findViewById(R.id.refreshTopicsButton);
        topicsListView = (ListView)findViewById(R.id.topicsListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        newTopicButton.setEnabled(false);
        refreshTopicsButton.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connected){
                    //TODO: request username and broker IPs, create new usernode
                    userNode = UserNode.getUserNodeInstance("username","10.0.2.2","10.0.2.2","10.0.2.2");

                    topicsAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1,userNode.topics);
                    userNode.topics.addOnListChangedCallback(new ObservableList.OnListChangedCallback() {
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
                    topicsListView.setAdapter(topicsAdapter);

                    topicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Toast.makeText(MainActivity.this, adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show();
                        }
                    });

                    refreshTopicsButton.setEnabled(true);
                    newTopicButton.setEnabled(true);
                    connectButton.setText(R.string.disconnect_button_text);
                    connected = true;
                } else {
                    //TODO: disconnect, destroy usernode
                    userNode.quit();
                    refreshTopicsButton.setEnabled(false);
                    newTopicButton.setEnabled(false);
                    connectButton.setText(R.string.connect_button_text);
                    connected = false;
                }
            }
        });

        final int[] i = {1};
        newTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: request new topic String, usernode.setTopic, go to TopicActivity with this topic string
                userNode.setTopic("test" + i[0]);
                i[0]++;
            }
        });

        refreshTopicsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNode.requestTopics();
            }
        });
    }
}