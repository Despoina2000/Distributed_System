package Distributed_System_part1.Nodes;

public class UserNode {
    String username;

    public UserNode() {
        //read and set username
        // new thread publisher(username, this)
        // new thread consumer(username, this)
    }

    private class Publisher implements Runnable{
        UserNode parent;

        public Publisher(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {

        }
    }

    private class Consumer implements Runnable {
        UserNode parent;

        public Consumer(UserNode parent) {
            this.parent = parent;
        }

        @Override
        public void run() {

        }
    }
}
