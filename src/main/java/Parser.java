import io.swagger.models.HttpMethod;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.Swagger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class Parser {

    public static void main(String[] args) throws IOException
    {

        Swagger swagger = new SwaggerParser().read(
                "./kohls-swagger.json");

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
                template2.append(".\t" + method.toString().toLowerCase()).append(pathUrl).append("))\n");
            }
        }
        String template3 = "setUp(scn.inject(atOnceUsers(5).protocols(httpConf))\n";


        StringBuilder finalText = new StringBuilder();
        finalText.append(template).append(template2).append(template3);
        String output = finalText.toString();
        BufferedWriter bw = new BufferedWriter(new FileWriter("TestOutput.scala"));
        bw.write(output);
        bw.write("}");
        bw.close();
    }
}