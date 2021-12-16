package compare;

import com.csvreader.CsvReader;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FindRes {
    static List<String>  fieldsName;
    static List<String> methodName;
    static List<String> variableName;
    static List<String> callSet;
    //获取declaration
    static Map<String, FieldDeclaration> fieldMap;
    static Map<String, MethodDeclaration> methodMap;
    static Map<String, VariableDeclarationExpr> variableMap;
    static Map<String, String> methodCallMap;
    static Map<String, String> callMap;
    static List<String> results;

    public static void main(String[] args) throws Exception {
        //single-process
//        create("axiom");
        //batch-process
        int start=0;
        int end=10;
        BufferedWriter bw=new BufferedWriter(new FileWriter("C:\\project\\Projects_50\\compare"+start+"_"+(end-1)+".csv"));
        String path = "C:\\project\\Projects_50\\Renaming\\method";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中

        for(int i=start;i<end;i++) {                    //遍历File[]数组

            Map<String,String> map=create(fs[i].getName().split("\\.")[0].split("_")[0]);
            String pre=map.get("precision");
            String rec=map.get("recall");
            String f1=map.get("f1");
            bw.write(fs[i].getName()+","+pre+","+rec+","+f1+"\n");
//            bw.write("【"+fs[i].getName()+"】\n"+"precision="+pre+"\nrecall="+rec+"\nf1="+f1+"\n");
//            System.out.println(fs[i].getName().split("\\.")[0].split("_")[0]);

        }
        bw.close();
    }

    public static Map<String,String> create(String proj) throws Exception {
        results=new ArrayList<>();
        List<String> rename_list=new ArrayList<>();
        List<String> oldname_list=new ArrayList<>();

        //只获取来自method的renaming
//        CsvReader br=new CsvReader("C:\\project\\IdentifierStyle\\log\\dump\\"+proj+"_method.csv");
//        CsvReader br=new CsvReader("C:\\project\\Projects_50\\Renaming\\"+proj+".csv");
        CsvReader br=new CsvReader("C:\\project\\Projects_50\\Renaming\\method\\"+proj+"_method.csv");
        String line=null;
       int num=0;
        while(br.readRecord()) {
            int index=0;
            line=br.getRawRecord();
            num++;
            String[] data = line.split(",");
            //获取change变化
            String[] changes = data[3].split("<-");

            //获取Id


            //获取文件名字
            String path = data[2].split("<=")[0].split("<-")[0];
            String filename = path.substring( path.lastIndexOf("\\")+1);


            for(int i=0;i<changes.length-1;i++) {
                String id = data[4+index];
                String oldName=changes[changes.length-1-i];

                String newName=changes[changes.length-2-i];



                String re=handle(oldName, newName, proj, id.trim(), filename.trim(), changes[i]+"_"+changes[i+1]+"_");

                if(re.length()>0) {

                    results.add(re);
                }

                if(!oldName.trim().equals(newName.trim())) {
                    if (newName.trim().length() > 1) {

                        rename_list.add(newName);
                    }
                    if (oldName.trim().length() > 1) {
                        rename_list.add(oldName);
                    }
                }
                oldname_list.add(oldName);

                index++;

            }


        }
//        while (br_method.readRecord()){
//            String[] data=br_method.get(3).split("<-");
//            for(String d:data) {
//                if(d.trim().length()>0) {
//                    rename_list.add(d);
//                }
//            }
//
//        }
        //评估测试结果
        System.out.println(results);
//        System.out.println(results.length);
        //rename_list包含了所有重命名标识符
        //TP:在rename_list,FP:不在

        br.close();



        int tp=0,fp=0,fn=0;
        for(String predict:results){
            if(predict.contains("=")){
                predict=predict.split("=")[0].substring(1).trim();

            }
            if(predict.contains("[")){
                predict=predict.substring(1,predict.length()-1);

            }
            if (rename_list.contains(predict)){
//                System.out.println("预测正确");
                tp++;
                System.out.println(predict);

            }else{
                //FP
//                System.out.println("预测错误");
                fp++;

            }
        }
        //FN 没有预测为renaming的在renaming list中
        Set<String> renameSet=new HashSet<>(rename_list);
        for(String name:renameSet){
            if(!results.contains(name.trim()) && oldname_list.contains(name)){
                fn++;
            }
        }
//        tp=1;
//        fp=0;
        System.out.println("tp="+tp+",fp="+fp+",fn="+fn);
        float precision=(float) tp/(tp+fp);
        float recall=(float) tp/(tp+fn);
        System.out.println("precision="+precision);
        System.out.println("recall="+recall);
        System.out.println("f1="+(2*precision*recall/(precision+recall)));
        System.out.println(results.size());
        System.out.println(proj);
        System.out.println(renameSet.size());
        System.out.println(rename_list.toString());
        System.out.println(results.toString());
        Map<String,String> map=new HashMap<>();
        map.put("precision",String.valueOf(precision));
        map.put("recall",String.valueOf(recall));
        map.put("f1",String.valueOf((2*precision*recall/(precision+recall))));
        return map;








        }
    public static String handle(String oldname,String newname,String proj,String id,String filename,String change) throws Exception {
        if(oldname.trim().length()==0 || newname.trim().length()==0 ) return "";
//        System.out.println("old_before="+oldname);
        //从Parser中获取变量名和方法名
        Map<String,List> map=new HashMap<>();
//        String file="C:\\project\\IdentifierStyle\\data\\VersionDB\\ver_old\\"+proj+"\\"+id+"_"+change+filename;

        String file="C:\\project\\Projects_50\\VersionDB\\raw_project\\GROUP_1\\"+proj+"\\"+proj+"_old\\"+id+"_"+change+filename;
        File f=new File(file);
        if(!f.exists()) {
            System.out.println(filename);
            return "";
        }
        try {
            map = JavaParserUtils.getData(file);

        } catch (Exception e) {
            System.out.println("parse error");
            return "";
        }
        fieldsName = map.get("fields_name");
        methodName = map.get("method_name");
        variableName = map.get("variable_name");
            callSet = map.get("call_relation");
            fieldMap = JavaParserUtils.fieldMap;
            methodMap = JavaParserUtils.methodMap;
            variableMap = JavaParserUtils.variableMap;
            callMap = JavaParserUtils.nameExprMap;
          methodCallMap=JavaParserUtils.methodCallMap;

//        classMap=getClassMap();



        //为了方便，先假设已经检测出更改的变量名为m_BaseInstance,寻找其他高相似度标识符

        String old="["+oldname+"]";
        String news="["+newname+"]";

        //SearchEnginne获取res集合（相关）
        String [] res=searchRes(old);
        System.out.println("res length="+res.length);
//
        //按照术语分割标识
        System.out.println("olaname="+oldname);
        String[] oldSet=split(old);
        String[] newSet=split(news);



        //RenamingAnalyzer
        // 1-generateChangeSet:得到重构活动的转化脚本
        Map<String,List<List<String>>> changeScript=generateChangeSet(oldSet, newSet);
        // 2-对于每一个resName，计算分割集合sps=<sp1,sp2..>--》根据sps分割--》计算相似度--》最大相似度推荐
        String re="";
        double maxSim=0;
        double sim=0;
//        System.out.println("changeScrfipt size="+changeScript.size());
        for(String resName:res) {
            //，计算分割集合sps=<sp1,sp2..>
            if (resName.contains("[")) resName = resName.substring(1, resName.length() - 1);


//            System.out.println("old="+old+",res="+resName);

            List<List<String>> sps = analysis(changeScript, oldSet, resName);
            //根据sps分割--》计算相似度
            if(old.trim().length()==0 || resName.length()==0) return "";
            sim = recommendator(split(old), sps, resName);
//            System.out.println("字符串相似度是" + sim);
//            System.out.println("--------------------------------------");
//            //--》最大相似度推荐
//
            if (maxSim <= sim) {
                re = resName;
                maxSim = sim;
            }
        }



//
        if(!methodName.contains(re.trim())) {
            return "";
        }

        return re;

        //Recommendation计算文本相似度，推荐重合度最高（先返回这个字符串）

        //System.out.println("对m_BaseInstance进行删除操作后应该修改的标识符为----》"+re);

    }


    private static double recommendator(String old[], List<List<String>> sps, String resName) {

        /*
        1.先按照sps的方法分割old字符串old为oldSub和res中的字符串resSub[i]
        2.计算oldSub和resSub[i]的相似度sim
        3.返回最高相似度的resSub
         */





//        double maxSim=0;
//        int index=0;



        //根据sps分割子串

            //【方法一】论文中的方法
            //获取对比的两个字符串的子序列()
            //计算相似度

        //double sim = CalSim(SplitwithSps(old, false, sps), SplitwithSps(split("[" + resName + "]"), true, sps));



           double maxsim=0;
            for(List<String> spi:sps) {
                double sim = CalSim(SplitwithSps(old, false, spi), SplitwithSps(split("[" + resName + "]"), true, spi));
                System.out.println("sim="+sim);
                if(sim>maxsim){
                    maxsim=sim;
                }
            }

            return maxsim;

            //return sim;
           // System.out.println(res[i]+"与m_BaseInstance的相似度为:"+sim);

//
//            if(sim>maxSim){
//                index=i;
//            }
            //【方法二】直接计算相似度

       // return res[index];

    }

    private static double CalSim(List<String> o, List<String> c) {
        double sum=0;
        int os=o.size();
        int cs=c.size();
        int k=0;
        if(o.size()!=c.size()){
            k=os-cs;
            if(k>0){
                int i1=0;
                while(i1<k) {
                    c.add("");
                    i1++;
                }
            }else{
                int i2=0;
                while(i2<k) {
                    o.add("");
                    i2++;
                }
            }

        }
        System.out.println("oldStr="+o.toString());
        System.out.println("resStr="+c.toString());
        for(int i=0;i<o.size();i++) {
            System.out.println("oldStr="+o.get(i));
            System.out.println("resStr="+c.get(i));
            double dis=0;
            if(o.get(i).trim().length()==0 && c.get(i).trim().length()==0){
                dis=1;
            }else {
                dis = StringUtils.getJaroWinklerDistance(o.get(i).toLowerCase(), c.get(i).toLowerCase());
            }
            //double dis=StringUtils.getLevenshteinDistance(o.get(i).toLowerCase(), c.get(i).toLowerCase());
//            double dis=CosineSimilarity.getSimilarity(o.get(i).toLowerCase(), c.get(i).toLowerCase());
//            double dis=similarScoreCos(o.get(i).toLowerCase(),c.get(i).toLowerCase());
            sum+=dis;
            System.out.println(dis);

        }


        return sum/(o.size());

    }
    public static double similarScoreCos(String text1, String text2){
        if(text1 == null || text2 == null){
            //只要有一个文本为null，规定相似度分值为0，表示完全不相等
            return 0.0;
        }else if("".equals(text1)&&"".equals(text2)) return 1.0;
        Set<Integer> ASII=new TreeSet<>();
        Map<Integer, Integer> text1Map=new HashMap<>();
        Map<Integer, Integer> text2Map=new HashMap<>();
        for(int i=0;i<text1.length();i++){
            Integer temp1=new Integer(text1.charAt(i));
            if(text1Map.get(temp1)==null) text1Map.put(temp1,1);
            else text1Map.put(temp1,text1Map.get(temp1)+1);
            ASII.add(temp1);
        }
        for(int j=0;j<text2.length();j++){
            Integer temp2=new Integer(text2.charAt(j));
            if(text2Map.get(temp2)==null) text2Map.put(temp2,1);
            else text2Map.put(temp2,text2Map.get(temp2)+1);
            ASII.add(temp2);
        }
        double xy=0.0;
        double x=0.0;
        double y=0.0;
        //计算
        for (Integer it : ASII) {
            Integer t1=text1Map.get(it)==null?0:text1Map.get(it);
            Integer t2=text2Map.get(it)==null?0:text2Map.get(it);
            xy+=t1*t2;
            x+=Math.pow(t1, 2);
            y+=Math.pow(t2, 2);
        }
        if(x==0.0||y==0.0) return 0.0;
        return xy/Math.sqrt(x*y);
    }


    //根据sps集合分别对oldSet(术语集合)和cSet(术语集合)进行分割,返回子序列
    //暂时只考虑删除操作
    /*
    isC表示是否为resName
     */
    private static List<String> SplitwithSps (String[]set,boolean isC, List<String> spi) {
        //spi=<pi,qi,len>
        //理解为重构活动为删除的Id分割点为pi
        //相关实体的分割点为qi

        //1.Sub存储分割后的序列
        List<String> Sub=new ArrayList<>();
        //分割为 a[pi-1]~a[pi]的多段序列，存储进入Sub
        int pre_p=0,p=0;


            if(isC){
                //是否为候选名字
                p=Integer.parseInt(spi.get(1));//3
            }else{
                p=Integer.parseInt(spi.get(0));//2
            }

            StringBuffer ele= new StringBuffer();
//            if(p==0) {
//                ele.append(set[0]);
//            }
            if(p<set.length ) {
                for (int i = pre_p; i <=p; i++) {
                    ele.append(set[i]);
                }
                pre_p = p + 1;
                Sub.add(ele.toString());

            }


            //最后剩下的为end
            if(p<set.length ){
                StringBuffer end=new StringBuffer();
                for(int j=p+1;j<set.length;j++){
                    end.append(set[j]);
                }
                System.out.println("end="+end);
                Sub.add(end.toString());
            }
            System.out.println(Arrays.asList(set)+"分割为："+Sub);

//        for(String s:Sub){
//            System.out.println(s);
//        }
        return Sub;


    }
//    private static List<String> SplitwithSps(String[]set,boolean isC, List<List<String>> sps) {
//        //spi=<pi,qi,len>
//        //理解为重构活动为删除的Id分割点为pi
//        //相关实体的分割点为qi
//
//        //1.Sub存储分割后的序列
//        List<String> Sub=new ArrayList<>();
//        //分割为 a[pi-1]~a[pi]的多段序列，存储进入Sub
//        int pre_p=1,p=0;
//
//        for(List<String> sp:sps){
//            if(isC){
//                //是否为候选名字
//                p=Integer.parseInt(sp.get(1));//3
//            }else{
//                p=Integer.parseInt(sp.get(0));//2
//            }
//
//            StringBuffer ele= new StringBuffer();
//            ele.append(set[0]);
//            if(p<set.length ) {
//                for (int i = pre_p; i <=p; i++) {
//                    ele.append(set[i]);
//                }
//                pre_p = p + 1;
//                Sub.add(ele.toString());
//                System.out.println("ele="+ele);
//            }
//
//
//        //最后剩下的为end
//        if(p<set.length){
//            StringBuffer end=new StringBuffer();
//            for(int j=p+1;j<set.length;j++){
//                end.append(set[j]);
//            }
//            System.out.println("end="+end);
//            Sub.add(end.toString());
//        }
//        System.out.println(Arrays.asList(set)+"分割为："+Sub);
//        }
////        for(String s:Sub){
////            System.out.println(s);
////        }
//        return Sub;
//
//
//    }
    public static  Map<String,List<List<String>>> generateChangeSet(String[] s1,String[] s2){
        List<String> oldN=new ArrayList<>(Arrays.asList(s1));
        List<String> newN=new ArrayList<>(Arrays.asList(s2));
        //存储三种类型的集合：删除、插入和替换操作
        Map<String,List<List<String>>> map=new HashMap<>();

//        //------------------检测是否为删除操作--------------------
//        /*思路：旧str中有，新的无则删除了*/
//        //key="DELETION";ai~ak 从0开始
//        //1.删除集合di
//        List<List<String>> d=new ArrayList<>();
//        int i=-1,k=-1;
//        for(int j=0;j<oldN.size();j++){
//            //2.如果新串中不包含术语j，则更新为aj-aj
//            if(!newN.contains(s1[j])){
//                if(i==-1){
//                    i=j;
//                    k=j;
//
//
//                }else if(j==i+1){
//                    //3.如果遍历的连续的不包含术语j，则更新k值
//                    k=j;
//
//                }
//                //4.边界处理
//                if(j==s1.length-1){
//                    d.add(new ArrayList<>(Arrays.asList(new String[]{String.valueOf(i), String.valueOf(k)})));
//                    i = -1;
//                    k = -1;
//
//                }
//
//            }else{
//                if (i != -1 && j != -1) {
//                    d.add(new ArrayList<>(Arrays.asList(new String[]{String.valueOf(i), String.valueOf(k)})));
//                    i = -1;
//                    k = -1;
//
//                }
//            }
//        }
//        //5.把所有的删除集合放入map
//        map.put("DELETION",d);


        //检测时增加操作
        /*同理，思路是newN有，oldN无则为插入*/
        //插入集合


//        for(int j=0;j<newN.size();j++){
//            //oldName无则为插入术语
//            if(!oldN.contains(s2[j])){
//                if(j==0){
//                    //开头，后一个是oldName的下一个则是update（只考虑update一个词？？）
//
//
//                }else if(j==newN.size()-1){
//                    //结尾
//                }
//
//
//            }
//        }
//


        //检测替换操作
//        System.out.println("changeScrip"+oldN+","+newN);
        List<List<String>> rps=new ArrayList<>();
        List<String> rp=new ArrayList<>();
        if(oldN.size()== newN.size()) {
            //有可能存在替换操作
            //首先考虑仅替换一个词的情况
            int start = -1;
            int end = -1;
            for (int i = 0; i < oldN.size(); i++) {
                String n = newN.get(i);
                if (!oldN.get(i).equals(n) && i < oldN.size() - 1 && oldN.get(i + 1).equals(n)) {
                    start = i;

                } else if (!oldN.get(i).equals(n) && i == oldN.size() - 1) {
                    end = i;
                    if (start == -1) start = i;
                    rp.add(String.valueOf(start));
                    rp.add(String.valueOf(end));
                    rps.add(rp);

                } else if (!oldN.get(i).equals(n) && i < oldN.size() - 1 && (!oldN.get(i + 1).equals(n))) {
                    //后续字母不同
                    end = i;
                    if (start == -1) start = i;
                    rp.add(String.valueOf(start));
                    rp.add(String.valueOf(end));
                    rps.add(rp);

                }
            }


            map.put("REPLACE", rps);
//            return map;

        }



        //工具获取del和add集合
        List<List<String>> sc=new ArrayList<>();
        Diff diff = new Diff(oldN, newN);
        List diffOut = diff.diff();
        //Difference h = ((Difference)diffOut.get(0));

        //获取所有修改集合
        List<List<String>> d=new ArrayList<>();
        List<List<String>> ins=new ArrayList<>();
        for(int m=0;m<diffOut.size();m++) {
            Difference h=(Difference)diffOut.get(m);
            //for del
            int i=h.getDeletedStart();
            int k=h.getDeletedEnd();
            if(i!=-1 && k!=-1){
                d.add(new ArrayList<>(Arrays.asList(new String[]{String.valueOf(i), String.valueOf(k)})));
            }
            //for add
            i=h.getAddedStart();
            k=h.getAddedEnd();
            if(i!=-1 && k!=-1){
               ins.add(new ArrayList<>(Arrays.asList(new String[]{String.valueOf(i), String.valueOf(k)})));
            }



        }
        map.put("DELETION",d);
        map.put("ADD",ins);



       //测试

        //System.out.println(list.size());
        System.out.println("------------------------------------------------------------------");
        System.out.println(oldN+"转化到"+newN+"的转化脚本：");
        //打印删除集合
        for (String key:map.keySet()){
            for(List<String> ds:map.get(key)){
                System.out.println(key+"=<"+ds.get(0)+','+ds.get(1)+">");
            }
            System.out.println();
        }
//        List<List<String>> list=map.get("DELETION");
//        for(List<String> ds:list){
//            System.out.println("d=<"+ds.get(0)+','+ds.get(1)+">");
//        }
//        //打印插入集合
//        List<List<String>> list2=map.get("ADD");
//        for(List<String> ds:list2){
//            System.out.println("add=<"+ds.get(0)+','+ds.get(1)+">");
//        }
//        //打印替换集合
//        List<List<String>> list3=map.get("REPLACE");
//        if(list3.size()>0) {
//            for (List<String> ds : list3) {
//                System.out.println("replace=<" + ds.get(0) + ',' + ds.get(1) + ">");
//            }
//        }

        return map;



    }


     //得到将oldName转换为newName的最小的删除、插入和替换集合。
    public static List<List<String>> analysis( Map<String,List<List<String>>>  changeScript,String[] oldName,String resName){

        List<List<String>> sps=new ArrayList<>();
        //如果是替换操作做，rp=<k>，分为前面的和后面的 sp《i。k,len>
       for(String key:changeScript.keySet()) {
           if (key == "REPLACE") {
               List<List<String>> rpS = changeScript.get("REPLACE");
               List<String> elrp = new ArrayList<>();
               if (rpS.size() > 0) {
                   List<String> rp = rpS.get(0);

                   //rp=<i,k>
                   int i_rp = Integer.parseInt(rp.get(0));
                   int k_rp = Integer.parseInt(rp.get(1));
                   int len_rp = (i_rp == k_rp) ? 1 : k_rp - i_rp;
                   elrp.add(rp.get(0));
                   elrp.add(rp.get(1));
                   elrp.add(String.valueOf(len_rp));
                   sps.add(elrp);
               }
           }
       }

//            return sps;









//        for(String key:changeScript.keySet()) {
            //如果是删除操作 d<i,k>---->sp<p,q,len>
//            if(key=="DELETION") {
                List<List<String>> delS=changeScript.get("DELETION");
                //计算res集合中: <一个resName,oldNAME>的sps集合

                for(List<String> d:delS) {
                    //生成分割点sp
                    //1.p=分割点k
                    int p = Integer.parseInt(d.get(1));
                    //2.q是相同字符串的下标（例如，删除base，在res【i】中找到base的下标）
                    List<String> resS = new ArrayList<>(Arrays.asList(split("["+resName+"]")));
                    int q = resS.indexOf(oldName[p]);
                    //3.长度len，就是删除了几个术语
                    int len = (Integer.parseInt(d.get(1)) - Integer.parseInt(d.get(0))) + 1;
                    //System.out.println("sp1={"+p+" "+q+" "+len+"}");
                    sps.add(new ArrayList<>(Arrays.asList(String.valueOf(p),String.valueOf(q),String.valueOf(len))));

                }
                    System.out.println("------------------------------------------------------------------");
                    System.out.println("根据转化脚本与"+resName+"生成的对应分割集合sps为：");
//                    for (List<String> spi:sps){
//                        System.out.println("spi_del={"+spi.get(0)+" "+spi.get(1)+" "+spi.get(2)+"}");
//                    }
                //System.out.println("------------------------------------------------------------------");




//            }


            //如果是add操作 d<i>---->sp<I,P,0>
//            else if(key=="ADD"){
                List<List<String>> addS=changeScript.get("ADD");
                //计算res集合中: <一个resName,oldNAME>的sps集合

                for(List<String> a:addS) {
                    //生成分割点sp
                    //1.p=分割点k
                    int i = Integer.parseInt(a.get(0));
                    //2.p是ai字符串后一个字符串的下标（例如，删除base，在res【i】中找到base的下标）

                    List<String> resS = new ArrayList<>(Arrays.asList(split("["+resName+"]")));
                    int p=0;
                    if(i>=(oldName.length-1)){
                        p=resS.size()-1;
                    }else {
                         p = resS.indexOf(oldName[i + 1]);
                    }
                    //3.长度len，就是删除了几个术语

                    //System.out.println("sp1={"+p+" "+q+" "+len+"}");
                    sps.add(new ArrayList<>(Arrays.asList(String.valueOf(i),String.valueOf(p),String.valueOf(0))));



                }



//            }
            //如果是删除操作 d<i,k>---->sp<p,q,len>
//            else if(key=="REPLACE"){
//
//            }

//        }
        for (List<String> spi:sps){
            System.out.println("spi={"+spi.get(0)+" "+spi.get(1)+" "+spi.get(2)+"}");
        }
       // System.out.println("------------------------------------------------------------------");
        return sps;

    }

    //method 这个map 对应的List<Map<merhodName,List<String>>={<methodName1,List<localVariable>,<methName2,List<localVariable>}},methodName被单独取出。
    //”。它收集密切相关的软件实体(访问该字段的方法)
    private static String[] searchRes(String old) {
        System.out.println("ent===="+old);
// 1）Inclusion：包含e直接包含的实体和直接包含e的元素。
//2）Sibling：e是一个方法，同一个类中的所有方法和字段都被认为是紧密相关的实体
//3）Reference：e所引用的所有实体和引用e的实体
//4)   Inheritance：e是一个类，则其超类和子类


        Set<String> set=new HashSet<>();
        //同级的
        for (String method:methodName){
            //非空
            if(!method.equals("")&&! method.equals(old.substring(1,old.length()-1))){

                set.add(method);
            }else{
                System.out.println("same="+method+","+old);
            }
        }
//        for (String field:fieldsName){
//            //非空
//            if(!field.equals("")&&! field.equals(old.substring(1,old.length()-1))){
//
//                set.add(field);
//            }else{
//
//            }
//        }
//        for (String field:fieldsName){
//            int eq=field.indexOf("=");
//            if (eq!=-1) {
//                field = field.substring(1, eq);
//            }else{
//                field=field.substring(1,field.length()-1);
//            }
//            if(!field.equals("")&&! field.equals(old.substring(1,old.length()-1))){
//                set.add(field);
//                System.out.println("fileld="+field);
//            }
//        }
//
//        for (String var:variableName){
//            if(!var.equals("")&&! var.equals(old.substring(0,old.length()-1))){
//                set.add(var);
//                System.out.println("varble="+var);
//            }
//        }


        //-------------------------------方法二-----------------------------
        //1.直接包含的实体--
            //1.1如果是函数,检测调用该函数的软件实体
        //父类
        if(methodName.contains(old.substring(1,old.length()-1))){
            System.out.println(methodMap.size());
                    MethodDeclaration m=methodMap.get(old.substring(1,old.length()-1));
                    if(m!=null) {
                        String[] data = JavaParserUtils.getParents(m).split("\\.");
                        for (String s : data) {
                            if (!s.equals("")) {
                                set.add(s);
                            }


                        }
                        //把methodname全部放进去

                    }



                }
//        子类

       for(String nameExpr:callMap.keySet()) {

            //得nameexpr父类
            String[] pStr3=callMap.get(nameExpr).split("\\.");

            if(pStr3[pStr3.length-1].equals(old.substring(0,old.length()-1))){

                    set.add(nameExpr);

            }

        }
        for(String callExpr:methodCallMap.keySet()) {
            //得nameexpr父类
            String[] pStr4=methodCallMap.get(callExpr).split("@@@@");
            Set<String> parents4 = new HashSet<String>(Arrays.asList(pStr4));
            for(String parent:parents4){
                String[] data=parent.split("\\.");
                if(data[data.length-1].trim().equals(old.substring(1,old.length()-1))){
                    set.add(callExpr);
                }
            }

        }





/*NameExpr:m_BaseInstance
MethodCallExpr:setBaseInstances*/
        //2.sibling 类Srccode的所有字段和方法，父类的所有子元素

        //3.直接访问这个字段的方法

        //先生成数据
        String[] res = set.toArray(new String[set.size()]);
        //String[] res_={ "[m_BaseInstPanelCase]", "[setBaseInstanceFromFileQCase]","[setBaseInstancesFromDBQCase]"};
        return res;
    }


     /*IdName被分解为由下划线和大写字母分隔的术语序列，假设名称遵循流行的驼峰式或蛇形式命名约定。
        分解不遵循这些约定的标识符名称的替代方法。
     */
    private static String[] split(String old) {
        /*IdName被分解为由下划线和大写字母分隔的术语序列，假设名称遵循流行的驼峰式或蛇形式命名约定。
        分解不遵循这些约定的标识符名称的替代方法。
         */
        //1.先处理变量名
        String id=old.substring(1,old.length()-1);

        //根据下划线和大写字母分割
        /*正则表达式：句子结束符*/
        String regEx="(?=[_|[A-Z]])";
        Pattern p =Pattern.compile(regEx);
        Matcher m = p.matcher(id);

        /*按照句子结束符分割句子*/
        String[] fieldSeq = p.split(id);

        return fieldSeq;

    }
}
