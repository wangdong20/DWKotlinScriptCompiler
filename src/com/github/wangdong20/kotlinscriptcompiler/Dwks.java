package com.github.wangdong20.kotlinscriptcompiler;

import com.github.wangdong20.kotlinscriptcompiler.codegen.CodeGenerator;
import com.github.wangdong20.kotlinscriptcompiler.parser.Parser;
import com.github.wangdong20.kotlinscriptcompiler.parser.Program;
import com.github.wangdong20.kotlinscriptcompiler.token.Token;
import com.github.wangdong20.kotlinscriptcompiler.token.Tokenizer;
import com.github.wangdong20.kotlinscriptcompiler.typechecker.Typechecker;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Dwks {

    private static void welcome() {
        System.out.println("This is DwKotlinScriptCompiler written by Dong Wang. This compiler is based on JVM");
        System.out.println("Please use dwks path/src.ks to compile source code file with suffix .ks");
        System.out.println("Then you can use java src to run the program in JVM");
        System.out.println("Type quit to quit this compiler program\n");
    }

    private static String readKsToString(String fileNameWithPath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileNameWithPath));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        // 删除最后一个新行分隔符
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        return stringBuilder.toString();
    }

    private static void compileSourceCode(String fileNameWithPath) {
        try {
            File file = new File(fileNameWithPath.trim());
            String fileName = file.getName();
            String input = readKsToString(fileNameWithPath);
            Tokenizer tokenizer = new Tokenizer(input);
            List<Token> tokenList = tokenizer.tokenize();
            Token[] tokens = new Token[tokenList.size()];
            tokenList.toArray(tokens);
            Parser parser = new Parser(tokens);
            Program program = parser.parseToplevelProgram();
            Typechecker.typecheckProgram(program);
            CodeGenerator codeGenerator = new CodeGenerator(fileName.substring(0, fileName.lastIndexOf('.')), "compiledProgram");
            codeGenerator.writeProgram(program);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void runProgram(String currentClassName) {
        final ProcessBuilder builder = new ProcessBuilder("java", currentClassName);
        builder.redirectErrorStream(true);
        Process process = null;
        try {
            process = builder.start();
            readAndPrint(new BufferedReader(new InputStreamReader(process.getInputStream())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(process != null) {
                try {
                    process.getErrorStream().close();
                    process.getOutputStream().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void readAndPrint(final BufferedReader reader) throws IOException {
        try {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                System.out.println(currentLine);
            }
        } finally {
            reader.close();
        }
    }

    public static void main(String[] args) {
        welcome();
        Scanner sc = new Scanner(System.in);
        String command;
//        System.out.print("~ DwKotlinScriptCompiler$ ");
        while(!(command = sc.nextLine()).equals("quit")) {
//            System.out.print("~ DwKotlinScriptCompiler$ ");
            String[] a = command.split("\\s+");
            if(a.length == 2 && a[0].equals("dwks")) {
                compileSourceCode(a[1]);
            }

            if(a.length == 2 && a[0].equals("java")) {
                runProgram(a[1]);
            }
        }
    }

}
