package bearmaps;

import bearmaps.utils.graph.streetmap.Node;
import bearmaps.utils.graph.streetmap.StreetMapGraph;
import bearmaps.utils.ps.NodePoint;
import bearmaps.utils.ps.Point;
import bearmaps.utils.ps.WeirdPointSet;
import bearmaps.utils.trie.MyTrieSet;
import bearmaps.utils.trie.TrieSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 * @author Alan Yao, Josh Hug, ________
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {

    private WeirdPointSet pointSet;

    private TrieSet trieNames;
    private boolean mapInitialzed = false;
    public Map<String, Set<String>> cleanedNameMap = new HashMap<>();
    private Map<String, Set<Node>> nameNodeMap = new HashMap<>();

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        List<Node> nodes = this.getNodes(); //
        List<NodePoint> points = nodes.stream().parallel()
            .filter(n -> this.neighbors(n.id()).size() != 0)
            .map(NodePoint::new)
            .collect(Collectors.toList());
        pointSet = new WeirdPointSet(points);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point res = pointSet.nearest(lon, lat);
        return ((NodePoint) res).id();
    }


    private void initMap() {
        nameNodeMap = nodes.values().parallelStream()
            .filter((node) -> node.name() != null)
            .collect(Collectors.toMap(
                (node) -> cleanString(node.name()),
                (node) -> newNodeSet(node),
                (conf1, conf2) -> {
                    conf1.addAll(conf2);
                    return conf1;
                }
            ));

        cleanedNameMap = nameNodeMap.entrySet().parallelStream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                (e) -> e.getValue().stream().map(Node::name).collect(Collectors.toSet())
            ));

        trieNames = new MyTrieSet();
        cleanedNameMap.keySet().forEach(trieNames::add);
        mapInitialzed = true;
    }

    private Set<Node> newNodeSet(Node initElement) {
        Set<Node> toReturn = new HashSet<>();
        toReturn.add(initElement);
        return toReturn;
    }

    /**
     * For Project Part III (extra credit)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        if (!mapInitialzed) {
            initMap();
        }
        List<String> names = trieNames.keysWithPrefix(cleanString(prefix));
        return names.stream().parallel()
            .map(cleanedNameMap::get)
            .flatMap(Set::stream)
            .collect(Collectors.toList());
    }

    /**
     * For Project Part III (extra credit)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        if (!mapInitialzed) {
            initMap();
        }
        return nameNodeMap.getOrDefault(locationName, new HashSet<>())
            .parallelStream().map((n) -> {
                Map<String, Object> tmp = new HashMap<>();
                tmp.put("lat", n.lat());
                tmp.put("lon", n.lon());
                tmp.put("name", n.name());
                tmp.put("id", n.id());
                return tmp;
            }).collect(Collectors.toList());
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}
