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
        //TODO
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
            case 1 -> this.currentBrokerPort = BROKER1;
            case 2 -> this.currentBrokerPort = BROKER1;
        }
        publisher = new Publisher();
        consumer = new Consumer(this);
        consumer.start();
        // create new Folder with name username (to store images and videos)

        //destructor to delete folder and files, kaleitai otan kleinoume to app me CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //TODO: delete all files in folder "username" and delete folder
            }
        });

        startCLI();
        System.exit(0);
    }

    /**
     * arxizei to command line interface gia na dwsoume entoles (px /topic, message klp)
     */
    private void startCLI() {
        //TODO
        /*
         * pithanes entoles:
         * /topic <topic> (currentTopic = <topic>; publisher.setTopic(); consumerThread.setTopic();)
         * /topics (consumer.requestTopics)
         * <sketo message> (sendMessage(new TextMessage...))
         * /image <imagepath> (sendMessage(new ImageMessage xwris to content)) meta arxizoume kai stelnoume ta byte[]
         *                 apo tin ArrayList<byte[]> splitFileToChunks isws me tin sendFileChunks(splitFileToChunks)
         * /video <videopath> (sendMessage(new VideoMessage xwris to content)) meta arxizoume kai stelnoume ta byte[]
         *                apo tin ArrayList<byte[]> splitFileToChunks isws me tin sendFileChunks(splitFileToChunks)
         */
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
                } else if (userInput.startsWith("/video ")) {
                    // send VideoMessage
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

        public void setTopic() {
            //TODO

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
            //TODO
            try {
                objectOutputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendFileChunks(ArrayList<byte[]> fileChunks) {
            //TODO
        }

        public void disconnect(){
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
            //TODO
            connectToBroker(currentBrokerPort);
            requestTopics();
            processIncomingMessages();
        }

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
                e.printStackTrace();
            }
        }

        public void setTopic() {
            try {
                //TODO
                //send to broker currentTopic
                objectOutputStream.writeObject(currentTopic);
                //edw mporoume na kanoume print olo to istoriko gia to currentTopic apo to topicsMessages kai tha perimenoume gia kainouria minimata
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                        } else if (ImageMessage.class.equals(incomingMessage.getClass())) {
                            //handle ImageMessage
                        } else if (VideoMessage.class.equals(incomingMessage.getClass())) {
                            //handle VideoMessage
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
                    if (!(e instanceof SocketException || e instanceof EOFException)) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void requestTopics() {
            //TODO
            //zitaei tin lista me ta topic apo ton broker
            //receive lista brokerPortsAndTopics
            //kanei update tin topikh brokerPortsAndTopics
            try {
                objectOutputStream.writeObject("/getTopics");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void disconnect(){
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
