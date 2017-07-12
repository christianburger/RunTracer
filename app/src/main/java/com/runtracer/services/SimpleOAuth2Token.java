package com.runtracer.services;

import java.util.Date;

public class SimpleOAuth2Token {
	public SimpleOAuth2Token(String token, Date expiry) {
		this.token = token;
		this.expiry = expiry;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	private Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Long timeout) {
		long now= new Date().getTime();
		this.expiry.setTime(now+timeout);
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public boolean isExpired() {
		long now= new Date().getTime();
		return now > this.getExpiry().getTime();
	}

	private String token;
	private Date expiry;
}
