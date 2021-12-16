import com.csvreader.CsvReader;
import detectId.Trace.SyncPipe;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class createVerDB {
    public static void main(String[] args) throws Exception {
        //single-process:
        //ProjectCommit("abdera");

        //batch-process:生成前十个项目的versionDB
        //遍历的路径
        String path = "C:\\project\\Projects_50\\Renaming\\5";
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中

        for(File proj:fs) {                    //遍历File[]数组

           ProjectCommit(proj.getName());

        }
        //遍历项目
//        String[] projs={"beam","cassandra","dubbo","hbase","jmeter","storm","tomcat","zeppelin"};
//        for(String proj:projs) {                    //遍历File[]数组
//
//           ProjectCommit(proj);
//
//        }


    }

    private static void ProjectCommit(String project) throws Exception {
//        String projectpath="C:\\project\\Projects_50\\Java\\"+project;
////        String csvPath="C:\\project\\Projects_50\\Renaming\\method\\"+project+"_method.csv";
//        String csvPath="C:\\project\\Projects_50\\Renaming\\"+project+".csv";
//        //String csvPath="C:\\project\\IdentifierStyle\\log\\dump\\"+project+"_test.csv";
//        String LogOutput = "C:\\project\\Projects_50\\log.txt";

        String projectpath="C:\\project\\MethodPrediction_All_ID\\data\\GitProject\\"+project;
//        String csvPath="C:\\project\\Projects_50\\Renaming\\method\\"+project+"_method.csv";
        String csvPath="C:\\project\\MethodPrediction_All_ID\\Renaming\\"+project+".csv";
        //String csvPath="C:\\project\\IdentifierStyle\\log\\dump\\"+project+"_test.csv";
        String LogOutput = "C:\\project\\MethodPrediction_All_ID\\log.txt";




        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(csvPath);
            while (csvReader.readRecord()){
                // 读一整行
                // System.out.println(csvReader.getRawRecord());
                // 打印标识符变化情况
                System.out.println(csvReader.get(3));
                //生成对比的两个文件
                String locHis=csvReader.get(2);
                String his=csvReader.get(3);
                String[] hisId=his.split("<-");
                String[] loc=locHis.split("<=");
                //第一个版本比对

                //String curCom=finalCom;
//                String oldCom=csvReader.get(4);


                for(int i=0;i<hisId.length-1;i++){
                    String change=hisId[i]+"<-"+hisId[i+1];
                    System.out.println(change);
//                    System.out.println(curCom+","+oldCom);
                    String curCom=csvReader.get(4+i);
                    //生成两个文件传入ProjectDiff
                    genFile(project,loc[i],curCom,change);
                    ProjectDiff(change);
                    //删除新旧文件夹中的对比文件
                    curCom=csvReader.get(4+(i+1));


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

//        File file1 =new File("C:\\project\\Projects_50\\VersionDB\\raw_project\\"+project+"\\"+project+"_new");
        File file1 =new File("C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+project+"\\"+project+"_new");
//如果文件夹不存在则创建 false
        if  (!file1 .exists()  && !file1 .isDirectory())
        {
            System.out.println("//不存在");
            file1 .mkdirs();
        } else
        {
            System.out.println("//目录存在");
        }

//        File file2 =new File("C:\\project\\Projects_50\\VersionDB\\raw_project\\"+project+"\\"+project+"_old");
        File file2 =new File("C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+project+"\\"+project+"_old");
//如果文件夹不存在则创建 false
        if  (!file2 .exists()  && !file2 .isDirectory())
        {
            System.out.println("//不存在");
            file2 .mkdirs();
        } else
        {
            System.out.println("//目录存在");
        }
        //去重。后续一个renaming对应一个文件比较方便，但是label=0的我们就要去重
//        File[] files1=file1.listFiles();
//        File[] files2=file2.listFiles();

//        List<String> f1=new ArrayList<>();
//        List<String> f2=new ArrayList<>();
//        for(File f:files1){
//            String[] data=f.getName().split("_");
//            f1.add(data[0]+"_"+data[data.length-1]);
//        }
//        for(File f:files2){
//            String[] data=f.getName().split("_");
//            f2.add(data[0]+"_"+data[data.length-1]);
//        }
//
//
//        if((f1.contains(curCom+"_"+fileName_o))||(f2.contains(curCom+"_"+fileName_n))){
//            return;
//        }




        //newCom读取当前的代码
//        generateNew(srcAnddst[0],curCom,fileName_n,"C:\\project\\Projects_50\\VersionDB\\raw_project\\"+project+"\\"+project+"_new\\"+curCom+"_"+change+"_"+fileName_n);
        generateNew(srcAnddst[0],curCom,fileName_n,"C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+project+"\\"+project+"_new\\"+curCom+"_"+change+"_"+fileName_n);

        //oldcam读取记录代码的上一个版本
//        generateOld(srcAnddst[1],fileName_o,"C:\\project\\Projects_50\\VersionDB\\raw_project\\"+project+"\\"+project+"_old\\"+curCom+"_"+change+"_"+fileName_o);
        generateOld(srcAnddst[1],fileName_o,"C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+project+"\\"+project+"_old\\"+curCom+"_"+change+"_"+fileName_o);

    }

    private static void generateOld(String location,String fileName, String output) throws Exception {

        String[] data=location.split("\\\\");
//        for (String s:data){
//            System.out.println("generateNew======>"+s);
//
//        }


//        String projdir=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8];
//        String proj=data[8];
//        System.out.println("new location="+proj);
//        System.out.println("parmeter location="+location);
        String projdir=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8]+"\\\\"+data[10];
        String proj=data[10];
        System.out.println("new location="+proj);
        System.out.println("parmeter location="+location);


        //先回退到版本old,再回退到上一个版本
//        ExecuteCommand(proj,"git reset --hard "+oldCom,"C:\\Users\\delll\\IdeaProjects\\DatasetCreate\\src\\main\\resources\\result.txt");
//        copyTo(location,output);
//        ExecuteCommand(projdir,"git reset --hard \"HEAD^\"","C:\\project\\Projects_50\\VersionDB\\raw_project\\"+proj+"\\"+proj+"_log.txt");
        ExecuteCommand(projdir,"git reset --hard \"HEAD^\"","C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+proj+"\\"+proj+"_log.txt");


        copyTo(location,output);

    }

    private static void generateNew(String location, String curCom,String fileName,String output) throws Exception {

        String[] data=location.split("\\\\");
//        for (String s:data){
//            System.out.println("generateNew======>"+s);
//
//        }

        //=======================================这里错了=======================================================================================================================================

//        String projdir=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8];
//        String proj=data[8];
//        System.out.println("new location="+proj);
//        System.out.println("parmeter location="+location);
        String projdir=data[0]+"\\\\"+data[2]+"\\\\"+data[4]+"\\\\"+data[6]+"\\\\"+data[8]+"\\\\"+data[10];
        String proj=data[10];
        System.out.println("new location="+proj);
        System.out.println("parmeter location="+location);

        //返回到记录的commitId就是当前版本（旧版本是上一个）
        //这里的logoutput随便找一个地方记录就可以（无用）
//        ExecuteCommand(projdir,"git reset --hard "+curCom,"C:\\project\\Projects_50\\VersionDB\\raw_project\\"+proj+"\\"+proj+"_log.txt");
        ExecuteCommand(projdir,"git reset --hard "+curCom,"C:\\project\\MethodPrediction_All_ID\\versionDB\\raw_project\\"+proj+"\\"+proj+"_log.txt");

        //from location(only file) copy to output
        copyTo(location,output);
    }

    private static void copyTo(String location, String output) throws IOException {
        Boolean validFile=match(output);
        //读取
        File in=new File(location);

        //写入
        File out=new File(output);
        if(in.exists() && !validFile) {

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
        boolean isBad=fileName.contains("<")||fileName.contains(">")||fileName.contains(".")||fileName.contains("\"")||fileName.contains("{")||fileName.contains("}")
                ||fileName.contains("(")||fileName.contains(")")||fileName.contains("<-<-");

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
