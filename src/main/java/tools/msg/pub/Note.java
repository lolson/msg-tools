/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.pub;

import gov.nasa.gsfc.gmsec.api.*;
import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.util.TimeUtil;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;

import static java.lang.Thread.*;

public class Note extends AbstractMessageService
{
    private static String SUBJECT = "GMSEC.DEFAULT-MISSION.DEFAULT-SATELLITE.MSG.LOG.MSG-GENERATOR";

    public Note(Options options) {
        this.options = options;
        init();
        run();
    }

    public void run()
    {
        Message msg = new Message(SUBJECT, Message.MessageKind.PUBLISH);
        isNotDone = true;
        int i=0;
        while (isNotDone)
        {
            try
            {
                msg.addField("MISSION-ID","DEFAULT-MISSION");
                msg.addField("COMPONENT","TESTER");
                msg.addField("FACILITY","DEFAULT-FACILITY");
                msg.addField("MESSAGE-TYPE","MSG");
                msg.addField("MESSAGE-SUBTYPE","LOG");

                msg.addField("EVENT-TIME", TimeUtil.formatTime(TimeUtil.getCurrentTime()));
                msg.addField("MSG-TEXT", "NOTE (stknudse): message with \"double quotes\" and 'single quotes' and `backticks` and a \n new line");
                msg.addField("OCCURRENCE-TYPE", "NOTE");
                msg.addField("SEVERITY", 3);
                msg.addField("SUBCLASS", "NOTE");

                cm.publish(msg);
                sleep(options.getWaitInMillis());
            } catch (GMSEC_Exception e)
            {
                print("GMSEC exception: " + e.getErrorMessage());
                isNotDone = false;
            } catch (InterruptedException e)
            {
                print("Thread interruption sleeping "+e.getMessage());
            }
        }
    }

}
