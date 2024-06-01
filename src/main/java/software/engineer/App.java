package software.engineer;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.Node;
import org.apache.batik.swing.JSVGCanvas;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * 通过命令行参数读取文件内容，并实现预处理
 */
class InputFile
{
    private static final String FILE_PATH = "article.txt";
    private String file_path = null;
    private final String[] words;

    public InputFile(String[] args) throws IOException {
        read_args(args);
        this.words = read();
    }

    /**
     * 读取命令行参数中的指定文件（-f, --file）
     * @param args 参数列表
     */
    public void read_args(String[] args){
        for (int i = 0; i < args.length; i++) {
            if (("-f".equals(args[i]) || "--file".equals(args[i])) && i + 1 < args.length) {
                this.file_path = args[i + 1];
                break;
            }
        }
        if (this.file_path == null) this.file_path = FILE_PATH;
    }

    /**
     * 输入文件，预处理文件内容，返回单词列表
     * @return 单词列表
     */
    private String[] read() throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file_path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        String filter_non_alphabet = content.toString().replaceAll("[^A-Za-z]", " "); // 将非字母字符替换为空格
        return filter_non_alphabet.toLowerCase().split("\\s+");
    }

    public String[] getWords() {
        return this.words;
    }
}


/**
 * 图结构的接口
 * 后期可能需要用不同的数据结构实现程序：
 * -[x] 邻接矩阵的图结构（稠密图）
 * -[ ] 邻接表的图结构（稀疏图）
 */
interface Graph {
    int size(); // 顶点数

    /* 顶点 */
    void addVertex(String vertex);
    List<String> getVertexes(); // 顶点列表V
    int getVertex(String vertex); // vertex -> index
    String getVertex(int index); // index -> vertex
    List<String> getNeighbors(String v); // 邻居节点

    /* 边 */
    void addEdge(String a, String b) throws Exception; // 添加边（边权重+1）
    void addEdge(String a, String b, int value) throws Exception; // 设置边权重为 value
    int getEdge(String a, String b);
    List<Edge> getEdges(); // 边列表E


    /* 显示 */
    void print();

    /* 图算法 */
    List<List<Object>> Dijkstra(String v); // [路径, 路径长度]
}


record Edge(String from, String to, int value) {

    @Override
    public String toString() {
        return "(" + from + ", " + to + ") = " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return value == edge.value && Objects.equals(from, edge.from) && Objects.equals(to, edge.to);
    }

}


/**
 * 邻接矩阵数据结构的图实现
 */
class AdjMatrixGraph implements Graph{
    private final List<String> vertexes;
    private int[][] edges;
    private int size;

    public AdjMatrixGraph(String[] vertexes, int[][] edges) {
        this.vertexes = new ArrayList<>(Arrays.asList(vertexes));
        this.edges = edges;
    }
    public AdjMatrixGraph(String[] vertexes) {
        this.vertexes = new ArrayList<>(Arrays.asList(vertexes));
        this.size = this.vertexes.size();
        this.edges = new int[this.size][this.size];
    }
    public AdjMatrixGraph(){
        this.vertexes = new ArrayList<>();
        this.size = 0;
        this.edges = new int[0][];
    }
    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void addVertex(String vertex) {
        this.vertexes.add(vertex);
        int[][] newEdges = new int[this.size+1][this.size+1];
        for (int i=0; i<this.size; i++){
            System.arraycopy(this.edges[i], 0, newEdges[i], 0, this.size);
        }
        this.edges = newEdges;
        this.size++;
    }

    @Override
    public List<String> getVertexes() {
        return vertexes;
    }

    @Override
    public List<String> getNeighbors(String v) {
        List<String> results = new ArrayList<>();
        if (vertexes.contains(v)){
            int index = vertexes.indexOf(v);
            for (int i=0; i<this.size; i++){
                if (this.edges[index][i] > 0) results.add(vertexes.get(i));
            }
        }
        return results;
    }

    @Override
    public int getVertex(String vertex) {
        return vertexes.indexOf(vertex);
    }

    @Override
    public String getVertex(int index) {
        if (index < this.size && index >=0)
            return vertexes.get(index);
        else {
            throw new IndexOutOfBoundsException(index);
        }
    }


    @Override
    public void addEdge(String a, String b, int value) throws Exception {
        if (vertexes.contains(a) && vertexes.contains(b)){
            int index_a = vertexes.indexOf(a);
            int index_b = vertexes.indexOf(b);
            this.edges[index_a][index_b] = value;
        }
        else {
            throw new Exception("Vertex is not exist");
        }
    }

    @Override
    public void addEdge(String a, String b) throws Exception {
        if (vertexes.contains(a) && vertexes.contains(b)){
            int index_a = vertexes.indexOf(a);
            int index_b = vertexes.indexOf(b);
            this.edges[index_a][index_b]++;
        }
        else {
            throw new Exception("Vertex is not exist");
        }
    }

    @Override
    public List<Edge> getEdges() {
        List<Edge> results = new ArrayList<>();
        for (int i=0; i<this.size; i++){
            for (int j=0; j<this.size; j++){
                if (edges[i][j] > 0)
                    results.add(new Edge(vertexes.get(i), vertexes.get(j), edges[i][j]));
            }
        }
        return results;
    }

    @Override
    public int getEdge(String a, String b) {
        if (vertexes.contains(a) && vertexes.contains(b)) {
            int index_a = vertexes.indexOf(a);
            int index_b = vertexes.indexOf(b);
            return edges[index_a][index_b];
        }
        else {
            return -1;
        }
    }

    @Override
    public void print() {
        System.out.println("Adjacency Matrix:");
        System.out.println(this.vertexes.toString());
        for (int[] row : this.edges) {
            System.out.println(Arrays.toString(row));
        }
    }

    private int distance(int[][] dis, int i, int j){
        if (dis[i][j] > 0) return dis[i][j];
        else return 10000000;
    }

    @Override
    public List<List<Object>> Dijkstra(String v) {
        if (!this.vertexes.contains(v)) return null;
        int start = vertexes.indexOf(v);
        int[] visit = new int[this.size];
        int[] bestmin = new int[this.size];
        String[] path = new String[this.size];
        int max = 10000000;
        int[][] dis = new int[this.size][this.size];
        for(int i=0; i<this.size; i++) dis[i]=Arrays.copyOf(edges[i], this.size);
        visit[start] = 1;
        bestmin[start] = 0;

        //大循环（搞定这里就算搞定该算法了，后面的输出什么的可以不看）
        for(int l = 0; l < this.size; l++) {
            int Dtemp = max;
            int k = -1;

            //步骤① 找出与源点距离最短的那个点，即遍历distance[1][1]，distance[1][2],.....distance[1][N]中的最小值
            for(int i = 0; i < this.size; i++) {
                if(visit[i] == 0 && distance(dis, start, i) < Dtemp) {
                    Dtemp = distance(dis, start, i);
                    k = i;
                }
            }
            if (k == -1) continue;
            visit[k] = 1;
            bestmin[k] = Dtemp;

            //步骤② 松弛操作
            for(int i = 0; i < this.size; i++) {
                if(visit[i] == 0 && (distance(dis, start, k) + distance(dis, k, i)) < distance(dis, start, i)) {
                    dis[start][i] = distance(dis, start, k) + distance(dis, k, i);
                    path[i] = (path[k]==null?(v+"-->"+ vertexes.get(k)):path[k]) + "-->" + vertexes.get(i);
                }
                if (path[i] == null && (bestmin[i] > 0 || i==start)) path[i] = v+"-->"+ vertexes.get(i);
            }
        }

        //输出路径
        List<List<Object>> results = new ArrayList<>();
        for(int i=0; i<this.size; i++) {
            List<Object> t = new ArrayList<>();
            t.add(path[i]!=null?path[i]:v+" -x "+ vertexes.get(i)+" 不可达");
            t.add(path[i]!=null?bestmin[i]:-1);
            results.add(t);
        }
        return results;
    }
}


public class App
{
    private static Graph graph;
    private static final ImageFrame imageFrame = new ImageFrame();

    static volatile boolean isRunning=true;

    /**
     * 主程序入口，接收用户输入文件，生成图，并允许用户选择后续各项功能
     * @param args -f 或者 --file 指定输入文件路径
     */
    public static void main(String[] args) throws Exception {

        // 关闭日志显示
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        InputFile inputFile = new InputFile(args);
        String[] words = inputFile.getWords();
        graph = buildGraph(words);
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean flag = true;
        while (flag) {
            System.out.println("====================");
            System.out.println("请选择后续功能：");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两个单词之间的最短路径");
            System.out.println("5. 随机游走");
            System.out.println("0. 退出");

            int choice;
            while (!(scanner.hasNextInt())) {
                scanner.next();
                System.out.println("请输入一个数字：");
            }
            choice=scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> showDirectedGraph(graph);
                case 2 -> {
                    System.out.println("请输入两个单词(" + graph.getVertexes().toString() + "): ");
                    input = scanner.nextLine();
                    words = input.split("\\s+");
                    while (words.length < 2) {
                        System.out.println("请输入『两个』单词");
                        input = scanner.nextLine();
                        words = input.split("\\s+");
                    }
                    queryBridgeWords(words[0], words[1], true);
                }
                case 3 -> {
                    System.out.println("请输入一个句子");
                    input = scanner.nextLine();
                    System.out.println(generateNewText(input));
                }
                case 4 -> {
                    System.out.println("请输入一个或者两个单词(" + graph.getVertexes().toString() + "): ");
                    input = scanner.nextLine();
                    words = input.split("\\s+");
                    while (words.length < 1) {
                        System.out.println("请输入『一个或者两个』单词");
                        input = scanner.nextLine();
                        words = input.split("\\s+");
                    }
                    if (words.length == 1) System.out.println(calcShortestPath(words[0]));
                    else System.out.println(calcShortestPath(words[0], words[1]));
                }
                case 5 -> {
                    String randomwalk = randomWalk();
                    PrintWriter out = new PrintWriter("random_walk.txt");
                    out.print(randomwalk);
                    out.close();
                }
                case 0 -> flag = false;
                default -> System.out.println("无效选择，请重新输入");
            }
        }
        //这行不能删，因为画图程序会开后台线程（或者进程，不清楚），删去这行后画图线程（进程）不结束，程序无法退出。
        System.exit(0);
    }

    private static Graph buildGraph(String[] words) throws Exception {
        Set<String> set = new HashSet<>(Arrays.asList(words));
        Graph graph = new AdjMatrixGraph(set.toArray(new String[0]));
        String previousWord = null;
        for (String word : words) {
            if (previousWord != null) {
                graph.addEdge(previousWord, word);
            }
            previousWord = word;
        }
        return graph;
    }

    /**
     * 展示生成的有向图
     * ✅ 可选功能：将生成的有向图以图形文件形式保存到磁盘，可以调用外部
     * 绘图库或绘图工具API自动生成有向图，但不能采用手工方式绘图
     * @param g 有向图
     * @param path 突出标注路径
     * @param filename 保存文件名
     */
    private static void showDirectedGraph(Graph g, List<Edge> path, String filename) throws IOException {
        imageFrame.draw(g, path, filename);
    }

    private static void showDirectedGraph(Graph g) throws IOException {
        showDirectedGraph(g, new ArrayList<>(), "graph.svg");
    }

    /**
     * 在生成有向图之后，用户输入任意两个英文单词word1、word2，程
     * 序从图中查询它们的“桥接词”。
     * @param word1 单词1
     * @param word2 单词2
     * @param message 是否显示提示信息（功能3需要显示。功能4复用函数时不显示）
     * @return 桥接词列表
     */
    private static List<String> queryBridgeWords(String word1, String word2, boolean message){
        List<String> results = new ArrayList<>();
        if (graph.getVertexes().contains(word1) && graph.getVertexes().contains(word2)){
            List<String> pos = graph.getNeighbors(word1);
            for (String p : pos){
                if (graph.getNeighbors(p).contains(word2))
                    results.add(p);
            }
        }
        else if(message){
            System.out.println("No \"" + word1 + "\" or \"" + word2 + "\" in the graph!");
        }

        // 输出提示词
        if (message){
            if (results.size() == 0){
                System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
            } else if (results.size() == 1) {
                System.out.println("The bridge word from \"" + word1 + "\" to \"" + word2 + "\" is: "+results.get(0));
            } else {
                StringJoiner joiner = new StringJoiner(", ", "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ", ".");
                for (int i = 0; i < results.size(); i++) {
                    if (i == results.size() - 1) joiner.add("and " + results.get(i));
                    else joiner.add(results.get(i));
                }
                System.out.println(joiner);
            }
        }
        return results;
    }

    private static List<String> queryBridgeWords(String word1, String word2) {
        return queryBridgeWords(word1, word2, false);
    }

    /**
     * 用户输入一行新文本，程序根据之前输入文件生
     * 成的图，计算该新文本中两两相邻的单词的
     * bridge word，将bridge word插入新文本的两个
     * 单词之间，输出到屏幕上展示。
     * *********************
     * 测试输入：Seek to explore new and exciting synergies
     * 预期输出：Seek to explore strange new life and exciting synergies
     * *********************
     *  @param inputText 用户输入的一行新文本
     * @return 新生成的字符串
     */
    private static String generateNewText(String inputText) {
        String filter_non_alphabet = inputText.replaceAll("[^A-Za-z]", " "); // 将非字母字符替换为空格
        String[] words = filter_non_alphabet.split("\\s+"); // 分割处理后的文本
        String preword = null;
        StringBuilder result = new StringBuilder();
        for (String word : words){
            if (preword != null){
                List<String> bridges = queryBridgeWords(preword, word.toLowerCase());
                if (bridges.size() == 1) result.append(bridges.get(0)).append(" ");
                else if (bridges.size() > 1) {
                    Random random = new Random();
                    int randomIndex = random.nextInt(bridges.size());
                    result.append(bridges.get(randomIndex)).append(" ");
                }
            }
            result.append(word).append(" ");
            preword = word.toLowerCase();
        }
        return result.toString();
    }

    /**
     * 用户输入两个单词，程序计算它们之间在图中的
     * 最短路径（路径上所有边权值之和最小），以某
     * 种突出的方式将路径标注在原图并展示在屏幕上
     * ，同时展示路径的长度（所有边权值之和）。
     * *********************
     * ✅ 可选功能：如果用户只输入一个单词，则程序计
     * 算出该单词到图中其他任一单词的最短路径，并
     * 逐项展示出来。
     * *********************
     * @param word1 单词1
     * @param word2 单词2
     * @return 最短路径的字符串
     */
    private static String calcShortestPath(String word1, String word2) throws IOException {
        List<List<Object>> paths = graph.Dijkstra(word1);
        if (paths == null) return "\"" + word1 + "\" is not exist";
        if (!graph.getVertexes().contains(word2)) return "\"" + word2 + "\" is not exist";
        List<Object> res = paths.get(graph.getVertex(word2));

        // 突出显示路径
        List<Edge> path = new ArrayList<>();
        String[] nodes = ((String) res.get(0)).split("-->");
        String preword = null;
        for (String node : nodes){
            if (preword != null) path.add(new Edge(preword, node, graph.getEdge(preword, node)));
            preword = node;
        }
        showDirectedGraph(graph, path, "graph_path.svg");
        return ("(" + res.get(1) + "): " + res.get(0));
    }

    private static String calcShortestPath(String word1) {
        List<List<Object>> paths = graph.Dijkstra(word1);
        if (paths == null) return "\"" + word1 + "\" is not exist";
        StringBuilder res = new StringBuilder();
        for (List<Object> path : paths){
            res.append("(").append(path.get(1)).append("): ").append(path.get(0)).append("\n");
        }
        return res.toString();
    }

    /**
     * 进入该功能时，程序随机的从图中选择一个节
     * 点，以此为起点沿出边进行随机遍历，记录经
     * 过的所有节点和边，直到出现第一条重复的边
     * 为止，或者进入的某个节点不存在出边为止。
     * 在遍历过程中，用户也可通过键入'i'键随时
     * 停止遍历。
     * *********************
     * @return 随机路径的字符串
     */
    private static String randomWalk() throws IOException, InterruptedException, NativeHookException {
        boolean[] visited = new boolean[graph.size()];
        // 随机起点
        Random random = new Random();
        int randomIndex = random.nextInt(graph.size());
        String v = graph.getVertex(randomIndex);

        StringBuilder path = new StringBuilder(v);
        List<Edge> paths = new ArrayList<>();
        visited[randomIndex] = true;
        List<String> neighbors;

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("注册全局键盘监听器失败");
            System.exit(1);
        }

        isRunning=true;
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (NativeKeyEvent.getKeyText(e.getKeyCode()).equalsIgnoreCase("i")) {
                    isRunning = false;
                }
            }

            public void nativeKeyReleased(NativeKeyEvent e) { }

            public void nativeKeyTyped(NativeKeyEvent e) { }
        });

        while ((neighbors = graph.getNeighbors(v)) != null) {
            Thread.sleep(2000);
            if (isRunning) {
                // 随机选择邻居
                randomIndex = random.nextInt(graph.size());
                while (!neighbors.contains(graph.getVertex(randomIndex))) randomIndex = random.nextInt(graph.size());

                path.append("-->").append(graph.getVertex(randomIndex));
                paths.add(new Edge(v, graph.getVertex(randomIndex), graph.getEdge(v, graph.getVertex(randomIndex))));

                imageFrame.draw(graph, paths, "random_walk.svg");
                System.out.println(path);

                if (visited[randomIndex]) break;
                else visited[randomIndex] = true;
                v = graph.getVertex(randomIndex);
            }
            else {
                System.out.println("User interrupt.");
                break;
            }
        }

        GlobalScreen.unregisterNativeHook();
        return path.toString();
    }
}


/**
 * GUI窗口，用于展示有向图
 * 利用 Graphviz 生成图片并保存，读取图片展示在界面上。
 */
class ImageFrame extends JFrame {

    public void draw(Graph graph, List<Edge> path, String filename) throws IOException {
        setTitle("Graph Display");

        // 移除所有组件
        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.repaint();
        contentPane.revalidate();

        generateImage(graph, path, filename);

        // 使用Batik创建SVG画布
        JSVGCanvas canvas = new JSVGCanvas();
        canvas.setURI(filename);

        // 创建一个标签并设置画布
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);

        // 设置窗口大小
        setSize(new Dimension(500, 500));

//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void generateImage(Graph graph, List<Edge> path, String filename) throws IOException {
        List<String> vertexes = graph.getVertexes();
        List<Edge> edges = graph.getEdges();
        Map<String, Node> nodes = vertexes.stream().collect(Collectors.toMap(vertex -> vertex, Factory::node, (a, b) -> b));
        LinkSource[] linkSources = new LinkSource[edges.size()];
        int index = 0;
        for (Edge edge:edges){
            linkSources[index++] = nodes.get(edge.from())
                    .link(
                            Link.to(nodes.get(edge.to()))
                                    .with(guru.nidi.graphviz.attribute.Label.of(Integer.toString(edge.value())), path.contains(edge)? guru.nidi.graphviz.attribute.Color.RED: Color.BLACK)
                    );
        }
        guru.nidi.graphviz.model.Graph g = graph("text").directed()
                .linkAttr().with("class", "link-class")
                .with(linkSources);
        Graphviz.fromGraph(g).render(Format.SVG).toFile(new File(filename));
    }
}