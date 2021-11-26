/**
 * WebServer.java
 * @author Annie Abhay and Sophanna Ngov
 * */

package http.server;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Launch a web server
 */
public class WebServer {



  /**
   * main of WebServer.java
   * @param args includes ip address and port
   */
  public static void main(String args[]) {
    WebServer ws = new WebServer();
    ws.start(Integer.parseInt(args[0]));
  }

  /**
   * starts the web server
   */
  protected void start(int port) {
    ServerSocket s;

    System.out.println("Webserver starting up on port 80");
    System.out.println("(press ctrl-c to exit)");
    try {
      // create the main server socket
      s = new ServerSocket(port);
    } catch (Exception e) {
      System.out.println("Error: " + e);
      return;
    }

    System.out.println("Waiting for connection");
    for (; ; ) {
      PrintWriter out = null;
      try {
        // wait for a connection
        Socket remote = s.accept();
        // remote is now the connected socket
        System.out.println("Connection, sending data.");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                remote.getInputStream()));
        out = new PrintWriter(remote.getOutputStream());

        // read the data sent. We basically ignore it,
        // stop reading once a blank line is hit. This
        // blank line signals the end of the client HTTP
        // headers.
        String str = ".";
        String request = "";
        int lengthContent = 0;
        while (str != null && !str.equals("")) {
          str = in.readLine();
          request += str + '\n';
          System.out.println(str);
          if (str.contains("Content-Length")){
            lengthContent = Integer.parseInt(str.split(" ",2)[1]);
          }
        }

        //------------------------------------------REQUEST GET---------------------------------------------
        if (request.startsWith("GET ")) {
          //remove the '/' and get the url
          String url = request.split(" ", 3)[1].substring(1);
          File file = new File(url);
          out.println();
          // Send the response
          if (!file.exists()) {
            out.println("HTTP/1.0 404 FILE NOT FOUND");
            out.println("");
          } else if (url.contains(".txt")){
            out.println("HTTP/1.0 403 FORBIDDEN");
            out.println("");
          } else {
            BufferedOutputStream buffOutputStream = new BufferedOutputStream(remote.getOutputStream());
            out.println("HTTP/1.0 200 OK");
            out.println("Server: Bot");
            //we ignore the icon request
            if (url.equals("favicon.ico")) {
              continue;
            }
            addContentType(url, out);
            // this blank line signals the end of the headers
            out.println("");
            out.flush();
            // Send the HTML page
            Files.copy(file.toPath(), buffOutputStream);
            buffOutputStream.close();
          }

          //------------------------------------------REQUEST POST---------------------------------------------
        } else if (request.startsWith("POST ")) { // Status 200 = ok ; Status 201 = Created
          String url = request.split(" ", 3)[1].substring(1);
          char[] data = new char[lengthContent];
          in.read(data,0,lengthContent);
          System.out.println(data);
          File dataFile = new File(url);
          BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true));
          writer.append(String.valueOf(data)+"\n");

          writer.close();
          if(dataFile.exists()){
            out.println("HTTP/1.0 200 OK");
          }else{
            out.println("HTTP/1.0 201 FILE CREATED");
          }
          out.println("Server: Bot");
          out.println("Content-Type: text/html");
          out.println("");
          out.println("<H3>Suscribed !</H3>");

          //------------------------------------------REQUEST HEAD---------------------------------------------
        } else if (request.startsWith("HEAD ")) {

          //remove the '/' and get the url
          String url = request.split(" ", 3)[1].substring(1);
          if (!url.equals("favicon.ico")) {
            out.println();
            // Send the response
            // Send the headers
            out.println("HTTP/1.0 200 OK");
            addContentType(url, out);
            out.println("Server: Bot");
            out.println("");

          }
          //------------------------------------------REQUEST PUT---------------------------------------------
        } else if (request.startsWith("PUT ")) { //200 Status ok or 204 Status No content , 201 Created
          String url = request.split(" ", 3)[1].substring(1);

          char[] data = new char[lengthContent];
          in.read(data,0,lengthContent);
          System.out.println(data);
          File dataFile = new File(url);

          try{
            if(dataFile.exists()) {
              //clear the old file
              FileWriter fw = new FileWriter(dataFile, false);
              PrintWriter pw = new PrintWriter(fw, false);
              pw.flush();
              pw.close();
              fw.close();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true));
            writer.append(String.valueOf(data) + "\n");
            writer.close();
            if(dataFile.exists()){
              out.println("HTTP/1.0 200 OK");
            }else{
              out.println("HTTP/1.0 201 FILE CREATED");
            }
            out.println("Server: Bot");
            out.println("Content-Type: text/html");
            out.println("");
            out.println("<H3>Suscribed !</H3>");

          }catch(Exception exception){
            System.out.println(exception);
            out.println("HTTP/1.0 500 INTERNAL SERVER ERROR");
            out.println("");
          }

          //------------------------------------------REQUEST DELETE---------------------------------------------
        } else if (request.startsWith("DELETE ")) { //200(OK) includes message body or 204(NO CONTENT) completed but no body

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
              if ((fileExist = fileToDelete.exists())) {
                deleteSuccess = fileToDelete.delete();
              }
              // Send the headers
              if (deleteSuccess) {
                out.println("HTTP/1.0 204 NO CONTENT"); // didn't read the file, I just deleted it
                addContentType(url, out);
                out.println("Server: Bot");
                out.println("");
              } else if (!fileExist) {
                out.println("HTTP/1.0 404 FILE NOT FOUND");
              } else {
                out.println("HTTP/1.0 401 UNAUTHORIZED");
              }

            } catch (Exception e) {
              System.out.println(e);
              out.println("HTTP/1.0 500 INTERNAL SERVER ERROR");
              out.println("");
            }
            // Send the rest of the headers
            out.println("");

          }
        } else {
          out.println("HTTP/1.0 400 BAD REQUEST");
          out.println("");
        }

        out.flush();
        remote.close();
      } catch (Exception e) {
        out.println("HTTP/1.0 500 INTERNAL SERVER ERROR");
        out.println("");
        System.out.println("Error: " + e);
      }
    }
  }

  /**
   * add the Content-Type line on the header
   * @param url the url or the path of the file
   * @param out the Print writer where we send the information
   */
  public void addContentType(String url,PrintWriter out){
      if (url.contains(".html")) {
        out.println("Content-Type: text/html");
      } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png") || url.contains(".gif")) {
        out.println("Content-Type: image/" + url.substring(url.lastIndexOf(".") + 1));
      } else if (url.contains(".pdf")) {
        out.println("Content-Type: application/pdf");
      } else if (url.contains(".wav")) {
        out.println("Content-Type: audio/wav");
      } else if (url.contains(".mp3")) {
        out.println("Content-Type: audio/mpeg");
      } else if (url.contains(".mp4") || url.contains(".mpeg")) {
        out.println("Content-Type: video/" + url.substring(url.lastIndexOf(".") + 1));
      }
  }
}
