import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

class Parser {

    public static Scanner keyboard = new Scanner(System.in);

    public static void main(String[] args) throws IOException {

        if (args.length != 3) {
            System.out.println("\n***Expected 3 args: inputFile.json outFile.scala numSimUsers***\n");
           String in = getInputFile();
           String out = getOutputFile();
           int num = getNumber();
            writeFile(buildString(new File(in), num),
                    new File(out));
        }

        if (args.length == 3) {
            System.out.println("Checking your args");
            if (!checkInputFile(args[0])) {
                getInputFile();
            }
            if (!checkOutputFile(args[1])) {
                getOutputFile();
            }
            if (!checkNumber(Integer.parseInt(args[2]))) {
                getNumber();
            }
            writeFile(buildString(new File(args[0]), Integer.parseInt(args[2])),
                    new File(args[1]));
        }

    }

    public static String getInputFile () throws IOException {
        String input = " ";
        System.out.print("Enter input file name to be parsed: ");
        input = keyboard.nextLine();
        while (!checkInputFile(input)) {
            System.out.print("Enter input file name to be parsed: ");
            input = keyboard.nextLine();
        }
        return input;
    }

    public static boolean checkInputFile(String in) throws IOException {
        try {FileReader fileCheck = new FileReader(in);
            return fileCheck.ready();}
        catch(FileNotFoundException f) {
            System.out.println("Couldn't find that one");
            return false;
            }
    }

    public static String getOutputFile() throws IOException {
        String input = " ";
        System.out.print("\nEnter output file name to be created: ");
        input = keyboard.nextLine();
        while (!checkOutputFile(input)) {
            System.out.print("Enter output file name to be created: ");
            input = keyboard.nextLine();
        }
        return input;
    }

    public static boolean checkOutputFile(String out) throws IOException {
        try {FileReader fileCheck = new FileReader(out);

            if(fileCheck.ready()) {
                System.out.println("There's already a Scala file with this name.");
                System.out.println("Press Y/y to replace it or any other character to input new filename: ");
                return keyboard.next().equalsIgnoreCase("y");
            }
        }
            catch(FileNotFoundException e) {
                System.out.println("Creating a new file");
                return true;
            }

        return true;
    }

    public static int getNumber() throws IOException {
        int input = -1;
        while(!checkNumber(input)) {
            System.out.print("Enter number of users to simulate (0-100K): ");
            try{input = keyboard.nextInt();}
            catch(InputMismatchException f) {
                System.out.println("Those weren't numbers");
            }
        }
        return input;
    }

    public static boolean checkNumber(int n) {
        return (n > 0) && (n < 100000);
    }

    public static String buildString(File inFile, int numberUsers) {

        Swagger swagger = new SwaggerParser().read(String.valueOf(inFile));

        StringBuilder header = new StringBuilder(),
                body = new StringBuilder(),
                tail = new StringBuilder(),
                fullText = new StringBuilder();

                header.append("package oci\n\n").append("import io.gatling.core.Predef._\n")
                .append("import io.gatling.http.Predef._\n")
                .append("import scala.concurrent.duration._\n\n")
                .append("class TestSimulation extends Simulation {\n\n")
                .append("val httpConf = http\n")
                .append("\t.baseURL(")
                 .append(swagger.getBasePath())
                .append( ")\n");

        for (String path : swagger.getPaths().keySet()) {
            for (HttpMethod method : swagger.getPath(path).getOperationMap().keySet()) {

                StringBuilder operationName = new StringBuilder(),
                pathURL = new StringBuilder();
                operationName.append("\".")
                        .append(method.toString())
                        .append(":").append(path)
                        .append("\"");
                body.append("\t.exec(http(")
                        .append(operationName)
                        .append("))\n");
                pathURL.append("\"")
                        .append(path)
                        .append("\"");
                body.append("\t.")
                        .append(method.toString().toLowerCase())
                        .append("(")
                        .append(pathURL)
                        .append("))\n");
            }
        }

        tail.append("setUp(scn.inject(atOnceUsers(")
                .append(Integer.toString(numberUsers))
                .append(").protocols(httpConf))\n");

        fullText.append(header)
                .append(body)
                .append(tail)
                .append("\n}");

        return fullText.toString();
    }

    public static void writeFile(String generatedScala, File outFile) throws IOException {
        FileWriter fw = new FileWriter(outFile,false);
        fw.write(generatedScala);
        fw.close();
        System.out.println("File " + outFile.toString() + " has been created.");
    }
}

