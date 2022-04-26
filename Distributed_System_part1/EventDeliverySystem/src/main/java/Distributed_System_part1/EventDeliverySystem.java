package Distributed_System_part1;

import Distributed_System_part1.Nodes.Broker;
import Distributed_System_part1.Nodes.UserNode;

import java.util.Locale;

public class EventDeliverySystem {

    public static void main(String[] args) {
        if (args.length != 1) System.out.println("Expected 1 argument, got " + args.length);
        else {
            if (args[0].toLowerCase(Locale.ROOT).startsWith("use")) {
                UserNode userNode = new UserNode();
            } else if (args[0].toLowerCase(Locale.ROOT).startsWith("bro")) {
                Thread brokerThread = new Thread(new Broker());
                brokerThread.start();
            } else {
                System.out.println("Expected arguments: broker/user");
            }
        }
    }
}
