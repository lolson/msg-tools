/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg;

import gov.nasa.gsfc.gmsec.gss.tools.msg.pub.*;
import gov.nasa.gsfc.gmsec.gss.tools.msg.sub.MessageSubscriber;
import gov.nasa.gsfc.gmsec.gss.tools.msg.sub.MvalDataProvider;

/**
 * Utility to publish dummy MVAL mnenomic or Archive Request messages
 *
 * Created by leif on 6/12/17.
 */
public class Application
{
    private Options options;
    //pub
    private MvalData mvalGenerator;
    private AmsgRequest amsgRequest;
    private Note noteGenerator;
    private MvalRequest mvalRequest;
    private EchoService echoService;

    //sub
    private MessageSubscriber messageSubscriber;
    private MvalDataProvider mvalDataProvider;

    public Application() {
        options = new Options();
    }

    public void run(String[] args)
    {
        options.parse(args);
        options.showAll();
        if(options.getApp().equalsIgnoreCase("amsg"))
        {
            amsgRequest = new AmsgRequest(options);
        } else if(options.getApp().equalsIgnoreCase("note")) {
            noteGenerator = new Note(options);
        } else if(options.getApp().equalsIgnoreCase("mval_req")) {
            mvalRequest = new MvalRequest(options);
        } else if(options.getApp().equalsIgnoreCase("mval_data")) {
            mvalDataProvider = new MvalDataProvider(options);
        } else if(options.getApp().equalsIgnoreCase("gmsub")) {
            messageSubscriber = new MessageSubscriber(options);
        } else if(options.getApp().equalsIgnoreCase("echo")) {
            echoService = new EchoService(options);
        } else { // default to mval generator
            mvalGenerator = new MvalData(options);
        }
    }

    public static void main(String[] args)
    {
        Application app = new Application();
        app.run(args);
    }
}
