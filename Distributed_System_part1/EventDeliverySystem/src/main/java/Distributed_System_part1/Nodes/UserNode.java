package Distributed_System_part1.Nodes;

import Distributed_System_part1.Model.Message;

public class UserNode {
    public String username;
    volatile String currentTopic;
    private Publisher publisher;
    private Thread consumerThread;
    volatile int currentBrokerPort;

    public UserNode() {
        //read and set username
        //currentBrokerPort = random broker port
        // publisher = new Publisher(this) (mporei aplws na trexei sto main thread o publisher kai se allo o consumer anti na exoume 2 threads)
        // consumerThread = new thread consumer(this)
        startCLI();
    }

    //arxizei to command line interface gia na dwsoume entoles (/topic, message klp)
    private void startCLI() {

        /*
        pithanes entoles:
        /topic <topic> (publisher.setTopic(<topic>) consumerThread.setTopic(<topic>))
        /topics ( get brokerPortsAndTopics list from broker)
        <sketo message> (sendMessage(new TextMessage...))
        /image <imagepath> (sendMessage(new ImageMessage...))
        /video <videopath> (sendMessage(new VideoMessage...))
         */
    }



    private class Publisher{
        UserNode parent;

        public Publisher(UserNode parent) {
            this.parent = parent;
        }

        public void connectToBroker(int port) {
            //connect to broker at port
            //send "publisher"
            //read "username?"
            //send parent.username
        }

        public boolean setTopic() {
            //send to broker parent.currentTopic
            //perimenoume apantisi apo broker,
            // an i apantisi einai broker port thetoume parent.currentBrokerPort = port
            // kai kanoume connectToBroker(port) kai ksana sendTopic
            //perimenoume na mas pei o broker na sinexisoume, otan mas pei nai return true;
            return true;
        }

        public void sendMessage(Message message){

        }
    }

    private class Consumer implements Runnable {
        UserNode parent;

        public Consumer(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            //connectToBroker(currentBrokerPort)
            //while running processIncomingMessage
        }

        public void connectToBroker(int port) {
            //connect to broker at port
            //send "consumer"
            //read "username?"
            //send parent.username
        }

        public void setTopic() {
            //send to broker parent.currentTopic
            //receive lista brokerPortsAndTopics
        }

        private void processIncomingMessage(Message message) {
            //if message.contentType=="text"/"image"/"video/ do this..
        }
    }
}
