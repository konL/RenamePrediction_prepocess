package extend_impl;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import detectId.DS.ClassDS;
import detectId.DS.IdentifierDS;
import detectId.DS.MethodDS;
import detectId.ParseInfo.ClassCollector;
import detectId.ParseInfo.VariableCollector;


import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class createMergeFile {
    static List<String> fieldsName;
    static List<String> methodName;
    static List<String> variableName;
    static List<String> callSet;
    //获取declaration
    static Map<String, FieldDeclaration> fieldMap;
    static Map<String, MethodDeclaration> methodMap;
    static Map<String, VariableDeclarationExpr> variableMap;
    static Map<String, String> callMap;
    static List<String> results;
    static Map<String, String> classMap;
    static String[] type = {"package", "type", "method", "field", "variable"};
   static List<String> changeIdlist;

    public static void main(String[] args) throws Exception {
        String proj = "beam";
        File dir = new File("C:\\project\\IdentifierStyle\\data\\VersionDB\\raw_project_extend\\" + proj + "\\");
        //记录所有重命名Id
        Set<String> renamingId = new HashSet<String>();
        List<String> fileSet=new LinkedList<>();
        changeIdlist=new ArrayList<>();
        //写入文件
        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\project\\IdentifierStyle\\data\\VersionDB\\raw_data_extend\\context\\" + proj + "_merge_method.csv", true));
        CsvWriter writer = new CsvWriter(bw, ',');
        //=============================从所有版本库中获取重命名标识符的前后实现

        //① 读取记录了改名的csv文件
        CsvReader read1 = new CsvReader("C:\\project\\IdentifierStyle\\log\\dump\\" + proj + ".csv");
        System.out.println("===========① reading renaming name=============");

        while (read1.readRecord()) {
            String[] changeIdSet = read1.get(3).split("<-");
            if (changeIdSet.length == 0) continue;
            changeIdlist.add(read1.get(3));
            for (int i = 1; i < changeIdSet.length; i++) {
                renamingId.add(changeIdSet[i]);
            }
        }
        System.out.println("renaming id="+renamingId.toString());
        System.out.println("renaming size="+renamingId.size());
//        System.out.println("renaming id="+renamingId.toString());
        CsvReader read = new CsvReader("C:\\project\\IdentifierStyle\\log\\dump\\" + proj + ".csv");
        System.out.println("===========① reading renaming csv file=============");
        while (read.readRecord()) {

            if(Integer.parseInt(read.get(0))!=3) continue;
            String[] changeIdSet = read.get(3).split("<-");
//            System.out.println("origin:"+read.get(3));
            if (changeIdSet.length == 0) continue;
            String[] t = read.get(2).split("<=")[0].split("<-")[0].split("\\\\");
            String filename = t[t.length - 1].trim();
            for(int i=1;i<changeIdSet.length;i++) {
                renamingId.add(changeIdSet[i]);
                String changeId=changeIdSet[i-1]+"_"+changeIdSet[i];
                String changeId_ori = changeIdSet[i-1]+"."+changeIdSet[i];;
                String id = read.get(4+(i-1)).trim();

                //获得对应的java文件，根据changeId生成oldCContext，newContext
                String file_old = "C:\\project\\IdentifierStyle\\data\\VersionDB\\raw_project_extend\\" + proj + "\\"+proj+"\\" + proj + "_old\\" + id + "_" + changeId + "_" + filename;
                String file_new = "C:\\project\\IdentifierStyle\\data\\VersionDB\\raw_project_extend\\" + proj + "\\"+proj+"\\" + proj + "_new\\" + id + "_" + changeId + "_" + filename;


                List<String> oldInfo = new ArrayList<>();
                List<String> newInfo = new ArrayList<>();

                //获得对应的java文件，生成oldCContext，newContext（重命名版本的）
                oldInfo = create("old", changeId_ori, file_old);
                newInfo = create("new", changeId_ori, file_new);
                System.out.println("oldsize="+oldInfo.size());
                System.out.println("newsize="+newInfo.size());
                if(oldInfo.size()==0 || newInfo.size()==0) continue;
                try{
                    String oldedge=oldInfo.get(4);
                    String newedge=newInfo.get(4);
//                  System.out.println("typeId="+Integer.parseInt(oldInfo.get(0)));
                    writer.writeRecord(new String[]{"1", type[Integer.parseInt(oldInfo.get(0))-1], oldInfo.get(2), newInfo.get(2), oldInfo.get(3), newInfo.get(3),oldedge,newedge});
                }catch (Exception e){
                    continue;
                }

                String filedone=id+"_"+filename;

                if(fileSet.contains(filedone)){
                    System.out.println("repeat!!!!");
                    continue;
                }
                fileSet.add(filedone);

                //获取该文件的代码，义查找方法以外的标识符
                File f= new File(file_new);
                BufferedReader br = new BufferedReader(new FileReader(f));
                Vector<String> allcode = new Vector<String>();
                String one = "";
                while ((one = br.readLine()) != null) {
                    allcode.add(one);
                }
                br.close();
                //3. 获取文件中的所有标识符
                Vector<iden> alliden = ObtainIdentifier(allcode, file_new);
//                System.out.println("allid="+alliden.size());
                for(iden identifier:alliden){
                    if(identifier.getType()!=3) continue;
                    List<String> unchange_old;
                    List<String> unchange_new;
                    String oneid=identifier.getIdentifier();
                    if(!renamingId.contains(oneid.trim())&&oneid.trim().length()>0){
                        unchange_old=create("old",oneid+"."+oneid,file_old);
                        unchange_new=create("new",oneid+"."+oneid,file_new);
                        if(unchange_old.size()==0 || unchange_new.size()==0) continue;
                        try{
                            String oldedge=oldInfo.get(4);
                            String newedge=newInfo.get(4);
//                  System.out.println("typeId="+Integer.parseInt(oldInfo.get(0)));
                            writer.writeRecord(new String[]{"0", type[Integer.parseInt(unchange_old.get(0))-1], unchange_old.get(2), unchange_new.get(2), unchange_old.get(3), unchange_new.get(3),oldedge,newedge});
                        }catch (Exception e){
                            continue;
                        }

                    }
                }



            }


        }

        //解析与重命名标识符处于同一个文件的未重命名标识符的历史实现（也许有修改也许无修改），这里的思路是程序员的一次重构活动，某一次他会修改一个需要重命名的方法和一个不需要重命名的方法，而不是我们去考虑方法整个历史中的变化情况
        //只需要关注”某一次“，以提交历史为依据
//        System.out.println(renamingId.toString());
//        System.out.println(fileSet.toString());
        System.out.println("项目"+proj+"的上下文文件（context+change entity）已生成");





//        compare
        read.close();
        writer.close();

    }

    private static List<String> create(String ver, String changeId, String loc) throws Exception {


//        System.out.println("ver="+ver);
//        System.out.println("changeid="+changeId);
        System.out.println("loc="+loc);
        List<String> info = new ArrayList<>();
        String id = "";
        if (ver.equals("old")) {
            id = changeId.split("\\.")[1];
        } else {
            id = changeId.split("\\.")[0];
        }
        List<String> methodName = new ArrayList<String>();
        List<String> methodBody = new ArrayList<String>();

        File f = new File(loc);
        if (!f.exists()) {
            System.out.println("【not exist!】"+loc);
            return info;
        }

        Map<String, List> map = new HashMap<>();
        try {
            map = JavaParserUtils.getData(loc);
            fieldMap = JavaParserUtils.fieldMap;
            callSet = map.get("call_relation");
            methodMap = JavaParserUtils.methodMap;
            variableMap = JavaParserUtils.variableMap;
            callMap = JavaParserUtils.nameExprMap;
            fieldsName=map.get("fields_name");
            methodName = map.get("method_name");
            methodBody = map.get("method_body");
            classMap=JavaParserUtils.classMap;
//
        } catch (Exception e) {
            System.out.println("JAVAPARSER ERR!!!!");
            return info;
        }

        //fieldMap处理



        String context = "";
        StringBuffer edges = new StringBuffer();

        //2. 获取该文件的代码，义查找方法以外的标识符
        BufferedReader br = new BufferedReader(new FileReader(f));
        Vector<String> allcode = new Vector<String>();
        String one = "";
        while ((one = br.readLine()) != null) {
            allcode.add(one);
        }
        br.close();
        //3. 获取文件中的所有标识符
        Vector<iden> alliden = ObtainIdentifier(allcode, loc);
//        System.out.println(alliden.toString());
        System.out.println("file" + ver + ":"+loc );
//        System.out.println("field="+fieldMap.keySet().toString());
//        System.out.println("field name="+fieldsName.toString());
//        System.out.println("classMap="+classMap.keySet().toString());
//        System.out.println("callSet="+callSet.toString());
//        System.out.println("varible="+variableMap.keySet().toString());
//        System.out.println("all id="+showAllId(alliden).toString());
        System.out.println("【curr id】="+id.trim());


        int typeId=0;
        //①使用javaParser获取函数的上下文
        if (methodName.contains(id.trim())) {
            int index = methodName.indexOf(id);
            context = methodBody.get(index);
            iden method=getIdenInfo(id,alliden);
            if(method==null) return info;
            typeId=method.getType();
            System.out.println("methods contains " + id);
            System.out.println("context : " + context);


            //写入文件
//            if (ver.equals("old")) {
                //查找相关实体，获取相关网络
                try {
                    relateEmbedding re = new relateEmbedding(id.trim(),map);
//                    List[] network = re.searchRes(id.trim());
                    List[] network = re.searchRes(id.trim(),changeIdlist);

                    List<String> node = network[0];
                    List<List> edge = network[1];

//                System.out.print("node=");
//                for (String n : node) {
//                    System.out.print(n + " ");
//
//                }
                System.out.println();
                System.out.print("edge=");
                for (List<String> e : edge) {
                    System.out.print("<" + e.get(0) + "," + e.get(1) + ">");
                    edges.append("<" + e.get(0) + "," + e.get(1) + ">" + "|");
                }
                }catch (NullPointerException e){
                    System.out.println("ERR ID=="+id);
                }
                System.out.println();

//            }
        }


        if(fieldMap.containsKey(id)){
            System.out.println("fields contains :" +id);
            iden field=getIdenInfo(id,alliden);
            if(field==null) return info;
            typeId=field.getType();
            context=field.getStatement();
            System.out.println("context : " + field.getStatement());
            //写入文件
//            if (ver.equals("old")) {
                //查找相关实体，获取相关网络
                try {
                    relateEmbedding re = new relateEmbedding(id.trim(),map);
//                    List[] network = re.searchRes(id.trim());
                    List[] network = re.searchRes(id.trim(),changeIdlist);

                    List<String> node = network[0];
                    List<List> edge = network[1];

//                System.out.print("node=");
//                for (String n : node) {
//                    System.out.print(n + " ");
//
//                }
                    System.out.println();
                    System.out.print("edge=");
                    for (List<String> e : edge) {
                        System.out.print("<" + e.get(0) + "," + e.get(1) + ">");
                        edges.append("<" + e.get(0) + "," + e.get(1) + ">" + "|");
                    }
                }catch (NullPointerException e){
                    System.out.println("ERR ID=="+id);
                }
                System.out.println();

            }


//        }
        if(variableMap.containsKey(id.trim())){

                System.out.println("variable contains :" +id);
                iden var=getIdenInfo(id,alliden);
                if(var==null) return info;
                typeId=var.getType();
                context=var.getStatement();
                System.out.println("context : " + var.getStatement());
            //写入文件
//            if (ver.equals("old")) {
                //查找相关实体，获取相关网络
                try {
                    relateEmbedding re = new relateEmbedding(id.trim(),map);
//                    List[] network = re.searchRes(id.trim());
                    List[] network = re.searchRes(id.trim(),changeIdlist);

                    List<String> node = network[0];
                    List<List> edge = network[1];

//                System.out.print("node=");
//                for (String n : node) {
//                    System.out.print(n + " ");
//
//                }
                    System.out.println();
                    System.out.print("edge=");
                    for (List<String> e : edge) {
                        System.out.print("<" + e.get(0) + "," + e.get(1) + ">");
                        edges.append("<" + e.get(0) + "," + e.get(1) + ">" + "|");
                    }
                }catch (NullPointerException e){
                    System.out.println("ERR ID=="+id);
                }
                System.out.println();

            }



//        }
        //type：类，接口
        if(classMap.containsKey(id.trim())){
            System.out.println("type contains :" +id);
            iden c_type=getIdenInfo(id,alliden);
            if(c_type==null) return info;
            typeId=c_type.getType();
            context=c_type.getStatement();
            System.out.println("context : " + c_type.getStatement());
//            if (ver.equals("old")) {
                //查找相关实体，获取相关网络
                try {
                    relateEmbedding re = new relateEmbedding(id.trim(),map);
                    List[] network = re.searchRes(id.trim(),changeIdlist);

                    List<String> node = network[0];
                    List<List> edge = network[1];

//                System.out.print("node=");
//                for (String n : node) {
//                    System.out.print(n + " ");
//
//                }
                    System.out.println();
                    System.out.print("edge=");
                    for (List<String> e : edge) {
                        System.out.print("<" + e.get(0) + "," + e.get(1) + ">");
                        edges.append("<" + e.get(0) + "," + e.get(1) + ">" + "|");
                    }
                }catch (NullPointerException e){
                    System.out.println("ERR ID=="+id);
                }
                System.out.println();

            }

//        }

        //参数？不加入
        if(callSet.contains(id.trim()) && !variableMap.containsKey(id.trim()) && !fieldMap.containsKey(id.trim())){
            return info;
//            System.out.println("other contains :" +id);
//            iden other=getIdenInfo(id,alliden);
//            if(other==null) return info;
//            typeId=other.getType();
//            context=other.getStatement();
//            System.out.println("context : " + other.getStatement());
//            if (ver.equals("old")) {
//                //查找相关实体，获取相关网络
//                try {
//                    relateEmbedding re = new relateEmbedding(id.trim(),map);
//                    List[] network = re.searchRes(id.trim());
//
//                    List<String> node = network[0];
//                    List<List> edge = network[1];
//
////                System.out.print("node=");
////                for (String n : node) {
////                    System.out.print(n + " ");
////
////                }
//                    System.out.println();
//                    System.out.print("edge=");
//                    for (List<String> e : edge) {
//                        System.out.print("<" + e.get(0) + "," + e.get(1) + ">");
//                        edges.append("<" + e.get(0) + "," + e.get(1) + ">" + "|");
//                    }
//                }catch (NullPointerException e){
//                    System.out.println("ERR ID=="+id);
//                }
//                System.out.println();
//
//            }

        }
//        if(ver.equals("old")) {
            String[] csvContent = {String.valueOf(typeId),loc, id, normText(context), normText(edges.toString())};
            info=Arrays.asList(csvContent);
//        }else{
//            String[] csvContent = {String.valueOf(typeId),loc, id, normText(context)};
//            info=Arrays.asList(csvContent);
//        }
//            rawData.add(csvContent);
//        System.out.println("typeId="+typeId);

        System.out.println("==================================================================================");


        return info;
    }

    private static List<String> showAllId(Vector<iden> alliden) {
        List<String> res=new ArrayList<>();
        for(iden d:alliden){
            res.add(d.getIdentifier());
        }
        return res;

    }

    private static iden getIdenInfo(String id, Vector<iden> alliden) {
        iden res=null;
        for(iden d:alliden){
            if(d.getIdentifier().toString().equals(id)) {
                res = d;
                break;
            }

        }
        return res;
    }

    public static String normText(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        //去除注释
        return dest;
    }
    public static Vector<iden> ObtainIdentifier(Vector<String> allstate, String javafilepath) throws Exception
    {
//        System.out.println(javafilepath);
        Vector<IdentifierDS> packages=new Vector<IdentifierDS>();  //加入当前的package
        Vector<IdentifierDS> types=new Vector<IdentifierDS>();     //类，接口，枚举
        Vector<IdentifierDS> methods=new Vector<IdentifierDS>();   //method，包括了constructor,setter,getter
        Vector<IdentifierDS> fields=new Vector<IdentifierDS>();    //
        Vector<IdentifierDS> variables=new Vector<IdentifierDS>(); //包括了函数的参数

        CompilationUnit cu =null;

        try {
            cu = JavaParser.parse(new File(javafilepath));

        }
        catch(Exception e)
        {
            System.err.println(e.toString());
//			BufferedWriter bw=new BufferedWriter(new FileWriter("D:\\project\\IdentifierStyle\\data\\JavaParserCannotParse.txt",true));
//            bw.write(javafilepath);
//            bw.newLine();
//            bw.close();
        }

        String packagename="";
        try {
            Optional<PackageDeclaration> packagename1=cu.getPackageDeclaration();
            if(packagename1.isPresent())
            {

                packagename=packagename1.get().toString().trim();
                if(packagename.startsWith("/*"))
                    packagename=packagename.substring(packagename.indexOf("*/")+2, packagename.length()).trim();
                if(packagename.startsWith("//"))
                    packagename=packagename.substring(packagename.indexOf("package "),packagename.length());
                if(packagename.startsWith("package "))
                    packagename=packagename.substring(packagename.indexOf(" ")+1,packagename.length());
                if(packagename.endsWith(";"))
                    packagename=packagename.substring(0,packagename.length()-1);
            }

        }
        catch(Exception e)
        {
            System.err.println(e.toString());
        }
        int packloc=1;
        for(int i=0;i<allstate.size();i++)
        {
            if(allstate.get(i).trim().startsWith("package "))
            {
                packloc=i+1;
                break;
            }
        }

        IdentifierDS newpackage=new IdentifierDS("","",packagename,"","",packloc);
        packages.add(newpackage);


        Hashtable<String,Integer> variableSet =new Hashtable<String,Integer>();	    	//所有的变量和对象
        VoidVisitor<Hashtable<String, Integer>> VariableCollector = new VariableCollector();
        try {
            VariableCollector.visit(cu, variableSet);
        }
        catch(Exception e)
        {
            System.err.println(e.toString());
        }


        Vector<ClassDS> classdetails=new Vector<ClassDS>();
        VoidVisitor<Vector<ClassDS>> classNameCollector = new ClassCollector();
        try {
            classNameCollector.visit(cu, classdetails);
        }
        catch(Exception e)
        {
            System.err.println(e.toString());
        }


        for(ClassDS one :classdetails)
        {
            IdentifierDS newclass=new IdentifierDS(one.getClassname(),"",one.getClassname(),"class","",one.getIndex());
            types.add(newclass);

            Vector<MethodDS> methodlist=one.getMethodlist();
            for(MethodDS onemethod:methodlist)
            {
                IdentifierDS newmethod=new IdentifierDS(one.getClassname(),onemethod.getMethodname(),onemethod.getMethodname(),onemethod.getReturntype(),"",onemethod.getBeginindex());
                methods.add(newmethod);

                Vector<IdentifierDS> parameters=onemethod.getParameters();
                //var 包括了参数
                for(IdentifierDS oneid:parameters)
                {
                    variables.add(oneid);
                }

            }

            Vector<IdentifierDS> fieldlist=one.getFieldlist();
            fields.addAll(fieldlist);

        }


        Set<String> keyset=variableSet.keySet();
        for(String onekey:keyset)
        {
            int onevalue=variableSet.get(onekey);
            String methodpar="";
            String classpar="";
            for(ClassDS one :classdetails)
            {

                Vector<MethodDS> methodlist=one.getMethodlist();
                for(MethodDS onemethod:methodlist)
                {
                    if(onevalue>=onemethod.getBeginindex()&&onevalue<=onemethod.getEndindex())
                    {
                        methodpar=onemethod.getMethodname();
                        classpar=one.getClassname();
                        break;
                    }
                }
            }


            if(onekey.contains("="))
            {
                String front=onekey.substring(0, onekey.indexOf("=")).trim();
                String end=onekey.substring(onekey.indexOf("=")+1, onekey.length()).trim();
                String name=front.substring(front.lastIndexOf(" ")+1, front.length()).trim();
                String type="";

                if(front.contains(" "))
                {
                    front=front.substring(0, front.lastIndexOf(" "));
                    if(front.contains(" "))
                    {
                        type=front.substring(front.lastIndexOf(" ")+1,front.length()).trim();
                    }
                    else type=front;
                }
                else
                    type=front;

                type=type.trim();
                IdentifierDS oneid=new IdentifierDS(classpar,methodpar,name,type,end,onevalue);

                variables.add(oneid);


            }
            else
            {
                String name=onekey.substring(onekey.lastIndexOf(" ")+1, onekey.length()).trim();
                onekey=onekey.substring(0, onekey.lastIndexOf(" ")).trim();
                String type="";
                if(onekey.contains(" "))
                    type=onekey.substring(onekey.lastIndexOf(" ")+1, onekey.length()).trim();
                else
                    type=onekey;

                type=type.trim();

                IdentifierDS oneid=new IdentifierDS(classpar,methodpar,name,type,"",onevalue);

                variables.add(oneid);

            }
        }


        Vector<iden> allid=new Vector<>();
        for(IdentifierDS onepackage:packages)
        {
//        	System.out.println(onepackage.toString());
            String identifiername=onepackage.getName();
            int location=onepackage.getLocation();
            String singlestate="";
            int purelocation=-1;
            if(location-1>=0&&allstate.size()>0)
            {
                singlestate=allstate.get(location-1);
                purelocation=location-1;
            }

            if(singlestate.contains(identifiername))
            {
                iden oness=new iden(1,identifiername,singlestate,purelocation);
                allid.add(oness);
            }
            else
            {
                System.err.println("1: 标识符位置不对！"+identifiername+"  "+singlestate);
            }

        }
        for(IdentifierDS onetype:types)
        {
//        	System.out.println(onetype.toString());

            String identifiername=onetype.getName();
            int location=onetype.getLocation();
            location=location/100000;

            String singlestate="";
            int purelocation=-1;
            if(location-1>=0&&allstate.size()>0)
            {
                singlestate=allstate.get(location-1);
                purelocation=location-1;
            }

            if(singlestate.trim().startsWith("@"))
            {
                if(location<allstate.size())
                {
                    singlestate=allstate.get(location);
                    purelocation=location;
                }

                if(singlestate.trim().startsWith("@"))
                {
                    singlestate=allstate.get(location+1);
                    purelocation=location+1;
                }
            }

            if(singlestate.contains(identifiername))
            {
                iden oness=new iden(2,identifiername,singlestate,purelocation);
                allid.add(oness);
            }
            else
            {
                System.err.println("2: 标识符位置不对！"+identifiername+"  "+singlestate);
            }

        }
        for(IdentifierDS onemethod:methods)
        {
            String identifiername=onemethod.getName();
            int location=onemethod.getLocation();

            String singlestate=allstate.get(location-1);
            int purelocation=location-1;

            if(singlestate.trim().startsWith("@"))
            {
                singlestate=allstate.get(location);
                purelocation=location;

                if(singlestate.trim().startsWith("@"))
                {
                    singlestate=allstate.get(location+1);
                    purelocation=location+1;
                }
            }

            if(singlestate.contains(identifiername))
            {
                iden oness=new iden(3,identifiername,singlestate,purelocation);
                allid.add(oness);
            }
            else
            {
                System.err.println("3: 标识符位置不对！"+identifiername+"  "+singlestate);
            }
        }
        for(IdentifierDS onefield:fields)
        {
            String identifiername=onefield.getName();
            int location=onefield.getLocation();
            String singlestate=allstate.get(location-1);
            int purelocation=location-1;
            if(singlestate.contains(identifiername))
            {

                iden oness=new iden(4,identifiername,singlestate,purelocation);
                allid.add(oness);
            }
            else
            {
                System.err.println("4: 标识符位置不对！"+identifiername+"  "+singlestate);
            }
        }
        for(IdentifierDS onevariable:variables)
        {
            String identifiername=onevariable.getName();
            int location=onevariable.getLocation();
            String singlestate=allstate.get(location-1);
            int purelocation=location-1;
            if(singlestate.trim().startsWith("@"))
            {
                singlestate=allstate.get(location);
                purelocation=location;
                if(singlestate.trim().startsWith("@"))
                {
                    singlestate=allstate.get(location+1);
                    purelocation=location+1;
                }
            }

            if(singlestate.contains(identifiername))
            {
                iden oness=new iden(5,identifiername,singlestate,purelocation);
                allid.add(oness);
            }
            else
            {
                if(purelocation+1<allstate.size())
                {
                    singlestate=singlestate+" "+allstate.get(purelocation+1);
                    //        		purelocation=location+1;
                    if(singlestate.contains(identifiername))
                    {
                        iden oness=new iden(5,identifiername,singlestate,purelocation);
                        allid.add(oness);
                    }
                    else
                    {
                        System.err.println("5: 标识符位置不对！"+identifiername+"  "+singlestate);
                    }
                }
            }
        }


        return allid;

    }

}
