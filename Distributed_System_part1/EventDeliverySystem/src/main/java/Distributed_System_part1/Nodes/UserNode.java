package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.ImageMessage;
import Distributed_System_part1.Model.Message;
import Distributed_System_part1.Model.TextMessage;
import Distributed_System_part1.Model.VideoMessage;
import Distributed_System_part1.Util.Util;

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
    public Util util;

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
        util = new Util();
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
                            if (file.delete()) System.out.println("Deleted file:" + file.getName());
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
                    if (!topicsMessages.containsKey(currentTopic)) topicsMessages.put(currentTopic, new ArrayList<>());
                    publisher.setTopic();
                    consumer.setTopic();
                } else if (userInput.equals("/topics")) {
                    consumer.requestTopics();
                } else if (userInput.startsWith("/image ")) {
                    // send ImageMessage
                    try {
                        File image;
                        if ((userInput).substring(7).contains("/") || (userInput).substring(7).contains("\\") || (userInput).substring(7).contains(".")) {
                            image = new File((userInput).substring(7));
                        } else {
                            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("images/" + userInput.substring(7) + ".jpg");
                            image = new File(userInput.substring(7) + ".jpg");
                            OutputStream os = new FileOutputStream(image);
                            is.transferTo(os);
                        }
                        publisher.sendMessage(new ImageMessage(username, currentTopic, util.extractImageMetadata(image), image));
                        System.gc();
                        image.delete();
                    } catch (NullPointerException e) {
                        System.out.println("File does not exist/Incorrect file path");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (userInput.startsWith("/video ")) {
                    // send VideoMessage
                    try {
                        File video;
                        if ((userInput).substring(7).contains("/") || (userInput).substring(7).contains("\\") || (userInput).substring(7).contains(".")) {
                            video = new File((userInput).substring(7));
                        } else {
                            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("videos/" + userInput.substring(7) + ".mp4");
                            video = new File(userInput.substring(7) + ".mp4");
                            OutputStream os = new FileOutputStream(video);
                            is.transferTo(os);
                        }
                        publisher.sendMessage(new VideoMessage(username, currentTopic, util.extractVideoMetadata(video), video));
                        System.gc();
                        video.delete();
                    } catch (NullPointerException e) {
                        System.out.println("File does not exist/Incorrect file path");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (userInput.equals("/images")) {
                    System.out.println("clouds\ndog\nflowers\nparis\nUse /image <image name> to send image.");
                } else if (userInput.equals("/videos")) {
                    System.out.println("birds\nmolten-metal\nslowmo-dog\nwingsuit\nUse /video <video name> to send video.");
                } else if (userInput.equals("/quit")) {
                    publisher.disconnect();
                    consumer.disconnect();
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
         *
         * @param port
         */
        public void connectToBroker(int port) {
            try {
                socket = new Socket(url, port);
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
            try {
                if (message instanceof TextMessage) objectOutputStream.writeObject(message);
                else if (message instanceof ImageMessage) {
                    objectOutputStream.writeObject(new ImageMessage(username, currentTopic, ((ImageMessage) message).getMetadata()));
                    sendFileChunks(util.splitFileToChunks(((ImageMessage) message).getContent(), 1));
                } else if (message instanceof VideoMessage) {
                    objectOutputStream.writeObject(new VideoMessage(username, currentTopic, ((VideoMessage) message).getMetadata()));
                    sendFileChunks(util.splitFileToChunks(((VideoMessage) message).getContent(), 1));
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
         *
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
            Object incomingMessage;
            while (true) {
                try {
                    if (socket.getInputStream().available() > 0 && !socket.isClosed()) {
                        incomingMessage = objectInputStream.readObject();
                        if (TextMessage.class.equals(incomingMessage.getClass())) {
                            topicsMessages.get(currentTopic).add((TextMessage) incomingMessage); // add message to topicsMessages list
                            System.out.println(((TextMessage) incomingMessage).getUsername() +
                                    ": " + ((TextMessage) incomingMessage).getContent());
                        } else if (ImageMessage.class.equals(incomingMessage.getClass())) {
                            ImageMessage tempImageMessage = new ImageMessage(
                                    ((ImageMessage) incomingMessage).getUsername(),
                                    ((ImageMessage) incomingMessage).getTopic(),
                                    ((ImageMessage) incomingMessage).getMetadata(),
                                    receiveFileChunks(((ImageMessage) incomingMessage).getMetadata().getFileSize(),
                                            ((ImageMessage) incomingMessage).getMetadata().getFileName()));
                            topicsMessages.get(currentTopic).add(tempImageMessage);
                            System.out.println(((ImageMessage) incomingMessage).getUsername() +
                                    " sent image " + ((ImageMessage) incomingMessage).getMetadata().getFileName());
                        } else if (VideoMessage.class.equals(incomingMessage.getClass())) {
                            VideoMessage tempVideoMessage = new VideoMessage(
                                    ((VideoMessage) incomingMessage).getUsername(),
                                    ((VideoMessage) incomingMessage).getTopic(),
                                    ((VideoMessage) incomingMessage).getMetadata(),
                                    receiveFileChunks(((VideoMessage) incomingMessage).getMetadata().getFileSize(),
                                            ((VideoMessage) incomingMessage).getMetadata().getFileName()));
                            topicsMessages.get(currentTopic).add(tempVideoMessage);
                            System.out.println(((VideoMessage) incomingMessage).getUsername() +
                                    " sent video " + ((VideoMessage) incomingMessage).getMetadata().getFileName());
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
         * pernei ta file chunks ena ena kai ta sinthetei se ena file xrisimopoiwntas tin util
         */
        public File receiveFileChunks(long fileSize, String fileName) {
            ArrayList<byte[]> chunksList = new ArrayList<>();
            int chunkSize = 1024; //TODO fix chunksize allover the project
            for (int i = 0; i <= fileSize / chunkSize; i++) {
                try {
                    chunksList.add((byte[]) objectInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            File incomingFile = new File(username + "/" + fileName);
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
