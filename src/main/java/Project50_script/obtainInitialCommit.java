package Project50_script;

import detectId.Trace.SyncPipe;

import java.io.*;

public class obtainInitialCommit {
    public static void main(String[] args) throws Exception {
        //获取git Project下所有文件夹
        String path = "C:\\project\\Projects_50\\Java";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        BufferedWriter bw=new BufferedWriter(new FileWriter("C:\\project\\Projects_50\\Java\\commits.txt"));
        for(int i=0;i<fs.length;i++) {                    //遍历File[]数组

           String initialCommit=fs[i].getName()+":"+ExecuteCommand(fs[i].getAbsolutePath(),"git rev-parse HEAD","C:\\project\\Projects_50\\Java\\log.txt");
           bw.write(initialCommit+"\n");

        }
        bw.close();
    }
    public static String ExecuteCommand(String projectdir,String cmd,String output) throws Exception
    {
        //进入该项目
        //获取最新的commit
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
        String final_com="";

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