/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.sub;

import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.Message;
import gov.nasa.gsfc.gmsec.api.U16;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManager;
import gov.nasa.gsfc.gmsec.api.mist.ConnectionManagerCallback;
import gov.nasa.gsfc.gmsec.api.mist.message.MistMessage;
import gov.nasa.gsfc.gmsec.api.util.TimeUtil;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;
import gov.nasa.gsfc.gmsec.gss.tools.msg.pub.MvalData;
import gov.nasa.gsfc.gmsec.api.field.Field;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Thread.sleep;

public class MvalDataProvider extends AbstractMessageService
{

    public MvalDataProvider(Options options)
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
            print("Using middleware client: " + cm.getLibraryVersion());

            // listen for MVAL REQ MSG
            si = cm.subscribe(options.getSubject(), new ConnectionManagerCallback()
            {
                public void onMessage(ConnectionManager cm, Message msg)
                {
                    String mnemonic = "SAMPLE";
                    long numOfMnemonics = 0;

                    try
                    {
                        numOfMnemonics = msg.getField("NUM-OF-MNEMONICS").getIntegerValue();
                        mnemonic = msg.getField("MNEMONIC.1.NAME").getStringValue();

                        String subject = String.format(
                                "%s.RESP.MVAL",
                                baseSubject
                        );

                        MistMessage reply = new MistMessage(subject, "RESP.MVAL", cm.getSpecification());
                        reply = setStandardFields(reply);
                        int responseStatus = 3; //successful comnpletion

                        //Mnemonic Value Response Specific
                        reply.addField("RESPONSE-STATUS", responseStatus);
                        reply.addField("RETURN-VALUE", System.nanoTime());
                        reply.addField("NUM-OF-MNEMONICS", numOfMnemonics);
                        reply.addField("MNEMONIC.1.NAME", mnemonic);
                        cm.reply(msg, reply);

                        isNotDone = true;
                        while (isNotDone)
                        {

                            Optional<Field> pubRateField = Optional.ofNullable(msg.getField("PUBLISH-RATE"));
                            long pubRate = 5;
                            if (pubRateField.isPresent())
                            {
                                print("Using pub rate: " + msg.getField("PUBLISH-RATE").getStringValue());
                                pubRate = Long.parseLong(msg.getField("PUBLISH-RATE").getStringValue());
                            }

                            String mvalSubject = String.format(
                                    "%s.MSG.MVAL.%s",
                                    baseSubject,
                                    component
                            );

                            MistMessage mvalData = new MistMessage(mvalSubject, "MSG.MVAL", cm.getSpecification());
                            mvalData = setStandardFields(mvalData);

                            mvalData.addField("MSG-ID","NONE");
                            mvalData.addField("NUM-OF-MNEMONICS", numOfMnemonics);
                            mvalData.addField("PUBLISH-RATE", pubRate);

                            for (int i = 1; i <= numOfMnemonics; i++)
                            {
                                mvalData.addField("MNEMONIC."+i+".NAME", msg.getField("MNEMONIC."+i+".NAME").getStringValue());
                                mvalData.addField("MNEMONIC."+i+".NUM-OF-SAMPLES", 1);
                                mvalData.addField("MNEMONIC."+i+".STATUS", 2);
                                mvalData.addField("MNEMONIC."+i+".SAMPLE.1.TIME-STAMP", TimeUtil.formatTime(TimeUtil.getCurrentTime()));
                                int intValue = randomIntValue(100, 500);
                                mvalData.addField("MNEMONIC."+i+".SAMPLE.1.RAW-VALUE", intValue);
                                mvalData.addField("MNEMONIC."+i+".SAMPLE.1.EU-VALUE", intValue );
                                mvalData.addField("MNEMONIC."+i+".SAMPLE.1.TEXT-VALUE", intValue+"");
                            }

                            // start mval data stream
                            cm.publish(mvalData);

                            long wait_in_millis = pubRate * 1000;
                            sleep(wait_in_millis);
                        }
                    } catch (GMSEC_Exception e)
                    {
                        print("GMSEC exception: " + e.getErrorMessage());
                        isNotDone = false;
                    } catch (InterruptedException e)
                    {
                        print("Thread interruption sleeping " + e.getMessage());
                    }
                }
            });

            cm.startAutoDispatch();
            while (true)
            {
                Thread.sleep(100);
            }
        } catch (GMSEC_Exception | IllegalArgumentException | InterruptedException e)
        {
            print(e.getMessage());
        } finally
        {
            cleanup();
            System.exit(-1);
        }
    }

    private int randomIntValue(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
