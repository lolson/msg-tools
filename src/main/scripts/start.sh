#!/bin/sh

JAVA_CMD=$JAVA_HOME/bin/java


# ------------------------------------------------------------------------------
# Adding jar files to CLASSPATH
# ------------------------------------------------------------------------------
CLASSPATH=""
for i in *.jar; do
    CLASSPATH="$CLASSPATH":"$i"
done
export CLASSPATH=$GMSEC_HOME/bin/gmsecapi.jar:.:$CLASSPATH

export LD_LIBRARY_PATH=$GMSEC_HOME/bin:$LD_LIBRARY_PATH
export PATH=$GMSEC_HOME/bin:$PATH

PLATFORM=`uname -s`
if [[ $PLATFORM == 'Darwin' ]]; then
    export DYLD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DYLD_LIBRARY_PATH
fi

# ------------------------------------------------------------------------------
# Starting the application
# ------------------------------------------------------------------------------
"$JAVA_CMD" -cp $CLASSPATH gov/nasa/gsfc/gmsec/gss/tools/msg/Application "$@"

# Sample AMQ
#"$JAVA_CMD" -cp $CLASSPATH gov/nasa/gsfc/gmsec/gss/tools/msg/Application server=tcp://127.0.0.1:61616 connectiontype=gmsec_activemq384 wait_in_millis=2
# Sample Bolt
#"$JAVA_CMD" -cp $CLASSPATH gov/nasa/gsfc/gmsec/gss/tools/msg/Application server=127.0.0.1 connectiontype=gmsec_bolt wait_in_millis=2
# Sample Websphere MQ
#"$JAVA_CMD" -cp $CLASSPATH gov/nasa/gsfc/gmsec/gss/tools/msg/Application server=gs580s-gpts2,gs580s-gpts1 connectiontype=gmsec_websphere75 port=1414 wait_in_millis=2
# e.g. ./start.sh app=amsg
# e.g. ./start.sh app=note
# e.g. ./start.sh app=mval_data subject="GMSEC.*.*.REQ.MVAL.*"
# e.g. ./start.sh app=gmsub subject="GMSEC.SDO.SDO1.MSG.MVAL.ITOS-SDO.+" file="./sdo_msg_mval.txt"
# e.g. ./start.sh app=mval_req mval_req_subject="GMSEC.SDO.SDO1.REQ.MVAL.ITOS-SDO" mval_resp_subject="GMSEC.SDO.SDO1.RESP.MVAL.ITOS-SDO.+" mnemonic=AIA_IS3_DIODE_MY
# e.g. ./start.sh app=mval_req spec=201400 mval_req_subject="GMSEC.SDO.SDO1.REQ.MVAL.ITOS-SDO" mval_resp_subject="GMSEC.SDO.SDO1.RESP.MVAL.ITOS-SDO.+" mnemonic=GCEA_CS_TOTEEPR1
# e.g. ./start.sh app=mval_req spec=201400 mval_req_subject="GMSEC.SDO.SDO1.REQ.MVAL.ITOS-SDO" mval_resp_subject="GMSEC.SDO.SDO1.RESP.MVAL.ITOS-SDO.+" mnemonic=PSEB_EVE_MEGS_A_CCD_T
# e.g. ./start.sh app=mval_req spec=201400 mval_req_subject="GMSEC.SDO.SDO1.REQ.MVAL.ITOS-SDO" mval_resp_subject="GMSEC.SDO.SDO1.RESP.MVAL.ITOS-SDO.+" mnemonic=ACEA_IRU1_CURR,GCEA_CS_TOTEEPR1
# e.g. ./start.sh app=mval_req mw-id=activemq384 server=tcp://server1:61616 mval_req_subject="GMSEC.THEMIS.THEMIS_MASTER.REQ.MVAL.ITOS" mval_resp_subject="GMSEC.THEMIS.THEMIS_MASTER.RESP.MVAL.ITOS" mnemonic=THEMIS[0].vsat.vs.spacecraft.angular_rate[2],THEMIS[0].vsat.vs.thruster.net_torque[2]
# e.g. ./start.sh app=mval_req mw-id=activemq384 server=tcp://server1:61616 mval_req_subject="GMSEC.THEMIS.THEMIS_MASTER.REQ.MVAL.ITOS" mval_resp_subject="GMSEC.THEMIS.THEMIS_MASTER.RESP.MVAL.ITOS" mnemonic=THEMIS[1].vsat.vs.spacecraft.angular_rate[2]

