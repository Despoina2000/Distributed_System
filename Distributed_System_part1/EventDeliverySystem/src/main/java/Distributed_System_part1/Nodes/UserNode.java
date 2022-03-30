package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class UserNode {
    public String username;
    public final String url = "localhost";
    volatile int currentBrokerPort;
    volatile String currentTopic;
    private Publisher publisher;
    private Thread consumerThread;



    public UserNode() {
        //TODO:
        //read and set username
        //currentBrokerPort = random broker port
        // publisher = new Publisher(this) (mporei aplws na trexei sto main thread o publisher kai se allo o consumer anti na exoume 2 threads)
        // consumerThread = new thread consumer(this)
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

    //arxizei to command line interface gia na dwsoume entoles (/topic, message klp)
    private void startCLI() {
        //TODO:
        /*
        pithanes entoles:
        /topic <topic> (currentTopic = <topic>; publisher.setTopic(); consumerThread.setTopic();)
        /topics (consumer.requestTopics)
        <sketo message> (sendMessage(new TextMessage...))
        /image <imagepath> (sendMessage(new ImageMessage xwris to content)) meta arxizoume kai stelnoume ta byte[]
                        apo tin ArrayList<byte[]> splitFileToChunks isws me tin sendFileChunks(splitFileToChunks)
        /video <videopath> (sendMessage(new VideoMessage xwris to content)) meta arxizoume kai stelnoume ta byte[]
                        apo tin ArrayList<byte[]> splitFileToChunks isws me tin sendFileChunks(splitFileToChunks)
         */
    }



    private class Publisher{
        private UserNode parent;

        public Publisher(UserNode parent) {
            this.parent = parent;
        }

        public void connectToBroker(int port) {
            //TODO:
            //connect to broker at port
            //send "publisher"
            //read "username?"
            //send parent.username
        }

        public boolean setTopic() {
            //TODO:
            //send to broker parent.currentTopic
            //perimenoume apantisi apo broker,
            // an i apantisi einai broker port thetoume parent.currentBrokerPort = port
            // kai kanoume connectToBroker(port), consumer.connectToBroker(port) kai ksana setTopic kai consumer.setTopic
            //perimenoume na mas pei o broker na sinexisoume, otan mas pei nai return true;
            return true;
        }

        public void sendMessage(Message message){
            //TODO:
        }

        public void sendFileChunks(ArrayList<byte[]> fileChunks){
            //TODO:
        }
    }

    private class Consumer implements Runnable {
        private UserNode parent;
        private HashMap<String, ArrayList<Message>> topicsMessages; //edw mporoume na kratame osa minimata exoume diavasei idi

        public Consumer(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            //TODO:
            //connectToBroker(currentBrokerPort)
            //while running processIncomingMessage
        }

        public void connectToBroker(int port) {
            //TODO:
            //close socket an iparxei sindesi me proigoumeno broker
            //connect to broker at port
            //send "consumer"
            //read "username?"
            //send parent.username
            //receive lista brokerPortsAndTopics
        }

        public void setTopic() {
            //TODO:
            //send to broker parent.currentTopic
            //edw mporoume na kanoume print olo to istoriko gia to currentTopic apo to topicsMessages kai tha perimenoume gia kainouria minimata
        }

        private void processIncomingMessage(Message message) {
            //TODO:
            //if message.getContentType=="text"/"image"/"video/ do this..
            // an to mesage einai imagemessage i videomessage arxizoume na diavazoume byte[] se mia arraylist
            // kai to sinthetoume meta me mergeChunksToFile se new ImageMessage/VideoMessage me ton overloaded constructor pou periexei kai tto content

            // prosthiki tou message sto topicsMessages (sto message.getTopic)
        }

        public void requestTopics() {
            //TODO:
            //zitaei tin lista me ta topic apo ton broker
            //receive lista brokerPortsAndTopics
        }
    }
}
