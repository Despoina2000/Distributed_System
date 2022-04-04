package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;
import Distributed_System_part1.Model.TextMessage;
import Distributed_System_part1.Util.Util;
import org.checkerframework.checker.units.qual.A;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * broker class, handles connections with publishers and consumers
 */
public class Broker implements Runnable {

    public static final int BROKER1 = 4000;
    public static final int BROKER2 = 5555;
    public static final int BROKER3 = 5984;

    public final String url = "localhost";
    private ServerSocket serverSocket;
    public int port;
    public ArrayList<Socket> otherBrokers;

    /**
     * volatile = koino gia ola ta threads
     * edw kratame poios broker(to port tou) einai ipefthinos gia poia topics, ginetai update opote mpainei kainourio topic
     * xrisimevei kai ws lista olwn twn topic pou iparxoun gia na stelnoume ston consumer
     */
    public volatile HashMap<Integer, ArrayList<String>> brokerPortsAndTopics;
    /**
     * edw kratame gia kathe topic tou broker tin lista me ta messages
     * (den evala queue gia na min adeiazei na mporoume na ta ksanasteiloume)
     */
    public volatile HashMap<String, ArrayList<Message>> topicsMessages;
    /**
     * istoriko: mexri pio message exei lavei o consumer gia kathe topic {@code <username<topic,index>> }
     */
    public volatile HashMap<String, HashMap<String, Integer>> usernamesTopicsIndex;


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
        otherBrokers = new ArrayList<>();
        brokerPortsAndTopics = new HashMap<>();
        topicsMessages = new HashMap<>();
        usernamesTopicsIndex = new HashMap<>();

        //initialize inner hashmaps/arraylists
        brokerPortsAndTopics.put(BROKER1,new ArrayList<>());
        brokerPortsAndTopics.put(BROKER2,new ArrayList<>());
        brokerPortsAndTopics.put(BROKER3,new ArrayList<>());
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
                Socket broker1Socket = new Socket(url,BROKER1);
                otherBrokers.add(broker1Socket);
                PrintWriter broker1Writer = new PrintWriter(broker1Socket.getOutputStream(), true);
                broker1Writer.println("broker");
            }
            //an einai o tritos broker kanei connect me tous allous 2 kai vazei ta socket stin otherBrokers
            if (port == BROKER3) {
                System.out.println("Connecting to Broker1.");
                Socket broker1Socket = new Socket(url,BROKER1);
                otherBrokers.add(broker1Socket);
                PrintWriter broker1Writer = new PrintWriter(broker1Socket.getOutputStream(), true);
                broker1Writer.println("broker");
                new Thread(new BrokerBrokerConnection(broker1Socket, this)).start(); // kanourio BrokerBrokerConnection
                System.out.println("Connecting to Broker2.");
                Socket broker2Socket = new Socket(url,BROKER2);
                otherBrokers.add(broker2Socket);
                PrintWriter broker2Writer = new PrintWriter(broker2Socket.getOutputStream(), true);
                broker2Writer.println("broker");
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
            BufferedReader newConnectionInput = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
            String firstLine = newConnectionInput.readLine();
            System.out.println("first line received: " + firstLine);
            switch (firstLine) {
                case "broker" -> {  //an to prwto minima einai broker swzoume to socket stin lista otherBrokers
                    System.out.println("Broker connected, saving socket to list otherBrokers.");
                    otherBrokers.add(newConnection);
                    if (otherBrokers.size() == 2) {
                        for (Socket broker :otherBrokers) {
                            new Thread(new BrokerBrokerConnection(broker, this)).start(); // kanourio BrokerBrokerConnection
                        }
                    }
                }
                //an to prwto minima einai publisher ftiaxnoume kainourio BrokerPublisherConnection thread
                case "publisher" -> new Thread(new BrokerPublisherConnection(newConnection, this)).start();
                //an to prwto minima einai consumer ftiaxnoume kainourio BrokerConsumerConnection thread
                case "consumer" -> new Thread(new BrokerConsumerConnection(newConnection, this)).start();
                default -> {
                    System.out.println("Wrong identifier from socket: " + firstLine + " Expected \"broker\" or \"publisher\" or \"consumer\"");
                    newConnection.close();
                }
            }
        } catch (IOException e) {
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
        for (Socket broker : otherBrokers) {
            try {
                PrintWriter pw = new PrintWriter(broker.getOutputStream(), true);
                pw.println(port + topic); // notify other brokers about our port and the new topic
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
        private PrintWriter brokerPublisherWriter;
        private BufferedReader brokerPublisherReader;
        private ObjectOutputStream brokerPublisherObjectOutputStream;
        private ObjectInputStream brokerPublisherObjectInputStream;

        public BrokerPublisherConnection(Socket socket, Broker parent) {
            System.out.println("Started new BrokerPublisherConnectionThread");
            this.socket = socket;
            this.parent = parent;
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
                brokerPublisherWriter = new PrintWriter(socket.getOutputStream(), true);
                brokerPublisherWriter.println("username?");

                //o publisher stelnei to username tou
                brokerPublisherReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.username = brokerPublisherReader.readLine();
                //an einai prwti fora pou vlepoume to username kanoume initialize tin usernamesTopicsIndex
                if (!usernamesTopicsIndex.containsKey(username)) usernamesTopicsIndex.put(username, new HashMap<String, Integer>());

                while (!socket.isClosed()) {
                    try {

                        //o publisher stelnei se pio topic thelei na steilei message
                        String topic;
                        if ((topic = brokerPublisherReader.readLine()) != null) {
                            System.out.println("Publisher:" + username + " set topic:" + topic);
                            if (brokerPortsAndTopics.get(port).contains(topic)) { //an aftos o broker exei hdh afto to topic, (an iparxei idi stin brokerPortsAndTopics)
                                brokerPublisherWriter.println("continue");// leme ston publisher oti mporei na sinexisei

                            } else if ( getResponsibleBrokerPort(topic) == port){ //an aftos o broker einai ipefthinos gia to kainourio topic (an i getResponsibleBrokerPort vgazei tin diki mas port diladi)
                                //dimiourgoume to topic kai to vazoume mazi me ton user stin lista usernamesTopicsIndex me index -1
                                topicsMessages.put(topic, new ArrayList<Message>());
                                usernamesTopicsIndex.get(username).put(topic, -1);
                                brokerPortsAndTopics.get(port).add(topic);//to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port
                                parent.notifyBrokers(topic);// kai kanoume notify tous brokers gia to kainourio topic
                                brokerPublisherWriter.println("continue");// leme ston publisher oti mporei na sinexisei
                            } else { //an oxi xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker kai tou proothoume tin port
                                brokerPublisherWriter.println(getResponsibleBrokerPort(topic));
                            }

                            String incomingMessage;
                            while (!(incomingMessage = brokerPublisherReader.readLine()).equalsIgnoreCase("end")) {
                                System.out.println("reading message");
                                topicsMessages.get(topic).add(new TextMessage(username,topic,incomingMessage)); //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages
                                synchronized (topicsMessages.get(topic)) {
                                    topicsMessages.get(topic).notifyAll(); //kaloume tin topicsMessages.get(topic).notifyAll(); gia na staloun oles oi allages stous consumers
                                }
                                System.out.print("topicsMessages: ");
                                System.out.println(topicsMessages);
                                System.out.print("usernamesTopicsIndex: ");
                                System.out.println(usernamesTopicsIndex);
                                //TODO: xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                            }
                        }
                    } catch (SocketException e) {
                        socket.close();
                        System.out.println("Publisher with username:" + username + " disconnected.");
                    }
                }//end while
            } catch (IOException e) {
                e.printStackTrace();
            }
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


        private String currentTopic; // to topic to opoio diavazei twra o consumer

        public BrokerConsumerConnection(Socket socket, Broker parent) {
            this.socket = socket;
            this.parent = parent;
            //edw mporoume na kanoume initialize kai ta input output streams
        }

        /**
         * handles tin epikoinwnia me ton Consumer
         */
        @Override
        public void run() {
            //TODO
            //stelnoume "username?" gia na dwsei o consumer to username tou
            //o consumer stelnei to username tou
            if (!usernamesTopicsIndex.containsKey(username))
                usernamesTopicsIndex.put(username, new HashMap<>()); //an den ton exoume ksanadei ton user ton vazoume sta usernamesTopicsIndex
            sendBrokerTopics(); //tou stelnoume tin lista brokerPortsAndTopics (opote kserei ola ta topics kai pou na apefthinthei gia kathe topic)

            String currentTopic = "topic";//TODO: o consumer stelnei to topic pou thelei na diavasei kai to thetoume ws current topic
            sendMessages();

            //kanoume .wait(1000) sto topicsMessages.get(currentTopic)
            // kai opote kalesei kapoios notifyAll() sto topicsMessages.<currentTopic> sinexizoume
            // h otan perasoun 1000ms (gia na exei kai aftomato polling)
            while (!socket.isClosed()) { //oso iparxei sindesi
                //TODO: while den stelnei kati o consumer, isws me inputstream.hasnext
                try {
                    topicsMessages.get(currentTopic).wait(1000);
                    sendMessages();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //TODO: telos while, opote exei steilei kainourio topic o consumer h zitaei sendBrokerTopics();
                //TODO: if kainourio topic:
                currentTopic = "newTopic"; //TODO: diavazoume kai thetoume to kainourio topic kai sinexizei to loop
                //TODO: if zitaei topics:
                sendBrokerTopics(); //TODO: stelnoume sendBrokerTopics(); kai sinexizei to loop
            }
        }

        /**
         * tou stelnoume ta messages pou iparxoun idi sto sigkekrimeno topic apo ti lista topicsMessages
         * kanoume update to topicMessageIndex mexri ekei pou tou exoume steilei(-1 an den iparxoun messages)
         */
        private void sendMessages() {
            //stelnei ta kainouria messages gia to currentTopic ston consumer
            if (!usernamesTopicsIndex.get(username).containsKey(currentTopic))
                usernamesTopicsIndex.get(username).put(currentTopic, -1);//an einai kainourio topic to vazoume lista me index -1
            if (topicsMessages.get(currentTopic).size() > usernamesTopicsIndex.get(username).get(currentTopic)) { // an iparxoun perisotera minimata apo osa exei idi diavasei o consumer
                for (int i = usernamesTopicsIndex.get(username).get(currentTopic) + 1; i < topicsMessages.get(currentTopic).size(); i++) { // gia kathe kainourio minima
                    // TODO: send topicMessageIndex.get(currentTopic)[i]
                    // TODO: if message is ImageMessage or VideoMessage send without content and then send chunkedContent one by one chunk
                    usernamesTopicsIndex.get(username).put(currentTopic, i);//update the current index for this consumer
                }
            }
        }

        private void sendBrokerTopics() {
            //TODO: send lista brokerPortsAndTopics
        }
    }

    /**
     * h acceptConnection dimiourgei kainourio thread gia kathe Broker connected,
     * ston constructor pairname (socket,this) to this einai o parent broker
     */
    private class BrokerBrokerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private BufferedReader brokerBrokerReader;

        public BrokerBrokerConnection(Socket socket, Broker parent) {
            this.socket = socket;
            this.parent = parent;
            try {
                // initialize bufferedreader for socket
                brokerBrokerReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * handles tin epikoinwnia me tous allous Broker
         */
        @Override
        public void run() {
            System.out.println("Started new brokerBrokerConnectionThread");
            while (!socket.isClosed()) { // oso einai anoixto to socket
                try {
                    String incomingPortAndTopic;
                    if ((incomingPortAndTopic = brokerBrokerReader.readLine()) != null) {
                        String incomingPort = incomingPortAndTopic.substring(0, 4);
                        String incomingTopic = incomingPortAndTopic.substring(4);
                        //add port and topic to brokerPortsAndTopics
                        brokerPortsAndTopics.get(Integer.parseInt(incomingPort)).add(incomingTopic);
                        System.out.println("Added to brokerPortsAndTopics in port:" + incomingPort + " topic:" + incomingTopic);
                        System.out.println(brokerPortsAndTopics);
                    }
                } catch (IOException e) {
                    try {
                        otherBrokers.remove(socket);
                        socket.close();
                        System.out.println("A broker disconnected.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
