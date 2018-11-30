/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package gov.nasa.gsfc.gmsec.gss.tools.msg;

import gov.nasa.gsfc.gmsec.api.Config;
import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManager;
import gov.nasa.gsfc.gmsec.api.mist.SubscriptionInfo;
import gov.nasa.gsfc.gmsec.api.mist.message.MistMessage;
import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;

public abstract class AbstractMessageService
{
    protected boolean isNotDone = true;
    protected Options options;
    protected ConnectionManager cm;
    protected SubscriptionInfo si;

    protected String baseSubject = "GMSEC.DEFAULT-MISSION.SAT-ID";
    protected String missionId = "DEFAULT-MISSION";
    protected String satelliteId = "SAT-ID";
    protected String facility = "DEFAULT-FACILITY";
    protected String component = "GSS";
    protected String constellationId = "TEST-CONSTELLATION-ID";

    protected void init()
    {
        Config config = new Config(options.getMwConfig());
        try
        {
            cm = new ConnectionManager(config, false);
            cm.initialize();
        } catch (GMSEC_Exception e)
        {
            print("Error creating Connection Manager, "+e.getErrorMessage());
            return;
        }
    }

    protected void print(String s)
    {
        System.out.println(s);
    }

    public void cleanup() {
        try {
            if(si != null) {
                cm.unsubscribe(si);
            }
            cm.stopAutoDispatch();
            cm.cleanup();
        } catch (GMSEC_Exception e)
        {
            print(e.getMessage());
        }
    }

    protected MistMessage setStandardFields(MistMessage msg) throws GMSEC_Exception {
        // Stanadrd Fields
        msg.setValue("MISSION-ID", missionId);
        msg.setValue("SAT-ID-PHYSICAL", satelliteId);
        msg.setValue("SAT-ID-LOGICAL", satelliteId);
        msg.setValue("CONSTELLATION-ID", constellationId);
        msg.setValue("FACILITY", facility);
        msg.setValue("COMPONENT", component);
        return msg;
    }
}
