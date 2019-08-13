package bearmaps.test;

import bearmaps.AugmentedStreetMapGraph;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class TestAutoComplete {
    private static final String PARAMS_FILE
        = "../library-su19/data/proj3_test_inputs/path_params.txt";
    private static final String RESULTS_FILE
        = "../library-su19/data/proj3_test_inputs/path_results.txt";
    private static final int NUM_TESTS = 8;
    private static final String OSM_DB_PATH
        = "../library-su19/data/proj3_xml/berkeley-2019.osm.xml";
    private static AugmentedStreetMapGraph graph;
    private static boolean initialized = false;

    @Before
    public void setUp() throws Exception {
        if (initialized) {
            return;
        }
        graph = new AugmentedStreetMapGraph(OSM_DB_PATH);
        initialized = true;

    }

    @Test
    public void sanityCTest() {
        List<String> startsWithC = graph.getLocationsByPrefix("c");
        assertTrue(startsWithC.contains("Citibank"));
        assertTrue(startsWithC.contains("citibank"));
    }

    @Test
    public void sanityMATest() {
        List<String> startsWithMA = graph.getLocationsByPrefix("ma");
        assertFalse(startsWithMA.contains("McDonald's"));
    }

    @Test
    public void sanityLocationGetTest() {
        List<Map<String, Object>> berry = graph.getLocations("black repertory group");
        assertTrue(berry.size() != 0);
    }

    @Test
    public void testRandomGetPrefix() {
        Random randomer = new Random(System.currentTimeMillis());
        for (int i = 0; i < 1000; i++) {
            int length = randomer.nextInt(2);
            String k = "";
            for (int j = 0; j < length; j++) {
                k = k + (char) (97 + randomer.nextInt(26));
            }
            String kp = k;
            List<String> startsWithMA = graph.getLocationsByPrefix(k);
            if (startsWithMA.isEmpty()) {
                continue;
            }
            startsWithMA.sort(String::compareTo);
            List<String> expected = new ArrayList<>(graph.cleanedNameMap.entrySet().stream()
                .filter((e) -> e.getKey().startsWith(kp))
                .map(e -> e.getValue())
                .reduce((s1, s2) -> {
                    Set<String> tmp = new HashSet<>(s1);
                    tmp.addAll(s2);
                    return tmp;
                }).orElse(new HashSet<String>()));
            expected.sort(String::compareTo);
            assertEquals(startsWithMA, expected);
        }
    }

    @Test
    public void testGivenPrefix() {
        String[] prefixesToTest = new String[]{
            "ma", "k", "kp", "ma ", "s", "berk", "grizz", "top", "oa", "im"
        };
        for (String k : prefixesToTest) {
            List<String> startsWithMA = graph.getLocationsByPrefix(k);
            startsWithMA.sort(String::compareTo);
            List<String> expected = new ArrayList<>(graph.cleanedNameMap.entrySet().stream()
                .filter((e) -> e.getKey().startsWith(k))
                .map(e -> e.getValue())
                .reduce((s1, s2) -> {
                    Set<String> tmp = new HashSet<>(s1);
                    tmp.addAll(s2);
                    return tmp;
                }).orElse(new HashSet<String>()));
            expected.sort(String::compareTo);
            assertEquals(startsWithMA, expected);
        }
    }

    private List<Map<String, Double>> paramsFromFile() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(PARAMS_FILE), Charset.defaultCharset());
        List<Map<String, Double>> testParams = new ArrayList<>();
        int lineIdx = 2; // ignore comment lines
        for (int i = 0; i < NUM_TESTS; i++) {
            Map<String, Double> params = new HashMap<>();
            params.put("start_lon", Double.parseDouble(lines.get(lineIdx)));
            params.put("start_lat", Double.parseDouble(lines.get(lineIdx + 1)));
            params.put("end_lon", Double.parseDouble(lines.get(lineIdx + 2)));
            params.put("end_lat", Double.parseDouble(lines.get(lineIdx + 3)));
            testParams.add(params);
            lineIdx += 4;
        }
        return testParams;
    }

    private List<List<Long>> resultsFromFile() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(RESULTS_FILE), Charset.defaultCharset());
        List<List<Long>> expected = new ArrayList<>();
        int lineIdx = 2; // ignore comment lines
        for (int i = 0; i < NUM_TESTS; i++) {
            int numVertices = Integer.parseInt(lines.get(lineIdx));
            lineIdx++;
            List<Long> path = new ArrayList<>();
            for (int j = 0; j < numVertices; j++) {
                path.add(Long.parseLong(lines.get(lineIdx)));
                lineIdx++;
            }
            expected.add(path);
        }
        return expected;
    }

}
