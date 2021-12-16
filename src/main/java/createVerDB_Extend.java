import com.csvreader.CsvReader;
import detectId.Trace.SyncPipe;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class createVerDB_Extend {
    public static void main(String[] args) throws Exception {
        //传入项目名称
        //ProjectCommit("zeppelin");
        ProjectCommit("zeppelin");
       //ProjectCommit("facebook-android-sdk");
        //ProjectCommit("dubbo");
    }

    private static void ProjectCommit(String project) throws Exception {
        String projectpath="C:\\project\\MethodPrediction_All_ID\\data\\GitProject\\"+project;
        String csvPath="C:\\project\\MethodPrediction_All_ID\\Renaming\\"+project+".csv";

//        String LogOutput = "C:/project/MethodPrediction_All_ID/Renaming/log/commit_"+project+".txt";
//        //保存当前实验中的最新commit
//        System.out.println(ExecuteCommand(projectpath, "git rev-parse HEAD ",LogOutput));

//①读取dump中每个项目中的重命名标识符
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(csvPath);
            while (csvReader.readRecord()){
               //变化标识符 hisID
                String his=csvReader.get(3);
                String[] hisId=his.split("<-");

                //变化id所在文件（有时候文件夹变化）
                String locHis=csvReader.get(2);
                String[] loc=locHis.split("<=");

                for(int i=0;i<hisId.length-1;i++){
                    String change=hisId[i]+"<-"+hisId[i+1];
                    //每次重命名活动发生的commitId
                    String curCom=csvReader.get(4+i);
                    //根据commitId生成两个前后实现文件
                    genFile(project,loc[i],curCom,change);







                }
                //结束后把old文件夹和new文件夹中的内容清空




            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private static void ProjectDiff(String change) {
        //读取旧文件，新文件，对比
        //读取变化的标识符
        String changeId=change;
    }

    private static void genFile(String project, String loc, String curCom, String change) throws Exception {
        String[] srcAnddst=loc.split("<-");
        change=change.replace("<-","_");
        //获取FileName和前面的文件夹名字
        String[] data_n=srcAnddst[0].split("\\\\");
        String fileName_n=data_n[data_n.length-1];

        String[] data_o=srcAnddst[1].split("\\\\");
        String fileName_o=data_o[data_o.length-1];



        File file =new File("C:\\project\\MethodPrediction_All_ID\\versionDB\\"+project+"\\"+project+"_new");
//如果文件夹不存在则创建 false
        if  (!file .exists()  && !file .isDirectory())
        {
            System.out.println("//不存在");
            file .mkdirs();
        } else
        {
            System.out.println("//目录存在");
        }

        File file2 =new File("C:\\project\\MethodPrediction_All_ID\\versionDB\\"+project+"\\"+project+"_old");
//如果文件夹不存在则创建 false
        if  (!file2 .exists()  && !file2 .isDirectory())
        {
            System.out.println("//不存在");
            file2 .mkdirs();
        } else
        {
            System.out.println("//目录存在");
        }
        File[] files1=file.listFiles();
        File[] files2=file2.listFiles();
        List<String> f1=new ArrayList<>();
        List<String> f2=new ArrayList<>();
        for(File f:files1){
            f1.add(f.getName());
        }
        for(File f:files2){
            f2.add(f.getName());
        }
        //false+false
        if((f1.contains(fileName_o))||(f2.contains(fileName_n))){
            return;
        }


        //newCom读取当前的代码
        generateNew(srcAnddst[0],curCom,fileName_n,"C:/project/MethodPrediction_All_ID/versionDB/"+project+"/"+project+"_new/"+curCom+"_"+change+"_"+fileName_n);

        //oldcam读取记录代码的上一个版本
        generateOld(srcAnddst[1],fileName_o,"C:/project/MethodPrediction_All_ID/versionDB/"+project+"/"+project+"_old/"+curCom+"_"+change+"_"+fileName_o);

    }

    private static void generateOld(String location,String fileName, String output) throws Exception {
        System.out.println("location="+location);

        String[] data=location.split("\\\\");
//        for (String s:data){
//            System.out.println("generateNew======>"+s);
//
//        }

        String proj=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8]+"\\\\"+data[10];

        //先回退到版本old,再回退到上一个版本
//        ExecuteCommand(proj,"git reset --hard "+oldCom,"C:\\Users\\delll\\IdeaProjects\\DatasetCreate\\src\\main\\resources\\result.txt");
//        copyTo(location,output);
        ExecuteCommand(proj,"git reset --hard \"HEAD^\"","C:\\Users\\delll\\IdeaProjects\\DatasetCreate\\src\\main\\resources\\result.txt");


        copyTo(location,output);

    }

    private static void generateNew(String location, String curCom,String fileName,String output) throws Exception {
        System.out.println("location="+location);
        System.out.println("output="+output);

        String[] data=location.split("\\\\");


        String proj=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8]+"\\\\"+data[10];

        ExecuteCommand(proj,"git reset --hard "+curCom,"C:\\Users\\delll\\IdeaProjects\\DatasetCreate\\src\\main\\resources\\result.txt");
        copyTo(location,output);
    }

    private static void copyTo(String location, String output) throws IOException {
        Boolean badFile=match(output);
        //读取
        File in=new File(location);

        //写入
        File out=new File(output);
        if(in.exists() && !badFile) {
            System.out.println("hello====");
            BufferedReader br = new BufferedReader(new FileReader(in));
            BufferedWriter bw = new BufferedWriter(new FileWriter(out));

            String line = "";
            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }

            br.close();
            bw.close();
        }
    }

    private static Boolean match(String fileName) {
        fileName=fileName.substring(0,fileName.lastIndexOf("."));
        //假设任意一个 true
        boolean isBad=fileName.contains("<")||fileName.contains(">")||fileName.contains(".")||fileName.contains("\"");

//        return fileName.matches("[^/\\\\<>*?|\"]+\\.[^/\\\\<>*?|\"]+");
        return isBad;
    }


    private static String readCurCommit(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line = br.readLine();

        br.close();
        return line;
    }

    public static String ExecuteCommand(String projectdir,String cmd,String output) throws Exception
    {
        String final_com=null;
        System.out.println("projectdir:"+projectdir);

        String[] command =
                {
                        "cmd",
                };
        Process p = Runtime.getRuntime().exec(command);
        new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
        new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
        PrintWriter stdin = new PrintWriter(p.getOutputStream());
        stdin.println("c:");
        stdin.println("cd "+projectdir);

            stdin.println(cmd + " > " + output);



        stdin.close();
        int returnCode = p.waitFor();
        System.out.println("Return code = " + returnCode);

        try (FileReader reader = new FileReader(output);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            //网友推荐更加简洁的写法
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                //System.out.println(line);
                final_com=line;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return final_com;
    }
}
