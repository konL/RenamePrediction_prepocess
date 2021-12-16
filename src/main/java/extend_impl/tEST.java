package extend_impl;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import methodEmbedding.createMergeFile_method;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class tEST {
    public static void main(String[] args) throws Exception {
//     Map<String, List> map=JavaParserUtils.getData("C:\\Users\\delll\\Desktop\\liangjh\\iden_project\\RenamePrediction_prepocess\\src\\main\\resources\\Example.java");
//        Map<String, List> map = JavaParserUtils.getData("C:\\project\\MethodPrediction_All_ID\\data\\GitProject\\dubbo\\dubbo-cluster\\src\\main\\java\\com\\alibaba\\dubbo\\rpc\\cluster\\configurator\\AbstractConfigurator.java");
                Map<String, List> map = JavaParserUtils.getData("public class test{public void test(String t,int group){}" +
                                "}" , true);

        List<String> methodset = map.get("method_name_extend");
        System.out.println("method=" + methodset.toString());

        List<String> methodBody = map.get("method_body");
//        System.out.println("context=" + methodBody.toString());

        //callSet包括方法内的局部变量，静态对象（JavaParser.sent的JabaParser），field,总之是方法内的所有“对象”，【没有内部调用的方法】
        List<String> callset = map.get("call_relation");
        System.out.println(callset.toString());



        Map<String, String> callMap = JavaParserUtils.nameExprMap;
//        //callMap包括所有方法体内引用的方法及其调用者
        Map<String, String> methodCallMap = JavaParserUtils.methodCallMap;
        List<String> methodCall = map.get("method_call");


        //获取方法体内的所有代码实体

//        String ent = methodset.get(0);
//        Set<String> set = new HashSet<>();
//        for (String nameExpr : callset) {
//            //得nameexpr父类
//            set.add(nameExpr);
//
//        }
//        for (String callExpr : methodCall) {
//
//            set.add(callExpr);
//
//
//        }
//        System.out.println(ent + "包含实体");
//        System.out.println(set.toString());



        //var和field获取其声明语句和使用语句
        List fieldsName = map.get("fields_name");
        List variableName = map.get("variable_name");
        List methodName = map.get("method_name");
        Map<String, String> var_stmtMap = JavaParserUtils.var_stmtMap;
        Map<String, String> entUsageMap = JavaParserUtils.entUsageMap;

        Map<String, VariableDeclarationExpr> varMap = JavaParserUtils.variableMap;
        Map<String, FieldDeclaration> fieldMap = JavaParserUtils.fieldMap;
        //获取局部变量context
//        String ent = "conditionKeys";
//        String methodpar="configureIfMatch";
//        String body="";
//        if(methodName.contains(methodpar.trim())) {
//
//            int index = methodName.indexOf(methodpar);
//            body = methodBody.get(index);
////            System.out.println("methodbody="+body);
//            while (!body.contains(ent)){
//                System.out.println("not exist");
//            }
//
//        }



        //解析获取其中的variable和其n.getVariableParentNode(初始语句)+callSetStmt里面包含variable的
//        StringBuffer code=new StringBuffer(body);
//        code.insert(0, "public class test{");
//        code.append("}");

        Map<String, List> codeMap = new HashMap<>();
//            System.out.println("code="+code);

//            codeMap = JavaParserUtils.getData(code.toString(), true);
            var_stmtMap = JavaParserUtils.var_stmtMap;
            entUsageMap = JavaParserUtils.entUsageMap;
            System.out.println("------------------------------------------------");
            for (String e:var_stmtMap.keySet()){
                System.out.println(var_stmtMap.get(e));
            }
        System.out.println("------------------------------------------------");
           for(String e:entUsageMap.keySet()){
               System.out.println(entUsageMap.get(e));
           }



//            StringBuffer context=new StringBuffer();
//        if(var_stmtMap.containsKey(ent)){
////           System.out.println(ent+":"+var_stmtMap.get(ent));
//            context.append(var_stmtMap.get(ent)+";");
//        }
//        //获取使用语句
//        if(entUsageMap.containsKey(ent)){
////          System.out.println(ent+":"+entUsageMap.get(ent));
//
//                    context.append(entUsageMap.get(ent));
//
//
//
//        }
//        System.out.println(context.toString());


//        //class获取类中的所有使用语句 rename肯定是同一个类的不同类的识别不出来
//        String ent="resourceRemove_result";
//        Map<String,String> classMap=JavaParserUtils.classMap;
//        Map<String,List<String>> class_ent=JavaParserUtils.class_ent;
//        if(classMap.containsKey(ent)){
//            System.out.println(ent+",statement="+classMap.get(ent));
//        }
//        //相关实体 其extend或者implement的或者没有
//        if(class_ent.containsKey(ent)){
//            System.out.println(ent+",class_ent="+class_ent.get(ent));


    }






    public static int indexOf(List<?> list, int start, Object value) {
        int idx = list.subList(start, list.size()).indexOf(value);
        return idx != -1 ? idx + start : -1;
    }




}
