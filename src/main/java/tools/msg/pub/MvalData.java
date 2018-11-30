/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.pub;

import gov.nasa.gsfc.gmsec.api.*;
import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.field.Field;
import gov.nasa.gsfc.gmsec.api.field.StringField;
import gov.nasa.gsfc.gmsec.api.util.TimeUtil;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.*;

public class MvalData extends AbstractMessageService
{
    private static String SUBJECT = "GMSEC.TEST-MISSION-ID.SATID.MSG.MVAL.MSG-GENERATOR";

    public MvalData(Options options) {
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
                msg.addField("MISSION-ID","TEST-MISSION-ID");
                msg.addField("CONSTELLATION-ID","TEST-CONSTELLATION-ID");
                msg.addField("COMPONENT","TEST MSG GENERATOR");
                msg.addField("FACILITY","TEST-FACILITY");
                msg.addField("MESSAGE-TYPE","MSG");
                msg.addField("MESSAGE-SUBTYPE","MVAL");

                if(!options.getMvalMap().isEmpty()) {
                    for(Map.Entry<String, String> entry : options.getMvalMap().entrySet()) {
                        if(entry.getKey().equals("MNEMONIC.1.NAME")) {
                            print("Adding mnemonic "+entry.getValue());
                            msg.addField("MNEMONIC.1.NAME", entry.getValue());
                        }
                        if(entry.getKey().equals("NUM-OF-MNEMONICS")) {
                            msg.addField("NUM-OF-MNEMONICS", entry.getValue());
                        }
                        if(entry.getKey().equals("PUBLISH-RATE")) {
                            msg.addField("PUBLISH-RATE", entry.getValue());
                        }
                    }
                }
                Optional<Field> mName = Optional.ofNullable(msg.getField("MNEMONIC.1.NAME"));
                if (!mName.isPresent())
                {
                    print("no mnemonic name found, setting as sample");
                    msg.addField("MNEMONIC.1.NAME", "SAMPLE");
                }

                Optional<Field> numMnemonics = Optional.ofNullable(msg.getField("NUM-OF-MNEMONICS"));
                if (!numMnemonics.isPresent())
                {
                    print("no num-of-mnemonics found, setting as 1");
                    msg.addField("NUM-OF-MNEMONICS", 1);
                }

                msg.addField("MSG-ID","NONE");
                msg.addField("MNEMONIC.1.NUM-OF-SAMPLES", 1);
                msg.addField("MNEMONIC.1.STATUS", 2);
                msg.addField("MNEMONIC.1.SAMPLE.1.TIME-STAMP", TimeUtil.formatTime(TimeUtil.getCurrentTime()));
                msg = addMnemonicValue(msg, i++);

                cm.publish(msg);
                Optional<Field> pubRateField = Optional.ofNullable(msg.getField("PUBLISH-RATE"));
                long pubRate = 3;
                if(pubRateField.isPresent()) {
                    print("Using pub rate: "+msg.getField("PUBLISH-RATE").getStringValue());
                    pubRate = Long.parseLong(msg.getField("PUBLISH-RATE").getStringValue());
                }
                long wait_in_millis = pubRate * 1000;
                sleep(wait_in_millis);
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

    private int randomIntValue(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private Message addMnemonicValue(Message msg, int count)
    {
        int offset = 50;
        if(options.getMode().equalsIgnoreCase(Options.Mode.RANDOM.toString())) {
            int intValue = randomIntValue(100, 500);
            msg.addField("MNEMONIC.1.SAMPLE.1.RAW-VALUE", intValue);
            msg.addField("MNEMONIC.1.SAMPLE.1.EU-VALUE", intValue );
            msg.addField("MNEMONIC.1.SAMPLE.1.TEXT-VALUE", intValue+"");
        } else if(options.getMode().equalsIgnoreCase(Options.Mode.SINE.toString())) {
            float floatValue = getSineValue(count, offset);
            msg.addField("MNEMONIC.1.SAMPLE.1.RAW-VALUE", floatValue);
            msg.addField("MNEMONIC.1.SAMPLE.1.EU-VALUE", floatValue );
            msg.addField("MNEMONIC.1.SAMPLE.1.TEXT-VALUE", floatValue+"");
        }
        return msg;
    }

    private float getSineValue(int i, int offset)
    {
        return (float)Math.floor(((Math.sin(i)*2)+offset) * 100) /100;
    }

}
