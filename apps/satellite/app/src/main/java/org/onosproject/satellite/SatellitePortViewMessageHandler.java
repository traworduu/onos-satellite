/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.satellite;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import org.onlab.osgi.ServiceDirectory;
import org.onlab.util.Frequency;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Direction;
import org.onosproject.net.ModulationScheme;
import org.onosproject.net.OchSignal;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.behaviour.PowerConfig;
import org.onosproject.net.behaviour.protection.ProtectedTransportEndpointState;
import org.onosproject.net.behaviour.protection.TransportEndpointState;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.OpticalPathIntent;
import org.onosproject.net.optical.OpticalAnnotations;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiConnection;
import org.onosproject.ui.UiMessageHandler;
import org.onosproject.ui.table.TableModel;
import org.onosproject.ui.table.TableRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.onosproject.net.Device.Type;
import static org.onosproject.net.behaviour.protection.ProtectedTransportEndpointState.ACTIVE_UNKNOWN;
import static org.onosproject.satellite.SatelliteUtil.OPS_OPT_AUTO;
import static org.onosproject.satellite.SatelliteUtil.OPS_OPT_FORCE;
import static org.onosproject.satellite.SatelliteUtil.OPS_OPT_MANUAL;

/**
 * Table-View message handler for ROADM port view.
 */
public class SatellitePortViewMessageHandler extends UiMessageHandler {

    private static final String ROADM_PORT_DATA_REQ = "satellitePortDataRequest";
    private static final String ROADM_PORT_DATA_RESP = "satellitePortDataResponse";
    private static final String ROADM_PORTS = "satellitePorts";
    private static final String ROADM_SET_TARGET_POWER_REQ = "satelliteSetTargetPowerRequest";
    private static final String ROADM_SET_TARGET_POWER_RESP = "satelliteSetTargetPowerResponse";
    private static final String ROADM_SET_MODULATION_REQ = "satelliteSetModulationRequest";
    private static final String ROADM_SET_MODULATION_RESP = "satelliteSetModulationResponse";
    private static final String ROADM_SET_FREQ_REQ = "satelliteSetFrequencyRequest";
    private static final String ROADM_SET_FREQ_RESP = "satelliteSetFrequencyResponse";
    private static final String ROADM_SYNC_TARGET_POWER_REQ = "satelliteSyncTargetPowerRequest";
    private static final String ROADM_SYNC_TARGET_POWER_RESP = "satelliteSyncTargetPowerResp";
    private static final String ROADM_SHOW_ITEMS_REQ = "satelliteShowPortItemsRequest";
    private static final String ROADM_SHOW_ITEMS_RESP = "satelliteShowPortItemsResponse";
    private static final String ROADM_SET_OPS_MODE_REQ = "satelliteSetOpsModeRequest";
    private static final String ROADM_SET_OPS_MODE_RESP = "satelliteSetOpsModeResponse";

    private static final String ID = "id";
    private static final String REVERSE_PORT = "reversePort";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ENABLED = "enabled";
    private static final String MIN_FREQ = "minFreq";
    private static final String MAX_FREQ = "maxFreq";
    private static final String GRID = "grid";
    private static final String CURR_FREQ = "currFreq";
    private static final String POWER_RANGE = "powerRange";
    private static final String CURRENT_POWER = "currentPower";
    private static final String CURRENT_INPUT_POWER = "currentInputPower";
    private static final String TARGET_POWER = "targetPower";
    private static final String MODULATION = "modulation";
    private static final String HAS_TARGET_POWER = "hasTargetPower";
    private static final String SERVICE_STATE = "serviceState";

    private static final String[] COLUMN_IDS = {
            ID, REVERSE_PORT, TYPE, NAME, ENABLED, MIN_FREQ, MAX_FREQ, GRID, CURR_FREQ, POWER_RANGE,
            CURRENT_POWER, CURRENT_INPUT_POWER, SERVICE_STATE, TARGET_POWER, MODULATION, HAS_TARGET_POWER
    };

    private SatelliteService satelliteService;
    private DeviceService deviceService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void init(UiConnection connection, ServiceDirectory directory) {
        super.init(connection, directory);
        satelliteService = get(SatelliteService.class);
        deviceService = get(DeviceService.class);
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(new PortTableDataRequestHandler(),
                new SetTargetPowerRequestHandler(),
                new CreateShowItemsRequestHandler(),
                new CreateOpsModeSetRequestHandler(),
                new SyncTargetPowerRequestHandler(),
                new SetModulationRequestHandler(),
                new SetFrequencyRequestHandler()
        );
    }

    // Handler for sample table requests
    private final class PortTableDataRequestHandler extends TableRequestHandler {

        private PortTableDataRequestHandler() {
            super(ROADM_PORT_DATA_REQ, ROADM_PORT_DATA_RESP, ROADM_PORTS);
        }

        @Override
        protected String[] getColumnIds() {
            return COLUMN_IDS;
        }

        @Override
        protected String noRowsMessage(ObjectNode payload) {
            return SatelliteUtil.NO_ROWS_MESSAGE;
        }

        @Override
        protected void populateTable(TableModel tm, ObjectNode payload) {
            DeviceId deviceId = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            if (deviceService.isAvailable(deviceId)) {
                List<Port> ports = deviceService.getPorts(deviceId);
                for (Port port : ports) {
                    populateRow(tm.addRow(), port, deviceId);
                }
            }
        }

        private void populateRow(TableModel.Row row, Port port, DeviceId deviceId) {
            PortNumber portNum = port.number();
            getFrequencyLimit(deviceId, portNum);
            row.cell(ID, portNum.toLong())
                    .cell(REVERSE_PORT, SatelliteUtil.getAnnotation(port.annotations(),
                            OpticalPathIntent.REVERSE_PORT_ANNOTATION_KEY))
                    .cell(TYPE, port.type())
                    .cell(ENABLED, port.isEnabled())
                    .cell(NAME, SatelliteUtil.getAnnotation(port.annotations(), AnnotationKeys.PORT_NAME))
                    .cell(MIN_FREQ, SatelliteUtil.asTHz(minFreq))
                    .cell(MAX_FREQ, SatelliteUtil.asTHz(maxFreq))
                    .cell(GRID, SatelliteUtil.asGHz(channelSpacing))
                    .cell(CURR_FREQ, getWavelength(deviceId, portNum))
                    .cell(POWER_RANGE, getPowerRange(deviceId, portNum))
                    .cell(CURRENT_POWER, getCurrentPower(deviceId, portNum))
                    .cell(CURRENT_INPUT_POWER, getCurrentInputPower(deviceId, portNum))
                    .cell(SERVICE_STATE, getPortServiceState(deviceId, portNum))
                    .cell(MODULATION, getModulation(deviceId, portNum))
                    .cell(TARGET_POWER, getTargetPower(deviceId, portNum))
                    .cell(HAS_TARGET_POWER, satelliteService.hasPortTargetPower(deviceId, portNum));
        }

        private String getPortServiceState(DeviceId deviceId, PortNumber portNumber) {
            if (deviceService.getDevice(deviceId).type() != Type.FIBER_SWITCH) {
                return SatelliteUtil.NA;
            }
            Map<ConnectPoint, ProtectedTransportEndpointState> map =
                    satelliteService.getProtectionSwitchStates(deviceId);
            for (ProtectedTransportEndpointState state : map.values()) {
                for (TransportEndpointState element : state.pathStates()) {
                    if (element.description().output().connectPoint().port().equals(portNumber)) {
                        return SatelliteUtil.defaultString(element.attributes()
                                .get(OpticalAnnotations.INPUT_PORT_STATUS), SatelliteUtil.UNKNOWN);
                    }
                }
            }
            return SatelliteUtil.UNKNOWN;
        }

        private Frequency minFreq = null, maxFreq = null, channelSpacing = null, currFreq = null;

        // Gets min frequency, max frequency, channel spacing
        private void getFrequencyLimit(DeviceId deviceId, PortNumber portNumber) {
            Set<OchSignal> signals = satelliteService.queryLambdas(deviceId, portNumber);
            if (signals.isEmpty()) {
                return;
            }
            Comparator<OchSignal> compare =
                    (OchSignal a, OchSignal b) -> a.spacingMultiplier() - b.spacingMultiplier();
            OchSignal minOch = Collections.min(signals, compare);
            OchSignal maxOch = Collections.max(signals, compare);
            minFreq = minOch.centralFrequency();
            maxFreq = maxOch.centralFrequency();
            channelSpacing = minOch.channelSpacing().frequency();
        }

        // Returns the power range as a string, N/A if the power range not exists.
        // The power range would be input power range or target power range determined by port property.
        // If the port is RX direction then acquire the input power range from driver.
        // Otherwise there will be a TX direction port, thus acquire the target power range.
        private String getPowerRange(DeviceId deviceId, PortNumber portNumber) {
            Range<Double> range = satelliteService.inputPortPowerRange(deviceId, portNumber);
            if (range == null) {
                range = satelliteService.targetPortPowerRange(deviceId, portNumber);
            }
            return SatelliteUtil.objectToString(range, SatelliteUtil.NA);
        }

        // Returns the current power as a string, Unknown if no value can be found.
        private String getCurrentPower(DeviceId deviceId, PortNumber portNumber) {
            Double currentPower = satelliteService.getCurrentPortPower(deviceId, portNumber);
            return SatelliteUtil.objectToString(currentPower, SatelliteUtil.UNKNOWN);
        }

        // Returns the current input power as a string, Unknown if no value can be found.
        private String getCurrentInputPower(DeviceId deviceId, PortNumber portNumber) {
            PowerConfig powerConfig = deviceService.getDevice(deviceId).as(PowerConfig.class);
            Optional<Double> currentInputPower = powerConfig.currentInputPower(portNumber, Direction.ALL);
            Double inputPowerVal = null;
            if (currentInputPower.isPresent()) {
                inputPowerVal = currentInputPower.orElse(Double.MIN_VALUE);
            }
            return SatelliteUtil.objectToString(inputPowerVal, SatelliteUtil.UNKNOWN);
        }

        // Returns target power as a string, Unknown if target power is expected but
        // cannot be found, N/A if port does not have configurable target power
        private String getTargetPower(DeviceId deviceId, PortNumber portNumber) {
            if (!satelliteService.hasPortTargetPower(deviceId, portNumber)) {
                return SatelliteUtil.NA;
            }
            Double targetPower = satelliteService.getTargetPortPower(deviceId, portNumber);
            return SatelliteUtil.objectToString(targetPower, SatelliteUtil.UNKNOWN);
        }

        // Returns modulation as a string, Unknown if modulation is expected but
        // cannot be found
        private String getModulation(DeviceId deviceId, PortNumber portNumber) {
            Port port = deviceService.getPort(deviceId, portNumber);
            ModulationScheme modulation = null;
            if (port.type().equals(Port.Type.OCH)) {
                modulation = satelliteService.getModulation(deviceId, portNumber);
            }
            return SatelliteUtil.objectToString(modulation, SatelliteUtil.UNKNOWN);
        }

        // Returns modulation as a string, Unknown if modulation is expected but
        // cannot be found
        private String getWavelength(DeviceId deviceId, PortNumber portNumber) {
            Frequency currentFrequency = satelliteService.getWavelength(deviceId, portNumber);
            if (currentFrequency == null) {
                return "0";
            } else {
                return String.valueOf(currentFrequency.asTHz());
            }
        }
    }


    // Handler for setting port target power
    private final class SetTargetPowerRequestHandler extends RequestHandler {

        private static final String TARGET_POWER_ERR_MSG = "Target power range is %s.";

        private SetTargetPowerRequestHandler() {
            super(ROADM_SET_TARGET_POWER_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId deviceId = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            PortNumber portNumber = PortNumber.portNumber(payload.get(ID).asLong());
            Range<Double> range = satelliteService.targetPortPowerRange(deviceId, portNumber);
            if (range == null) {
                log.warn("Unable to determine target power range for device {}", deviceId);
                return;
            }
            Double targetPower = payload.get(TARGET_POWER).asDouble();
            boolean validTargetPower = range.contains(targetPower);
            if (validTargetPower) {
                satelliteService.setTargetPortPower(deviceId, portNumber, targetPower);
            }
            ObjectNode rootNode = objectNode();
            rootNode.put(ID, payload.get(ID).asText());
            rootNode.put(SatelliteUtil.VALID, validTargetPower);
            rootNode.put(SatelliteUtil.MESSAGE, String.format(TARGET_POWER_ERR_MSG, range.toString()));
            sendMessage(ROADM_SET_TARGET_POWER_RESP, rootNode);
        }
    }

    // Handler for sync-up port target power
    private final class SyncTargetPowerRequestHandler extends RequestHandler {

        private static final String SYNCED_TARGET_POWER = "Synced target power is %s.";

        private SyncTargetPowerRequestHandler() {
            super(ROADM_SYNC_TARGET_POWER_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId deviceId = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            PortNumber portNumber = PortNumber.portNumber(payload.get(ID).asLong());
            Double targetPower = satelliteService.syncTargetPortPower(deviceId, portNumber);
            String power = SatelliteUtil.objectToString(targetPower, SatelliteUtil.UNKNOWN);
            ObjectNode rootNode = objectNode();
            rootNode.put(ID, payload.get(ID).asText())
                    .put(SatelliteUtil.VALID, true)
                    .put(SatelliteUtil.MESSAGE, String.format(SYNCED_TARGET_POWER, power));
            sendMessage(ROADM_SYNC_TARGET_POWER_RESP, rootNode);
        }
    }

    // Handler for setting port modulation
    private final class SetModulationRequestHandler extends RequestHandler {

        private static final String TARGET_MODULATION_MSG = "Target modulation is %s.";

        private SetModulationRequestHandler() {
            super(ROADM_SET_MODULATION_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId deviceId = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            PortNumber portNumber = PortNumber.portNumber(payload.get(ID).asLong());
            String modulation = payload.get(MODULATION).asText();
            satelliteService.setModulation(deviceId, portNumber, modulation);
            ObjectNode rootNode = objectNode();
            rootNode.put(ID, payload.get(ID).asText());
            rootNode.put(SatelliteUtil.VALID, modulation);
            rootNode.put(SatelliteUtil.MESSAGE, String.format(TARGET_MODULATION_MSG, modulation));
            sendMessage(ROADM_SET_MODULATION_RESP, rootNode);
        }
    }

    // Handler for setting port frequency
    private final class SetFrequencyRequestHandler extends RequestHandler {

        private static final String TARGET_FREQ_MSG = "Target frequency is %s.";

        private SetFrequencyRequestHandler() {
            super(ROADM_SET_FREQ_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId deviceId = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            PortNumber portNumber = PortNumber.portNumber(payload.get(ID).asLong());
            String frequency = payload.get(CURR_FREQ).asText();
            double freqThz = Double.parseDouble(frequency);
            Frequency freq = Frequency.ofTHz(freqThz);
            OchSignal ochSignal = SatelliteUtil.createOchSignalFromWavelength(freq.asMHz(), deviceService,
                    deviceId, portNumber);
            satelliteService.createConnection(deviceId, 100, true, 0,
                    portNumber, portNumber, ochSignal);
            ObjectNode rootNode = objectNode();
            rootNode.put(ID, payload.get(ID).asText());
            rootNode.put(SatelliteUtil.VALID, frequency);
            rootNode.put(SatelliteUtil.MESSAGE, String.format(TARGET_FREQ_MSG, frequency));
            sendMessage(ROADM_SET_FREQ_RESP, rootNode);
        }
    }

    // Protection switch operation type and path index
    private static final String OPS_ARRAY_INDEX = "index";
    private static final String OPS_ARRAY_OPERATION = "operation";
    private static final String[] OPS_NON_AUTO_OPTS = {OPS_OPT_FORCE, OPS_OPT_MANUAL};

    private final class CreateShowItemsRequestHandler extends RequestHandler {
        private static final String SHOW_TARGET_POWER = "showTargetPower";
        private static final String SHOW_SERVICE_STATE = "showServiceState";
        private static final String SHOW_FLOW_ICON = "showFlowIcon";
        private static final String OPS_PATHS = "opsOperations";
        private static final String OPS_ARRAY_NAME = "name";
        private static final String OPS_GROUP_FMT = "GROUP%d ";

        private CreateShowItemsRequestHandler() {
            super(ROADM_SHOW_ITEMS_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId did = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            Type devType = deviceService.getDevice(did).type();
            // Build response
            ObjectNode node = objectNode();
            node.put(SHOW_FLOW_ICON, devType == Type.ROADM);
            if (devType == Type.FIBER_SWITCH) {
                node.put(SHOW_TARGET_POWER, false);
                node.put(SHOW_SERVICE_STATE, true);
                // add protection switch paths
                putProtectionSwitchPaths(did, node);
            } else {
                node.put(SHOW_TARGET_POWER, true);
                node.put(SHOW_SERVICE_STATE, false);
            }
            sendMessage(ROADM_SHOW_ITEMS_RESP, node);
        }

        private void putProtectionSwitchPaths(DeviceId deviceId, ObjectNode node) {
            Map<ConnectPoint, ProtectedTransportEndpointState> states =
                    satelliteService.getProtectionSwitchStates(deviceId);
            ArrayNode nodes = node.putArray(OPS_PATHS);
            // Add path names for every identifier.
            int groupIndex = 0;
            for (ConnectPoint identifier : states.keySet()) {
                // No group name needed if there is only one connection point identifier.
                String groupName = states.keySet().size() == 1 ? "" : String.format(OPS_GROUP_FMT, ++groupIndex);
                // Add AUTOMATIC operation.
                nodes.add(new ObjectNode(JsonNodeFactory.instance)
                        .put(OPS_ARRAY_INDEX, ACTIVE_UNKNOWN)
                        .put(OPS_ARRAY_OPERATION, OPS_OPT_AUTO)
                        .put(OPS_ARRAY_NAME, String.format("%s%s", groupName, OPS_OPT_AUTO)));
                // Add FORCE and MANUAL operations for every path.
                for (String opt : OPS_NON_AUTO_OPTS) {
                    int pathIndex = 0;
                    for (TransportEndpointState state : states.get(identifier).pathStates()) {
                        nodes.add(new ObjectNode(JsonNodeFactory.instance)
                                .put(OPS_ARRAY_INDEX, pathIndex++)
                                .put(OPS_ARRAY_OPERATION, opt)
                                .put(OPS_ARRAY_NAME,
                                        String.format("%s%s %s", groupName, opt, state.id().id().toUpperCase())));
                    }
                }
            }


        }
    }

    private final class CreateOpsModeSetRequestHandler extends RequestHandler {
        private static final String DEVICE_INVALID_ERR_MSG = "Apply failed: device is offline or unavailable.";
        private static final String TYPE_INVALID_ERR_MSG = "Apply failed: invalid device type.";

        private CreateOpsModeSetRequestHandler() {
            super(ROADM_SET_OPS_MODE_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            DeviceId did = DeviceId.deviceId(string(payload, SatelliteUtil.DEV_ID));
            ObjectNode node = objectNode();
            if (!deviceService.isAvailable(did)) {
                node.put(SatelliteUtil.VALID, false);
                node.put(SatelliteUtil.MESSAGE, DEVICE_INVALID_ERR_MSG);
                sendMessage(ROADM_SET_OPS_MODE_RESP, node);
                return;
            }
            Type devType = deviceService.getDevice(did).type();
            if (devType != Type.FIBER_SWITCH) {
                node.put(SatelliteUtil.VALID, false);
                node.put(SatelliteUtil.MESSAGE, TYPE_INVALID_ERR_MSG);
                sendMessage(ROADM_SET_OPS_MODE_RESP, node);
                return;
            }
            // get switch configuration from payload, and then switch the device.
            satelliteService.configProtectionSwitch(did, string(payload, OPS_ARRAY_OPERATION),
                    satelliteService.getProtectionSwitchStates(did).keySet().toArray(new ConnectPoint[0])[0],
                    (int) number(payload, OPS_ARRAY_INDEX));
            node.put(SatelliteUtil.VALID, true);
            sendMessage(ROADM_SET_OPS_MODE_RESP, node);
        }
    }
}
