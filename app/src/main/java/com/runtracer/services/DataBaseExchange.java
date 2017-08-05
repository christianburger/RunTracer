package com.runtracer.services;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
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

	/**
	 * Creates and returns a copy of this {@code Object}. The default
	 * implementation returns a so-called "shallow" copy: It creates a new
	 * instance of the same class and then copies the field values (including
	 * object references) from this instance to the new instance. A "deep" copy,
	 * in contrast, would also recursively clone nested objects. A subclass that
	 * needs to implement this kind of cloning should call {@code super.clone()}
	 * to create the new instance and then create deep copies of the nested,
	 * mutable objects.
	 *
	 * @return a copy of this object.
	 * @throws CloneNotSupportedException if this object's class does not implement the {@code
	 *                                    Cloneable} interface.
	 */
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

	/*
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DataBaseExchange)) return false;
		DataBaseExchange that = (DataBaseExchange) o;
		return getError_no() == that.getError_no() && isPending() == that.isPending() && getUrl().equals(that.getUrl()) && getCommand().equals(that.getCommand()) && getAccountEmail().equals(that.getAccountEmail()) && getFull_name().equals(that.getFull_name()) && getHash().equals(that.getHash()) && getJson_data_in().equals(that.getJson_data_in()) && getJson_data_out().equals(that.getJson_data_out());
	}

	@Override
	public int hashCode() {
		int result = getUrl().hashCode();
		result = 31 * result + getCommand().hashCode();
		result = 31 * result + getAccountEmail().hashCode();
		result = 31 * result + getFull_name().hashCode();
		result = 31 * result + getHash().hashCode();
		result = 31 * result + getJson_data_in().hashCode();
		result = 31 * result + getJson_data_out().hashCode();
		result = 31 * result + getError_no();
		result = 31 * result + (isPending() ? 1 : 0);
		return result;
	}
  */

	public void clear() {
		try {
			url= new URL("https://www.runtrace.com");
			command = "empty";
			accountEmail = "empty";
			full_name = "empty";
			json_data_in = new JSONObject("{\"key\":\"data\"}");
			json_data_out= new JSONObject("{\"key\":\"data\"}");
			error_no=0;
			pending= false;
		} catch (JSONException | MalformedURLException e) {
			e.printStackTrace();
		}
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
