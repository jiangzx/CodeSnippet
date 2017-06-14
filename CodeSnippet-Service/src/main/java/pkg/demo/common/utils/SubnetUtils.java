package pkg.demo.common.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that performs some subnet calculations given a network address and a
 * subnet mask.
 * 
 * @see "http://www.faqs.org/rfcs/rfc1519.html"
 * @since 2.0
 */
public class SubnetUtils {

	private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
	private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,3})";
	private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
	private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
	private static final int NBITS = 32;
	private static final int SUBNET_SIZE_THRESHOLD = 100000;

	private long netmask = 0;
	private long address = 0;
	private long network = 0;
	private long broadcast = 0;

	/** Whether the broadcast/network address are included in host count */
	private boolean inclusiveHostCount = false;

	/**
	 * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
	 * 
	 * @param cidrNotation
	 *            A CIDR-notation string, e.g. "192.168.0.1/16"
	 * @throws IllegalArgumentException
	 *             A. if the parameter is invalid, i.e. does not match n.n.n.n/m
	 *             where n=1-3 decimal digits in range 0-255, m = 1-3 decimal
	 *             digits in range 1-32
	 * 
	 *             B. Check if prefix is valid (verifyPrefix is true) e.g.
	 *             10.120.34.64/25 is not a valid cidr, if prefix is 25, it
	 *             should be 10.120.34.0/25
	 */
	public SubnetUtils(String cidrNotation, boolean verifyPrefix) {
		calculate(cidrNotation);
		if (verifyPrefix) {
			if (!cidrNotation.trim().equals(getInfo().getNetworkAddressCidr())) {
				long prefixLen = getInfo().getPrefixLen();
				throw new IllegalArgumentException(cidrNotation + " has invalid prefix length " + prefixLen
						+ ". The proper format for prefix length " + prefixLen + " is something like: "
						+ getInfo().getNetworkAddressCidr());
			}
		}
	}

	/**
	 * Constructor that takes a dotted decimal address and a dotted decimal
	 * mask.
	 * 
	 * @param address
	 *            An IP address, e.g. "192.168.0.1"
	 * @param mask
	 *            A dotted decimal netmask e.g. "255.255.0.0"
	 * @throws IllegalArgumentException
	 *             if the address or mask is invalid, i.e. does not match
	 *             n.n.n.n where n=1-3 decimal digits and the mask is not all
	 *             zeros
	 */
	public SubnetUtils(String address, String mask) {
		calculate(toCidrNotation(address, mask));
	}

	/**
	 * Returns <code>true</code> if the return value of
	 * {@link SubnetInfo#getAddressCount()} includes the network and broadcast
	 * addresses.
	 * 
	 * @since 2.2
	 * @return true if the hostcount includes the network and broadcast
	 *         addresses
	 */
	public boolean isInclusiveHostCount() {
		return inclusiveHostCount;
	}

	/**
	 * Set to <code>true</code> if you want the return value of
	 * {@link SubnetInfo#getAddressCount()} to include the network and broadcast
	 * addresses.
	 * 
	 * @param inclusiveHostCount
	 *            true if network and broadcast addresses are to be included
	 * @since 2.2
	 */
	public void setInclusiveHostCount(boolean inclusiveHostCount) {
		this.inclusiveHostCount = inclusiveHostCount;
	}

	/**
	 * Convenience container for subnet summary information.
	 * 
	 */
	public final class SubnetInfo {
		/* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
		private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;

		private SubnetInfo() {
		}

		private long netmask() {
			return netmask;
		}

		private long network() {
			return network;
		}

		private long address() {
			return address;
		}

		private long broadcast() {
			return broadcast;
		}

		// long versions of the values (as unsigned int) which are more suitable
		// for range checking
		private long networkLong() {
			return network & UNSIGNED_INT_MASK;
		}

		private long broadcastLong() {
			return broadcast & UNSIGNED_INT_MASK;
		}

		private long low() {
			return isInclusiveHostCount() ? network() : broadcastLong() - networkLong() > 1 ? network() + 1 : 0;
		}

		private long high() {
			return isInclusiveHostCount() ? broadcast() : broadcastLong() - networkLong() > 1 ? broadcast() - 1 : 0;
		}

		/**
		 * Returns true if the parameter <code>address</code> is in the range of
		 * usable endpoint addresses for this subnet. This excludes the network
		 * and broadcast adresses.
		 * 
		 * @param address
		 *            A dot-delimited IPv4 address, e.g. "192.168.0.1"
		 * @return True if in range, false otherwise
		 */
		public boolean isInRange(String address) {
			return isInRange(toInteger(address));
		}

		/**
		 * 
		 * @param address
		 *            the address to check
		 * @return true if it is in range
		 * @since 3.4 (made public)
		 */
		public boolean isInRange(int address) {
			long addLong = address & UNSIGNED_INT_MASK;
			long lowLong = low() & UNSIGNED_INT_MASK;
			long highLong = high() & UNSIGNED_INT_MASK;
			return addLong >= lowLong && addLong <= highLong;
		}

		public String getBroadcastAddress() {
			return format(toArrayLong(broadcast()));
		}

		public String getNetworkAddress() {
			return format(toArrayLong(network()));
		}

		public String getNetmask() {
			return format(toArrayLong(netmask()));
		}

		public String getAddress() {
			return format(toArrayLong(address()));
		}

		/**
		 * Return the low address as a dotted IP address. Will be zero for
		 * CIDR/31 and CIDR/32 if the inclusive flag is false.
		 * 
		 * @return the IP address in dotted format, may be "0.0.0.0" if there is
		 *         no valid address
		 */
		public String getLowAddress() {
			return format(toArrayLong(low()));
		}

		/**
		 * Return the high address as a dotted IP address. Will be zero for
		 * CIDR/31 and CIDR/32 if the inclusive flag is false.
		 * 
		 * @return the IP address in dotted format, may be "0.0.0.0" if there is
		 *         no valid address
		 */
		public String getHighAddress() {
			return format(toArrayLong(high()));
		}

		/**
		 * Return the address by index Will be zero for CIDR/31 and CIDR/32 if
		 * the inclusive flag is false.
		 * 
		 * @author Wallace Zhang
		 * @return the IP address in dotted format, may be "0.0.0.0" if there is
		 *         no valid address
		 */
		public String getAddressByIndex(long index) {
			return format(toArrayLong(low() + index));
		}

		/**
		 * Get the count of available addresses. Will be zero for CIDR/31 and
		 * CIDR/32 if the inclusive flag is false.
		 * 
		 * @return the count of addresses, may be zero.
		 * @throws RuntimeException
		 *             if the correct count is greater than
		 *             {@code Integer.MAX_VALUE}
		 * @deprecated (3.4) use {@link #getAddressCountLong()} instead
		 */
		@Deprecated
		public int getAddressCount() {
			long countLong = getAddressCountLong();
			if (countLong > Integer.MAX_VALUE) {
				throw new RuntimeException("Count is larger than an integer: " + countLong);
			}
			// N.B. cannot be negative
			return (int) countLong;
		}

		/**
		 * Get the count of available addresses. Will be zero for CIDR/31 and
		 * CIDR/32 if the inclusive flag is false.
		 * 
		 * @return the count of addresses, may be zero.
		 * @since 3.4
		 */
		public long getAddressCountLong() {
			long b = broadcastLong();
			long n = networkLong();
			long count = b - n + (isInclusiveHostCount() ? 1 : -1);
			return count < 0 ? 0 : count;
		}

		public int asInteger(String address) {
			return toInteger(address);
		}

		public String getCidrSignature() {
			return toCidrNotation(format(toArrayLong(address())), format(toArrayLong(netmask())));
		}

		/**
		 * @author Wallace Zhang
		 * @return all the subnet addresses
		 */
		public List<String> getAllAddresses() {
			return getAllAddressesByIndex(0, getAddressCountLong() - 1);
		}

		/**
		 * @author Wallace Zhang
		 * @return subnet addresses by start and end index
		 */
		public List<String> getAllAddressesByIndex(long start, long end) {
			long ct = getAddressCountLong();
			long maxEndIndex = getAddressCountLong() - 1;
			if (start < 0 || end > maxEndIndex) {
				throw new RuntimeException("getAllAddressesByIndex start index and end index should be between 0 to "
						+ maxEndIndex);
			}
			if (ct > SUBNET_SIZE_THRESHOLD) {
				throw new RuntimeException("Subnet mask = " + getPrefixLen() + ", which support " + ct
						+ " subnet IP Addresses, is not practical. Please choose a higher value.");
			}
			List<String> list = new ArrayList<String>();
			for (long j = start; j <= end; j++) {
				list.add(getAddressByIndex(j));
			}

			return list;
		}

		/**
		 * @author Wallace Zhang
		 * @return prefix length i.e. 10.120.34.64/26 -> 26
		 */
		public long getPrefixLen() {
			return pop(toInteger(format(toArrayLong(netmask()))));
		}

		/**
		 * @author Wallace Zhang
		 * @return the size of subnet
		 */
		public long getSubnetSize() {
			if (getPrefixLen() <= 29) {
				return getAddressCountLong() - 3;
			} else {
				return getAddressCountLong();
			}
		}

		/**
		 * @author Wallace Zhang
		 * @return the value for reverse lookup/PTR records as RFC 2317 look
		 *         alike
		 */
		public String getReverseName() {
			// 10.120.34.64
			String s = getNetworkAddress();

			// 10.120.34.64 -> [10, 120, 34, 64]
			String[] s_split = s.split("\\.");

			// [10, 120, 34, 64] -> [64, 34, 120, 10]
			List<String> list = Arrays.asList(s_split);
			Collections.reverse(list);
			s_split = (String[]) list.toArray();

			// 10.120.34.64/26 -> 26
			long prefixLen = getPrefixLen();

			int offset = (int) (prefixLen / 8);

			// 64-127.
			String head = "";
			if (prefixLen % 8 != 0) {
				head = s_split[3 - offset] + "-" + getHighAddress().split("\\.")[3];
				head = head + ".";
			}

			// 34.120.10.
			String tail = "";
			int first_byte_index = 4 - offset;
			for (int i = first_byte_index; i < s_split.length; i++) {
				tail = tail + s_split[i] + ".";
			}

			// 64-127.34.120.10.in-addr.arpa.
			if (tail.equals("")) {
				return head + tail + ".in-addr.arpa.";
			} else {
				return head + tail + "in-addr.arpa.";
			}
		}

		/**
		 * @author Wallace Zhang
		 * @return the gateway address
		 */
		public String getGateway() {
			if (getPrefixLen() >= 31) {
				return getAddressByIndex(0);
			} else {
				return getAddressByIndex(1);
			}
		}

		/**
		 * @author Wallace Zhang
		 * @return the network address in CIDR format
		 */
		public String getNetworkAddressCidr() {
			boolean orgInclusiveHostCount = inclusiveHostCount;
			String cidrMask = "";
			if (getPrefixLen() < 32) {
				cidrMask = "/" + getPrefixLen();
			}

			inclusiveHostCount = true;
			String networkAddressCidr = format(toArrayLong(network())) + cidrMask;
			inclusiveHostCount = orgInclusiveHostCount;

			return networkAddressCidr;
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @since 2.2
		 */
		@Override
		public String toString() {
			final StringBuilder buf = new StringBuilder();
			buf.append("CIDR Signature:\t[").append(getCidrSignature()).append("]").append("Network CIDR:\t[")
					.append(getNetworkAddressCidr()).append("]\n").append("Network:\t[").append(getNetworkAddress())
					.append("]\n").append("First Address:\t[").append(getLowAddress()).append("]\n")
					.append("Last Address:\t[").append(getHighAddress()).append("]\n").append("# Addresses:\t[")
					.append(getAddressCountLong()).append("]\n").append("Gateway: [").append(getGateway())
					.append("]\n").append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n")
					.append("Netmask: [").append(getNetmask()).append("]\n").append("Zone File:\t[")
					.append(getReverseName()).append("]\n");
			return buf.toString();
		}
	}

	/**
	 * Return a {@link SubnetInfo} instance that contains subnet-specific
	 * statistics
	 * 
	 * @return new instance
	 */
	public final SubnetInfo getInfo() {
		return new SubnetInfo();
	}

	/*
	 * Initialize the internal fields from the supplied CIDR mask
	 */
	private void calculate(String mask) {
		Matcher matcher = cidrPattern.matcher(mask);

		if (matcher.matches()) {
			address = matchAddress(matcher);

			/* Create a binary netmask from the number of bits specification /x */
			int cidrPart = rangeCheck(Integer.parseInt(matcher.group(5)), 0, NBITS);
			for (int j = 0; j < cidrPart; ++j) {
				netmask |= 1 << 31 - j;
			}

			/* Calculate base network address */
			network = address & netmask;

			/* Calculate broadcast address */
			broadcast = network | ~netmask;
		} else {
			throw new IllegalArgumentException("Invalid CIDR. The proper format is xxx.xxx.xxx.xxx/xxx");
		}
	}

	/*
	 * Convert a dotted decimal format address to a packed integer format
	 */
	private int toInteger(String address) {
		Matcher matcher = addressPattern.matcher(address);
		if (matcher.matches()) {
			return matchAddress(matcher);
		} else {
			throw new IllegalArgumentException("Could not parse [" + address + "]");
		}
	}
	
	/*
	 * Convenience method to extract the components of a dotted decimal address
	 * and pack into an integer using a regex match
	 */
	private int matchAddress(Matcher matcher) {
		int addr = 0;
		for (int i = 1; i <= 4; ++i) {
			int n = rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255);
			addr |= (n & 0xff) << 8 * (4 - i);
		}
		return addr;
	}

	/*
	 * Convert a packed integer address into a 4-element array
	 */
	@Deprecated
	private int[] toArray(int val) {
		int ret[] = new int[4];
		for (int j = 3; j >= 0; --j) {
			ret[j] |= val >>> 8 * (3 - j) & 0xff;
		}
		return ret;
	}

	/**
	 * @author Wallace Zhang Replace deprecated toArray method
	 */
	private int[] toArrayLong(long val) {
		int ret[] = new int[4];
		for (int j = 3; j >= 0; --j) {
			ret[j] |= val >>> 8 * (3 - j) & 0xff;
		}
		return ret;
	}

	/*
	 * Convert a 4-element array into dotted decimal format
	 */
	private String format(int[] octets) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < octets.length; ++i) {
			str.append(octets[i]);
			if (i != octets.length - 1) {
				str.append(".");
			}
		}
		return str.toString();
	}

	/*
	 * Convenience function to check integer boundaries. Checks if a value x is
	 * in the range [begin,end]. Returns x if it is in range, throws an
	 * exception otherwise.
	 */
	private int rangeCheck(int value, int begin, int end) {
		if (value >= begin && value <= end) { // (begin,end]
			return value;
		}

		throw new IllegalArgumentException("Value [" + value + "] not in range [" + begin + "," + end + "]");
	}

	/*
	 * Count the number of 1-bits in a 32-bit integer using a divide-and-conquer
	 * strategy see Hacker's Delight section 5.1
	 */
	int pop(int x) {
		x = x - (x >>> 1 & 0x55555555);
		x = (x & 0x33333333) + (x >>> 2 & 0x33333333);
		x = x + (x >>> 4) & 0x0F0F0F0F;
		x = x + (x >>> 8);
		x = x + (x >>> 16);
		return x & 0x0000003F;
	}

	/*
	 * Convert two dotted decimal addresses to a single xxx.xxx.xxx.xxx/yy
	 * format by counting the 1-bit population in the mask address. (It may be
	 * better to count NBITS-#trailing zeroes for this case)
	 */
	private String toCidrNotation(String addr, String mask) {
		return addr + "/" + pop(toInteger(mask));
	}

}
