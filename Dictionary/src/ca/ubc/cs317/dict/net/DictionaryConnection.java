package ca.ubc.cs317.dict.net;

import ca.ubc.cs317.dict.model.Database;
import ca.ubc.cs317.dict.model.Definition;
import ca.ubc.cs317.dict.model.MatchingStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

/**
 * Created by Jonatan on 2017-09-09.
 */
public class DictionaryConnection {

    private static final int DEFAULT_PORT = 2628;

    Socket DictSocket;
    PrintWriter DictOut;
    BufferedReader DictIn;

    /** Establishes a new connection with a DICT server using an explicit host and port number, and handles initial
     * welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @param port Port number used by the DICT server
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     * don't match their expected value.
     */
    public DictionaryConnection(String host, int port) throws DictConnectionException {

        try {
            this.DictSocket = new Socket(host, port);                                       //Initializing Socket object called DictSocket
            this.DictOut = new PrintWriter(DictSocket.getOutputStream(), true);    //Initializing Socket output to object DictOut
            this.DictIn = new BufferedReader(new InputStreamReader(DictSocket.getInputStream()));//Initializing Socket input to object DictIn

            //DictIn = Server Output | DictOut = Server Input

            //Checking connection
            String Welcomemsg[] = this.DictIn.readLine().split(" ",2);
            if(!Welcomemsg[0].equals("220")){
                throw new DictConnectionException("Connection not established");
            }

            //Testing stuff
//            this.DictOut.println("SHOW DB");
//            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);

//            String OneLine[] = this.DictIn.readLine().split(" ", 2);
//            System.out.println(OneLine[0]);
//            OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1); //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
//            System.out.println(OneLine[1]);


//            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
//                String OneLine[] = this.DictIn.readLine().split(" ", 2);
//                System.out.println(OneLine[0]);
//                OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1); //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
//                System.out.println(OneLine[1]);
//            }

//            this.DictOut.println("SHOW STRAT");                                         //Sending command
//            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);   //This stores the first line of response
//            if(!ResponseCode[0].equals("111")){                                         //Making sure we get a valid response
//                throw new DictConnectionException("Message does not match expected value.");
//            }
//            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
//                String OneLine[] = this.DictIn.readLine().split(" ", 2);
//                System.out.println(OneLine[0]);
//                OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1); //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
//                System.out.println(OneLine[1]);
//            }
//
//            this.DictOut.println("DEFINE wn hello");
//            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);
//            if(ResponseCode[0].equals("552")){
//                                                                                       //Returning empty set if no match found.
//            }
//            if(!ResponseCode[0].equals("150")){                                         //Making sure we get a valid response
//                throw new DictConnectionException("Message does not match expected value.");
//            }
//            String DefinitionResponseCode[] = this.DictIn.readLine().split(" ", 4);
//            DefinitionResponseCode[1] = DefinitionResponseCode[1].substring(1, DefinitionResponseCode[1].length() - 1);     //word as put in database
//            //Creating definition object
//            //Definition ThisDefinition = new Definition(DefinitionResponseCode[1],DefinitionResponseCode[2]);
//            //Reading Definition
//            String DefinitionSingleLine = this.DictIn.readLine();
//            String DefinitionBody = "";
//            while(!DefinitionSingleLine.equals(".")) {
//                DefinitionBody += DefinitionSingleLine;
//                DefinitionBody += "\n";                                                  // Took this from https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
//                DefinitionSingleLine = this.DictIn.readLine();
//            }
//            this.DictIn.readLine();
//
//            System.out.println(DefinitionBody);


        } catch(Exception e) {
            throw new DictConnectionException("Connection cannot be established.");
        }
    }

    /** Establishes a new connection with a DICT server using an explicit host, with the default DICT port number, and
     * handles initial welcome messages.
     *
     * @param host Name of the host where the DICT server is running
     * @throws DictConnectionException If the host does not exist, the connection can't be established, or the messages
     * don't match their expected value.
     */
    public DictionaryConnection(String host) throws DictConnectionException {
        this(host, DEFAULT_PORT);
    }

    /** Sends the final QUIT message and closes the connection with the server. This function ignores any exception that
     * may happen while sending the message, receiving its reply, or closing the connection.
     *
     */
    public synchronized void close() {
        try {
            this.DictOut.println("QUIT");
            this.DictIn.readLine(); //Not doing anything since we must close the connection even if it gives bad response code
            this.DictSocket.close();
        } catch(Exception e)
        {}
    }

    /** Requests and retrieves all definitions for a specific word.
     *
     * @param word The word whose definition is to be retrieved.
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 definitions in the first database that has a definition for the word should be used
     *                 (database '!').
     * @return A collection of Definition objects containing all definitions returned by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Collection<Definition> getDefinitions(String word, Database database) throws DictConnectionException {
        Collection<Definition> set = new ArrayList<>();

        String command = "DEFINE "+database.getName()+" \""+word+"\"";

        try{
            this.DictOut.println(command);
            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);
            if(ResponseCode[0].equals("552") || ResponseCode[0].equals("550")){
                return set;                                                             //Returning empty set if no match found.
            }
            if(!ResponseCode[0].equals("150")){                                         //Making sure we get a valid response
                throw new DictConnectionException("Message does not match expected value.");
            }
            //Looping for each Definition
            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
                String DefinitionResponseCode[] = this.DictIn.readLine().split("\"", 4);                        //Breaking into 4 substrings seperated by "
                DefinitionResponseCode[2] = DefinitionResponseCode[2].substring(1, DefinitionResponseCode[2].length() - 1); //Removing first and last space from databse
                //Creating definition object
                Definition ThisDefinition = new Definition(DefinitionResponseCode[1],DefinitionResponseCode[2]);
                //Reading Definition
                String DefinitionSingleLine = this.DictIn.readLine();
                String DefinitionBody = "";
                while(!DefinitionSingleLine.equals(".")) {
                    DefinitionBody += DefinitionSingleLine;
                    DefinitionBody += "\n";                                                  // Took this from https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
                    DefinitionSingleLine = this.DictIn.readLine();
                }
                ThisDefinition.appendDefinition(DefinitionBody);
                set.add(ThisDefinition);
            }
            this.DictIn.readLine();
        } catch(Exception e) {
            throw new DictConnectionException("Inside Define");
        }
        return set;
    }

    /** Requests and retrieves a list of matches for a specific word pattern.
     *
     * @param word     The word whose definition is to be retrieved.
     * @param strategy The strategy to be used to retrieve the list of matches (e.g., prefix, exact).
     * @param database The database to be used to retrieve the definition. A special database may be specified,
     *                 indicating either that all regular databases should be used (database name '*'), or that only
     *                 matches in the first database that has a match for the word should be used (database '!').
     * @return A set of word matches returned by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Set<String> getMatchList(String word, MatchingStrategy strategy, Database database) throws DictConnectionException {
        Set<String> set = new LinkedHashSet<>();

        String command = "MATCH "+database.getName()+" "+strategy.getName()+" \""+word+"\"";
        //System.out.println(command);

        try{
            this.DictOut.println(command);
            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);   //This stores the first line of response
            if(ResponseCode[0].equals("552") || ResponseCode[0].equals("551") || ResponseCode[0].equals("550")){
                return set;                                                             //Returning empty set if no match found.
            }
            if(!ResponseCode[0].equals("152")){                                         //Making sure we get a valid response
                throw new DictConnectionException("Message does not match expected value.");
            }
            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
                String OneLine[] = this.DictIn.readLine().split(" ", 2);
                OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1);          //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
                set.add(OneLine[1]);
            }
            this.DictIn.readLine();                                                     //This took the longest to figure out, doing twice to make sure I finish reading the response.
            this.DictIn.readLine();
        } catch(Exception e){
            throw new DictConnectionException("Error in matching");
        }
        return set;
    }

    /** Requests and retrieves a map of database name to an equivalent database object for all valid databases used in the server.
     *
     * @return A map of Database objects supported by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Map<String, Database> getDatabaseList() throws DictConnectionException {
        Map<String, Database> databaseMap = new HashMap<>();

        try {
            this.DictOut.println("SHOW DB");                                            //Sending command
            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);   //This stores the first line of response
            if(ResponseCode[0].equals("554")){                                          //Returning empty dataset
                return databaseMap;
            }
            if(!ResponseCode[0].equals("110")){                                         //Making sure we get a valid response
                throw new DictConnectionException("Message does not match expected value.");
            }
            //Making Database Objects and adding them to map, one line at a time.
            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
                String OneLine[] = this.DictIn.readLine().split(" ", 2);
                OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1);          //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
                Database ThisLinesDatabase = new Database(OneLine[0] , OneLine[1]);
                databaseMap.put(OneLine[0], ThisLinesDatabase);
            }
            this.DictIn.readLine();                                                     //This took the longest to figure out, doing twice to make sure I finish reading the response.
            this.DictIn.readLine();

        } catch (Exception e) {
            throw new DictConnectionException("Dictionary Connection Failed");
        }

        return databaseMap;
    }

    /** Requests and retrieves a list of all valid matching strategies supported by the server.
     *
     * @return A set of MatchingStrategy objects supported by the server.
     * @throws DictConnectionException If the connection was interrupted or the messages don't match their expected value.
     */
    public synchronized Set<MatchingStrategy> getStrategyList() throws DictConnectionException {
        Set<MatchingStrategy> set = new LinkedHashSet<>();

        try {
            this.DictOut.println("SHOW STRAT");                                         //Sending command
            String ResponseCode[] = this.DictIn.readLine().split(" ", 3);
            if(ResponseCode[0].equals("555")){                                         //Returning empty strategy
                return set;
            }
            if(!ResponseCode[0].equals("111")){                                         //Making sure we get a valid response
                throw new DictConnectionException("Message does not match expected value.");
            }
            //Making MatchingStrategy Objects and adding them to set, one line at a time.
            for(int i=0; i<Integer.parseInt(ResponseCode[1]) ; i++) {
                String OneLine[] = this.DictIn.readLine().split(" ", 2);
                OneLine[1] = OneLine[1].substring(1, OneLine[1].length() - 1);          //Disclosure, I took this from https://www.geeksforgeeks.org/remove-first-and-last-character-of-a-string-in-java/
                MatchingStrategy ThisLinesStrategy = new MatchingStrategy(OneLine[0] , OneLine[1]);
                set.add(ThisLinesStrategy);
            }
            this.DictIn.readLine();                                                     //This took the longest to figure out, doing twice to make sure I finish reading the response.
            this.DictIn.readLine();

        } catch (Exception e) {
            throw new DictConnectionException("Connection Failed");
        }

        return set;
    }
}
