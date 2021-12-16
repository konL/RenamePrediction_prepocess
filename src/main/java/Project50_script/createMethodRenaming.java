package Project50_script;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class createMethodRenaming {
    public static void main(String[] args) throws IOException {
        //循环遍历
        //遍历文件夹
        String path = "C:\\project\\Projects_50\\Renaming";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        for(int i=0;i<10;i++){					//遍历File[]数组,十个一组
            if(fs[i].getName().endsWith("csv"))		//若非目录(即文件)，输出method pair
                create(fs[i].getName().split("\\.")[0]);
        }

    }
    public static void create(String proj) throws IOException {

        //变化标识符数据源
        String dataSource = "C:\\project\\Projects_50\\Renaming\\" + proj + ".csv";
        String outputFile = "C:\\project\\Projects_50\\Renaming\\method\\" + proj + "_method.csv";
        //1.获取操作数据源中的标识符部分
        // 创建CSV读对象
        CsvWriter csvWriter = new CsvWriter(outputFile);

        //读取dump
        CsvReader stmtReader = new CsvReader(dataSource);
        while (stmtReader.readRecord()) {

            int typeId = Integer.parseInt(stmtReader.get(0));
            if(typeId==3){
                String[] opdata = stmtReader.getRawRecord().split(",");
                csvWriter.writeRecord(opdata);
            }


//                //分类为函数,则写入到新文件
//                csvWriter.write("method");
//                csvWriter.writeRecord(opdata);
//                System.out.println("method------->" + stmt);





        }
        csvWriter.close();
        stmtReader.close();

    }
}