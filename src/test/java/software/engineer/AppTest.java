package software.engineer;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
//        AdjMatrixGraph graph = new AdjMatrixGraph();
//        graph.addVertex("a");
//        graph.addVertex("b");
////        System.out.println(graph.getVertex().toString());
////        System.out.println(graph.getEdges().toString());
//        graph.print();
    }

    private Graph graph;
    private void initGraph() throws Exception {
        String[] arg = {"-f", "article.txt"};
        InputFile inputFile = null;
        inputFile = new InputFile(arg);
        String[] words = inputFile.getWords();
        graph = new AdjMatrixGraph();
        String previousWord = null;
        for (String word : words) {
            graph.addVertex(word);
            if (previousWord != null) {
                graph.addEdge(previousWord, word);
            }
            previousWord = word;
        }
    }
    public void testFindNeighbor() throws Exception {
        initGraph();
        System.out.println(graph.getNeighbors("to").toString());

    }

    public void testBridgeWord() throws Exception {
        initGraph();
        assertEquals("No bridge words from \"seek\" to \"to\"!", queryBridgeWords(graph, "seek", "to"));
        assertEquals("No bridge words from \"to\" to \"explore\"!", queryBridgeWords(graph, "to", "explore"));
        assertEquals("The bridge word from \"explore\" to \"new\" is: strange", queryBridgeWords(graph, "explore", "new"));
    }

    private static String queryBridgeWords(Graph g, String word1, String word2){
        List<String> results = new ArrayList<>();
        if (g.getVertexes().contains(word1) && g.getVertexes().contains(word2)){
            List<String> pos = g.getNeighbors(word1);
            for (String p : pos){
                if (g.getNeighbors(p).contains(word2))
                    results.add(p);
            }
        }else {
            return ("No \"" + word1 + "\" or \"" + word2 + "\" in the graph!");
        }

        // 输出提示词
        if (results.size() == 0){
            return ("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
        } else if (results.size() == 1) {
            return ("The bridge word from \"" + word1 + "\" to \"" + word2 + "\" is: "+results.get(0));
        } else {
            StringJoiner joiner = new StringJoiner(", ", "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ", ".");
            for (int i = 0; i < results.size(); i++) {
                if (i == results.size() - 1 && i != 0) joiner.add("and " + results.get(i));
                else joiner.add(results.get(i));
            }
            return (joiner.toString());
        }
    }

    public void testGraph() throws IOException {
        String[] vertexes = {"aa", "bb", "cc"};
        Edge[] edges = {new Edge("aa", "bb", 1), new Edge("bb", "cc", 1)};
        Map<String, Node> map = new HashMap<>();
        for (String vertex:vertexes){
            map.put(vertex, node(vertex));
        }
        LinkSource[] linkSources = new LinkSource[edges.length];
        int index = 0;
        for (Edge edge:edges){
            linkSources[index++] = map.get(edge.from()).link(to(map.get(edge.to())).with(Label.of(Integer.toString(edge.value()))));
        }
        guru.nidi.graphviz.model.Graph g = graph("text").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .linkAttr().with("class", "link-class")
                .with(
            linkSources
        );
        Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("example/ex1.png"));
    }
}