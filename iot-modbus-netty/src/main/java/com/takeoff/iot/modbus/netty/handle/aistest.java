package com.takeoff.iot.modbus.netty.handle;

import com.takeoff.iot.modbus.netty.handle.gps.GPSAnalysis;
import com.takeoff.iot.modbus.netty.handle.gps.GPSInfo;
import dk.tbsalling.aismessages.AISInputStreamReader;
import dk.tbsalling.aismessages.ais.messages.Metadata;
import dk.tbsalling.aismessages.ais.messages.types.AISMessageType;
import dk.tbsalling.aismessages.ais.messages.types.MMSI;
import dk.tbsalling.aismessages.nmea.messages.NMEAMessage;
import org.opensky.libadsb.ModeSDecoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.*;
import org.opensky.libadsb.tools;
import java.io.*;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;


public class aistest {

    public static void main(String[] args) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(demoNmeaStrings.getBytes());
        System.out.println("AISMessages Demo App");
        System.out.println("--------------------");
        AISInputStreamReader streamReader = new AISInputStreamReader(inputStream, aisMessage ->{
                MMSI mmsi = aisMessage.getSourceMmsi();
                NMEAMessage[] nmeaMessages =  aisMessage.getNmeaMessages();
                AISMessageType messageType = aisMessage.getMessageType();
                Map<String, Object> fields = aisMessage.dataFields();
                Metadata metadata = aisMessage.getMetadata();
                Integer repeatIndicator =  aisMessage.getRepeatIndicator();
                 aisMessage.toString();
                System.out.println("Received AIS message from MMSI " + aisMessage.getSourceMmsi().getMMSI() + ": " + aisMessage);
            }
        );
        streamReader.run();
        String gnrmc  = "$GNRMC,160346.000,A,2935.165173,N,11316.266391,E,0.000,330.24,200222,,E,A*02";
         GPSInfo  gpsInfo = GPSAnalysis.GNRMCAnalysis(gnrmc);
         log.println(gpsInfo);

        Position rec = new Position(0.0D, 0.0D, 0.0D);

        String fileName = "C:\\\\Users\\\\888\\\\Desktop\\\\spd";
        //java 8中这样写也可以
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
            byte[] line;
            while ((line = br.readLine().getBytes()) != null) {
//                System.out.println(line);
                aistest  a = new aistest();
                try {
                    ModeSReply r = a.decoderm.decode(line);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }


//        if (values.length == 5) {
//            rec.setLatitude(Double.parseDouble(values[2]));
//            rec.setLongitude(Double.parseDouble(values[3]));
//            a.decodeMsg((long)Double.parseDouble(values[0]) * 1000L, values[4], rec);
//        } else if (values.length == 4) {
//            rec.setLatitude(Double.parseDouble(values[1]));
//            rec.setLongitude(Double.parseDouble(values[2]));
//            a.decodeMsg((long)Double.parseDouble(values[0]) * 1000L, values[3], rec);
//        } else if (values.length == 2) {
//            a.decodeMsg((long)Double.parseDouble(values[0]) * 1000L, values[1], (Position)null);
//        }
    }


    private final static String demoNmeaStrings = new String(
            //"!AIVDM,1,1,,B,B6:k>eP09j1anCT?@f:35`v021Mk,0*67"
            "!AIVDM,1,1,,B,H6:`UK0p4pH4pOS7H0000000000,2*76"
    );


    private ModeSDecoder decoderm = new ModeSDecoder();

    public void decodeMsg(long timestamp, byte[] adsbData, Position receiver) {
        ModeSReply msg;
        try {
             msg = decoderm.decode(adsbData);
            //msg = this.decoderm.decode(raw);
        } catch (BadFormatException var21) {
            System.out.println("Malformed message! Skipping it. Message: " + var21.getMessage());
            return;
        } catch (UnspecifiedFormatError var22) {
            System.out.println("Unspecified message! Skipping it...");
            return;
        }

        String icao24 = tools.toHexString(msg.getIcao24());

        if (!tools.isZero(msg.getParity()) && !msg.checkParity()) {
            if (msg.getDownlinkFormat() != 17) {
                switch(msg.getType()) {
                    case MODES_REPLY:
                        System.out.println("[" + icao24 + "]: Unknown message with DF " + msg.getDownlinkFormat());
                        break;
                    case SHORT_ACAS:
                        ShortACAS acas = (ShortACAS)msg;
                        System.out.println("[" + icao24 + "]: Altitude is " + acas.getAltitude() + "ft and ACAS is " + (acas.hasOperatingACAS() ? "operating." : "not operating."));
                        System.out.println("          A/C is " + (acas.isAirborne() ? "airborne" : "on the ground") + " and sensitivity level is " + acas.getSensitivityLevel());
                        break;
                    case ALTITUDE_REPLY:
                        AltitudeReply alti = (AltitudeReply)msg;
                        System.out.println("[" + icao24 + "]: Short altitude reply: " + alti.getAltitude() + "ft");
                        break;
                    case IDENTIFY_REPLY:
                        IdentifyReply identify = (IdentifyReply)msg;
                        System.out.println("[" + icao24 + "]: Short identify reply: " + identify.getIdentity());
                        break;
                    case ALL_CALL_REPLY:
                        AllCallReply allcall = (AllCallReply)msg;
                        System.out.println("[" + icao24 + "]: All-call reply for " + tools.toHexString(allcall.getInterrogatorCode()) + " (" + (allcall.hasValidInterrogatorCode() ? "valid" : "invalid") + ")");
                        break;
                    case LONG_ACAS:
                        LongACAS long_acas = (LongACAS)msg;
                        System.out.println("[" + icao24 + "]: Altitude is " + long_acas.getAltitude() + "ft and ACAS is " + (long_acas.hasOperatingACAS() ? "operating." : "not operating."));
                        System.out.println("          A/C is " + (long_acas.isAirborne() ? "airborne" : "on the ground") + " and sensitivity level is " + long_acas.getSensitivityLevel());
                        System.out.println("          RAC is " + (long_acas.hasValidRAC() ? "valid" : "not valid") + " and is " + long_acas.getResolutionAdvisoryComplement() + " (MTE=" + long_acas.hasMultipleThreats() + ")");
                        System.out.println("          Maximum airspeed is " + long_acas.getMaximumAirspeed() + "kn.");
                        break;
                    case MILITARY_EXTENDED_SQUITTER:
                        MilitaryExtendedSquitter mil = (MilitaryExtendedSquitter)msg;
                        System.out.println("[" + icao24 + "]: Military ES of application " + mil.getApplicationCode());
                        System.out.println("          Message is 0x" + tools.toHexString(mil.getMessage()));
                        break;
                    case COMM_B_ALTITUDE_REPLY:
                        CommBAltitudeReply commBaltitude = (CommBAltitudeReply)msg;
                        System.out.println("[" + icao24 + "]: Long altitude reply: " + commBaltitude.getAltitude() + "ft");
                        break;
                    case COMM_B_IDENTIFY_REPLY:
                        CommBIdentifyReply commBidentify = (CommBIdentifyReply)msg;
                        System.out.println("[" + icao24 + "]: Long identify reply: " + commBidentify.getIdentity());
                        break;
                    case COMM_D_ELM:
                        CommDExtendedLengthMsg commDELM = (CommDExtendedLengthMsg)msg;
                        System.out.println("[" + icao24 + "]: ELM message w/ sequence no " + commDELM.getSequenceNumber() + " (ACK: " + commDELM.isAck() + ")");
                        System.out.println("          Message is 0x" + tools.toHexString(commDELM.getMessage()));
                }
            } else {
                System.out.println("Message contains biterrors.");
            }
        } else {
            switch(msg.getType()) {
                case ADSB_AIRBORN_POSITION_V0:
                case ADSB_AIRBORN_POSITION_V1:
                case ADSB_AIRBORN_POSITION_V2:
                    AirbornePositionV0Msg ap0 = (AirbornePositionV0Msg)msg;
                    System.out.print("[" + icao24 + "]: ");
                    Position c0 = this.decoderm.decodePosition(timestamp, ap0, receiver);
                    if (c0 == null) {
                        System.out.println("Cannot decode position yet.");
                    } else {
                        System.out.println("Now at position (" + c0.getLatitude() + "," + c0.getLongitude() + ")");
                    }

                    System.out.println("          Horizontal containment radius limit/protection level: " + ap0.getHorizontalContainmentRadiusLimit() + " m");
                    if (ap0.isBarometricAltitude()) {
                        System.out.println("          Altitude (barom.): " + (ap0.hasAltitude() ? ap0.getAltitude() : "unknown") + " ft");
                    } else {
                        System.out.println("          Height (geom.): " + (ap0.hasAltitude() ? ap0.getAltitude() : "unknown") + " ft");
                    }

                    Integer geoMinusBaro = this.decoderm.getGeoMinusBaro(msg);
                    if (ap0.hasAltitude() && ap0.isBarometricAltitude() && geoMinusBaro != null) {
                        System.out.println("          Height (geom.): " + ap0.getAltitude() + geoMinusBaro + " ft");
                    }

                    System.out.println("          Navigation Integrity Category: " + ap0.getNIC());
                    System.out.println("          Surveillance status: " + ap0.getSurveillanceStatusDescription());
                    switch(msg.getType()) {
                        case ADSB_AIRBORN_POSITION_V0:
                            System.out.println("          Navigation Accuracy Category for position (NACp): " + ap0.getNACp());
                            System.out.println("          Position Uncertainty (based on NACp): " + ap0.getPositionUncertainty());
                            System.out.println("          Surveillance Integrity Level (SIL): " + ap0.getSIL());
                            return;
                        case ADSB_AIRBORN_POSITION_V1:
                            AirbornePositionV1Msg ap1 = (AirbornePositionV1Msg)msg;
                            System.out.println("          NIC supplement A set: " + ap1.hasNICSupplementA());
                            return;
                        case ADSB_AIRBORN_POSITION_V2:
                            AirbornePositionV2Msg ap2 = (AirbornePositionV2Msg)msg;
                            System.out.println("          NIC supplement A set: " + ap2.hasNICSupplementA());
                            System.out.println("          NIC supplement B set: " + ap2.hasNICSupplementB());
                            return;
                        default:
                            return;
                    }
                case ADSB_SURFACE_POSITION_V0:
                case ADSB_SURFACE_POSITION_V1:
                case ADSB_SURFACE_POSITION_V2:
                    SurfacePositionV0Msg sp0 = (SurfacePositionV0Msg)msg;
                    System.out.print("[" + icao24 + "]: ");
                    Position sPos0 = this.decoderm.decodePosition(timestamp, sp0, receiver);
                    if (sPos0 == null) {
                        System.out.println("Cannot decode position yet or no reference available (yet).");
                    } else {
                        System.out.println("Now at position (" + sPos0.getLatitude() + "," + sPos0.getLongitude() + ")");
                    }

                    System.out.println("          Horizontal containment radius limit/protection level is " + sp0.getHorizontalContainmentRadiusLimit() + "m");
                    if (sp0.hasValidHeading()) {
                        System.out.println("          Heading: " + sp0.getHeading() + "°");
                    }

                    System.out.println("          Airplane is on the ground.");
                    if (sp0.hasGroundSpeed()) {
                        System.out.println("          Ground speed: " + sp0.getGroundSpeed() + "kt");
                        System.out.println("          Ground speed resolution: " + sp0.getGroundSpeedResolution() + "kt");
                    }

                    switch(msg.getType()) {
                        case ADSB_SURFACE_POSITION_V0:
                            System.out.println("          Navigation Accuracy Category for position (NACp): " + sp0.getNACp());
                            System.out.println("          Position Uncertainty (based on NACp): " + sp0.getPositionUncertainty() + "m");
                            System.out.println("          Surveillance Integrity Level (SIL): " + sp0.getSIL());
                            return;
                        case ADSB_SURFACE_POSITION_V1:
                            SurfacePositionV1Msg sp1 = (SurfacePositionV1Msg)msg;
                            System.out.println("          NIC supplement A set: " + sp1.hasNICSupplementA());
                            return;
                        case ADSB_SURFACE_POSITION_V2:
                            SurfacePositionV2Msg sp2 = (SurfacePositionV2Msg)msg;
                            System.out.println("          NIC supplement A set: " + sp2.hasNICSupplementA());
                            System.out.println("          NIC supplement C set: " + sp2.hasNICSupplementC());
                            return;
                        default:
                            return;
                    }
                case ADSB_EMERGENCY:
                    EmergencyOrPriorityStatusMsg status = (EmergencyOrPriorityStatusMsg)msg;
                    System.out.println("[" + icao24 + "]: " + status.getEmergencyStateText());
                    System.out.println("          Mode A code is " + status.getModeACode()[0] + status.getModeACode()[1] + status.getModeACode()[2] + status.getModeACode()[3]);
                    break;
                case ADSB_AIRSPEED:
                    AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg)msg;
                    System.out.println("[" + icao24 + "]: Airspeed: " + (airspeed.hasAirspeedInfo() ? airspeed.getAirspeed() + " kt" : "unkown"));
                    if (this.decoderm.getAdsbVersion(msg) == 0) {
                        System.out.println("          Heading: " + airspeed.getHeading() + "° relative to " + (airspeed.hasHeadingStatusFlag() ? "magnetic north" : "true north"));
                    } else {
                        System.out.println("          Heading: " + (airspeed.hasHeadingStatusFlag() ? airspeed.getHeading() + "°" : "unkown"));
                    }

                    if (airspeed.hasVerticalRateInfo()) {
                        System.out.println("          Vertical rate: " + (airspeed.hasVerticalRateInfo() ? airspeed.getVerticalRate() + " ft/min" : "unkown"));
                    }
                    break;
                case ADSB_IDENTIFICATION:
                    IdentificationMsg ident = (IdentificationMsg)msg;
                    System.out.println("[" + icao24 + "]: Callsign: " + new String(ident.getIdentity()));
                    System.out.println("          Category: " + ident.getCategoryDescription());
                    break;
                case ADSB_STATUS_V0:
                    OperationalStatusV0Msg opstat0 = (OperationalStatusV0Msg)msg;
                    System.out.println("[" + icao24 + "]: Using ADS-B version " + opstat0.getVersion());
                    System.out.println("          Has operational TCAS: " + opstat0.hasOperationalTCAS());
                    System.out.println("          Has operational CDTI: " + opstat0.hasOperationalCDTI());
                    break;
                case ADSB_AIRBORN_STATUS_V1:
                case ADSB_AIRBORN_STATUS_V2:
                    AirborneOperationalStatusV1Msg opstatA1 = (AirborneOperationalStatusV1Msg)msg;
                    System.out.println("[" + icao24 + "]: Using ADS-B version " + opstatA1.getVersion());
                    System.out.println("          Barometric altitude cross-checked: " + opstatA1.getBarometricAltitudeIntegrityCode());
                    System.out.println("          Gemoetric vertical accuracy: " + opstatA1.getGeometricVerticalAccuracy() + "m");
                    if (opstatA1.getHorizontalReferenceDirection()) {
                        System.out.println("          Horizontal reference: true north");
                    } else {
                        System.out.println("          Horizontal reference: true north");
                    }

                    System.out.println("          Navigation Accuracy Category for position (NACp): " + opstatA1.getNACp());
                    System.out.println("          Position Uncertainty (based on NACp): " + opstatA1.getPositionUncertainty());
                    System.out.println("          Has NIC supplement A: " + opstatA1.hasNICSupplementA());
                    System.out.println("          Surveillance/Source Integrity Level (SIL): " + opstatA1.getSIL());
                    System.out.println("          System design assurance: " + opstatA1.getSystemDesignAssurance());
                    System.out.println("          Has 1090ES in: " + opstatA1.has1090ESIn());
                    System.out.println("          IDENT switch active: " + opstatA1.hasActiveIDENTSwitch());
                    System.out.println("          Has operational TCAS: " + opstatA1.hasOperationalTCAS());
                    System.out.println("          Has TCAS resolution advisory: " + opstatA1.hasTCASResolutionAdvisory());
                    System.out.println("          Has UAT in: " + opstatA1.hasUATIn());
                    System.out.println("          Uses single antenna: " + opstatA1.hasSingleAntenna());
                    System.out.println("          Supports air-referenced velocity reports: " + opstatA1.hasAirReferencedVelocity());
                    if (msg instanceof AirborneOperationalStatusV2Msg) {
                        System.out.println("          Has SIL supplement: " + ((AirborneOperationalStatusV2Msg)msg).hasSILSupplement());
                    }
                    break;
                case ADSB_SURFACE_STATUS_V1:
                case ADSB_SURFACE_STATUS_V2:
                    SurfaceOperationalStatusV1Msg opstatS1 = (SurfaceOperationalStatusV1Msg)msg;
                    System.out.println("[" + icao24 + "]: Using ADS-B version " + opstatS1.getVersion());
                    System.out.println("          Gemoetric vertical accuracy: " + opstatS1.getGeometricVerticalAccuracy() + "m");
                    if (opstatS1.getHorizontalReferenceDirection()) {
                        System.out.println("          Horizontal reference: true north");
                    } else {
                        System.out.println("          Horizontal reference: true north");
                    }

                    System.out.println("          Navigation Accuracy Category for position (NACp): " + opstatS1.getNACp());
                    System.out.println("          Position Uncertainty (based on NACp): " + opstatS1.getPositionUncertainty());
                    System.out.println("          Has NIC supplement A: " + opstatS1.hasNICSupplementA());
                    System.out.println("          Has NIC supplement C: " + opstatS1.getNICSupplementC());
                    System.out.println("          Surveillance/Source Integrity Level (SIL): " + opstatS1.getSIL());
                    System.out.println("          System design assurance: " + opstatS1.getSystemDesignAssurance());
                    System.out.println("          Has 1090ES in: " + opstatS1.has1090ESIn());
                    System.out.println("          IDENT switch active: " + opstatS1.hasActiveIDENTSwitch());
                    System.out.println("          Has TCAS resolution advisory: " + opstatS1.hasTCASResolutionAdvisory());
                    System.out.println("          Has UAT in: " + opstatS1.hasUATIn());
                    System.out.println("          Uses single antenna: " + opstatS1.hasSingleAntenna());
                    System.out.println("          Airplane length: " + opstatS1.getAirplaneLength() + "m");
                    System.out.println("          Airplane width: " + opstatS1.getAirplaneWidth() + "m");
                    System.out.println("          Navigation Accuracy Category for velocity (NACv): " + opstatS1.getNACv());
                    System.out.println("          Low (<70W) TX power: " + opstatS1.hasLowTxPower());
                    System.out.println("          Encoded GPS antenna offset: " + opstatS1.getGPSAntennaOffset());
                    System.out.println("          Has track heading info: " + opstatS1.hasTrackHeadingInfo());
                    if (msg instanceof SurfaceOperationalStatusV2Msg) {
                        System.out.println("          Has SIL supplement: " + ((SurfaceOperationalStatusV2Msg)msg).hasSILSupplement());
                    }
                    break;
                case ADSB_TCAS:
                    TCASResolutionAdvisoryMsg tcas = (TCASResolutionAdvisoryMsg)msg;
                    System.out.println("[" + icao24 + "]: TCAS Resolution Advisory completed: " + tcas.hasRATerminated());
                    System.out.println("          Threat type is " + tcas.getThreatType());
                    if (tcas.getThreatType() == 1) {
                        System.out.println("          Threat identity is 0x" + String.format("%06x", tcas.getThreatIdentity()));
                    }
                    break;
                case ADSB_VELOCITY:
                    VelocityOverGroundMsg veloc = (VelocityOverGroundMsg)msg;
                    System.out.println("[" + icao24 + "]: Velocity: " + (veloc.hasVelocityInfo() ? veloc.getVelocity() : "unknown") + " kt");
                    System.out.println("          Heading: " + (veloc.hasVelocityInfo() ? veloc.getHeading() : "unknown") + " °");
                    System.out.println("          Vertical rate: " + (veloc.hasVerticalRateInfo() ? veloc.getVerticalRate() : "unknown") + " ft/min");
                    if (this.decoderm.getAdsbVersion(veloc) == 1) {
                        System.out.println("          Has IFR capability: " + veloc.hasIFRCapability());
                    }
                    break;
                case ADSB_TARGET_STATE_AND_STATUS:
                    TargetStateAndStatusMsg tStatus = (TargetStateAndStatusMsg)msg;
                    System.out.println("[" + icao24 + "]: Target State and Status reported");
                    System.out.println("          Navigation Accuracy Category for position (NACp): " + tStatus.getNACp());
                    System.out.println("          Has operational TCAS: " + tStatus.hasOperationalTCAS());
                    System.out.println("          Surveillance/Source Integrity Level (SIL): " + tStatus.getSIL());
                    System.out.println("          Has SIL supplement: " + tStatus.hasSILSupplement());
                    System.out.println("          Barometric altitude cross-checked: " + tStatus.getBarometricAltitudeIntegrityCode());
                    System.out.printf("          Selected altitude is derived from %s\n", tStatus.isFMSSelectedAltitude() ? "FMS" : "MCP/FCU");
                    if (tStatus.hasSelectedAltitudeInfo()) {
                        System.out.println("          Selected altitude: " + tStatus.getSelectedAltitude() + " ft");
                    } else {
                        System.out.println("          No selected altitude info");
                    }

                    if (tStatus.hasBarometricPressureSettingInfo()) {
                        System.out.println("          Barometric pressure setting (minus 800 mbar): " + tStatus.getBarometricPressureSetting() + " mbar");
                    } else {
                        System.out.println("          No barometric pressure setting info");
                    }

                    if (tStatus.hasSelectedHeadingInfo()) {
                        System.out.println("          Selected heading: " + tStatus.getSelectedHeading() + "°");
                    } else {
                        System.out.println("          No selected heading info");
                    }

                    if (tStatus.hasModeInfo()) {
                        System.out.printf("          Autopilot is%s enganged\n", tStatus.hasAutopilotEngaged() ? "" : " not");
                        System.out.printf("          VNAV mode is%s enganged\n", tStatus.hasVNAVModeEngaged() ? "" : " not");
                        System.out.printf("          Altitude hold mode is%s enganged\n", tStatus.hasActiveAltitudeHoldMode() ? "" : " not");
                        System.out.printf("          Approach mode is%s enganged\n", tStatus.hasActiveApproachMode() ? "" : " not");
                        System.out.printf("          LNAV mode is%s enganged\n", tStatus.hasLNAVModeEngaged() ? "" : " not");
                    } else {
                        System.out.println("          No MCP/FCU mode info");
                    }
                    break;
                case EXTENDED_SQUITTER:
                    System.out.println("[" + icao24 + "]: Unknown extended squitter with type code " + ((ExtendedSquitter)msg).getFormatTypeCode() + "!");
            }
        }

    }
}

