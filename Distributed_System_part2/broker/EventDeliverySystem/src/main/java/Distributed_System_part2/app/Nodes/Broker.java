package Distributed_System_part2.app.Nodes;

import Distributed_System_part2.app.Model.ImageMessage;
import Distributed_System_part2.app.Model.Message;
import Distributed_System_part2.app.Model.TextMessage;
import Distributed_System_part2.app.Model.VideoMessage;
import Distributed_System_part2.app.Util.Util;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * broker class, handles connections with publishers and consumers
 */
public class Broker implements Runnable {

    public static final int PORT_BROKER1 = 4000;
    public static final int PORT_BROKER2 = 5555;
    public static final int PORT_BROKER3 = 5984;
    public static final String URL_BROKER1 = "127.0.0.1";
    public static final String URL_BROKER2 = "127.0.0.1";
    public static final String URL_BROKER3 = "127.0.0.1";
    public static Util util;
    public final String url = "localhost";
    public int port;
    public ArrayList<ObjectOutputStream> otherBrokersOutputStreams;
    /**
     * volatile = koino gia ola ta threads
     * edw kratame poios broker(to port tou) einai ipefthinos gia poia topics, ginetai update opote mpainei kainourio topic
     * xrisimevei kai ws lista olwn twn topic pou iparxoun gia na stelnoume ston consumer
     */
    volatile public HashMap<Integer, ArrayList<String>> brokerPortsAndTopics;
    /**
     * edw kratame gia kathe topic tou broker tin lista me ta messages
     * (den evala queue gia na min adeiazei na mporoume na ta ksanasteiloume)
     */
    volatile public HashMap<String, ArrayList<Message>> topicsMessages;
    /**
     * istoriko: mexri pio message exei lavei o consumer gia kathe topic {@code <username<topic,index>> }
     */
    volatile public HashMap<String, HashMap<String, Integer>> usernamesTopicsIndex;
    private ServerSocket serverSocket;


    /**
     * Constructor tou broker
     * katalavenei an einai o prwtos,defteros i tritos broker kai analoga kanei initialize tin port.
     */
    public Broker(int brokerNumber) {

        switch (brokerNumber) {
            case 1 -> this.port = PORT_BROKER1;
            case 2 -> this.port = PORT_BROKER2;
            case 3 -> this.port = PORT_BROKER3;
        }

        //initialize util
        util = new Util();

        //initialize hashmaps/arraylists
        otherBrokersOutputStreams = new ArrayList<>();
        brokerPortsAndTopics = new HashMap<>();
        topicsMessages = new HashMap<>();
        usernamesTopicsIndex = new HashMap<>();

        //initialize inner hashmaps/arraylists
        brokerPortsAndTopics.put(PORT_BROKER1, new ArrayList<>());
        brokerPortsAndTopics.put(PORT_BROKER2, new ArrayList<>());
        brokerPortsAndTopics.put(PORT_BROKER3, new ArrayList<>());
    }

    /**
     * Edw arxizei na trexei o broker se diko tou thread.
     * Mesa tha dimiourgei kainouria thread gia kathe connection.
     * Arxika, an einai o defteros i tritos broker kanei establish sindesi me tous ipolipous brokers
     */
    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
            //an einai o defteros broker kanei connect me ton prwto kai vazei to socket stin otherBrokers
            if (port == PORT_BROKER2) {
                System.out.println("Connecting to Broker1.");
                Socket broker1Socket = new Socket(URL_BROKER1, PORT_BROKER1);
                ObjectOutputStream broker1Writer = new ObjectOutputStream(broker1Socket.getOutputStream());
                broker1Writer.flush();
                otherBrokersOutputStreams.add(broker1Writer);
                broker1Writer.writeObject("broker");
                ObjectInputStream broker1Reader = new ObjectInputStream(broker1Socket.getInputStream());
                new Thread(new BrokerBrokerConnection(broker1Socket, this, broker1Reader)).start(); // kanourio BrokerBrokerConnection
            }
            //an einai o tritos broker kanei connect me tous allous 2 kai vazei ta outputstreams stin otherBrokersOutputStream
            if (port == PORT_BROKER3) {
                System.out.println("Connecting to Broker1.");
                Socket broker1Socket = new Socket(URL_BROKER1, PORT_BROKER1);
                ObjectOutputStream broker1Writer = new ObjectOutputStream(broker1Socket.getOutputStream());
                broker1Writer.flush();
                otherBrokersOutputStreams.add(broker1Writer);
                broker1Writer.writeObject("broker");
                ObjectInputStream broker1Reader = new ObjectInputStream(broker1Socket.getInputStream());
                new Thread(new BrokerBrokerConnection(broker1Socket, this, broker1Reader)).start(); // kanourio BrokerBrokerConnection
                System.out.println("Connecting to Broker2.");
                Socket broker2Socket = new Socket(URL_BROKER2, PORT_BROKER2);
                ObjectOutputStream broker2Writer = new ObjectOutputStream(broker2Socket.getOutputStream());
                broker2Writer.flush();
                otherBrokersOutputStreams.add(broker2Writer);
                broker2Writer.writeObject("broker");
                ObjectInputStream broker2Reader = new ObjectInputStream(broker2Socket.getInputStream());
                new Thread(new BrokerBrokerConnection(broker2Socket, this, broker2Reader)).start(); // kanourio BrokerBrokerConnection
            }
            System.out.println("Broker listening at port " + port);

            while (true) {
                acceptConnection();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * An to connection einai apo Publisher dimiourgei kainourio thread BrokerPublisherConnection,
     * an einai Consumer kainourio thread BrokerConsumerConnection
     * kai stis 2 periptwseis pernaei to socket,'this'(parent broker) kai ta object output/input streams sto thread.
     * An einai Broker prosthetoume to outputstream tou stin lista otherBrokersOutputStreams(gia tin notifyBrokers)
     */
    private void acceptConnection() {
        //to prwto minima einai "publisher","consumer" h "broker"
        try {
            Socket newConnection = serverSocket.accept();
            ObjectOutputStream newConnectionOutput = new ObjectOutputStream(newConnection.getOutputStream());
            newConnectionOutput.flush();
            ObjectInputStream newConnectionInput = new ObjectInputStream(newConnection.getInputStream());
            String firstLine = (String) newConnectionInput.readObject();
//            System.out.println("first line received: " + firstLine);
            switch (firstLine) {
                case "broker" -> {  //an to prwto minima einai broker swzoume to outputstream tou stin lista otherBrokersOutputStreams
//                    System.out.println("Broker connected, saving stream to list otherBrokersOutputStreams.");
                    otherBrokersOutputStreams.add(newConnectionOutput);
                    for (ObjectOutputStream oos : otherBrokersOutputStreams) oos.flush();
                    new Thread(new BrokerBrokerConnection(newConnection, this, newConnectionInput)).start(); // kanourio BrokerBrokerConnection
                }
                //an to prwto minima einai publisher ftiaxnoume kainourio BrokerPublisherConnection thread
                case "publisher" -> new Thread(new BrokerPublisherConnection(newConnection, this, newConnectionOutput, newConnectionInput)).start();
                //an to prwto minima einai consumer ftiaxnoume kainourio BrokerConsumerConnection thread
                case "consumer" -> new Thread(new BrokerConsumerConnection(newConnection, this, newConnectionOutput, newConnectionInput)).start();
                default -> {
                    System.out.println("Wrong identifier from socket: " + firstLine + " Expected \"broker\" or \"publisher\" or \"consumer\"");
                    newConnection.close();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * xrisimopoiei thn hash apo to Util.java gia na vrei se poion broker anoikei to topic
     * kai epistrefei tin port aftou tou broker
     *
     * @param topic String topic
     * @return port tou responsible gia to topic broker
     * @see Util#hash(String topic)
     */
    private int getResponsibleBrokerPort(String topic) {
        switch ((util.hash(topic).mod(BigInteger.valueOf(3))).intValue()) {
            case 0 -> {
                return PORT_BROKER1;
            }
            case 1 -> {
                return PORT_BROKER2;
            }
            case 2 -> {
                return PORT_BROKER3;
            }
        }
        return 0; // something went wrong
    }

    /**
     * stelnoume stous allous broker (lista otherBrokersOutputStreams) to kainourio topic
     * kai aftoi to prosthetoun stin brokerPortAndTopics tous mazi me to port mas
     *
     * @param topic String topic
     */
    private void notifyBrokers(String topic) {
        for (ObjectOutputStream brokerOutputStream : otherBrokersOutputStreams) {
            try {
                brokerOutputStream.writeObject(Integer.toString(port) + topic); // notify other brokers about our port and the new topic
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * h acceptConnection dimiourgei kainourio thread gia kathe Publisher connected,
     * ston constructor pairname (socket,this,objectoutputstream,objectinputstream) to this einai o parent broker
     * gia na kalesoume px parent.notifyBrokers(topic)
     */
    private class BrokerPublisherConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private String currentTopic = null;
        private ObjectOutputStream brokerPublisherOutputStream;
        private ObjectInputStream brokerPublisherInputStream;

        public BrokerPublisherConnection(Socket socket, Broker parent, ObjectOutputStream brokerPublisherOutputStream, ObjectInputStream brokerPublisherInputStream) {
//            System.out.println("Started new BrokerPublisherConnectionThread");
            this.socket = socket;
            this.parent = parent;
            this.brokerPublisherInputStream = brokerPublisherInputStream;
            this.brokerPublisherOutputStream = brokerPublisherOutputStream;
        }

        /**
         * handles tin epikoinwnia me ton Publisher
         * <p>
         * arxika rotaei ton xristi gia to username tou kai to swzei sto this.username
         * meta mpenei sto while loop pou koitaei an to incoming message anoikei stin superclass Message.class
         * xeirizetai to message analoga me tin class tou (px ImageClass)
         * an to incoming message den anoikei stin superclass Message.class simainei oti o user esteile string
         * opote koitaei: an to string einai "/disconnect" kleinei ta streams kai to socket
         * alliws to string einai pio topic thelei o user
         * an aftos o broker einai responsible gia to topic stelnei "continue"
         * allios xrisimopoiei tin getResponsibleBroker(topic) kai stelnei tin port tou katallilou Broker
         */
        @Override
        public void run() {
            try {
                //stelnoume "username?" gia na dwsei o publisher to username tou
                brokerPublisherOutputStream.writeObject("username?");
                //o publisher stelnei to username tou
                this.username = (String) brokerPublisherInputStream.readObject();
                System.out.println("Publisher with username: " + username + " connected.");
                //an einai prwti fora pou vlepoume to username kanoume initialize tin usernamesTopicsIndex
                if (!usernamesTopicsIndex.containsKey(username))
                    usernamesTopicsIndex.put(username, new HashMap<String, Integer>());

                while (!socket.isClosed()) {
                    try {
                        Object incomingMessage;
                        while ((incomingMessage = brokerPublisherInputStream.readObject()).getClass().getSuperclass() == Message.class) {
//                            System.out.println("reading message");
                            //xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                            if (incomingMessage.getClass() == TextMessage.class) {
                                topicsMessages.get(currentTopic).add((TextMessage) incomingMessage); //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages
                                //xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                            } else if (incomingMessage.getClass() == ImageMessage.class) {
                                topicsMessages.get(currentTopic).add(new ImageMessage(((ImageMessage) incomingMessage).getUsername(),
                                        ((ImageMessage) incomingMessage).getTopic(),
                                        ((ImageMessage) incomingMessage).getMetadata(),
                                        receiveFileChunks(brokerPublisherInputStream, ((ImageMessage) incomingMessage).getMetadata().getFileSize())));
                            } else if (incomingMessage.getClass() == VideoMessage.class) {
                                topicsMessages.get(currentTopic).add(new VideoMessage(((VideoMessage) incomingMessage).getUsername(),
                                        ((VideoMessage) incomingMessage).getTopic(),
                                        ((VideoMessage) incomingMessage).getMetadata(),
                                        receiveFileChunks(brokerPublisherInputStream, ((VideoMessage) incomingMessage).getMetadata().getFileSize())));
                            }
                            synchronized (topicsMessages.get(currentTopic)) {
                                topicsMessages.get(currentTopic).notifyAll(); //kaloume tin topicsMessages.get(topic).notifyAll(); gia na staloun oles oi allages stous consumers
                            }
                            //Print out broker messages
//                            System.out.print("topicsMessages: ");
//                            System.out.println(topicsMessages);
                        }
                        if (incomingMessage.equals("/disconnect")) {
                            throw new SocketException();
                        } else {
                            //o publisher stelnei se pio topic thelei na steilei message
                            currentTopic = (String) incomingMessage;
//                            System.out.println("Publisher:" + username + " set topic:" + incomingMessage);
                            if (brokerPortsAndTopics.get(port).contains(currentTopic)) { //an aftos o broker exei hdh afto to topic, (an iparxei idi stin brokerPortsAndTopics)
                                brokerPublisherOutputStream.writeObject("continue");// leme ston publisher oti mporei na sinexisei
                            } else if (getResponsibleBrokerPort(currentTopic) == port) { //an aftos o broker einai ipefthinos gia to kainourio topic (an i getResponsibleBrokerPort vgazei tin diki mas port diladi)
                                //dimiourgoume to topic kai to vazoume mazi me ton user stin lista usernamesTopicsIndex me index -1
                                if (!topicsMessages.containsKey(currentTopic))
                                    topicsMessages.put(currentTopic, new ArrayList<Message>());
                                usernamesTopicsIndex.get(username).put(currentTopic, -1);
                                brokerPortsAndTopics.get(port).add(currentTopic);//to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port
                                parent.notifyBrokers(currentTopic);// kai kanoume notify tous brokers gia to kainourio topic
                                brokerPublisherOutputStream.writeObject("continue");// leme ston publisher oti mporei na sinexisei
                            } else { //an oxi xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker kai tou proothoume tin port
                                brokerPublisherOutputStream.writeObject(Integer.toString(getResponsibleBrokerPort(currentTopic)));
//                                System.out.println("sent " + Integer.toString(getResponsibleBrokerPort(currentTopic)));
                            }
                        }
                    } catch (SocketException | EOFException e) {
                        socket.close();
                        System.out.println("Publisher with username:" + username + " disconnected.");
                    }
                }//end while
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * Pairnei to arxeio apo ton publisher kommati kommati
         *
         * @param brokerPublisherInputStream
         * @param fileSize
         * @return
         */
        ArrayList<byte[]> receiveFileChunks(ObjectInputStream brokerPublisherInputStream, long fileSize) {
            ArrayList<byte[]> chunksList = new ArrayList<>();
            for (int i = 0; i <= fileSize / util.chunkSize; i++) {
                try {
                    chunksList.add((byte[]) brokerPublisherInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            return chunksList;
        }
    }

    /**
     * h acceptConnection dimiourgei kainourio thread gia kathe Consumer connected,
     * ston constructor pairname (socket,this,objectoutputstream,objectinputstream) to this einai o parent broker
     */
    private class BrokerConsumerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private ObjectInputStream brokerConsumerInputStream;
        private ObjectOutputStream brokerConsumerOutputStream;


        private String currentTopic = null; // to topic to opoio diavazei twra o consumer

        public BrokerConsumerConnection(Socket socket, Broker parent, ObjectOutputStream brokerConsumerOutputStream, ObjectInputStream brokerConsumerInputStream) {
            this.socket = socket;
            this.parent = parent;
            this.brokerConsumerInputStream = brokerConsumerInputStream;
            this.brokerConsumerOutputStream = brokerConsumerOutputStream;
        }

        /**
         * handles tin epikoinwnia me ton Consumer
         * <p>
         * arxika rotaei ton xristi gia to username tou kai to swzei sto this.username
         * meta mpainei sto while loop opou stelnei ta minimata tou currentTopic ston consumer
         * an o consumer steilei kapoio string koitame:
         * an to string einai "/getTopics" tou stelnoume ta topics me tin sendBrokerTopics()
         * an to string einai "/disconnect" kleinoume ta streams kai tin socket
         * alliws to string einai to kainourio topic pou thelei o consumer opote to thetoume ws current topic
         * <p>
         * episis kathe 5 defterolepta stelnei ston consumer "there?" kai perimenei apantisi "yes"
         * afto to xrisimopoioume gia na kseroume oti o consumer sinexizei kai einai online kai den exei kanei disconnect
         * an den steilei apantisi "yes" kleinoume ta streams kai to socket
         */
        @Override
        public void run() {
            try {
//                System.out.println("Started new brokerConsumerConnectionThread");
                //stelnoume "username?" gia na dwsei o consumer to username tou
                brokerConsumerOutputStream.writeObject("username?");
                //o consumer stelnei to username tou
                this.username = (String) brokerConsumerInputStream.readObject();
                if (username != null) System.out.println("Consumer with username: " + username + " connected.");
                //an einai prwti fora pou vlepoume to username kanoume initialize tin usernamesTopicsIndex
                if (!usernamesTopicsIndex.containsKey(username))
                    usernamesTopicsIndex.put(username, new HashMap<String, Integer>());
//                sendBrokerTopics(); //tou stelnoume tin lista brokerPortsAndTopics (opote kserei ola ta topics kai pou na apefthinthei gia kathe topic)
                String consumerMessage;
                while (!socket.isClosed()) { //oso iparxei sindesi
                    try {
                        //kanoume .wait(1000) sto topicsMessages.get(currentTopic)
                        // kai opote kalesei kapoios notifyAll() sto topicsMessages.<currentTopic> sinexizoume
                        // h otan perasoun 1000ms (gia na exei kai aftomato polling)
                        int i = 0;
                        if (currentTopic != null) { //an exei thesei currentTopic o consumer
                            while (!socket.isClosed() && socket.getInputStream().available() == 0) {
                                i++;
                                if (i == 5) { // check every 5 seconds if consumer is still connected
                                    brokerConsumerOutputStream.writeObject("there?" + currentTopic);
                                    Object consumerAnswer = brokerConsumerInputStream.readObject();
                                    if (!consumerAnswer.equals("yes")) {
                                        if (consumerAnswer.equals("/getTopics")) {
                                            sendBrokerTopics();
                                        } else {
                                            throw new SocketException();
                                        }
                                    }
                                    i = 0;
                                }
                                synchronized (parent.topicsMessages.get(currentTopic)) {
                                    topicsMessages.get(currentTopic).wait(1000);
                                }
                                sendMessages(brokerConsumerOutputStream);
                            }//telos while, opote exei steilei kati o consumer
                        }
                        if ((consumerMessage = (String) brokerConsumerInputStream.readObject()) != null) {
                            if (consumerMessage.equals("/getTopics")) {
                                sendBrokerTopics(); //stelnoume sendBrokerTopics(); kai sinexizei to loop
                            } else if (consumerMessage.equals("/disconnect")) {
                                throw new SocketException();
                            } else {
                                currentTopic = consumerMessage; //o consumer stelnei to topic pou thelei na diavasei kai to thetoume ws current topic
//                                System.out.println("consumer sent topic:" + currentTopic);
                                if (!topicsMessages.containsKey(currentTopic))
                                    topicsMessages.put(currentTopic, new ArrayList<Message>());
//                                System.out.println("sending messages");
                                sendMessages(brokerConsumerOutputStream);
//                                System.out.println("sent messages");
                            }
                        }
                    } catch (SocketException | EOFException e) {
                        socket.close();
                        System.out.println("Consumer with username:" + username + " disconnected.");
                    }
                }
            } catch (SocketException | EOFException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (username != null) System.out.println("Consumer with username:" + username + " disconnected.");
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * tou stelnoume ta messages pou iparxoun idi sto sigkekrimeno topic apo ti lista topicsMessages
         * kanoume update to topicMessageIndex mexri ekei pou tou exoume steilei(-1 an den iparxoun messages)
         */
        private void sendMessages(ObjectOutputStream brokerConsumerOutputStream) {
            try {
                //stelnei ta kainouria messages gia to currentTopic ston consumer
                if (!usernamesTopicsIndex.get(username).containsKey(currentTopic))
                    usernamesTopicsIndex.get(username).put(currentTopic, -1);//an einai kainourio topic to vazoume lista me index -1

                if (topicsMessages.get(currentTopic).size() > usernamesTopicsIndex.get(username).get(currentTopic)) { // an iparxoun perisotera minimata apo osa exei idi diavasei o consumer
                    for (int i = usernamesTopicsIndex.get(username).get(currentTopic) + 1; i < topicsMessages.get(currentTopic).size(); i++) { // gia kathe kainourio minima
                        try {
                            if (topicsMessages.get(currentTopic).get(i).getClass() == TextMessage.class) {
                                //send text message
                                brokerConsumerOutputStream.writeObject(topicsMessages.get(currentTopic).get(i));
                            } else if (topicsMessages.get(currentTopic).get(i).getClass() == ImageMessage.class) {
                                //send image message
                                ImageMessage tempImageMessage = (ImageMessage) topicsMessages.get(currentTopic).get(i);
                                brokerConsumerOutputStream.writeObject(
                                        new ImageMessage(
                                                tempImageMessage.getUsername(),
                                                tempImageMessage.getTopic(),
                                                tempImageMessage.getMetadata()));
                                sendFileChunks(tempImageMessage.getChunkedContent());
                            } else if (topicsMessages.get(currentTopic).get(i).getClass() == VideoMessage.class) {
                                //send video message
                                VideoMessage tempVideoMessage = (VideoMessage) topicsMessages.get(currentTopic).get(i);
                                brokerConsumerOutputStream.writeObject(
                                        new VideoMessage(tempVideoMessage.getUsername(),
                                                tempVideoMessage.getTopic(),
                                                tempVideoMessage.getMetadata()));
                                sendFileChunks(tempVideoMessage.getChunkedContent());
                            }
                            usernamesTopicsIndex.get(username).put(currentTopic, i);//update the current index for this consumer
                        } catch (SocketException e) {
                            socket.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * stelnei ston consumer to arxeio chunk by chunk
         *
         * @param chunks
         */
        private void sendFileChunks(ArrayList<byte[]> chunks) {
            for (byte[] chunk : chunks) {
                try {
                    brokerConsumerOutputStream.writeObject(chunk);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * stelnei ston consumer ta available ports+topics
         */
        private void sendBrokerTopics() {
            try {
                brokerConsumerOutputStream.reset();
                brokerConsumerOutputStream.writeObject(brokerPortsAndTopics);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * h acceptConnection dimiourgei kainourio thread gia kathe Broker connected,
     * ston constructor pairname (socket,this,objectinputstream) to this einai o parent broker
     */
    private class BrokerBrokerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private ObjectInputStream brokerBrokerInputStream;

        public BrokerBrokerConnection(Socket socket, Broker parent, ObjectInputStream brokerBrokerInputStream) {
            this.socket = socket;
            this.parent = parent;
            this.brokerBrokerInputStream = brokerBrokerInputStream;
        }

        /**
         * handles tin epikoinwnia me tous allous Broker
         * <p>
         * perimenei minima apo kapoion broker
         * to minima tha einai port+topic opote to vazei stin brokerPortsAndTopics
         */
        @Override
        public void run() {
            System.out.println("Started new brokerBrokerConnectionThread");
            while (!socket.isClosed()) { // oso einai anoixto to socket
                try {
                    String incomingPortAndTopic;
                    if ((incomingPortAndTopic = (String) brokerBrokerInputStream.readObject()) != null) {
                        String incomingPort = incomingPortAndTopic.substring(0, 4);
                        String incomingTopic = incomingPortAndTopic.substring(4);
                        //add port and topic to brokerPortsAndTopics
                        brokerPortsAndTopics.get(Integer.parseInt(incomingPort)).add(incomingTopic);
                        System.out.println("Added to brokerPortsAndTopics in port:" + incomingPort + " topic:" + incomingTopic);
                        System.out.println(brokerPortsAndTopics);
                    }
                } catch (IOException e) {
                    try {
                        for (ObjectOutputStream brokerOutputStream : otherBrokersOutputStreams) {
                            if (brokerOutputStream == socket.getOutputStream())
                                otherBrokersOutputStreams.remove(brokerOutputStream);
                        }
                        socket.close();
                        System.out.println("A broker disconnected.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
