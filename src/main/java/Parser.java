import io.swagger.models.HttpMethod;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.Swagger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

class Parser {

    public static void main(String[] args) throws IOException
    {
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Enter file name: ");
        String filename = keyboard.nextLine();

        Swagger swagger = new SwaggerParser().read(filename);

        String template = ("package oci\n\n" +
                "import io.gatling.core.Predef._\n" +
                "import io.gatling.http.Predef._\n" +
                "import scala.concurrent.duration._\n\n" +
                "class TestSimulation extends Simulation {\n\n" +
                "val httpConf = http\n" +
                "\t.baseURL(" + swagger.getBasePath() + ")\n");

        StringBuilder template2 = new StringBuilder();
        for (String path : swagger.getPaths().keySet()) {
            for (HttpMethod method : swagger.getPath(path).getOperationMap().keySet()) {
                String opName = "\"." + method.toString() + ":" + path + "\"";
                template2.append("\t.exec(http(").append(opName).append("))\n");
                String pathUrl = "\"" + path + "\"";
                template2.append("\t." + method.toString().toLowerCase()).append("(").append(pathUrl).append("))\n");
            }
        }
        String template3 = "setUp(scn.inject(atOnceUsers(5).protocols(httpConf))\n";
        
        StringBuilder finalText = new StringBuilder();
        finalText.append(template).append(template2).append(template3).append("\n}");
        String output = finalText.toString();
        BufferedWriter bw = new BufferedWriter(new FileWriter("TestSimulation.scala"));
        bw.write(output);
        bw.close();
    }
}