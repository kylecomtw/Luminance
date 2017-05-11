/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import py4j.GatewayServer;


/**
 *
 * @author Sean
 */
public class Py4jMain {    
    public Luminance GetInst(String index_dir){
        try {
            return new Luminance(index_dir);
        } catch (IOException ex) {
            Logger.getLogger(Py4jMain.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static void main(String[] argv) throws IOException {
        Py4jMain app = new Py4jMain();
        GatewayServer server = new GatewayServer(app);
        System.out.println();
        Writer writer = new PrintWriter(System.out);
        writer.write("Py4j Java gateway running....\n");
        writer.write("Python Interface: \n");
        writer.write("from py4j.java_gateway import JavaGateway\n");
        writer.write("L = JavaGateway().entry_point.GetInst()\n");
        writer.write("\n");
        writer.close();
        server.start();
        // String json = app.match("美國(N)/派出(V)/艦隊(N)/前往(V)/朝鮮半島(Nd)/。(CATEGORY)", "<V> NP:<N*>");
        // System.out.println(json);
    }
}
