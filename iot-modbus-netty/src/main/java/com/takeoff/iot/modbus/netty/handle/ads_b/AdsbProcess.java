package com.takeoff.iot.modbus.netty.handle.ads_b;

import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.PositionDecoder;
import org.opensky.libadsb.tools;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.AirborneOperationalStatusV1Msg;
import org.opensky.libadsb.msgs.AirborneOperationalStatusV2Msg;
import org.opensky.libadsb.msgs.AirbornePositionV0Msg;
import org.opensky.libadsb.msgs.AirbornePositionV1Msg;
import org.opensky.libadsb.msgs.AirbornePositionV2Msg;
import org.opensky.libadsb.msgs.AirspeedHeadingMsg;
import org.opensky.libadsb.msgs.AllCallReply;
import org.opensky.libadsb.msgs.AltitudeReply;
import org.opensky.libadsb.msgs.CommBAltitudeReply;
import org.opensky.libadsb.msgs.CommBIdentifyReply;
import org.opensky.libadsb.msgs.CommDExtendedLengthMsg;
import org.opensky.libadsb.msgs.EmergencyOrPriorityStatusMsg;
import org.opensky.libadsb.msgs.ExtendedSquitter;
import org.opensky.libadsb.msgs.IdentificationMsg;
import org.opensky.libadsb.msgs.IdentifyReply;
import org.opensky.libadsb.msgs.LongACAS;
import org.opensky.libadsb.msgs.MilitaryExtendedSquitter;
import org.opensky.libadsb.msgs.ModeSReply;
import org.opensky.libadsb.msgs.OperationalStatusV0Msg;
import org.opensky.libadsb.msgs.ShortACAS;
import org.opensky.libadsb.msgs.SurfaceOperationalStatusV1Msg;
import org.opensky.libadsb.msgs.SurfaceOperationalStatusV2Msg;
import org.opensky.libadsb.msgs.SurfacePositionV0Msg;
import org.opensky.libadsb.msgs.SurfacePositionV1Msg;
import org.opensky.libadsb.msgs.SurfacePositionV2Msg;
import org.opensky.libadsb.msgs.TCASResolutionAdvisoryMsg;
import org.opensky.libadsb.msgs.TargetStateAndStatusMsg;
import org.opensky.libadsb.msgs.VelocityOverGroundMsg;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdsbProcess {
    private ModeSDecoder decoder = new ModeSDecoder();
    private PositionDecoder positionDecoder = new PositionDecoder();

    public AdsbProcess() {
    }

    public void decodeMsg(long timestamp, String raw, Position receiver, Map info) {
        ModeSReply msg;
        try {
            msg = this.decoder.decode(raw);
        } catch (BadFormatException var21) {
            System.out.println("Malformed message! Skipping it. Message: " + var21.getMessage());
            return;
        } catch (UnspecifiedFormatError var22) {
            System.out.println("Unspecified message! Skipping it...");
            return;
        }

        String icao24 = tools.toHexString(msg.getIcao24());
        info.put("icao24",icao24);

        if (!tools.isZero(msg.getParity()) && !msg.checkParity()) {
            if (msg.getDownlinkFormat() != 17) {
                switch(msg.getType()) {
                    case MODES_REPLY:
                        info.put("message","[" + icao24 + "]: Unknown message with DF " + msg.getDownlinkFormat());
                        break;
                    case SHORT_ACAS:
                        ShortACAS acas = (ShortACAS)msg;
                        info.put("altitude",acas.getAltitude()+"ft");
                        info.put("acas",(acas.hasOperatingACAS() ? "operating." : "not operating."));
                        info.put("airborne",acas.isAirborne() ? "airborne" : "on the ground");
                        info.put("sensitivity_level",acas.getSensitivityLevel());
                        break;
                    case ALTITUDE_REPLY:
                        AltitudeReply alti = (AltitudeReply)msg;
                        info.put("altitude", alti.getAltitude() + "ft");
                        break;
                    case IDENTIFY_REPLY:
                        IdentifyReply identify = (IdentifyReply)msg;
                        info.put("identify",identify.getIdentity());
                        break;
                    case ALL_CALL_REPLY:
                        AllCallReply allcall = (AllCallReply)msg;
                        info.put("message", "All-call reply for " + tools.toHexString(allcall.getInterrogatorCode()) + " (" + (allcall.hasValidInterrogatorCode() ? "valid" : "invalid"));
                        break;
                    case LONG_ACAS:
                        LongACAS long_acas = (LongACAS)msg;
                        info.put("altitude",long_acas.getAltitude());
                        info.put("acas", (long_acas.hasOperatingACAS() ? "operating.": "not operating."));
                        info.put("airborne",(long_acas.isAirborne() ? "airborne" : "on the ground"));
                        info.put("sensitivity_level",long_acas.getSensitivityLevel());
                        info.put("rac",(long_acas.hasValidRAC() ? "valid" : "not valid"));
                        info.put("message","          RAC is " + (long_acas.hasValidRAC() ? "valid" : "not valid") + " and is " + long_acas.getResolutionAdvisoryComplement() + " (MTE=" + long_acas.hasMultipleThreats() + ")");
                        info.put("maximum_airspeed",long_acas.getMaximumAirspeed()+"kn");
                        break;
                    case MILITARY_EXTENDED_SQUITTER:
                        MilitaryExtendedSquitter mil = (MilitaryExtendedSquitter)msg;
                        info.put("message","Military ES of application " + mil.getApplicationCode()+"          Message is 0x" + tools.toHexString(mil.getMessage()));
                        break;
                    case COMM_B_ALTITUDE_REPLY:
                        CommBAltitudeReply commBaltitude = (CommBAltitudeReply)msg;
                        info.put("altitude",commBaltitude.getAltitude() + "ft");
                        break;
                    case COMM_B_IDENTIFY_REPLY:
                        CommBIdentifyReply commBidentify = (CommBIdentifyReply)msg;
                        info.put("identity",commBidentify.getIdentity());
                        break;
                    case COMM_D_ELM:
                        CommDExtendedLengthMsg commDELM = (CommDExtendedLengthMsg)msg;
                       info.put("message","]: ELM message w/ sequence no " + commDELM.getSequenceNumber() + " (ACK: " + commDELM.isAck() + ")          Message is 0x" + tools.toHexString(commDELM.getMessage()));
                       info.put("sequenceNumber",commDELM.getSequenceNumber());
                       info.put("ack",commDELM.isAck());
                }
            } else {
                info.put("message","Message contains biterrors.");
            }
        } else {
            switch(msg.getType()) {
                case ADSB_AIRBORN_POSITION_V0:
                case ADSB_AIRBORN_POSITION_V1:
                case ADSB_AIRBORN_POSITION_V2:
                    AirbornePositionV0Msg ap0 = (AirbornePositionV0Msg)msg;
                    Position c0 = ap0.getLocalPosition(receiver);
                    if (c0 == null) {
                        info.put("message","Cannot decode position yet.");
                    } else {
                        info.put("latitude",c0.getLatitude());
                        info.put("longitude",c0.getLongitude());
                        info.put("altitude",c0.getAltitude());
                    }
                    info.put("horizontal_containment_radius", ap0.getHorizontalContainmentRadiusLimit()+" m");
                    if (ap0.isBarometricAltitude()) {
                        info.put("altitude(barometric)", (ap0.hasAltitude() ? ap0.getAltitude() : "unknown")+" ft");
                    } else {
                        info.put("height (geom)",(ap0.hasAltitude() ? ap0.getAltitude() : "unknown")+ " ft");
                    }

                    Integer geoMinusBaro = this.decoder.getGeoMinusBaro(msg);
                    if (ap0.hasAltitude() && ap0.isBarometricAltitude() && geoMinusBaro != null) {
                        info.put("Height (geom)",ap0.getAltitude() + geoMinusBaro + " ft");
                    }
                    info.put("navigation_integrity_category",ap0.getNIC());
                    info.put("surveillance_status",ap0.getSurveillanceStatus());
                    info.put("surveillance_description",ap0.getSurveillanceStatusDescription());
                    switch(msg.getType()) {
                        case ADSB_AIRBORN_POSITION_V0:
                            info.put("navigation_accuracy_category_for_position", ap0.getNACp());
                            info.put("position_uncertainty",ap0.getPositionUncertainty());
                            info.put("surveillance_integrity_level",ap0.getSIL());
                            return;
                        case ADSB_AIRBORN_POSITION_V1:
                            AirbornePositionV1Msg ap1 = (AirbornePositionV1Msg)msg;
                            info.put("NIC_supplement_A_set", ap1.hasNICSupplementA());
                            return;
                        case ADSB_AIRBORN_POSITION_V2:
                            AirbornePositionV2Msg ap2 = (AirbornePositionV2Msg)msg;
                            info.put("NIC_supplement_A_set", ap2.hasNICSupplementA());
                            info.put("NIC_supplement_B_set", ap2.hasNICSupplementB());
                            return;
                        default:
                            return;
                    }
                case ADSB_SURFACE_POSITION_V0:
                case ADSB_SURFACE_POSITION_V1:
                case ADSB_SURFACE_POSITION_V2:
                    SurfacePositionV0Msg sp0 = (SurfacePositionV0Msg)msg;
                    Position sPos0 = this.decoder.decodePosition(timestamp, sp0, receiver);
                    if (sPos0 == null) {
                        info.put("message","Cannot decode position yet or no reference available (yet).");
                    } else {
                        info.put("latitude",sPos0.getLatitude());
                        info.put("longitude",sPos0.getLongitude());
                        info.put("altitude",sPos0.getAltitude());
                    }
                    info.put("horizontal_containment_radius",sp0.getHorizontalContainmentRadiusLimit());
                    if (sp0.hasValidHeading()) {
                        info.put("heading",sp0.getHeading());
                    }
                    info.put("message","          Airplane is on the ground.");
                    if (sp0.hasGroundSpeed()) {
                        info.put("ground_speed",sp0.getGroundSpeed());
                        info.put("ground_speed_resolution",sp0.getGroundSpeedResolution());
                    }

                    switch(msg.getType()) {
                        case ADSB_SURFACE_POSITION_V0:
                            info.put("navigation_accuracy_category_for_position",sp0.getNACp());
                            info.put("position_uncertainty",sp0.getPositionUncertainty());
                            info.put("surveillance_interity_level",sp0.getSIL()) ;
                            return;
                        case ADSB_SURFACE_POSITION_V1:
                            SurfacePositionV1Msg sp1 = (SurfacePositionV1Msg)msg;
                            info.put("NIC_supplement_A_set",sp1.hasNICSupplementA());
                            return;
                        case ADSB_SURFACE_POSITION_V2:
                            SurfacePositionV2Msg sp2 = (SurfacePositionV2Msg)msg;
                            info.put("NIC_supplement_A_set",sp2.hasNICSupplementA());
                            info.put("NIC_supplement_C_set",sp2.hasNICSupplementC());
                            return;
                        default:
                            return;
                    }
                case ADSB_EMERGENCY:
                    EmergencyOrPriorityStatusMsg status = (EmergencyOrPriorityStatusMsg)msg;
                    info.put("emergency_or_priority",status.getEmergencyStateText());
                    info.put("message",  " Mode A code is " + status.getModeACode()[0] + status.getModeACode()[1] + status.getModeACode()[2] + status.getModeACode()[3]);
                    break;
                case ADSB_AIRSPEED:
                    AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg)msg;
                   info.put("airspeed", (airspeed.hasAirspeedInfo() ? airspeed.getAirspeed() + " kt" : "unkown"));
                    if (this.decoder.getAdsbVersion(msg) == 0) {
                        info.put("heading",airspeed.getHeading()+"°");
                        info.put("relative_to",(airspeed.hasHeadingStatusFlag() ? "magnetic north" : "true north"));
                    } else {
                        info.put("heading",(airspeed.hasHeadingStatusFlag() ? airspeed.getHeading() + "°" : "unkown"));
                    }

                    if (airspeed.hasVerticalRateInfo()) {
                        info.put("verticalRate",(airspeed.hasVerticalRateInfo() ? airspeed.getVerticalRate() + " ft/min" : "unkown"));
                    }
                    break;
                case ADSB_IDENTIFICATION:
                    IdentificationMsg ident = (IdentificationMsg)msg;
                    info.put("callsign",ident.getIdentity());
                    info.put("category",ident.getCategoryDescription());
                    break;
                case ADSB_STATUS_V0:
                    OperationalStatusV0Msg opstat0 = (OperationalStatusV0Msg)msg;
                    info.put("adsb_version",opstat0.getVersion());
                    info.put("tcas",opstat0.hasOperationalTCAS());
                    info.put("cdti",opstat0.hasOperationalCDTI());
                    break;
                case ADSB_AIRBORN_STATUS_V1:
                case ADSB_AIRBORN_STATUS_V2:
                    AirborneOperationalStatusV1Msg opstatA1 = (AirborneOperationalStatusV1Msg)msg;
                    info.put("adsb_version",opstatA1.getVersion());
                    info.put("barometric_altitude_cross-checked)",opstatA1.getBarometricAltitudeIntegrityCode());
                    info.put("gemoetric_vertical_accuracy",opstatA1.getGeometricVerticalAccuracy());
                    if (opstatA1.getHorizontalReferenceDirection()) {
                        info.put("message","Horizontal reference: true north");
                    } else {
                        info.put("message","Horizontal reference: true north");
                    }
                    info.put("navigation_accuracy_category_for_position",opstatA1.getNACp());
                    info.put("position_uncertainty",opstatA1.getPositionUncertainty());
                    info.put("NICsuplementA",opstatA1.hasNICSupplementA());
                    info.put("surveillance_integrity_level",opstatA1.getSIL());
                    info.put("system_design_assurance",opstatA1.getSystemDesignAssurance());
                    info.put("has_1090es_in",opstatA1.has1090ESIn());
                    info.put("ident_switch_active",opstatA1.hasActiveIDENTSwitch());
                    info.put("has_operational_tcas",opstatA1.hasOperationalTCAS());
                    info.put("has_TCAS_resolution_advisory",opstatA1.hasTCASResolutionAdvisory());
                    info.put("has_uat_in",opstatA1.hasUATIn());
                    info.put("uses_single_antenna",opstatA1.hasSingleAntenna());
                    info.put("supports_air-referenced_velocity_reports",opstatA1.hasAirReferencedVelocity());
                    if (msg instanceof AirborneOperationalStatusV2Msg) {
                       info.put("Has_SIL_supplement",((AirborneOperationalStatusV2Msg)msg).hasSILSupplement());
                    }
                    break;
                case ADSB_SURFACE_STATUS_V1:
                case ADSB_SURFACE_STATUS_V2:
                    SurfaceOperationalStatusV1Msg opstatS1 = (SurfaceOperationalStatusV1Msg)msg;
                    info.put("adsb_version",opstatS1.getVersion());
                    info.put("gemoetric_vertical_accuracy",opstatS1.getGeometricVerticalAccuracy() + "m");
                    if (opstatS1.getHorizontalReferenceDirection()) {
                        info.put("message","          Horizontal reference: true north");
                    } else {
                        info.put("message","          Horizontal reference: true north");
                    }
                    info.put("navigation_accuracy_category_for_position" ,opstatS1.getNACp());
                    info.put("position_uncertainty" , opstatS1.getPositionUncertainty());
                    info.put("has_NIC supplement_A" , opstatS1.hasNICSupplementA());
                    info.put("has_NIC supplement_C" , opstatS1.getNICSupplementC());
                    info.put("surveillance_integrity_level" , opstatS1.getSIL());
                    info.put("system_design_assurance" , opstatS1.getSystemDesignAssurance());
                    info.put("has_1090es_in" , opstatS1.has1090ESIn());
                    info.put("ident_switch_active" , opstatS1.hasActiveIDENTSwitch());
                    info.put("has_TCAS_resolution_advisory" , opstatS1.hasTCASResolutionAdvisory());
                    info.put("has_uat_in" , opstatS1.hasUATIn());
                    info.put("uses_single_antenna" , opstatS1.hasSingleAntenna());
                    info.put("airplane_length" , opstatS1.getAirplaneLength() + "m");
                    info.put("airplane_width" , opstatS1.getAirplaneWidth() + "m");
                    info.put("navigation_accuracy_category_for_velocity" , opstatS1.getNACv());
                    info.put("low(<70W)TX_power" , opstatS1.hasLowTxPower());
                    info.put("encoded_GPS_antenna_offset" , opstatS1.getGPSAntennaOffset());
                    info.put("has_track_heading_info", opstatS1.hasTrackHeadingInfo());
                    if (msg instanceof SurfaceOperationalStatusV2Msg) {
                        info.put("has_sil_supplement",((SurfaceOperationalStatusV2Msg)msg).hasSILSupplement());
                    }
                    break;
                case ADSB_TCAS:
                    TCASResolutionAdvisoryMsg tcas = (TCASResolutionAdvisoryMsg)msg;
                   info.put("TCAS_resolution_advisory_completed",tcas.hasRATerminated());
                   info.put("threat_type_is " ,tcas.getThreatType());
                    if (tcas.getThreatType() == 1) {
                       info.put("message",   "Threat identity is 0x" + String.format("%06x", tcas.getThreatIdentity()));
                    }
                    break;
                case ADSB_VELOCITY:
                    VelocityOverGroundMsg veloc = (VelocityOverGroundMsg)msg;
                    info.put("Velocity",(veloc.hasVelocityInfo() ? veloc.getVelocity() : "unknown"));
                    info.put("Heading",(veloc.hasVelocityInfo() ? veloc.getHeading() : "unknown") + " °");
                    info.put("Vertical rate " , (veloc.hasVerticalRateInfo() ? veloc.getVerticalRate() : "unknown"));
                    if (this.decoder.getAdsbVersion(veloc) == 1) {
                        info.put("has_IFR_capability", veloc.hasIFRCapability());
                    }
                    break;
                case ADSB_TARGET_STATE_AND_STATUS:
                    TargetStateAndStatusMsg tStatus = (TargetStateAndStatusMsg)msg;
                    info.put("navigation_accuracy_category_for_position" , tStatus.getNACp());
                    info.put("has_operational_TCAS" , tStatus.hasOperationalTCAS());
                    info.put("surveillance_integrity_level" , tStatus.getSIL());
                    info.put("has_SIL_supplement" , tStatus.hasSILSupplement());
                    info.put("barometric_altitude_cross-checked" , tStatus.getBarometricAltitudeIntegrityCode());
                    info.put("selected_altitude_is_derived_from", tStatus.isFMSSelectedAltitude() ? "FMS" : "MCP/FCU");
                    if (tStatus.hasSelectedAltitudeInfo()) {
                        info.put("selected_altitude",tStatus.getSelectedAltitude() + " ft");
                    } else {
                        info.put("message"," No selected altitude info");
                    }

                    if (tStatus.hasBarometricPressureSettingInfo()) {
                        info.put("barometric_pressure_setting",tStatus.getBarometricPressureSetting() + " mbar");
                    } else {
                        String message = info.get("message") + "  No barometric pressure setting info";
                        info.put("message",message);
                    }

                    if (tStatus.hasSelectedHeadingInfo()) {
                        info.put("selected_heading",tStatus.getSelectedHeading() + "°");
                    } else {
                        String message = info.get("message") + "    No selected heading info";
                        info.put("message",message);
                    }

                    if (tStatus.hasModeInfo()) {
                        info.put("autopilot_is_enganged", tStatus.hasAutopilotEngaged() ? "" : " not");
                        info.put("VNAV_mode_is_enganged", tStatus.hasVNAVModeEngaged() ? "" : " not");
                        info.put("altitude_hold_mode_is_enganged", tStatus.hasActiveAltitudeHoldMode() ? "" : " not");
                        info.put("approach_mode_is_enganged", tStatus.hasActiveApproachMode() ? "" : " not");
                        info.put("LNAV_mode_is_enganged", tStatus.hasLNAVModeEngaged() ? "" : " not");
                    } else {
                        info.put("message",  " No MCP/FCU mode info");
                    }
                    break;
                case EXTENDED_SQUITTER:
                    info.put("message","Unknown extended squitter with type code " + ((ExtendedSquitter)msg).getFormatTypeCode() + "!");
            }
        }
    }

    public String subStrRowData(String[] adsbData ,String row){
        String head = row.substring(0, 4);
        if(head.equals("1a33")){
            String ifIsAdsb = row.substring(18,20);
            if(ifIsAdsb.equals("8d")){
                adsbData[0] = row.substring(18,46);
                row = row.substring(46,row.length());
                return row;
            }else{
                row = row.substring(4,row.length());
                int index_1a33 = row.indexOf("1a33");
                if(index_1a33 == -1){
                    return "";
                }
                row = row.substring(index_1a33,row.length());
                return subStrRowData(adsbData,row);
            }
        }else{
           int index_1a33 = row.indexOf("1a33");
           if(index_1a33 == -1){
               return "";
           }
            row = row.substring(index_1a33,row.length());
            return subStrRowData(adsbData,row);
        }
    }
    public static byte[] hexStringToByte(String hex) {
        byte[] bytes = new byte[hex.length()/2];
        for(int i=0;i<bytes.length;i++){
            bytes[i]=(byte)Integer.parseInt(hex.substring(
                    i*2,i*2+2),16);
        }
        return bytes;
    }

    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }



    public static void main(String args[]) throws IOException, InterruptedException {
        DatagramSocket ds = new DatagramSocket();
        Position reciver = new Position(112.7103, 28.1556, 23.5);
        AdsbProcess process = new AdsbProcess();
        File file = new File("C:\\Users\\SK\\Desktop\\adsb.txt");//Text文件
        BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
        String s = null;
      //int i= 0;
//       Map result =new HashMap();
        while ((s = br.readLine()) != null) {
 //           String[] adbsData = new String[1];
//            String row = process.subStrRowData(adbsData,s);
 //           while(null!=row&&!"".equals(row)){
               // i++;
//                process.decodeMsg(new Date().getTime(),
//                        adbsData[0],
//                        reciver,
//                        result
//                        );
 //               row = process.subStrRowData(adbsData,row);
                byte[] message = hexStringToByte(s) ;

                String rec = bytesToHexString(message);
                DatagramPacket dp = new DatagramPacket(message,message.length);
                InetAddress address = InetAddress.getLocalHost();
                dp.setAddress(address);
                dp.setPort(4009);
                ds.send(dp);
                Thread.sleep(1500L);
  //          }
//           String jsonStr=  JSONUtil.toJsonStr(result);
//            System.out.println(jsonStr);
//            byte[] message = jsonStr.getBytes();
        }
    }
}
