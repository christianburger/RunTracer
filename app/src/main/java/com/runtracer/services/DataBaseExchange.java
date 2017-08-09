package com.runtracer.services;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DataBaseExchange implements Serializable, Cloneable {
	private static final long serialVersionUID = 100L;
	private URL url;
	private String command = "";
	private String accountEmail = "";
	private String full_name = "";
	private String client_id;
	private String client_secret;
	private String grant_type;
	private String hash= "";
	private String method;
	private JSONObject json_data_in;
	private JSONObject json_data_out;
	private SimpleOAuth2Token simpleOAuth2Token;
	private int maxAttempts;
	private int attemptNo;
	private int error_no=0;
	private boolean pending= false;

	@Override
	public Object clone() throws CloneNotSupportedException {
		this.attemptNo++;
		return super.clone();
	}

	private String createHash() {
		String hash= null;
		try {
			hash = this.clone().toString();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		assert hash != null;
		this.hash= String.valueOf((hash.hashCode()));
		return (this.hash);
	}

	private DataBaseExchange() {
		this.createHash();
		this.setMethod("GET");
		this.setAttemptNo(0);
		this.setMaxAttempts(4);
	}

	public static DataBaseExchange createDataBaseExchange() {
		return new DataBaseExchange();
	}

	public void clear() {
			url= null;
			command = null;
			accountEmail = null;
			full_name = null;
			json_data_in = null;
			json_data_out= null;
			error_no=0;
			pending= false;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(this.url);
		out.writeObject(this.command);
		out.writeObject(this.accountEmail);
		out.writeObject(this.full_name);
		out.writeObject(this.hash);

		out.writeObject(this.json_data_in.toString());
		out.writeObject(this.json_data_out.toString());
		out.writeObject(this.error_no);
		out.writeObject(this.pending);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, JSONException {
		this.url= (URL) in.readObject();
		this.command= (String) in.readObject();
		this.accountEmail= (String) in.readObject();
		this.full_name= (String) in.readObject();
		this.hash= (String) in.readObject();

		this.json_data_in= new JSONObject((String) in.readObject());
		this.json_data_out= new JSONObject((String) in.readObject());
		this.error_no= (int) in.readObject();
		this.pending= (boolean) in.readObject();
	}
}
