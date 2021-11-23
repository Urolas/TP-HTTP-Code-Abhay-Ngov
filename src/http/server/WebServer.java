///A Simple Web Server (WebServer.java)

package http.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

  /**
   * WebServer constructor.
   */
  protected void start() {
    ServerSocket s;

    System.out.println("Webserver starting up on port 80");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(80);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (;;) {
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
            remote.getInputStream()));
        PrintWriter out = new PrintWriter(remote.getOutputStream());

        // read the data sent. We basically ignore it,
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        String request = "";
        while (str != null && !str.equals("")){
          str = in.readLine();
          request+=str+'\n';
        }

        if(request.startsWith("GET ")){
          //remove the '/' and get the url
          String url = request.split(" ",3)[1].substring(1);
          if (!url.equals("favicon.ico")){
            Path fileName = Path.of(url);
            String actual = Files.readString(fileName);
            out.println();
            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            // this blank line signals the end of the headers
            out.println("");
            // Send the HTML page
            out.println(actual);
            System.out.println(actual);
          }
        }else if(request.startsWith("POST ")) {

          String content = "";
          String strParser = ".";
          while (strParser != null && !strParser.equals("")) {
            strParser = in.readLine();
            content += strParser + '\n';
          }
          System.out.println(content);
          String url = request.split(" ", 3)[1].substring(1);

        }else if(request.startsWith("HEAD ")) {

          //remove the '/' and get the url
          String url = request.split(" ", 3)[1].substring(1);
          if (!url.equals("favicon.ico")) {
            out.println();
            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            out.println("");

          }

        }else if(request.startsWith("PUT ")) {


        }else if(request.startsWith("DELETE ")){ //200(OK) includes message body ,202(ACCEPTED) not yet performed, 204(NO CONTENT) completed but no body

          //remove the '/' and get the url
          String url = request.split(" ", 3)[1].substring(1);
          Path fileName = Path.of(url);
          if (!url.equals("favicon.ico")) {
            out.println();
            // Send the response

            try {
              File fileToDelete = new File(String.valueOf(fileName));

              // Delete file
              boolean deleteSuccess = false;
              boolean fileExist = false;

              if((fileExist = fileToDelete.exists())) {
                deleteSuccess = fileToDelete.delete();
              }

              // Send the headers
              if(deleteSuccess) {
                out.write("HTTP/1.0 204 NO CONTENT");
              } else if (!fileExist) {
                out.write("HTTP/1.0 404 FILE NOT FOUND");
              } else {
                out.write("HTTP/1.0 403 FORBIDDEN");
              }
              out.flush();

            } catch (Exception e) {
              System.out.println(e);
            }

            // Send the rest of the headers
            out.println("Content-Type: text/html");
            out.println("Server: Bot");
            out.println("");

          }


        }else{
          out.println("HTTP/1.0 400");
          out.println("");
        }

        out.flush();
        remote.close();
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }


  /**
   * Start the application.
   * 
   * @param args
   *            Command line parameters are not used.
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start();
  }
}
