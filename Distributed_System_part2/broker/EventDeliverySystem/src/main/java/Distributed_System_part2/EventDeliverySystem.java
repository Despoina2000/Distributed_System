package Distributed_System_part2;

import Distributed_System_part2.app.Nodes.Broker;
import Distributed_System_part2.app.Nodes.UserNode;

import java.util.Locale;

public class EventDeliverySystem {

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) System.out.println("Expected 1 or 2 arguments, got " + args.length);
        else {
            if (args.length == 1 && args[0].toLowerCase(Locale.ROOT).startsWith("use")) {
                UserNode userNode = new UserNode();
            } else if (args.length == 2 && args[0].toLowerCase(Locale.ROOT).startsWith("bro")) {
                Thread brokerThread = new Thread(new Broker(Integer.parseInt(args[1])));
                brokerThread.start();
            } else {
                System.out.println("Expected arguments: broker <number> / user");
            }
        }
    }
}
