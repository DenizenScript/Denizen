package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dLocation;

import java.util.HashSet;

public class PathFinder {

    public static class Node {
        public dLocation position;
        public double cost;
        public double pathCost;
        public Node next;
        public Node nextListElement;
    }

    static class MinHeap {
        public Node head;
        public boolean hasNext() {
            return head != null;
        }
        public void add(Node node) {
            if (head == null) {
                head = node;
            }
            else if (head.next == null && node.cost < head.cost) {
                node.nextListElement = head;
                head = node;
            }
            else {
                Node cur = head;
                while (cur.nextListElement != null && cur.nextListElement.cost < node.cost)
                {
                    cur = cur.nextListElement;
                }
                node.nextListElement = cur.nextListElement;
                cur.nextListElement = node;
            }
        }
        public Node extractFirst()
        {
            Node res = head;
            head = head.nextListElement;
            return res;
        }
    }

    public static dLocation[] surroundings = new dLocation[] {
            new dLocation(null, 1, 0, 0),
            new dLocation(null, -1, 0, 0),
            new dLocation(null, 0, 0, 1),
            new dLocation(null, 0, 0, -1),
            new dLocation(null, 0, 1, 1),
            new dLocation(null, 0, 1, -1),
            new dLocation(null, 1, 1, 0),
            new dLocation(null, -1, 1, 0),
            new dLocation(null, 0, -1, 1),
            new dLocation(null, 0, -1, -1),
            new dLocation(null, 1, -1, 0),
            new dLocation(null, -1, -1, 0)
    };

    public static Node findPath(dLocation start, dLocation end, int radius) {
        int maxRadius = Settings.pathfindingMaxDistance();
        Node snode = new Node();
        snode.position = start;
        MinHeap heaplist = new MinHeap();
        heaplist.add(snode);
        HashSet<dLocation> tried = new HashSet<dLocation>(100);
        tried.add(start);
        while (heaplist.hasNext()) {
            Node curr = heaplist.extractFirst();
            if (curr.position.distanceSquared(end) <= radius * radius) {
                Node n = new Node();
                n.position = start;
                n.cost = curr.pathCost + 1;
                n.pathCost = curr.cost + 1;
                n.next = curr;
                return n;
            }
            for (int i = 0; i < surroundings.length; i++) {
                dLocation surr = surroundings[i];
                dLocation point = new dLocation(start.getWorld(), curr.position.getX() + surr.getX(), curr.position.getY() + surr.getY(),
                        curr.position.getZ() + surr.getZ());
                if (pointIsFree(point, maxRadius, start) && !tried.contains(point)) {
                    tried.add(point);
                    Node node = new Node();
                    node.position = point;
                    double surrCost = lengthSquared(surr);
                    node.cost = curr.pathCost + surrCost + point.distanceSquared(end);
                    node.pathCost = curr.pathCost + surrCost;
                    node.next = curr;
                    heaplist.add(node);
                }
            }
        }
        return null; // Give up
    }

    public static double lengthSquared(dLocation loc) {
        return loc.getX() * loc.getX() + loc.getY() * loc.getY() + loc.getZ() * loc.getZ();
    }

    public static boolean pointIsFree(dLocation point, int maxRadius, dLocation start) {
        return point.getWorld() != null
                && !point.getBlock().getType().isSolid()
                && !point.clone().add(0, 1, 0).getBlock().getType().isSolid()
                && point.clone().add(0, -1, 0).getBlock().getType().isSolid()
                && point.distanceSquared(start) < maxRadius * maxRadius;
    }
}
