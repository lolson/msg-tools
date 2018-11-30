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
import gov.nasa.gsfc.gmsec.api.mist.message.MistMessage;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MvalRequest extends AbstractMessageService
{

    public MvalRequest(Options options)
    {
        this.options = options;
        init();
        run();
    }

    /**
     *  Mnemonic Value Request
     *  REQUEST-TYPE: 2                   // I16:  (required) Start
     *  PUBLISH-RATE: 0+                  // U16: (optional) rate in sec mnemonic value data pub, defaults to 5 sec
     *  NUM-OF-MNEMONICS:  1+             // (required) U16
     *  MNEMONIC.n.NAME:  "n" starts at 1 // (required) String
     *  MNEMONIC.n.SAMPLE-RATE: 1+        // (depends) millis
     *  MNEMONIC.n.CRITERIA: 2            // (default) every sample
     */
    public void run()
    {
        print(options.toString());
        try
        {
            short startDataStream = 2;
            Optional<String> subject = Optional.ofNullable(options.getSubject());
            Optional<String> mvalProvider = Optional.ofNullable(options.getTlmProvider());
            String tlmComponent = mvalProvider.isPresent() ? mvalProvider.get() : "ITOS";
            if (!subject.isPresent() || subject.get().isEmpty())
            {
                subject = Optional.ofNullable(
                        String.format(
                                "%s.REQ.MVAL.%s",
                                options.getBaseSubject(),
                                tlmComponent
                        ));
            }

            Optional<String[]> mnemonics = Optional.ofNullable(options.getMnemonic());
            if (!mnemonics.isPresent())
            {
                String[] mArr = {"SOME_NICE_MNEMONIC"};
                mnemonics = Optional.ofNullable(mArr);
            }

            print("About to send mval request using subject " + subject.get());
            MistMessage msg = new MistMessage(subject.get(), "REQ.MVAL", cm.getSpecification());
            msg = setStandardFields(msg);

            //Mnemonic Value Request Specific
            msg.addField("REQUEST-TYPE", startDataStream); // start stream of mnemonic data
            msg.addField("MSG-ID", "20181015"); // start stream of mnemonic data

            msg.addField("NUM-OF-MNEMONICS", new U16(mnemonics.get().length));
            int ix = 1;
            for (String mnem : mnemonics.get())
            {
                msg.addField("MNEMONIC." + ix + ".NAME", mnem);
                msg.addField("MNEMONIC." + ix + ".CRITERIA", 2);
                msg.addField("MNEMONIC." + ix + ".DATA-TYPE", 3); // send both raw/converted
                msg.addField("MNEMONIC." + ix + ".STATE-ATTRIBUTES", 2);
                ix++;
            }

            msg.addField("PUBLISH-RATE", new U16(0)); // before 0, TODO does this differ from GSS Atlas?

//            Optional<Field> pubRateField = Optional.ofNullable(options.getPubRate());
//            long pubRate = 5;
//            if (pubRateField.isPresent())
//            {
//                print("Using pub rate: " + msg.getField("PUBLISH-RATE").getStringValue());
//                pubRate = Long.parseLong(msg.getField("PUBLISH-RATE").getStringValue());
//            } else {
//                msg.addField("PUBLISH-RATE", new U16(5));
//            }

            makeMvalRequest(msg);

        } catch (GMSEC_Exception e)
        {
            print("GMSEC exception publishing MVAL request" + e.getErrorMessage());
        }

    }

    private void makeMvalRequest(MistMessage msg) {
        int TIMEOUT = 30000;
        int DO_NOT_REPUBLISH = -1;
        boolean notDone = true;
        try
        {
//            Mnemonic Value Response
//            acknowledges receipt and status to the MVAL request
//            MESSAGE-TYPE:        RESP
//            MESSAGE-SUBTYPE:     MVAL
//                --- contents required ---
//            RESPONSE-STATUS:  [1,3,4, or 5]   // I16: ack, success, fail, invalid
//            NUM-OF-MNEMONICS: 1+  // U16
//            MNEMONIC.1.NAME: foo    // String
//            MNEMONIC.1.STATUS: [1,2]  //I16: valid, valid-no-data
//            MNEMONIC.1.NUM-OF-SAMPLES:  //U16: num data samples for first mnemonic
            Message reply = cm.request(msg, TIMEOUT, DO_NOT_REPUBLISH);
            if (reply != null)
            {
                print("Received reply message:\n "+reply.toXML());
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
