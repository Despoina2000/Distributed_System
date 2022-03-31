package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;
import Distributed_System_part1.Util.Util;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Broker implements Runnable {

    public final String url = "localhost";
    private ServerSocket serverSocket;
    public ArrayList<Socket> otherBrokers;
    public int port;



    /**
     * volatile = koino gia ola ta threads
     * edw kratame poios broker(to port tou) einai ipefthinos gia poia topics, ginetai update opote mpainei kainourio topic
     * xrisimevei kai ws lista olwn twn topic pou iparxoun gia na stelnoume ston consumer
     */
    public volatile HashMap<Integer,ArrayList<String>> brokerPortsAndTopics;

    /**
     * istoriko: mexri pio message exei lavei o consumer gia kathe topic <username<topic,index>>
     */
    public volatile HashMap<String,HashMap<String, Integer>> usernamesTopicsIndex;

    /**
     * edw kratame gia kathe topic tou broker tin lista me ta messages
     * (den evala queue gia na min adeiazei na mporoume na ta ksanasteiloume)
     */
    public volatile HashMap<String,ArrayList<Message>> topicsMessages;

    /**
     * Constructor tou broker
     * katalavenei an einai o prwtos,defteros i tritos broker kai analoga kanei initialize tin port.
     */
    public Broker() {
        //TODO: edw kapws katalavenei an einai o prwtos,defteros i tritos broker kai analoga kanei initialize tin port.
        //TODO: initialize lists and hashmaps
    }

    /**
     * Edw arxizei na trexei o broker se diko tou thread.
     * Mesa tha dimiourgei kainouria thread gia kathe connection.
     */
    @Override
    public void run() {
        //TODO
        //an einai o defteros broker kanei connect me ton prwto
        //an einai o tritos broker kanei connect me tous allous 2
        //etsi exoume connection metaksi olwn twn broker gia na kanoume update to brokerPortsAndTopics
        while(true) {
            acceptConnection();
        }
    }

    /**
     * An to connection einai apo Publisher dimiourgei kainourio thread BrokerPublisherConnection,
     *     an einai Consumer kainourio thread BrokerConsumerConnection
     *     an einai Broker kainourio thread BrokerBrokerConnection(afto isws na min xreiazete, mporoume na kratame lista me ta sockets twn allwn broker sto main thread)
     *     kai stis 3 periptwseis pernaei to socket kai 'this'(parent broker) sto thread
     */
    private void acceptConnection(){
        //TODO
        //to prwto minima einai "publisher","consumer" h "broker"
    }

    /**
     * xrisimopoiei thn hash apo to Util.java gia na vrei se poion broker anoikei to topic
     * kai epistrefei tin port aftou tou broker
     * @param topic String topic
     * @return port tou responsible gia to topic broker
     * @see Util#hash(String topic)
     */
    private int getResponsibleBrokerPort(String topic) {
        //TODO
        return 0;
    }

    /**
     * stelnoume stous allous broker to kainourio topic
     * kai aftoi to prosthetoun stin brokerPortAndTopics tous mazi me to port mas
     * @param topic String topic
     */
    private void notifyBrokers(String topic) {
        //TODO: send to other brokers port+topic
    }


    /**
     * h acceptConnection dimiourgei kainourio thread gia kathe Publisher connected,
     *     ston constructor pairname (socket,this) to this einai o broker kai ton
     *     xrisimopoioume gia na vlepoume tis koines metavlites px parent.topics
     */
    private class BrokerPublisherConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;

        public BrokerPublisherConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
            //TODO
            //edw mporoume na kanoume initialize kai ta input output streams
        }

        /**
         * handles tin epikoinwnia me ton Publisher
         */
        @Override
        public void run() {
            //TODO

            // den exw ftiaksei akoma alles methodous pou tha xreiastoun gia tin parakatw leitourgia:

            //stelnoume "username?" gia na dwsei o publisher to username tou
            //o publisher stelnei to username tou

            // while !socket.isClosed()
                //o publisher stelnei se pio topic thelei na steilei message
                //vlepoume an aftos o broker einai ipefthinos gia to topic me brokerPortsAndTopics.get(port).contains(topic)
                //an einai aftos o katalilos broker tou stelnoume minima na sinexisei
                //an oxi xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker kai tou proothoume tin port

                //an to topic den iparxei to dimiourgoume kai to vazoume mazi me ton user stin lista usernamesTopicsIndex me index -1
                //to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port
                // kai kanoume notify tous brokers gia to kainourio topic(isws me parent.notifyBrokers(topic);)

                //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages
                //TODO: xeirismos katallilos an einai ImageMessage i VideoMessage (pairnoume prwta to message xwris to content kai meta ta chunks ena ena)
                //kaloume tin topicsMessages.get(topic).notifyAll(); gia na staloun oles oi allages stous consumers
            //end while
        }
    }

    // i acceptConnection dimiourgei kainourio thread gia kathe Consumer connected, (socket,this) opws sta alla
    private class BrokerConsumerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;


        private String currentTopic; // to topic to opoio diavazei twra o consumer

        public BrokerConsumerConnection(Socket socket, Broker parent){
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
            if ( !usernamesTopicsIndex.containsKey(username)) usernamesTopicsIndex.put(username,new HashMap<>()); //an den ton exoume ksanadei ton user ton vazoume sta usernamesTopicsIndex
            sendBrokerTopics(); //tou stelnoume tin lista brokerPortsAndTopics (opote kserei ola ta topics kai pou na apefthinthei gia kathe topic)

            String currentTopic = "topic";//TODO: o consumer stelnei to topic pou thelei na diavasei kai to thetoume ws current topic
            sendMessages();

            //kanoume .wait(1000) sto topicsMessages.get(currentTopic)
            // kai opote kalesei kapoios notifyAll() sto topicsMessages.<currentTopic> sinexizoume
            // h otan perasoun 1000ms (gia na exei kai aftomato polling)
            while(!socket.isClosed()){ //oso iparxei sindesi
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
         *         kanoume update to topicMessageIndex mexri ekei pou tou exoume steilei(-1 an den iparxoun messages)
         */
        private void sendMessages() {
            //stelnei ta kainouria messages gia to currentTopic ston consumer
            if (!usernamesTopicsIndex.get(username).containsKey(currentTopic)) usernamesTopicsIndex.get(username).put(currentTopic,-1);//an einai kainourio topic to vazoume lista me index -1
            if (topicsMessages.get(currentTopic).size() > usernamesTopicsIndex.get(username).get(currentTopic)) { // an iparxoun perisotera minimata apo osa exei idi diavasei o consumer
                for (int i = usernamesTopicsIndex.get(username).get(currentTopic) + 1 ; i < topicsMessages.get(currentTopic).size(); i++) { // gia kathe kainourio minima
                    // TODO: send topicMessageIndex.get(currentTopic)[i]
                    // TODO: if message is ImageMessage or VideoMessage send without content and then send chunkedContent one by one chunk
                    usernamesTopicsIndex.get(username).put(currentTopic,i);//update the current index for this consumer
                }
            }
        }

        private void sendBrokerTopics() {
            //TODO: send lista brokerPortsAndTopics
        }
    }

    // i acceptConnection dimiourgei kainourio thread gia tous allous 2 brokers, (socket,this) opws sta alla (mallon tha kratame aplws lista me sockets anti threads gia tous brokers)
    private class BrokerBrokerConnection implements Runnable {
        private Socket socket;
        private Broker parent;

        public BrokerBrokerConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
        }

        //handles tin epikoinwnia me ton allo Broker
        @Override
        public void run() {

        }
    }
}
