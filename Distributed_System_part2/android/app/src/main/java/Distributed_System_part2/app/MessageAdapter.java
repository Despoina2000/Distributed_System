package Distributed_System_part2.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableArrayList;

import Distributed_System_part2.app.Model.ImageMessage;
import Distributed_System_part2.app.Model.Message;
import Distributed_System_part2.app.Model.TextMessage;
import Distributed_System_part2.app.Model.VideoMessage;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(@NonNull Context context, int resource, @NonNull ObservableArrayList<Message> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_layout, parent, false);
        }

        Message message = getItem(position);

        TextView senderTextView = (TextView)convertView.findViewById(R.id.senderTextView);
        senderTextView.setText(message.getUsername() + ":");

        TextView textMessageTextView = (TextView)convertView.findViewById(R.id.textMessageTextView);
        ImageView imageMessageImageView = (ImageView)convertView.findViewById(R.id.imageMessageImageView);
        VideoView videoMessageVideoView = (VideoView)convertView.findViewById(R.id.videoMessageVideoView);
        if (message instanceof TextMessage) {
            textMessageTextView.setVisibility(View.VISIBLE);
            imageMessageImageView.setVisibility(View.GONE);
            videoMessageVideoView.setVisibility(View.GONE);
            textMessageTextView.setText(((TextMessage)message).getContent());
        } else if (message instanceof ImageMessage) {
            textMessageTextView.setVisibility(View.GONE);
            imageMessageImageView.setVisibility(View.VISIBLE);
            videoMessageVideoView.setVisibility(View.GONE);
            imageMessageImageView.setImageBitmap(BitmapFactory.decodeFile(((ImageMessage)message).getContent().getAbsolutePath()));
        } else if (message instanceof VideoMessage) {
            textMessageTextView.setVisibility(View.GONE);
            imageMessageImageView.setVisibility(View.GONE);
            videoMessageVideoView.setVisibility(View.VISIBLE);
            videoMessageVideoView.setVideoPath(((VideoMessage)message).getContent().getAbsolutePath());
            MediaController mediaController = new MediaController(getContext());
            videoMessageVideoView.setMediaController(mediaController);
            mediaController.setMediaPlayer(videoMessageVideoView);
        }
        return convertView;
    }
}
