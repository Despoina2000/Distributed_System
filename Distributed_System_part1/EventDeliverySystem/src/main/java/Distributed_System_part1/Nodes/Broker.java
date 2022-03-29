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

    //volatile = tha einai koinh gia ola ta threads, edw kratame ta topics gia ta opoia einai ipefthinos o sigkekrimenos broker
    public volatile ArrayList<String> topics;

    //edw kratame poios broker(to port tou) einai ipefthinos gia poia topics, ginetai update opote mpainei kainourio topic
    //xrisimevei kai ws lista olwn twn topic pou iparxoun gia na stelnoume ston consumer
    public volatile HashMap<Integer,ArrayList<String>> brokerPortAndTopics;

    //poia username akolouthoun poia topic
    public volatile HashMap<String,ArrayList<String>> usernamesTopics;

    //edw kratame gia kathe topic tou broker tin lista me ta messages (den evala queue gia na min adeiazei na mporoume na ta ksanasteiloume)
    public volatile HashMap<String,ArrayList<Message>> topicsMessages;

    //Constructor tou broker
    public Broker() {
        //edw kapws katalavenei an einai o prwtos,defteros i tritos broker kai analoga rithmizei port.
        run();
    }

    //Edw arxizei na trexei o broker se diko tou thread. Mesa tha dimiourgei kainouria thread gia kathe connection.
    @Override
    public void run() {
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
        //to prwto minima einai "publisher","consumer" h "broker"
    }

    //Epistrefei MD5(or SHA-1) hash apo string (px gia na vgaloume to hash tou broker "ip:port" h to hash tou "topic"
    private BigInteger hash(String s) {
        return null;
    }

    //xrisimopoiei thn hash gia na vrei se poion broker anoikei to topic kai epistrefei tin port aftou tou broker
    private int getResponsibleBrokerPort(String topic) {
        return 0;
    }

    //stelnoume stous allous broker to kainourio topic kai aftoi to prosthetoun stin brokerPortAndTopics tous mazi me to port mas
    private void notifyBrokers(String topic) {

    }

    // i acceptConnection dimiourgei kainourio thread gia kathe Publisher connected,
    // ston constructor pairname (socket,this) to this einai o broker kai ton
    // xrisimopoioume gia na vlepoume tis koines metavlites px parent.topics
    private class BrokerPublisherConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;

        public BrokerPublisherConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
        }

        //handles tin epikoinwnia me ton Publisher
        @Override
        public void run() {
            //prwta o publisher stelnei to username tou

            //o publisher stelnei se pio topic thelei na steilei message
            //xrisimopoioume tin getResponsibleBrokerPort(topic) gia na vroume ton katalilo broker
            //an den einai aftos o broker o katalilos tou stelnoume tin port pou prepei na apefthinthei
            //an einai aftos o katalilos tou stelnoume minima na sinexisei

            //an to topic den iparxei to dimiourgoume, to vazoume stin lista topics kai mazi me ton user stin lista usernamesTopics
            //to prosthetoume kai stin lista brokerPortAndTopics stin diki mas port kai kanoume notify tous brokers gia to kainourio topic

            //o publisher stelnei to message tou kai to prosthetoume sto katalilo topic stin topicsMessages
            //kaloume tin topicsMessages.notifyAll(); gia na staloun oles oi allages stous consumers
        }
    }

    // i acceptConnection dimiourgei kainourio thread gia kathe Consumer connected, (socket,this) opws sta alla
    private class BrokerConsumerConnection implements Runnable {
        private Socket socket;
        private Broker parent;
        private String username;
        private HashMap<String,Integer> topicMessageIndex; //mexri pio message exei lavei o consumer gia kathe topic

        public BrokerConsumerConnection(Socket socket, Broker parent){
            this.socket = socket;
            this.parent = parent;
        }

        //handles tin epikoinwnia me ton Consumer
        @Override
        public void run() {
            //prwta o consumer stelnei to username tou
            //tou stelnoume ola ta topics
            //tou stelnoume ola ta minimata apo to TopicsMessages gia ta topics pou to username tou einai sto usernamesTopics
            //kanoume update to topicMessageIndex mexri ekei pou tou exoume steilei

            //kanoume .wait() sto topicsMessages kai opote kalesei kapoios notifyAll() sto topicsMessages sinexizoume
            while(!socket.isClosed()){
                try {
                    parent.topicsMessages.wait();
                    sendMessage();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessage() {
            //psaxnei pio itan to kainourio message pou mpike sto TopicsMessages kai to stelnei ston consumer an einai subscribed sto topic
            topicsMessages.forEach((topic, messageList) -> {
                if (!topicMessageIndex.containsKey(topic)) topicMessageIndex.put(topic,0);//an einai kainourio topic to vazoume stin topiki lista
                if (usernamesTopics.get(username).contains(topic) && messageList.size()>topicMessageIndex.get(topic)) {
                    for (int i = topicMessageIndex.get(topic) + 1 ; i < messageList.size(); i++) {
                        //send messageList[i]
                        topicMessageIndex.put(topic,i);//update the current index for this consumer
                    }
                }
            });
        }
    }

    // i acceptConnection dimiourgei kainourio thread gia tous allous 2 brokers, (socket,this) opws sta alla
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
