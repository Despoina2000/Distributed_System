package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class UserNode {
    public final String url = "localhost";

    private Publisher publisher;
    private Thread consumerThread;

    public volatile String username;
    public volatile int currentBrokerPort;
    public volatile String currentTopic;
    public volatile HashMap<Integer,ArrayList<String>> brokerPortsAndTopics;
    public volatile HashMap<String, ArrayList<Message>> topicsMessages; //edw mporoume na kratame osa minimata exoume diavasei idi

    /**
     * Main thread: publisher, other thread: consumer
     */
    public UserNode() {
        //TODO
        //read and set username
        //currentBrokerPort = random broker port
        // publisher = new Publisher()
        // consumerThread = new thread(new Consumer(this))
        // create new Folder with name username (to store images and videos)

        //destructor to delete folder and files, kaleitai otan kleinoume to app me CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                //TODO: delete all files in folder "username" and delete folder
            }
        });

        startCLI();
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
    }



    private class Publisher{

        public Publisher() {}

        public void connectToBroker(int port) {
            //TODO
            //connect to broker at port
            //send "publisher"
            //read "username?"
            //send username
        }

        public void setTopic() {
            //TODO

            //TODO: check an exoume idi tin pliroforia sto brokerPortsAndTopics allios:

            //send to broker currentTopic
            //perimenoume apantisi apo broker,
            // an i apantisi einai broker port thetoume currentBrokerPort = port
            // kai kanoume connectToBroker(port), consumer.connectToBroker(port) kai ksana setTopic kai consumer.setTopic

            //perimenoume na mas pei o broker na sinexisoume
        }

        public void sendMessage(Message message){
            //TODO
        }

        public void sendFileChunks(ArrayList<byte[]> fileChunks){
            //TODO
        }
    }

    private class Consumer implements Runnable {
        private UserNode parent;

        public Consumer(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            //TODO
            //connectToBroker(currentBrokerPort)
            //while running processIncomingMessage
        }

        public void connectToBroker(int port) {
            //TODO
            //close socket an iparxei sindesi me proigoumeno broker
            //connect to broker at port
            //send "consumer"
            //read "username?"
            //send username
            //receive lista brokerPortsAndTopics - update tin topikh brokerPortsAndTopics
        }

        public void setTopic() {
            //TODO
            //send to broker currentTopic
            //edw mporoume na kanoume print olo to istoriko gia to currentTopic apo to topicsMessages kai tha perimenoume gia kainouria minimata
        }

        private void processIncomingMessage(Message message) {
            //TODO
            //if message.getContentType=="text"/"image"/"video/ do this..
            // an to mesage einai imagemessage i videomessage arxizoume na diavazoume byte[] se mia arraylist
            // kai to sinthetoume meta me mergeChunksToFile se new ImageMessage/VideoMessage me ton overloaded constructor pou periexei kai to content

            // prosthiki tou message sto topicsMessages (sto message.getTopic)
        }

        public void requestTopics() {
            //TODO
            //zitaei tin lista me ta topic apo ton broker
            //receive lista brokerPortsAndTopics
            //kanei update tin topikh brokerPortsAndTopics
        }
    }
}
