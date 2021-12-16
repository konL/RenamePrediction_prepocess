package methodEmbedding;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.util.*;


public class JavaParserUtils {
    static List<String> methodName;
    static List<String> methodName_extend;
    static List<String> fieldsName;
    static List<String> methodBody;
    static List<String> varibleName;
    static List<String> callSet;
    static List<String> methodCall;
    //存储对应的xxName和xxxDeclaration
    static Map<String, FieldDeclaration> fieldMap;
    //局部变量的名字-》父类的映射
    static Map<String, VariableDeclarationExpr> variableMap;
    static Map<String, MethodDeclaration> methodMap;
    static Map<String, MethodDeclaration> methodMap_extend;
    //callSet:方法参数，方法内的全局变量
    static Map<String, String> nameExprMap;
    static Map<String, String> methodCallMap;

    static Map<String, List<String>> classMap;

    public static Map<String, List> getData(String code, Boolean isCode) throws Exception {
//        FileInputStream in = new FileInputStream("D:\\kon_data\\JAVA_DATA\\SpotRenaming\\ast-javaParser\\src\\main\\resources\\Srccode.java");


        // parse the file
        methodName = new ArrayList<String>();
        methodName_extend = new ArrayList<String>();
        fieldsName = new ArrayList<String>();
        methodBody = new ArrayList<String>();
        varibleName = new ArrayList<String>();
        callSet = new ArrayList<String>();
        methodCall = new ArrayList<String>();
        //存储对应的xxName和xxxDeclaration
        fieldMap = new HashMap<>();
        //局部变量的名字-》父类的映射
        variableMap = new HashMap<>();
        methodMap = new HashMap<>();
        methodMap_extend = new HashMap<>();
        //callSet:方法参数，方法内的全局变量
        nameExprMap = new HashMap<>();
        methodCallMap = new HashMap<>();

        classMap = new HashMap<>();
        CompilationUnit cu = null;
        cu = JavaParser.parse(code);


        // prints the resulting compilation unit to default system output
//        System.out.println(cu.toString());

        cu.accept(new Visitor(), null);
//        //查看list
//        for(String mn:methodBody){
//            System.out.println(mn);
//        }
        Map<String, List> map = new HashMap<>();
        map.put("fields_name", fieldsName);
        map.put("method_name", methodName);
        map.put("method_name_extend", methodName_extend);
        map.put("method_body", methodBody);
        map.put("variable_name", varibleName);
        map.put("call_relation", callSet);
        map.put("method_call", methodCall);
        return map;


    }

    public static Map<String, List> getData(String file) throws Exception {
//        FileInputStream in = new FileInputStream("D:\\kon_data\\JAVA_DATA\\SpotRenaming\\ast-javaParser\\src\\main\\resources\\Srccode.java");
        methodName = new ArrayList<String>();
        methodName_extend = new ArrayList<String>();
        fieldsName = new ArrayList<String>();
        methodBody = new ArrayList<String>();
        varibleName = new ArrayList<String>();
        callSet = new ArrayList<String>();
        methodCall = new ArrayList<String>();
        //存储对应的xxName和xxxDeclaration
        fieldMap = new HashMap<>();
        //局部变量的名字-》父类的映射
        variableMap = new HashMap<>();
        methodMap = new HashMap<>();
        methodMap_extend = new HashMap<>();
        //callSet:方法参数，方法内的全局变量
        nameExprMap = new HashMap<>();
        methodCallMap = new HashMap<>();

        classMap = new HashMap<>();
        FileInputStream in = new FileInputStream(file);

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);


        // prints the resulting compilation unit to default system output
//        System.out.println(cu.toString());

        cu.accept(new Visitor(), null);
//        //查看list
//        for(String mn:methodBody){
//            System.out.println(mn);
//        }
        Map<String, List> map = new HashMap<>();
        map.put("fields_name", fieldsName);
        map.put("method_name", methodName);
        map.put("method_name_extend", methodName_extend);
        map.put("method_body", methodBody);
        map.put("variable_name", varibleName);
        map.put("call_relation", callSet);

        map.put("method_call", methodCall);
        return map;


    }

    public static String getParents(final NameExpr nameExp) {
        final StringBuilder path = new StringBuilder();

        nameExp.walk(Node.TreeTraversal.PARENTS, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                path.insert(0, ((ClassOrInterfaceDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof ObjectCreationExpr) {
                path.insert(0, ((ObjectCreationExpr) node).getType().getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof MethodDeclaration) {
                path.insert(0, ((MethodDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof CompilationUnit) {
                final Optional<PackageDeclaration> pkg = ((CompilationUnit) node).getPackageDeclaration();
                if (pkg.isPresent()) {
                    path.replace(0, 1, ".");
                    path.insert(0, pkg.get().getNameAsString());
                }
            }
        });

        // convert StringBuilder into String and return the String
        //System.out.println("parents:"+path.toString());
        return path.toString();
    }

    public static String getParents(final MethodCallExpr methodCallExpr) {
        final StringBuilder path = new StringBuilder();

        methodCallExpr.walk(Node.TreeTraversal.PARENTS, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                path.insert(0, ((ClassOrInterfaceDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof ObjectCreationExpr) {
                path.insert(0, ((ObjectCreationExpr) node).getType().getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof MethodDeclaration) {
                path.insert(0, ((MethodDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof CompilationUnit) {
                final Optional<PackageDeclaration> pkg = ((CompilationUnit) node).getPackageDeclaration();
                if (pkg.isPresent()) {
                    path.replace(0, 1, ".");
                    path.insert(0, pkg.get().getNameAsString());
                }
            }
        });

        // convert StringBuilder into String and return the String
        //System.out.println("parents:"+path.toString());
        return path.toString();
    }

    public static String getParents(final MethodDeclaration methodDeclaration) {
        final StringBuilder path = new StringBuilder();

        methodDeclaration.walk(Node.TreeTraversal.PARENTS, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                path.insert(0, ((ClassOrInterfaceDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof ObjectCreationExpr) {
                path.insert(0, ((ObjectCreationExpr) node).getType().getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof MethodDeclaration) {
                path.insert(0, ((MethodDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof CompilationUnit) {
                final Optional<PackageDeclaration> pkg = ((CompilationUnit) node).getPackageDeclaration();
                if (pkg.isPresent()) {
                    path.replace(0, 1, ".");
                    path.insert(0, pkg.get().getNameAsString());
                }
            }
        });

        // convert StringBuilder into String and return the String
//        System.out.println("parents:"+path.toString());
        return path.toString();
    }

    public static String getParents(final VariableDeclarationExpr variableDeclaration) {
        final StringBuilder path = new StringBuilder();

        variableDeclaration.walk(Node.TreeTraversal.PARENTS, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                path.insert(0, ((ClassOrInterfaceDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof ObjectCreationExpr) {
                path.insert(0, ((ObjectCreationExpr) node).getType().getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof MethodDeclaration) {
                path.insert(0, ((MethodDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof CompilationUnit) {
                final Optional<PackageDeclaration> pkg = ((CompilationUnit) node).getPackageDeclaration();
                if (pkg.isPresent()) {
                    path.replace(0, 1, ".");
                    path.insert(0, pkg.get().getNameAsString());
                }
            }
        });

        // convert StringBuilder into String and return the String
//        System.out.println("parents:"+path.toString());
        return path.toString();
    }

    public static String getParents(final FieldDeclaration fieldDeclaration) {
        final StringBuilder path = new StringBuilder();

        fieldDeclaration.walk(Node.TreeTraversal.PARENTS, node -> {
            if (node instanceof ClassOrInterfaceDeclaration) {
                path.insert(0, ((ClassOrInterfaceDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof ObjectCreationExpr) {
                path.insert(0, ((ObjectCreationExpr) node).getType().getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof MethodDeclaration) {
                path.insert(0, ((MethodDeclaration) node).getNameAsString());
                path.insert(0, '.');
            }
            if (node instanceof CompilationUnit) {
                final Optional<PackageDeclaration> pkg = ((CompilationUnit) node).getPackageDeclaration();
                if (pkg.isPresent()) {
                    path.replace(0, 1, ".");
                    path.insert(0, pkg.get().getNameAsString());
                }
            }
        });

        // convert StringBuilder into String and return the String
//        System.out.println("parents:"+path.toString());
        return path.toString();
    }


    private static class Visitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(final FieldDeclaration n, Void arg) {
//            System.out.println("Fields:"+n.getVariables());
            String field = n.getVariables().toString();
            int eq = field.indexOf("=");
            String fid = "";
            if (eq != -1) {
                fid = field.substring(1, eq);
            } else {
                fid = field.substring(1, field.length() - 1);
            }


            fieldsName.add(fid.trim());
            fieldMap.put(fid.trim(), n);
            super.visit(n, arg);

        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            /* here you can access the attributes of the method.
             this method will be called for all methods in this
             CompilationUnit, including inner class methods */
            StringBuffer param = new StringBuffer();
            for (String par : n.getParameters().toString().substring(1, n.getParameters().toString().length() - 1).split(",")) {
                param.append(par.trim() + "#");
            }

            String method_extend = n.getType().toString() + "@@@@" + n.getNameAsString() + "@@@@" + param.toString();
            methodName_extend.add(method_extend);
            methodMap_extend.put(method_extend, n);

            methodName.add(n.getNameAsString());
            methodMap.put(n.getNameAsString(), n);

            //首先分割
//            System.out.println("declare="+n.getDeclarationAsString());
            StringBuffer b=new StringBuffer();
            if(n.getBody().isPresent()) {
                if(n.getBody().get().getStatements().isNonEmpty()) {
                    for(Statement bs:n.getBody().get().getStatements()) {

                        b .append( bs.toString()+"\n");
                    }
                }
            }


            StringBuffer body=new StringBuffer();
            body.append(n.getDeclarationAsString());
            body.append("{");
            body.append(b.toString()+"}");
            methodBody.add(body.toString());
//            System.out.println("body==="+n.getDeclarationAsString().trim()+sb.toString());

            //getParents(n);

            super.visit(n, arg);
        }

        @Override
        public void visit(VariableDeclarationExpr n, Void arg) {
            //System.out.println(n.getVariables());
            String data = n.getVariables().toString();
            String[] set = data.substring(1, data.length() - 1).split("=");
            varibleName.add(set[0].trim());
            variableMap.put(set[0].trim().toString(), n);


            super.visit(n, arg);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
//            System.out.println("class:"+n.getName());
//            System.out.println("extends:"+n.getExtendedTypes());
//            System.out.println("implements:"+n.getImplementedTypes());
            List<String> classInfo = new ArrayList<>();
            classInfo.add(n.getExtendedTypes().toString().replace("[", "").replace("]", ""));
            classInfo.add(n.getImplementedTypes().toString().replace("[", "").replace("]", ""));


            if (n.isInnerClass()) {
                classInfo.add(n.getParentNode().toString());
            }


            classMap.put(n.getName().toString(), classInfo);

            super.visit(n, arg);
        }

        @Override
        public void visit(PackageDeclaration n, Void arg) {

            super.visit(n, arg);
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
//            System.out.println("MethodCallExpr:"+n.getNameAsString());
//            System.out.println("parent:"+getParents(n));
            methodCall.add(n.getNameAsString());

            if (methodCallMap.get(n.getNameAsString()) == null) {
                methodCallMap.put(n.getNameAsString(), getParents(n));
//                System.out.println("map:"+methodCallMap.get(n.getNameAsString()));
            } else {
                //多个父类
                methodCallMap.put(n.getNameAsString(), methodCallMap.get(n.getNameAsString()) + "@@@@" + (getParents(n)));
//                System.out.println("map:"+methodCallMap.get(n.getNameAsString()));
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(NameExpr n, Void arg) {
            //System.out.println("NameExpr:"+n.getName());
            getParents(n);

            callSet.add(n.getNameAsString());
            //就是函数中调用的东西
            if (nameExprMap.get(n.getNameAsString()) == null) {
                nameExprMap.put(n.getNameAsString(), getParents(n));
            } else {
                //多个父类
                nameExprMap.put(n.getNameAsString(), nameExprMap.get(n.getNameAsString()) + "@@@@" + (getParents(n)));
            }
            super.visit(n, arg);
        }


    }
}
