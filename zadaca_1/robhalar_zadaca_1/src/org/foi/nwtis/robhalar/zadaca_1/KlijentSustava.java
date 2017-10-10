package org.foi.nwtis.robhalar.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KlijentSustava
{
    public String serverHandle;
    public String port;
    public String user;
    public String command;
    public String commandArg;

    public KlijentSustava(String[] arguments)
    {
        this.serverHandle = arguments[0];
        this.port = arguments[1];
        this.user = arguments[2];
        
        String commandString = arguments[3];
        switch(commandString)
        {
            case "-a":
                this.command = "ADD ";
                this.commandArg = arguments[4];
                break;
            case "-t":
                this.command = "TEST ";
                this.commandArg = arguments[4];
                break;
            case "-w":
                this.command  = "WAIT ";
                long miliSecs = Integer.parseInt(arguments[4]) * 1000;
                this.commandArg =  Long.toString(miliSecs);
                break;
        }
    }

    public void
    start()
    {
        try
        {
            Socket socket = new Socket(serverHandle, Integer.parseInt(port));

            OutputStream clientOutputStream = socket.getOutputStream();
            InputStream serverResponseStream = socket.getInputStream();
            
            StringBuilder inputString = new StringBuilder();
            inputString.append("USER ").append(user).append("; ");
            inputString.append(command).append(commandArg).append(";");

            clientOutputStream.write(inputString.toString().getBytes());
            socket.shutdownOutput();
            
            StringBuilder builder = new StringBuilder();
            byte inputByte;
            while( (inputByte = (byte)serverResponseStream.read()) != -1)
            {
                builder.append((char)inputByte);
            }
            String serverResponse = builder.toString();
            System.out.println(serverResponse);
            
            
        } catch (IOException exception)
        {
            Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, exception);
        }
    }

}
