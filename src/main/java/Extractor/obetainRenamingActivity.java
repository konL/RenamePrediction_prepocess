package Extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class obetainRenamingActivity {
    public static void main(String[] args) throws Exception {
        //SINGLE-PROCESS
//        HistoryAnalysis.ProjectCommit("CS225FinalProject","2");

        //BATCH-PROCESS
        List<String> brokenProject=new ArrayList<>();
        String path = "C:\\project\\Projects_50\\Java\\";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        String group="4";
        for(int i=33;i<=40;i++){					//遍历File[]数组
            String project=fs[i].getName();
            try {
                HistoryAnalysis.ProjectCommit(project,group);
            }catch (Exception e){
                brokenProject.add(project);
                continue;

            }

            System.out.println(project+" finish");
    }

        BufferedWriter bw=new BufferedWriter(new FileWriter("C:\\project\\Projects_50\\ERROR_GROUP"+group+".txt"));
        bw.write(brokenProject.toString());
        bw.close();


    }
}
