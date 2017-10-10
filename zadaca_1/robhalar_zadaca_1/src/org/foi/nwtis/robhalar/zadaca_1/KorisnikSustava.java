package org.foi.nwtis.robhalar.zadaca_1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KorisnikSustava
{
    //-prikaz -server datoteka
    static String prikazArgsPattern = "^-(prikaz) -server (((([A-Za-z]:\\\\)?([A-Za-z0-9_\\-]+\\\\)*)|" +
                                        "(http://(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                  "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                  "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                  "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/)|" +
                                                  "(([A-Za-z0-9_\\-]+/)*))))" +
                                                  "(([a-zA-Z0-9_\\.\\-/]+)((\\.bin)|(\\.ser))*))$";
    
        
    //-admin -s [ipadresa | adresa] -port port -u korisnik -p lozinka [-pause | -start | -stop | -stat ]
    static String adminArgsPattern = "^-(admin) -server (((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                         "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))|(([a-zA-z0-9][.]?){1,})) " +
                                                         "-port ((8[0-9]{3})|(9[0-9]{3})) " +
                                                         "-u ([a-zA-Z0-9\\-_]+) " +
                                                         "-p ([a-zA-Z0-9\\-_!#]+)" +
                                                         " \\-((pause)|(start)|(stop)|(stat))$";
    
    //-korisnik -server [ipadresa | adresa] -port port -u korisnik [[-a | -t] URL] | [-w nnn]
    static String userArgsPattern = "^-(korisnik) -server (((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                           "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                           "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                                                           "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))|(([a-zA-z0-9][.]?){1,})) " +
                                                           "-port ((8[0-9]{3})|(9[0-9]{3})) " +
                                                           "-u ([a-zA-Z0-9\\-_]+)\\s? " +
                                                           "(((-a|-t) (http://((([^/?#]*))?([^?#]*)(\\\\?([^#]*))?(#(.*))?)))" +
                                                           "|((-w) ([1-9][0-9]?[0-9]?)))$";

    private static String[]
    matchRegExString(String pattern, String string)
    {   
        String[] result;
        
        Pattern patern = Pattern.compile(pattern);
        Matcher matcher = patern.matcher(string);
        if(matcher.matches())
        {
            switch(matcher.group(1))
            {
                case "admin":
                    result = new String[5];
                    result[0] = matcher.group(2);
                    result[1] = matcher.group(10);
                    result[2] = matcher.group(13);
                    result[3] = matcher.group(14);
                    result[4] = matcher.group(15);
                    break;
                case "korisnik":
                    result = new String[5];
                    result[0] = matcher.group(2);
                    result[1] = matcher.group(10);
                    result[2] = matcher.group(13);
                    
                    if(matcher.group(15) != null)
                    {
                        result[3] = matcher.group(16);
                        result[4] = matcher.group(17);
                    }
                    else
                    {
                        result[3] = matcher.group(27);
                        result[4] = matcher.group(28);
                    }
                    break;
                case "prikaz":
                    result = new String[1];
                    result[0] = matcher.group(2);
                    break;
                default:
                    result = null;
            }
        }
        else
            result = null;
        
        return result;
    }

    private static void printUsage()
    {
        System.out.println("Usage: KorisnikSustava -korisnik -server [<ipadresa> | <adresa>] -port <port> -u <korisnik> [[-a | -t] <URL>] | [-w <nnn>]");
        System.out.println("Usage: KorisnikSustava -admin -server [<ipadresa> | <adresa>] -port <port> -u <korisnik> -p <lozinka> [-pause | -start | -stop | -stat ]");
        System.out.println("Usage: KorisnikSustava -prikaz -server [<filepath> | <URL>]");
    }
    
    public static void main(String[] args)
    {
        if(args.length < 1)
       {
           printUsage();
           return;
       }
        
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args)
        {
            stringBuilder.append(arg).append(" ");
        }
        String argumentsString = stringBuilder.toString().trim();
        
        String[] arguments;
        switch(args[0])
        {
           case "-admin":
                arguments = matchRegExString(adminArgsPattern, argumentsString);
                if(arguments != null)
                {
                    AdministratorSustava administratorSustava = new AdministratorSustava(arguments);
                    administratorSustava.start();
                }
                break;
            case "-korisnik":
                arguments = matchRegExString(userArgsPattern, argumentsString);
                if(arguments != null)
                {
                    KlijentSustava klijentSustava = new KlijentSustava(arguments);
                    klijentSustava.start();
                }
                break;
            case "-prikaz":
                arguments = matchRegExString(prikazArgsPattern, argumentsString);
                if(arguments != null)
                {
                    PregledSustava pregledSustava = new PregledSustava(arguments);
                    pregledSustava.start();
                }
                break;
            
            default:
                printUsage();
        }
    }
}
