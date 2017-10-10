package org.foi.nwtis.robhalar.zadaca_1;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdministratorSustava
{
    String serverHandle;
    String port;
    String user;    
    String password;
    String command;
    
    public AdministratorSustava(String[] arguments)
    {
        this.serverHandle = arguments[0];
        this.port = arguments[1];
        this.user = arguments[2];
        this.password = arguments[3];
        
        String commandString = arguments[4];
        this.command = commandString.toUpperCase(); 
    }

    public static Object deserialize(byte[] data)
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is;
        try
        {
            is = new ObjectInputStream(in);
            return is.readObject();

        } catch (IOException ex)
        {
            Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(AdministratorSustava.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public void start()
    {
        try
        {
            Socket socket = new Socket(serverHandle, Integer.parseInt(port));

            OutputStream clientOutputStream = socket.getOutputStream();
            InputStream serverResponseStream = socket.getInputStream();
            
            StringBuilder inputString = new StringBuilder();
            inputString.append("USER ")  .append(user)    .append("; ");
            inputString.append("PASSWD ").append(password).append("; ");
            inputString.append(command)                   .append(";");

            clientOutputStream.write(inputString.toString().getBytes());
            socket.shutdownOutput();
            
            StringBuilder builder = new StringBuilder();
            byte inputByte;
            if(command.equals("STAT"))
            {
                while((inputByte = (byte)serverResponseStream.read()) != -1)
                {
                    if((char)inputByte == '\r')
                        if((char)serverResponseStream.read() == '\n')
                        {
                            String response = builder.toString().trim();
                            System.out.println(response);
                            
                            Pattern pattern = Pattern.compile("^OK; LENGTH ([1-9][0-9]+)");
                            Matcher matcher = pattern.matcher(response);

                            if(matcher.matches())
                            {
                                int size = Integer.parseInt(matcher.group(1));
                                byte[] serializedData = new byte[size];
                                if(serverResponseStream.read(serializedData) == size)
                                {
                                    Object readObject = deserialize(serializedData);
                                    if(readObject != null && readObject instanceof Evidencija)
                                    {
                                        ((Evidencija)readObject).printEvidencija();
                                        break;
                                    }
                                }
                            }
                            else
                            {
                                break;
                            }
                        }
                    
                    builder.append((char)inputByte);
                }
            }
            else
            {
                while( (inputByte = (byte)serverResponseStream.read()) != -1)
                {
                    builder.append((char)inputByte);
                }
                String serverResponse = builder.toString();
                System.out.println(serverResponse);
            }
         
            socket.close();
            
        } catch (IOException exception)
        {
            Logger.getLogger(KlijentSustava.class.getName()).log(Level.SEVERE, null, exception);
        }
    }
}
