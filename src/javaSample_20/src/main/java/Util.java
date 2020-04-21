import java.util.List;

public class Util {



    public static void addEdge_weight(Dijkstra dj,int from,int to ,double cost){
        dj.addEdge(from,to,cost);
        dj.addEdge(to,from,cost);
    }

    public static void addEdge_NoWeight(Dijkstra dj,int from,int to){
        dj.addEdge(from,to,1);
        dj.addEdge(to,from,1);
    }

    public static void printResult(Result result){
        System.out.println("*****************************************");
        System.out.println(result.cost);
        result.path.forEach(i-> System.out.println(i));
    }

    public static int getGraphSize(Dijkstra graph){
        int i=0;
        for(List<Dijkstra.Edge> list:graph.getGraph()){
            i+=list.size();
        }
        return i;
    }

    public static void printList(List<Integer> list){
        for (int i:list){
            System.out.print(i);
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void init_default_list(List<Integer> list){
        int curIndex=Main.config.index;
        int total_node=Main.config.mainConfig.nodeCount;

        if(curIndex==1){
            list.add(total_node);
        }

        if(curIndex==total_node){
            list.add(1);
        }

        if(curIndex*2<=total_node){
            list.add(curIndex*2);
        }
        if((curIndex*2+1)<=total_node){
            list.add(curIndex*2+1);
        }

        if(curIndex/2>0){
            list.add(curIndex/2);
        }



        if(((curIndex-1)*2+1)>total_node&&curIndex%2==0){
            list.add(curIndex-1);
        }
        if((curIndex*2+1)>total_node&&curIndex%2==1){
            list.add(curIndex+1);
        }


    }

}
