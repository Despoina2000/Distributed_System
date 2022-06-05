package Distributed_System_part2.app.Node;

import android.os.Environment;

import Distributed_System_part2.app.Model.ImageMessage;
import Distributed_System_part2.app.Model.Message;
import Distributed_System_part2.app.Model.TextMessage;
import Distributed_System_part2.app.Model.VideoMessage;
import Distributed_System_part2.app.Util.Util;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import androidx.databinding.ObservableArrayList;

/**
 * user node class, starts publisher and consumer
 */
public class UserNode {
    public static final int PORT_BROKER1 = 4000;
    public static final int PORT_BROKER2 = 5555;
    public static final int PORT_BROKER3 = 5984;
    public final String url = "localhost";
    public Util util;

    public volatile String username;
    public volatile String URL_BROKER1;
    public volatile String URL_BROKER2;
    public volatile String URL_BROKER3;
    public volatile int currentBrokerPort;
    public volatile String currentTopic = null;
    public volatile HashMap<Integer, ArrayList<String>> brokerPortsAndTopics;
    public volatile ObservableArrayList<String> topics;
    public volatile HashMap<String, ObservableArrayList<Message>> topicsMessages; //edw kratame osa minimata exoume diavasei idi

    private Publisher publisher;
    private Consumer consumer;
    private BufferedReader br;

    private static UserNode userNodeInstance = null;
    /**
     * Main thread: publisher, other thread: consumer
     */
    private UserNode(String username, String URL_BROKER1, String URL_BROKER2, String URL_BROKER3) {
        this.username = username;
        this.URL_BROKER1 = URL_BROKER1;
        this.URL_BROKER2 = URL_BROKER2;
        this.URL_BROKER3 = URL_BROKER3;

        brokerPortsAndTopics = new HashMap<Integer, ArrayList<String>>();
        topicsMessages = new HashMap<String, ObservableArrayList<Message>>();
        topics = new ObservableArrayList<String>();

        // currentBrokerPort = random broker port
        Random random = new Random();
        switch (random.nextInt(3)) {
            case 0:
                this.currentBrokerPort = PORT_BROKER1;
                break;
            case 1:
                this.currentBrokerPort = PORT_BROKER2;
                break;
            case 2:
                this.currentBrokerPort = PORT_BROKER3;
                break;
        }

        util = new Util();
        publisher = new Publisher();
        consumer = new Consumer(this);
        consumer.start();

        //TODO: create folder (to store images and videos)
    }

    public static synchronized void createUserNodeInstance(String username, String URL_BROKER1, String URL_BROKER2, String URL_BROKER3) {
        if (userNodeInstance == null) {
            userNodeInstance = new UserNode(username,URL_BROKER1,URL_BROKER2,URL_BROKER3);
        }
    }

    public static UserNode getUserNodeInstance() {
        return userNodeInstance;
    }

    public static boolean isUserNodeInitialized() {
        if (userNodeInstance == null) return false;
        return true;
    }

    public void setTopic(String currentTopic) {
        this.currentTopic = currentTopic;
        if (!topicsMessages.containsKey(currentTopic)) topicsMessages.put(currentTopic, new ObservableArrayList<>());
        publisher.setTopic();
//        consumer.connectToBroker(currentBrokerPort);
//        consumer.setTopic();
    }

    public void requestTopics() {
        consumer.requestTopics();
        System.out.println("requesting topics");
        //TODO: add topics to ObservableArrayList
    }

    public void quit() {
        publisher.disconnect();
        consumer.disconnect();
        userNodeInstance = null;
    }

    public void sendTextMessage(String msg) {
        publisher.sendMessage(new TextMessage(username, currentTopic, msg));
    }

    public void sendImageMessage(File image) {
        publisher.sendMessage(new ImageMessage(username, currentTopic, util.extractImageMetadata(image), image));
    }

    public void sendVideoMessage(File video) {
        publisher.sendMessage(new VideoMessage(username, currentTopic, util.extractVideoMetadata(video), video));
    }


    private class Publisher {
        private Socket socket;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;

        public Publisher() {
            connectToBroker(currentBrokerPort);
        }

        /**
         * kanei connect ston broker, kanei initialize ta object input/output streams
         * stelnei ston broker oti einai "publisher" kai meta stelnei to username tou
         *
         * @param port
         */
        public void connectToBroker(int port) {
            try {
                switch (port) {
                    case PORT_BROKER1:
                        socket = new Socket(URL_BROKER1, PORT_BROKER1);
                        break;
                    case PORT_BROKER2:
                        socket = new Socket(URL_BROKER2, PORT_BROKER2);
                        break;
                    case PORT_BROKER3:
                        socket = new Socket(URL_BROKER3, PORT_BROKER3);
                        break;
                }
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject("publisher");
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                if (objectInputStream.readObject().equals("username?")) objectOutputStream.writeObject(username);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * sends to broker currentTopic, perimenei apantisi apo ton broker
         * an i apantisi einai "continue" mporei na arxisei na stelnei minimata sto currentTopic
         * an i apantisi einai kapoia port simainei oti prepei na sindethei ston katallilo broker
         * kai na ksanasteilei to topic mexri na akousei "continue"
         */
        public void setTopic() {

            try {
                //send to broker currentTopic
                objectOutputStream.writeObject(currentTopic);
                //perimenoume apantisi apo broker,
                Object brokerAnswer = objectInputStream.readObject();
                //perimenoume na mas pei o broker na sinexisoume
                if (brokerAnswer.equals("continue")) {
//                    System.out.println("broker sent continue");
                    consumer.setTopic();
                    System.out.println("Current topic: " + currentTopic);
                } else {
                    // an i apantisi einai broker port thetoume currentBrokerPort = port
                    // kai kanoume connectToBroker(port), consumer.connectToBroker(port) kai ksana setTopic kai consumer.setTopic
                    currentBrokerPort = Integer.parseInt((String) brokerAnswer);
                    publisher.disconnect();
                    publisher.connectToBroker(currentBrokerPort);
                    publisher.setTopic();
//                    consumer.disconnect();
                    consumer.connectToBroker(currentBrokerPort);
                    consumer.setTopic();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(Message message) {
            try {
                if (message instanceof TextMessage) objectOutputStream.writeObject(message);
                else if (message instanceof ImageMessage) {
                    objectOutputStream.writeObject(new ImageMessage(username, currentTopic, ((ImageMessage) message).getMetadata()));
                    sendFileChunks(util.splitFileToChunks(((ImageMessage) message).getContent()));
                } else if (message instanceof VideoMessage) {
                    objectOutputStream.writeObject(new VideoMessage(username, currentTopic, ((VideoMessage) message).getMetadata()));
                    sendFileChunks(util.splitFileToChunks(((VideoMessage) message).getContent()));
                } else objectOutputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendFileChunks(ArrayList<byte[]> fileChunks) {
            try {
                for (byte[] chunk : fileChunks) {
                    objectOutputStream.writeObject(chunk);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * sends "/disconnect" to Broker and then closes streams and socket
         */
        public void disconnect() {
            try {
                objectOutputStream.writeObject("/disconnect");
                objectOutputStream.close();
                objectInputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Consumer extends Thread {
        private UserNode parent;
        private Socket socket = null;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;

        public Consumer(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            connectToBroker(currentBrokerPort);
            requestTopics();
            processIncomingMessages();
        }

        /**
         * kanei connect ston broker, kanei initialize ta object input/output streams
         * stelnei ston broker oti einai "consumer" kai meta stelnei to username tou
         *
         * @param port
         */
        public void connectToBroker(int port) {

            try {
//                if (socket != null) disconnect();
                //connect to broker at port
                switch (port) {
                    case PORT_BROKER1:
                        socket = new Socket(URL_BROKER1, PORT_BROKER1);
                        break;
                    case PORT_BROKER2:
                        socket = new Socket(URL_BROKER2, PORT_BROKER2);
                        break;
                    case PORT_BROKER3:
                        socket = new Socket(URL_BROKER3, PORT_BROKER3);
                        break;
                }
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                //send "consumer"
                objectOutputStream.writeObject("consumer");
                objectOutputStream.flush();
                //read "username?"
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                if (objectInputStream.readObject().equals("username?"))
                    //send username
                    objectOutputStream.writeObject(username);
            } catch (IOException | ClassNotFoundException e) {
                if (!(e instanceof SocketException)) e.printStackTrace();
            }
        }

        /**
         * sends to broker currentTopic
         */
        public void setTopic() {
            try {
                objectOutputStream.writeObject(currentTopic);
                //edw mporoume na kanoume print olo to istoriko gia to currentTopic apo to topicsMessages kai tha perimenoume gia kainouria minimata
            } catch (IOException e) {
                if (!(e instanceof SocketException)) e.printStackTrace();
            }
        }

        /**
         * dexetai messages apo ton broker kai pratei analoga me to Class tou incoming message
         * px. incomingMessage.getClass().equals(ImageMessage.class) kanei process image message
         */
        private void processIncomingMessages() {
            Object incomingMessage;
            while (true) {
                try {
                    if (socket.getInputStream().available() > 0 && !socket.isClosed()) {
                        incomingMessage = objectInputStream.readObject();
                        if (TextMessage.class.equals(incomingMessage.getClass())) {
                            topicsMessages.get(currentTopic).add((TextMessage) incomingMessage); // add message to topicsMessages list
                        } else if (ImageMessage.class.equals(incomingMessage.getClass())) {
                            ImageMessage tempImageMessage = new ImageMessage(
                                    ((ImageMessage) incomingMessage).getUsername(),
                                    ((ImageMessage) incomingMessage).getTopic(),
                                    ((ImageMessage) incomingMessage).getMetadata(),
                                    receiveFileChunks(((ImageMessage) incomingMessage).getMetadata().getFileSize(),
                                            ((ImageMessage) incomingMessage).getMetadata().getFileName()));
                            topicsMessages.get(currentTopic).add(tempImageMessage);
                        } else if (VideoMessage.class.equals(incomingMessage.getClass())) {
                            VideoMessage tempVideoMessage = new VideoMessage(
                                    ((VideoMessage) incomingMessage).getUsername(),
                                    ((VideoMessage) incomingMessage).getTopic(),
                                    ((VideoMessage) incomingMessage).getMetadata(),
                                    receiveFileChunks(((VideoMessage) incomingMessage).getMetadata().getFileSize(),
                                            ((VideoMessage) incomingMessage).getMetadata().getFileName()));
                            topicsMessages.get(currentTopic).add(tempVideoMessage);
                        } else if (brokerPortsAndTopics.getClass().equals(incomingMessage.getClass())) {
                            //receive lista brokerPortsAndTopics - update tin topikh brokerPortsAndTopics
                            brokerPortsAndTopics.putAll((Map<? extends Integer, ? extends ArrayList<String>>) incomingMessage);
                            //TODO: add all topics in ObservableArrayList topics
                            topics.clear();
                            for (ArrayList<String> t : brokerPortsAndTopics.values()) {
                                topics.addAll(t);
                            }
                            System.out.println(topics);
                        } else if (String.class.equals(incomingMessage.getClass())) {
                            if (incomingMessage.equals("there?" + currentTopic)) objectOutputStream.writeObject("yes");
                        } else {
                            System.out.println(incomingMessage);
                            System.out.println(incomingMessage.getClass().toString());
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!(e instanceof SocketException || e instanceof EOFException || e instanceof StreamCorruptedException)) {
                        e.printStackTrace();
                    }
                    if (e instanceof StreamCorruptedException) {
                        disconnect();
                        connectToBroker(currentBrokerPort);
                        setTopic();
                    }
                }
            }
        }

        /**
         * zitaei tin lista me ta ports kai topics apo ton broker
         */
        public void requestTopics() {
            try {
                objectOutputStream.writeObject("/getTopics");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * pernei ta file chunks ena ena kai ta sinthetei se ena file xrisimopoiwntas tin util
         */
        public File receiveFileChunks(long fileSize, String fileName) {
            ArrayList<byte[]> chunksList = new ArrayList<>();
            for (int i = 0; i <= fileSize / util.chunkSize; i++) {
                try {
                    chunksList.add((byte[]) objectInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            //TODO: items now go to folder Downloads
            String state = Environment.getExternalStorageState();
            //external storage availability check
            if (!Environment.MEDIA_MOUNTED.equals(state)) {
                return null;
            }
            File incomingFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            return util.mergeChunksToFile(chunksList, incomingFile);
        }

        /**
         * sends "/disconnect" to Broker and then closes streams and socket
         */
        public void disconnect() {
            try {
                objectOutputStream.writeObject("/disconnect");
                objectOutputStream.close();
                objectInputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
