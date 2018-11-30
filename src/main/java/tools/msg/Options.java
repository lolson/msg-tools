/*
 * Copyright 2016 United States Government as represented by the
 * Administrator of The National Aeronautics and Space Administration.
 * No copyright is claimed in the United States under Title 17, U.S. Code.
 * All Rights Reserved.
 */

package tools.msg;

import java.util.HashMap;
import java.util.Map;

/**
 * This class parses any command line arguments and prints out usage
 */
public class Options
{

    private int wait_in_millis = 100;
    private String mwId = "bolt";
    private String server = "localhost";
    private String mode = "random";
    private String app = "mval";
    private String reqString = "";
    private String subject = "";
    private String file = "./gmsec_message.txt";
    private Map mvalParams = new HashMap<String, String>();
    private String mnemonic = "";
    private String spec = "201600";
    private String baseSubject = "";
    private String tlmProvider = "";
    private String msgFormat = "JSON";

    public boolean usage()
    {
        print("\nusage: start.sh [key=value]...\n"
                + "\nKnown keys/values are:"
                + "\n\tserver=<String>\t\t\t# defaults to localhost "
                + "\n\tmw-id=<String>\t# defaults to bolt"
                + "\n\tapp=<String>\t\t\t# mval, amsg, mval_req, note, gmsub, mval_data, or echo"
                + "\nOptional/depends keys/values are:"
                + "\n\tsubject=<String>\t\t\t# GMSEC message topic"
                + "\n\tmval_req_subject=<String>\t\t\t# MVAL request topic"
                + "\n\tmval_resp_subject=<String>\t\t\t# MVAL response topic"
                + "\n\tmnemonic=<String>\t\t\t# mnenomic name for MVAL request, or comma separated names "
                + "\n\tspec=\t\t\t# GMSEC specification version 201400, 201600 or 201800 (defaults to 201600)"
                + "\n\tfile=<String>\t\t\t# File to write out message json"
                + "\n\tquery=<String>\t\t\t# archive message request query"
                + "\n\twait_in_millis=<int>\t# use 1000 for 1 msg/sec"
                + "\n\tmode=<String>\t\t\t# random or sine"
                + "\n\tbaseSubject=<String>\t\t\t# usually spec and mission e.g. GMSEC.MISSION_ID"
                + "\n\ttlmComponent=<String>\t\t\t# Component providing TLM MVAL request"
                + "\n\tmsg_format=<String>\t\t\t# XML or JSON (defaults to JSON)"
        );
        return false;
    }

    public boolean parse(String[] args)
    {
        for (String arg : args)
        {
            int p = arg.indexOf('=');
            if (p != -1)
            {
                String key = arg.substring(0, p);
                String value = arg.substring(p + 1);

                if ("wait_in_millis".equalsIgnoreCase(key))
                    this.setWaitInMillis(value);
                if ("mw-id".equalsIgnoreCase(key))
                    this.setMwId(value);
                if ("server".equalsIgnoreCase(key))
                    this.setServer(value);
                if ("mode".equalsIgnoreCase(key))
                    this.setMode(value);
                if ("app".equalsIgnoreCase(key))
                    this.setApp(value);
                if ("query".equalsIgnoreCase(key))
                    this.setReqString(value);
                if ("subject".equalsIgnoreCase(key))
                    this.setSubject(value);
                if ("file".equalsIgnoreCase(key))
                    this.setFile(value);
                if ("mnemonic".equalsIgnoreCase(key))
                    this.setMnemonic(value);
                if ("spec".equalsIgnoreCase(key))
                    this.setSpec(value);
                if ("baseSubject".equalsIgnoreCase(key))
                    this.setBaseSubject(value);
                if ("tlmComponent".equalsIgnoreCase(key))
                    this.setTlmProvider(value);
                if ("msg_format".equalsIgnoreCase(key))
                    this.setMsgFormat(value);
            } else {
                if("help".equalsIgnoreCase(arg) || "usage".equalsIgnoreCase(arg))
                    usage();
            }
        }
        return true;
    }

    public int getWaitInMillis()
    {
        return wait_in_millis;
    }

    public void setWaitInMillis(String wait_in_millis)
    {
        this.wait_in_millis = Integer.parseInt(wait_in_millis);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\nConfigured settings:");
        for (String elem : getMwConfig())
        {
            sb.append("\n\t"+elem);
        }
        sb.append("\n\tapp=" + getApp());

        if(getApp().equalsIgnoreCase("mval") || getApp().equalsIgnoreCase("note")) {
            sb.append("\n\twait_in_millis=" + getWaitInMillis());
        }
        if(getApp().equalsIgnoreCase("mval")) {
            sb.append("\n\tmode=" + getMode());
        }
        if(getApp().equalsIgnoreCase("amsg")) {
            sb.append("\n\tquery=" + getReqString());
        }
        if(getApp().equalsIgnoreCase("gmsub")) {
            sb.append("\n\tsubject=" + getSubject());
            sb.append("\n\tfile=" + getFile());
            sb.append("\n\tmsg_format=" + getMsgFormat());
        }
        if(getApp().equalsIgnoreCase("mval_data")) {
            sb.append("\n\tsubject=" + getSubject());
        }
        if(getApp().equalsIgnoreCase("mval_req")) {
            sb.append("\n\tbase subject=" + getBaseSubject());
            sb.append("\n\ttlm component=" + getTlmProvider());
        }
        if(getApp().equalsIgnoreCase("echo")) {
            sb.append("\n\tfile=" + getFile());
        }
        return sb.toString();
    }

    public void showAll()
    {
        print(toString());
    }

    private void print(String s)
    {
        System.out.println(s);
    }

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public String[] getMwConfig() {
        StringBuilder sb = new StringBuilder();
        sb.append("mw-id=");
        sb.append(getMwId());
        sb.append(",");
        sb.append("server=");
        sb.append(getServer());
        sb.append(",");
        sb.append("GMSEC-SPECIFICATION-VERSION=");
        sb.append(getSpec());
        if(getApp().equalsIgnoreCase("mval_data")) {
            sb.append(",");
            //for whether responses recevied by a cxn are available via receive
            sb.append("mw-expose-resp=true");
            sb.append(",");
            // enables open resp behavior. Subject not remapped.  Requestor must subscribe to RESP
            sb.append("gmsec-req-resp=open-resp");
        }
        String[] mwConfig = sb.toString().split(",");
        return mwConfig;
    }

    public String getApp()
    {
        return app;
    }

    public void setApp(String app)
    {
        this.app = app;
    }

    public String getReqString()
    {
        return reqString;
    }

    public void setReqString(String reqString)
    {
        this.reqString = reqString;
    }

    public String[] getMnemonic()
    {
        return mnemonic.split(",");
    }

    public void setMnemonic(String mnemonic)
    {
        this.mnemonic = mnemonic;
    }

    public String getSpec()
    {
        return spec;
    }

    public void setSpec(String spec)
    {
        this.spec = spec;
    }

    public String getMwId()
    {
        return mwId;
    }

    public void setMwId(String mwId)
    {
        this.mwId = mwId;
    }

    public String getBaseSubject()
    {
        return baseSubject;
    }

    public void setBaseSubject(String baseSubject)
    {
        this.baseSubject = baseSubject;
    }

    public String getTlmProvider()
    {
        return tlmProvider;
    }

    public void setTlmProvider(String tlmProvider)
    {
        this.tlmProvider = tlmProvider;
    }

    public String getMsgFormat()
    {
        return msgFormat;
    }

    public void setMsgFormat(String msgFormat)
    {
        this.msgFormat = msgFormat;
    }

    public enum Mode
    {
        RANDOM("RANDOM"),
        SINE("SINE");
        private String type;

        Mode(String type)
        {
            this.type = type;
        }
    }

    public String getMode()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode = mode;
    }
    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String sub)
    {
        this.subject = sub;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String filePath)
    {
        this.file = filePath;
    }

    public Map<String, String> getMvalMap()
    {
        return mvalParams;
    }


}
