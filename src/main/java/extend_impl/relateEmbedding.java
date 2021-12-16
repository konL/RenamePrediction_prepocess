package extend_impl;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import compare.Diff;
import compare.Difference;
import org.apache.commons.lang3.StringUtils;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class relateEmbedding {
    static String ent;
    static Map<String, List> map;
    static List<String>  fieldsName;
    static List<String> methodName;
    static List<String> variableName;
    static List<String> callSet;
    //获取declaration
    static Map<String, FieldDeclaration> fieldMap;
    static Map<String, MethodDeclaration> methodMap;
    static Map<String, VariableDeclarationExpr> variableMap;
    static Map<String, String> callMap;
    static Map<String, String> classMap;
    Map<String,String> methodCallMap;
    public relateEmbedding(String ent,Map<String, List> map){
        this.ent=ent;
        this.map=map;

        fieldsName=map.get("fields_name");
        methodName=map.get("method_name");
        variableName=map.get("variable_name");
        callSet=map.get("call_relation");
        fieldMap= JavaParserUtils.fieldMap;
        methodMap= JavaParserUtils.methodMap;
        variableMap= JavaParserUtils.variableMap;
        callMap= JavaParserUtils.nameExprMap;
        classMap=JavaParserUtils.classMap;
        methodCallMap=JavaParserUtils.methodCallMap;


    }



    public   List[]searchRes(String ent,List<String> sameFileChange) {
// 1）Inclusion：包含e直接包含的实体和直接包含e的元素（包含）

//3）Reference：e所引用的所有实体和引用e的实体(调用)
//4)   Inheritance：e是一个类，则其超类和子类
        List<String> res = new ArrayList<>();
        //加入相关的
        Set<String> set = new HashSet<>();

        //1.Inclusion-method：实体是函数，包含该函数的实体（父类）:其他方法或者类
        if(methodName.contains(ent)){
//            //①--------------------父级类---------------------------
//            MethodDeclaration m = methodMap.get(ent);
//            String pStr=JavaParserUtils.getParents(m);
//            String[] parents=pStr.split("\\.");
//            for(String parent:parents){
//                    if(parent.length()>0) {
//                        set.add(parent);
//                    }
//                }
//
//            //②--------------------父级方法---------------------------
//            System.out.println(methodCallMap.get(ent));
//            String[] pStr2=methodCallMap.get(ent).split("@@@@");
//            Set<String> parents2 = new HashSet<String>(Arrays.asList(pStr2));
//            for(String parent:parents2){
//                if(parent.length()>0) {
//                    String[] data = parent.split("\\.");
//                    if(data.length>0) {
//                        set.add(data[data.length - 1]);
//                    }
//                }
//            }

            //③Inclusion-callSet:函数中的实体，包括callSet和methodCall
            for(String nameExpr:callMap.keySet()) {
                //得nameexpr父类
                String[] pStr3=callMap.get(nameExpr).split("@@@@");
                Set<String> parents3 = new HashSet<String>(Arrays.asList(pStr3));
                for(String parent:parents3){
                    String[] data=parent.split("\\.");
                    if(data[data.length-1].trim().equals(ent)){
                        set.add(nameExpr);
                    }
                }

            }
            for(String callExpr:methodCallMap.keySet()) {
                //得nameexpr父类
                String[] pStr4=methodCallMap.get(callExpr).split("@@@@");
                Set<String> parents4 = new HashSet<String>(Arrays.asList(pStr4));
                for(String parent:parents4){
                    String[] data=parent.split("\\.");
                    if(data[data.length-1].trim().equals(ent)){
                        set.add(callExpr);
                    }
                }

            }





        }
//        //2。field相关实体是其父类
//        if(fieldsName.contains(ent.trim())){
//            //①--------------------父级类---------------------------
//            FieldDeclaration f = fieldMap.get(ent);
//            String pStr=JavaParserUtils.getParents(f);
//            String[] parents=pStr.split("\\.");
//            for(String parent:parents){
//                if(parent.length()>0) {
//                    set.add(parent);
//                }
//            }
//
//
//            //②父级方法
//            if(callMap.containsKey(ent)) {
//                //父类方法
//                String[] pStr3=callMap.get(ent).split("@@@@");
//                Set<String> parents3 = new HashSet<String>(Arrays.asList(pStr3));
//                for(String parent:parents3){
//                    if(parent.length()>0){
//                    String[] data=parent.split("\\.");
//                    set.add(data[data.length-1]);
//                    }
//                }
//
//            }
//
//        }
//        //2。variable相关实体是其父类
//        if(variableName.contains(ent.trim())) {
//            if(callMap.containsKey(ent)) {
//                //父类方法
//                String[] pStr3=callMap.get(ent).split("@@@@");
//                Set<String> parents3 = new HashSet<String>(Arrays.asList(pStr3));
//                for(String parent:parents3){
//                    if(parent.length()>0){
//                        String[] data=parent.split("\\.");
//                        set.add(data[data.length-1]);
//                    }
//                }
//
//            }
//        }

//
            List<String> v = new ArrayList<>();
            List<List<String>> es = new ArrayList<>();
            for (String s : set) {
//            System.out.println("set_ele="+s);
                v.add(s);
                List<String> edgePair = new ArrayList<>();
                edgePair.add(ent);
                edgePair.add(s);
                es.add(edgePair);
            }


            return new List[]{v, es};
        }
    private static String[] split(String old) {
        /*IdName被分解为由下划线和大写字母分隔的术语序列，假设名称遵循流行的驼峰式或蛇形式命名约定。
        分解不遵循这些约定的标识符名称的替代方法。
         */
        //1.先处理变量名
        String id=old;

        //根据下划线和大写字母分割
        /*正则表达式：句子结束符*/
        String regEx="(?=[_|[A-Z]])";
        Pattern p =Pattern.compile(regEx);
        Matcher m = p.matcher(id);

        /*按照句子结束符分割句子*/
        String[] fieldSeq = p.split(id);

        return fieldSeq;

    }
    public static  Map<String,List<List<String>>> generateChangeSet(String[] s1,String[] s2){
        boolean isSame=true;
        if(s1.length==s2.length){
            for(int i=0;i<s1.length;i++){
                if(!s1[i].trim().equals(s2[i].trim())) isSame=false;
            }
        }
        if(isSame) return null;
        List<String> oldN=new ArrayList<>(Arrays.asList(s1));
        List<String> newN=new ArrayList<>(Arrays.asList(s2));
        //存储三种类型的集合：删除、插入和替换操作
        Map<String,List<List<String>>> map=new HashMap<>();


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
//        System.out.println("------------------------------------------------------------------");
//        System.out.println("根据转化脚本与"+resName+"生成的对应分割集合sps为：");
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
//        for (List<String> spi:sps){
//            System.out.println("spi={"+spi.get(0)+" "+spi.get(1)+" "+spi.get(2)+"}");
//        }
        // System.out.println("------------------------------------------------------------------");
        return sps;

    }
    private static int compare(String str, String target, boolean isIgnore) {
        int d[][]; // 矩阵
        int n = str.length();
        int m = target.length();
        int i; // 遍历str的
        int j; // 遍历target的
        char ch1; // str的
        char ch2; // target的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) { // 遍历str
            ch1 = str.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (isIgnore) {
                    if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }
                } else {
                    if (ch1 == ch2) {
                        temp = 0;
                    } else {
                        temp = 1;
                    }
                }

                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
            }
        }
        return d[n][m];
    }

    private static int min(int one, int two, int three) {
        return (one = one < two ? one : two) < three ? one : three;
    }

    public static float getSimilarityRatio(String str, String target, boolean isIgnore) {
        float ret = 0;
        if (Math.max(str.length(), target.length()) == 0) {
            ret = 1;
        } else {
            ret = 1 - (float) compare(str, target, isIgnore) / Math.max(str.length(), target.length());
        }
        return ret;
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

        for(int i=0;i<o.size();i++) {


            double dis= StringUtils.getJaroWinklerDistance(o.get(i).toLowerCase(), c.get(i).toLowerCase());
            //double dis=StringUtils.getLevenshteinDistance(o.get(i).toLowerCase(), c.get(i).toLowerCase());
//            double dis=CosineSimilarity.getSimilarity(o.get(i).toLowerCase(), c.get(i).toLowerCase());
//            double dis=similarScoreCos(o.get(i).toLowerCase(),c.get(i).toLowerCase());
            sum+=dis;


        }


        return sum/(o.size());

    }
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
//            System.out.println("ele="+ele);
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
    private static String[] searchAllRes(String old) {
// 1）Inclusion：包含e直接包含的实体和直接包含e的元素。
//2）Sibling：e是一个方法，同一个类中的所有方法和字段都被认为是紧密相关的实体
//3）Reference：e所引用的所有实体和引用e的实体
//4)   Inheritance：e是一个类，则其超类和子类


        Set<String> set=new HashSet<>();
        for (String method:methodName){
            //非空
            if(!method.equals("")&&! method.equals(old)){


                set.add(method);
            }
        }
        for (String field:fieldsName){

            if(!field.equals("")&&! field.equals(old)){
                set.add(field);
//                System.out.println("fileld="+field);
            }
        }

        for (String var:variableName){
            if(!var.equals("")&&! var.equals(old)){
                set.add(var);
//                System.out.println("varble="+var);
            }
        }


        //-------------------------------方法二-----------------------------
        //1.直接包含的实体--
        //1.1如果是函数,检测调用该函数的软件实体
        if(methodName.contains(old.substring(1,old.length()-1))){
            MethodDeclaration m=methodMap.get(old);
            if(m!=null) {
                String[] data = compare.JavaParserUtils.getParents(m).split("\\.");
                for (String s : data) {
                    if (!s.equals("")) {
                        set.add(s);
                    }


                }
                //把methodname全部放进去

            }



        }

        if (variableName.contains(old.substring(1,old.length()-1))) {
            VariableDeclarationExpr v = variableMap.get(old);
            System.out.println("var=" + variableMap.keySet());
            if (v != null) {
                String[] data = compare.JavaParserUtils.getParents(v).split("\\.");
                for (String s : data) {
                    if (!s.equals("")) {
                        set.add(s);
                    }


                }

            }
            if (fieldsName.contains(old.substring(1, old.length() - 1))) {
                FieldDeclaration f = fieldMap.get(old);
                System.out.println("field=" + fieldMap.keySet());
                if (f != null) {
                    String[] data = compare.JavaParserUtils.getParents(f).split("\\.");
                    for (String s : data) {
                        if (!s.equals("")) {
                            set.add(s);
                        }


                    }
                }

            }
        }
        //1.5 对于函数合并的处理
        //1.6对于函数分裂的处理

/*NameExpr:m_BaseInstance
MethodCallExpr:setBaseInstances*/
        //2.sibling 类Srccode的所有字段和方法，父类的所有子元素

        //3.直接访问这个字段的方法

        //先生成数据
        String[] res = set.toArray(new String[set.size()]);
        //String[] res_={ "[m_BaseInstPanelCase]", "[setBaseInstanceFromFileQCase]","[setBaseInstancesFromDBQCase]"};
        return res;
    }

    }
