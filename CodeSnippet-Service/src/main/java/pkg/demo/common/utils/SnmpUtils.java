package pkg.demo.common.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import pkg.demo.common.pojo.CustomException;
import pkg.demo.common.pojo.SnmpUtilsParam;
import pkg.demo.common.pojo.SnmpUtilsParam.SnmpUtilEnum;

/**
 * Get/Walk command in SNMP Version3
 *
 * @author zhajiang
 *
 */
public class SnmpUtils {

	private static final Logger logger = LoggerFactory.getLogger(SnmpUtils.class);

	private Snmp snmp = null;
	private SnmpUtilsParam command = null;
	private Address targetAddress = null;
	private TransportMapping<?> transport = null;

	public SnmpUtils(SnmpUtilsParam command) {
		setCommand(command);
		targetAddress = GenericAddress.parse("udp:" + command.getHost() + "/" + command.getPort());
		if (targetAddress != null && targetAddress.isValid()) {
			OctetString securityName = new OctetString(command.getSecName());
			try {
				snmp = createSession(securityName, command.getAuthProto(), new OctetString(command.getAuthPass()),
						command.getPrivProto(), new OctetString(command.getPrivPass()));
				snmp.listen();
			} catch (IOException e) {
				logger.error("SnmpToolV3 - Snmp Initialization Failed: ", e);
				close();
			}
		} else {
			logger.error("Target address: " + "udp:" + command.getHost() + "/" + command.getPort() + " is not valid");
		}
	}

	public Map<String, String> snmpGet(List<String> oids) {
		if (snmp == null) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		UserTarget target = createUserTarget();
		PDU pdu = createGetPdu(oids);
		try {
			ResponseEvent response = snmp.send(pdu, target);
			if (response != null) {
				return processPdu(response);
			}
		} catch (IOException e) {
			logger.error("SnmpToolV3 - Snmp Send Request Failed: " + e);
		} finally {
			long elapsed = System.currentTimeMillis() - startTime;
			logger.info("SnmpToolV3 - Get command cost : " + elapsed + " milliseconds");
		}
		return null;
	}

	public Map<String, String> snmpWalk(String oid, SnmpUtilEnum mode) {
		Map<String, String> maps = Maps.newHashMap();
		if (snmp == null) {
			return maps;
		}
		UserTarget target = createUserTarget();
		DefaultPDUFactory factory = new DefaultPDUFactory(PDU.GETBULK);
		TreeUtils treeUtils = new TreeUtils(snmp, factory);
		treeUtils.setMaxRepetitions(40);
		List<TreeEvent> events = treeUtils.getSubtree(target, new OID(oid));
		if (events == null || events.size() == 0) {
			logger.error("HostName -> " + command.getHost() + " No events returned.");
			return maps;
		}
		for (TreeEvent event : events) {
			if (event != null) {
				if (event.isError()) {
					if (event.getStatus() == TreeEvent.STATUS_REPORT) {
						throw new CustomException("request failed");
					}
					logger.error("host[" + command.getHost() + "]" + " ,oid [" + oid + "] event->" + event);
				} else {
					VariableBinding[] varBindings = event.getVariableBindings();
					if (varBindings == null || varBindings.length == 0) {
						logger.error("HostName -> " + command.getHost() + " No events returned.");
					} else {
						for (VariableBinding varBinding : varBindings) {
							if (mode == SnmpUtilEnum.VO) {
								maps.put(varBinding.getVariable().toString(), varBinding.getOid().toString());
							} else if (mode == SnmpUtilEnum.OV) {
								maps.put(varBinding.getOid().toString(), varBinding.getVariable().toString());
							}
						}
					}
				}
			}
		}
		return maps;
	}

	private Snmp createSession(OctetString securityName, OID authProtocol, OctetString authPass, OID privacyProtocol,
			OctetString privacyPass) throws IOException {

		transport = new DefaultUdpTransportMapping();
		Snmp snmp = new Snmp(transport);
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);

		UsmUser user = new UsmUser(securityName, authProtocol, authPass, privacyProtocol, privacyPass);
		snmp.getUSM().addUser(securityName, user);
		return snmp;
	}

	// Create PDU for V3
	private PDU createGetPdu(List<String> oids) {
		ScopedPDU pdu = new ScopedPDU();
		pdu.setType(PDU.GET);
		for (String oid : oids) {
			pdu.add(new VariableBinding(new OID(oid)));
		}
		return pdu;
	}

	// Create Target/Agent
	private UserTarget createUserTarget() {
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setVersion(SnmpConstants.version3);
		target.setSecurityLevel(command.getSecLevel());
		target.setSecurityName(new OctetString(command.getSecName()));
		target.setRetries(command.getRetryTimes());
		target.setTimeout(command.getTimeout());
		return target;
	}

	private Map<String, String> processPdu(ResponseEvent response) {
		PDU responsePDU = response.getResponse();
		if (responsePDU != null) {
			int errorStatus = responsePDU.getErrorStatus();
			int errorIndex = responsePDU.getErrorIndex();
			String errorStatusText = responsePDU.getErrorStatusText();
			if (errorStatus == PDU.noError) {
				Map<String, String> map = new HashMap<String, String>();
				Vector<? extends VariableBinding> result = responsePDU.getVariableBindings();
				for (int i = 0; i < result.size(); i++) {
					VariableBinding vb = result.get(i);
					if (vb.isException()) {
						logger.error("Got exception for " + vb.getVariable().getSyntaxString());
					} else {
						String k = vb.getOid().toString();
						String v = vb.getVariable().toString();
						map.put(k, v);
					}
				}
				return map;
			} else {
				Map<String, Object> err = new HashMap<String, Object>();
				err.put("Error:", "Request Failed");
				err.put("Error Status", errorStatus);
				err.put("Error Index", errorIndex);
				err.put("Error Status Text ", errorStatusText);
				logger.error(JSON.toJSONString(err));
			}
		} else {
			logger.error("Error: Response PDU is null");
		}
		return null;
	}

	public SnmpUtilsParam getCommand() {
		return command;
	}

	public void setCommand(SnmpUtilsParam command) {
		this.command = command;
	}

	public Map<String, String> snmpGetAndClose(List<String> oids) {
		Map<String, String> ret = Maps.newHashMap();
		try {
			ret = snmpGet(oids);
		} finally {
			close();
		}
		return ret;
	}

	public Map<String, String> snmpWalkAndClose(String oid, SnmpUtilEnum mode) {
		Map<String, String> ret = Maps.newHashMap();
		int tryTimes = 3;
		try {
			for (int i = 0; i < tryTimes; i++) {
				try {
					ret = snmpWalk(oid, mode);
					break;
				} catch (CustomException e) {
				} catch (Exception e) {
					logger.error("snmpWalk exception at {} loop. {}", i, e);
					break;
				}
			}
		} finally {
			close();
		}
		return ret;
	}

	public void close() {
		try {
			if (transport != null && transport.isListening()) {
				transport.close();
			}
			if (snmp != null) {
				snmp.close();
			}
		} catch (IOException e) {
			snmp = null;
			transport = null;
		}
	}
}
