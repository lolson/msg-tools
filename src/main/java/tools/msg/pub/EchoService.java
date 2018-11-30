/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg.pub;

import gov.nasa.gsfc.gmsec.api.GMSEC_Exception;
import gov.nasa.gsfc.gmsec.api.Message;
import gov.nasa.gsfc.gmsec.gss.tools.msg.AbstractMessageService;
import gov.nasa.gsfc.gmsec.gss.tools.msg.Options;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EchoService extends AbstractMessageService
{

    private boolean isDone;
    private StringBuilder buffer;
        public EchoService(Options options) {
            this.options = options;
            init();
            run();
        }

    public void run()
    {
        isDone = false;
        while (!isDone) {
            try
            {
                //read file into stream, try-with-resources
                try (Stream<String> lines = Files.lines(Paths.get(options.getFile()))) {
                    lines.forEach(s ->
                    {
                        try
                        {
                            processLine(s);
                        } catch (GMSEC_Exception e)
                        {
                            print("GMSEC exception");
                        } catch (InterruptedException e)
                        {
                            print("Interrupted exception");
                        }
                    });
                }

                //log.info("Reached end of file. Starting over...");
                print("Reached end of file.");
                isDone = true;

            } catch(IOException e) {
                print("IO exception");
                e.printStackTrace();
                isDone = true;
            }
        }
    }

    private void processLine(String s) throws GMSEC_Exception, InterruptedException
    {
        if (s.indexOf("<MESSAGE") != -1)
        {
            buffer = new StringBuilder();
            buffer.append(s);
        } else if (s.equals("</MESSAGE>"))
        {
            buffer.append(s);
            Message message = new Message(buffer.toString());
            cm.publish(message);
            TimeUnit.SECONDS.sleep(1);
        } else if (s.indexOf("<FIELD") != -1) {
            buffer.append(s);
        }
    }

}
