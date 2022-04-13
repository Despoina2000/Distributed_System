package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.ImageMessage;
import Distributed_System_part1.Model.Message;
import Distributed_System_part1.Model.TextMessage;
import Distributed_System_part1.Model.VideoMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * user node class, starts publisher and consumer
 */
public class UserNode {
    public static final int BROKER1 = 4000;
    public static final int BROKER2 = 5555;
    public static final int BROKER3 = 5984;
    public final String url = "localhost";

    public volatile String username;
    public volatile int currentBrokerPort;
    public volatile String currentTopic = null;
    public volatile HashMap<Integer, ArrayList<String>> brokerPortsAndTopics;
    public volatile HashMap<String, ArrayList<Message>> topicsMessages; //edw mporoume na kratame osa minimata exoume diavasei idi

    private Publisher publisher;
    private Consumer consumer;
    private BufferedReader br;

    /**
     * Main thread: publisher, other thread: consumer
     */
    public UserNode() {
        //read and set username
        brokerPortsAndTopics = new HashMap<Integer, ArrayList<String>>();
        topicsMessages = new HashMap<String, ArrayList<Message>>();
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Your username:");
            this.username = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // currentBrokerPort = random broker port
        Random random = new Random();
        switch (random.nextInt(3)) {
            case 0 -> this.currentBrokerPort = BROKER1;
            case 1 -> this.currentBrokerPort = BROKER2;
            case 2 -> this.currentBrokerPort = BROKER3;
        }
        publisher = new Publisher();
        consumer = new Consumer(this);
        consumer.start();
        // create new Folder with name username (to store images and videos)
        File userDirectory = new File("./" + username);
        if (userDirectory.mkdirs()) System.out.println("Created user directory.");
        userDirectory.deleteOnExit();

        //destructor to delete folder and files, kaleitai otan kleinoume to app me CTRL+C h /quit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (userDirectory.exists()) {
                    File[] allContents = userDirectory.listFiles();
                    if (allContents != null) {
                        for (File file : allContents) {
                            if (file.delete()) System.out.println("Deleted file:" + file.getName());;
                        }
                    }
                }
                if (userDirectory.delete()) System.out.println("Deleted folder:" + userDirectory.getName());
            }
        });

        startCLI();
        System.exit(0);
    }

    /**
     * arxizei to command line interface gia na dwsoume entoles (px /topic , message klp)
     */
    private void startCLI() {


        System.out.println("Command line interface started:");
        String userInput = " ";
        while (!userInput.equals("/quit")) {
            try {
                userInput = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (userInput.startsWith("/")) {
                if (userInput.startsWith("/topic ")) {
                    this.currentTopic = userInput.substring(7);
                    publisher.setTopic();
                    consumer.setTopic();
                } else if (userInput.equals("/topics")) {
                    consumer.requestTopics();
                } else if (userInput.startsWith("/image ")) {
                    // send ImageMessage
                    System.out.println(Thread.currentThread().getContextClassLoader().getResource("images/" + userInput.substring(7) + ".jpg"));
                } else if (userInput.startsWith("/video ")) {
                    // send VideoMessage
                    System.out.println(Thread.currentThread().getContextClassLoader().getResource("videos/" + userInput.substring(7) + ".mp4"));
                } else if (userInput.equals("/images")) {
                    System.out.println("clouds\ndog\nflowers\nparis\nUse /image <image name> to send image.");
                } else if (userInput.equals("/videos")) {
                    System.out.println("birds\nmolten-metal\nslowmo-dog\nwingsuit\nUse /video <video name> to send image.");
                } else if (userInput.equals("/quit")) {
                    break;
                } else {
                    System.out.println("*******************************************************");
                    System.out.println("*   USAGE:                                            *");
                    System.out.println("*******************************************************");
                    System.out.println("*   /topic <topic>      : set current topic           *");
                    System.out.println("*   /topics             : request available topics    *");
                    System.out.println("*   <message>           : send new TextMessage        *");
                    System.out.println("*   /image <imagepath>  : send new ImageMessage       *");
                    System.out.println("*   /video <videopath>  : send new VideoMessage       *");
                    System.out.println("*   /images             : lists available images      *");
                    System.out.println("*   /videos             : lists available videos      *");
                    System.out.println("*   /help               : display this message        *");
                    System.out.println("*   /quit               : close application           *");
                    System.out.println("*******************************************************");
                }
            } else {
                if (currentTopic == null) {
                    System.out.println("You have to set a topic first with: /topic <your topic> (type /help for help");
                } else {
                    publisher.sendMessage(new TextMessage(username, currentTopic, userInput));
                }
            }
        }
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
         * @param port
         */
        public void connectToBroker(int port) {
            try {
                socket = new Socket(url, port);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject("publisher");
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                if (objectInputStream.readObject().equals("username?"))
                    objectOutputStream.writeObject(username);
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

            //TODO: check an exoume idi tin pliroforia sto brokerPortsAndTopics allios:
            try {
                //send to broker currentTopic
                objectOutputStream.writeObject(currentTopic);
                //perimenoume apantisi apo broker,
                Object brokerAnswer = objectInputStream.readObject();
                //perimenoume na mas pei o broker na sinexisoume
                if (brokerAnswer.equals("continue")) {
                    System.out.println("broker sent continue");
                } else {
                    // an i apantisi einai broker port thetoume currentBrokerPort = port
                    // kai kanoume connectToBroker(port), consumer.connectToBroker(port) kai ksana setTopic kai consumer.setTopic
                    currentBrokerPort = Integer.parseInt((String) brokerAnswer);
                    publisher.disconnect();
                    publisher.connectToBroker(currentBrokerPort);
                    publisher.setTopic();
                    consumer.disconnect();
                    consumer.connectToBroker(currentBrokerPort);
                    consumer.setTopic();

                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(Message message) {
            //TODO: handle Text/Image/VideoMessage
            try {
                objectOutputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendFileChunks(ArrayList<byte[]> fileChunks) {
            //TODO
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
        private Socket socket;
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
         * @param port
         */
        public void connectToBroker(int port) {
            try {
                //connect to broker at port
                socket = new Socket(url, port);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.flush();
                //send "consumer"
                objectOutputStream.writeObject("consumer");
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
            //TODO
            //if message.getContentType=="text"/"image"/"video/ do this..
            // an to mesage einai imagemessage i videomessage arxizoume na diavazoume byte[] se mia arraylist
            // kai to sinthetoume meta me mergeChunksToFile se new ImageMessage/VideoMessage me ton overloaded constructor pou periexei kai to content

            // prosthiki tou message sto topicsMessages (sto message.getTopic)
            Object incomingMessage;
            while (true) {
                try {
                    if (socket.getInputStream().available() > 0 && !socket.isClosed()) {
                        incomingMessage = objectInputStream.readObject();
                        if (TextMessage.class.equals(incomingMessage.getClass())) {
                            System.out.println(incomingMessage);
                            //TODO: add message to topicsMessages list
                        } else if (ImageMessage.class.equals(incomingMessage.getClass())) {
                            //TODO: handle ImageMessage
                        } else if (VideoMessage.class.equals(incomingMessage.getClass())) {
                            //TODO: handle VideoMessage
                        } else if (brokerPortsAndTopics.getClass().equals(incomingMessage.getClass())) {
                            //receive lista brokerPortsAndTopics - update tin topikh brokerPortsAndTopics
                            brokerPortsAndTopics.putAll((Map<? extends Integer, ? extends ArrayList<String>>) incomingMessage);
                            System.out.println(brokerPortsAndTopics);
                        } else if (String.class.equals(incomingMessage.getClass())) {
                            if (incomingMessage.equals("there?")) objectOutputStream.writeObject("yes");
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
