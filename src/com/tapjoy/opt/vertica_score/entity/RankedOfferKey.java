package com.tapjoy.opt.vertica_score.entity;

import com.tapjoy.opt.vertica_score.config.Configuration;

public class RankedOfferKey extends Object {
	public String algorithm;
	public Integer platform;
	public String os;
	public String country;
	public String currency_id;
	public String device;
	public String segment;

	public boolean isEquals(Object o1, Object o2) {
		boolean equals = false;

		if ((o1 == null) && (o2 == null)) {
			equals = true;
		} else if ((o1 != null) && (o2 != null)) {
			equals = o1.equals(o2);
		}

		return equals;
	}

	public String toKeyString() {
		StringBuffer buff = new StringBuffer();

		// append key
		if (algorithm != null) {
			buff.append(algorithm);
		}
		buff.append(".");

		if (platform != null) {
			if (platform == Configuration.Platform.OFFERWALL) {
				buff.append("0");
			} else {
				buff.append("1");
			}
		}
		buff.append(".");

		if (os != null) {
			buff.append(os);
		}
		buff.append(".");

		if (country != null) {
			buff.append(country);
		}
		buff.append(".");

		if (currency_id != null) {
			buff.append(currency_id);
		}
		buff.append(".");

		if (device != null) {
			buff.append(device);
		}

		String keyString = buff.toString();
		return keyString;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();

		buff.append(toKeyString());
		
		if (segment != null) {
			buff.append(segment);
		}

		String str = buff.toString();
		return str;
	}

	@Override
	public RankedOfferKey clone() {
		RankedOfferKey clone = new RankedOfferKey();
		clone.algorithm = this.algorithm;
		clone.country = this.country;
		clone.currency_id = this.currency_id;
		clone.device = this.device;
		clone.os = this.os;
		clone.platform = this.platform;
		clone.segment = this.segment;

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals = false;

		if ((obj != null) && (obj instanceof RankedOfferKey)) {
			RankedOfferKey offerKey = (RankedOfferKey) obj;
			equals = true;

			if (isEquals(algorithm, offerKey.algorithm) == false) {
				equals = false;
			} else if (isEquals(platform, offerKey.platform) == false) {
				equals = false;
			} else if (isEquals(os, offerKey.os) == false) {
				equals = false;
			} else if (isEquals(currency_id, offerKey.currency_id) == false) {
				equals = false;
			} else if (isEquals(country, offerKey.country) == false) {
				equals = false;
			} else if (isEquals(device, offerKey.device) == false) {
				equals = false;
			} else if (isEquals(segment, offerKey.segment) == false) {
				equals = false;
			}
		}

		return equals;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;

		if (algorithm != null) {
			hashCode += algorithm.hashCode();
		}

		if (platform != null) {
			hashCode += platform.hashCode();
		}

		if (os != null) {
			hashCode += os.hashCode();
		}

		if (currency_id != null) {
			hashCode += currency_id.hashCode();
		}

		if (country != null) {
			hashCode += country.hashCode();
		}

		if (device != null) {
			hashCode += device.hashCode();
		}

		if (segment != null) {
			hashCode += segment.hashCode();
		}

		return hashCode;
	}
}
