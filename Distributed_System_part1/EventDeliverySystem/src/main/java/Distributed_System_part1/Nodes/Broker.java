package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.ImageMessage;
import Distributed_System_part1.Model.Message;
import Distributed_System_part1.Model.TextMessage;
import Distributed_System_part1.Model.VideoMessage;
import Distributed_System_part1.Util.Util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * broker class, handles connections with publishers and consumers
 */
public class Broker implements Runnable {

    public static final int BROKER1 = 4000;
    public static final int BROKER2 = 5555;
    public static final int BROKER3 = 5984;

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
    public Broker() {

        //destructor, deletes initBroker.txt if exists
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                File brokerFile = new File("initBroker.txt");
                System.gc();
                if (brokerFile.exists()) System.out.println(brokerFile.delete());
            }
        });

        //initialize port according to what number broker it is, uses shared file initBroker.txt
        try {
            File brokerFile = new File("initBroker.txt");
            //create file initBroker.txt if not exists
            if (brokerFile.createNewFile()) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(brokerFile));
                bw.write("0\n");
                bw.close();
            }
            BufferedReader br = new BufferedReader(new FileReader(brokerFile));
            String brokerFileLine = br.readLine();
            BufferedWriter bw = new BufferedWriter(new FileWriter(brokerFile));
            switch (brokerFileLine) {
                case "0" -> {
                    this.port = BROKER1;
                    bw.write("1\n");
                    br.close();
                    bw.close();
                }
                case "1" -> {
                    this.port = BROKER2;
                    bw.write("2\n");
                    br.close();
                    bw.close();
                }
                case "2" -> {
                    this.port = BROKER3;
                    br.close();
                    bw.close();
                    System.gc();
                    brokerFile.delete(); // deletes file initBroker.txt
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Broker port set to: " + port);

        //initialize hashmaps/arraylists
        otherBrokersOutputStreams = new ArrayList<>();
        brokerPortsAndTopics = new HashMap<>();
        topicsMessages = new HashMap<>();
        usernamesTopicsIndex = new HashMap<>();

        //initialize inner hashmaps/arraylists
        brokerPortsAndTopics.put(BROKER1, new ArrayList<>());
        brokerPortsAndTopics.put(BROKER2, new ArrayList<>());
        brokerPortsAndTopics.put(BROKER3, new ArrayList<>());
    }

    /**
     * Edw arxizei na trexei o broker se diko tou thread.
     * Mesa tha dimiourgei kainouria thread gia kathe connection.
     */
    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
            //an einai o defteros broker kanei connect me ton prwto kai vazei to socket stin otherBrokers
            if (port == BROKER2) {
                System.out.println("Connecting to Broker1.");
                Socket broker1Socket = new Socket(url, BROKER1);
                ObjectOutputStream broker1Writer = new ObjectOutputStream(broker1Socket.getOutputStream());
                otherBrokersOutputStreams.add(broker1Writer);
                broker1Writer.writeObject("broker");
                broker1Writer.flush();
                new Thread(new BrokerBrokerConnection(broker1Socket, this)).start(); // kanourio BrokerBrokerConnection
            }
            //an einai o tritos broker kanei connect me tous allous 2 kai vazei ta outputstreams stin otherBrokersOutputStream
            if (port == BROKER3) {
                System.out.println("Connecting to Broker1.");
                Socket broker1Socket = new Socket(url, BROKER1);
                ObjectOutputStream broker1Writer = new ObjectOutputStream(broker1Socket.getOutputStream());
                otherBrokersOutputStreams.add(broker1Writer);
                broker1Writer.writeObject("broker");
                new Thread(new BrokerBrokerConnection(broker1Socket, this)).start(); // kanourio BrokerBrokerConnection
                System.out.println("Connecting to Broker2.");
                Socket broker2Socket = new Socket(url, BROKER2);
                ObjectOutputStream broker2Writer = new ObjectOutputStream(broker2Socket.getOutputStream());
                otherBrokersOutputStreams.add(broker2Writer);
                broker2Writer.writeObject("broker");
                new Thread(new BrokerBrokerConnection(broker2Socket, this)).start(); // kanourio BrokerBrokerConnection
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
     * kai stis 2 periptwseis pernaei to socket kai 'this'(parent broker) sto thread
     * <p>
     * an einai Broker prosthetoume to socket tou stin lista otherBrokers
     */
    private void acceptConnection() {
        //to prwto minima einai "publisher","consumer" h "broker"
        try {
            Socket newConnection = serverSocket.accept();
            ObjectInputStream newConnectionInput = new ObjectInputStream(newConnection.getInputStream());
            String firstLine = (String) newConnectionInput.readObject();
            System.out.println("first line received: " + firstLine);
            switch (firstLine) {
                case "broker" -> {  //an to prwto minima einai broker swzoume to socket stin lista otherBrokers
                    System.out.println("Broker connected, saving stream to list otherBrokersOutputStreams.");
                    otherBrokersOutputStreams.add(new ObjectOutputStream(newConnection.getOutputStream()));
                    for (ObjectOutputStream oos : otherBrokersOutputStreams) oos.flush();
                    new Thread(new BrokerBrokerConnection(newConnection, this, newConnectionInput)).start(); // kanourio BrokerBrokerConnection
                }
                //an to prwto minima einai publisher ftiaxnoume kainourio BrokerPublisherConnection thread
                case "publisher" -> new Thread(new BrokerPublisherConnection(newConnection, this, newConnectionInput)).start();
                //an to prwto minima einai consumer ftiaxnoume kainourio BrokerConsumerConnection thread
                case "consumer" -> new Thread(new BrokerConsumerConnection(newConnection, this, newConnectionInput)).start();
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
        //TODO
        return 4000;
    }

    /**
     * stelnoume stous allous broker (lista otherBrokers) to kainourio topic
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
     * ston constructor pairname (socket,this) to this einai o parent broker
     * gia na kalesoume px parent.notifyBrokers(topic)
     */
    private class BrokerPublisherConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private ObjectOutputStream brokerPublisherOutputStream;
        private ObjectInputStream brokerPublisherInputStream;

        public BrokerPublisherConnection(Socket socket, Broker parent, ObjectInputStream brokerPublisherInputStream) {
            System.out.println("Started new BrokerPublisherConnectionThread");
            this.socket = socket;
            this.parent = parent;
            this.brokerPublisherInputStream = brokerPublisherInputStream;
            //TODO
        }

        /**
         * handles tin epikoinwnia me ton Publisher
         */
        @Override
        public void run() {
            try {
                //TODO

                //stelnoume "username?" gia na dwsei o publisher to username tou
                brokerPublisherOutputStream = new ObjectOutputStream(socket.getOutputStream());
                brokerPublisherOutputStream.writeObject("username?");

                //o publisher stelnei to username tou
                this.username = (String) brokerPublisherInputStream.readObject();
                System.out.println("publisher username: " + username);
                //an einai prwti fora pou vlepoume to username kanoume initialize tin usernamesTopicsIndex
                if (!usernamesTopicsIndex.containsKey(username))
                    usernamesTopicsIndex.put(username, new HashMap<String, Integer>());

                while (!socket.isClosed()) {
                    try {
                        //o publisher stelnei se pio topic thelei na steilei message
                        String topic;
                        if ((topic = (String) brokerPublisherInputStream.readObject()) != null) {
                            System.out.println("Publisher:" + username + " set topic:" + topic);
                            if (brokerPortsAndTopics.get(port).contains(topic)) { //an aftos o broker exei hdh afto to topic, (an iparxei idi stin brokerPortsAndTopics)
                                brokerPublisherOutputStream.writeObject("continue");// leme ston publisher oti mporei na sinexisei

                            } else if (getResponsibleBrokerPort(topic) == port) { //an aftos o broker einai ipefthinos gia to kainourio topic (an i getResponsibleBrokerPort vgazei tin diki mas port diladi)
                                //dimiourgoume to topic kai to vazoume mazi me ton user stin lista usernamesTopicsIndex me index -1
                                if (!topicsMessages.containsKey(topic))
                                    topicsMessages.put(topic, new ArrayList<Message>());
                                usernamesTopicsIndex.get(username).put(topic, -1);
                                brokerPortsAndTopics.get(port).add(topic);//to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port
                                parent.notifyBrokers(topic);// kai kanoume notify tous brokers gia to kainourio topic
                                brokerPublisherOutputStream.writeObject("continue");// leme ston publisher oti mporei na sinexisei
                            } else { //an oxi xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker kai tou proothoume tin port
                                brokerPublisherOutputStream.writeObject(Integer.toString(getResponsibleBrokerPort(topic)));
                            }

                            Object incomingMessage;
                            while ((incomingMessage = brokerPublisherInputStream.readObject()).getClass().getSuperclass() == Message.class) {
                                System.out.println("reading message");
                                //xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                                if (incomingMessage.getClass() == TextMessage.class) {
                                    topicsMessages.get(topic).add((TextMessage) incomingMessage); //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages

                                    //xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                                } else if (incomingMessage.getClass() == ImageMessage.class) {
                                    topicsMessages.get(topic).add(new ImageMessage(((ImageMessage) incomingMessage).getUsername(),
                                            ((ImageMessage) incomingMessage).getTopic(),
                                            ((ImageMessage) incomingMessage).getMetadata(),
                                            getFileChunks(brokerPublisherInputStream, ((ImageMessage) incomingMessage).getMetadata().getFileSize())));
                                } else if (incomingMessage.getClass() == VideoMessage.class) {
                                    topicsMessages.get(topic).add(new VideoMessage(((VideoMessage) incomingMessage).getUsername(),
                                            ((VideoMessage) incomingMessage).getTopic(),
                                            ((VideoMessage) incomingMessage).getMetadata(),
                                            getFileChunks(brokerPublisherInputStream, ((VideoMessage) incomingMessage).getMetadata().getFileSize())));
                                }
                                synchronized (topicsMessages.get(topic)) {
                                    topicsMessages.get(topic).notifyAll(); //kaloume tin topicsMessages.get(topic).notifyAll(); gia na staloun oles oi allages stous consumers
                                }
                                System.out.print("topicsMessages: ");
                                System.out.println(topicsMessages);
                            }
                            System.out.println("publisher sent:" + incomingMessage); // o xristis estile "end" den thelei na steilei allo message se afto to topic
                        }
                    } catch (SocketException e) {
                        brokerPublisherOutputStream.close();
                        brokerPublisherInputStream.close();
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
        ArrayList<byte[]> getFileChunks(ObjectInputStream brokerPublisherInputStream, int fileSize) {
            ArrayList<byte[]> chunksList = new ArrayList<>();
            int chunkSize = 1024;
            for (int i = 0; i <= fileSize / chunkSize; i++) {
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
     * ston constructor pairname (socket,this) to this einai o parent broker
     */
    private class BrokerConsumerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private ObjectInputStream brokerConsumerInputStream;
        private ObjectOutputStream brokerConsumerOutputStream;


        private String currentTopic = null; // to topic to opoio diavazei twra o consumer

        public BrokerConsumerConnection(Socket socket, Broker parent, ObjectInputStream brokerConsumerInputStream) {
            this.socket = socket;
            this.parent = parent;
            this.brokerConsumerInputStream = brokerConsumerInputStream;
            //edw mporoume na kanoume initialize kai ta input output streams
        }

        /**
         * handles tin epikoinwnia me ton Consumer
         */
        @Override
        public void run() {
            try {
                System.out.println("Started new brokerConsumerConnectionThread");
                //TODO
                //stelnoume "username?" gia na dwsei o consumer to username tou
                brokerConsumerOutputStream = new ObjectOutputStream(socket.getOutputStream());
                brokerConsumerOutputStream.writeObject("username?");
                //o consumer stelnei to username tou
                this.username = (String) brokerConsumerInputStream.readObject();
                System.out.println("consumer username: " + username);
                //an einai prwti fora pou vlepoume to username kanoume initialize tin usernamesTopicsIndex
                if (!usernamesTopicsIndex.containsKey(username))
                    usernamesTopicsIndex.put(username, new HashMap<String, Integer>());

                sendBrokerTopics(); //tou stelnoume tin lista brokerPortsAndTopics (opote kserei ola ta topics kai pou na apefthinthei gia kathe topic)
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
                                    brokerConsumerOutputStream.writeObject("there?");
                                    Object consumerAnswer = brokerConsumerInputStream.readObject();
                                    System.out.println(consumerAnswer);
                                    if (!consumerAnswer.equals("yes")) {
                                        if (consumerAnswer.equals("end")) break;
                                        else throw new SocketException();
                                    }
                                    i = 0;
                                }
                                synchronized (parent.topicsMessages.get(currentTopic)) {
                                    topicsMessages.get(currentTopic).wait(1000);
                                }
//                                        System.out.println("Trying to send new messages to consumer");
                                sendMessages(brokerConsumerOutputStream);
                            }//telos while, opote exei steilei end o consumer
                        }
                        if ((consumerMessage = (String) brokerConsumerInputStream.readObject()) != null) {
                            if (consumerMessage.equals("end")) {
                                consumerMessage = (String) brokerConsumerInputStream.readObject();
                            }
                            if (consumerMessage.equals("/getTopics")) {
                                sendBrokerTopics(); //stelnoume sendBrokerTopics(); kai sinexizei to loop
                            } else {
                                currentTopic = consumerMessage; //o consumer stelnei to topic pou thelei na diavasei kai to thetoume ws current topic
                                System.out.println("consumer sent topic:" + currentTopic);
                                if (!topicsMessages.containsKey(currentTopic))
                                    topicsMessages.put(currentTopic, new ArrayList<Message>());
                                System.out.println("sending messages");
                                sendMessages(brokerConsumerOutputStream);
                                System.out.println("sent messages");

                            }
                        }
                    } catch (SocketException | EOFException e) {
                        socket.close();
                        System.out.println("Consumer with username:" + username + " disconnected.");
                    }
                }
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
                                brokerConsumerOutputStream.writeObject(topicsMessages.get(currentTopic).get(i));
                            } else if (topicsMessages.get(currentTopic).get(i).getClass() == ImageMessage.class) {
                                //TODO: send Image message
                                brokerConsumerOutputStream.writeObject(topicsMessages.get(currentTopic).get(i));
                            } else if (topicsMessages.get(currentTopic).get(i).getClass() == VideoMessage.class) {
                                //TODO: send Video message
                                brokerConsumerOutputStream.writeObject(topicsMessages.get(currentTopic).get(i));
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
     * ston constructor pairname (socket,this) to this einai o parent broker
     */
    private class BrokerBrokerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private ObjectInputStream brokerBrokerInputStream;

        public BrokerBrokerConnection(Socket socket, Broker parent) {
            this.socket = socket;
            this.parent = parent;
        }

        public BrokerBrokerConnection(Socket socket, Broker parent, ObjectInputStream brokerBrokerInputStream) {
            this.socket = socket;
            this.parent = parent;
            this.brokerBrokerInputStream = brokerBrokerInputStream;
        }

        /**
         * handles tin epikoinwnia me tous allous Broker
         */
        @Override
        public void run() {
            try {
                if (brokerBrokerInputStream == null)
                    brokerBrokerInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
