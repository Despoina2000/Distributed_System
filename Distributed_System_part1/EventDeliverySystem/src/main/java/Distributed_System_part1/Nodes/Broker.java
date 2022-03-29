package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;

import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Broker implements Runnable {

    private final String url = "localhost";
    private ServerSocket serverSocket;
    private int port;

    //volatile = koino gia ola ta threads
    //edw kratame poios broker(to port tou) einai ipefthinos gia poia topics, ginetai update opote mpainei kainourio topic
    //xrisimevei kai ws lista olwn twn topic pou iparxoun gia na stelnoume ston consumer
    public volatile HashMap<Integer,ArrayList<String>> brokerPortsAndTopics;

    //poia username akolouthoun poia topic
    public volatile HashMap<String,ArrayList<String>> usernamesTopics;

    //edw kratame gia kathe topic tou broker tin lista me ta messages (den evala queue gia na min adeiazei na mporoume na ta ksanasteiloume)
    public volatile HashMap<String,ArrayList<Message>> topicsMessages;

    //Constructor tou broker
    public Broker() {
        //TODO: edw kapws katalavenei an einai o prwtos,defteros i tritos broker kai analoga kanei initialize tin port.
        //TODO: intitialize lists and hashmaps
        run();
    }

    //Edw arxizei na trexei o broker se diko tou thread. Mesa tha dimiourgei kainouria thread gia kathe connection.
    @Override
    public void run() {
        //TODO
        //an einai o defteros broker kanei connect me ton prwto
        //an einai o tritos broker kanei connect me tous allous 2
        //etsi exoume connection metaksi olwn twn broker gia na kanoume update to brokerPortAndTopics
        while(true) {
            acceptConnection();
        }
    }

    //An to connection einai apo Publisher dimiourgei kainourio thread BrokerPublisherConnection,
    //an einai Consumer kainourio thread BrokerConsumerConnection
    //an einai Broker kainourio thread BrokerBrokerConnection(afto isws na min xreiazete, mporoume na kratame lista me ta sockets twn allwn broker sto main thread)
    //kai stis 3 periptwseis pernaei to socket kai 'this'(parent broker) sto thread
    private void acceptConnection(){
        //TODO
        //to prwto minima einai "publisher","consumer" h "broker"
    }

    //Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic") (mporoume na tin valoume se helper class px utils.java)
    private BigInteger hash(String s) {
        //TODO
        return null;
    }

    //xrisimopoiei thn hash gia na vrei se poion broker anoikei to topic kai epistrefei tin port aftou tou broker
    private int getResponsibleBrokerPort(String topic) {
        //TODO
        return 0;
    }

    //stelnoume stous allous broker to kainourio topic kai aftoi to prosthetoun stin brokerPortAndTopics tous mazi me to port mas
    private void notifyBrokers(String topic) {
        //TODO
    }



    // h acceptConnection dimiourgei kainourio thread gia kathe Publisher connected,
    // ston constructor pairname (socket,this) to this einai o broker kai ton
    // xrisimopoioume gia na vlepoume tis koines metavlites px parent.topics
    private class BrokerPublisherConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;

        public BrokerPublisherConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
            //edw mporoume na kanoume initialize kai ta input output streams
        }

        //handles tin epikoinwnia me ton Publisher
        @Override
        public void run() {
            //TODO

            //prwta o publisher stelnei to username tou

            // while !socket.isClosed()
                //o publisher stelnei se pio topic thelei na steilei message
                //vlepoume an aftos o broker einai ipefthinos gia to topic me brokerPortsAndTopics.get(port).contains(topic)
                //an einai aftos o katalilos broker tou stelnoume minima na sinexisei
                //an oxi xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker kai tou proothoume tin port

                //an to topic den iparxei to dimiourgoume kai to vazoume mazi me ton user stin lista usernamesTopics
                //to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port
                // kai kanoume notify tous brokers gia to kainourio topic(isws me parent.notifyBrokers(topic);)

                //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages
                //kaloume tin parent.topicsMessages.get(topic).notifyAll(); gia na staloun oles oi allages stous consumers
            //end while
        }
    }

    // i acceptConnection dimiourgei kainourio thread gia kathe Consumer connected, (socket,this) opws sta alla
    private class BrokerConsumerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private HashMap<String,Integer> topicMessageIndex; //mexri pio message exei lavei o consumer gia kathe topic
        private String currentTopic; // to topic to opoio diavazei twra o consumer

        public BrokerConsumerConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
            //edw mporoume na kanoume initialize kai ta input output streams
        }

        //handles tin epikoinwnia me ton Consumer
        @Override
        public void run() {
            //TODO
            //prwta o consumer stelnei to username tou
            //tou stelnoume tin lista brokerPortsAndTopics (opote kserei ola ta topics kai pou na apefthinthei gia kathe topic)

            String currentTopic = "topic";//TODO: o consumer stelnei to topic pou thelei na diavasei kai to thetoume ws current topic
            sendMessages();

            //kanoume .wait(2000) sto parent.topicsMessages.get(currentTopic)
            // kai opote kalesei kapoios notifyAll() sto topicsMessages.<currentTopic> sinexizoume
            // h otan perasoun 1000ms (gia na exei kai aftomato polling)
            while(!socket.isClosed()){ //oso iparxei sindesi
                //TODO: while den stelnei kati o consumer, isws me inputstream.hasnext
                    try {
                        parent.topicsMessages.get(currentTopic).wait(1000);
                        sendMessages();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                //TODO: telos while, opote exei steilei kainourio topic o consumer
                currentTopic = "newTopic"; //TODO: diavazoume kai thetoume to kainourio topic kai sinexizei to loop
            }
        }

        //tou stelnoume ta messages pou iparxoun idi sto sigkekrimeno topic apo ti lista topicsMessages
        //kanoume update to topicMessageIndex mexri ekei pou tou exoume steilei(-1 an den iparxoun messages)
        private void sendMessages() {
            //stelnei ta kainouria messages gia to currentTopic ston consumer
            if (!topicMessageIndex.containsKey(currentTopic)) topicMessageIndex.put(currentTopic,-1);//an einai kainourio topic to vazoume stin topiki lista
            if (topicsMessages.get(currentTopic).size()>topicMessageIndex.get(currentTopic)) { // an iparxoun perisotera minimata apo osa exei idi diavasei o consumer
                for (int i = topicMessageIndex.get(currentTopic) + 1 ; i < topicsMessages.get(currentTopic).size(); i++) { // gia kathe kainourio minima
                    // TODO: send topicMessageIndex.get(currentTopic)[i]
                    topicMessageIndex.put(currentTopic,i);//update the current index for this consumer
                }
            }
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
