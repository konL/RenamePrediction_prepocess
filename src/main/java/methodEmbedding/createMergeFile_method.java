package methodEmbedding;

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


public class createMergeFile_method {
    static List<String> fieldsName;
    static List<String> methodName_extend;
    static List<String> variableName;
    static List<String> callSet;
    static List<String> methodCall;
    //获取declaration
    static Map<String, FieldDeclaration> fieldMap;
    static Map<String, MethodDeclaration> methodMap;
    static Map<String, VariableDeclarationExpr> variableMap;
    static Map<String, String> callMap;
    static List<String> results;
    static Map<String, List<String>> classMap;
    static String[] type = {"package", "type", "method", "field", "variable"};
    static List<String> changeIdlist;

    public static void main(String[] args) throws Exception {
        //single-process:
//        create("101repo");
        //batch-process:
        String path = "C:\\project\\Projects_50\\VersionDB\\raw_project\\GROUP_1";		//要遍历的路径
        File file = new File(path);		//获取其file对象
        File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
        for(File proj:fs) {                    //遍历File[]数组
            if(!proj.getName().equals("abdera") && !proj.getName().equals("101repo")  ) {
                create(proj.getName(),"1");
            }

        }
    }

    public static void create(String proj,String group) throws Exception {
        String renamePath="C:\\project\\Projects_50\\Renaming\\" + proj + ".csv";


        //记录所有重命名Id，fileSeet是记录已经获取过unchange的文件=============================================================
        Set<String> renamingId = new HashSet<String>();
        List<String> fileSet = new LinkedList<>();
        changeIdlist = new ArrayList<>();
        //① 读取记录了改名的csv文件
        CsvReader read1 = new CsvReader(renamePath);
//        CsvReader read1 = new CsvReader("C:\\project\\Projects_50\\Renaming\\" + proj + ".csv");
        System.out.println("===========① reading renaming name=============");
        while (read1.readRecord()) {
            String[] changeIdSet = read1.get(3).split("<-");
            if (changeIdSet.length == 0) continue;
            changeIdlist.add(read1.get(3));
            for (int i = 1; i < changeIdSet.length; i++) {
                if (changeIdSet[i].length() > 0)
                    renamingId.add(changeIdSet[i]);
            }
        }
        System.out.println("renaming id=" + renamingId.toString());
        System.out.println("renaming size=" + renamingId.size());


        //写入的文件
        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\project\\Projects_50\\VersionDB\\raw_data\\context\\GROUP_"+group+"\\" + proj + "_merge_method.csv", true));
        CsvWriter writer = new CsvWriter(bw, ',');
        CsvReader read = new CsvReader(renamePath);
        while (read.readRecord()) {
            //仅仅关注method的renaming pair
            if (Integer.parseInt(read.get(0)) != 3) continue;
            String[] changeIdSet = read.get(3).split("<-");
            if (changeIdSet.length == 0) continue;
            String[] t = read.get(2).split("<=")[0].split("<-")[0].split("\\\\");
            String filename = t[t.length - 1].trim();
            //每一对renaming pair，找到对应的版本对实现
            for (int i = 1; i < changeIdSet.length; i++) {
                renamingId.add(changeIdSet[i]);
                String changeId = changeIdSet[i - 1] + "_" + changeIdSet[i];//id
                String changeId_ori = changeIdSet[i - 1] + "." + changeIdSet[i];
                ;
                String id = read.get(4 + (i - 1)).trim();//commitId
                //4+（所有id数量）=changeIdset.length-1
                String oldStmt = read.get(4 + (changeIdSet.length - 1) + (i)).trim();
                String newStmt = read.get(4 + (changeIdSet.length - 1) + (i - 1)).trim();

                //获得对应的java文件，根据changeId生成oldCContext，newContext
                String file_old = "C:\\project\\Projects_50\\VersionDB\\raw_project\\GROUP_"+group+"\\" + proj +"\\" + proj + "_old\\" + id + "_" + changeId + "_" + filename;
                String file_new = "C:\\project\\Projects_50\\VersionDB\\raw_project\\GROUP_"+group+"\\" + proj  + "\\" + proj + "_new\\" + id + "_" + changeId + "_" + filename;



                List<String> oldInfo = new ArrayList<>();
                List<String> newInfo = new ArrayList<>();

                //获得对应的java文件，生成oldCContext，newContext（重命名版本的）
                oldInfo = create("old", oldStmt.trim(), changeId_ori, file_old);
                newInfo = create("new", newStmt.trim(), changeId_ori, file_new);
//
                if(oldInfo.size()==0 || newInfo.size()==0) continue;
                try{
                    String oldedge=oldInfo.get(4);
                    String newedge=newInfo.get(4);

                    writer.writeRecord(new String[]{"1", type[Integer.parseInt(oldInfo.get(0))-1], oldInfo.get(2), newInfo.get(2), oldInfo.get(3), newInfo.get(3),oldedge,newedge});
                }catch (Exception e){
                    continue;
                }
                String filedone=id+"_"+filename;
                //去重
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
                    String stmt=identifier.getStatement();
                   //假设标识符不变的声明也不变，声明变化但是标识符不变的就不管了
                    if(!renamingId.contains(oneid.trim())&&oneid.trim().length()>0){
                        unchange_old=create("old",stmt,oneid+"."+oneid,file_old);
                        unchange_new=create("new",stmt,oneid+"."+oneid,file_new);
                        if(unchange_old.size()==0 || unchange_new.size()==0) continue;
                        try{
                            String oldedge=unchange_old.get(4);
                            String newedge=unchange_new.get(4);
//                  System.out.println("typeId="+Integer.parseInt(oldInfo.get(0)));
                            writer.writeRecord(new String[]{"0", type[Integer.parseInt(unchange_old.get(0))-1], unchange_old.get(2), unchange_new.get(2), unchange_old.get(3), unchange_new.get(3),oldedge,newedge});
                        }catch (Exception e){
                            continue;
                        }

                    }
                }
            }

        }
        System.out.println("项目" + proj + "的上下文文件（context+change entity）已生成");
        read.close();
        writer.close();
    }

    private static List<String> create(String ver, String statement, String changeId, String loc) throws Exception {
        //记录传入method的信息
        List<String> info = new ArrayList<>();
        //name，param，type
        statement.replace("####", ",");
        StringBuffer code = new StringBuffer(statement.trim());
        if (statement.trim().endsWith("{")) {
            code.deleteCharAt(code.length() - 1);
            code.append(";");
        }
        if (statement.trim().endsWith(")")) {
            code.append(";");
        }
        code.insert(0, "public class test{");
        code.append("}");
        Map<String, List> codeMap = new HashMap<>();
        System.out.println("code="+code);
        try {
            codeMap = JavaParserUtils.getData(code.toString(), true);

        }catch (Exception e){
            return info;
        }
        String parsed_id = codeMap.get("method_name_extend").get(0).toString();




        String id = "";
        if (ver.equals("old")) {
            id = changeId.split("\\.")[1];
        } else {
            id = changeId.split("\\.")[0];
        }
        List<String> methodName = new ArrayList<String>();
        List<String> methodBody = new ArrayList<String>();
//
        File f = new File(loc);
        if (!f.exists()) {
            System.out.println("【not exist!】" + loc);
            return info;
        }

        Map<String, List> map = new HashMap<>();
        try {
            map = JavaParserUtils.getData(loc);
            fieldMap = JavaParserUtils.fieldMap;
            callSet = map.get("call_relation");

            variableMap = JavaParserUtils.variableMap;
            callMap = JavaParserUtils.nameExprMap;
            fieldsName = map.get("fields_name");
            methodName_extend = map.get("method_name_extend");
            methodBody = map.get("method_body");

//
        } catch (Exception e) {
            System.out.println("JAVAPARSER ERR!!!!");
            System.out.println("its file" + ver + ":" + loc);
            System.out.println("【parsed id】=" + parsed_id);
            System.out.println("【curr id】=" + id.trim());
            System.out.println("===============================================");
            return info;
        }
////
        String context = "";
        StringBuffer edges = new StringBuffer();
//
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
////        System.out.println(alliden.toString());

////        System.out.println("field="+fieldMap.keySet().toString());
////        System.out.println("field name="+fieldsName.toString());
////        System.out.println("classMap="+classMap.keySet().toString());
////        System.out.println("callSet="+callSet.toString());
////        System.out.println("varible="+variableMap.keySet().toString());
////        System.out.println("all id="+showAllId(alliden).toString());
        System.out.println("its file" + ver + ":" + loc);
        System.out.println("【parsed id】=" + parsed_id);
        System.out.println("【curr id】=" + id.trim());
        System.out.println("【method set】=" + methodName_extend.toString());


        int typeId = 0;
        //①使用javaParser获取函数的上下文
        if (methodName_extend.contains(parsed_id.trim())) {
            int index = methodName_extend.indexOf(parsed_id.trim());
            context = methodBody.get(index);
            iden method = getIdenInfo(id, alliden);
            if (method == null) return info;
            typeId = method.getType();
            System.out.println("methods contains " + id);
            System.out.println("context : " + context);


            try {
                //找到其包含的所有实体(set)
                StringBuffer code_context=new StringBuffer(context.trim());
                code_context.insert(0, "public class test{");
                code_context.append("}");


                Map<String,List> contextmap= JavaParserUtils.getData(code_context.toString(),true);
                List<String> containent_method = contextmap.get("call_relation");
                List<String> containcall_method = contextmap.get("method_call");




                Set<String> set = new HashSet<>();

                for (String nameExpr : containent_method) {
                    //得nameexpr父类
                    set.add(nameExpr);

                }
                for (String callExpr : containcall_method) {

                    set.add(callExpr);


                }
                System.out.print(id + ":" + parsed_id + "【包含实体】");
                System.out.println(set.toString());
                List<String> node = new ArrayList<>();
                List<List<String>> edge = new ArrayList<>();
                for (String s : set) {
                    node.add(s);
                    List<String> edgePair = new ArrayList<>();
                    edgePair.add(id);
                    edgePair.add(s);
                    edge.add(edgePair);
                }
//                List<String> node = network[0];
//                List<List> edge = network[1];

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
            } catch (NullPointerException e) {
                System.out.println("ERR ID==" + id);
            }
            System.out.println();


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
