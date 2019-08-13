package bearmaps.utils.ps;

import bearmaps.utils.graph.streetmap.Node;

public class NodePoint extends Point {

    private Node thisNode;

    public NodePoint(Node n) {
        super(n.lon(), n.lat());
        this.thisNode = n;
    }

    public long id() {
        return thisNode.id();
    }
}
