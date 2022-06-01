package Distributed_System_part2.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private boolean connected = false;
    private Button connectButton;
    private Button newTopicButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        newTopicButton = findViewById(R.id.newTopicButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        newTopicButton.setEnabled(false);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connected){
                    //TODO: request username and broker IPs, create new usernode
                    newTopicButton.setEnabled(true);
                    connectButton.setText(R.string.disconnect_button_text);
                    connected = true;
                } else {
                    //TODO: disconnect, destroy usernode
                    newTopicButton.setEnabled(false);
                    connectButton.setText(R.string.connect_button_text);
                    connected = false;
                }
            }
        });

        newTopicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: request new topic String, usernode.setTopic, go to TopicActivity with this topic string
            }
        });
    }
}