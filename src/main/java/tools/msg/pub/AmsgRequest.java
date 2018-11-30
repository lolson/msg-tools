/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.pub;

import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.Message;
import gov.nasa.gsfc.gmsec.api.U16;
import gov.nasa.gsfc.gmsec.api.mist.gmsecMIST;
import gov.nasa.gsfc.gmsec.api.mist.message.MistMessage;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AmsgRequest extends AbstractMessageService
{

    private static String SUBJECT="GMSEC.TEST-MISSION-ID.SATID.REQ.AMSG.GSS-ELYSIUM-SQL";

    public AmsgRequest(Options options)
    {
        this.options = options;
        init();
        run();
    }

    public void run()
    {
        HashMap<String, String> params = new HashMap<>();
        params.put("MAX-MESSAGE-RETURN", "500");
	    //"REQ-STRING", "(MESSAGE-SUBTYPE equals MVAL) AND (NODE contains node1)"

        try
        {
            String clientUid= "CLIENT-UID";
            String widgetUid= "WIDGET-UID";
            String destination = "/queue/"+clientUid+"/"+widgetUid;
            String resultDestination = "/archive/*.>/"+clientUid+"/"+widgetUid;
            String query = "(MESSAGE-SUBTYPE equals MVAL)";
            if(!options.getReqString().isEmpty()) query = options.getReqString();

            String subjectStandard = "GMSEC";
            String missionId = "TEST-MISSION-ID";
            String satelliteId = "SATELLITE-ID";
            String facility = "TEST-FACILITY";
            String component = "TEST-WIDGET";
            String constellationId = "TEST-CONSTELLATION-ID";

            int maxMessageReturn = 0;
            try {
                maxMessageReturn = Integer.parseInt(params.get("MAX-MESSAGE-RETURN"));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }

            String subject = String.format(
                    "%s.%s.%s.REQ.AMSG.GSS-ELYSIUM-SQL",
                    subjectStandard,
                    missionId,
                    satelliteId
            );

            String specId = String.format(
                    "%.2f.%s.REQ.AMSG",
                    (double) gmsecMIST.GMSEC_ISD_CURRENT / 100.0,
                    subjectStandard
            );

            MistMessage msg = new MistMessage(subject, specId, cm.getSpecification());
            msg.setValue("MISSION-ID", missionId);
            msg.setValue("SAT-ID-PHYSICAL", satelliteId);
            msg.setValue("SAT-ID-LOGICAL", satelliteId);
            if (constellationId != null) msg.setValue("CONSTELLATION-ID", constellationId);
            if (facility != null) msg.setValue("FACILITY", facility);
            if (component != null) msg.setValue("COMPONENT", component);
            msg.addField("MAX-MESSAGE-RETURN", new U16(maxMessageReturn));  // template calls for field type?
            msg.addField("REQ-STRING", query);

            msg.addField("STOMP-DEST", resultDestination);
            makeArchiveRequest(msg, destination);

        } catch (NumberFormatException ex)
        {
            print("Error parsing incoming archive retrieval message data");
        } catch (GMSEC_Exception ex)
        {
            print(ex.toString());
            print("Failed to publish archive retrieval message");
        }

    }

    private void makeArchiveRequest(MistMessage msg, String destination) {
        int TIMEOUT = 30000;
        int DO_NOT_REPUBLISH = -1;
        boolean notDone = true;
        try
        {
            Message reply = cm.request(msg, TIMEOUT, DO_NOT_REPUBLISH);
            if (reply != null)
            {
                System.out.println("Received reply message:\n "+reply.toXML());
                long msgCount = reply.getI64Field("RETURN-VALUE").getIntegerValue();
                if (msgCount >= 0)
                {
                    System.out.format("Reply received with RETURN-VALUE=%d", msgCount);
                    print("\n");
                    notDone = false;
                }
                do
                {
                    TimeUnit.SECONDS.sleep(1);
                } while (notDone);
                print("Response to archive request message sent to: "+ destination);
            } else {
                print("Null archive message request reply received");
            }
        } catch (GMSEC_Exception e)
        {
            print(e.getErrorMessage());
        } catch(InterruptedException ie)
        {
            print("Interrupted exception thrown while waiting for archive message service.");
        }
    }

}
