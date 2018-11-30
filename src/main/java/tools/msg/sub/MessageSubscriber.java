/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.sub;

import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.Message;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManager;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManagerCallback;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to subscribe to the GMSEC bus for a given subject and write out the received
 * messages to file in json format.
 */
public class MessageSubscriber extends AbstractMessageService
{

    public MessageSubscriber(Options options)
    {
        this.options = options;
        init();
        run();
    }

    public void run()
    {
        try
        {
            // Output middleware client library version
            print("Using middleware client: "+cm.getLibraryVersion());

            si = cm.subscribe(options.getSubject(), new ConnectionManagerCallback()
            {
                public void onMessage(ConnectionManager cm, Message msg)
                {
                    // Get string value of the GMSEC message in JSON format
                    String message = options.getMsgFormat().equals("JSON") ? msg.toJSON() : msg.toXML();
                    // Print to console
                    print(message);
                    try
                    {
                        // Append message json string to file
                        FileWriter fw = new FileWriter("gmsec_messages.txt",true); //the true will append the new data
                        fw.write(message+"\n");//appends the string to the file
                        fw.close();
                    }
                    catch(IOException ioe)
                    {
                        print("IOException: " + ioe.getMessage());
                    }
                }
            });
            cm.startAutoDispatch();
            while(true) {
                Thread.sleep(100);
            }
        } catch (GMSEC_Exception | IllegalArgumentException | InterruptedException e)
        {
            print(e.getMessage());
        } finally {
            cleanup();
            System.exit(-1);
        }
    }
}
